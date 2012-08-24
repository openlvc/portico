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
package org.portico.lrc.services.ownership.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.QueryOwnership;
import org.portico.lrc.services.ownership.msg.QueryOwnershipResponse;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=QueryOwnership.class)
public class QueryOwnershipHandler extends LRCMessageHandler
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
		lrcState.checkSave();
		lrcState.checkRestore();
		
		QueryOwnership request = context.getRequest( QueryOwnership.class, this );
		int objectHandle = request.getObjectHandle();
		int attributeHandle = request.getAttributeHandle();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "QUERY   Query owner of attribute "+acMoniker(attributeHandle)+
			              " of object ["+objectMoniker(objectHandle)+"]" );
		}
		
		// try and find the object
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance == null )
			throw new JObjectNotKnown( "unknown object: " + objectHandle );
		
		// try and find the attribute
		ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
		if( attributeInstance == null )
			throw new JAttributeNotDefined( "unknown attribute: "+attributeHandle );
		
		// queue callback notice
		QueryOwnershipResponse response = null;
		if( attributeInstance.isUnowned() )
		{
			response = new QueryOwnershipResponse( objectHandle,
			                                       attributeHandle,
			                                       QueryOwnershipResponse.UNOWNED );
		}
		else if( attributeInstance.isOwnedByRti() )
		{
			response = new QueryOwnershipResponse( objectHandle,
			                                       attributeHandle,
			                                       QueryOwnershipResponse.OWNED_BY_RTI );
		}
		else
		{
			response = new QueryOwnershipResponse( objectHandle,
			                                       attributeHandle,
			                                       attributeInstance.getOwner() );
		}
		
		lrcState.getQueue().offer( response );
		context.success();
		if( logger.isDebugEnabled() )
		{
			String owner = moniker( attributeInstance.getOwner() );
			if( attributeInstance.isUnowned() )
				owner = "UNOWNED";
			
			logger.debug( "QUERY-R Owner of attribute "+acMoniker(attributeHandle)+" in ["+
			              objectMoniker(objectHandle)+"] is ["+owner+"]" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
