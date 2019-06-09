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

import hla.rti13.java1.AttributeHandleSet;

import java.util.Map;
import java.util.Set;

import org.portico.impl.hla13.types.HLA13AttributeHandleSet;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.RequestObjectUpdate;

/**
 * Generate provideAttributeValueUpdate() callbacks to a HLA 1.3 compliant federate ambassador
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=RequestObjectUpdate.class)
public class ProvideUpdateCallbackHandler extends HLA13CallbackHandler
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
		if( isStandard() )
			hla13().provideAttributeValueUpdate( objectHandle, new HLA13AttributeHandleSet(attributes) );
		else
			java1().provideAttributeValueUpdate( objectHandle, new AttributeHandleSet(attributes) );

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
