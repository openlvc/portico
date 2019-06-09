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
import org.portico2.common.services.time.msg.EnableTimeConstrained;
import org.portico2.rti.services.RTIMessageHandler;

public class EnableTimeConstrainedHandler extends RTIMessageHandler
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
		EnableTimeConstrained request = context.getRequest( EnableTimeConstrained.class, this );
		int federate = request.getSourceFederate();

		logger.debug( "@REMOTE Federate ["+moniker(federate)+"] requests ENABLE time constrained" );
		
		/////////////////////////////////////
		// determine new time for federate //
		/////////////////////////////////////
		// We used to bump the federate time up to the LBTS of the federation if it were to low,
		// however, in order to maintain a link to DMSO, we now just do what it does, which is 
		// give the federate its current time as the new time.
		timeManager.enableConstrained( federate );
		logger.info( "ENABLED time constrained for ["+moniker(federate)+"]" );

		// OLD WAY:
		// FEDERATE TIME >  FEDERATION LBTS: Use federate time
		// FEDERATE TIME <= FEDERATION LBTS: Use federation lbts

		// Mark as a success and return
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
