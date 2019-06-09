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
package org.portico2.rti.services.sync.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JSynchronizationLabelNotAnnounced;
import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.sync.data.SyncPoint.Status;

/**
 * This class manages all the record keeping about synchronization points. It takes care of
 * transitioning them from one state to the next and keeping track of which points have been
 * achieved by which federates. The point also keeps a set of the handles of all federates that
 * have attempted to register it.
 */
public class SyncPointManager implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Federation federation;
	private HashMap<String,SyncPoint> syncPoints;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SyncPointManager( Federation federation )
	{
		this.federation = federation;
		this.syncPoints = new HashMap<String,SyncPoint>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Create and register a federation-wide synchronization point. If the label is already known,
	 * an exception will be thrown. 
	 * 
	 * @param label      The label of the sync point to create
	 * @param tag        The tag that was given during the point registration
	 * @param registrant The handle of the federate that registered the point
	 * @return The newly created and registered sync point
	 * @throws JRTIinternalError If the point already exists
	 */
	public synchronized SyncPoint registerSyncPoint( String label, byte[] tag, int registrant )
		throws JRTIinternalError
	{
		return registerSyncPoint( label, tag, null, registrant );
	}
	
	/**
	 * Create and register a restricted synchronization point, with the participant federate
	 * handles listed in the given set. If the label is already known, an exception will be thrown. 
	 * 
	 * @param label      The label of the sync point to create
	 * @param tag        The tag that was given during the point registration
	 * @param federates  The handles of the federates that should be subject to this sync point
	 * @param registrant The handle of the federate that registered the point
	 * @return The newly created and registered sync point
	 * @throws JRTIinternalError If the point already exists
	 */
	public synchronized SyncPoint registerSyncPoint( String label,
	                                                 byte[] tag,
	                                                 Set<Integer> federates,
	                                                 int registrant )
		throws JRTIinternalError
	{
		if( syncPoints.containsKey(label) )
			throw new JRTIinternalError( "Synchronziation Point already exists: label="+label );

		SyncPoint point = new SyncPoint( label, tag, federates, registrant );
		syncPoints.put( label, point );
		return point;
	}

	/**
	 * Record that the given federate has achieved the sync point with the provided label.
	 * After each registration we also recalculate to see if the federation is now synchronized.
	 * The point is returned at the conclusion of this (and can be queried for status).
	 * 
	 * @param label The label of the point
	 * @param federateHandle The federate that achieved the point
	 * @return The sync point that was marked as achieved for the given federate
	 * @throws JSynchronizationLabelNotAnnounced If we can't find a point with the given label
	 */
	public synchronized SyncPoint achieveSyncPoint( String label, int federateHandle )
		throws JSynchronizationLabelNotAnnounced
	{
		// Get the point
		SyncPoint point = syncPoints.get( label );
		if( point == null )
			throw new JSynchronizationLabelNotAnnounced( "Synchronization Point not announced: "+label );
		
		// Record that the federate has achieved the point
		point.federateAchieved( federateHandle );
		
		// Update the point status just in case everyone has achieved it
		updatePointStatus( point );
		
		return point;
	}
	
	private void updatePointStatus( SyncPoint point )
	{
		// Get the set of federates that need to be synchronized
		Set<Integer> federateHandles = null;
		if( point.isFederationWide() )
			federateHandles = new HashSet<>( federation.getFederateHandles() );
		else
			federateHandles = point.getFederates();
		
		// Check that every federate in the federation has achieved the point
		// If they haven't, don't change it
		for( int federateHandle : federation.getFederateHandles() )
		{
			if( point.hasFederateAchieved(federateHandle) == false )
				return;
		}
		
		// If we get here we know that everyone in the federation (or everyone in the
		// registration set) has achieved the point
		point.status = Status.SYNCHRONIZED;
	}

	/**
	 * Return <code>true</code> if the point with the given label is marked as "synchronized".
	 * If the label is not known, an exception is thrown
	 * @param label The label to check the sync status for
	 * @return <code>true</code> if the point is achieved, <code>false</code> otherwise
	 * @throws JSynchronizationLabelNotAnnounced If the label is no known
	 */
	public synchronized boolean isSynchronized( String label )
		throws JSynchronizationLabelNotAnnounced
	{
		SyncPoint point = syncPoints.get( label );
		if( point == null )
			throw new JSynchronizationLabelNotAnnounced( "Synchronization Point not announced: "+label );

		return point.isSynchronized();
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public synchronized boolean containsPoint( String label )
	{
		return syncPoints.containsKey( label );
	}
	
	public synchronized SyncPoint getPoint( String label )
	{
		return syncPoints.get( label );
	}
	
	public synchronized SyncPoint removePoint( String label )
	{
		return syncPoints.remove( label );
	}
	
	public synchronized Collection<SyncPoint> getAllPoints()
	{
		return syncPoints.values();
	}

	public synchronized Set<String> getAllUnsynchronizedLabels()
	{
		return syncPoints.values().stream()
		                          .filter( p -> p.isFederationWide() && !p.isSynchronized() )
		                          .map( p -> p.label )
		                          .collect( Collectors.toSet() );
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( this.syncPoints );
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.syncPoints = (HashMap<String,SyncPoint>)input.readObject();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
