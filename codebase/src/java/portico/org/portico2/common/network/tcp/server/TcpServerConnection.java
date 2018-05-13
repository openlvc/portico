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
package org.portico2.common.network.tcp.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.configuration.ConnectionConfiguration;
import org.portico2.common.configuration.TcpConnectionConfiguration;
import org.portico2.common.messaging.CallType;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.network.IConnection;
import org.portico2.common.network.IMessageReceiver;
import org.portico2.common.network.tcp.client.TcpClientConnection;

/**
 * The purpose of the {@link TcpServerConnection} is to hold an open TCP server socket within
 * the RTI and then allow remote {@link TcpClientConnection} instances to attach and start
 * communicating with it.
 */
public class TcpServerConnection implements IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Logger logger;
	private boolean isConnected;
	protected IMessageReceiver receiver;
	
	// Configuration Options
	private TcpConnectionConfiguration configuration;
	private InetSocketAddress socketAddress;
	private String connectionInfo;
	
	// Runtime Components
	private ServerSocket serverSocket;
	private ConnectionAcceptor connectionAcceptor;
	
	// Connected Client Properties
	private List<TcpClientProxy> clients;
	
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpServerConnection()
	{
		this.logger = null;
		this.isConnected = false;
		this.receiver = null;           // set in configure()
		
		// Configuration Options
		this.configuration  = null;     // set in configure()
		this.socketAddress  = null;     // set in configure()
		this.connectionInfo = null;     // set in configure()
		
		// Runtime Components
		this.serverSocket   = null;     // set in startup()
		this.connectionAcceptor = null; // set in startup()
		
		// Connected Clients
		this.clients = new LinkedList<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	///  Connection Lifecycle Methods   ///////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 * @param configuration Configuration data that was extracted from the RID
	 * @param receiver The component that should receive incoming messages from the network medium
	 * @throws JConfigurationException Thown if there is an error in the configuration data
	 */
	@Override
	public void configure( ConnectionConfiguration configuration, IMessageReceiver receiver )
		throws JConfigurationException
	{
		this.receiver = receiver;
		this.logger = LogManager.getFormatterLogger( receiver.getLogger().getName()+".tcp" );
		this.configuration = (TcpConnectionConfiguration)configuration;
		
		this.socketAddress = new InetSocketAddress( this.configuration.getAddress(),
		                                            this.configuration.getPort() );
		
		logger.debug( "--- TCP Server Configuration ---" );
		logger.debug( "  >> Listen Address: "+this.configuration.getAddressString() );
		logger.debug( "  >> Listen Port   : "+this.configuration.getPort() );
	}

	/**
	 * Open up a server socket to start accepting connection requests. The connection
	 * establishment process is handled by the {@lik ConnectionAcceptor} inner class.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	@Override
	public void connect() throws JRTIinternalError
	{
		if( this.isConnected )
			return;
		
		try
		{
    		logger.debug( "Opening server socket and listening for new connection requests" );

    		// re-initialize the socket address just in case
    		this.socketAddress = new InetSocketAddress( this.configuration.getAddress(),
    		                                            this.configuration.getPort() );
    		
    		// open the server socket
    		this.serverSocket = new ServerSocket();
    		this.serverSocket.bind( this.socketAddress );
		}
		catch( IOException ioex )
		{
			throw new JRTIinternalError( "Error starting TCP Server: "+ioex.getMessage(), ioex );
		}

		this.connectionInfo = this.serverSocket.toString();
		this.connectionAcceptor = new ConnectionAcceptor( this );
		this.connectionAcceptor.start();

		this.isConnected = true;
	}
	
	/**
	 * Close out the server socket so that we can't accept any more connections.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	@Override
	public void disconnect() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;

		try
		{
			logger.debug( "Closing server socket and refusing any new connections" );
			// kill the connection acceptor
			this.serverSocket.close();
			this.connectionInfo = "Not Connected";
			
			// stop the acceptor and wait for it to wrap up
			this.connectionAcceptor.interrupt();
			this.connectionAcceptor.join();
		}
		catch( IOException ioex )
		{
			throw new JRTIinternalError( "Error stopping TCP Server: "+ioex.getMessage(), ioex );
		}
		catch( InterruptedException ie )
		{
			// it's time to go anyway; just let it go man
		}
		finally
		{
			this.isConnected = false;
		}
	}

	/**
	 * This connection should only deploy into an RTI, so you know one must be running!
	 */
	@Override
	public boolean findRti()
	{
		return true;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void sendControlRequest( MessageContext context ) throws JRTIinternalError
	{
		// TODO Optimize me - we should only hand off to those representing certain federate Ids

		// Serialize the message (once, for all targets)
		CallType type = context.getRequest().isAsync() ? CallType.ControlAsync : CallType.ControlSync;
		byte[] payload = MessageHelpers.deflate2( context.getRequest(), type, 0 );
		
		// Hand off to all the clients to process
		clients.parallelStream().forEach( client -> client.sendControlRequest(payload) );
	}
	
	@Override
	public void sendDataMessage( PorticoMessage message ) throws JException
	{
		// TODO Optimize me - we should only hand off to those representing certain federate Ids

		// Serialize the message (once, for all targets)
		byte[] payload = MessageHelpers.deflate2( message, CallType.DataMessage, 0 );
		
		// Hand off to all the clients to process
		clients.parallelStream().forEach( client -> client.sendDataMessage(payload) );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Sub-Client Management Methods   ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected void addClient( TcpClientProxy proxy )
	{
		this.clients.add( proxy );
		logger.debug( "Connected TCP client: "+proxy );
	}
	
	public void removeClient( TcpClientProxy proxy )
	{
		this.clients.remove( proxy );
		logger.debug( "Disconnected TCP client: "+proxy );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected Logger getLogger() { return this.logger; }
	protected TcpConnectionConfiguration getConfiguration() { return this.configuration; }
	protected IMessageReceiver getReceiver() { return this.receiver; }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Private Inner Class: ConnectionAcceptor ////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private class ConnectionAcceptor extends Thread
	{
		private TcpServerConnection server;
		public ConnectionAcceptor( TcpServerConnection server )
		{
			super( "TCP Connection Acceptor" );
			this.server = server;
		}
	
		public void run()
		{
			logger.debug( "Ready to accept connections" );
			while( Thread.interrupted() == false )
			{
				try
				{
					Socket socket = serverSocket.accept();
					socket.setTcpNoDelay( true );
					TcpClientProxy proxy = new TcpClientProxy( server, socket );
					proxy.startup();
					logger.info( "(Accepted) Connection ID=%d, ip=%s",
					             proxy.getID(),
					             socket.getRemoteSocketAddress() );
				}
				catch( Exception e )
				{
					if( Thread.interrupted() )
						break;
					else
						logger.error( "Error starting host. "+e.getMessage(), e );
				}
			}

			logger.info( "Stopped accepting new connections, shutting down" );
		}
	}

}
