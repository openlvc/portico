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
package org.portico.impl.hla1516;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.ISpecHelper;
import org.portico.lrc.LRC;
import org.portico.lrc.LRCState;
import org.portico.lrc.compat.*;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.MessageContext;

import hla.rti1516.*;

/**
 * This class helps provides helper methods to the {@link Rti1516Ambassador} class and helps
 * bridge the gap between the Portico compatibility layer and the HLA 1516 interface. The basic
 * thrust of this class (like all {@link ISpecHelper} implementations) is that it provides the
 * necessary facilities to turn HLA-interface-specific code into Portico-clean, interface
 * independant code.
 * <p/>
 * To maintain the independence of Portico from any particular HLA interface specification, this is
 * one of the *ONLY* classes that is allowed to have classes from the hla.rti namespace in it. All
 * other classes should use the facilities provided by the {@link org.portico.lrc.compat} package.
 */
public class Impl1516Helper implements ISpecHelper
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRC lrc;
	private LRCState state;
	
	private FederateAmbassador fedamb;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Impl1516Helper() throws RTIinternalError
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
		return HLAVersion.IEEE1516;
	}
	
	public void processMessage( MessageContext context ) throws Exception
	{
		this.lrc.getOutgoingSink().process( context );
	}
	
	public LRCState getState()
	{
		return this.state;
	}
	
	public ObjectModel getFOM()
	{
		return this.state.getFOM();
	}
	
	/**
	 * Process a single callback, waiting as long as the given timeout (in seconds) for one if
	 * there are none pendings. Return true if there are more messages waiting to be processed
	 * or false if there are none.
	 * 
	 * @param timeout The length of time to wait if there are no callbacks to process (in seconds)
	 * @return True if there are still more callbacks that can be processed, false otherwise
	 */
	public boolean evokeSingle( double timeout ) throws RTIinternalError
	{
		try
		{
			return this.lrc.tickSingle( timeout );
		}
		catch( Exception e )
		{
			throw new RTIinternalError( e.getMessage(), e );
		}
	}

	/**
	 * Try and process as many messages as possible. Spend up to <code>max</code> time doing so.
	 * If there are none to process, wait only as long as <code>min</code>. Return true if there
	 * are more callbacks that could be processed, false otherwise
	 *  
	 * @param min The minimum amount of time (in seconds) to wait if there are no callbacks
	 * to process
	 * @param max The maximum amount of time to process messages for (in seconds)
	 * @return True if there are still more callbacks that can be processed, false otherwise
	 */
	public boolean evokeMultiple( double min, double max ) throws RTIinternalError
	{
		try
		{
			return this.lrc.tick( min, max );
		}
		catch( Exception e )
		{
			throw new RTIinternalError( e.getMessage(), e );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Convenience Methods ////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Check to see if we are currently ticking (and thus not able to make an RTI callback). If
	 * we are currently ticking, a {@link hla.rti.ConcurrentAccessAttempted
	 * ConcurrentAccessAttempted} will be thrown. 
	 */
	public void checkAccess() throws RTIinternalError
	{
		try
		{
			state.checkAccess();
		}
		catch( JConcurrentAccessAttempted ca )
		{
			throw new RTIinternalError( ca.getMessage() );
		}
	}
	
	/**
	 * Check to see if we are advancing. If we are, throw an exception. 
	 */
	public void checkAdvancing() throws InTimeAdvancingState
	{
		try
		{
			state.checkAdvancing();
		}
		catch( JTimeAdvanceAlreadyInProgress ca )
		{
			throw new InTimeAdvancingState( ca.getMessage() );
		}
	}
	
	/**
	 * Check to see if there is a time regulation enable pending. If there is, throw an exception 
	 */
	public void checkTimeRegulation() throws RequestForTimeRegulationPending
	{
		try
		{
			state.checkTimeRegulation();
		}
		catch( JEnableTimeRegulationPending erp )
		{
			throw new RequestForTimeRegulationPending( erp.getMessage() );
		}
	}
	
	/**
	 * Check to see if there is a time constrained enable pending. If there is, throw an exception 
	 */
	public void checkTimeConstrained() throws RequestForTimeConstrainedPending
	{
		try
		{
			state.checkTimeConstrained();
		}
		catch( JEnableTimeConstrainedPending ecp )
		{
			throw new RequestForTimeConstrainedPending( ecp.getMessage() );
		}
	}
	
	/**
	 * Validate that the given time is valid for the current state (that it is equal to or greater
	 * than the current LBTS for <b>this federate</b>).
	 */
	public void checkValidTime( double time ) throws InvalidLogicalTime
	{
		try
		{
			state.checkValidTime( time );
		}
		catch( JInvalidFederationTime ift )
		{
			throw new InvalidLogicalTime( ift.getMessage() );
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
	public void checkSyncAnnounced( String label ) throws SynchronizationPointLabelNotAnnounced
	{
		try
		{
			state.checkSyncAnnounced( label );
		}
		catch( JSynchronizationLabelNotAnnounced na )
		{
			throw new SynchronizationPointLabelNotAnnounced( na.getMessage() );
		}
	}

	////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Helper  Methods //////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	public FederateAmbassador getFederateAmbassador()
	{
		return this.fedamb;
	}
	
	public void setFederateAmbassador( FederateAmbassador fedamb )
	{
		this.fedamb = fedamb;
	}

	public Logger getLrcLogger()
	{
		return this.lrc.getLrcLogger();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
