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

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.model.OCMetadata;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.RequestClassUpdate;
import org.portico2.lrc.LRCMessageHandler;

public class RequestClassUpdateHandler extends LRCMessageHandler
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
		RequestClassUpdate request = context.getRequest( RequestClassUpdate.class, this );

		// Basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		int classHandle = request.getClassHandle();
		Set<Integer> attributes = request.getAttributes();
		int regionToken = request.getRegionToken();
		
		if( logger.isDebugEnabled() )
		{
			String ddmStatus = request.usesDDM() ? ", regionToken="+regionToken : "";
			logger.debug( "ATTEMPT Request class update for [%s] with attributes=%s%s",
			              ocMoniker(classHandle),
			              acMoniker(attributes),
			              ddmStatus );
		}
		
		// Make sure we can find the class
		OCMetadata metadata = getObjectClass( classHandle );
		if( metadata == null )
			throw new JObjectClassNotDefined( "Can't request class update, unknown class: handle="+classHandle );

		// make sure each of the attributes is valid
		for( Integer attributeHandle : attributes )
		{
			if( metadata.hasAttribute(attributeHandle) == false )
			{
				throw new JAttributeNotDefined( "Can't request class update. Attribute [%s] not defined in class [%s]",
				                                acMoniker(attributeHandle),
				                                ocMoniker(classHandle) );
			}
		}
		
		// Validate that the region exists
// FIXME Implement DDM
//		if( request.usesDDM() && regions.getRegion(regionToken) == null )
//			throw new JRegionNotKnown( "token: " + regionToken );

		// Send the request to the RTI
		connection.sendControlRequest( context );

		// Check for error
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		context.success();
		if( logger.isInfoEnabled() )
		{
			String ddmStatus = request.usesDDM() ? ", regionToken="+regionToken : "";
			logger.debug( "SUCCESS Request class update for [%s] with attributes=%s%s",
			              ocMoniker(classHandle),
			              acMoniker(attributes),
			              ddmStatus );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
