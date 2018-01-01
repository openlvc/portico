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
package org.portico.bindings.jgroups.wan.local;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.portico.bindings.jgroups.Configuration;
import org.portico.bindings.jgroups.Federation;
import org.portico.bindings.jgroups.channel.ControlHeader;
import org.portico.bindings.jgroups.channel.UUIDHeader;
import org.portico.bindings.jgroups.wan.global.Header;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.StringUtils;
import org.portico.utils.bithelpers.BitHelpers;

/**
 * This class is the gateway to the wider WAN network for the local connection. Messages sent
 * here are forwarded to other connections via the WAN router.  
 * 
 * ### Lifecycle
 * 
 * All connections will create an instance of this class, but the internal mechanics will not
 * be started unless WAN mode is enabled in the RID file.
 * 
 * This class starts _TWO DISTINCT THREADS_. The first reads incoming messages from the socket
 * that connects us to the WAN router. The other is started by the {@link Bundler} and sends
 * outgoing messages to the router.
 * 
 * 
 * ### Sending to the WAN
 *
 * As the JGroups `ChannelListener` receives messages from the local cluster, they both given
 * to the local federate for processing, but also forwarded here to be sent to the gateway.
 * The method {@link #forwardToGateway(ControlHeader, Message)} does that processing, handing
 * messages off to the {@link Bundler}, which will send them over the WAN in an efficient manner.
 * 
 * 
 * ### Receiving messages from the WAN
 * 
 * Messages are received from the WAN by the `GatewayListener` inner class which is running its
 * own separate thread. When received, they are packaged into JGroups `Message` types and handed
 * off to the {@link Channel} class to be sent to the local cluster.
 * 
 * Note that messages are *NOT* immediately processed locally. The Portico JGroups configuration
 * loops all messages back to the local member, so rather than explicitly process them as received,
 * we catch them as they're looped back just as if they came from the local cluster. 
 */
