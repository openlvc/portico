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
import org.portico.lrc.services.ownership.msg.AttributesUnavailable;
import org.portico2.common.messaging.MessageContext;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class AttributesUnavailableCallbackHandler extends LRC1516eCallbackHandler
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
		AttributesUnavailable unavailable = context.getRequest( AttributesUnavailable.class, this );
		vetoUnlessFromUs( unavailable );
		ObjectInstanceHandle objectHandle = new HLA1516eHandle( unavailable.getObjectHandle() );
		Set<Integer> attributes = unavailable.getAttributeHandles();
		AttributeHandleSet handleSet = new HLA1516eAttributeHandleSet( attributes );
		
		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK attributeOwnershipUnavailable(object="+objectHandle+
			              ",attributes="+attributes+")" );
		}

		
		fedamb().attributeOwnershipUnavailable( objectHandle, handleSet );
		helper.reportServiceInvocation( "attributeOwnershipUnavailable", 
		                                true, 
		                                null, 
		                                objectHandle, 
		                                handleSet );
		
		context.success();
		
		logger.trace( "         attributeOwnershipUnavailable() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
