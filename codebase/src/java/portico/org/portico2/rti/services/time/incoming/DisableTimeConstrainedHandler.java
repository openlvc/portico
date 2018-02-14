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
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.DisableTimeConstrained;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico2.rti.services.RTIMessageHandler;

public class DisableTimeConstrainedHandler extends RTIMessageHandler
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
		DisableTimeConstrained request = context.getRequest( DisableTimeConstrained.class, this );
		int federate = request.getSourceFederate();

		logger.debug( "@REMOTE Federate ["+moniker(federate)+"] DISABLED time constrained" );
		timeManager.disableConstrained( federate );
		logger.info( "DISABLE time constrained for ["+moniker(federate)+"]" );
		
		// can we advance now? by disabling contrained we are no longer limited by
		// what other federates want to do, so let's check to see if we can
		TimeStatus status = timeManager.getTimeStatus( federate );
		if( status.isInAdvancingState() )
		{
			TimeAdvanceRequest tar = new TimeAdvanceRequest( status.getRequestedTime() );
			tar.setSourceFederate( federate );
			super.federation.getIncomingSink().process( new MessageContext(tar) );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
