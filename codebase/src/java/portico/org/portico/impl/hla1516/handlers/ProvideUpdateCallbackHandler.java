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
package org.portico.impl.hla1516.handlers;

import hla.rti1516.FederateAmbassador;

import java.util.Map;
import java.util.Set;

import org.portico.impl.hla1516.Impl1516Helper;
import org.portico.impl.hla1516.types.HLA1516AttributeHandleSet;
import org.portico.impl.hla1516.types.HLA1516ObjectInstanceHandle;
import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.RequestObjectUpdate;

/**
 * Generate provideAttributeValueUpdate() callbacks to a IEEE1516 compliant federate ambassador
 */
@MessageHandler(modules="lrc1516-callback",
                keywords="lrc1516",
                sinks="incoming",
                priority=3,
                messages=RequestObjectUpdate.class)
public class ProvideUpdateCallbackHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl1516Helper helper;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.helper = (Impl1516Helper)lrc.getSpecHelper();
	}
	
	public void process( MessageContext context ) throws Exception
	{
		RequestObjectUpdate request = context.getRequest( RequestObjectUpdate.class, this );
		int objectHandle = request.getObjectId();
		Set<Integer> attributes = request.getAttributes();

		if( logger.isTraceEnabled() )
		{
			logger.trace( "provideAttributeValueUpdate(object="+objectHandle+
			              ",attributes="+attributes+")" );
		}
		
		// do the callback
		FederateAmbassador fedamb = helper.getFederateAmbassador();
		fedamb.provideAttributeValueUpdate( new HLA1516ObjectInstanceHandle(objectHandle),
		                                    new HLA1516AttributeHandleSet(attributes),
		                                    request.getTag() );

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
