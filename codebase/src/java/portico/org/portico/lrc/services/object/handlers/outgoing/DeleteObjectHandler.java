/*
 *   Copyright 2008 The Portico Project
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
package org.portico.lrc.services.object.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JDeletePrivilegeNotHeld;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DeleteObject;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=DeleteObject.class)
public class DeleteObjectHandler extends LRCMessageHandler
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
		DeleteObject request = context.getRequest( DeleteObject.class, this );
		int objectHandle = request.getObjectHandle();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		if( logger.isDebugEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.debug( "ATTEMPT Delete object ["+objectMoniker(objectHandle)+"]" + timeStatus );
		}

		// if this is a TSO message, check the time
		if( request.isTimestamped() )
			lrcState.checkValidTime( request.getTimestamp() );
		
		// check that the object exists and that we own it
		OCInstance instance = repository.getInstance( objectHandle );
		if( instance == null )
		{
			throw new JObjectNotKnown( "can't delete object ["+objectHandle+"]: unknown" );
		}
		else if( instance.isOwner(lrcState.getFederateHandle()) == false )
		{
			throw new JDeletePrivilegeNotHeld( "can't delete object [" + objectHandle +
			                                   "]: delete privilege not held" );
		}
		
		// remove the object
		repository.removeDiscoveredInstance( objectHandle );
		
		// notify the other federates that a new remote object has been created
		connection.broadcast( request );
		context.success();
		
		if( logger.isInfoEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.info( "SUCCESS Deleted object ["+objectMoniker(objectHandle)+"]" + timeStatus );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
