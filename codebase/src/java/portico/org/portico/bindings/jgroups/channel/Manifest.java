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
package org.portico.bindings.jgroups.channel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.portico.lrc.model.ObjectModel;

public class Manifest implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 3112252018924L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int manifestVersion; // updated each time the manifest changes
	private String channelName;
	private transient UUID localUUID;
	private UUID coordinator;

	// The highest handle assigned to a connected member. Each joined member
	// is given a handle that then becomes their federate handle if they join.
	// Handles are allocated by the coordinator, who uses this to determine
	// the next suitable value
	private int highestHandle;

	// channel members - each is assigned a number
	private Map<UUID,Integer> members;
	
	// federation information
	private ObjectModel fom;
	private Map<UUID,FederateInfo> federates;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Manifest( String name, UUID localUUID )
	{
		this.manifestVersion = 0;
		this.channelName = name;
		this.localUUID = localUUID;
		this.coordinator = null;  // set after creation
		this.highestHandle = 0;
		this.members = new HashMap<UUID,Integer>();
		
		// federation information
		this.fom = null; // set when someone creates a federation
		this.federates = new HashMap<UUID,FederateInfo>();
		
		// record the fact that we have joined
		memberConnectedToChannel( localUUID );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Accessors and Mutators /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void setLocalUUID( UUID localUUID )
	{
		this.localUUID = localUUID;
	}

	public int getLocalFederateHandle()
	{
		return members.get( localUUID );
	}
	
	public String getLocalFederateName()
	{
		return federates.get(localUUID).name;
	}
	
	public String getFederateName( UUID uuid )
	{
		FederateInfo info = federates.get(uuid);
		return info != null ? info.name : "unknown";
	}
	
	public int getFederateHandle( UUID uuid )
	{
		return federates.get(uuid).handle;
	}
	
	public boolean isLocalFederateJoined()
	{
		return federates.containsKey( localUUID );
	}

	public boolean isJoinedFederate( UUID uuid )
	{
		return federates.containsKey( uuid );
	}

	public UUID getCoordinator()
	{
		return this.coordinator;
	}
	
	public void setCoordinator( UUID coordinator )
	{
		this.coordinator = coordinator;
	}
	
	public boolean isCoordinator()
	{
		return this.coordinator.equals( this.localUUID );
	}

	public boolean isCoordinator( UUID uuid )
	{
		return this.coordinator.equals( uuid );
	}

	/**
	 * @return A set containing the handles of all joined federates.
	 */
	public Set<Integer> getFederateHandles()
	{
		Set<Integer> handleSet = new HashSet<Integer>();
		for( FederateInfo federateInfo : federates.values() )
			handleSet.add( federateInfo.handle );
		
		return handleSet;
	}

	public ObjectModel getFom()
	{
		return this.fom;
	}
	
	public void setFom( ObjectModel model )
	{
		// called when a federation is first created and distributed to connections
		// when they join a channel (prior to them joining the federation). Also set
		// when a join *notification* is received if it contains FOM modules we need
		// to merge (this is done by each local federate).
		this.fom = model;
	}

	/** Return true if there is an active federation in this channel */
	public boolean containsFederation()
	{
		return this.fom != null;
	}

	public boolean containsFederate( String name )
	{
		for( FederateInfo federate : federates.values() )
		{
			if( federate.name.equals(name) )
				return true;
		}
		
		return false;
	}

	public int getManifestVersion()
	{
		return this.manifestVersion;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Lifecycle Notification Methods /////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public synchronized void memberConnectedToChannel( UUID uuid )
	{
		this.members.put( uuid, ++highestHandle );
		++manifestVersion;
	}

	public synchronized void federationCreated( ObjectModel fom )
	{
		this.fom = fom;
		++this.manifestVersion;
	}
	
	public synchronized void federateJoined( UUID uuid, String federateName )
	{
		FederateInfo federateInfo = new FederateInfo();
		federateInfo.name = federateName;
		federateInfo.handle = members.get(uuid);
		this.federates.put( uuid, federateInfo );
		++manifestVersion;
	}

	/**
	 * Update the internal state to reflect that the federate resigned. It is still a channel
	 * member, just no longer a joined federate as well.
	 */
	public synchronized void federateResigned( UUID uuid )
	{
		federates.remove( uuid );
		++manifestVersion;
	}
	
	public synchronized void federationDestroyed()
	{
		this.fom = null;
		++manifestVersion;
	}

	/**
	 * Record that the given member has left the channel. If they were the coordinator
	 * we need to decide on a new coordinator. We do this by picking the member with the
	 * lowest handle from those remaining.
	 */
	public synchronized void memberLeftChannel( UUID uuid )
	{
		// remove the unbeliever
		members.remove( uuid );
		++manifestVersion;
		
		// did the coord leave? if so, figure out his next in line
		if( uuid.equals(this.coordinator) )
		{
			int lowest = Integer.MAX_VALUE;
			UUID spartacus = null;
			for( UUID currentUUID : members.keySet() )
			{
				int currentHandle = members.get( currentUUID );
				if( currentHandle < lowest )
					spartacus = currentUUID;
			}
			
			// all hail!
			this.coordinator = spartacus;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "\n-----------------------------------" );
		builder.append( "\nManifest: channel="+channelName );
		builder.append( "\n-----------------------------------" );
		builder.append( "\n Version        = "+manifestVersion );
		builder.append( "\n Local UUID     = "+localUUID );
		builder.append( "\n Highest Handle = "+highestHandle );
		builder.append( "\n Is Federation  = "+containsFederation() );
		// members
		builder.append( "\n Channel members: " );
		builder.append( members.size() );
		builder.append( "\n" );
		for( UUID uuid : members.keySet() )
		{
			// is this a federate?
			int connectionID = members.get( uuid );
			FederateInfo info = federates.get( uuid );
			if( info == null )
			{
				builder.append( "  (application) id=" );
				builder.append( connectionID );
				builder.append( ", uuid=" );
				builder.append( uuid );
				if( uuid.equals(coordinator) )
					builder.append( " **CO-ORDINATOR**" );
				builder.append( "\n" );
			}
			else
			{
				builder.append( "     (federate) id=" );
				builder.append( connectionID );
				builder.append( ", name=" );
				builder.append( info.name );
				builder.append( ", uuid=" );
				builder.append( uuid );
				if( uuid.equals(coordinator) )
					builder.append( " **CO-ORDINATOR**" );
				builder.append( "\n" );
			}
		}
		
		return builder.toString();
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Private Inner Class: FedeateInfo ////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This class stores basic information about a joined federate.
	 */
	private class FederateInfo implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;
		public int handle;
		public String name;
		public String toString(){ return name; }
	}

}
