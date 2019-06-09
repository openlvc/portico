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
package org.portico2.lrc.services.object.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.lrc.services.object.data.LOCInstance;

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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		DeleteObject notice = context.getRequest( DeleteObject.class, this );
		vetoIfMessageFromUs( notice ); // throws VetoException if its our message
		int federate = notice.getSourceFederate();
		int objectHandle = notice.getObjectHandle();
		
		if( logger.isDebugEnabled() )
		{
			String timeStatus = notice.isTimestamped() ? " @"+notice.getTimestamp() : " (RO)";
			logger.debug( "@REMOTE Delete Object: object="+objectMoniker(objectHandle)+
			              ", federate="+federate+timeStatus );
		}

		// remove the object
		LOCInstance objectInstance = repository.deleteObject( objectHandle );
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
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
