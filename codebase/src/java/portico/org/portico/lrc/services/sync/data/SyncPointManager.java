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
package org.portico.lrc.services.sync.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;
import org.portico.lrc.services.sync.data.SyncPoint.Status;
import org.portico2.common.services.federation.msg.RoleCall;

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
	private HashMap<String,SyncPoint> syncPoints;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SyncPointManager()
	{
		this.syncPoints = new HashMap<String,SyncPoint>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Tries to create and register a restricted synchronization point. If a point with the given
	 * label alrady exists, an exception will be thrown. If one with the same name doesn't exist,
	 * a new one will be created, stored and returned.
	 * 
	 * @return The newly created point if everything is successful
	 * @throws RuntimeException If a point with the same label already exists
	 */
	public synchronized SyncPoint registerPoint( String label,
	                                             byte[] tag,
	                                             Set<Integer> handles,
	                                             int registrant )
		throws RuntimeException
	{
		if( syncPoints.containsKey(label) )
			throw new RuntimeException( "Synchronziation Point already exists: label="+label );

		SyncPoint point = new SyncPoint( label,
		                                 tag,
		                                 handles,
		                                 SyncPoint.Status.REQUESTED,
		                                 registrant );
		syncPoints.put( label, point );
		return point;
	}
	
	/**
	 * A federate has received a remote notification that request to "lock" a point has been made.
	 * If there is no other point registered locally, create a new point with the status set to 
	 * {@link SyncPoint.Status#PENDING} and store it locally. If a point with the label has already
	 * been registered, throw an exception.
	 * 
	 * @param label The label of the point
	 * @param tag The tag provided with the point
	 * @param handles The handles of federates involved in the sync opint
	 * @param registrant The federate requesting registration of the point
	 * @return The newly created point if everything is successful
	 * @throws RuntimeException If a point with the same label already exists
	 */
	public synchronized SyncPoint pointPending( String label,
	                                            byte[] tag,
	                                            Set<Integer> handles,
	                                            int registrant )
	{
		if( syncPoints.containsKey(label) )
			throw new RuntimeException( "Synchronziation Point already exists: label="+label );
		
		SyncPoint point = new SyncPoint( label, tag, handles, SyncPoint.Status.PENDING, registrant );
		syncPoints.put( label, point );
		return point;
	}

	/**
	 * A federate has received notification of a synchronization point announcement. This method
	 * will create the point locally if it doesn't already exist, or it will update the point with
	 * the given tag and handle information, setting the points status to
	 * {@link SyncPoint.Status#ANNOUNCED}.
	 * 
	 * @param label The label of the sync point that has been announced
	 * @param tag The tag that was provided with the announcement notification
	 * @param handles The set of federate handles involved with the point. A null or empty set
	 *                indicates that this is a federation-wide synchronization point.
	 */
	public synchronized void pointAnnounced( String label,
	                                         byte[] tag,
	                                         Set<Integer> handles,
	                                         int registrant )
	{
		SyncPoint point = syncPoints.get( label );
		if( point == null )
		{
			point = new SyncPoint( label, tag, handles, SyncPoint.Status.ANNOUNCED, registrant );

			syncPoints.put( label, point );
		}
		else
		{
			point.setTag( tag );
			point.setFederates( handles );
			point.setStatus( SyncPoint.Status.ANNOUNCED );
		}
	}

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
	
	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Convenience Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Return a map of all the sync points that have been achieved by the given federate handle.
	 * The keys for the map are the labels, the values are the tags that were used when the sync
	 * points were announced. This is generally only used for role call information so the tag is
	 * needed in order to allow a point to be announced properly to a local federate if it hasn't
	 * already been so. Note that only federation-wide sync points will be considered. Restricted
	 * points are ignored by this method because a newly joined federate couldn't be part of a
	 * restricted point that had already been registered (as its handle wouldn't have existed yet,
	 * and so it couldn't be in the set of federate handles the point is restricted to).
	 */
	public HashMap<String,byte[]> getAchieved( int federateHandle )
	{
		HashMap<String,byte[]> map = new HashMap<String,byte[]>();
		for( SyncPoint point : syncPoints.values() )
		{
			if( point.isFederationWide() && point.hasFederateAchieved(federateHandle) )
				map.put( point.label, point.tag );
		}
		
		return map;
	}
	
	/**
	 * Returns <code>true</code> if the manager knows about the point AND the status for the point
	 * is {@link Status#PENDING}. Returns <code>false</code> otherwise.
	 */
	public boolean isPending( String label )
	{
		SyncPoint point = syncPoints.get( label );
		if( point == null )
			return false;
		else
			return point.getStatus() == Status.PENDING;
	}
	
	/**
	 * Returns <code>true</code> if the manager knows about the point AND the status for the point
	 * is {@link Status#REQUESTED}. Returns <code>false</code> otherwise.
	 */
	public boolean isRequested( String label )
	{
		SyncPoint point = syncPoints.get( label );
		if( point == null )
			return false;
		else
			return point.getStatus() == Status.REQUESTED;
	}

	/**
	 * Fills the {@link RoleCall} message with the required sync point information.
	 */
	public void fillRolecall( RoleCall rolecall )
	{
		for( SyncPoint point : syncPoints.values() )
		{
			SyncPoint.Status status = point.getStatus();
			if( point.isFederationWide() && (status == SyncPoint.Status.ANNOUNCED ||
			                                 status == SyncPoint.Status.ACHIEVED) )
			{
				boolean achieved = (status == SyncPoint.Status.ACHIEVED);
				rolecall.getSyncPointStatus().put( point.label, achieved );
				rolecall.getSyncPointTags().put( point.label, point.tag );
			}
		}
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
