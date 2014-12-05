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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jgroups.Message;
import org.jgroups.Message.Flag;
import org.portico.bindings.jgroups.Configuration;
import org.portico.bindings.jgroups.Federation;
import org.portico.bindings.jgroups.channel.ControlHeader;
import org.portico.bindings.jgroups.channel.UUIDHeader;
import org.portico.bindings.jgroups.wan.global.Header;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.bithelpers.BitHelpers;

/**
 * This class represents the local connection to the WAN router. Messages can be sent here to be
 * forwarded over the WAN to other clusters running externally. This class also started a separate
 * thread to receive incoming messages from the WAN which it will then package and send out to the
 * local cluster.
 * 
 * ### Sending to the WAN
 * 
 * As messages are received by the JGroups channel listener, they are sent to the local federate
 * to be processed, but also handed off to {@link #forwardToGateway(ControlHeader, Message)} to
 * be send over the WAN.
 * 
 * It is expected that messages will not be given to this class unless WAN-mode is turned on in
 * the RID file for the federate.
 * 
 * 
 * ### Receiving messages from the WAN
 * 
 * Messages are received from the WAN by the inner class, {@link GatewayListener} running in its
 * own separate thread. When received, they are packaged into JGroups `Message` types and handed
 * off to the {@link Channel} class to be sent to the local cluster.
 * 
 * Note that messages are *NOT* immediately processed locally. The Portico JGroups configuration
 * loops all messages back to the local member, so rather than explicitly process them as received,
 * we catch them as they're looped back just as if they came from the local cluster. 
 */
