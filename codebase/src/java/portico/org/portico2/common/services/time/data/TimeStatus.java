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
package org.portico2.common.services.time.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.portico2.rti.services.time.data.TimeManager;

/**
 * This class contains a bunch of information outlining the current time related status of a
 * particular federate. Once instance exists for each federate inside the {@link TimeManager}.
 */
public class TimeStatus implements Externalizable
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	public enum TriState{ ON, PENDING, OFF };

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public TriState constrained   = TriState.OFF; // is the federate constrained?
	public TriState regulating    = TriState.OFF; // is the federate regulating?
	public TAR      advancing     = TAR.NONE;     // the current time advancement request status
	public double   currentTime   = 0.0;          // the current federate time
	public double   requestedTime = 0.0;          // the time the federate last requested
	public double   lookahead     = 0.0;          // the lookahead value for the federate
	public double   lbts          = 0.0;          // the federate-lbts (requested time+lookahead)
	public boolean  asynchronous  = false;        // should "HLA messages" be delivered with TAR?

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Reset our time status back to defaults. Done when we join a federation for the first time.
	 */
	public void reset()
	{
		this.constrained   = TriState.OFF; // is the federate constrained?
		this.regulating    = TriState.OFF; // is the federate regulating?
		this.advancing     = TAR.NONE;     // the current time advancement request status
		this.currentTime   = 0.0;          // the current federate time
		this.requestedTime = 0.0;          // the time the federate last requested
		this.lookahead     = 0.0;          // the lookahead value for the federate
		this.lbts          = 0.0;          // the federate-lbts (requested time+lookahead)
		this.asynchronous  = false;        // should "HLA messages" be delivered with TAR?
	}
	
	/**
	 * This method will make all the appropriate internal changes to set the status once an advance
	 * to the given time has been granted. Note that the method won't do any checks to see if the
	 * advance is valid (for example, that it is to a time higher than previous), it will just make
	 * all the changes needed assuming that the advance is valid. This will set the advancing
	 * status of this federate to {@link TAR#PROVISIONAL}. When the grant callback is processed,
	 * this should be flipped over to {@link TAR#NONE}.
	 */
	public void advanceFederate( double newTime )
	{
		this.currentTime = newTime;
		this.lbts = this.currentTime + this.lookahead;
		this.advancing = TAR.PROVISIONAL;
	}
	
	/**
	 * Same as calling {@link #advanceFederate(double) advanceFederate(getRequestedTime())}
	 */
	public void advanceFederate()
	{
		advanceFederate( requestedTime );
	}
	
	/**
	 * Returns <code>true</code> if a time-advance grant can take place assuming that the federation
	 * LBTS is as given. No internal changes are made, only a check.
	 * <p/>
	 * This method will also return <code>false</code> if an advance has already been granted for
	 * a local federate, but that grant callback has not yet been delivered.
	 */
	public boolean canAdvance( double federationLbts )
	{
		// if there is no pending advancement, we're not ready
		if( advancing == TAR.NONE || advancing == TAR.PROVISIONAL )
			return false;
		
		// if we're not constrained we can advance all we want
		if( !isConstrained() )
			return true;
		
		// we're regulating and have an outstanding advance request, can we advance?
		// FIXME if TAR then compare with "<", if TARA the compare with "<="
		return requestedTime < federationLbts;
	}

	/**
	 * Modifies the state appropriately to reflect a new time advance request to the given time.
	 * This will not check that the change is valid, it will just blindly make the changes
	 * (set the requested time, flick the advancing flag, etc...)
	 */
	public void timeAdvanceRequested( double requestedTime )
	{
		this.requestedTime = requestedTime;
		this.lbts = this.requestedTime + this.lookahead;
		this.advancing = TAR.REQUESTED;
	}

	/**
	 * Sets the advancing status to {@link TAR#NONE}. This should be called for local federates
	 * just after their callback has been delivered. The given time is what the curren time is
	 * set to (as is the requested time).
	 */
	public void advanceGrantCallbackProcessed( double newTime )
	{
		this.advancing = TAR.NONE;
		this.currentTime = newTime;
		this.requestedTime = newTime;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Basic Get and Set Methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	public boolean isRegulating()
	{
		return regulating == TriState.ON;
	}

	public boolean isRegulatingPending()
	{
		return regulating == TriState.PENDING;
	}

	public TriState getRegulating()
	{
		return regulating;
	}

	public void setRegulating( TriState regulating )
	{
		this.regulating = regulating;
	}

	public boolean isConstrained()
	{
		return constrained == TriState.ON;
	}

	public boolean isConstrainedPending()
	{
		return constrained == TriState.PENDING;
	}

	public TriState getConstrained()
	{
		return constrained;
	}

	public void setConstrained( TriState constrained )
	{
		this.constrained = constrained;
	}

	public double getLbts()
	{
		return lbts;
	}

	public TAR getAdvancing()
	{
		return advancing;
	}
	
	/**
	 * This method returns <code>true</code> if the federate is currently waiting for a time
	 * advance grant AND once has not yet been received either internally or through a callback.
	 * There is a period of time between when a grant is granted and when the callback is delivered,
	 * this method returns <code>true</code> as soon as the advance is granted, even if the callback
	 * has not been delivered yet.
	 */
	public boolean isInAdvancingState()
	{
		if( advancing == TAR.NONE || advancing == TAR.PROVISIONAL )
			return false;
		else
			return true;
	}

	/**
	 * This method returns <code>true</code> if the federate is currently waiting for a time
	 * advance grant. <b>NOTE:</b> This includes the {@link TAR#PROVISIONAL} status, where an
	 * advance has been granted but the callback has not yet been delivered.
	 * There is a period of time between when a grant is granted and when the callback is delivered,
	 * this method returns <code>true</code> as soon as the advance is granted, even if the callback
	 * has not been delivered yet.
	 * <p/>
	 * Be careful with this, it has caused problems before. Only use it if you want to know whether
	 * the actual federate has an outstanding request that has not been satisifed through a
	 * callback yet.
	 */
	public boolean isAdvanceRequestOutstanding()
	{
		if( advancing == TAR.NONE )
			return false;
		else
			return true;
	}

	public double getCurrentTime()
	{
		return currentTime;
	}

	public void setCurrentTime( double currentTime )
	{
		this.currentTime = currentTime;
	}

	public double getRequestedTime()
	{
		return requestedTime;
	}

	public void setRequestedTime( double requestedTime )
	{
		this.requestedTime = requestedTime;
	}

	public double getLookahead()
	{
		return lookahead;
	}

	public void setLookahead( double lookahead )
	{
		this.lookahead = lookahead;
	}

	public boolean isAsynchronous()
	{
		return this.asynchronous;
	}
	
	public void setAsynchronous( boolean asynchronous )
	{
		this.asynchronous = asynchronous;
	}
	
	/**
	 * Creates and returns a new {@link TimeStatus} instance that is a direct copy of this one.
	 */
	public TimeStatus copy()
	{
		TimeStatus newStatus = new TimeStatus();
		newStatus.constrained = constrained;
		newStatus.regulating = regulating;
		newStatus.advancing = advancing;
		newStatus.currentTime = currentTime;
		newStatus.requestedTime = requestedTime;
		newStatus.lookahead = lookahead;
		newStatus.lbts = lbts;
		return newStatus;
	}
	
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( "{\n\tcurrentTime=   " );
		builder.append( currentTime );
		builder.append( "\n\trequestedTime= " );
		builder.append( requestedTime );
		builder.append( "\n\tlookahead=     " );
		builder.append( lookahead );
		builder.append( "\n\tlbts=          " );
		builder.append( lbts );
		builder.append( "\n\tconstained=    " );
		builder.append( constrained );
		builder.append( "\n\tregulating=    " );
		builder.append( regulating );
		builder.append( "\n}" );		

		return builder.toString();
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		this.constrained = (TimeStatus.TriState)input.readObject();
		this.regulating = (TimeStatus.TriState)input.readObject();
		this.advancing = (TAR)input.readObject();
		this.currentTime = input.readDouble();
		this.requestedTime = input.readDouble();
		this.lookahead = input.readDouble();
		this.lbts = input.readDouble();
		this.asynchronous = input.readBoolean();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		output.writeObject( this.constrained );
		output.writeObject( this.regulating );
		output.writeObject( this.advancing );
		output.writeDouble( this.currentTime );
		output.writeDouble( this.requestedTime );
		output.writeDouble( this.lookahead );
		output.writeDouble( this.lbts );
		output.writeBoolean( this.asynchronous );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