public class Gateway
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Federation federation;
	private Logger logger;
	private boolean connected;

	// Gateway connection properties
	private Socket socket;
	private DataInputStream instream;
	private DataOutputStream outstream;

	// Sending and Receiving
	private GatewayListener receiver;  // receiving
	private Bundler bundler;           // sending
	
	// Statistics keeping
	private long totalMessagesReceived = 0;
	private long totalBytesReceived = 0;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Gateway( Federation federation )
	{
		this.federation = federation;
		this.logger = LogManager.getFormatterLogger( "portico.lrc.wan" );
		this.connected = false;

		// Gateway network properties
		this.socket = null;
		this.instream = null;
		this.outstream = null;
		
		// Sending and Receiving
		this.receiver = null;
		this.bundler = new Bundler();
		if( Configuration.isWanBundlingEnabled() == false )
		{
			this.bundler.setBundling( false );
			this.logger.debug( "Message bundling disabled for WAN" );
		}

		this.totalMessagesReceived = 0;
		this.totalBytesReceived = 0;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////
	/// Lifecycle Management   ///////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public void connect() throws JRTIinternalError
	{
		if( this.connected )
			return;

		//
		// 1. Connect to the WAN router
		//
		try
		{
			InetSocketAddress address = Configuration.getWanRouter();
			logger.debug( "Opening connection to WAN router: "+address );

			// create the socket - try and give it decent sized buffers
			this.socket = new Socket();
			this.socket.setTcpNoDelay( true );
			this.socket.connect( address );

			this.instream = new DataInputStream( new BufferedInputStream(socket.getInputStream()) );
			this.outstream = new DataOutputStream( socket.getOutputStream() );
			logger.debug( "Connection to WAN router successful: "+address );
		}
		catch( IOException ioex )
		{
			throw new JRTIinternalError( "Failed to connect to WAN Router at address: "+
			                             Configuration.getWanRouter(), ioex );
		}

		//
		// 2. Complete handshake
		//    - Wait for Welcome message (initial contact)
		//    - Wait for Ready message
		//    - Respond with Ready message
		//
		try
		{
			byte code = instream.readByte();
			if( code != Header.WELCOME )
			{
				throw new JRTIinternalError( "Failed handshake with WAN Router. Recieved code "+
				                             Header.toString(code)+", expected WELCOME code" );
			}
			
			// read the welcome message
			int size = instream.readInt();
			byte[] welcome = new byte[size];
			instream.read( welcome, 0, size );
			logger.info( "[WanRouter]: "+new String(welcome) );
			
			// got the welcome message, wait for the ready sign
			code = instream.readByte();
			if( code != Header.READY )
			{
				throw new JRTIinternalError( "Failed handshake with WAN Router. Received code "+
				                             code+", expected READY code" );
			}
			
			// tell the router that we're ready!
			outstream.writeByte( Header.READY );

			//
			// Start the sending and receiving threads
			//
			this.receiver = new GatewayListener();
			this.receiver.start();
			// fire up the bundler for sending
			this.bundler.connect( this.outstream );

			// all done!
			this.connected = true;
		}
		catch( IOException e )
		{
			throw new JRTIinternalError( "Problem connecting to WAN Router: "+e.getMessage(), e );
		}
	}

	/**
	 * Break off the connection with the WAN Router. We close the socket which will cause the
	 * listener thread to sense this, at which point it will exit.
	 */
	public void disconnect()
	{
		if( this.connected == false )
			return;
		
		// Stop sending to the router
		this.bundler.disconnect();

		//
		// Kill our pipe, we're done
		//
		try
		{
			logger.debug( "Disconnected WAN Router socket " );
			socket.close();
		}
		catch( Exception e )
		{
			// nfi - just log it and move on, we need to kill the active thread regardless
			logger.warn( "Error while disconnecting from WAN router", e );
		}

		//
		// Wait for the listener thread to clean itself up
		//
		try
		{
    		receiver.interrupt();
    		receiver.join();
		}
		catch( InterruptedException ie )
		{
			// we're shutting down anyway - ignore this
		}
		
		// log some parting metrics
		logger.info( "WAN Gateway shutdown." );
		String bytesSent = StringUtils.getSizeString( bundler.getSentBytesCount() );
		String messagesSent = StringUtils.getSizeString( bundler.getSentMessageCount() );
		String bytesReceived = StringUtils.getSizeString( totalBytesReceived );
		logger.info( "       Sent: "+bytesSent+" ("+messagesSent+" messages)" );
		logger.info( "   Received: "+bytesReceived+" ("+totalMessagesReceived+" messages)" );
		
		// Annnnnnnnd, we're done
		this.connected = false;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	/// Forwarding TO WAN Methods  ///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Forward the given local JGroups channel message over the gateway to everyone else
	 * on the other end of the WAN.
	 */
	public void forwardToGateway( ControlHeader header, Message message )
	{
		// if there is no control header, this is just a regular message
		if( header == null )
		{
			relay( Header.RELAY, null, message.getBuffer() );
		}
		else
		{
			// Make sure we're connected first. On resign we will immediately disconnect
			// from the WAN for various reasons, so for control messages it isn't a given
			// that we're actually still attached
			if( socket.isConnected() == false )
			{
				logger.fatal( "WAN connection not open. Discarding control message: "+header );
				return;
			}

			// this is a control message - forward with the appropriate header
			UUID sender = ((UUIDHeader)message.getHeader(UUIDHeader.HEADER)).getUUID();
			byte convertedHeader = convertHeader( header );
			if( convertedHeader != -1 )
				relay( convertedHeader, sender, message.getBuffer() );
		}
	}
	
	private byte convertHeader( ControlHeader controlHeader )
	{
		switch( controlHeader.getMessageType() )
		{
			case ControlHeader.FIND_COORDINATOR:   return Header.FIND_COORD;
			case ControlHeader.SET_MANIFEST:       return Header.SET_MANIFEST;
			case ControlHeader.CREATE_FEDERATION:  return Header.CREATE_FEDERATION;
			case ControlHeader.JOIN_FEDERATION:    return Header.JOIN_FEDERATION;
			case ControlHeader.RESIGN_FEDERATION:  return Header.RESIGN_FEDERATION;
			case ControlHeader.DESTROY_FEDERATION: return Header.DESTROY_FEDERATION;
			case ControlHeader.GOODBYE:            return -1; // don't log, but don't process
			default:                               // drop through
		}

		logger.warn( "Unknown header [type="+controlHeader.getMessageType()+"]. Ignore." );
		return -1;
	}

	/**
	 * Send the given message to the {@link Gateway} so that it can be relayed to other
	 * connected hosts. If the message is a connection control one (find coordinator, set
	 * manifest, create, join, etc...) the `sender` must be a value UUID; otherwise it should
	 * be `null`.
	 */
	private synchronized void relay( byte header, UUID sender, byte[] message )
	{
		if( logger.isDebugEnabled() )
		{
			String headerString = Header.toString( header );
			int length = message.length;
			logger.debug( "(LOCAL->WAN) "+headerString+", sender="+sender+", payload="+length+"b" );
		}

		// Give the message to the bundler for sending to the WAN router.
		// If bundler is currently sending, this method will block until it is finished.
		bundler.submit( header, sender, message );
	}

	/////////////////////////////////////////////////////////////////////////////////////
	/// Receive FROM WAN Methods  ///////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This class is responsible for listening and routing messages received from the Gateway.
	 */
	private class GatewayListener extends Thread
	{
		public void run()
		{
			while( Thread.interrupted() == false )
			{
				try
				{
					// Read the next message from sender
					byte code = instream.readByte();
					int length = instream.readInt();
					byte[] payload = new byte[length];
					instream.readFully( payload );

					// process the given payload (individual message or bundle) 
					receive( code, payload );
				}
				catch( SocketException se )
				{
					// nothing more to read which means the socket must have closed out.
					// that's our sign to gtfo!
					logger.debug( "Connection to Gateway was closed. Listener thread exiting" );
					return;
				}
				catch( Exception e )
				{
					// Failure with the comms pipe, let's shut this puppy down
					e.printStackTrace();
					return;
				}
			}
		}

		private void receive( byte header, byte[] payload ) throws Exception
		{
			//
			// Bundle Processing
			//
			if( header == Header.BUNDLE )
			{
				DataInputStream dis = new DataInputStream( new ByteArrayInputStream(payload) );
				while( dis.available() > 0 )
				{
					byte subHeader = dis.readByte();
					int subLength = dis.readInt();
					byte[] subPayload = new byte[subLength];
					dis.readFully( subPayload );
					receive( subHeader, subPayload );
				}
			}
			//
			// Individual Message Processing
			//
			else
			{
				// keep some stats
				++totalMessagesReceived;
				totalBytesReceived += (payload.length+1);

				switch( header )
				{
					case Header.RELAY:
						receiveRelay( payload );
						break;
					case Header.FIND_COORD:
						receiveFindCoordinator( payload );
						break;
					case Header.SET_MANIFEST:
						receiveSetManifest( payload );
						break;
					case Header.CREATE_FEDERATION:
						receiveCreateFederation( payload );
						break;
					case Header.JOIN_FEDERATION:
						receiveJoinFederation( payload );
						break;
					case Header.RESIGN_FEDERATION:
						receiveResignFederation( payload );
						break;
					case Header.DESTROY_FEDERATION:
						receiveDestroyFederation( payload );
						break;
					default:
						logger.warn( "Unknown message type received: "+Header.toString(header) );
						break;
				}		
			}
		}
		
		/**
		 * Received a request to relay a message. Because we ignore our own messages
		 * we need to both forward it to the local channel, but also up our own stack.
		 */
		private void receiveRelay( byte[] payload ) throws Exception
		{
			if( logger.isDebugEnabled() )
				logger.debug( "(WAN->LOCAL) relay("+payload.length+"b)" );

			Message message = new Message( null /*destination*/, payload );
			message.setFlag( Flag.NO_RELAY ); // don't double up!
			federation.getChannel().forwardToChannel( message );
		}

		/**
		 * Received a request to locate the coordinator. Pass it on to the local cluster.
		 */
		private void receiveFindCoordinator( byte[] payload ) throws Exception
		{
			UUID sender = BitHelpers.uuidFromBytes( payload );
			forwardToChannel( ControlHeader.findCoordinator(), sender, new byte[]{} );
		}

		/**
		 * We have received notification of a manifest. These come in response to find
		 * coordinator requests. Pass it to the local channel in case someone on our
		 * local side requested it.
		 */
		private void receiveSetManifest( byte[] payload ) throws Exception
		{
			// read out the UUID & manifest
			UUID sender = BitHelpers.readUUID( payload, 0 );
			byte[] manifest = BitHelpers.readByteArray( payload, 16, payload.length-16 );
			forwardToChannel( ControlHeader.setManifest(), sender, manifest );
		}

		private void receiveCreateFederation( byte[] payload ) throws Exception
		{
			// read out the UUID and remainder of the payload
			UUID sender = BitHelpers.readUUID( payload, 0 );
			byte[] remainder = BitHelpers.readByteArray( payload, 16, payload.length-16 );
			forwardToChannel( ControlHeader.newCreateHeader(), sender, remainder );
		}
		
		private void receiveJoinFederation( byte[] payload ) throws Exception
		{
			UUID sender = BitHelpers.readUUID( payload, 0 );
			byte[] remainder = BitHelpers.readByteArray( payload, 16, payload.length-16 );
			forwardToChannel( ControlHeader.newJoinHeader(), sender, remainder );
		}
		
		private void receiveResignFederation( byte[] payload ) throws Exception
		{
			UUID sender = BitHelpers.readUUID( payload, 0 );
			byte[] remainder = BitHelpers.readByteArray( payload, 16, payload.length-16 );
			forwardToChannel( ControlHeader.newResignHeader(), sender, remainder );
		}
		
		private void receiveDestroyFederation( byte[] payload ) throws Exception
		{
			UUID sender = BitHelpers.readUUID( payload, 0 );
			byte[] remainder = BitHelpers.readByteArray( payload, 16, payload.length-16 );
			forwardToChannel( ControlHeader.newDestroyHeader(), sender, remainder );
		}
		
		/**
		 * Forwards the given message to the local JGroups cluster.
		 */
		private void forwardToChannel( ControlHeader header, UUID sender, byte[] payload )
			throws Exception
		{
			if( logger.isDebugEnabled() )
			{
				logger.debug( "(WAN->LOCAL) "+header+", sender="+sender+
				              ", payload="+payload.length+"b" );
			}

			Message message = new Message();
			message.putHeader( ControlHeader.HEADER, header );
			message.putHeader( UUIDHeader.HEADER, new UUIDHeader(sender) );
			message.setBuffer( payload );
			message.setFlag( Flag.DONT_BUNDLE );
			message.setFlag( Flag.NO_FC );
			message.setFlag( Flag.OOB );
			message.setFlag( Flag.NO_RELAY );
			federation.getChannel().forwardToChannel( message );
		}
	}
	
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
