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

import org.portico.lrc.services.saverestore.msg.SaveComplete;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Generates federationSaved() & federationNotSaved() callbacks to a HLA 1.3 compliant
 * federate ambassador
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=SaveComplete.class)
public class SaveCompleteCallbackHandler extends HLA13CallbackHandler
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
		SaveComplete request = context.getRequest( SaveComplete.class, this );
		boolean success = request.isSuccessful();

		if( success )
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK federationSaved()" );
			
			if( isStandard() )
				hla13().federationSaved();
			else
				java1().federationSaved();
		}
		else
		{
			if( logger.isTraceEnabled() )
				logger.trace( "CALLBACK federationNotSaved()" );
			
			if( isStandard() )
				hla13().federationNotSaved();
			else
				java1().federationNotSaved();
		}

		// mark the call as successful
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
