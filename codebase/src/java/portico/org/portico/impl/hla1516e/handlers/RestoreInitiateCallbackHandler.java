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

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.lrc.services.saverestore.msg.RestoreInitiate;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Generates initiateFederateRestore() callbacks to a IEEE-1516e compliant federate ambassador
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords= {"lrc1516e"},
                sinks="incoming",
                priority=3,
                messages=RestoreInitiate.class)
public class RestoreInitiateCallbackHandler extends HLA1516eCallbackHandler
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
		RestoreInitiate callback = context.getRequest( RestoreInitiate.class, this );
		String label = callback.getLabel();
		int federateHandle = callback.getFederateHandle();
		
		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK initiateFederateRestore(label="+label+",federateHandle="+
			              federateHandle+")" );
		}
		
		fedamb().initiateFederateRestore( label,
		                                  helper.getState().getFederateName(),
		                                  new HLA1516eHandle(federateHandle) );

		// mark the call as successful
		context.success();
		
		logger.trace( "         initiateFederateRestore() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
