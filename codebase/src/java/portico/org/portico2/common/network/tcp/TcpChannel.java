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
package org.portico2.common.network.tcp;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.StringUtils;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.configuration.TcpConnectionConfiguration;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.ResponseCorrelator;

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
	
	// Request/Response Correlation
	private ResponseCorrelator<byte[]> responseCorrelator;

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

		// Request/Response Correlation
		this.responseCorrelator = new ResponseCorrelator<>();

		// Metrics
		this.metrics = new Metrics();
		this.bundler.setMetrics( metrics ); // share our metrics
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void configure( TcpConnectionConfiguration configuration )
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
	 * Send the given data message to the other side of the channel.
	 * 
	 * @param porticoMessage The message to send
	 * @throws JRTIinternalError If we are not connected or there is an I/O problem while sending
	 */
	public final void sendDataMessage( PorticoMessage porticoMessage ) throws JRTIinternalError
	{
		sendRawMessage( Type.DATA_MESSAGE, MessageHelpers.deflate(porticoMessage) );
	}

	/**
	 * Sends the given message as a control request. If the request is NOT from the RTI then
	 * if <b>will block</b> until either a response is returned or timeout has happened. If
	 * the message is from the RTI (as determined by {@link PorticoMessage#isFromRti()}) then
	 * this method will return automatically.
	 * <p/>
	 * This avoids some deadlock situations as the LRC waits for the RTI to acknowledge a message
	 * it sent while the RTI waits for the LRC to also acknowledge a request it sent during the
	 * processing of the LRC's request. 
	 * 
	 * @param request The request to serialize and send
	 * @return The received response
	 * @throws JRTIinternalError If there is no response returned in time 
	 */
	public final void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		PorticoMessage request = context.getRequest();
		
		// Serialize the message
		byte[] payload = MessageHelpers.deflate( request );
		if( request.isAsync() )
		{
			// ASYNC call; just submit and return
			bundler.submit( Type.CONTROL_REQ_ASYNC, payload );
			context.success();
		}
		else
		{
			// SYNC call; send and wait
			// Get a request ID
			int requestId = responseCorrelator.register();
			// Send the message
			bundler.submit( Type.CONTROL_REQ_SYNC, requestId, payload );
			// Wait for the response
			payload = responseCorrelator.waitFor( requestId );
			if( payload == null )
			{
				context.error( new JRTIinternalError("No response received (request:%s) - RTI/Federates still running?",
				                                     request.getType()) );
			}
			else
			{
				context.setResponse( MessageHelpers.inflate(payload,ResponseMessage.class) );
			}
		}
	}
	
	/**
	 * Send a control <b>response</b> message. The first paramter is the ID of the request that
	 * we are responding to. This will populate the sent message with the appropriate headers to
	 * identify the message as a response with the given ID.
	 * 
	 * @param requestId The ID of the request we are responding to.
	 * @param payload The payload of the message we are to send.
	 */
	public final void sendControlResponse( int requestId, byte[] payload )
	{
		bundler.submit( Type.CONTROL_RESP, requestId, payload );
	}

	/**
	 * Send teh given payload as a message of the given type. Primarily used when someone else
	 * has done the serialization (perhaps because it is being sent to a lot of people and you
	 * only want to do it once, in one place). This just offers the message to the bundler with
	 * the given type header.
	 * 
	 * @param type Type of message being sent
	 * @param payload Raw payload to send
	 */
	public final void sendRawMessage( Type type, byte[] payload )
	{
		bundler.submit( type, payload );
	}

	//////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING Methods   /////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////

	private void receive( byte header, int requestId, byte[] payload ) throws IOException
	{
		// Convert the header to a type
		Type type = Type.fromHeader( header );
		
		//
		// Bundle Processing -- TODO OPTIMIZE ME!
		//
		if( type == Type.BUNDLE )
		{
			DataInputStream dis = new DataInputStream( new ByteArrayInputStream(payload) );
			while( dis.available() > 0 )
			{
				byte subHeader = dis.readByte();
				int subRequestId = dis.readInt();
				int subLength = dis.readInt();
				byte[] subPayload = new byte[subLength];
				dis.readFully( subPayload );
				receive( subHeader, subRequestId, subPayload );
			}
		}
		//
		// Individual Message Processing
		//
		else
		{
			// keep some stats
			++metrics.messagesReceived;
			metrics.bytesReceived += (payload.length+1);

			// do some specialized logging
			if( logger.isTraceEnabled() )
			{
				switch( type )
				{
					case CONTROL_REQ_ASYNC: logControlRequest( type, requestId, payload ); break;
					case CONTROL_REQ_SYNC:  logControlRequest( type, requestId, payload ); break;
					default:                logBasicMessage( type, requestId, payload ); break;
				}
			}
			
			// pass the message off for processing
			switch( type )
			{
				case DATA_MESSAGE:       appListener.receiveDataMessage( this, payload ); break;
				case CONTROL_REQ_ASYNC:  appListener.receiveControlRequest( this, requestId, payload ); break;
				case CONTROL_REQ_SYNC:   appListener.receiveControlRequest( this, requestId, payload ); break;
				case CONTROL_RESP:       responseCorrelator.offer( requestId, payload ); break;
				default:                 logger.warn( "Unknown header code received from client: "+header );
			}
		}
	}

	/////////////////////////////////////////////////
	///  Logging Helpers   //////////////////////////
	/////////////////////////////////////////////////
	private final void logBasicMessage( Type type, int requestId, byte[] payload )
	{
		logger.trace( "(incoming) type=%s (id=%d), size=%d, app=%s",
		              type,
		              requestId,
		              payload.length,
		              connectionInfo );
	}

	private final void logControlRequest( Type type, int requestId, byte[] payload )
	{
		PorticoMessage portico = MessageHelpers.inflate( payload, PorticoMessage.class );
		
		logger.trace( "(incoming) type=%s (id=%d), ptype=%s, from=%s, to=%s, size=%d, app=%s",
		              type,
		              requestId,
		              portico.getType(),
		              StringUtils.sourceHandleToString(portico),
		              StringUtils.targetHandleToString(portico),
		              payload.length,
		              connectionInfo );
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
					byte header = instream.readByte();
					int length = instream.readInt(); // messages always come through bundler, so no requestId on outside, just read length
					byte[] payload = new byte[length];
					instream.readFully( payload );

					// process the given payload (individual message or bundle) 
					receive( header, 0, payload );
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
