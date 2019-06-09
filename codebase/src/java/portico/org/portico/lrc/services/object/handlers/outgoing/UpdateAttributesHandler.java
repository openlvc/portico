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

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotOwned;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.UpdateAttributes;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=UpdateAttributes.class)
public class UpdateAttributesHandler extends LRCMessageHandler
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
		UpdateAttributes request = context.getRequest( UpdateAttributes.class, this );
		int objectHandle = request.getObjectId();
		HashMap<Integer,byte[]> attributes = request.getAttributes();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		if( logger.isDebugEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.debug( "ATTEMPT Update object ["+objectMoniker(objectHandle)+"], attributes "+
			              acMoniker(attributes.keySet()) + timeStatus );
		}

		// if this is a TSO message, check the time
		if( request.isTimestamped() )
			lrcState.checkValidTime( request.getTimestamp() );
		
		// fetch the OCInstance that this update concerns
		OCInstance instance = repository.getInstance( objectHandle );
		if( instance == null )
			throw new JObjectNotKnown( "unknown object: " + objectHandle );
		
		// check each of the attributes to make sure that we own them and thus can update them
		int federateHandle = lrcState.getFederateHandle();
		for( Integer attributeHandle : attributes.keySet() )
		{
			ACInstance attributeInstance = instance.getAttribute( attributeHandle );
			if( attributeInstance == null )
			{
				// the attribute doesn't exist
				throw new JAttributeNotDefined( "attribute "+attributeHandle+" undefined in class "+
				                                ocMoniker(instance.getDiscoveredClassHandle()) );
			}
			else if( attributeInstance.getOwner() != federateHandle )
			{
				// we don't own that attribute
				throw new JAttributeNotOwned( "attribute "+acMoniker(attributeHandle) +
				                              " of instance: "+objectMoniker(objectHandle)+
				                              " not owned by ["+moniker()+"] (owner:"+
				                              moniker(attributeInstance.getOwner()) + ")" );
			}
		}

		// everything is OK here, broadcast out the update
		connection.broadcast( request );
		context.success();
		
		if( logger.isInfoEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.info( "SUCCESS Updated object ["+objectMoniker(objectHandle)+"], attributes "+
			              acMoniker(attributes.keySet()) + timeStatus );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
