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
package org.portico.impl.hla1516e;

import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.exceptions.AlreadyConnected;
import hla.rti1516e.exceptions.CallNotAllowedFromWithinCallback;
import hla.rti1516e.exceptions.ConnectionFailed;
import hla.rti1516e.exceptions.FederateIsExecutionMember;
import hla.rti1516e.exceptions.FederateNotExecutionMember;
import hla.rti1516e.exceptions.InTimeAdvancingState;
import hla.rti1516e.exceptions.InvalidLogicalTime;
import hla.rti1516e.exceptions.NotConnected;
import hla.rti1516e.exceptions.RTIinternalError;
import hla.rti1516e.exceptions.RequestForTimeConstrainedPending;
import hla.rti1516e.exceptions.RequestForTimeRegulationPending;
import hla.rti1516e.exceptions.RestoreInProgress;
import hla.rti1516e.exceptions.SaveInProgress;

import java.util.Properties;

import org.apache.logging.log4j.Logger;

import org.portico.impl.HLAVersion;
import org.portico.impl.ISpecHelper;
import org.portico.lrc.compat.JConcurrentAccessAttempted;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JConnectionFailed;
import org.portico.lrc.compat.JEnableTimeConstrainedPending;
import org.portico.lrc.compat.JEnableTimeRegulationPending;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JInvalidFederationTime;
import org.portico.lrc.compat.JRestoreInProgress;
import org.portico.lrc.compat.JSaveInProgress;
import org.portico.lrc.compat.JTimeAdvanceAlreadyInProgress;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.StringUtils;
import org.portico2.common.configuration.RID;
import org.portico2.common.messaging.ErrorResponse;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.lrc.LRC;
import org.portico2.lrc.LRCState;
/**
 * This class helps provides helper methods to the {@link Rti1516eAmbassador} class and helps
 * bridge the gap between the Portico compatibility layer and the HLA 1516e interface. The basic
 * thrust of this class (like all {@link ISpecHelper} implementations) is that it provides the
 * necessary facilities to turn HLA-interface-specific code into Portico-clean, interface
 * independant code.
 * <p/>
 * To maintain the independence of Portico from any particular HLA interface specification, this is
 * one of the *ONLY* classes that is allowed to have classes from the hla.rti namespace in it. All
 * other classes should use the facilities provided by the {@link org.portico.lrc.compat} package.
 */
