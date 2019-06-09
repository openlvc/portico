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
package org.portico2.lrc.services.object.incoming;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.lrc.LRCMessageHandler;

/**
 * Handle requests from the RTI to provide updates. These actually come wrapped in a
 * {@link RequestObjectUpdate} request message.
 */
public class ProvideObjectUpdateHandler extends LRCMessageHandler
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
	public void process( MessageContext context ) throws JException
	{
		RequestObjectUpdate request = context.getRequest( RequestObjectUpdate.class, this );
		int objectHandle = request.getObjectId();
		Set<Integer> attributeHandles = request.getAttributes();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Provide object update: object="+objectMoniker(objectHandle)+
			              ", attributes="+acMoniker(attributeHandles));
		}
		
		// Let through to the callback handler
		// We used to filter attributes here, but that happens in the RTI now
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
