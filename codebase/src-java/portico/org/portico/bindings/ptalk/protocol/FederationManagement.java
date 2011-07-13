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
package org.portico.bindings.ptalk.protocol;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.portico.bindings.ptalk.Common;
import org.portico.bindings.ptalk.channel.Channel;
import org.portico.bindings.ptalk.channel.Header;
import org.portico.bindings.ptalk.channel.Packet;
import org.portico.lrc.compat.JConfigurationException;

/**
 * The federation management protocol is responsible for maintaining an index of federations, as
 * well as a list of which multicast address/port combinations are used to hold them.
 * <p/>
 * <b>NOTE:</b> This protocol should ONLY be present in the admin channel. Putting it in the
 * protocol stack for every channel has the potential to create large chunks of strangeness, and
 * potentially kill a minimum of one kitten per-packet processed.
 */
@Protocol(name="FederationManagement")
public class FederationManagement extends AbstractProtocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public enum MessageType
	{
		UpdateChannelList,        // Call to update the list of active channels and their names
		                          // This list is maintained by the GM master and reflected by
		                          // all other components
		LocateOrCreateChannel;    // Call to locate the channel, or create it if is doesn't exist
		
		public byte byteValue()
		{
			return (byte)this.ordinal();
		}

		public static MessageType valueOf( byte id )
		{
			return MessageType.values()[id];
		}
	};

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private GroupManagement gm;
	
	// stores the names and locations of active channels
	private HashMap<String,InetSocketAddress> channels;
	
	// used when creating new channels if this is the master
	private InetSocketAddress lastAddress = null;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederationManagement()
	{
		this.channels = new HashMap<String,InetSocketAddress>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void configure( Channel channel, Map<String,Object> properties )
		throws JConfigurationException
	{
		super.configure( channel, properties );
		this.logger = Logger.getLogger( Common.getLogger().getName()+".FederationManagement" );
		
		// when the master has to create a new channel, it does so by using the same multicast
		// address as before, just incrementing the port number. the initial address/port combo
		// is obtained from the UdpTrasport of the channel we're attached to.
		this.lastAddress = channel.getTransport().getGroupAddress();
		
		// make sure we can find the group manager in the protocol stack - anywhere is fine
		this.gm = channel.getPipeline().getParentProtocol( GroupManagement.class, null );
		if( this.gm == null )
		{
			logger.error( "FederationManagement protocol required GM to exist in the stack to "+
			              "function properly" );
			throw new JConfigurationException( "FederationManagement protocol required GM to "+
			                                   "exist in the stack to function properly" );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Outgoing Packet Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 */
	public boolean outgoing( Packet packet ) throws RuntimeException
	{
		// skip out if this packet isn't for us
		if( packet.hasHeader(Header.FederationManagement) == false )
			return true;
		
		// this is a GM packet, process it
		MessageType type = MessageType.valueOf( packet.getHeaderAsByte(Header.FederationManagement) );
		switch( type )
		{
			case LocateOrCreateChannel:
				return localLocateOrCreateChannel( packet );
			default:
				break;
		}

		return true;
	}

	/**
	 * This method will either locate the channel requested by the incoming packet, or request that
	 * it be created. If the local store doesn't know about the channel of the given name, a
	 * request to find or create it will be sent out. The {@link GroupManagement} master is expected
	 * to respond to this.
	 * 
	 * @return This method will return true if processing of the packet should continue (and it
	 *         should be sent out of the network). False if processing should end now.
	 */
	private boolean localLocateOrCreateChannel( Packet packet )
	{
		String channelName = packet.getPayloadAsString();
	
		synchronized( channels )
		{
    		if( logger.isDebugEnabled() )
    			logger.debug( "Request to locate channel ["+channelName+"]" );
    		
    		// if we know about this channel, package up a response packet and queue it
    		// for receiving. if we DON'T know about the channel, we'll have to ask for
    		// a channel list update
    		InetSocketAddress address = channels.get( channelName );
    		if( address != null )
    		{
    			byte[] payload = Common.serialize( address.getAddress().getHostAddress(),
    			                                   address.getPort() );
    			Packet response = new Packet( payload );
    			response.attachSerial( packet );
    			channel.queueForReceiving( response );
    			if( logger.isDebugEnabled() )
    				logger.debug( "Found channel in local store; address="+address );
    			
    			return false; // no more processing required
    		}
    		
    		// we don't know about the channel locally. either it has just been created and we
    		// haven't had a channel list update yet, or it doesn't exist.
    		
    		// if we're the master, there is no point querying ourselves, as we already know
    		// that it doesn't exist. in this case, create the channel
    		if( gm.isMaster() )
    		{
    			// create and store the channel address
    			address = createChannelAddress();
    			channels.put( channelName, address );
    			if( logger.isDebugEnabled() )
    			{
    				logger.debug( "Channel unknown. We are master, created new channel address: "+
    				              address );
    			}
    			
    			// put the response in a response packet and queue it for response processing
    			byte[] payload = Common.serialize( address.getAddress().getHostAddress(),
    			                                   address.getPort() );
    			Packet response = new Packet( payload );
    			response.attachSerial( packet );
    			channel.queueForReceiving( response );
    
    			// send out an channel update packet to everyone
    			Packet updateListPacket = new Packet( Common.serialize(channels) );
    			updateListPacket.setHeader( Header.FederationManagement,
    			                            MessageType.UpdateChannelList.byteValue() );
    			channel.queueForSending( updateListPacket );
    			return false;
    		}
    		else
    		{
    			// we're NOT the master, so let's just ask them, ay?
    			// we'll just let the packet go and flow out to the network. when it comes back
    			// (with the same serial) we'll extract the address and store it locally. As there
    			// is a connection waiting on a packet with the same serial, that information will
    			// then flow out to it.
    			return true;
    		}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Incoming Packet Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handles incoming federation management packets. If a channel list update comes in, the
	 * contents will be extracted and stored. If a request for the location or creation of a
	 * channel comes in (and we're the group management master), it will be handled, with the
	 * location sent back (with the same serial) or a new location created for the channel.
	 */
	public boolean incoming( Packet packet ) throws RuntimeException
	{
		// skip if the packet doesn't have the GM header
		if( packet.hasHeader(Header.FederationManagement) == false )
			return true;
		
		MessageType type = MessageType.valueOf( packet.getHeaderAsByte(Header.FederationManagement) );
		switch( type )
		{
			case UpdateChannelList:
				updateListFromPacket( packet );
				return false;
			case LocateOrCreateChannel:
				locateOrCreateChannel( packet );
				return false;
			default:
				break;
		}
		
		return true;
	}

	/**
	 * <b>NOTE:</b> <i>This method should only be called if the local component if the Group
	 * Management master.</i>
	 * <p/> 
	 * This method responds to be a message to locate or create a new channel. If the channel is
	 * known locally, a response packet consisting of the location will be broadcast back out. If
	 * the channel isn't known, and this component is the group management master, a new address
	 * for the channel will be created, with the new channel information being broadcast back out
	 * (using the same serial as the request). Following this, a channel location update will be
	 * broadcast out for everyone.
	 */
	private void locateOrCreateChannel( Packet packet )
	{
		// if we are not the master, skip out as it isn't our responsibility
		if( !gm.isMaster() )
			return;

		synchronized( channels )
		{
			String channelName = packet.getPayloadAsString();
			if( logger.isDebugEnabled() )
			{
				logger.debug( "Client ["+packet.getSender()+"] requests location for channel ["+
				              channelName+"]" );
			}
			
			// do we already know about the channel locally?
			if( channels.containsKey(channelName) )
			{
				// we have it, package up a response and send it back
				// no need for a FM header as we've filled out the response serial number
				InetSocketAddress address = channels.get( channelName );
				byte[] payload = Common.serialize( address.getAddress().getHostAddress(),
				                                   address.getPort() );
				Packet response = new Packet( payload );
				response.attachSerial( packet );
				channel.getTransport().sendToNetwork( response );
				return;
			}
			else
			{
				// we don't know the channel, create it and tell the requester
    			// create and store the channel address
    			InetSocketAddress address = createChannelAddress();
    			channels.put( channelName, address );
    			if( logger.isDebugEnabled() )
    			{
    				logger.debug( "Channel unknown. We are master, created new channel address: "+
    				              address );
    			}
    			
    			// put the response in a response packet and queue it for response processing
    			byte[] payload = Common.serialize( address.getAddress().getHostAddress(),
    			                                   address.getPort() );
    			Packet response = new Packet( payload );
    			response.attachSerial( packet );
    			channel.getTransport().sendToNetwork( response );
    
    			// send out an channel update packet to everyone - this isn't totally time
    			// sensitive, so we can just queue it up for later sending
    			Packet updateListPacket = new Packet( Common.serialize(channels) );
    			updateListPacket.setHeader( Header.FederationManagement,
    			                            MessageType.UpdateChannelList.byteValue() );
    			channel.queueForSending( updateListPacket );
			}
		}
	}
	
	/**
	 * Updates the local list of all channel locations from the incoming packet.
	 * 
	 * @return Returns true if processing of this message should continue, false otherwise
	 */
	@SuppressWarnings("unchecked")
	private void updateListFromPacket( Packet packet )
	{
		// if we are the master, ignore this as we created the message in the first place!
		if( gm.isMaster() )
			return;
		
		synchronized( channels )
		{
			HashMap<String,InetSocketAddress> received =
				(HashMap<String,InetSocketAddress>)Common.deserialize(packet.getPayload(),1)[0];
			channels.putAll( received );
		}
	}
	
	/**
	 * Creates a new address for a new channel to work on. This should only be called if the local
	 * component is the {@link GroupManagement} master.
	 */
	private InetSocketAddress createChannelAddress()
	{
		this.lastAddress = new InetSocketAddress( lastAddress.getAddress(), lastAddress.getPort()+1 );
		return this.lastAddress;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Creates a packet that can be broadcast out to the component that handles federation
	 * mangement to find out the location of the provided channel.
	 */
	public static final Packet locateChannel( String channelName )
	{
		Packet packet = new Packet();
		packet.setHeader( Header.FederationManagement, MessageType.LocateOrCreateChannel.byteValue() );
		packet.setPayload( channelName.getBytes() );
		return packet;
	}
}
