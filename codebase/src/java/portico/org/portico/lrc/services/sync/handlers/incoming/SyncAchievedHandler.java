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
package org.portico.lrc.services.sync.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.services.sync.data.SyncPoint;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.sync.msg.SyncPointAchieved;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
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
		SyncPointAchieved notice = context.getRequest( SyncPointAchieved.class, this );
		String label = notice.getLabel();
		int federate = notice.getSourceFederate();
		SyncPoint point = syncManager.getPoint( label );
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Federate ["+moniker(federate)+
			              "] ACHIEVED synchronization point ["+label+"]" );
		}

		// if we don't know about the point, ignore it - it might be restricted and not for us
		if( point == null )
			veto("unknown synchronization point");

		// if we know about it, mark the federate has having achieved it
		point.federateAchieved( notice.getSourceFederate() );
		if( point.isSynchronized(lrcState.getFederation().getFederateHandles()) )
		{
			// mark the point as synchronized and let it through to the callback handler
			point.setStatus( SyncPoint.Status.SYNCHRONIZED );
			logger.info( "Federation SYNCHRONIZED on point ["+label+"], queued callback" );
			return; // don't veto, it's synchronized, let it through
		}
		else
		{
			// no need for further processing, it's not time yet
			veto("still waiting for federation to synchronize on point ["+label+"]");
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
