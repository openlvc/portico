/*
 *   Copyright 2015 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL) 
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *   
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
package org.portico.bindings.jgroups.wan.local;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.bindings.jgroups.Configuration;
import org.portico.bindings.jgroups.wan.global.Header;
import org.portico.utils.StringUtils;
import org.portico.utils.bithelpers.BitHelpers;

/**
 * This class is responsible for sending all messages to the WAN router. It will bundle a number
 * of messages together to ensure efficient use of network resources and enable higher throughput.
 * This class handles all message sending.
 * 
 * ## Bundling
 * As messages are received, they are stored until one of two trigger conditions is met:
 * 
 *    - The size of the bundled messages exceeds a configurable threashold
 *    - The amount of time we have been holding onto messages exceeds a configurable threshold
 * 
 * To ensure a balance between throughput and latency, we will only hold onto messages for a
 * certain amount of time. At this point they will be flushed to the router, even if the combined
 * size of the messages is less than our bundling limit.
 * 
 * If a message is submitted that causes the bundled size to exceed the threshold, the
 * {@link #submit(byte, UUID, byte[])} method notifies the dedicated sender thread, which then
 * calls {@link #flush()}, sending all the messages to the router. This will cause the call to
 * `submit()` to block until that flush is complete. This helps throttle the sender somewhat to
 * prevent overloading.
 * 
 * ## Disable Bundling
 * Although the default behaviour of this component is to bundle messages, it is also the main
 * interface to the WAN router. When required, the actual bundling of the messages can be turned
 * off, causing every messaegs to be flushed to the WAN router as soon as the call to
 * {@link #submit(byte, UUID, byte[])} is made. See {@link #setBundling(boolean)}.
 */
