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

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class keeps track of all the information about a particular synchronization point. Each
 * point has an associated status that spells out where in the synchronization process the point
 * currently is. See {@link Status} for more details.
 * <p/>
 * The decision about who has the right to register a sync point is handles on a first-come,
 * first-serve apporach. 
 * <p/>
 * The point also keeps track of all the federates associated with it. Generally, a sync point will
 * be "federation-wide" (that is, meant for all federates). However, on creation, a point can have
 * restricted membership. If this is the case, only those federates whose handles are in the sync
 * point will be informed about its existence and only those federates will be considered when
 * deciding is a synchronization point has been achieved or not.
 */
public class SyncPoint implements Serializable
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	/**
	 * The current status of the sync point can be one of:
	 * <ul>
	 *  <li><b>REQUESTED</b>: the local federate has requested registration of the point</li>
	 *  <li><b>PENDING</b>: some other federate has a registration request out for this point</li>
	 *  <li><b>ANNOUNCED</b>: the point has been announced</li>
	 *  <li><b>ACHIEVED</b>: the local federate has achieved the point</li>
	 *  <li><b>SYNCHRONIZED</b>: all federates have achieved the point</li>
	 * </ul>
	 */
	public enum Status
	{
		ANNOUNCED,     // the point has been announced
		ACHIEVED,      // the local federate has achieved the point
		SYNCHRONIZED;  // all federates have achieved the point
	}

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected String label;
	protected byte[] tag;
	protected Set<Integer> federates;
	protected int registrant;
	protected HashSet<Integer> achieved;
	protected Status status;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new sync point with the given label, tag and set of federate handles who are part
	 * of the point. If it is a federation-wide sync point, pass <code>null</code> for the handles
	 * parameter (or an empty set).
	 */
	protected SyncPoint( String label, byte[] tag, Set<Integer> federates, int registrant )
	{
		this.label = label;
		this.tag = tag;
		this.achieved = new HashSet<Integer>();
		this.registrant = registrant;
		this.status = Status.ANNOUNCED;
		if( federates == null )
			this.federates = new HashSet<Integer>();
		else
			this.federates = federates;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Registration Request Handling Methods //////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	public int getRegistrant()
	{
		return registrant;
	}
	
	/**
	 * Returns <code>true</code> if this sycn point is a federation-wide (non-restricted) point
	 * that all federates currently in a federation need to have achieved before it becomes
	 * synchronized. This method should always return the opposite of {@link #isRestricted()}.
	 */
	public boolean isFederationWide()
	{
		return this.federates == null || this.federates.isEmpty();
	}
	
	/**
	 * Returns <code>true</code> if this sync point is a restricted one (that is, it isn't
	 * federation-wide). The return value of this method should always be the opposite of
	 * {@link #isFederationWide()}.
	 */
	public boolean isRestricted()
	{
		if( this.federates != null && this.federates.isEmpty() == false )
			return true;
		else
			return false;
	}

	/**
	 * The given federate has achieved this synchronization point. Record it.
	 */
	public void federateAchieved( int federateHandle )
	{
		this.achieved.add( federateHandle );
	}
	
	/**
	 * Returns <code>true</code> if the given federate has said it has achieved this point,
	 * <code>false</code> otherwise.
	 */
	public boolean hasFederateAchieved( int federateHandle )
	{
		return this.achieved.contains( federateHandle );
	}
	
	public boolean isSynchronized()
	{
		return status == Status.SYNCHRONIZED;
	}
	
	/**
	 * Tests to see if all of the federates in the given set have achieved this synchronization
	 * point yet or not.
	 * <p/>
	 * If this is a <b>federation-wide</b> synchronization point, the set of given federates should
	 * be the set of federates currently in the federation. In this case, this method will return
	 * true if all the given federate handles have been recorded as having achieved the point.
	 * <p/>
	 * If this is a <b>restricted</b> synchronization point, the set of given 
	 * 
	 * Tests to see if all the federates in the given set have achieved the point yet. If this
	 * is a private sync point, <code>null</code> should be passed for the argument (this will
	 * cause the method to use the stored set of federate handles). If this is a federation-wide
	 * sync point, a set of all the handles should be passed. Assuming that a valid set is given
	 * to the method, <code>true</code> will be returned if all the federates in the given set
	 * have achieved the point.
	 */
	public boolean isSynchronized( Set<Integer> allFederates )
	{
		// if the given set is null, it means we should defer to the set we have locally
		//                                 -OR-
		// if we are a resticted point, ignore the given federates and use our local set
		if( allFederates == null || isRestricted() )
			allFederates = this.federates;

		// check each of the fedeates to see if it has been marked as having achieved the point
		for( Integer federateHandle : allFederates )
		{
			if( achieved.contains(federateHandle) == false )
				return false;
		}
		
		return true;
	}
	
	public String getLabel()
	{
		return this.label;
	}
	
	public byte[] getTag()
	{
		return this.tag;
	}
	
	public Status getStatus()
	{
		return this.status;
	}
	
	/**
	 * Returns the set of all federate handles that are associated with this point. If this is a
	 * federation-wide point, returns an empty set.
	 */
	public Set<Integer> getFederates()
	{
		return this.federates;
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "{\n\tlabel=       " );
		builder.append( label );
		builder.append( "\n\tstatus=      " );
		builder.append( status );
		builder.append( "\n\tregistrant=  " );
		builder.append( registrant );
		builder.append( "\n\tfederates=   " );
		builder.append( federates );
		builder.append( "\n\tachieved=    " );
		builder.append( achieved );
		builder.append( "\n}" );		

		return builder.toString();
	}
}
