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
package org.portico.lrc.services.saverestore.handlers.outgoing;

import hla.rti.RTIinternalError;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JFederationTimeAlreadyPassed;
import org.portico.lrc.services.saverestore.msg.SaveRequest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=SaveRequest.class)
public class SaveRequestHandler extends LRCMessageHandler
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
	
	/**
	 * Registers the request to begin a federation save. Note that the save isn't actually started
	 * in this handler, the request is just checked to see if everything is OK and the message is
	 * sent out. The actual recording of the save point (in the SaveManager) is done in the incoming
	 * handler.
	 */
	public void process( MessageContext context ) throws Exception
	{
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		// get the request and details
		SaveRequest request = context.getRequest( SaveRequest.class, this );
		String label = request.getLabel();
		
		// make sure we haven't got a null handle
		if( label == null )
			throw new RTIinternalError( "Save label was null" );
		
		if( logger.isDebugEnabled() )
		{
			String time = request.isTimestamped() ? "@"+request.getTimestamp() : "@now";
			logger.debug( "REQUEST Start federation SAVE with label ["+label+"] "+time );
		}
		
		// if a timestamp is present, validate it
		if( request.isTimestamped() && request.getTimestamp() < timeStatus().getCurrentTime() )
			throw new JFederationTimeAlreadyPassed( "Time "+request.getTimestamp()+" has passed" );
		
		// pass the request on to the federation
		connection.broadcast( request );
		
		if( logger.isInfoEnabled() )
			logger.info( "Broadcast request to start federation save with label ["+label+"]" );
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
