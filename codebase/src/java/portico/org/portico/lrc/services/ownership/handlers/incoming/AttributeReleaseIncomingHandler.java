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
package org.portico.lrc.services.ownership.handlers.incoming;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.AttributeRelease;
import org.portico.lrc.services.ownership.msg.OwnershipAcquired;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * This handler processes incoming {@link AttributeRelease} messages. These can be stimulated
 * either by a direct release (through the attribure release response service) or as part of an
 * unconditional attribute divestiture.
 * <p/>
 * When a release is received, the handler will check to see if the local federate has an
 * outstanding request for any of the local attributes. If it does, it will broadcast out an
 * ownership assumption notice for those attributes. After this, the handler will see if any of
 * the attributes are under *NO* request by any federate. If this is the case, a request to acquire
 * those attributes should be sent to the local federate ambassador if the local federate is in a
 * position (in terms of current publication and subscription interests) to be a potential new
 * owner for the attributs.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7,
                messages=AttributeRelease.class)
public class AttributeReleaseIncomingHandler extends LRCMessageHandler
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
		AttributeRelease request = context.getRequest(AttributeRelease.class, this);
		int federate = request.getSourceFederate();
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE ["+moniker(federate)+"] has released attributes "+
			              acMoniker(attributes)+" of object ["+objectMoniker(objectHandle)+"]" );
		}
		
		// record the release locally
		ownership.releaseAttributes( objectHandle, attributes );
		
		// find out if any are for us and broadcast out our acceptance of them
		Set<Integer> ours = ownership.getAttributesReleasedToFederate( objectHandle,
		                                                               federateHandle() );
		
		////////////////////////////////////////
		// change the owner on the attributes //
		////////////////////////////////////////
		// note that we only change the ownership of the attributes that WE now own because
		// we want to know about them right away. We change the ownership for other attributes
		// that get picked up by other federates when they broadcast out the message that says
		// they have taken ownership of those attributes
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance == null )
		{
			logger.debug( "Can't change ownership on attributes of object ["+
			              objectMoniker(objectHandle)+"]: object not known or discovered" );
			return;
		}

		for( Integer attributeHandle : ours )
		{
			ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			attributeInstance.setOwner( federateHandle() );
		}

		///////////////////////////////////////////////////////////////
		// generate and send out a callback if we got any attributes // 
		///////////////////////////////////////////////////////////////
		if( ours.isEmpty() == false )
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "Attributes "+acMoniker(ours)+" of object ["+
				              objectMoniker(objectHandle)+"] released to local federate ["+
				              moniker()+"], broadcasting OwnershipAcquired notification" );
			}
			
			OwnershipAcquired acquired = new OwnershipAcquired( objectHandle, ours, false );
			connection.broadcast( fill(acquired) );
		}

		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
