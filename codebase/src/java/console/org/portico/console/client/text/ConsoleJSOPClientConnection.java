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
package org.portico.console.client.text;

import com.lbf.commons.component.ComponentException;
import com.lbf.commons.component.IComponent;
import com.lbf.commons.config.ConfigurationException;
import com.lbf.commons.messaging.MessageContext;
import com.lbf.commons.messaging.MessagingException;
import com.lbf.commons.messaging.RequestMessage;
import com.lbf.commons.messaging.ResponseMessage;
import com.lbf.commons.utils.Bag;
import org.portico.console.shared.comms.ConsoleJSOPConstants;
import org.portico.shared.MulticastLookup;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ConsoleJSOPClientConnection implements IComponent
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String PROP_HOST = "client.jsop.host";

	private static final String DEFAULT_HOST = null;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String host;
	private Socket daemonSocket = null;
	private ObjectOutputStream toRTI = null;
	private ObjectInputStream fromRTI = null;
	private String componentName;
	private boolean isExecuting;
	private boolean isShutdown;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ConsoleJSOPClientConnection() 
	{
		this.host = DEFAULT_HOST;
		this.componentName = "console-jsop-connection";
		this.isExecuting = false;
		this.isShutdown = true;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void configure(Bag<String,?> properties) throws ConfigurationException
	{
		this.host = properties.get( PROP_HOST, String.class );
	}

	/**
	 * This method will connect to the RTI console binding and set up the input and output streams 
	 * that communication will flow over. It will also start a separate thread that will sit and 
	 * listen for all incoming messages and route them appropriately.
	 */
	public void execute() throws ComponentException
	{
		/////////////////////////////////////////////////////////////////
		// try and connect to the host, setting up the streams we need //
		/////////////////////////////////////////////////////////////////
		this.daemonSocket = connectToHost(this.host);
		try
		{
			this.toRTI = new ObjectOutputStream(this.daemonSocket.getOutputStream());
			this.fromRTI = new ObjectInputStream(this.daemonSocket.getInputStream());
		}
		catch( IOException ioe )
		{
			throw new ComponentException( "Error preparing streams: " + ioe.getMessage(), ioe );
		}
		
		this.isExecuting = true;
		this.isShutdown = false;
	}
	
	/**
	 * Sends the message contained in the given context to the RTI console binding and places the 
	 * response in the it once received.
	 * 
	 * @throws MessagingException 
	 */
	public ResponseMessage sendMessage( MessageContext message ) throws MessagingException
	{
		// get the request //
		RequestMessage request = message.getRequest();
		
		// pass the message to the RTI and wait for a response //
		try
		{
			// write message to RTI
			toRTI.writeObject( request );
			
			// get the response
			message.setResponse( (ResponseMessage)fromRTI.readObject() );
		}
		catch( IOException ioe )
		{
			throw new MessagingException( "Communication failure with RTI: "+ioe.getMessage(), ioe );
		}
		catch( Exception e )
		{
			throw new MessagingException("Unknown error communicating with RTI: "+e.getMessage(), e);
		}
		
		return message.getResponse();
	}

	/**
	 * This method will attempt to open a JSOP connection to an RTI using the information in the
	 * given parameter. Depending on the value of the given parameter, the process involved will
	 * differ. If the value is:
	 * <ul>
	 *  <li><code>null</code>: The location of the RTI will be discovered via a multicast request
	 *  on the default port (as defined in {@link #DEFAULT_PORT DEFAULT_PORT}.</li>
	 *  <li><code>1234</code>: 1234 is assumed to be the port number to use when sending a
	 *  multicast discovery request. This can be any number (not just 1234) as long as you are
	 *  expecting an RTI to be listening for multicast requests on that port.</li>
	 *  <li><code>192.168.0.1</code>: Where the value is an IP address, it is assumed to be the
	 *  address of the RTI. A direct connection to the JSOP listener running in the RTI will be
	 *  made on the default port (as defined in {@link #DEFAULT_PORT DEFAULT_PORT}.</li>
	 *  <li><code>192.168.0.1:1234</code>: Attempt to make a direct connection to the RTI via
	 *  JSOP on the given IP and port.</li>
	 * </ul>
	 */
	private Socket connectToHost( String theHost ) throws ComponentException
	{
		if( theHost == null )
		{
			///////////////////////////////////////
			// multicast discovery, default port //
			///////////////////////////////////////
			return discoverRTI( ConsoleJSOPConstants.DEFAULT_PORT );
		}
		else if( theHost.contains(":") )
		{
			//////////////////////////////////////
			// direct connection to IP and Port //
			//////////////////////////////////////
			// 192.168.0.1:1234
			// break the string apart into host and port portions
			String host = null;
			int port = -1;
			try
			{
				host = theHost.substring( 0, theHost.indexOf(":") );
				String temp = theHost.substring( theHost.indexOf(":")+1 );
				// convert the port into an integer
				port = Integer.parseInt( temp );
			}
			catch( Exception e )
			{
				throw new ComponentException( "Can't make JSOP connection to RTI console binding [" 
				                              + theHost + "]: " + e.getMessage(), e );
			}
			
			// get the connection
			return directConnect( host, port );
		}
		else if( theHost.contains(".") )
		{
			///////////////////////////////////////////
			// direct connection to IP, default port //
			///////////////////////////////////////////
			// 192.168.0.1
			return directConnect( theHost, ConsoleJSOPConstants.DEFAULT_PORT );
		}
		else
		{
			//////////////////////////////////////
			// multicast discovery, custom PORT //
			//////////////////////////////////////
			// attempt to parse it as the port number
			try
			{
				return discoverRTI( Integer.parseInt(theHost) );
			}
			catch( NumberFormatException nfe )
			{
				throw new ComponentException( "Can't make JSOP connection to RTI console binding: " 
				                              + "Host [" +theHost + "] is invalid" );
			}
		}
	}
	
	/**
	 * Discovers the location and port of the RTI using the JSOP multicast discovery mechanism.
	 * It will then connect to the RTI and return the socket to use. (connection is made via the
	 * {@link #directConnect(String, int) directConnect()} method one the location and port of
	 * the RTI have been discovered).
	 */
	private Socket discoverRTI( int port ) throws ComponentException
	{
		try
		{
			//////////////////////////////////////////////////////////////////
			// 1. lookup the RTI Discovery Registry for binding information //
			//////////////////////////////////////////////////////////////////
			String value = MulticastLookup.lookup( "console" );

			/////////////////////////////////
			// 2. break apart the response //
			/////////////////////////////////
			int first = value.indexOf( ":" );
			String jsopHost = value.substring( 0, first );
			int jsopPort = Integer.parseInt( value.substring(first+1) );
			
			return directConnect( jsopHost, jsopPort );
		}
		catch( Exception e )
		{
			throw new ComponentException( "Could not discover the RTI: (" +e.getClass().getName()+
			                              "): " + e.getMessage(), e );
		}
	}
	
	/**
	 * Connect directly to the JSOPBootstrap running on the given host and port.
	 */
	private Socket directConnect( String host, int port ) throws ComponentException
	{
		// open a connection to the host on the port and return it
		try
		{
			Socket socket = new Socket( host, port );
			socket.setTcpNoDelay( true ); // FIX: PORT-152: stops the Nagle Algorithm problem
			return socket;
		}
		catch( Exception e )
		{
			throw new ComponentException( "Could not connect to server: " + e.getMessage(), e );
		}
	}

	public void shutdown() throws ComponentException
	{
		this.isExecuting = false;
		try
		{
			this.daemonSocket.close();
		}
		catch ( IOException ioe )
		{
			// log the error
		}
		
		this.isShutdown = true;
	}

	public void cleanup() throws ComponentException
	{
		
	}

	public boolean isExecuting()
	{
		return this.isExecuting;
	}

	public boolean isShutdown()
	{
		return this.isShutdown;
	}

	public String getName()
	{
		return this.componentName;
	}

	public void setName( String newName )
	{
		this.componentName = newName;
	}
	
	public String getHost()
	{
		return this.daemonSocket.getRemoteSocketAddress().toString();
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
