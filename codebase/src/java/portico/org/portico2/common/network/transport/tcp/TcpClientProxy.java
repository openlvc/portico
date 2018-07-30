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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.StringUtils;
import org.portico2.common.PorticoConstants;
import org.portico2.common.network.Message;
import org.portico2.common.network.transport.tcp.channel.ITcpChannelListener;
import org.portico2.common.network.transport.tcp.channel.Metrics;
import org.portico2.common.network.transport.tcp.channel.TcpChannel;

public class TcpClientProxy implements ITcpChannelListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// used to give each client an index
	private static AtomicLong ID_GENERATOR = new AtomicLong( 0 );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	
	// network information
	private TcpServerTransport parent;
	private Socket socket;
	private DataInputStream instream;
	private DataOutputStream outstream;
	private TcpChannel channel;
	
	private long hostID;
	private boolean running;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpClientProxy( TcpServerTransport server, Socket socket ) throws IOException
	{
		this.logger = server.getLogger();

		// network members
		this.parent = server;
		this.socket = socket;
		this.instream  = new DataInputStream( socket.getInputStream() );
		this.outstream = new DataOutputStream( socket.getOutputStream() );
		this.channel = new TcpChannel( this );

		this.hostID = ID_GENERATOR.incrementAndGet();
		this.running = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public long getID() { return this.hostID; }
	
	/////////////////////////////////////////////////////////////////////////////////////
	/// Lifecycle Methods  //////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	public void startup() throws IOException
	{
		if( this.running )
			return;
		
		// perform the handshake before we do anything else
		handshake();

		// connect the channel
		this.channel.connect( this.socket, this.instream, this.outstream );

		// register ourselves with the parent
		this.parent.addClient( this );

		// mark us as up and running
		this.running = true;
	}

	private void handshake() throws IOException
	{
		//
		// Welcome Message
		// (Send)
		//
		String welcome = "Portico Router ("+PorticoConstants.RTI_VERSION+"): Your ID"+hostID;
		byte[] buffer = welcome.getBytes();
		outstream.writeInt( 0xbeef );
		outstream.writeInt( buffer.length );
		outstream.write( buffer );

		//
		// Synchronize on ready code
		// (Send and Receive)
		//
		outstream.writeInt( 0xfeed );
		int received = instream.readInt();
		if( received != 0xfeed )
		{
			throw new RuntimeException( "Expected code READY (0xfeed) but got"+StringUtils.formatAsHex(received) );
		}
	}

	public void shutdown()
	{
		if( this.running == false )
			return;

		// pull ourselves out of the processing queue
		this.parent.removeClient( this );
		
		// shutdown the channel
		this.channel.disconnect();
		
		// close the socket connection, just to be sure
		try
		{
			this.socket.close();
		}
		catch( Exception e )
		{
			logger.error( "Exception while closing TCP socket: "+e.getMessage(), e );
		}
		
		this.running = false;

		// user feedback
		Metrics metrics = channel.getMetrics();
		String dataReceived = StringUtils.getSizeString( metrics.bytesReceived, 2 );
		String dataSent = StringUtils.getSizeString( metrics.bytesSent, 2 );
		logger.info( "  (Removed) Connection ID="+hostID+" has disconnected" );
		logger.info( "            Packets From: "+metrics.messagesReceived+" packets, "+dataReceived );
		logger.info( "            Packets Sent: "+metrics.messagesSent+" packets, "+dataSent );
	}

	public boolean isRunning()
	{
		return this.running;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message SENDING Methods   ////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
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
		// pass up the protocol stack and into the RTI
		Message message = new Message( payload );
		parent.up( message );
		
		// We must also loop it around to all the other clients that are attached
		// directly to us. The RTI will loop it around to any other _connections_,
		// but our connection is actually the Server connection (that is our parent),
		// under which a number of clients could be connected. As such, it'll skip
		// over the server connection (because it is technically the origin), thus
		// missing any other client proxies that are connected through it. So we
		// must pick up the slack. We only do this for data messages, as control
		// messages are meant for the RTI
		if( message.getHeader().isDataMessage() )
		{
			for( TcpClientProxy proxy : parent.clients )
				if( proxy != this )
					proxy.send( message );
		}
	}

	@Override
	public void disconnected( Throwable throwable )
	{
		// Channel has disconnected. Socket probably has closed, but we'll close it to be sure
		logger.debug( "TCP Channel has disconnected, closing connection down" );
		this.shutdown();
	}
	
	@Override
	public Logger provideLogger()
	{
		return this.logger;
	}
	
	@Override
	public String toString()
	{
		return socket.getRemoteSocketAddress().toString();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
