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
package org.portico.lrc.services.pubsub.handlers.incoming;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.pubsub.msg.UnpublishObjectClass;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before any callback handler
                messages=UnpublishObjectClass.class)
public class UnpublishObjectClassIncomingHandler extends LRCMessageHandler
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
		UnpublishObjectClass request = context.getRequest( UnpublishObjectClass.class, this );
		// ignore if we sent this message
		vetoIfMessageFromUs( request );
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Federate ["+moniker(request)+"] unpublished object class ["+
			              ocMoniker(request.getClassHandle())+"]" );
		}
		
		// store the publication information in the interest manager and move on with life
		interests.unpublishObjectClass( request.getSourceFederate(),
		                                request.getClassHandle() );
		
		// release any attributes we had control of but can no longer update
		releaseAttributes( request.getSourceFederate(), request.getClassHandle() );
		
		context.success();
	}
	
	/**
	 * Release all the attributes that we did control but can no longer control due to the
	 * unpublication.
	 */
	private void releaseAttributes( int federate, int classHandle )
	{
		for( OCInstance objectInstance : repository.getAllInstances(classHandle) )
		{
			Set<Integer> released = new HashSet<Integer>();
			for( ACInstance attributeInstance : objectInstance.getAllOwnedAttributes(federate) )
			{
				released.add( attributeInstance.getHandle() );
			}
			
			// spit out an unconditional divest notification for any attributes that were released
			AttributeDivest release = new AttributeDivest( objectInstance.getHandle(),
			                                               released,
			                                               true );
			reprocessIncoming( release );
			
			// log a helpful message
			if( logger.isDebugEnabled() )
			{
				logger.debug( "Releasing attributes ["+acMoniker(released)+"] of object ["+
				              objectMoniker(objectInstance.getHandle())+
				              "] after unpublish of class ["+ocMoniker(classHandle)+
				              "] by federate ["+moniker(federate)+"]" );
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
