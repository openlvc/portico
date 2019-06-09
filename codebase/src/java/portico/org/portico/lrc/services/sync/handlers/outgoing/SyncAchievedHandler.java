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
package org.portico.lrc.services.sync.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JSynchronizationLabelNotAnnounced;
import org.portico.lrc.services.sync.data.SyncPoint;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.sync.msg.SyncPointAchieved;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=SyncPointAchieved.class)
public class SyncAchievedHandler extends LRCMessageHandler
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

		SyncPointAchieved achieved = context.getRequest( SyncPointAchieved.class, this );
		String label = achieved.getLabel();
		
		/////////////////////////////////////////////
		// Run the message/request validity checks //
		/////////////////////////////////////////////
		lrcState.checkJoined();

		SyncPoint point = syncManager.getPoint( label );
		if( point == null )
			throw new JSynchronizationLabelNotAnnounced( "Unknown sync point: " + label );

		// check to see if we've already achieved this point
		if( point.getStatus() != SyncPoint.Status.ANNOUNCED )
		{
			throw new JRTIinternalError( "Can't achieve sync point in its current state: point=" +
			                             label + ", status=" + point.getStatus() );
		}
		
		// set the status on the point (the federate is marked as "achieved" in the set of federate
		// handles that tracks that when the broadcast message is received locally)
		point.setStatus( SyncPoint.Status.ACHIEVED );
		if( logger.isInfoEnabled() )
		{
			logger.info( "NOTICE  Federate ["+moniker()+"] achieved sync point ["+
			             point.getLabel()+"]" );
		}
		
		// SUCCESS throw the notification out onto the network (for us as well)
		connection.broadcast( achieved );
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
