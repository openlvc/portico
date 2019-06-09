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
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=RegisterSyncPoint.class)
public class SyncAnnounceHandler extends LRCMessageHandler
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
		RegisterSyncPoint announcement = context.getRequest( RegisterSyncPoint.class, this );
		vetoIfMessageFromUs( announcement ); // announcement handled in reg.result handler if local federate
		String label = announcement.getLabel();
		Set<Integer> federates = announcement.getFederateSet();
		int announcer = announcement.getSourceFederate();
		
		// record the announcement locally, but only if we are part of the set (unless there
		// in no set, in which case it is a federation wide sync point)
		if( announcement.isFederationWide() || federates.contains(federateHandle()) )
		{
			if( logger.isDebugEnabled() )
			{
				logger.debug( "@REMOTE Synchronization point ["+label+"] ANNOUNCED by ["+
				              moniker(announcer)+"]" );
			}
			
			syncManager.pointAnnounced( label, announcement.getTag(), federates, announcer );
			context.success();
			
			if( logger.isInfoEnabled() &&
				(federates == null || federates.contains(federateHandle())) )
			{
				logger.info( "Synchronization point ["+label+"] ANNOUNCED by ["+
				             moniker(announcer)+"] to ["+moniker()+"]" );
			}
		}
		else
		{
			veto("Local federate not involved in synchronization point");
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
