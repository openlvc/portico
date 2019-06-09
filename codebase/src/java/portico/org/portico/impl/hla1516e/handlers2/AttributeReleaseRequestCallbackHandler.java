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
import java.util.Set;

import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.ownership.msg.AttributeAcquire;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class AttributeReleaseRequestCallbackHandler extends LRC1516eCallbackHandler
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
		AttributeAcquire callback = context.getRequest( AttributeAcquire.class, this );
		ObjectInstanceHandle objectHandle = new HLA1516eHandle( callback.getObjectHandle() );
		Set<Integer> attributes = callback.getAttributes();
		AttributeHandleSet ahs = new HLA1516eAttributeHandleSet( attributes );
		byte[] tag = callback.getTag();
		
		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK requestAttributeOwnershipRelease(object="+objectHandle+
			              ",attributes="+attributes+",tagsize="+tag.length+")" );
		}
		
		fedamb().requestAttributeOwnershipRelease( objectHandle,
		                                           ahs,
		                                           tag );
		helper.reportServiceInvocation( "requestAttributeOwnershipRelease", 
		                                true, 
		                                null,
		                                objectHandle, 
		                                ahs, 
		                                tag );
		context.success();
		
		if( logger.isTraceEnabled() )
			logger.trace( "         requestAttributeOwnershipRelease() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
