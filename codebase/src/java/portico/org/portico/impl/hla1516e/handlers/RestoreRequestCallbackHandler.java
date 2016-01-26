/*
 *   Copyright 2012 The Portico Project
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

import org.portico.lrc.services.saverestore.msg.RestoreRequestResult;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Generates requestFederationRestoreSucceeded() & requestFederationRestoreFailed() callbacks
 * to a IEEE-1516e compliant federate ambassador
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords= {"lrc1516e"},
                sinks="incoming",
                priority=3,
                messages=RestoreRequestResult.class)
public class RestoreRequestCallbackHandler extends HLA1516eCallbackHandler
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
		RestoreRequestResult request = context.getRequest( RestoreRequestResult.class, this );
		String label = request.getLabel();
		boolean success = request.getSuccessStatus();
		String reason = request.getFailureReason();

		if( success )
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK requestFederationRestoreSucceeded(label="+label+")" );
			
			fedamb().requestFederationRestoreSucceeded( label );
			
			if( logger.isTraceEnabled() )
				logger.trace( "         requestFederationRestoreSucceeded() callback complete" );
			
		}
		else
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK requestFederationRestoreFailed(label="+label+
				              ",reason=\""+reason+"\")" );
			}
			
			fedamb().requestFederationRestoreFailed( label );
			
			if( logger.isTraceEnabled() )
				logger.trace( "         requestFederationRestoreFailed() callback complete" );
		}

		// mark the call as successful
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
