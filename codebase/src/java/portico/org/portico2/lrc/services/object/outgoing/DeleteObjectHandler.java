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

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JDeletePrivilegeNotHeld;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.lrc.services.object.data.LOCInstance;

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

		DeleteObject request = context.getRequest( DeleteObject.class, this );
		int objectHandle = request.getObjectHandle();

		if( logger.isDebugEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.debug( "ATTEMPT Delete object [%s] %s", objectMoniker(objectHandle), timeStatus );
		}

		// if this is a TSO message, check the time
		if( request.isTimestamped() )
			lrcState.checkValidTime( request.getTimestamp() );
		
		// check that the object exists and that we own it
		LOCInstance instance = repository.getObject( objectHandle );
		if( instance == null )
		{
			throw new JObjectNotKnown( "Can't delete object ["+objectHandle+"]: unknown" );
		}
		else if( instance.isOwner(lrcState.getFederateHandle()) == false )
		{
			throw new JDeletePrivilegeNotHeld( "Can't delete object [" + objectHandle +
			                                   "]: delete privilege not held" );
		}
		
		// request this from the RTI
		connection.sendControlRequest( context );
		
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();

		// the delete was successful, so remove the object from the repository
		repository.deleteObject( objectHandle );
		
		if( logger.isInfoEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.info( "SUCCESS Deleted object [%s] %s", objectMoniker(objectHandle), timeStatus );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
