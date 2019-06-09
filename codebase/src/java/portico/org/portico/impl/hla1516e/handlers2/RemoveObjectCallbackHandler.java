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

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.DeleteObject;

import hla.rti1516e.LogicalTime;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.exceptions.FederateInternalError;

public class RemoveObjectCallbackHandler extends LRC1516eCallbackHandler
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
		DeleteObject request = context.getRequest( DeleteObject.class, this );
		ObjectInstanceHandle objectHandle = new HLA1516eHandle( request.getObjectHandle() );
		byte[] tag = request.getTag();
		
		// generate the supplemental information
		SupplementalInfo supplement = null;//new SupplementalInfo( request.getSourceFederate() );

		// do the callback
		if( request.isTimestamped() )
		{
			LogicalTime<?,?> timestamp = new DoubleTime( request.getTimestamp() );
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK removeObjectInstance(object="+objectHandle+
				              ",time="+timestamp+") (TSO)" );
			}
			
			fedamb().removeObjectInstance( objectHandle,
			                               tag,                        // tag
			                               OrderType.TIMESTAMP,        // sent order
			                               timestamp,                  // time
			                               OrderType.TIMESTAMP,        // received order
			                               supplement );               // supplemental remove info
			helper.reportServiceInvocation( "removeObjectInstance", 
			                                true, 
			                                null, 
			                                objectHandle,
			                                tag,
			                                OrderType.TIMESTAMP,
			                                timestamp,
			                                OrderType.TIMESTAMP,
			                                supplement );
		}
		else
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK removeObjectInstance(object="+objectHandle+") (RO)" );
			
			fedamb().removeObjectInstance( objectHandle,
			                               tag,                        // tag
			                               OrderType.RECEIVE,          // sent order
			                               supplement );               // supplemental remove info
			helper.reportServiceInvocation( "removeObjectInstance", 
			                                true, 
			                                null, 
			                                objectHandle,
			                                tag,
			                                OrderType.RECEIVE,
			                                supplement );
		}
		
		context.success();
		
		if( logger.isTraceEnabled() )
			logger.trace( "         removeObjectInstance() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
