/*
 *   Copyright 2013 The Portico Project
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

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JIllegalName;
import org.portico.lrc.compat.JObjectAlreadyRegistered;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.ReserveObjectName;
import org.portico2.common.services.object.msg.ReserveObjectNameResult;

@MessageHandler(modules="lrc-base",
                keywords={"lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=ReserveObjectName.class)
public class ReserveObjectNameHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RegisterObjectHandler objectHandler;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.objectHandler = new RegisterObjectHandler();
		this.objectHandler.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		ReserveObjectName request = context.getRequest( ReserveObjectName.class, this );
		String objectName = request.getObjectName();

		// make sure the name is valid
		if( objectName == null )
			throw new JIllegalName( "Null is not a valid object name. Cannot be reserved" );
		
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		if( logger.isDebugEnabled() )
			logger.debug( "ATTEMPT Reserve object name ["+objectName+"]" );

		// the raw code for this is present in the RegisterObjectHandler as it uses
		// it to ensure that names are unique when it is given one.
		try
		{
			this.objectHandler.reserveName( objectName );

			// no exception, life must be good
			ReserveObjectNameResult result = new ReserveObjectNameResult( objectName, true );
			fill( result );
			lrcState.getQueue().offer( result );
		}
		catch( JObjectAlreadyRegistered ar )
		{
			// name is taken :( tell the federate
			ReserveObjectNameResult result = new ReserveObjectNameResult( objectName, false );
			fill( result );
			lrcState.getQueue().offer( result );
		}

		// this call was successful - but the federate will be informed via callback
		// about whether the reservation was
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
