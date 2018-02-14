/*
 *   Copyright 2006 The Portico Project
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
package org.portico2.rti.services.time.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;
import org.portico2.common.services.time.data.TAR;
import org.portico2.common.services.time.data.TimeStatus;

/**
 * This class is the central manager and keeper of time related information for each of the
 * federates in a federation. The time status for each federate is maintained and controlled here.
 * <p/>
 * <b>NOTE:</b> The various methods of this class do as they are told, no validation is completed.
 * Before methods are invoked on the {@link TimeManager}, all the necessary checks should be done
 * (such as whether the new time for a federate is above its current time before setting it). Also,
 * after some methods, further checks should be done to see if the status of the any federates
 * can change. For example, when a federate disables time regulation, it is important that a check
 * be done to see if there are any other federates that can now have a pending time advance
 * request granted. Nothing like this is done inside the {@link TimeManager}, it just maintains
 * state, leaving these considerations up to the developer.
 * <p/>
 * <b>NOTE:</b> The {@link TimeManager} needs to be notified whenever federates join to or resign
 * from a federation in order to make sure that a {@link TimeStatus} entity exists or is removed
 * for them.
 * <p/>
 * <b>IMPLEMENTATION NOTE:</b> It is important to note that all the methods of this class will
 * not check that a federate exists before attempting to get its time status information. This
 * means that if you attempt to get, for example, the current time of a federate that isn't in
 * the federation (or null, or any other invalid key), you will get a NullPointerException. It
 * is expected that the various handlers will have taken care to ensure that a federate exists
 * before calling these methods. To be fair, this should never really be a problem.
 */
