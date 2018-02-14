/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.object.handlers.incoming;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.ReserveObjectName;

/**
 * This handler takes care of incoming object name reservation requests. It just stores the
 * requests in the repository and shouldn't respond in any way to the request. If there is already
 * a reservation for the name, it will be replaced if the handler of the requesting federate is
 * lower than the handle of the federate that has already reserved the name.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before any callback handler
                messages=ReserveObjectName.class)
public class ReserveObjectNameHandler extends LRCMessageHandler
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
	public void process( MessageContext context ) throws Exception
	{
		ReserveObjectName request = context.getRequest( ReserveObjectName.class, this );
		String objectName = request.getObjectName();
		int federate = request.getSourceFederate();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Request reservation of object name ["+objectName+
			              "] for federate ["+moniker(federate)+"]" );
		}
		
		// if it is not reserved and NOT the name of an existing object:   reserve it
		// if it is not reserved and IS in the name of an existing object: ignore it
		// if it is reserved and the existing reserver has LOWER handle:   ignore it
		// if it is reserved and the existing reserver has HIGHER handle:  replace reservation
		
		// check to see if there is already a reservation in place
		int reservedBy = repository.getReserverOfName( objectName );
		if( reservedBy == PorticoConstants.NULL_HANDLE )
		{
			// not reserved, record it
			repository.reserveName( federate, objectName );
			logger.debug("Reserved object name ["+objectName+"] for ["+moniker(federate)+"]");
		}
		else
		{
			// it IS reserved, if we're lower, replace it
			if( reservedBy > federate )
			{
				// requesting federate is lower, replace reservation
				repository.reserveName( federate, objectName );
				logger.debug( "Replaced previous reservation for object name ["+objectName+
				              "]: previousOwner="+moniker(reservedBy)+", newOwner="+
				              moniker(federate) );
			}
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
