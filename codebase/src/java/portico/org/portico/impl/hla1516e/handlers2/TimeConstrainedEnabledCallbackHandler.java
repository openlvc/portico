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
package org.portico.impl.hla1516e.handlers2;

import java.util.Map;

import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.exceptions.FederateInternalError;

public class TimeConstrainedEnabledCallbackHandler extends LRC1516eCallbackHandler
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
	public void callback( MessageContext context ) throws FederateInternalError
	{
		LogicalTime<?,?> currentTime = new DoubleTime( lrcState.getCurrentTime() );
		logger.trace( "CALLBACK timeConstrainedEnabled(time="+currentTime+")" );
		fedamb().timeConstrainedEnabled( currentTime );
		helper.reportServiceInvocation( "timeConstrainedEnabled", true, null, currentTime );
		logger.trace( "         timeConstrainedEnabled() callback complete" );
		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
