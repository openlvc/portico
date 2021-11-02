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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import hla.rti1516e.AttributeHandle;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.RequestObjectUpdate;

import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.exceptions.FederateInternalError;
import org.portico2.lrc.services.object.data.LOCInstance;

public class ProvideUpdateCallbackHandler extends LRC1516eCallbackHandler
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
		RequestObjectUpdate request = context.getRequest( RequestObjectUpdate.class, this );
		ObjectInstanceHandle objectHandle = new HLA1516eHandle( request.getObjectId() );
		Set<Integer> attributes = request.getAttributes();
		AttributeHandleSet ahs = new HLA1516eAttributeHandleSet( attributes );
		byte[] tag = request.getTag();

		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK provideAttributeValueUpdate(object="+objectHandle+
			              ",attributes="+attributes+")" );
		}

		// Remove attributes from the callback that are not owned by this federate
		LOCInstance object = helper.getState().getRepository().getObject(request.getObjectId());

		Iterator<AttributeHandle> ahsIterator = ahs.iterator();
		while ( ahsIterator.hasNext() ) {
			AttributeHandle attributeHandle = ahsIterator.next();
			int attributeHandleInt = HLA1516eHandle.fromHandle(attributeHandle);

			if ( !object.getAttribute(attributeHandleInt).isOwnedBy(helper.getState().getFederateHandle()) ) {
				ahsIterator.remove();
			}
		}

		// If there are any owned attributes left, do the callback
		if ( !ahs.isEmpty() ) {
			// do the callback
			fedamb().provideAttributeValueUpdate(objectHandle,
					ahs,
					tag);
			helper.reportServiceInvocation("provideAttributeValueUpdate",
					true,
					null,
					objectHandle,
					ahs,
					tag);

		} else {
			if ( logger.isTraceEnabled() )
				logger.trace("CALLBACK provideAttributeValueUpdate(object="+objectHandle+"): no attributes owned by federate");
		}
		context.success();
		
		if( logger.isTraceEnabled() )
			logger.trace( "         provideAttributeValueUpdate() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
