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
package org.portico2.common.network.transport.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Message;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.configuration.transport.TcpConfiguration;
import org.portico2.common.network.transport.Transport;
import org.portico2.common.network.transport.TransportType;

public class TcpServerTransport extends Transport
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TcpConfiguration configuration;
	
	// Configuration Options
	private InetSocketAddress socketAddress;
	private String connectionInfo;
	
	// Runtime Components
	private boolean isConnected;
	private ServerSocket serverSocket;
	private ConnectionAcceptor connectionAcceptor;
	
	// Connected Client Properties
	protected List<TcpClientProxy> clients;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpServerTransport()
	{
		super( TransportType.TcpServer );

		this.configuration = null;      // set in configure()
		
		// Configuration Options
		this.socketAddress  = null;     // set in configure()
		this.connectionInfo = null;     // set in configure()
		
		// Runtime Components
		this.isConnected = false;
		this.serverSocket   = null;     // set in startup()
		this.connectionAcceptor = null; // set in startup()
		
		// Connected Clients
		this.clients = new LinkedList<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Lifecycle Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doConfigure( ProtocolConfiguration protocolConfiguration, Connection connection )
		throws JConfigurationException
	{
		this.configuration = (TcpConfiguration)protocolConfiguration;
		
		this.socketAddress = new InetSocketAddress( this.configuration.getAddress(),
		                                            this.configuration.getPort() );
	}

	/**
	 * Open up a server socket to start accepting connection requests. The connection
	 * establishment process is handled by the {@lik ConnectionAcceptor} inner class.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	@Override
	public void open() throws JRTIinternalError
	{
		if( this.isConnected )
			return;
		
		try
		{
			logger.trace( "--- TCP Server Configuration ---" );
			logger.trace( "  >> Listen Address: "+this.configuration.getAddressString() );
			logger.trace( "  >> Listen Port   : "+this.configuration.getPort() );
			logger.trace( "" );
			logger.trace( "Opening server socket and listening for new connection requests" );

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
		logger.trace( "TCP Server connection is open" );
	}
	
	/**
	 * Close out the server socket so that we can't accept any more connections.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during connection
	 */
	@Override
	public void close() throws JRTIinternalError
	{
		if( this.isConnected == false )
			return;

		try
		{
			logger.trace( "Closing server socket and refusing any new connections" );
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

	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Messaging Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void down( Message message )
	{
		// TODO Optimize me - we should only hand off to those representing certain federate Ids

		// Hand off to all the clients to process
		clients.parallelStream().forEach( client -> client.send(message) );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isOpen()
	{
		return this.isConnected;
	}

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
	
	protected Logger getLogger()
	{
		return super.logger;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Private Inner Class: ConnectionAcceptor ////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private class ConnectionAcceptor extends Thread
	{
		private TcpServerTransport server;
		public ConnectionAcceptor( TcpServerTransport server )
		{
			super( "TCP Connection Acceptor" );
			this.server = server;
		}
	
		public void run()
		{
			logger.trace( "Connection acceptor is open; listening for incoming connections" );
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
