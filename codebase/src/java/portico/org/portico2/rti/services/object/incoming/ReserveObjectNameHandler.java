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
package org.portico2.rti.services.object.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.ReserveObjectName;
import org.portico2.common.services.object.msg.ReserveObjectNameResult;
import org.portico2.rti.services.RTIMessageHandler;

public class ReserveObjectNameHandler extends RTIMessageHandler
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
		ReserveObjectName request = context.getRequest( ReserveObjectName.class, this );
		int federate = request.getSourceFederate();
		String objectName = request.getObjectName();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Federate [%s] requested RESERVATION of object name [%s]",
			              moniker(federate), objectName );
		}

		// Check with the repository
		if( repository.isNameReservedOrInUse(objectName) )
		{
			// FAIL! Name is in use
			ReserveObjectNameResult result = new ReserveObjectNameResult( objectName, false );
			queueUnicast( result, federate );
		}
		else
		{
			// SUCCESS! Name is now reserved
			repository.reserveName( federate, objectName );
			ReserveObjectNameResult result = new ReserveObjectNameResult( objectName, true );
			queueUnicast( result, federate );
		}
		
		// message all processed successfully
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