public class LocalGateway
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Federation federation;
	private Logger logger;
	private String fedname;
	private boolean connected;

	// Gateway connection properties
	private Socket socket;
	private DataInputStream instream;
	private DataOutputStream outstream;
	private GatewayListener gatewayListener;

	// Statistics keeping
	private volatile long messagesToWAN = 0;
	private volatile long messagesFromWAN = 0;
	private volatile long bytesToWAN = 0;
	private volatile long bytesFromWAN = 0;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LocalGateway( Federation federation )
	{
		this.federation = federation;
		this.fedname = federation.getFederationName();
		this.logger = Logger.getLogger( "portico.lrc.wan" );
		this.connected = false;

		// Gateway network properties
		this.socket = null;
		this.instream = null;
		this.outstream = null;
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
			this.socket = new Socket( address.getAddress(), address.getPort() );
			this.instream = new DataInputStream( socket.getInputStream() );
			this.outstream = new DataOutputStream( socket.getOutputStream() );
		}
		catch( IOException ioex )
		{
			throw new JRTIinternalError( "Couldn't connect to WAN Router: "+ioex.getMessage(), ioex );
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
			// Start the receiver thread
			//
			this.gatewayListener = new GatewayListener();
			this.gatewayListener.start();
			
			this.connected = true;
		}
		catch( IOException ioex )
		{
			throw new JRTIinternalError( "Problem talking to WAN Router: "+ioex.getMessage(), ioex );
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
		
		//
		// Kill our pipe, we're done
		//
		try
		{
			socket.close();
		}
		catch( Exception e )
		{
			// nfi - just log it and move on, we need to kill the active thread regardless
			e.printStackTrace();
		}

		//
		// Wait for the listener thread to clean itself up
		//
		try
		{
    		gatewayListener.interrupt();
    		gatewayListener.join();
		}
		catch( InterruptedException ie )
		{
			// we're shutting down anyway - ignore this
		}
		
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
			// this is a control message - forward with the appropriate header
			UUID sender = ((UUIDHeader)message.getHeader(UUIDHeader.HEADER)).getUUID();
			switch( header.getMessageType() )
			{
				case ControlHeader.FIND_COORDINATOR:
					relay( Header.FIND_COORD, sender, message.getBuffer() );
					break;
				case ControlHeader.SET_MANIFEST:
					relay( Header.SET_MANIFEST, sender, message.getBuffer() );
					break;
				case ControlHeader.CREATE_FEDERATION:
					relay( Header.CREATE_FEDERATION, sender, message.getBuffer() );
					break;
				case ControlHeader.JOIN_FEDERATION:
					relay( Header.JOIN_FEDERATION, sender, message.getBuffer() );
					break;
				case ControlHeader.RESIGN_FEDERATION:
					relay( Header.RESIGN_FEDERATION, sender, message.getBuffer() );
					break;
				case ControlHeader.DESTROY_FEDERATION:
					relay( Header.DESTROY_FEDERATION, sender, message.getBuffer() );
					break;
				case ControlHeader.GOODBYE:
					break; // no processing of this one
				default:
					logger.warn( "Unknown control message [type="+header.getMessageType()+"]. Ignore." );
			}
		}
	}
	
	/**
	 * Send the given message to the {@link LocalGateway} so that it can be relayed to other
	 * connected hosts. If the message is a connection control one (find coordinator, set
	 * manifest, create, join, etc...) the `sender` must be a value UUID; otherwise it should
	 * be `null`.
	 */
	private void relay( byte header, UUID sender, byte[] message )
	{
		int length = message.length;
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "(LOCAL->WAN) "+Header.toString(header)+
			              ", sender="+sender+", payload="+message.length+"b" );
		}

		try
		{
			synchronized(outstream)
			{
    			// header
    			outstream.writeByte( header );
    			
    			// payload
    			if( sender == null )
    			{
    				// no UUID needed for simple relay messages, just write them
    				outstream.writeInt( message.length );
    				outstream.write( message, 0, message.length );
    				
    				// keep the stats
    				++messagesToWAN;
    				bytesToWAN += (message.length+5);
    			}
    			else
    			{
    				// UUID used for control messages, write the UUID and then the payload
    				outstream.writeInt( 16 + message.length );
    				outstream.write( BitHelpers.uuidToBytes(sender), 0, 16 );
    				outstream.write( message, 0, length );
    				
    				// keep some stats
    				++messagesToWAN;
    				bytesToWAN += (message.length+5);
    			}
			}
		}
		catch( Exception e )
		{
			logger.error( "Problem forwarding message to WAN Router: "+e.getMessage(), e );
		}
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
					// Read the next message from the sender. Pull out the header and then
					// the payload of the packet to follow.
					byte code = instream.readByte();
					byte[] payload = readPayload();

					// keep some stats
					++messagesFromWAN;
					bytesFromWAN += (payload.length+1);

					switch( code )
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
							logger.warn( "Unknown message type received: "+Header.toString(code) );
							break;
					}					
				}
				catch( SocketException se )
				{
					// nothing more to read which means the socket must have closed out.
					// that's our sign to gtfo!
					logger.debug( "Connection to Gateway was closed. Listener thread exiting" );
					return;
				}
				catch( Exception ioex )
				{
					// Failure with the comms pipe, let's shut this puppy down
					ioex.printStackTrace();
					return;
				}
			}
		}

		/**
		 * Reads a message payload from the stream.
		 * 
		 *      (int) length
		 *   (byte[]) payload
		 * 
		 * Returns the payload once complete.
		 */
		private byte[] readPayload() throws Exception
		{
			int length = instream.readInt();
			byte[] buffer = new byte[length];
			instream.readFully( buffer );
			return buffer;
		}

		/**
		 * Received a request to relay a message. Because we ignore our own messages
		 * we need to both forward it to the local channel, but also up our own stack.
		 */
		private void receiveRelay( byte[] payload ) throws Exception
		{
			if( logger.isDebugEnabled() )
				logger.debug( "(WAN->LOCAL) relay("+payload.length+"b)" );

			Message message = new Message( null /*destination*/, null /*source*/, payload );
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
			forwardToChannel( ControlHeader.newCreateHeader(), sender, payload );
		}
		
		private void receiveJoinFederation( byte[] payload ) throws Exception
		{
			UUID sender = BitHelpers.readUUID( payload, 0 );
			byte[] remainder = BitHelpers.readByteArray( payload, 16, payload.length-16 );
			forwardToChannel( ControlHeader.newJoinHeader(), sender, payload );
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
