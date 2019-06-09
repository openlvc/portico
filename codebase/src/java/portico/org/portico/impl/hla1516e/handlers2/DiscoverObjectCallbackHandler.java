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
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.DiscoverObject;

import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class DiscoverObjectCallbackHandler extends LRC1516eCallbackHandler
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
		DiscoverObject request = context.getRequest( DiscoverObject.class, this );
		int objectHandle = request.getObjectHandle();
		int classHandle = request.getClassHandle();
		String objectName = request.getObjectName();

		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK discoverObjectInstance(object="+objectHandle+
			              ",class="+ classHandle+",name="+objectName+")" );
		}
		
		// do the callback
		ObjectInstanceHandle oHandle = new HLA1516eHandle( objectHandle );
		ObjectClassHandle cHandle = new HLA1516eHandle( classHandle );
		fedamb().discoverObjectInstance( oHandle, cHandle, objectName );
		helper.reportServiceInvocation( "discoverObjectInstance", 
		                                true, 
		                                null, 
		                                oHandle, 
		                                cHandle, 
		                                objectName );
		context.success();
		
		if( logger.isTraceEnabled() )
			logger.trace( "         discoverObjectInstance() callback complete");		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
