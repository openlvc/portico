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
package org.portico.impl.hla1516e.handlers;

import hla.rti1516e.OrderType;

import java.util.Map;

import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.impl.hla1516e.handlers2.SupplementalInfo;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DeleteObject;

/**
 * Generate removeObjectInstance() callbacks to a IEEE1516e compliant federate ambassador
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords="lrc1516e",
                sinks="incoming",
                priority=3,
                messages=DeleteObject.class)
public class RemoveObjectCallbackHandler extends HLA1516eCallbackHandler
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		DeleteObject request = context.getRequest( DeleteObject.class, this );
		int objectHandle = request.getObjectHandle();
		double timestamp = request.getTimestamp();
		
		// generate the supplemental information
		SupplementalInfo supplement = null;//new SupplementalInfo( request.getSourceFederate() );

		// do the callback
		if( request.isTimestamped() )
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK removeObjectInstance(object="+objectHandle+
				              ",time="+timestamp+") (TSO)" );
			}
			
			fedamb().removeObjectInstance( new HLA1516eHandle(objectHandle),
			                               request.getTag(),           // tag
			                               OrderType.TIMESTAMP,        // sent order
			                               new DoubleTime(timestamp),  // time
			                               OrderType.TIMESTAMP,        // received order
			                               supplement );               // supplemental remove info
		}
		else
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK removeObjectInstance(object="+objectHandle+") (RO)" );
			
			fedamb().removeObjectInstance( new HLA1516eHandle(objectHandle),
			                               request.getTag(),           // tag
			                               OrderType.RECEIVE,          // sent order
			                               supplement );               // supplemental remove info
		}
		
		context.success();
		
		if( logger.isTraceEnabled() )
			logger.trace( "         removeObjectInstance() callback complete" );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
