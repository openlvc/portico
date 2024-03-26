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
package org.portico.impl.hla13;

import hla.rti13.java1.ConcurrentAccessAttempted;
import hla.rti13.java1.EnableTimeConstrainedPending;
import hla.rti13.java1.EnableTimeRegulationPending;
import hla.rti13.java1.FederateAmbassador;
import hla.rti13.java1.FederateNotExecutionMember;
import hla.rti13.java1.InvalidFederationTime;
import hla.rti13.java1.RTIinternalError;
import hla.rti13.java1.RestoreInProgress;
import hla.rti13.java1.SaveInProgress;
import hla.rti13.java1.SynchronizationPointLabelWasNotAnnounced;
import hla.rti13.java1.TimeAdvanceAlreadyInProgress;

import org.apache.logging.log4j.Logger;

import org.portico.impl.HLAVersion;
import org.portico.impl.ISpecHelper;
import org.portico.lrc.LRC;
import org.portico.lrc.LRCState;
import org.portico.lrc.compat.JConcurrentAccessAttempted;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JEnableTimeConstrainedPending;
import org.portico.lrc.compat.JEnableTimeRegulationPending;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JInvalidFederationTime;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JRestoreInProgress;
import org.portico.lrc.compat.JSaveInProgress;
import org.portico.lrc.compat.JSynchronizationLabelNotAnnounced;
import org.portico.lrc.compat.JTimeAdvanceAlreadyInProgress;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.MessageContext;

public class ImplJava1Helper implements ISpecHelper
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRC lrc;
	private LRCState state;
	private FederateAmbassador federateAmbassador;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ImplJava1Helper() throws RTIinternalError
	{
		try
		{
			this.lrc = new LRC( this );
			this.state = this.lrc.getState();
		}
		catch( JConfigurationException jce )
		{
			throw new RTIinternalError( jce );
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public HLAVersion getHlaVersion()
	{
		return HLAVersion.JAVA1;
	}

	public LRC getLrc()
	{
		return this.lrc;
	}

	public Logger getLrcLogger()
	{
		return this.lrc.getLrcLogger();
	}

	public FederateAmbassador getFederateAmbassador()
	{
		return this.federateAmbassador;
	}
	
	public void setFederateAmbassador( FederateAmbassador federateAmbassador )
	{
		this.federateAmbassador = federateAmbassador;
	}

	public LRCState getState()
	{
		return this.state;
	}
	
	public ObjectModel getFOM()
	{
		return this.state.getFOM();
	}

	////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Convenience Methods ////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	public void processMessage( MessageContext context ) throws Exception
	{
		this.lrc.getOutgoingSink().process( context );
	}
	
	public void tick() throws ConcurrentAccessAttempted, RTIinternalError
	{
		try
		{
			this.lrc.tick();
		}
		catch( JRTIinternalError je )
		{
			throw new RTIinternalError( je.getMessage(), je );
		}
		catch( JConcurrentAccessAttempted ce )
		{
			throw new ConcurrentAccessAttempted( ce.getMessage() );
		}
	}
	
	public boolean tick( double min, double max ) throws ConcurrentAccessAttempted, RTIinternalError
	{
		try
		{
			return this.lrc.tick( min, max );
		}
		catch( JRTIinternalError je )
		{
			throw new RTIinternalError( je.getMessage() );
		}
		catch( JConcurrentAccessAttempted ce )
		{
			throw new ConcurrentAccessAttempted( ce.getMessage() );
		}
	}

	/**
	 * Check to see if we are currently ticking (and thus not able to make an RTI callback). If
	 * we are currently ticking, a {@link hla.rti.ConcurrentAccessAttempted
	 * ConcurrentAccessAttempted} will be thrown. 
	 */
	public void checkAccess() throws ConcurrentAccessAttempted
	{
		try
		{
			state.checkAccess();
		}
		catch( JConcurrentAccessAttempted ca )
		{
			throw new ConcurrentAccessAttempted( ca.getMessage() );
		}
	}
	
	/**
	 * Check to see if we are advancing. If we are, throw an exception. 
	 */
	public void checkAdvancing() throws TimeAdvanceAlreadyInProgress
	{
		try
		{
			state.checkAdvancing();
		}
		catch( JTimeAdvanceAlreadyInProgress ca )
		{
			throw new TimeAdvanceAlreadyInProgress( ca.getMessage() );
		}
	}
	
	/**
	 * Check to see if there is a time regulation enable pending. If there is, throw an exception 
	 */
	public void checkTimeRegulation() throws EnableTimeRegulationPending
	{
		try
		{
			state.checkTimeRegulation();
		}
		catch( JEnableTimeRegulationPending erp )
		{
			throw new EnableTimeRegulationPending( erp.getMessage() );
		}
	}
	
	/**
	 * Check to see if there is a time constrained enable pending. If there is, throw an exception 
	 */
	public void checkTimeConstrained() throws EnableTimeConstrainedPending
	{
		try
		{
			state.checkTimeConstrained();
		}
		catch( JEnableTimeConstrainedPending ecp )
		{
			throw new EnableTimeConstrainedPending( ecp.getMessage() );
		}
	}
	
	/**
	 * Validate that the given time is valid for the current state (that it is equal to or greater
	 * than the current LBTS for <b>this federate</b>).
	 */
	public void checkValidTime( double time ) throws InvalidFederationTime
	{
		try
		{
			state.checkValidTime( time );
		}
		catch( JInvalidFederationTime ift )
		{
			throw new InvalidFederationTime( ift.getMessage() );
		}
	}
	
	public void checkSave() throws SaveInProgress
	{
		try
		{
			state.checkSave();
		}
		catch( JSaveInProgress sip )
		{
			throw new SaveInProgress( sip.getMessage() );
		}
	}
	
	public void checkRestore() throws RestoreInProgress
	{
		try
		{
			state.checkRestore();
		}
		catch( JRestoreInProgress sip )
		{
			throw new RestoreInProgress( sip.getMessage() );
		}
	}
	
	/**
	 * This method checks to see if the federate associated with this LRC is joined
	 * to a federation. If it is not, a FederateNotExecutionMember exception is thrown. 
	 */
	public void checkJoined() throws FederateNotExecutionMember
	{
		try
		{
			state.checkJoined();
		}
		catch( JFederateNotExecutionMember nem )
		{
			throw new FederateNotExecutionMember( nem.getMessage() );
		}
	}
	
	/**
	 * Checks to see if the given synchronization point label has been announced 
	 */
	public void checkSyncAnnounced( String label ) throws SynchronizationPointLabelWasNotAnnounced
	{
		try
		{
			state.checkSyncAnnounced( label );
		}
		catch( JSynchronizationLabelNotAnnounced na )
		{
			throw new SynchronizationPointLabelWasNotAnnounced( na.getMessage() );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
