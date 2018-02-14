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
package org.portico.impl.hla13.handlers;

import java.util.Map;

import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DiscoverObject;

/**
 * Generate discoverObjectInstance() callbacks to a HLA 1.3 compliant federate ambassador
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=DiscoverObject.class)
public class DiscoverObjectCallbackHandler extends HLA13CallbackHandler
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
		if( isStandard() )
			hla13().discoverObjectInstance( objectHandle, classHandle, objectName );
		else
			java1().discoverObjectInstance( objectHandle, classHandle, objectName );
		
		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
