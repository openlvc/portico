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
package org.portico2.lrc.services.time.outgoing;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JTimeConstrainedWasNotEnabled;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.DisableTimeConstrained;
import org.portico2.lrc.LRCMessageHandler;

public class DisableTimeConstrainedHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		// basic state validity checks
		lrcState.checkJoined();         // FederateNotExecutionMember
		lrcState.checkSave();           // SaveInProgress
		lrcState.checkRestore();        // RestoreInProgress

		DisableTimeConstrained request = context.getRequest( DisableTimeConstrained.class, this );
		TimeStatus ourStatus = timeStatus();

		if( logger.isDebugEnabled() )
			logger.debug( "REQUEST Disable time constrained for ["+moniker()+"]" );

		if( ourStatus.getConstrained() == TimeStatus.TriState.OFF )
			throw new JTimeConstrainedWasNotEnabled("");
		
		// send the request to the RTI
		connection.sendControlRequest( context );
		
		// check for problems
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		// things were OK, record it and do any follow-up checks
		ourStatus.setConstrained( TimeStatus.TriState.OFF );
		lrcState.getQueue().becameUnconstrained();
		
		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS Disabled time constrained for ["+moniker()+"]" );
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
