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
package org.portico2.rti.services.pubsub.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.pubsub.msg.PublishInteractionClass;
import org.portico2.rti.services.RTIMessageHandler;

public class PublishInteractionClassHandler extends RTIMessageHandler
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
		PublishInteractionClass request = context.getRequest( PublishInteractionClass.class, this );
		int federateHandle = request.getSourceFederate();
		int classHandle = request.getClassHandle();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Federate [%s] publishing interaction [%s]",
			              federateName(federateHandle),
			              icMoniker(classHandle) );
		}
		
		// store the interest information
		interests.publishInteractionClass( request.getSourceFederate(), classHandle );

		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Federate [%s] published interaction  [%s]",
			             federateName(federateHandle),
			             icMoniker(classHandle) );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
