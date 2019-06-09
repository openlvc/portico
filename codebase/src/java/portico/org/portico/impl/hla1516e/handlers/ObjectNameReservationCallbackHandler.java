/*
 *   Copyright 2013 The Portico Project
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
package org.portico.impl.hla1516e.handlers;

import java.util.Map;

import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.ReserveObjectNameResult;

@MessageHandler(modules="lrc1516e-callback",
                keywords= {"lrc1516e"},
                sinks="incoming",
                priority=3,
                messages=ReserveObjectNameResult.class)
public class ObjectNameReservationCallbackHandler extends HLA1516eCallbackHandler
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		ReserveObjectNameResult message = context.getRequest( ReserveObjectNameResult.class, this );
		String name = message.getObjectName();
		if( message.getSourceFederate() != lrcState.getFederateHandle() )
		{
			// this is not us - ignore (shouldn't happen, but check to be sure)
			context.success();
			return;
		}
		

		// do the callback
		if( message.isSuccessful() )
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK objectInstanceNameReservationSucceeded(name="+name+")" );

			fedamb().objectInstanceNameReservationSucceeded( name );
			
			if( logger.isTraceEnabled() )
				logger.trace( "         objectInstanceNameReservationSucceeded() callback complete" );
		}
		else
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK objectInstanceNameReservationFailed(name="+name+")" );

			fedamb().objectInstanceNameReservationFailed( name );
			
			if( logger.isTraceEnabled() )
				logger.trace( "         objectInstanceNameReservationFailed() callback complete" );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
