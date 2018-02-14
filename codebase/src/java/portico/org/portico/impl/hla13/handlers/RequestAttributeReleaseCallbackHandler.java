/*
 *   Copyright 2009 The Portico Project
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
import org.portico2.common.services.ownership.msg.AttributeAcquire;

/**
 * Generates requestAttributeOwnershipRelease() callbacks.
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=AttributeAcquire.class)
public class RequestAttributeReleaseCallbackHandler extends HLA13CallbackHandler
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
		AttributeAcquire callback = context.getRequest( AttributeAcquire.class, this );
		int objectHandle = callback.getObjectHandle();
		Set<Integer> attributes = callback.getAttributes();
		byte[] tag = callback.getTag();
		
		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK requestAttributeOwnershipRelease(object="+objectHandle+
			              ",attributes="+attributes+",tagsize="+tag.length+")" );
		}
		
		if( isStandard() )
		{
			hla13().requestAttributeOwnershipRelease( objectHandle,
			                                          new HLA13AttributeHandleSet(attributes),
			                                          tag );
		}
		else
		{
			java1().requestAttributeOwnershipRelease( objectHandle,
			                                          new AttributeHandleSet(attributes),
			                                          new String(tag) );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
