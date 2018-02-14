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
package org.portico2.common.network.tcp.client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.StringUtils;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.configuration.ConnectionConfiguration;
import org.portico2.common.configuration.TcpConnectionConfiguration;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.network.IConnection;
import org.portico2.common.network.IMessageReceiver;
import org.portico2.common.network.tcp.ITcpChannelListener;
import org.portico2.common.network.tcp.Metrics;
import org.portico2.common.network.tcp.TcpChannel;
import org.portico2.common.network.tcp.Type;

/**
 * This class represents a TCP connection to an RTI server. It may connect directly to the RTI
 * or it may connect through a Forwarder.
 */
public class TcpClientConnection implements IConnection, ITcpChannelListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private boolean isConnected;
	private IMessageReceiver receiver;
	
	// Configuration Options
	private TcpConnectionConfiguration configuration;
	private InetSocketAddress serverAddress;
	private String connectionInfo;

	// Network Connection Properties
	private Socket socket;
	private TcpChannel channel;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpClientConnection()
	{
		this.logger = null;
		this.isConnected = false;
		this.receiver = null;           // set in configure()
		
		// Configuration Options
		this.configuration  = null;     // set in configure()
		this.serverAddress  = null;     // set in configure()
		this.connectionInfo = null;     // set in configure()
		
		// Network Connection Properties
		this.socket = null;             // set in connect()
		this.channel = null;            // set in connect()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( ConnectionConfiguration connectionConfiguration, IMessageReceiver receiver )
		throws JConfigurationException
	{
		this.receiver = receiver;
		this.logger = LogManager.getFormatterLogger( receiver.getLogger().getName()+".tcp" );
		this.configuration = (TcpConnectionConfiguration)connectionConfiguration;

		this.serverAddress = new InetSocketAddress( this.configuration.getAddress(),
		                                            this.configuration.getPort() );
		
		logger.debug( "--- TCP Client Configuration ---" );
		logger.debug( "  >> Server Address: "+this.configuration.getAddressString() );
		logger.debug( "  >> Server Port   : "+this.configuration.getPort() );

		// Sending and Receiving
		this.channel = new TcpChannel( this );
		this.channel.configure( configuration );
	}
	
	/**
	 * Open the socket, perform a small handshake and then wrap the socket in a {@link TcpChannel}. 
	 */
	@Override
	public void connect() throws JRTIinternalError
	{
		if( this.isConnected )
			return;

		//
		// 1. Connect to the server
		//
		DataInputStream instream = null;
		DataOutputStream outstream = null;
		try
		{
			InetSocketAddress address = this.serverAddress;
			logger.debug( "Opening connection to RTI server on: "+address );

			// create the socket - try and give it decent sized buffers
			this.socket = new Socket();
			this.socket.setTcpNoDelay( true );
			this.socket.connect( address );

			instream  = new DataInputStream( new BufferedInputStream(socket.getInputStream()) );
			outstream = new DataOutputStream( socket.getOutputStream() );
			logger.debug( "Connection to RTI server successful: "+address );
		}
		catch( IOException ioex )
		{
			throw new JRTIinternalError( "Failed to connect to RTI server at address: "+
			                             this.serverAddress, ioex );
		}

		//
		// 2. Complete handshake
		//    - Wait for Welcome message (initial contact)
		//    - Wait for Ready message
		//    - Respond with Ready message
		//
		try
		{
			byte code = instream.readByte();
			Type type = Type.fromHeader( code );
			if( type != Type.WELCOME )
			{
				throw new JRTIinternalError( "Failed handshake with WAN Router. Recieved header "+
				                             type+", expected WELCOME." );
			}
			
			// read the welcome message
			int size = instream.readInt();
			byte[] welcome = new byte[size];
			instream.read( welcome, 0, size );
			logger.info( "[WanRouter]: "+new String(welcome) );
			
			// got the welcome message, wait for the ready sign
			code = instream.readByte();
			type = Type.fromHeader( code );
			if( type != Type.READY )
			{
				throw new JRTIinternalError( "Failed handshake with WAN Router. Received header "+
				                             code+", expected READY" );
			}
			
			// tell the router that we're ready!
			logger.trace( "Welcome message received, writing back the READY code" );
			outstream.writeByte( Type.READY.getByteValue() );

			//
			// Connect the channel wrapper around the socket
			//
			this.channel.connect( socket, instream, outstream );
			
			// all done!
			this.isConnected = true;
		}
		catch( IOException e )
		{
			throw new JRTIinternalError( "Problem connecting to WAN Router: "+e.getMessage(), e );
		}
	}
	
	/**
	 * Close off the {@link TcpChannel} and the underlying socket.
	 */
	@Override
	public void disconnect() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;

		// Stop the channel
		this.channel.disconnect();
		
		//
		// Kill our pipe, we're done
		//
		try
		{
			logger.debug( "Disconnected socket from RTI server" );
			socket.close();
		}
		catch( Exception e )
		{
			// nfi - just log it and move on, we need to kill the active thread regardless
			logger.warn( "Error while disconnecting from RTI server", e );
		}

		// log some parting metrics
		logger.info( "TCP Connection shutdown." );
		Metrics metrics = channel.getMetrics();
		String bytesSent = StringUtils.getSizeString( metrics.bytesSent );
		String bytesReceived = StringUtils.getSizeString( metrics.bytesReceived );
		logger.info( "       Sent: "+bytesSent+" ("+metrics.messagesSent+" messages)" );
		logger.info( "   Received: "+bytesReceived+" ("+metrics.messagesReceived+" messages)" );
		
		// Annnnnnnnd, we're done
		this.isConnected = false;
	}


	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING Methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receiveControlRequest( TcpChannel channel, int requestId, byte[] payload ) throws JException
	{
		PorticoMessage incoming = MessageHelpers.inflate( payload, PorticoMessage.class );
		
		// Should we even process this?
		if( receiver.isReceivable(incoming.getTargetFederate()) == false )
			return;

		// Wrap the incoming request in a message context and hand it off for processing
		MessageContext context = new MessageContext( incoming );
		receiver.receiveControlRequest( context );
		
		if( context.hasResponse() == false )
			logger.warn( "No response received for Control Request "+incoming.getIdentifier() );

		// If the incoming message is async, don't send a response
		if( incoming.isAsync() == false )
			channel.sendControlResponse( requestId, MessageHelpers.deflate(context.getResponse()) );
	}

	/**
	 * A data message has been received on the given channel for processing.
	 * 
	 * @param channel The channel it was received on
	 * @param payload The raw byte payload that was received
	 * @throws JRTIinternalError Throw this if there is an error and the channel will log it
	 */
	@Override
	public void receiveDataMessage( TcpChannel channel, byte[] payload ) throws JRTIinternalError
	{
		PorticoMessage received = MessageHelpers.inflate( payload, PorticoMessage.class );
		receiver.receiveDataMessage( received );
	}

	/**
	 * This method is called when the channel has disconnected for any reason
	 * 
	 * @param throwable Exception causing the disconnection (may be null)
	 */
	@Override
	public void disconnected( Throwable throwable )
	{
		// Channel has disconnected. Socket probably has closed, but we'll close it to be sure
		logger.debug( "TCP Channel has disconnected, closing connection down" );
		this.disconnect();
	}
	
	/**
	 * @return Each listener must provide a logger to the channel.
	 */
	@Override
	public Logger provideLogger()
	{
		return this.logger;
	}



	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Forward the request to the channel. Will block if this is an Async request
	 */
	@Override
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		channel.sendControlRequest( context );
	}
	
	/**
	 * Forward the message to the channel.
	 */
	@Override
	public void sendDataMessage( PorticoMessage message ) throws JException
	{
		channel.sendDataMessage( message );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}