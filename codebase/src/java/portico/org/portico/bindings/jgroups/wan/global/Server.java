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

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.utils.SystemInformation;

/**
 * Server class has x main purposes:
 * 
 *   1. Start a server socket to accept new incoming connections
 *   1. As new connections are made, start new Clients for them
 *   1. Store and process messages are they are received???
 *   3. When told to exit, shut down all Clients, close the server socket and clean up 
 *
 */
public class Server
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	// Configuration Options
	private Configuration configuration;
	private InetSocketAddress socketAddress;
	private String connectionInfo;
	
	// Runtime stuff
	private ServerSocket serverSocket;
	private ConnectionAcceptor connectionAcceptor;
	
	// Message forwarding
	private Repeater repeater;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Server( Configuration configuration )
	{
		this.logger = LogManager.getFormatterLogger( "portico.wan" );

		// Configuration Options
		this.configuration = configuration;
		this.socketAddress = new InetSocketAddress( configuration.getAddress(),
		                                            configuration.getPort() );
		this.connectionInfo = "Not Connected";
		
		// Runtime properties
		this.serverSocket = null; // set on connect()
		this.connectionAcceptor = new ConnectionAcceptor( this );
		
		// Message forwarding
		this.repeater = new Repeater();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/////////////////////////////////////////////////////////////////
	/////   Lifecycle Methods   /////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	public void startup() throws Exception
	{
		// print some general system information
		logger.info( SystemInformation.getSystemInformationSummary() );
		
		// print some local configuration information
		logger.info( "WAN Router Configuration:" );
		logger.info( "|------------------------------------|" );
		logger.info( "| Address: "+String.format("%-25s |",configuration.getAddress()) );
		logger.info( "|    Port: "+String.format("%-25s |",configuration.getPort()) );
		logger.info( "| Metrics: "+String.format("%-25s |",configuration.recordMetrics()) );
		logger.info( "|------------------------------------|" );
		logger.info( "" );
		logger.info( "Starting Portico WAN Router. Press \"x\" to exit" );
		logger.info( "" );
		
		this.serverSocket = new ServerSocket();
		this.serverSocket.bind( this.socketAddress );

		this.connectionInfo = this.serverSocket.toString();
		this.connectionAcceptor = new ConnectionAcceptor( this );
		this.connectionAcceptor.start();
	}
	
	public void shutdown() throws Exception
	{
		// kill the connection acceptor
		this.serverSocket.close();
		this.connectionInfo = "Not Connected";
		this.connectionAcceptor.interrupt();
		this.connectionAcceptor.join();
	}
	
	/////////////////////////////////////////////////////////////////
	/////   General   ///////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////
	public String getConnectionInformation()
	{
		return this.connectionInfo;
	}
	
	public Repeater getRepeater()
	{
		return this.repeater;
	}
	
	public Configuration getConfiguration()
	{
		return this.configuration;
	}
	
	public Logger getLogger()
	{
		return this.logger;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Private Inner Class: ConnectionAcceptor ////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private class ConnectionAcceptor extends Thread
	{
		private Server server;
		public ConnectionAcceptor( Server server )
		{
			super( "Connection Acceptor" );
			this.server = server;
		}
	
		public void run()
		{
			logger.info( "Ready to accept connections" );
			while( Thread.interrupted() == false )
			{
				try
				{
					Socket socket = serverSocket.accept();
					socket.setTcpNoDelay( true );
					Host host = new Host( server, socket );
					host.startup();
					logger.info( " (Accepted) Connection ID="+host.getID()+
					             ", ip="+socket.getRemoteSocketAddress() );
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
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
