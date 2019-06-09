/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.common.network.transport.tcp.channel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Logger;
import org.portico.utils.StringUtils;
import org.portico2.common.network.CallType;
import org.portico2.common.network.Header;

/**
 * The Bundler class handles the buffering and flushing of a series of bytes to a
 * {@link DataOutputStream}. It acts as an intermediary between the caller and the
 * stream, and will store up messages so that they can be sent in larger chunks rather
 * than one at a time, making more efficient use of available write-bandwidth.
 * <p/>
 * 
 * The bundler has three properties that control when and how messages are flushed:
 * 
 * <ol>
 *     <li><b>Enabled</b>: If the bundler is disabled, a flush will happen on every call.</li>
 *
 *     <li><b>Max Size</b>: This is the maximum number of bytes that will be buffered before a
 *                          flush is triggered. If a submission pushes the buffer over this limit,
 *                          it will also trigger an instant flush.</li>
 *
 *     <li><b>Max Time</b>: If the bundler is enabled, this is the maximum amount of time that
 *                          data can sit inside the bundler before being flushed. This is to
 *                          ensure that messages don't sit forever, waiting for the last few
 *                          bytes needed to trigger a flush. Time is in millis.</li>
 * </ol>
 * 
 * <b>Flushing a Buffer</b>
 * As per the above, a flush will only be triggered if either the bytes have been sitting around
 * for too long, or the number of bytes in the buffer exceeds the limit (or if bundling is disabled
 * entirely).
 * <p/>
 * 
 * Flushing of the <i>buffer will happen on a <b>separate thread</b></i>. As such, a bundler will
 * not accept messages or process them until after {@link #startBundler()} has been called.
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
	private boolean isEnabled;      // bundle messages or not? if false, flush on every submit
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
	private Metrics metrics;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Bundler( Logger logger )
	{
		this.logger = logger;

		// message queuing
		this.isEnabled = true;
		this.sizeLimit = 64000; // 64k
		this.timeLimit = 20; // 20ms
		this.buffer = ByteBuffer.allocate( (int)(sizeLimit*1.1) );
		this.queuedMessages = 0;
		this.oldestMessage = 0;

		// output writing
		this.lock = new ReentrantLock();
		this.armCondition = this.lock.newCondition();
		this.flushCondition = this.lock.newCondition();
		this.returnCondition = this.lock.newCondition();

		// metrics
		this.metrics = new Metrics();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/** Conencts to the given output stream and starts the sender thread */
	public void startBundler( DataOutputStream outstream )
	{
		if( logger == null )
			throw new IllegalStateException( "You must give the bundler a logger prior to start" );
		
		logger.debug( "[Bundler] Starting. Max bundle size="+StringUtils.getSizeString(sizeLimit)+
		              ", max bundle time="+timeLimit+"ms" );

		this.outstream = outstream;

		// start the sender
		this.senderThread = new Thread( new Sender(), "Bundler-Sender" );
		this.senderThread.setDaemon( true );
		this.senderThread.start();
	}
	
	public void stopBundler()
	{
		// NOTE: Disabling - we can't tell if the connection is open or not.
		//                   If we are stopping because of a disconnection then
		//                   flushing will trigger an exception (duh, connection closed).
		// flush whatever we have in the pipes currently
		//logger.trace( "Flushing "+queuedMessages+" stored messages" );
		//flush();
		if( queuedMessages > 0 )
			logger.warn( "Shutting down bundler with %d messages still queued", queuedMessages );

		// kill the sender thread
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

	////////////////////////////////////////////////////////////////////////////////////////
	///  Bundling Methods   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Submit a message for bundling.
	 * <p/>
	 * 
	 * If the size limit is now broken, a flush will happen and this method will block.
	 * Otherwise it will buffer the request and then return immediately. If this is the first
	 * message since the last flush, the timer will be armed so that if our size cap isn't
	 * broken in a configurable amount of time (20ms by default), a flush will happen anyway.
	 * <p/>
	 * 
	 * This method will block in two instances:
	 *   <ul>
	 *    <li>If the total size of bundled messages exceeds the limit, causing a flush</li>
	 *    <li>If a flush is already under way</li>
	 *   </ul>
	 * 
	 * <b>Disable Bundling</b>
	 * Bundling can be disabled in configuration. If this is the case, submitted messages
	 * will immediately be flushed to the router.
	 * 
	 * <b>Control Sync and Control Response Messages</b>
	 * These two types of messages can be time sensitive. As such, if you submit a message with
	 * either of these headers it will trigger an immediate flush, regardless of queue state.
	 * 
	 * @param message the body of the message to send
	 */
	public void submit( byte[] message )
	{
		lock.lock();

		try
		{
			//
			// queue the message
			//
			growBufferIfNeeded( message.length );
			buffer.put( message );
			queuedMessages++; // metrics
			
//			growBufferIfNeeded( 9+message.length );
//			buffer.put( header.getByteValue() );
//			buffer.putInt( requestId );
//			buffer.putInt( message.length );
//			buffer.put( message );
//			queuedMessages++; // metrics
			
			// log that we've queued the message
			Header header = new Header( message, 0 );
			if( logger.isTraceEnabled() )
				logQueuedMessage( header, message );
			
			// if actual message bundling is turned off, flush right away
			//   -OR-
			// if the header is time critical (ControlSync, or ControlResp)
			if( this.isEnabled == false || header.getCallType() != CallType.DataMessage )
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

	/*
	 * Should only be called if trace is enabled. Does an obscene amount of work just to
	 * generate some better logs.
	 */
	private final void logQueuedMessage( Header header, byte[] payload )
	{
		if( logger.isTraceEnabled() == false )
			return;

		// Log some generation information about the message. If it is a response message,
		// log the result. We can only do this if the message is unencrypted. If it isn't,
		// we have to fall back to the header-only encryption.
//		if( header.isEncrypted() == false && header.getCallType() == CallType.ControlResp )
//		{
//			ResponseMessage response = MessageHelpers.inflate2( payload, ResponseMessage.class );
//			logger.trace( "(outgoing) type=%s (id=%d), success=%s, result=%s, size=%d",
//			              header.getCallType(),
//			              header.getRequestId(),
//			              ""+response.isSuccess(),
//			              response.getResult(),
//			              payload.length );
//		}
//		else
//		{
			// we have the PorticoMessage original, let's log some stuff!
			logger.trace( "(outgoing) type=%s (id=%d), ptype=%s, from=%s, to=%s, size=%d",
			              header.getCallType(),
			              header.getRequestId(),
			              header.getMessageType(),
			              StringUtils.sourceHandleToString( header.getSourceFederate() ),
			              StringUtils.targetHandleToString( header.getTargetFederate()),
			              payload.length );
//		}
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
			outstream.writeInt( 0xcafe );
			outstream.writeInt( bytes );
			outstream.write( buffer.array(), 0, bytes );

			// metrics
			metrics.messagesSent += queuedMessages;
			metrics.bytesSent += bytes;
			
			if( logger.isTraceEnabled() )
				logger.trace( "(outgoing) {FLUSH} %d messages (%s) have been flushed", queuedMessages, bytes );
		}
		catch( IOException ioex )
		{
			logger.error( "Error while flushing bundler: "+ioex.getMessage(), ioex );
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

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean isEnabled()
	{
		return this.isEnabled;
	}
	
	public void setEnabled( boolean isEnabled )
	{
		this.isEnabled = isEnabled;
	}

	/** Max number of bytes held prior to flush (in bytes). Default is 64k. */
	public int getSizeLimit()
	{
		return this.sizeLimit;
	}

	/**
	 * Sets the max size that the buffer can be prior to triggering a flush.
	 * 
	 * @param bytes Max size in bytes
	 */
	public void setSizeLimit( int bytes )
	{
		this.sizeLimit = bytes;
	}
	
	/** Max period of time that bytes are held prior to flush (in millis). Default is 20ms. */
	public int getTimeLimit()
	{
		return this.timeLimit;
	}

	/**
	 * Set the max time that bytes can sit in the buffer prior to triggering a flush.
	 * 
	 * @param millis The max wait time in millis
	 */
	public void setTimeLimit( int millis )
	{
		this.timeLimit = millis;
	}

	public Metrics getMetrics()
	{
		return this.metrics;
	}

	/**
	 * Let someone specify a shared metrics object we should be using.
	 */
	public void setMetrics( Metrics metrics )
	{
		this.metrics = metrics;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

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
				logger.debug( "Sender thread has started up inside the Bundler" );
				
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
				logger.debug( "Bundler Sender thread interrupted; shutting down" );
			}
			finally
			{
				lock.unlock();
			}
		}
	}
}
