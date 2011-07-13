/*
 *   Copyright 2008 The Portico Project
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
package org.portico.bindings.jgroups;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgroups.Address;
import org.jgroups.View;
import org.portico.lrc.model.ObjectModel;

/**
 * The Roster class represents the shared-state for a JGroups channel. It maintains information
 * about the memebers of the channel, which of those have "joined" the federation and which haven't,
 * along with other information like the object model.
 */
public class Roster implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String channelName;
	private transient Address localAddress;
	private View currentView;
	private Map<Address,Integer> membership; // any connected application
	private Map<Address,FederateInfo> federates; // only connected federates (issued join()'s)
	
	private transient boolean isController;
	private int highestHandle;
	private ObjectModel objectModel;
	
	// helper variables
	private transient Map<String,Integer> goneSinceLastUpdate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Roster( String channelName, Address localAddress, View initialView )
	{
		this.channelName = channelName;
		this.localAddress = localAddress;
		this.currentView = initialView;
		this.membership = new HashMap<Address,Integer>();
		this.federates = new HashMap<Address,FederateInfo>();
		
		// some defaults
		this.isController = false;
		this.highestHandle = 0;
		
		this.goneSinceLastUpdate = new HashMap<String,Integer>();
		
		// initial update
		updateFromView( currentView );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * When a ChannelWrapper connects to a federation, we tell it to block until it receives the
	 * shared state, which in this case is an instance of this {@link Roster} class. Once received,
	 * we need to update this roster with a bit of information about its local environment as it is
	 * just a serialized {@link Roster} from another connection. This method does that updating.
	 */
	protected synchronized void updateAfterGetState( Address localAddress )
	{
		this.localAddress = localAddress;
		this.isController = isController();
		this.goneSinceLastUpdate = new HashMap<String,Integer>();
	}

	/**
	 * When a new connection to the channel is made, a new View is created by JGroups. Each view
	 * contains a list of members, which is always ordered in a predictable way. This method is
	 * called when a new view is installed and is responsible for removing information about
	 * connections that have ceased to exist and store information about new connections we have.
	 * @param newView
	 */
	public synchronized void updateFromView( View newView )
	{
		this.goneSinceLastUpdate.clear();
		this.currentView = newView;
		
		// reset out "highest seen handle" variable
		int currentKnownHighest = findHighestHandle( currentView.getMembers() );
		if( currentKnownHighest > highestHandle )
			highestHandle = currentKnownHighest;
		
		// REMOVE members no longer joined from membership map
		Set<Address> memberAddresses = new HashSet<Address>( membership.keySet() );
		for( Address address : memberAddresses )
		{
			// if they're not in the current view, we don't want them
			if( currentView.containsMember(address) == false )
			{
				membership.remove( address );
				if( federates.containsKey(address) )
				{
					// is was a federate that has disappeared
					FederateInfo removed = federates.remove( address );
					goneSinceLastUpdate.put( removed.name, removed.handle );
				}
			}
		}
		
		// INSERT new members not yet in membership map
		for( Address address : currentView.getMembers() )
		{
			// if they're new, put in membership map but NOT federates map, only
			// members that have issued join's go in the federates map
			if( membership.containsKey(address) == false )
				membership.put( address, ++this.highestHandle );
		}
		
		// check to see if we are now the controller
		this.isController = localAddress.equals( currentView.getMembers().get(0) );
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// State Access Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	public synchronized boolean isController()
	{
		return this.isController;
	}
	
	public synchronized Address getController()
	{
		if( this.isController )
			return localAddress;
		else
			return currentView.getMembers().get(0);
	}

	public synchronized ObjectModel getObjectModel()
	{
		return this.objectModel;
	}
	
	public synchronized void setObjectModel( ObjectModel model )
	{
		this.objectModel = model;
	}
	
	public synchronized int getLocalId()
	{
		return membership.get( localAddress );
	}
	
	public synchronized int getConnectionId( Address address )
	{
		return membership.get( address );
	}

	public synchronized boolean containsFederate( String federateName )
	{
		for( FederateInfo federate : federates.values() )
		{
			if( federate.name.equals(federateName) )
				return true;
		}
		
		return false;
	}
	
	public synchronized Collection<FederateInfo> getFederates()
	{
		return federates.values();
	}
	
	public synchronized Set<Integer> getFederateHandles()
	{
		Set<Integer> handleSet = new HashSet<Integer>();
		for( FederateInfo federateInfo : federates.values() )
			handleSet.add( federateInfo.handle );
		
		return handleSet;
	}
	
	/**
	 * Remember to synchronize whatever is accessing this even after this call (when you are
	 * processing the map). It could change underneath you!
	 */
	public synchronized Map<String,Integer> getGoneSinceLastUpdate()
	{
		return goneSinceLastUpdate;
	}
	
	public synchronized String toString()
	{
		StringBuilder builder = new StringBuilder( "Roster: channel=" );
		builder.append( channelName );

		builder.append( "\n-----------------------------------" );
		builder.append( "\n Local Address  = "+localAddress );
		builder.append( "\n Highest Handle = "+highestHandle );
		builder.append( "\n Is Federation  = " );
		if( objectModel == null )
			builder.append( "false" );
		else
			builder.append( "true" );
		
		// members
		builder.append( "\n Channel members: " );
		builder.append( membership.size() );
		builder.append( "\n" );
		for( Address address : currentView.getMembers() )
		{
			// is this a federate?
			int connectionID = membership.get( address );
			FederateInfo info = federates.get( address );
			if( info == null )
			{
				builder.append( "  (application) id=" );
				builder.append( connectionID );
				builder.append( ", address=" );
				builder.append( address );
				if( address.equals(getController()) )
					builder.append( " **CONTROLLER**" );
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
				if( address.equals(getController()) )
					builder.append( " **CONTROLLER**" );
				builder.append( "\n" );
			}
		}
		
		return builder.toString();
	}

	////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Private Helper Methods //////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	private int findHighestHandle( List<Address> list )
	{
		int highest = 0;
		for( Address currentAddress : list )
		{
			Integer handle = membership.get( currentAddress );
			if( handle != null && handle > highest )
				highest = handle;
		}
		
		return highest;
	}

	/**
	 * Make all changes to the Roster needed to reflect that the connection has joined as a
	 * federate. If there is a problem, returns a string outlining the error. If everything
	 * goes ok, return null (so as to indicate no error).
	 */
	protected synchronized String connectionJoined( Address address,
	                                                String federateName,
	                                                String federationName )
	{
		// check to see if we know the federation even exists
		if( objectModel == null )
		{
			return "Federate ["+federateName+"] apparently joined ["+federationName+ 
			       "], but we don't know federation exists (no model in Roster)";
		}
		
		// check to see if we already have a federate with this name
		if( containsFederate(federateName) )
		{
			return "Federate ["+federateName+"] apparently joined ["+federationName+ 
			       "], but we already have a joined federate with that name";
		}
		
		// store the newly joined federate info
		FederateInfo federateInfo = new FederateInfo();
		federateInfo.handle = getConnectionId( address );
		federateInfo.name = federateName;
		federates.put( address, federateInfo );
		return null;
	}
	
	/**
	 * Make all changes to the Roster needed to reflect that the connection has resigned as a
	 * federate. If there is a problem, returns a string outlining the error. If everything
	 * goes ok, return null (so as to indicate no error).
	 */
	protected synchronized String connectionResigned( Address address,
	                                                  String federateName,
	                                                  String federationName )
	{
		// check to see if the connection is in the joined
		if( federates.remove(address) == null )
		{
			return "Federate ["+federateName+"] apparently resigned from ["+federationName+
			       "], but we don't have it listed in the federation, ignoring...";
		}
		
		return null;
	}
	
	/**
	 * Make all changes to the Roster needed to reflect that the connection has destroyed
	 * the federation. If there is a problem, returns a string outlining the error. If everything
	 * goes ok, return null (so as to indicate no error).
	 */
	protected synchronized String connectionDestroyed( Address address, String federationName )
	{
		if( this.objectModel == null )
		{
			return "Connection ["+address+"] apparently destroyed federation ["+federationName+
			       "], but we didn't know it existed, ignoring...";
		}
		else if( this.federates.size() > 0 )
		{
			return "Connection ["+address+"] apparnetly destoryed federation ["+federationName+
			       "], but we still have active federates: "+federates.values();
		}
		
		this.objectModel = null;
		return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	public class FederateInfo implements Serializable
	{
		private static final long serialVersionUID = 98121116105109L;
		public int handle;
		public String name;
		
		public String toString()
		{
			return name;
		}
	}
}
