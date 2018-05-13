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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.StringUtils;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.network.IMessageReceiver;
import org.portico2.common.network.tcp.ITcpChannelListener;
import org.portico2.common.network.tcp.Metrics;
import org.portico2.common.network.tcp.TcpChannel;
import org.portico2.common.network.tcp.Type;

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
	private TcpServerConnection parent;
	private Socket socket;
	private DataInputStream instream;
	private DataOutputStream outstream;
	private TcpChannel channel;
	private IMessageReceiver receiver;
	
	private long hostID;
	private boolean running;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TcpClientProxy( TcpServerConnection server, Socket socket ) throws IOException
	{
		this.logger = server.getLogger();

		// network members
		this.parent = server;
		this.socket = socket;
		this.instream  = new DataInputStream( socket.getInputStream() );
		this.outstream = new DataOutputStream( socket.getOutputStream() );
		this.channel = new TcpChannel( this );
		//this.channel.configure( null );
		this.receiver  = server.getReceiver();

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
		outstream.writeByte( Type.WELCOME.getByteValue() );
		outstream.writeInt( buffer.length );
		outstream.write( buffer );

		//
		// Synchronize on ready code
		// (Send and Receive)
		//
		outstream.writeByte( Type.READY.getByteValue() );
		byte received = instream.readByte();
		if( received != Type.READY.getByteValue() )
		{
			throw new RuntimeException( "Expected code READY but got"+Type.fromHeader(received) );
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
	public void sendControlRequest( byte[] payload )
	{
		try
		{
			channel.sendRawMessage( Type.CONTROL_REQ_ASYNC, payload );
		}
		catch( Exception e )
		{
			logger.warn( "Error while sending control message: "+e.getMessage(), e );
		}
	}
	
	public void sendDataMessage( byte[] payload )
	{
		try
		{
			channel.sendRawMessage( Type.DATA_MESSAGE, payload );
		}
		catch( Exception e )
		{
			logger.warn( "Error while sending data message: "+e.getMessage(), e );
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING Methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A control REQUEST has been recieved from the client on the other end and must now
	 * be actioned. Inflate the message and then hand it off to the {@link IMessageReceiver}
	 * for attention. If the message is ASYNC, we will not return a response. If it is SYNC,
	 * we will get a response and queue it to be written back down the pipeline.
	 *
	 * @param channel   The channel the request came from
	 * @param requestId The request id assigned to this message
	 * @param payload   The raw byte buffer with the serialized contents of the message
	 */
	@Override
	public void receiveControlRequest( TcpChannel channel, int requestId, byte[] payload ) throws JException
	{
		PorticoMessage incoming = MessageHelpers.inflate2( payload, PorticoMessage.class );
		
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
		{
			channel.sendControlResponse( requestId, MessageHelpers.deflate2(context.getResponse(),
			                                                                requestId,
			                                                                incoming) );
		}
	}

	@Override
	public void receiveDataMessage( TcpChannel channel, byte[] payload ) throws JRTIinternalError
	{
		PorticoMessage received = MessageHelpers.inflate2( payload, PorticoMessage.class );
		receiver.receiveDataMessage( received );
		
		parent.sendDataMessage( received );
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
