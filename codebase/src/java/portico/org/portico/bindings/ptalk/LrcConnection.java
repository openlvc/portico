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
package org.portico.bindings.ptalk;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.portico.bindings.ConnectedRoster;
import org.portico.bindings.IConnection;
import org.portico.bindings.ptalk.channel.Channel;
import org.portico.bindings.ptalk.channel.IPacketReceiver;
import org.portico.bindings.ptalk.channel.Packet;
import org.portico.bindings.ptalk.protocol.FederationManagement;
import org.portico.bindings.ptalk.transport.UdpTransport;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.services.federation.msg.CreateFederation;
import org.portico.lrc.services.federation.msg.DestroyFederation;
import org.portico.lrc.services.federation.msg.JoinFederation;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.messaging.PorticoMessage;

/**
 * This class provides the necessary infrastructure required to hook the PTalk communcations
 * library in with the main Portico framework.
 */
public class LrcConnection implements IConnection
{
	
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRC lrc;
	private Logger logger;
	
	/** A map of all the federations we are connected to, indexed by their names. To connect to
	    a federation (which is distinct from "joining" it) we just need to query about it. This
	    could be a call to create or destroy it etc... */
	private Map<String,Channel> connections;
	private Channel adminChannel; // a channel for admin gossip
	private Channel activeChannel; // the channel that represents the federation we're connected to

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LrcConnection()
	{
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// lifecycle methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Configures the PTalk connection. This will create a {@link Channel} for connecting to the
	 * administrative backbone. Note that the admin channel is not yet opened, it is just created.
	 */
	public void configure( LRC lrc, Map<String,Object> properties ) throws JConfigurationException
	{
		this.logger = Common.getLogger();
		this.lrc = lrc;
		this.connections = new HashMap<String,Channel>();

		logger.debug( "LrcConnection.configure()" );
		
		// create the admin channel, but don't connect to it yet
		// we need to give it a different protocol set, since we want it to handle different tasks
		String adminStack = "GM,FederationManagement";
		properties.put( Common.PROP_STACK, adminStack );
		this.adminChannel = new Channel( "admin", new NullReceiver(), properties );
		
		logger.debug( "LrcConnection configuration complete" );
	}
	
	/**
	 * Establishes the connection to the admin channel and readies the LrcConnection for incoming
	 * requests.
	 */
	public void connect() throws JRTIinternalError
	{
		logger.debug( "LrcConnection connecting" );
		this.activeChannel = null;
		this.adminChannel.connect();
		logger.debug( "LrcConnection established" );
	}
	
	/**
	 * Disconnects the connection from all channels, including the admin channel.
	 */
	public void disconnect() throws JRTIinternalError
	{
		logger.debug( "LrcConnection disconnecting" );
		// remove the active channel
		this.activeChannel = null;
		
		// loop through all open channels and disconnect from them
		logger.debug( "Disconnecting from ["+connections.size()+"] regular channels" );
		for( String federationName : connections.keySet() )
			connections.get(federationName).disconnect();

		// disconnect from the admin channel
		logger.debug( "Disconnect from the admin channel" );
		this.adminChannel.disconnect();
		
		logger.debug( "LrcConnection disconnected" );
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// message sending methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will convert the message into a {@link Packet} and then offer it to the
	 * {@link Brain} for transmission.
	 */
	public void broadcast( PorticoMessage message ) throws Exception
	{
		byte[] data = MessageHelpers.deflate( message );
		Packet packet = new Packet( data );
		activeChannel.queueForSending( packet );
	}
	
	/**
	 * This method will convert the message into a {@link Packet} and offer it to the
	 * {@link Brain} for transmission. It will then wait until a response to that specific
	 * message has been received. FIXME Or will it? It should just wait an "appropriate" amount
	 * of time for the message to be received back and then return. Obviously is we can guarantee
	 * that it won't return until the response is received, that's even better.
	 */
	public void broadcastAndSleep( PorticoMessage message ) throws Exception
	{
		broadcast( message );
		PorticoConstants.sleep( 1000 ); // FIXME
	}

	/**
	 * This method is to be called by local components when a message is ready to be passed
	 * back to the LRC. It should inflate the packet into a PorticoMessage and hand it off
	 * to the LRC for processing.
	 */
	protected void receiveMessage( Packet packet )
	{
		// inflate the packet
		PorticoMessage payload = MessageHelpers.inflate( packet.getPayload(),
		                                                 PorticoMessage.class,
		                                                 lrc );
		
		// inflate and add
		this.lrc.getState().getQueue().offer( payload );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// bootstrapping methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 */
	public void createFederation( CreateFederation createMessage ) throws Exception
	{
		String federationName = createMessage.getFederationName();
		logger.trace( "REQUEST createFederation: name=" + federationName );
		
		// check to see if we already have a connection to that channel
		Channel channel = connections.get( federationName );
		if( channel == null )
		{
			// we don't have a connection to the channel, pop a request into the admin channel
			// to find out if a channel for this federation exists yet or not
			Packet locateRequest = FederationManagement.locateChannel( federationName );
			Packet locateResponse = adminChannel.sendAndWait( locateRequest );
			if( locateResponse == null )
			{
				throw new JRTIinternalError( "Timeout contacting channel management index - "+
				                             "can't create federation channel" );
			}
			
			// extract the channel location from the response
			Object[] responseData = Common.deserialize( locateResponse.getPayload(), 2 );
			String address = (String)responseData[0];
			int port = (Integer)responseData[1];
			
			// connect to the new channel
			HashMap<String,Object> properties = new HashMap<String,Object>();
			properties.put( UdpTransport.PROP_MULTICAST_ADDRESS, address );
			properties.put( UdpTransport.PROP_MULTICAST_PORT, ""+port );
System.out.println( "SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS" );
			channel = new Channel( federationName, new NullReceiver(), properties );
			channel.connect();
			connections.put( federationName, channel );
			logger.fatal( "NEW CHANNEL!!!: "+address+":"+port );
			
			// START HERE
		}
		
		// now that we have a reference to the channel, see if it already has a FOM. If it does
		// this would indicate that there is an active federation going on there
		if( channel.getRoster().getFOM() != null )
		{
			logger.info( "FAILURE createFederation: already exists, name="+federationName );
			throw new JFederationExecutionAlreadyExists( "federation exists: " + federationName );
		}
		
		// serialize the packet & dump it into the brain for processing
		// before we do, make sure we mark it with the appropriate header
		//byte[] data = MessageHelpers.deflate( createMessage );
		//Packet packet = new Packet( data );
		//packet.getHeaders().addHeader( Headers.GROUP_MANAGEMENT, GroupManagement.CREATE );
		//channel.queueForSending( packet );
		
		logger.info( "SUCCESS createFederation: name=" + federationName );
		logger.info( "--------> LIES!! Federation not really created yet. I'm getting there!" );
	}
	
	/**
	 * 
	 */
	public void destroyFederation( DestroyFederation destoryMessage ) throws Exception
	{
		
	}
	
	/**
	 * 
	 */
	public ConnectedRoster joinFederation( JoinFederation joinMessage ) throws Exception
	{
		return null;
	}
	
	/**
	 * 
	 */
	public void resignFederation( ResignFederation resignMessage ) throws Exception
	{
		
	}
	
	/**
	 * 
	 */
	public String[] listActiveFederations() throws Exception
	{
		return new String[]{ "not supported" };
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	/////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Private Class: LrcReceiver /////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This class takes an incoming packet and routes it to the locally configured LRC
	 */
	//private class LrcReceiver implements IPacketReceiver
	//{
	//	public void receive( Packet packet )
	//	{
	//		receiveMessage( packet );
	//	}
	//}

	/////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Private Class: NullReceiver ////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This class takes an incoming packet and ignors it
	 */
	private class NullReceiver implements IPacketReceiver
	{
		public void receive( Packet packet )
		{
			// ignore it
		}
	}
}
