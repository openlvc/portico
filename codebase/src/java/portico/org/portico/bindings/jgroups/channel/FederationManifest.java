/*
 *   Copyright 2012 The Portico Project
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

import org.jgroups.Address;
import org.jgroups.View;
import org.portico.lrc.model.ObjectModel;

/**
 * The {@link FederationManifest} contains information about an active Portico federation
 * that resides inside a JGroups channel. It is passed to all new channel members via the
 * channel shared state, and from that point on it is updated whenever there is a change
 * in the membership of the channel (via the viewChanged() callback in Federation).
 */
public class FederationManifest implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationName;
	private boolean federationCreated;
	private ObjectModel fom;
	// the highest handle assigned to an active federate. this is used to
	// detemine which handle to assign to the next federate that joins
	private int highestHandle;
	
	// channel membership information
	private Address channelCoordinator;
	private Map<Address,Integer> channelMembers;        // any connected application
	private Map<Address,FederateInfo> channelFederates; // only *joined* federates
	
	// properties that relate to the local federate
	private transient Address localAddress;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new manifest for the federation with the given name. Our local address is provided
	 * so we can determine if we are the coordinator and an initial jgroup View is also provided
	 * for us to populate ourselves with.
	 * 
	 * @param federationName The name of the federation channel we've joined
	 * @param localAddress The address of the local application
	 * @param initialView The initial view that gives us our membership details
	 */
	public FederationManifest( String federationName, Address localAddress, View initialView )
	{
		this.federationName = federationName;
		this.federationCreated = false;
		this.fom = null;
		this.highestHandle = 0;
		
		// channel membership
		this.localAddress = localAddress;
		this.channelCoordinator = null;
		this.channelMembers = new HashMap<Address,Integer>();
		this.channelFederates = new HashMap<Address,FederateInfo>();
		updateMembership( initialView );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Accessors and Mutators /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets our local address. This needs to be called whenever we join a channel or
	 * reinitialized when we receive this manifest in serialized form as channel shared state
	 */
	protected void setLocalAddress( Address address )
	{
		this.localAddress = address;
	}

	public boolean isCreated()
	{
		return this.federationCreated;
	}
	
	public void setCreated( boolean federationCreated )
	{
		this.federationCreated = federationCreated;
	}

	public ObjectModel getFom()
	{
		return this.fom;
	}
	
	public void setFom( ObjectModel fom )
	{
		this.fom = fom;
	}

	/**
	 * @return true if the local channel member is the current coordinator, false otherwise.
	 */
	public boolean isController()
	{
		return this.localAddress.equals( channelCoordinator );
	}
	
	public boolean containsFederate( String federateName )
	{
		for( FederateInfo federate : channelFederates.values() )
		{
			if( federate.name.equals(federateName) )
				return true;
		}
		
		return false;
	}

	/**
	 * Return the name of the joined federate located at the given address, or null if there
	 * is no joined federate there.
	 */
	public String getFederateName( Address address )
	{
		if( channelFederates.containsKey(address) )
			return channelFederates.get(address).name;
		else
			return null;
	}

	/**
	 * @return A set containing the handles of all joined federates.
	 */
	public Set<Integer> getFederateHandles()
	{
		Set<Integer> handleSet = new HashSet<Integer>();
		for( FederateInfo federateInfo : channelFederates.values() )
			handleSet.add( federateInfo.handle );
		
		return handleSet;
	}

	// Local Federate Information Methods ///////////////////////

	public boolean isLocalFederateJoined()
	{
		return this.channelFederates.containsKey( localAddress );
	}
	
	public String getLocalFederateName()
	{
		return getFederateName( localAddress );
	}

	public int getLocalFederateHandle()
	{
		return channelMembers.get( localAddress );
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "\n-----------------------------------" );
		builder.append( "\nManifest: channel="+federationName );
		builder.append( "\n-----------------------------------" );
		builder.append( "\n Local Address  = "+localAddress );
		builder.append( "\n Highest Handle = "+highestHandle );
		builder.append( "\n Is Federation  = "+isCreated() );
		// members
		builder.append( "\n Channel members: " );
		builder.append( channelMembers.size() );
		builder.append( "\n" );
		for( Address address : channelMembers.keySet() )
		{
			// is this a federate?
			int connectionID = channelMembers.get( address );
			FederateInfo info = channelFederates.get( address );
			if( info == null )
			{
				builder.append( "  (application) id=" );
				builder.append( connectionID );
				builder.append( ", address=" );
				builder.append( address );
				if( address.equals(channelCoordinator) )
					builder.append( " **CO-ORDINATOR**" );
				builder.append( "\n" );
			}
			else
			{
				builder.append( "     (federate) id=" );
				builder.append( connectionID );
				builder.append( ", name=" );
				builder.append( info.name );
				builder.append( ", address=" );
				builder.append( address );
				if( address.equals(channelCoordinator) )
					builder.append( " **CO-ORDINATOR**" );
				builder.append( "\n" );
			}
		}
		
		return builder.toString();
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Manifest Updating Methods ///////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method take the channel membership information contained in the provided view and
	 * updates the local manifest with it. The installment of a new view may have been triggered
	 * by the presence of a new channel member or the exiting of an existing one. Either way,
	 * this method allows us to do internal housekeeping work to keep the manifest up to date.
	 * <p/>
	 * Handles are automatically assigned one at a time based on a counter. We have to go through
	 * and make sure our counter points to a value higher than any handle of any existing federate
	 * in the federation. All connections do the same algorithm, so the outcome (given the same
	 * JGroups View) is deterministic and can be used when assigning a new federate handle when
	 * a federate joins the federation.
	 * <p/>
	 * There are two types of members in a channel. Basic channel members, which are applications
	 * that have joined the channel but not yet joined the federation (haven't issued a join
	 * request) and federates, which have both joined the channel and federation inside.
	 * <p/>
	 * The return value for this method is a map (federate handle = fedeate name) of all known
	 * *federates* (not just basic channel members) which were present in the previous membership
	 * but are not present in the new view. These may require special handling.
	 * @param newView The new channel view
	 * 
	 * @return A map containing information about all the *federates* that are not longer part
	 *         of the channel membership according to this new view. 
	 */
	public synchronized Map<Integer,String> updateMembership( View newView )
	{
		// reset out "highest seen handle" variable
		int highest = 0;
		for( Address currentAddress : newView.getMembers() )
		{
			Integer handle = channelMembers.get( currentAddress );
			if( handle != null && handle > highest )
				highest = handle;
		}
		this.highestHandle = highest;

		// remove any channel members that have disappeared from the new view  
		Map<Integer,String> mia = new HashMap<Integer,String>(); // federaes missing without resign
		for( Address address : new HashSet<Address>(channelMembers.keySet()) )
		{
			// if this address is not in the current view, it means they left
			if( newView.containsMember(address) == false )
			{
				channelMembers.remove( address );
				if( channelFederates.containsKey(address) )
				{
					// they were a joined federate and they're gone without sending out
					// a resign, put them in the MIA list to return for further processing
					FederateInfo removed = channelFederates.remove( address );
					mia.put( removed.handle, removed.name );
				}
			}
		}
		
		// add any new members that joined the channel as part of the new view
		for( Address address : newView.getMembers() )
		{
			// if they're new, put in membership map but NOT federates map, only
			// members that have issued join's go in the federates map
			if( channelMembers.containsKey(address) == false )
				channelMembers.put( address, ++this.highestHandle );
		}
		
		return mia;
	}

	/**
	 * Update the internal state to reflect the fact that a member of the channel has become
	 * a *joined* federate. If there is an error, return it as a String for logging. As we
	 * are handling messages from remote federates here, there is little we can do except
	 * log that there was a problem.
	 * <p/>
	 * Returns null if there was no problem.
	 */
	public String federateJoined( Address address, String federateName )
	{
		// check to see if we know the federation even exists
		if( fom == null )
		{
			return "Federate ["+federateName+"] apparently joined ["+federationName+ 
			       "], but we have no record that federation has been installed into the channel";
		}

		// check to see if we already have a federate with this name
		if( containsFederate(federateName) )
		{
			return "Federate ["+federateName+"] apparently joined ["+federationName+ 
			       "], but we already have a joined federate with that name";
		}

		// store the newly joined federate info
		FederateInfo federateInfo = new FederateInfo();
		federateInfo.handle = channelMembers.get( address );
		federateInfo.name = federateName;
		channelFederates.put( address, federateInfo );
		return null;
	}

	/**
	 * Update the internal state to reflect that the federate resigned. It is still a channel
	 * member, just no longer a joined federate as well.
	 */
	public String federateResigned( Address address )
	{
		// check to see if the connection is in the joined
		if( channelFederates.remove(address) == null )
		{
			return "Federate at addresss ["+address+"] apparently resigned from ["+federationName+
			       "], but we don't have it listed in the federation, ignoring...";
		}
		
		return null;
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
	public class FederateInfo implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;
		public int handle;
		public String name;
		public String toString(){ return name; }
	}

}
