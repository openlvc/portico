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
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JIllegalName;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.ReserveObjectName;
import org.portico2.lrc.LRCMessageHandler;

public class ReserveObjectNameHandler extends LRCMessageHandler
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
		ReserveObjectName request = context.getRequest( ReserveObjectName.class, this );
		String objectName = request.getObjectName();

		// Make sure the name is valid
		if( objectName == null )
			throw new JIllegalName( "Null is not a valid object name. Cannot be reserved" );
		
		// Basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		if( logger.isDebugEnabled() )
			logger.debug( "ATTEMPT Reserve object name [%s]", objectName );

		// Send to the RTI for processing
		connection.sendControlRequest( context );
		
		// Check for error
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();

		// This call was successful - but the federate will be informed via callback
		// about whether the reservation was
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
