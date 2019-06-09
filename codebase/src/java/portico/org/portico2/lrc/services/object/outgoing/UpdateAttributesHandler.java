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
package org.portico2.lrc.services.object.outgoing;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotOwned;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.lrc.services.object.data.LACInstance;
import org.portico2.lrc.services.object.data.LOCInstance;

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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		UpdateAttributes request = context.getRequest( UpdateAttributes.class );
		int objectHandle = request.getObjectId();
		HashMap<Integer,byte[]> attributes = request.getAttributes();

		if( logger.isDebugEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.debug( "ATTEMPT Update object [%s] with attributes %s%s",
			              objectMoniker(objectHandle),
			              acMoniker(attributes.keySet()),
			              timeStatus );
		}

		// if this is a TSO message, check the time
		if( request.isTimestamped() )
			lrcState.checkValidTime( request.getTimestamp() );
		
		// fetch the OCInstance that this update concerns
		LOCInstance instance = repository.getObject( objectHandle );
		if( instance == null )
			throw new JObjectNotKnown( "unknown object: " + objectHandle );
		
		// check each of the attributes to make sure that we own them and thus can update them
		int federateHandle = lrcState.getFederateHandle();
		for( Integer attributeHandle : attributes.keySet() )
		{
			LACInstance attributeInstance = instance.getAttribute( attributeHandle );
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
				                              attributeInstance.getOwner() + ")" );
			}
		}

		// everything is OK here, broadcast out the update
		connection.sendDataMessage( request );
		context.success();
		
		if( logger.isInfoEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.info( "SUCCESS Updated object [%s] with attributes %s%s",
			             objectMoniker(objectHandle),
			             acMoniker(attributes.keySet()),
			             timeStatus );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
