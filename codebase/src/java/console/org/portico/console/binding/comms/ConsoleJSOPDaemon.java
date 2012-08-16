/*
 *   Copyright 2006 The Portico Project
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
package org.portico.console.binding.comms;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.LinkedList;

import org.apache.log4j.Logger;

import org.portico.console.binding.ConsoleBootstrap;

public class ConsoleJSOPDaemon implements Runnable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ConsoleBootstrap bootstrap;
	private int port;
	private Logger logger;
	private ServerSocket serverSocket;
	
	private List<ConsoleJSOPActiveConnection> active;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ConsoleJSOPDaemon( ConsoleBootstrap bootstrap, int port ) throws IOException
	{
		this.bootstrap = bootstrap;
		this.logger = bootstrap.getLogger();
		this.port = port;
		
		this.active = new LinkedList<ConsoleJSOPActiveConnection>();
		
		// create the server socket
		this.serverSocket = new ServerSocket( this.port );
		logger.debug( "(console) Bootstrap listener active on port [" + this.port + "]" );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This method will start listening on the server socket, creating a new
	 * {@link ConsoleJSOPActiveConnection ConsleJSOPActiveConnection} for each client that contacts 
	 * it. Once it has been created it will store it locally with a list of other running active 
	 * connections and will ask it to start up. It will continue to do this until the 
	 * {@link #shutdown() shutdown()} method manually closes the server socket.
	 */
	public void run()
	{
		/////////////////////////////////////
		// listen for incoming connections //
		/////////////////////////////////////
		try
		{
			while( true )
			{
				// get the socket for the new connection
				Socket socket = serverSocket.accept();
				// FIX: PORT-152: Remove the Nagle Algorithm problem
				socket.setTcpNoDelay( true );

				// log a message about the new connection
				logger.trace( "(console) new connection from [" + 
				              socket.getInetAddress().getHostName() + "]" );
				
				// create a new ConsoleJSOPActiveConnection around it
				ConsoleJSOPActiveConnection temp 
					= new ConsoleJSOPActiveConnection( socket, bootstrap );
				
				// start the connection and store it locally
				this.addConnection( temp );
				temp.start();
			}
		}
		catch( IOException ioex )
		{
			logger.debug( "(console) shutting down server socket" );
		}
	}
	
	/**
	 * This method will shut down the server socket (provided it is active) and then iterate over
	 * each of the active {@link ConsoleJSOPActiveConnection ConsoleJSOPActiveConnection}'s and 
	 * ask them to also shut down.
	 */
	public void shutdown()
	{
		// close off the server socket //
		if( serverSocket != null )
		{
			try
			{
				serverSocket.close();
			}
			catch( IOException ioex )
			{
				// ignore
			}
		}
		
		// shutdown each of the active connections //
		ConsoleJSOPActiveConnection[] connections 
			= this.active.toArray( new ConsoleJSOPActiveConnection[0] );
		
		// Loop through the existing connections
		for( ConsoleJSOPActiveConnection conn : connections )
		{
			// if the existing connection is still alive
			if( conn.isAlive() )
			{
				try
				{
					// shut it down and wait for it to finish
					conn.shutdown();
					while( conn.isAlive() )
					{
						conn.join();
					}
				}
				catch( Exception e )
				{
					// log a message but that's it
					logger.warn( "(console) Problem stopping active connection: "
					             + e.getMessage() );
				}
			}
		}
	}
	
	/**
	 * Add the given connection to our local store. We use this method explicitly so that we can
	 * synchronize it and control concurrent access
	 */
	private synchronized void addConnection( ConsoleJSOPActiveConnection connection )
	{
		this.active.add( connection );
	}
	
	/**
	 * So that references to long dead connection objects are not kept lying around, this method
	 * is provided with the sole intent that the {@link ConsoleJSOPActiveConnection 
	 * ConsoleJSOPActiveConnection} will use it to remove itself when it end processing though 
	 * natural causes (NOT when it is being shutdown)
	 */
	public synchronized void removeConnection( ConsoleJSOPActiveConnection connection )
	{
		this.active.remove( connection );
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