public class Impl1516eHelper implements ISpecHelper
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRC lrc;                         // TODO done
	private LRCState state;                  // TODO doing
	private CallbackModel callbackModel;     // TODO doing
	
	private FederateAmbassador federateAmbassador;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Impl1516eHelper() throws RTIinternalError
	{
		// all initialization done in initialize(), which is called from connect(...).
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private void initialize( Properties ridOverrides ) throws RTIinternalError
	{
		try
		{
			this.lrc = new LRC( this, RID.loadRid(ridOverrides) );
			this.state = this.lrc.getState();
			this.callbackModel = CallbackModel.HLA_EVOKED;
		}
		catch( JConfigurationException jce )
		{
			throw new RTIinternalError( jce.getMessage(), jce );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Lifecyle Methods /////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	public void connect( FederateAmbassador federateAmbassador, CallbackModel callbackModel )
		throws AlreadyConnected, ConnectionFailed, RTIinternalError
	{
		this.connect( federateAmbassador, callbackModel, null );
	}

	/**
	 * Connect the LRC to the RTI. We take the given fedamb as a callback reference, along with
	 * the given callback model. We can also optionally pass a string for the "local settings".
	 * This string, if defined, should contain the name of a system property that we can load
	 * a Properties file from and use as the basis to augment/override the RID.
	 * 
	 * @param federateAmbassador The fedamb to call the federate back on
	 * @param callbackModel      The model to use for callbacks
	 * @param localSettingsDesignator Null, or name of system property containing a Properties
	 *                                object that has RID override values in it
	 * @throws AlreadyConnected  We're already connected
	 * @throws ConnectionFailed  We couldn't connect
	 * @throws RTIinternalError  Something abnormal happened
	 */
	public void connect( FederateAmbassador federateAmbassador,
	                     CallbackModel callbackModel,
	                     String localSettingsDesignator )
		throws AlreadyConnected, ConnectionFailed, RTIinternalError
	{
		// if we don't have an LRC yet, initialize one
		if( lrc == null )
		{
			Properties overrides = new Properties();
			
			// check to see if we have any standard overrides
			String temp = System.getProperty( "lrc.overrides" );
			if( temp != null )
				overrides.putAll( StringUtils.propertiesFromString(temp) );
			
			// check to see if we have any local settings designated
			if( localSettingsDesignator != null )
				overrides.putAll( StringUtils.propertiesFromString(localSettingsDesignator) );

			// initialize with overrides
			this.initialize( overrides );
		}
		
		// check to make sure we're not already connected
		if( lrc.isConnected() )
			throw new AlreadyConnected("");
		
		// connect the LRC
		try
		{
			lrc.connect();
		}
		catch( JConnectionFailed cf )
		{
			throw new ConnectionFailed( cf.getMessage(), cf );
		}

		// set the callback model on the LRC approrpriately
		this.callbackModel = callbackModel;
		if( callbackModel == CallbackModel.HLA_EVOKED )
			lrc.disableImmediateCallbackProcessing();
		else if( callbackModel == CallbackModel.HLA_IMMEDIATE )
			lrc.enableImmediateCallbackProcessing();
	
		// store the FederateAmbassador for now, we'll stick it on the join call shortly
		this.federateAmbassador = federateAmbassador;
	}
	
	public void disconnect() throws FederateIsExecutionMember, RTIinternalError
	{
		// make sure we're not currently involved in a federation
		if( state.isJoined() )
		{
			throw new FederateIsExecutionMember( "Can't disconnect. Joined to federation ["+
			                                     state.getFederationName()+"]" );
		}
		
		// Tell the LRC to disconnect
		try
		{
			lrc.disconnect();
		}
		finally
		{
    		// remove our federate ambassador reference to signal we're "disconnected" :P
    		this.federateAmbassador = null;
    		
    		// turn off the immediate callback handler if we have to
    		if( callbackModel == CallbackModel.HLA_IMMEDIATE )
    			lrc.disableImmediateCallbackProcessing();
		}
	}
	
	////////////////////////////////////////////////////////////////////////////
	////////////////////// Implementation Helper Methods ///////////////////////
	////////////////////////////////////////////////////////////////////////////

	public HLAVersion getHlaVersion()
	{
		return HLAVersion.IEEE1516e;
	}
	
	public void processMessage( MessageContext context ) throws Exception
	{
		this.lrc.getOutgoingSink().process( context );
	}
	
	public LRCState getState()
	{
		return this.state;
	}
	
	public LRC getLrc()
	{
		return this.lrc;
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
	public boolean evokeSingle( double timeout ) throws CallNotAllowedFromWithinCallback,
	                                                    RTIinternalError
	{
		try
		{
			return this.lrc.tickSingle( timeout );
		}
		catch( JConcurrentAccessAttempted concurrent )
		{
			throw new CallNotAllowedFromWithinCallback( concurrent.getMessage(), concurrent );
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
	public boolean evokeMultiple( double min, double max ) throws CallNotAllowedFromWithinCallback,
	                                                              RTIinternalError
	{
		try
		{
			return this.lrc.tick( min, max );
		}
		catch( JConcurrentAccessAttempted concurrent )
		{
			throw new CallNotAllowedFromWithinCallback( concurrent.getMessage(), concurrent );
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
	 * Checks to make sure the federate has connected to the RTI. Throws a {@link NotConnected}
	 * exception if it is not.
	 */
	public void checkConnected() throws NotConnected
	{
		if( this.federateAmbassador == null )
			throw new NotConnected( "Federate has not yet called connect()" );
	}

	/**
	 * Check to see if we are currently ticking (and thus not able to make an RTI callback). If
	 * we are currently ticking, a {@link hla.rti.ConcurrentAccessAttempted
	 * ConcurrentAccessAttempted} will be thrown. 
	 */
	public void checkAccess() throws CallNotAllowedFromWithinCallback
	{
		try
		{
			state.checkAccess();
		}
		catch( JConcurrentAccessAttempted ca )
		{
			throw new CallNotAllowedFromWithinCallback( ca.getMessage() );
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
	
	////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Helper  Methods //////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	public FederateAmbassador getFederateAmbassador()
	{
		return this.federateAmbassador;
	}
	
	public Logger getLrcLogger()
	{
		return this.lrc.getLogger();
	}
	
	protected void reinitializeLrc()
	{
//		this.lrc.reinitialize();
	}

	/**
	 * Reports the result of an RTIambassador service invocation to the federation via the MOM
	 * 
	 * @param serviceName the name of the service invoked
	 * @param response the response from the RTI
	 * @param parameters the parameters the service was invoked with
	 */
	public void reportServiceInvocation( String serviceName,
	                                     ResponseMessage response,
	                                     Object... parameters )
	{
		boolean reporting = this.state.isServiceReporting() || this.state.isExceptionReporting();
		if( !reporting )
			return;
		
		boolean success = response.isSuccess();
		Object returnValue = success ? response.getResult() : null;
		String errorMessage = !success ? ((ErrorResponse)response).getCause().getMessage() : null;
		
		this.lrc.reportServiceInvocation( serviceName, 
		                                  success, 
		                                  returnValue, 
		                                  errorMessage, 
		                                  parameters );
	}
	
	/**
	 * Reports the result of a service invocation to the federation via the MOM
	 * 
	 * @param serviceName the name of the service invoked
	 * @param success whether the service invocation was successful
	 * @param result if the <code>success</code> parameter is <code>true</code> this value will be 
	 *               interpreted as the value the service invocation returned. If the <code>success</code>
	 *               parameter is <code>false</code> this value will be interpreted as the error message 
	 *               that was raised
	 * @param parameters the parameters the service was invoked with
	 */
	public void reportServiceInvocation( String serviceName,
	                                     boolean success,
	                                     Object result,
	                                     Object... parameters )
	{
		boolean reporting = this.state.isServiceReporting() || this.state.isExceptionReporting();
		if( !reporting )
			return;
		
		this.lrc.reportServiceInvocation( serviceName, 
		                                  success, 
		                                  success ? result : null, 
		                                  !success ? result.toString() : null, 
		                                  parameters );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
