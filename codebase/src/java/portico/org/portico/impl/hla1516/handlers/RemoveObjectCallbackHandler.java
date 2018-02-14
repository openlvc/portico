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
package org.portico.impl.hla1516.handlers;

import hla.rti1516.FederateAmbassador;

import java.util.Map;

import org.portico.impl.hla1516.Impl1516Helper;
import org.portico.impl.hla1516.types.DoubleTime;
import org.portico.impl.hla1516.types.HLA1516ObjectInstanceHandle;
import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DeleteObject;

/**
 * Generate removeObjectInstance() callbacks to a HLA 1.3 compliant federate ambassador
 */
@MessageHandler(modules="lrc1516-callback",
                keywords="lrc1516",
                sinks="incoming",
                priority=3,
                messages=DeleteObject.class)
public class RemoveObjectCallbackHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl1516Helper helper;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.helper = (Impl1516Helper)lrc.getSpecHelper();
	}
	
	public void process( MessageContext context ) throws Exception
	{
		DeleteObject request = context.getRequest( DeleteObject.class, this );
		int objectHandle = request.getObjectHandle();
		double timestamp = request.getTimestamp();

		// do the callback
		FederateAmbassador fedamb = helper.getFederateAmbassador();
		if( request.isTimestamped() )
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK removeObjectInstance(object="+objectHandle+
				              ",time="+timestamp+") (TSO)" );
			}
			
			fedamb.removeObjectInstance( new HLA1516ObjectInstanceHandle(objectHandle),
			                             request.getTag(),           // tag
			                             null,                       // sent order
			                             new DoubleTime(timestamp),  // time
			                             null );                     // received order
		}
		else
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK removeObjectInstance(object="+objectHandle+") (RO)" );
			
			fedamb.removeObjectInstance( new HLA1516ObjectInstanceHandle(objectHandle),
			                             request.getTag(),           // tag
			                             null );                     // sent order
		}
		
		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