public class TimeManager implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Integer,TimeStatus> timeStatus;
	private Set<Integer>            regulating;
	private Set<Integer>            constrained;
	private double                  lbts;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TimeManager()
	{
		this.timeStatus  = new HashMap<Integer,TimeStatus>();
		this.regulating  = new HashSet<Integer>();
		this.constrained = new HashSet<Integer>();
		this.lbts        = Double.MAX_VALUE;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////
	////////////////////// Helper Methods //////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * This method will interrogate the current time status of each of the recorded federates and
	 * will determine the LBTS for *the federation* (that is, the lowest relevant LBTS of any
	 * federate). Once it is been determined, the <code>lbts</code> property of the manager will
	 * be set to the value. The new federation-lbts will be returned.
	 */
	public double recalculateLBTS()
	{
		// if there are no regulating federates, reset the LBTS
		if( this.regulating.size() == 0 )
		{
			this.lbts = Double.MAX_VALUE;
			// the removal of all regulating federates might mean advances are possible for
			// constrained federates, thus we should return true
			return this.lbts;
		}
			
		// there are regulating federates, process them for a possible change
		double lowestLBTS = Double.MAX_VALUE;
		for( Integer federateHandle : regulating )
		{
			TimeStatus status = timeStatus.get( federateHandle );
			if( status.lbts < lowestLBTS )
				lowestLBTS = status.lbts;
		}
		
		// reset the current federation LBTS
		if( this.lbts != lowestLBTS )
			this.lbts = lowestLBTS;
		
		return this.lbts;
	}
	
	/**
	 * Advances the {@link TimeStatus} of the given federate to the value it requested using
	 * {@link TimeStatus#advanceFederate(double)}.
	 */
	public void advanceFederate( int federateHandle )
	{
		TimeStatus status = timeStatus.get( federateHandle );
		status.advanceFederate( status.requestedTime );
	}
	
	/**
	 * Gets the {@link TimeStatus} instance containing the time-data for the given federate.
	 * If there is no information for the given federate, null is returned.
	 */
	public TimeStatus getTimeStatus( int federate )
	{
		return this.timeStatus.get( federate );
	}

	////////////////////////////////////////////////////////////
	/////////////////// General Time Methods ///////////////////
	////////////////////////////////////////////////////////////
	/**
	 * This method fetches the "current time" for the given federate. The current time of a federate
	 * is the last time they were granted an advance to, even if they have requested another
	 * advancement past that point.
	 */
	public double getCurrentTime( int federate )
	{
		return timeStatus.get(federate).currentTime;
	}
	
	/**
	 * Shortcut to set the current time on the time status for the given federate
	 */
	public void setCurrentTime( int federate, double time )
	{
		TimeStatus ts = timeStatus.get( federate );
		
		// update the time and LBTS for the federate in question
		ts.currentTime = time;
		ts.lbts = ts.currentTime + ts.lookahead;
		
		// if this federate is regulating, recalculate the federation-wide LBTS
		if( ts.regulating == TimeStatus.TriState.ON )
			recalculateLBTS();
	}
	
	/**
	 * Returns the last time that a federate requested to be advanced to. This should always be
	 * equal to or greater than the {@link #getCurrentTime(int) current time}.
	 */
	public double getRequestedTime( int federate )
	{
		return timeStatus.get(federate).requestedTime;
	}

	/**
	 * Shortcut to set the requested time on the time status for the given federate
	 */
	public void setRequestedTime( int federate, double time )
	{
		timeStatus.get(federate).requestedTime = time;
	}
	
	public double getLookahead( int federate )
	{
		return timeStatus.get(federate).lookahead;
	}
	
	public void setLookahead( int federate, double newLookahead )
	{
		TimeStatus ts = timeStatus.get( federate );
		ts.lookahead = newLookahead;
		ts.lbts = ts.currentTime + ts.lookahead;
		recalculateLBTS();
	}
	
	/**
	 * Fetch the LBTS for a specific federate. This is equivalent to the federates current time +
	 * its lookahead.
	 */
	public double getLBTS( int federate )
	{
		return timeStatus.get(federate).lbts;
	}

	/**
	 * Return the <b>federation-wide</b> LBTS. This is equal o the lowest LBTS of all regulating
	 * federates.
	 */
	public double getLBTS()
	{
		return lbts;
	}
	
	/**
	 * @return <code>true</code> if the given federate has an outstanding time advancement
	 * request, <code>false</code> otherwise. 
	 */
	public boolean isAdvancing( int federate )
	{
		return !(timeStatus.get(federate).advancing == TAR.NONE);
	}
	
	/**
	 * Changes the current advancing state of the given federate.
	 */
	public void setAdvancing( int federate, TAR advancing )
	{
		timeStatus.get(federate).advancing = advancing;
	}
	
	////////////////////////////////////////////////////////////
	//////////////// Regulating and Constrained ////////////////
	////////////////////////////////////////////////////////////
	/**
	 * This method marks the given federate as regulating and sets its current time and lookahead
	 * to the given values. This method will add the federate to the list of all regulating
	 * federates for the federation. <b>NOTE:</b> This method will do exactly as it is told, it
	 * <i>will not</i> perform any validity checks.
	 * <p/>
	 * For example, if all regulating federates are currently at time 100, and another federate
	 * whose time is 0 wishes to become regulating, its current time should be auto-advanced to 100.
	 * That calculation is left to the appropriate handlers. This method just alters the state.
	 */
	public void enableRegulating( int federate, double federateTime, double lookahead )
	{
		// update the time status
		TimeStatus ts = timeStatus.get( federate );
		ts.regulating = TimeStatus.TriState.ON;
		ts.currentTime = federateTime;
		ts.lookahead = lookahead;
		ts.lbts = federateTime + lookahead;
		
		// update the cache of currently regulating federates
		regulating.add( federate );
		
		// this could affect the LBTS, recalculate it
		recalculateLBTS();
	}
	
	/**
	 * This method will disable regulation for the given federate (removing it from the appropriate
	 * cache). It will then call {@link #recalculateLBTS()} to update the current LBTS for the
	 * federation. Following a call to this method, some checks to see if any previously waiting
	 * federates can now receive an advance grant is necessary.
	 */
	public void disableRegulating( int federate )
	{
		// update the time status
		timeStatus.get(federate).regulating = TimeStatus.TriState.OFF;
		// update the cache
		regulating.remove( federate );

		// this could affect the LBTS, recalculate it
		recalculateLBTS();
	}
	
	public boolean isRegulating( int federate )
	{
		return timeStatus.get(federate).regulating == TimeStatus.TriState.ON;
	}
	
	public boolean isPendingRegulation( int federate )
	{
		return timeStatus.get(federate).regulating == TimeStatus.TriState.PENDING;
	}
	
	/**
	 * Gets the set of all currently regulating federates. DO NOT MODIFY this set unless you know
	 * what you are doing. This is a reference to the underlying set and should generally be
	 * treated as read-only.
	 */
	public Set<Integer> getRegulatingFederates()
	{
		return regulating;
	}
	
	/**
	 * @return <code>true</code> if any of the currently joined federates are regulating,
	 * <code>false</code> if none are.
	 */
	public boolean hasRegulatingFederates()
	{
		return !this.regulating.isEmpty();
	}
	
	/**
	 * Adds the given federate to the set of those which are time constrained. It will also
	 * recalculate the federation-wide LBTS after this has occured.
	 */
	public void enableConstrained( int federate )
	{
		// update the time status
		TimeStatus ts = timeStatus.get( federate );
		ts.constrained = TimeStatus.TriState.ON;
		ts.lbts = ts.currentTime + ts.lookahead;
		
		// update the cache
		constrained.add( federate );

		// this could affect the LBTS, recalculate it
		recalculateLBTS();
	}
	
	/**
	 * This will disable the constrained property for the given federate and remove it from the
	 * set of those that are constrained. No further checks are done to decide whether or not the
	 * federate can advance now that it is no longer constrained, these checks should be done in
	 * a handler.
	 */
	public void disableConstrained( int federate )
	{
		// update the time status
		timeStatus.get(federate).constrained = TimeStatus.TriState.OFF;
		// update the cache
		constrained.remove( federate );
		
		// this could affect the LBTS, recalculate it
		recalculateLBTS();
	}
	
	public boolean isConstrained( int federate )
	{
		return timeStatus.get(federate).constrained == TimeStatus.TriState.ON;
	}
	
	public boolean isPendingConstrained( int federate )
	{
		return timeStatus.get(federate).constrained == TimeStatus.TriState.PENDING;
	}
	
	/**
	 * Gets the set of all currently constrained federates. DO NOT MODIFY this set unless you know
	 * what you are doing. This is a reference to the underlying set and should generally be
	 * treated as read-only.
	 */
	public Set<Integer> getConstrainedFederates()
	{
		return this.constrained;
	}
	
	/**
	 * @return <code>true</code> if any of the currently joined federates are constrained,
	 * <code>false</code> if none are.
	 */
	public boolean hasConstrainedFederates()
	{
		return !this.constrained.isEmpty();
	}
	
	////////////////////////////////////////////////////////////
	/////////////// Lifecycle Management Methods ///////////////
	////////////////////////////////////////////////////////////
	/**
	 * The federate has just joined the federation. This will create a new {@link TimeStatus} 
	 * instance for the federate and store it for later use.
	 */
	public void joinedFederation( int federate, TimeStatus existingStatus )
	{
		if( existingStatus == null )
			existingStatus = new TimeStatus();

		// create a new time status for the federate
		timeStatus.put( federate, existingStatus );
		
		// put the federate in the regulating or constrained sets if required
		if( existingStatus.constrained == TimeStatus.TriState.ON )
		{
			constrained.add( federate );
		}

		if( existingStatus.regulating == TimeStatus.TriState.ON )
		{
			regulating.add( federate );
			recalculateLBTS();
		}
	}
	
	/**
	 * The given federate has just resigned from the federation. This will remove the old
	 * {@link TimeStatus} for the federate and also make sure that the federate is no
	 * longer part of the constrained or regulating cache sets. 
	 */
	public void resignedFederation( int federate )
	{
		timeStatus.remove( federate );
		regulating.remove( federate );
		constrained.remove( federate );

		// this could affect the LBTS, recalculate it
		recalculateLBTS();
	}

	public String toString()
	{
		StringBuilder builder = new StringBuilder( "TimeManager(" );
		builder.append( hashCode() );
		builder.append( ")\n" );
		builder.append( "  regulating : " );
		builder.append( this.regulating );
		builder.append( "\n  constrained: " );
		builder.append( this.constrained );
		builder.append( "\n  time status list:" );
		for( Integer handle : timeStatus.keySet() )
		{
			builder.append( "\n    [federate: handle=" );
			builder.append( handle );
			TimeStatus status = timeStatus.get( handle );
			builder.append( "]\n      regulating : " );
			builder.append( status.regulating );
			builder.append( "\n      constrained: " );
			builder.append( status.constrained );
			builder.append( "\n      advancing  : " );
			builder.append( status.advancing );
			builder.append( "\n      current    : " );
			builder.append( status.currentTime );
			builder.append( "\n      requested  : " );
			builder.append( status.requestedTime );
			builder.append( "\n      lookahead  : " );
			builder.append( status.lookahead );
			builder.append( "\n      local lbts : " );
			builder.append( status.lbts );
		}
		
		return builder.toString();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( timeStatus );
		output.writeObject( regulating );
		output.writeObject( constrained );
		output.writeDouble( lbts );
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.timeStatus = (Map<Integer,TimeStatus>)input.readObject();
		this.regulating = (Set<Integer>)input.readObject();
		this.constrained = (Set<Integer>)input.readObject();
		this.lbts = input.readDouble();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
