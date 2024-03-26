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

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.bindings.ptalk.Common;
import org.portico.bindings.ptalk.channel.Channel;
import org.portico.bindings.ptalk.channel.Header;
import org.portico.bindings.ptalk.channel.Packet;
import org.portico.bindings.ptalk.channel.Roster;
import org.portico.lrc.compat.JConfigurationException;

/**
 * The GroupManagement protocol takes care of keeping a roster of who is in a particular channel,
 * what order they joined in and who is the master responsible for answering questions of this
 * nature for other channel members.
 */
@Protocol(name="GM")
public class GroupManagement extends AbstractProtocol
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	public enum MessageType
	{
		Discovery,             // component has just joined the group
		RosterUpdate;          // a new roster update from the master
		
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
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The delay to wait for a response to the discovery call before we decide nobody else is out
	    there and take over the role of being master ourselves */
	public static final long DISCOVERY_DELAY = 3000;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean master;
	private Logger logger;

	// the roster of all the joined components in the channel
	private Roster roster;
	// can't lock on null objects, which the roster is at first, so this object is
	// used to lock on when manipulating the roster
	private Object rosterLock = new Object();
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void configure( Channel channel, Map<String,Object> properties )
		throws JConfigurationException
	{
		super.configure( channel, properties );
		this.master = false;
		this.roster = null;
		this.logger = LogManager.getLogger( Common.getLogger().getName()+".GM" );
	}

	/**
	 * Returns true if the local component is the master for the channel
	 */
	public boolean isMaster()
	{
		return this.master;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Outgoing Packet Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handle GM packets are are being sent out. If the packet is a {@link MessageType#DISCOVERY}
	 * packet, we attempt to discover the master before returning. Processing discovery packets will
	 * also cause the GM to stop them being passed down the stack to any other components in the
	 * Pipeline.
	 */
	public boolean outgoing( Packet packet ) throws RuntimeException
	{
		// skip out if this packet isn't for us
		if( packet.hasHeader(Header.GM) == false )
			return true;
		
		// this is a GM packet, process it
		MessageType type = MessageType.valueOf( packet.getHeaderAsByte(Header.GM) );

		switch( type )
		{
			case Discovery:
				discoverMaster( packet );
				return false;
			case RosterUpdate:
				break;
		}
		
		return true;
	}

	/**
	 * Attempts to locate an existing master and elicit a {@link Roster} from them. If none is
	 * found in time, the local component assumes that it is the master, creates a new roster
	 * and broadcasts this out to the other components.
	 */
	private void discoverMaster( Packet request )
	{
		// send this message directly to the transport and then wait for an appropriate response
		// this is a discovery message, so we'll skip over the rest of the protocol stack as it
		// only serves one purpose
		channel.getTransport().sendToNetwork( request );

		synchronized( rosterLock )
		{
			// sleep a bit while we wait for any sort of discovery
			try
			{
				this.rosterLock.wait( DISCOVERY_DELAY );
			}
			catch( InterruptedException ie )
			{
				// ignore, we have found a roster
			}

			// if we have no master, we must assume the role, otherwise, we're good
			if( roster == null )
			{
				master = true;
				logger.trace( "No existing master, assuming master role, broadcasting roster" );
				// create the roster and broadcast it
				roster = new Roster();
				roster.addMember( channel.getTransport().getAddress() );

				// have the transport send out a roster update
				Packet packet = new Packet( Roster.marshal( roster ) );
				packet.setHeader( Header.GM, MessageType.RosterUpdate.byteValue() );
				channel.getTransport().sendToNetwork( packet );
				
				// pass the roster back to the channel
				channel.setRoster( this.roster );
			}
		}
	}

	
	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Incoming Packet Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handle incoming packets. To figure out what action is being requested (if any), this method
	 * will check for the value associated with the {@link Headers#GM} header (which should be the
	 * byte representation of a {@link MessageType}. Most of the packets we're interested in
	 * handling are processed in separate methods. See those methods for the specifics of the
	 * actions taken for given requests.
	 */
	public boolean incoming( Packet packet ) throws RuntimeException
	{
		// skip if the packet doesn't have the GM header
		if( packet.hasHeader(Header.GM) == false )
			return true;
		
		MessageType type = MessageType.valueOf( packet.getHeaderAsByte(Header.GM) );
		switch( type )
		{
			case Discovery:
				if( master )
					newConnection( packet );
				break;
			case RosterUpdate:
				updateRoster( packet );
				break;
		}
		
		return true;
	}
	
	/**
	 * We are the channel master and someone new has joined. Add them to the roster and broadcast
	 * the new one out.
	 */
	private void newConnection( Packet packet )
	{
		synchronized( rosterLock )
		{
			// get the address of the new member and add them to the roster
			InetSocketAddress sender = packet.getSender();
			roster.addMember( sender );

			if( logger.isTraceEnabled() )
			{
				logger.trace( "New member has joined the channel: "+sender );
				logger.trace( roster );
			}
			
			// broadcast out the updated roster
			Packet update = new Packet( Roster.marshal( roster ) );
			update.setHeader( Header.GM, MessageType.RosterUpdate.byteValue() );
			channel.getTransport().sendToNetwork( update );
		}
	}

	/**
	 * A new roster has been received. If this is our initial roster, install it and log that the
	 * master component has been found. Otherwise just install the new roster.
	 */
	private void updateRoster( Packet packet )
	{
		// if we're the master, we already know about roster changes, so we can skip out of here
		if( master )
			return;

		synchronized( rosterLock )
		{
			try
			{
				// deserialise the roster
				ObjectInputStream ois =
					new ObjectInputStream( new ByteArrayInputStream( packet.getPayload()) );
				Roster newRoster = (Roster)ois.readObject();
				
				// is the incoming roster newer than the existing one?
				if( roster == null )
				{
					roster = newRoster;
					roster.setLocalMember( channel.getTransport().getAddress().getAddress() );
					channel.setRoster( newRoster );
					logger.trace( "Master located: "+roster.getMaster() );
					logger.trace( roster );
				}
				else if( roster.getLastUpdateTime() < newRoster.getLastUpdateTime() )
				{
					roster = newRoster;
					logger.trace( "Received new roster: " );
					logger.trace( roster );
				}
			}
			catch( Exception e )
			{
				throw new RuntimeException( e );
			}
		}
	}
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
