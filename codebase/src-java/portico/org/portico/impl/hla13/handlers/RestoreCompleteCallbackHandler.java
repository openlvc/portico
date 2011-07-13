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
package org.portico.impl.hla13.handlers;

import java.util.Map;

import org.portico.lrc.services.saverestore.msg.RestoreComplete;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Generates federationRestored() & federationNotRestored() callbacks to a HLA 1.3 compliant
 * federate ambassador
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=RestoreComplete.class)
public class RestoreCompleteCallbackHandler extends HLA13CallbackHandler
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
		RestoreComplete request = context.getRequest( RestoreComplete.class, this );
		boolean success = request.isSuccessful();

		if( success )
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK federationRestored()" );
			
			if( isStandard() )
				hla13().federationRestored();
			else
				java1().federationRestored();
		}
		else
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK federationNotRestored()" );
			
			if( isStandard() )
				hla13().federationNotRestored();
			else
				java1().federationNotRestored();
		}

		// mark the call as successful
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
