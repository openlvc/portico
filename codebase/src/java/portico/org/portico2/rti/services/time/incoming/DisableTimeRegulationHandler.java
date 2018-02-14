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
package org.portico2.rti.services.time.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.msg.DisableTimeRegulation;
import org.portico2.rti.services.RTIMessageHandler;

public class DisableTimeRegulationHandler extends RTIMessageHandler
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
		DisableTimeRegulation request = context.getRequest( DisableTimeRegulation.class, this );
		int federate = request.getSourceFederate();

		logger.debug( "@REMOTE Federate ["+moniker(federate)+"] DISABLED time regulation" );
		timeManager.disableRegulating( federate );
		logger.info( "DISABLE time regulating for ["+moniker(federate)+"] is disabled" );
		
		// check to see if anyone needs an advance in light of time regulation being disabled
		queueDummyAdvance();
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
