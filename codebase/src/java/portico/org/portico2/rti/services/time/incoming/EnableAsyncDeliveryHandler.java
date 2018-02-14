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

import org.portico.lrc.compat.JAsynchronousDeliveryAlreadyEnabled;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.EnableAsynchronousDelivery;
import org.portico2.rti.services.RTIMessageHandler;

public class EnableAsyncDeliveryHandler extends RTIMessageHandler
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
		EnableAsynchronousDelivery request = context.getRequest( EnableAsynchronousDelivery.class, this );
		int federate = request.getSourceFederate();
		
		if( logger.isDebugEnabled() )
			logger.debug( "@REMOTE Federate ["+moniker(federate)+"] requests ENABLE asynchronous delivery" );

		TimeStatus status = timeManager.getTimeStatus( federate );
		if( status == null )
			throw new JRTIinternalError( "Fetched time status for unknown federate: %d", federate );
		else if( status.isAsynchronous() )
			throw new JAsynchronousDeliveryAlreadyEnabled();
		else
			status.setAsynchronous( true );
		
		if( logger.isDebugEnabled() )
			logger.debug( "SUCCESS Enabled asynchronous delivery for ["+moniker(federate)+"]" );

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
