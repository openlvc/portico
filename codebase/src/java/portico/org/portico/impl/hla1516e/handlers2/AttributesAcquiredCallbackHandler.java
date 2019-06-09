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
import org.portico.lrc.services.ownership.msg.OwnershipAcquired;
import org.portico2.common.messaging.MessageContext;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;

public class AttributesAcquiredCallbackHandler extends LRC1516eCallbackHandler
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
		OwnershipAcquired acquired = context.getRequest( OwnershipAcquired.class, this );
		vetoUnlessFromUs( acquired );
		ObjectInstanceHandle objectHandle = new HLA1516eHandle( acquired.getObjectHandle() );
		Set<Integer> attributes = acquired.getAttributeHandles();
		AttributeHandleSet handleSet = new HLA1516eAttributeHandleSet( attributes );

		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK attributeOwnershipAcquisitionNotification(object="+objectHandle+
			              ",attributes="+attributes+")" );
		}

		
		fedamb().attributeOwnershipAcquisitionNotification( objectHandle,
		                                                    handleSet,
		                                                    null );
		helper.reportServiceInvocation( "attributeOwnershipAcquisitionNotification", 
		                                true, 
		                                null, 
		                                objectHandle,
		                                handleSet );
		logger.trace( "         attributeOwnershipAcquisitionNotification() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
