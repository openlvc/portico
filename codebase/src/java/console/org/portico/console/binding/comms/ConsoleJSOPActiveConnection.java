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

import org.portico.console.binding.ConsoleBootstrap;
import org.portico.console.shared.msg.CONSOLE_RequestMessage;
import com.lbf.commons.messaging.ExceptionMessage;
import com.lbf.commons.messaging.MessageContext;
import com.lbf.commons.messaging.ResponseMessage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.Logger;

/**
 * This class represents an active socket connection between a console client and the RTI console
 * binding. It handles all the semantics of message routing and processing.
 */
public class ConsoleJSOPActiveConnection extends Thread
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static int COUNTER = 0;
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Socket socket;
	private ConsoleBootstrap bootstrap;
	
	private Logger logger;

	// streams //
	private ObjectInputStream inStream;
	private ObjectOutputStream outStream;
		
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ConsoleJSOPActiveConnection( Socket socket, ConsoleBootstrap bootstrap )
	{
		super( "jsopConnection-" + socket.getInetAddress().getHostAddress() + "-" + (++COUNTER) );
		this.socket = socket;
		this.bootstrap = bootstrap;
		this.logger = bootstrap.getLogger();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void run()
	{
		////////////////////////////
		// 1. prepare the streams //
		////////////////////////////
		try
		{
			this.outStream = new ObjectOutputStream( this.socket.getOutputStream() );
			this.inStream  = new ObjectInputStream( this.socket.getInputStream() );
		}
		catch( IOException ioex )
		{
			logger.error( "Error preparing input/output streams: " + ioex.getMessage(), ioex );
			this.closeSocket();
			return;
		}
		
		/////////////////////////////////////
		// 2. listen for incoming messages //
		/////////////////////////////////////
		Object incoming = null;
		try
		{
			while( true )
			{
				//////////////////////////////////
				// 2.1 read the incoming object //
				//////////////////////////////////
				incoming = inStream.readObject();
				if( incoming == null )
				{
					break;
				}
				
				// create the response that process below should fill out
				ResponseMessage response = null;
				
				if( incoming instanceof CONSOLE_RequestMessage == false )
				{
					// INVALID REQUEST //
					response = new ExceptionMessage( new Exception("Message type [" +
					            incoming.getClass() +
					            "] is invalid: expecting CONSOLE_RequestMessage") );
				}
				else
				{
					MessageContext context 
						= new MessageContext( (CONSOLE_RequestMessage)incoming );
					
					this.bootstrap.getConsoleRequestSink().processMessage( context );
					
					response = context.getResponse();
					
				}
				
				////////////////////////////////
				// 2.3 pass back the response //
				////////////////////////////////
				synchronized( outStream )
				{
					outStream.writeObject( response );
				}
			}
		}
		catch( EOFException eof )
		{
			// the end of the connection has been reached, the other side has closed
			logger.debug( "(jsop-console) Console client closed connection" );
			// remove us from the list of active connections in the daemon
			bootstrap.getDaemon().removeConnection( this );
			return;
		}
		catch( SocketException se )
		{
			// FIX: PORT-146: Sent when other end closes sockets (seen on windows but not OS X)
			// This seems to be sent when we close the connection as well (on OS X)
			this.closeSocket();
			// remove us from the list of active connections
			bootstrap.getDaemon().removeConnection( this );
			return;
		}
		catch( IOException ioex )
		{
			// there was a problem working with the stream, log, close and exit
			logger.error( "(jsop-console) communication error: " + ioex.getMessage(), ioex );
			// remove us from the list of active connections in the daemon
			bootstrap.getDaemon().removeConnection( this );
			this.closeSocket();
			return;
		}
		catch( Exception ex )
		{
			// there was some other exception, log it and exit
			logger.error( "(jsop-console) processing error: " + ex.getMessage(), ex );
			// remove us from the list of active connections in the daemon
			bootstrap.getDaemon().removeConnection( this );
			this.closeSocket();
			return;
		}
	}

	/**
	 * Closes off the socket we are attached to
	 */
	private void closeSocket()
	{
		// if the socket is already closed, skip this
		if( socket.isClosed() == true )
		{
			return;
		}
		
		try
		{
			socket.close();
			logger.debug( "(jsop-console) closed connection to console client" );
		}
		catch( IOException i )
		{
			// ignore
		}
	}
	
	/**
	 * Interrupts the running thread and gets it to close the socket connection and shutdown.
	 */
	public void shutdown()
	{
		// FIX: PORT-144:  We can't just interrupt the thread and wait for it to finish because
		//                 it will block on the inStream.readObject() call and we will never be
		//                 able to test the interrupted status of thread. To end processing we
		//                 need to manually close the socket (thus causing an exception in the
		//                 read).
		this.closeSocket();
		// wait for the thread to finish
		while( this.isAlive() )
		{
			try
			{
				this.join();
			}
			catch( InterruptedException ie )
			{
				// ignore
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Private Worker Methods ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////

}
