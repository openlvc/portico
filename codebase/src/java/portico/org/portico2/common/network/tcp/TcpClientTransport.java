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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.StringUtils;
import org.portico2.common.network.Connection;
import org.portico2.common.network.ITransport;
import org.portico2.common.network.Message;
import org.portico2.common.network.ProtocolStack;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.configuration.TcpConfiguration;
import org.portico2.common.network.configuration.TransportType;
import org.portico2.common.network.tcp.channel.ITcpChannelListener;
import org.portico2.common.network.tcp.channel.Metrics;
import org.portico2.common.network.tcp.channel.TcpChannel;

public class TcpClientTransport implements ITransport, ITcpChannelListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private boolean isConnected;
	private ProtocolStack protocolStack;
	
	// Configuration Options
	private TcpConfiguration configuration;
	private InetSocketAddress serverAddress;
	private String connectionInfo;

	// Network Connection Properties
	private Socket socket;
	private TcpChannel channel;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpClientTransport()
	{
		this.logger = null;
		this.isConnected = false;
		this.protocolStack = null;      // set in configure()
		
		// Configuration Options
		this.configuration  = null;     // set in configure()
		this.serverAddress  = null;     // set in configure()
		this.connectionInfo = null;     // set in configure()
		
		// Network Connection Properties
		this.socket = null;             // set in connect()
		this.channel = null;            // set in configure()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@Override
	public TransportType getType()
	{
		return TransportType.TcpClient;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Lifecycle Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure( ConnectionConfiguration configuration, Connection connection )
		throws JConfigurationException
	{
		this.logger = connection.getLogger();
		this.configuration = (TcpConfiguration)configuration;
		this.protocolStack = connection.getProtocolStack();

		this.serverAddress = new InetSocketAddress( this.configuration.getAddress(),
		                                            this.configuration.getPort() );
		
		logger.debug( "--- TCP Client Configuration ---" );
		logger.debug( "  >> Server Address: "+this.configuration.getAddressString() );
		logger.debug( "  >> Server Port   : "+this.configuration.getPort() );

		// Sending and Receiving
		this.channel = new TcpChannel( this );
		this.channel.configure( this.configuration );
	}

	/**
	 * Open the socket, perform a small handshake and then wrap the socket in a {@link TcpChannel}. 
	 */
	@Override
	public void open() throws JRTIinternalError
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
			logger.trace( "Connection to RTI server successful: "+address );
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
			int code = instream.readInt();
			if( code != 0xbeef )
			{
				throw new JRTIinternalError( "Failed handshake with TCP Server. Recieved stream header "+
				                             StringUtils.formatAsHex(code)+", expected WELCOME (0xbeef)." );
			}
			
			// read the welcome message
			int size = instream.readInt();
			byte[] welcome = new byte[size];
			instream.read( welcome, 0, size );
			logger.info( "[TcpClient]: "+new String(welcome) );
			
			// got the welcome message, wait for the ready sign
			code = instream.readInt();
			if( code != 0xfeed )
			{
				throw new JRTIinternalError( "Failed handshake with TCP Server. Received stream header "+
				                             StringUtils.formatAsHex(code)+", expected READY (0xfeed)" );
			}
			
			// tell the router that we're ready!
			logger.trace( "Welcome message received, writing back the READY (0xfeed) code" );
			outstream.writeInt( 0xfeed );

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
	public void close() throws JRTIinternalError
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
			logger.trace( "Disconnected socket from RTI server" );
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
	///  Message SENDING Methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void send( Message message )
	{
		channel.send( message.getBuffer() );
	}


	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING Methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void receive( TcpChannel channel, byte[] payload ) throws JRTIinternalError
	{
		// TODO Put this back in
		// Should we even process this?
		//if( receiver.isReceivable(incoming.getTargetFederate()) == false )
		//	return;

		protocolStack.up( new Message(payload) );
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
		this.close();
	}
	
	/**
	 * @return Each listener must provide a logger to the channel.
	 */
	@Override
	public Logger provideLogger()
	{
		return this.logger;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
