/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.transport;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.portico.bindings.ptalk.Common;
import org.portico.bindings.ptalk.channel.Channel;
import org.portico.bindings.ptalk.channel.Packet;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;

public class UdpTransport implements ITransport
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////
	//////// System properties for configurable values /////////
	////////////////////////////////////////////////////////////
	/** Multicast address to bind to */
	public static final String PROP_MULTICAST_ADDRESS = "portico.ptalk.udp.address";
	/** Multicast socket to bind to - this is ADMIN port, federation port increment starting here */
	public static final String PROP_MULTICAST_PORT = "portico.ptalk.udp.port";
	/** Size of systems UDP send buffer */
	public static final String PROP_UDP_SEND_BUFFER_SIZE = "portico.ptalk.udp.sendBuffer";
	/** Size of systems UDP receive buffer */
	public static final String PROP_UDP_RECV_BUFFER_SIZE = "portico.ptalk.udp.receiveBuffer";
	
	////////////////////////////////////////////////////////////
	//////// Default values for configurable properties ////////
	////////////////////////////////////////////////////////////
	/** The default multicast address to send traffic on */
	public static final String DEFAULT_MULTICAST_ADDRESS = "228.10.10.11";
	
	/** The default multicast port */
	public static final String DEFAULT_MULTICAST_PORT = "20913";
	
	
	/** Used by the multicast packet receiver to create buffers to receive packets into */
	private static final int UDP_RECEIVE_BUFFER_SIZE = 65536;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private Channel channel;
	
	// sending socket information
	// we use a datagram socket for sending because it'll stamp specific host/port information
	// on all outgoing packets, allowing us to properly identify where they came from (even for
	// multiple instances on the same host). If we just use a multicast
	private DatagramSocket sendingSocket;
	private InetSocketAddress sendingAddress;
	
	// multicast socket information
	private MulticastSocket receivingSocket;
	private InetAddress multicastAddress;
	private int multicastPort;
	private int sendBufferSize;
	private int recvBufferSize;
	
	// incoming message processing
	private MessageReceiver messageReceiver;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UdpTransport()
	{
		this.sendBufferSize = 65507*10; // default value for Mac OS X * 100 messages
		this.recvBufferSize = 65507*10; // default value for Mac OS X * 100 messages
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	///////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Lifecycle Methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	public void configure( Channel channel, Map<String,Object> configurationProperties )
		throws JConfigurationException
	{
		this.logger = Common.getLogger();
		this.channel = channel;
		logger.debug( "Configuring new instance of UDP Transport" );
		
		// multicast address configuration
		try
		{
			String addressString = (String)configurationProperties.get( PROP_MULTICAST_ADDRESS );
			if( addressString == null ) addressString = DEFAULT_MULTICAST_ADDRESS;
			logger.trace( "...multicast address: " + addressString );
			this.multicastAddress = InetAddress.getByName( addressString );
		}
		catch( UnknownHostException uhe )
		{
			logger.error( uhe );
			throw new JConfigurationException( uhe );
		}
		
		// multicast port configuration
		String portString = (String)configurationProperties.get( PROP_MULTICAST_PORT );
		if( portString == null ) portString = DEFAULT_MULTICAST_PORT;
		logger.trace( "...multicast port: "+portString );
		this.multicastPort = Integer.parseInt( portString );

		// send and receive buffer sizes
		String sendBufferString = System.getProperty( PROP_UDP_SEND_BUFFER_SIZE );
		if( sendBufferString != null )
			this.sendBufferSize = Integer.parseInt( sendBufferString );
		
		String recvBufferString = System.getProperty( PROP_UDP_RECV_BUFFER_SIZE );
		if( recvBufferString != null )
			this.recvBufferSize = Integer.parseInt( recvBufferString );
	}
	
	public void connect() throws JRTIinternalError
	{	
		//////////////////////////////////////////
		// create the socket and join the group //
		//////////////////////////////////////////
		try
		{
			logger.debug( "ATTEMPT Connect to multicast group "+multicastAddress+":"+multicastPort );
			
			// set up the sending socket - we use a Datagram socket for sending to ensure that
			// a unique ip/port combination is stamped on each datagram (rather than that of the
			// generic multicast address)
			this.sendingSocket = new DatagramSocket();
			this.sendingAddress = new InetSocketAddress( InetAddress.getLocalHost(),
			                                             sendingSocket.getLocalPort() );
			this.sendingSocket.setSendBufferSize( this.sendBufferSize );
			this.sendingSocket.setBroadcast( true );

			// set up the receiving socket (multicast)
			this.receivingSocket = new MulticastSocket( multicastPort );
			this.receivingSocket.joinGroup( multicastAddress );
			this.receivingSocket.setBroadcast( true );
			this.receivingSocket.setLoopbackMode( false ); // we loop back manually
			// SKIPPING soTimeout - controls how long receive waits for a callback
			// SKIPPING setTrafficClass
			this.receivingSocket.setTimeToLive( 8 ); // give it a little room to breath, 4 too small
			//this.receivingSocket.setSendBufferSize( this.sendBufferSize );
			this.receivingSocket.setReceiveBufferSize( this.recvBufferSize );
			
			// log out the values that are in use
			logger.trace( "        Socket properties:" );
			logger.trace( "         -> ------ Sending Socket (DatagramSocket) ------" );
			logger.trace( "         -> (send)      Address = "+sendingAddress );
			logger.trace( "         -> (send)    Broadcast = "+sendingSocket.getBroadcast() );
			logger.trace( "         -> (send)   SendBuffer = "+sendingSocket.getSendBufferSize() );
			logger.trace( "         -> (send)   RecvBuffer = "+sendingSocket.getReceiveBufferSize() );
			logger.trace( "         -> ------ Receiving Socket (MulticastSocket) ------" );
			logger.trace( "         -> (recv)      Address = "+multicastAddress+":"+multicastPort );
			logger.trace( "         -> (recv)    Broadcast = "+receivingSocket.getBroadcast() );
			logger.trace( "         -> (recv)     Loopback = "+receivingSocket.getLoopbackMode() );
			logger.trace( "         -> (recv)          TTL = "+receivingSocket.getTimeToLive() );
			logger.trace( "         -> (recv) TrafficClass = "+receivingSocket.getTrafficClass() );
			logger.trace( "         -> (recv)   SendBuffer = "+receivingSocket.getSendBufferSize() );
			logger.trace( "         -> (recv)   RecvBuffer = "+receivingSocket.getReceiveBufferSize() );
		}
		catch( IOException ioex )
		{
			logger.error( ioex );
			throw new JRTIinternalError( "Couldn't connect to multicast group: "+ioex.getMessage(),
			                             ioex );
		}
		
		logger.debug( "SUCCESS Connected to multicast group" );
		
		///////////////////////////////////////////
		// start listening for incoming messages //
		///////////////////////////////////////////
		this.messageReceiver = new MessageReceiver();
		this.messageReceiver.start();
	}
	
	public void disconnect() throws JRTIinternalError
	{
		// disconnect from the multicast group
		try
		{
			logger.debug( "ATTEMPT Disconnecting from multicast group" );
			this.sendingSocket.close();
			this.receivingSocket.leaveGroup( multicastAddress );
			this.receivingSocket.disconnect();
			logger.debug( "SUCCESS Disconnected from multicast group" );
		}
		catch( IOException ioex )
		{
			logger.error( ioex );
			throw new JRTIinternalError( "Problem leaving multicast group"+ioex.getMessage(), ioex );
		}
	}

	public InetSocketAddress getAddress()
	{
		return sendingAddress;
	}

	/**
	 * Returns the socket address of the multicast group that this transport is connected to
	 */
	public InetSocketAddress getGroupAddress()
	{
		return new InetSocketAddress( multicastAddress, multicastPort );
	}

	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Message Sending Methods /////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	public void sendToNetwork( Packet packet ) throws RuntimeException
	{
		// toss this packet out onto the network
		byte[] payload = packet.marshal();
		DatagramPacket datagram = new DatagramPacket( payload,
		                                              payload.length,
		                                              multicastAddress,
		                                              multicastPort );
		
		try
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "Sending packet from /"+sendingAddress.getAddress().getHostAddress()+":"+
				              sendingAddress.getPort()+" ["+datagram.getLength()+" bytes, headers="+
				              packet.getHeaders()+"] to ["+multicastAddress+":"+multicastPort+"]" ); 
			}
			
			// do the send
			sendingSocket.send( datagram );
		}
		catch( IOException ioex )
		{
			logger.error( ioex );
			throw new RuntimeException( ioex );
		}
		
		// loop it back around to the local federate
		channel.queueForReceiving( packet );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	///////////////////////////////////////////////////////////////////////////////////
	///////////////////////// Private Class: Message Receiver /////////////////////////
	///////////////////////////////////////////////////////////////////////////////////
	private class MessageReceiver extends Thread
	{
		public MessageReceiver()
		{
			super( "MessageReceiver("+sendingSocket.getLocalPort()+")" );
			super.setDaemon( true );
		}

		public void run()
		{
			// This thread will pretty much run forever. The receive() method does not pay any
			// attention to interruptions, so the chances of Thread.interrupted() ever returning
			// true are pretty slim. To kill this thread, we wait for some external component to
			// close the socket on us, causing a SocketException to be thrown.
			while( Thread.interrupted() == false )
			{
				final byte[] buffer = new byte[UDP_RECEIVE_BUFFER_SIZE];
				final DatagramPacket datagram = new DatagramPacket( buffer, buffer.length );
				try
				{
					receivingSocket.receive( datagram );
					
					// skip packets from us
					if( datagram.getSocketAddress().equals(sendingAddress) )
						continue;

					byte[] payload = new byte[datagram.getLength()];
					System.arraycopy( datagram.getData(),
					                  datagram.getOffset(),
					                  payload,
					                  0,
					                  datagram.getLength() );
					Packet ptalkPacket = new Packet();
					ptalkPacket.setSender( (InetSocketAddress)datagram.getSocketAddress() );
					ptalkPacket.unmarshal( payload, 0 );

					if( logger.isTraceEnabled() )
					{
						logger.trace( "Received packet from "+datagram.getSocketAddress()+" ["+
						              datagram.getLength()+" bytes, headers="+ptalkPacket.getHeaders()+"]" ); 
					}

					channel.queueForReceiving( ptalkPacket );
				}
				catch( SocketException se )
				{
					// Socket has closed off. Most probable cause of this is that we're
					// shutting down. Log it and exit.
					
					logger.info( "Multicast socket closed. Exiting. No more messages will be received" );
					return;
				}
				catch( IOException ioex )
				{
					logger.error( "Exception in UDP MessageReceiver: "+ ioex.getMessage(), ioex );
					return;
				}
			}
		}
	}
}
