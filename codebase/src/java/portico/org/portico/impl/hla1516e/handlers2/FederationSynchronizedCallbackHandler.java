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

import org.portico.impl.hla1516e.types.HLA1516eFederateHandleSet;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.sync.msg.FederationSynchronized;

import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.exceptions.FederateInternalError;

public class FederationSynchronizedCallbackHandler extends LRC1516eCallbackHandler
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
		FederationSynchronized notice = context.getRequest( FederationSynchronized.class, this );
		String label = notice.getLabel();

		// let the fedamb know
		logger.trace( "CALLBACK federationSynchronized(label="+label+")" );
		FederateHandleSet federates = new HLA1516eFederateHandleSet();
		fedamb().federationSynchronized( label, federates );
		helper.reportServiceInvocation( "federationSynchronized", 
		                                true, 
		                                null, 
		                                label,
		                                federates);
		logger.trace( "         federationSynchronized() callback complete" );
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
