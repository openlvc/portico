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

import java.util.Map;
import java.util.Set;

import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.RequestObjectUpdate;

/**
 * Generate provideAttributeValueUpdate() callbacks to a IEEE1516e compliant federate ambassador
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords="lrc1516e",
                sinks="incoming",
                priority=3,
                messages=RequestObjectUpdate.class)
public class ProvideUpdateCallbackHandler extends HLA1516eCallbackHandler
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
		RequestObjectUpdate request = context.getRequest( RequestObjectUpdate.class, this );
		int objectHandle = request.getObjectId();
		Set<Integer> attributes = request.getAttributes();

		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK provideAttributeValueUpdate(object="+objectHandle+
			              ",attributes="+attributes+")" );
		}
		
		// do the callback
		fedamb().provideAttributeValueUpdate( new HLA1516eHandle(objectHandle),
		                                      new HLA1516eAttributeHandleSet(attributes),
		                                      request.getTag() );

		context.success();
		
		if( logger.isTraceEnabled() )
			logger.trace( "         provideAttributeValueUpdate() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
