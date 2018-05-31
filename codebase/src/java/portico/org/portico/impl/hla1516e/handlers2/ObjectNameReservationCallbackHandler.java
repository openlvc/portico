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
package org.portico.impl.hla1516e.handlers2;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.ReserveObjectNameResult;

import hla.rti1516e.exceptions.FederateInternalError;

public class ObjectNameReservationCallbackHandler extends LRC1516eCallbackHandler
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
		ReserveObjectNameResult message = context.getRequest( ReserveObjectNameResult.class, this );
		String name = message.getObjectName();

		// do the callback
		if( message.isSuccessful() )
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK objectInstanceNameReservationSucceeded(name="+name+")" );

			fedamb().objectInstanceNameReservationSucceeded( name );
			helper.reportServiceInvocation( "objectInstanceNameReservationSucceeded", true, null, name );
			
			if( logger.isTraceEnabled() )
				logger.trace( "         objectInstanceNameReservationSucceeded() callback complete" );
		}
		else
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK objectInstanceNameReservationFailed(name="+name+")" );

			fedamb().objectInstanceNameReservationFailed( name );
			helper.reportServiceInvocation( "objectInstanceNameReservationFailed", true, null, name );
			
			if( logger.isTraceEnabled() )
				logger.trace( "         objectInstanceNameReservationFailed() callback complete" );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
