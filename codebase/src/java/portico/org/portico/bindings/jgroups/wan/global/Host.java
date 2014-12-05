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
package org.portico.bindings.jgroups.wan.global;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.portico.lrc.PorticoConstants;

public class Host
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// used to give each client an index
	private static AtomicLong ID_GENERATOR = new AtomicLong( 0 );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Socket socket;
	private DataInputStream instream;
	private DataOutputStream outstream;
	private BlockingQueue<WanMessage> sendQueue;
	private Repeater repeater;

	private long hostID;
	private boolean running;
	private Thread receiveThread;
	private Thread sendThread;

	// message transmission stats
	private volatile long messagesSentTo;
	private volatile long messagesReceivedFrom;
	private volatile long bytesSentTo;
	private volatile long bytesReceivedFrom;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Host( Socket socket, Repeater repeater ) throws IOException
	{
		this.socket = socket;
		this.instream = new DataInputStream( socket.getInputStream() );
		this.outstream = new DataOutputStream( socket.getOutputStream() );
		this.sendQueue = new LinkedBlockingQueue<WanMessage>();
		this.repeater = repeater;

		this.hostID = ID_GENERATOR.incrementAndGet();
		this.running = false;
		this.receiveThread = null; // set in start, cleared in stop
		this.sendThread = null;    // set in start, cleared in stop

		// Message forwarding counters
		this.messagesSentTo = 0;
		this.messagesReceivedFrom = 0;
		this.bytesSentTo = 0;
		this.bytesReceivedFrom = 0;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public long getID() { return this.hostID; }
	
	/////////////////////////////////////////////////////////////////////////////////////
	/// Lifecycle Methods  //////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////

	public void startup()
	{
		if( this.running )
			return;

		// create our threads
		this.receiveThread = new Thread( new Receiver(), "Receiver["+hostID+"]" );
		this.sendThread = new Thread( new Sender(), "Sender["+hostID+"]" );
		this.receiveThread.start();
		this.sendThread.start();

		// add ourselves to the repeater
		this.repeater.addHost( this );
				
		// mark us as up and running
		this.running = true;
		System.out.println( "Host ["+hostID+"] is up and running" );
	}
	
	public void shutdown()
	{
		if( this.running == false )
			return;

		// pull ourselves out of the processing queue
		this.repeater.removeHost( this );
		
		// shutdown the threads
		this.receiveThread.interrupt();
		this.sendThread.interrupt();
		exceptionlessThreadJoin( receiveThread );
		exceptionlessThreadJoin( sendThread );
		
		// close the socket connection
		try
		{
			this.socket.close();
		}
		catch( Exception e )
		{
			System.err.println( "LOG EXCEPTION: "+e.getMessage() );
			e.printStackTrace();
		}
		
		// clear out our message queue
		// TODO log information here about the number of messages left
		this.sendQueue.clear();
		this.running = false;
	}

	public boolean isRunning()
	{
		return this.running;
	}


	/////////////////////////////////////////////////////////////////////////////////////
	/// Receive Processing  /////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/** Class responsible for receiving messages from the remote host represented by this instance */
	private class Receiver implements Runnable
	{
		public void run()
		{
			try
			{
				// complete the handshake with the new connection
				handshake();
    
    			// Process requests from the client
    			while( Thread.interrupted() == false )
    				receiveLoop();
			}
			catch( IOException ioe )
			{
				// A problem reading from the client, close connection and stop processing.
				finalizeOurselves();
			}
		}
	}

	/**
	 * Waits for the next message to be received and processes it.
	 */
	private void receiveLoop() throws IOException
	{
		// receive the next message
		byte headerCode = instream.readByte();
		int size = instream.readInt();
		byte[] payload = new byte[size];
		instream.readFully( payload );

		// store some stats
		++messagesReceivedFrom;
		bytesReceivedFrom += (size+5);

		// pass the message off for processing
		WanMessage message = new WanMessage( headerCode, this, payload );
		repeater.offer( message );
	}

	private void handshake() throws IOException
	{
		//
		// Welcome Message
		// (Send)
		//
		String welcome = "Portico Router ("+PorticoConstants.RTI_VERSION+"): Your ID"+hostID;
		byte[] buffer = welcome.getBytes();
		outstream.writeByte( Header.WELCOME );
		outstream.writeInt( buffer.length );
		outstream.write( buffer );

		//
		// Synchronize on ready code
		// (Send and Receive)
		//
		outstream.writeByte( Header.READY );
		byte received = instream.readByte();
		if( received != Header.READY )
		{
			throw new RuntimeException( "Expected code READY but got"+Header.toString(received) );
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////
	/// Send Processing  ////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/** Class responsible for sending messages to the remote host that this instance represents */
	private class Sender implements Runnable
	{
		public void run()
		{
			while( Thread.interrupted() == false )
			{
				try
				{
					WanMessage message = sendQueue.take();
					outstream.writeByte( message.getHeader() );
					outstream.writeInt( message.buffer.length );
					outstream.write( message.buffer );
					
					// store some status information we can call up later
					++messagesSentTo;
					bytesSentTo += (message.buffer.length+5);
				}
				catch( InterruptedException ie )
				{
					// Signal that we're shutting down
					break;
				}
				catch( IOException ioex )
				{
					// Bad write - something seriously wrong, disconnect ourselves
					finalizeOurselves();
					break;
				}
			}
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////
	/// General  ////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Put the given message onto the send queue for this host. This will block until
	 * space is available.
	 */
	public void queueForSend( WanMessage message )
	{
		try
		{
			this.sendQueue.put( message ); // will block if there is no space
		}
		catch( InterruptedException ie )
		{
			// if we are interrupted it is likely shutdown time, in which case we don't
			// care about putting things into queues any more, so just let this one go
			return;
		}
	}
	

	/**
	 * Join the provided thread, catching any interrupted exception and returning `false` if
	 * it happens. Return `true` otherwise.
	 */
	private boolean exceptionlessThreadJoin( Thread thread )
	{
		try
		{
			thread.join();
			return true;
		}
		catch( InterruptedException ie )
		{
			return false;
		}
	}

	/**
	 * This method will create a new thread that will spin off and close this host connection
	 * out, including removing us from the `Repeater`. We do this in a separate thread only
	 * for situations where it is one of the threads managed by this class that is initiating
	 * the close - typically in response to an exception in the processing code.
	 * 
	 * We have to do this as a separate thread because the {@link #stop()} method of this class
	 * will not return until both threads have finished, so if we call it within the processing
	 * of one of those threads, they will be blocked waiting for themselves to finish. Smrt.
	 * 
	 * This method is only for use by threads managed by this class.
	 */
	private void finalizeOurselves()
	{
		// Separate thread because the close method won't return until this thread
		// has finished, and this thread won't return until close returns.
		new Thread("Finalizer["+hostID+"]")
		{
			public void run()
			{
				shutdown();
			}
		}.start();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
