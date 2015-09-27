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
	// Configuration Options
	private Configuration configuration;
	private int port;
	private String connectionInfo;
	
	// Runtime stuff
	private ServerSocket serverSocket;
	private ConnectionAcceptor connectionAcceptor;
	
	private Repeater repeater;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Server( Configuration configuration )
	{
		this.configuration = configuration;
		this.connectionInfo = "Not Connected";
		
		// blank these out for now - we attach them in connect()
		this.serverSocket = null;
		this.connectionAcceptor = new ConnectionAcceptor();
		
		// message processing
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
		this.serverSocket = new ServerSocket();
		InetSocketAddress sa = new InetSocketAddress( configuration.getAddress(),
		                                              configuration.getPort() );
		this.serverSocket.bind( sa );

		this.connectionInfo = this.serverSocket.toString();
		this.connectionAcceptor = new ConnectionAcceptor();
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
	
	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Private Inner Class: ConnectionAcceptor ////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private class ConnectionAcceptor extends Thread
	{
		public ConnectionAcceptor()
		{
			super( "Connection Acceptor" );
		}
	
		public void run()
		{
			System.out.println( "Ready to accept connections: "+serverSocket.toString() );
			while( Thread.interrupted() == false )
			{
				try
				{
					Socket socket = serverSocket.accept();
					Host host = new Host( socket, repeater );
					host.startup();
					System.out.println( "  (Accepted) Now serving "+socket.getRemoteSocketAddress() );
				}
				catch( Exception e )
				{
					if( Thread.interrupted() )
					{
						break;
					}
					else
					{
						System.out.println( "Error starting host"+e.getMessage() );
						e.printStackTrace();
					}
				}
			}
			
			System.out.println( "Stopped accepting new connections" );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
