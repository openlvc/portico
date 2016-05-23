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
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * This handler takes AttributeRelease notifications and turns them into
 * <code>requestAttributeOwnershipAssumption()</code> callbacks. Make sure that the only attributes
 * in the request are those that are available to be acquired!
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords= {"lrc1516e"},
                sinks="incoming",
                priority=3,
                messages=AttributeDivest.class)
public class AssumptionRequestCallbackHandler extends HLA1516eCallbackHandler
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
		AttributeDivest request = context.getRequest( AttributeDivest.class, this );
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK requestAttributeOwnershipAssumption(object="+objectHandle+
			              ",attributes="+attributes+")" );
		}
		
		HLA1516eAttributeHandleSet handleSet = new HLA1516eAttributeHandleSet( attributes );
		HLA1516eHandle hlaHandle = new HLA1516eHandle( objectHandle );
		fedamb().requestAttributeOwnershipAssumption( hlaHandle, handleSet, null );
		context.success();

		logger.trace( "         requestAttributeOwnershipAssumption() callback complete" );

	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
