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
package org.portico.lrc.services.saverestore.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JSaveNotInitiated;
import org.portico.lrc.services.saverestore.msg.SaveBegun;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=SaveBegun.class)
public class SaveBegunHandler extends LRCMessageHandler
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
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkRestore();

		// check to make sure there is an active save request
		if( saveManager.isInProgress() == false )
			throw new JSaveNotInitiated( "Can't begin federate save, no save initiated" );
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "NOTICE  Local federate ["+moniker()+"] has begun saving (active label="+
			              saveManager.getActiveLabel()+")" );
		}
		
		saveManager.federateSaveBegun( federateHandle() );
		
		// pass the request on to the federation
		connection.broadcast( (SaveBegun)context.getRequest() );
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
