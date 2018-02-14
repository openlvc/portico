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
package org.portico.lrc.services.object.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DeleteObject;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=DeleteObject.class)
public class RemoveObjectHandler extends LRCMessageHandler
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
		DeleteObject notice = context.getRequest( DeleteObject.class, this );
		vetoIfMessageFromUs( notice ); // throws VetoException if its our message
		int federate = notice.getSourceFederate();
		int objectHandle = notice.getObjectHandle();
		
		if( logger.isDebugEnabled() )
		{
			String timeStatus = notice.isTimestamped() ? " @"+notice.getTimestamp() : " (RO)";
			logger.debug( "@REMOTE Delete Object: object="+objectMoniker(objectHandle)+
			              ", federate="+moniker(federate)+timeStatus );
		}

		// remove the object
		OCInstance objectInstance = repository.deleteDiscoveredOrUndiscovered( objectHandle );
		if( objectInstance != null && objectInstance.isDiscovered() )
		{
			// we had discovered the object, let request through to the callback handler
			if( logger.isInfoEnabled() )
				logger.info( "DELETED object ["+objectMoniker(objectHandle)+"]" );
		}
		else
		{
			// we hadn't discovered the object, no more processing
			if( logger.isDebugEnabled() )
			{
				logger.debug( "Removed undiscovered object from repository ["+
				              objectMoniker(objectHandle)+"]" );
			}
			
			veto();
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
