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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.lrc.services.object.data.LACInstance;
import org.portico2.lrc.services.object.data.LOCInstance;

public class RequestObjectUpdateHandler extends LRCMessageHandler
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
		RequestObjectUpdate request = context.getRequest( RequestObjectUpdate.class, this );

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		int objectHandle = request.getObjectId();
		Set<Integer> attributeHandles = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Request update of object [%s] for attributes=%s",
			              objectMoniker(objectHandle), acMoniker(attributeHandles) );
		}
		
		// make sure we have discovered the object
		LOCInstance instance = repository.getObject( objectHandle );
		if( instance == null )
			throw new JObjectNotKnown( "Can't request update, unknown object: handle="+objectHandle );

		// we only want to request updates for those attributes that we don't own.
		HashSet<Integer> nonOwnedAttributes = new HashSet<Integer>();
		for( Integer attributeHandle : attributeHandles )
		{
			// get the attribute
			LACInstance attribute = instance.getAttribute( attributeHandle );
			if( attribute == null )
			{
				throw new JAttributeNotDefined( "attribute: " +attributeHandle+
				                                " in instance: " + objectHandle );
			}
			
			// if we are the owner, remove the handle from the request
			if( attribute.getOwner() != lrcState.getFederateHandle() )
				nonOwnedAttributes.add( attributeHandle );
		}
		
		// reassign the set of nonOwnedAttributes as the set we are interested in
		request.setAttributes( nonOwnedAttributes );
		
		//
		// send to the RTI for processing
		//
		connection.sendControlRequest( context );
		
		// check for error
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		context.success();
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Requested update of object ["+objectMoniker(objectHandle)+
			              "] for attributes="+acMoniker(attributeHandles) );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
