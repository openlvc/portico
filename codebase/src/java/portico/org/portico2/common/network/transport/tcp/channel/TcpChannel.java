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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.portico.utils.StringUtils;
import org.portico2.common.network.Header;
import org.portico2.common.network.configuration.transport.TcpConfiguration;

/**
 * This class represents a bi-directional channel over which messages can be passed and received.
 */
public class TcpChannel
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private boolean isConnected;
	
	// Network Connection Properties
	private String connectionInfo;
	private DataInputStream instream;
	private DataOutputStream outstream;
	
	// Sending and Receiving
	private Bundler bundler;   // sending
	private Receiver receiver; // receiving
	private ITcpChannelListener appListener;
	
	// Metrics
	private Metrics metrics;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpChannel( ITcpChannelListener appListener )
	{
		this.logger = appListener.provideLogger();
		this.isConnected = false;
		
		// Network Connection Properties
		this.connectionInfo = "none";   // set in configure()
		this.instream = null;           // set in connect()
		this.outstream = null;          // set in connect()

		// Sending and Receiving
		this.receiver = new Receiver();
		this.bundler = new Bundler(logger);
		this.appListener = appListener; 

		// Metrics
		this.metrics = new Metrics();
		this.bundler.setMetrics( metrics ); // share our metrics
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void configure( TcpConfiguration configuration )
	{
		// Set up the bundler
		this.bundler = new Bundler( this.logger );
		this.bundler.setEnabled( configuration.isBundlingEnabled() );
		this.bundler.setTimeLimit( configuration.getBundleMaxTime() );
		this.bundler.setSizeLimit( configuration.getBundleMaxSize() );
		if( configuration.isBundlingEnabled() == false )
			logger.debug( "Message bundling disabled for TCP Channel" );
	}
	
	public void connect( Socket socket, DataInputStream instream, DataOutputStream outstream ) throws IOException
	{
		if( this.isConnected )
			return;
		
		// Set up the streams
		this.connectionInfo = socket.getRemoteSocketAddress().toString();
		this.instream = instream;
		this.outstream = outstream;
		
		// Start the bundler
		this.bundler.startBundler( outstream );
		
		// Set up the receiver and start listening
		this.receiver = new Receiver();
		this.receiver.start();

		this.isConnected = true;
	}

	public void disconnect()
	{
		if( this.isConnected == false )
			return;
		
		// Set the connection status to off - when we close the socket it will
		// notify the app listener that the channel disconnected, and as such,
		// we'll end up back in here in a different thread. Let's just set the
		// connected status to false now to prevent other people trying to
		// repeat the process
		this.isConnected = false;

		// Close the input stream off, which will break the thread out of its listening daze
		// and start its clean-up process
		try
		{
			this.instream.close();
		}
		catch( IOException ioex )
		{
			logger.error( "Exception while closing TCP Channel streams: "+ioex.getMessage(), ioex );
		}
		
		// Stop the receiver from taking any more messages
		this.receiver.interrupt();
		exceptionlessThreadJoin( receiver );

		// Stop the bundler from processing any more connections
		this.bundler.stopBundler();
	}
	
	/**
	 * Join the provided thread, catching any interrupted exception and returning false if
	 * it happens. Return true otherwise.
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

	//////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING Methods   ///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Send the given message to the network. If bundling is enabled this will not necessarily
	 * result in an immediate send.
	 * 
	 * @param payload Raw payload to send
	 */
	public final void send( byte[] payload )
	{
		bundler.submit( payload );
	}

	//////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING Methods   /////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	private final void receiveBundle( byte[] payload ) throws IOException
	{
		int bytesRead = 0;
		while( bytesRead < payload.length )
			bytesRead += receiveSingle( payload, bytesRead );
	}
	
	private final int receiveSingle( byte[] payload, int offset ) throws IOException
	{
		Header header = new Header( payload, offset );
		int messageLength = header.getPayloadLength() + Header.HEADER_LENGTH;
		
		// Extract the contents for this single message from the payload
		// If the payload is the same length as the message, they're one and the same
		byte[] messageOnly = payload;
		if( payload.length != messageLength )
		{
			messageOnly = new byte[messageLength];
			System.arraycopy( payload, offset, messageOnly, 0, messageLength ); // TODO Remove this wasteful copy
		}
		
		// Keep some stats
		++metrics.messagesReceived;
		metrics.bytesReceived += messageLength;
		
		// Log the message
		if( logger.isTraceEnabled() )
		{
			logger.trace( "(incoming) type=%s (id=%d), ptype=%s, from=%s, to=%s, size=%s, app=%s",
			              header.getCallType(),
			              header.getRequestId(),
			              header.getMessageType(),
			              StringUtils.sourceHandleToString( header.getSourceFederate() ),
			              StringUtils.targetHandleToString( header.getTargetFederate() ),
			              messageLength,
			              connectionInfo );
		}
		
		try
		{
    		// Handle the message
    		appListener.receive( this, messageOnly );
		}
		catch( Exception e )
		{
			logger.warn( "Error while processing message: "+e.getMessage() );
			logger.trace( "Exception Details", e );
		}
		
		// return bytes read
		return messageLength;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public Metrics getMetrics()
	{
		return this.metrics;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Private Inner Class: Receiver   ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/** Class responsible for receiving messages from the remote host and passing for processing */
	private class Receiver extends Thread
	{
		public Receiver()
		{
			super( "Receiver" );
		}

		public void run()
		{
			try
			{
    			// Process requests from the client
    			while( Thread.interrupted() == false )
    			{
					// Read the next message from sender
    				int header = instream.readInt();
					int length = instream.readInt(); // messages always come through bundler, so no requestId on outside, just read length
					byte[] payload = new byte[length];
					instream.readFully( payload );

					if( header == 0xcafe )
					{
						// Bundle Received
						receiveBundle( payload );
					}
					else if( header == 0xbabe )
					{
						// Single Message
						logger.warn( "We received a single message... wtf?" );
					}
    			}
			}
			catch( EOFException eof )
			{
				// The connection has been killed
				appListener.disconnected( eof );
			}
			catch( IOException ioe )
			{
				// A problem reading from the client, close connection and stop processing.
				appListener.disconnected( ioe );
			}
		}
	}
}