public class Bundler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	// message queuing
	private boolean bundleMessages; // bundle messages or not - if false, flush on every submit
	private int sizeLimit;          // max bytes to hold onto before release
	private int timeLimit;          // max amount of time to hold onto messages before release
	private ByteBuffer buffer;      // store incoming messages here prior to flush
	private int queuedMessages;     // number of messages we currently have queued
	private long oldestMessage;     // time (millis) when first message turned up in queue

	// output writing
	private DataOutputStream outstream; // connection to the router
	private Lock lock;                  // lock the send/receive processing
	private Condition armCondition;     // tell the timer that it should be ready to flush soon
	private Condition flushCondition;   // triggered when it is time to flush
	private Condition returnCondition;  // triggered when the flush is over
	private Thread senderThread;        // thread that will do all our sending work

	// metrics
	private long totalMessagesSent;
	private long totalBytesSent;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new Bundler around the given output stream. Sender thread will be started
	 * immediately (as a daemon).
	 */
	protected Bundler()
	{
		this.logger = LogManager.getFormatterLogger( "portico.lrc.wan" );

		// message queuing
		this.bundleMessages = true;
		this.sizeLimit = Configuration.getWanBundleSize();
		this.timeLimit = Configuration.getWanBundleTimeout();
		this.buffer = ByteBuffer.allocate( (int)(sizeLimit*1.1) );
		this.queuedMessages = 0;
		this.oldestMessage = 0;

		// output writing
		this.lock = new ReentrantLock();
		this.armCondition = this.lock.newCondition();
		this.flushCondition = this.lock.newCondition();
		this.returnCondition = this.lock.newCondition();

		// metrics
		this.totalMessagesSent = 0;
		this.totalBytesSent = 0;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Submit a message for bundling.
	 * 
	 * If the size limit is now broken, a flush will happen and this method will block.
	 * Otherwise it will return. If this is the first message since the last flush, the timer
	 * will be armed so that if our size cap isn't broken in a configurable amount of time
	 * (20ms by default), a flush will happen anyway.
	 * 
	 * This method will block in two instances:
	 * 
	 *    - If the total size of bundled messages exceeds the limit, causing a flush
	 *    - If a flush is already under way
	 * 
	 * #### Disable Bundling
	 * Bundling can be disabled in configuration. If this is the case, submitted messages
	 * will immediately be flushed to the router.
	 */
	public void submit( byte header, UUID sender, byte[] message )
	{
		lock.lock();

		try
		{			
			//
			// queue the message
			//
			if( sender == null )
			{
				growBufferIfNeeded( 5+message.length );
				// no UUID needed for simple relay messages, just write them
				buffer.put( header );
				buffer.putInt( message.length );
				buffer.put( message );
				
				// metrics
				queuedMessages++;
			}
			else
			{
				growBufferIfNeeded( 21+message.length );				
				// UUID used for control messages, write the UUID and then the payload
				buffer.put( header );
				buffer.putInt( 16 + message.length ); // size
				buffer.put( BitHelpers.uuidToBytes(sender) );
				buffer.put( message );

				// metrics
				queuedMessages++;
			}
			
			// if actual message bundling is turned off, flush right away
			if( this.bundleMessages == false )
			{
				flush();
				return;
			}

			// check if we need to reset the time trigger
			if( this.oldestMessage == 0 )
			{
				this.oldestMessage = System.currentTimeMillis();
				// arm the timer
				armCondition.signalAll();
			}

			// check to see if we've hit the size trigger
			if( buffer.position() > sizeLimit )
			{
				flushCondition.signalAll();
				returnCondition.await();
			}
		}
		catch( InterruptedException ie )
		{
			// only when we're exiting - ignore
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * If the buffer does not have enough space to store the give amount of bytes, grow it
	 * so that it can (with some to spare - currently 10%).
	 */
	private final void growBufferIfNeeded( int spaceRequired )
	{
		if( buffer.remaining() < spaceRequired )
		{
			// Create a new buffer and copy the existing one over
			// This is expensive, so let's hope it is rare!
			int newsize = buffer.capacity() + spaceRequired;
			ByteBuffer newBuffer = ByteBuffer.allocate( (int)(newsize*1.1) ); // 10% elbow room

			// copy the contents of the old buffer over and replace it
			this.buffer.flip();
			newBuffer.put( buffer );
			this.buffer = newBuffer;
		}
	}
	
	/**
	 * Check what's been bundled up to see if it should be released. There are two trigger
	 * conditions for release:
	 * 
	 *   1. We've stored up more than `MAX_BYTES` bytes
	 *   2. We've held messages longer than `MAX_WAIT` time
	 * 
	 * This method can be called from the thread that invokes `submit()` when the size trigger is
	 * tripped, or from the Sender-thread when the time trigger is tripped.
	 */
	private void flush()
	{
		// grab the lock so that stuff isn't jumping into the buffer while we're working
		lock.lock();
		try
		{
			// down the loo!
			int bytes = buffer.position();
			outstream.writeByte( Header.BUNDLE );
			outstream.writeInt( bytes );
			outstream.write( buffer.array(), 0, bytes );

			// metrics			
			totalMessagesSent += queuedMessages;
			totalBytesSent += bytes;
			
			if( logger.isTraceEnabled() )
				logger.trace( "Sent "+bytes+" bytes to WAN ("+queuedMessages+" messages)" );
		}
		catch( IOException ioex )
		{
			logger.error( "Error while sending messages to WAN router: "+ioex.getMessage() );
		}
		finally
		{
			// empty our buffer - it is used in submit() as well
			buffer.clear();
			this.queuedMessages = 0;
			this.oldestMessage = 0;

			this.returnCondition.signalAll();
			lock.unlock();
		}
	}

	/** Conencts to the given output stream and starts the sender thread */
	protected void connect( DataOutputStream outstream )
	{
		logger.debug( "[Bundler] Starting. Max bundle size="+StringUtils.getSizeString(sizeLimit)+
		              ", max bundle time="+timeLimit+"ms" );

		this.outstream = outstream;

		// start the sender
		this.senderThread = new Thread( new Sender(), "WAN-Sender" );
		this.senderThread.setDaemon( true );
		this.senderThread.start();
	}

	/** Shut down the sender thread and exit. Will _NOT_ close the connection */
	protected void disconnect()
	{

		// flush whatever we have in the pipes currently
		lock.lock();
		try
		{
			logger.trace( "Flushing "+queuedMessages+" stored messages" );
			flushCondition.signalAll();
			returnCondition.await( 2000, TimeUnit.MILLISECONDS );
		}
		catch( InterruptedException ie )
		{
			// ignore - what would we do anyway!?
		}
		finally
		{
			lock.unlock();
		}
		
		try
		{
			logger.trace( "Shutting down bundler sending thread" );
			senderThread.interrupt();
			senderThread.join( 2000 );
		}
		catch( InterruptedException ie )
		{
			logger.warn( "Bundler sending thread did not shut down cleanly (2 sec wait)" );
		}

		logger.debug( "Bundler has been shut down" );
	}

	public long getSentMessageCount()
	{
		return this.totalMessagesSent;
	}
	
	public long getSentBytesCount()
	{
		return this.totalBytesSent;
	}

	public boolean isBundling()
	{
		return this.bundleMessages;
	}
	
	public void setBundling( boolean bundle )
	{
		this.bundleMessages = bundle;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	////// Private Class: Sender   ///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	private class Sender extends TimerTask implements Runnable
	{
		public void run()
		{
			lock.lock();
			try
			{
				logger.debug( "WAN Sender thread has started up inside the Bundler" );
				
				while( true )
				{
					// Wait for someone to arm us for sending.
					// We don't want to just busy-loop - we only arm when there are messages
					armCondition.await();
					
					// The flush condition we were waiting for has triggered.
					// Either our wait time expired, or we reached our size threshold
					// and this condition was manually triggered
					//boolean triggered = flushCondition.await( timeLimit, TimeUnit.MILLISECONDS );
					flushCondition.await( timeLimit, TimeUnit.MILLISECONDS );

					//if( triggered )
					//	logger.trace( "Bundler triggered by busting our SIZE cap, flushing" );
					//else
					//	logger.trace( "Bundler triggered by busting our TIME cap, flushing" );
					
					// Do the actual work
					flush();
				}
			}
			catch( InterruptedException ie )
			{
				// We are shutting down and that's cool
				logger.debug( "WAN Sender thread interrupted; shutting down" );
			}
			finally
			{
				lock.unlock();
			}
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
