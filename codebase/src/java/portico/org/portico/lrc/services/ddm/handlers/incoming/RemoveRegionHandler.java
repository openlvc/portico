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
package org.portico.lrc.services.ddm.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.services.ddm.msg.DeleteRegion;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.ddm.data.RegionStore;

/**
 * Handles incoming notifications about the removal of a region. Notify the {@link RegionStore} and
 * take away any reference to the region.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516"},
                sinks="incoming",
                priority=7, // we want to handle it before any callback handler
                messages=DeleteRegion.class)
public class RemoveRegionHandler extends LRCMessageHandler
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
		DeleteRegion notice = context.getRequest( DeleteRegion.class, this );
		vetoIfMessageFromUs( notice ); // throws VetoException if its our message
		int federate = notice.getSourceFederate();
		int regionToken = notice.getRegionToken();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Delete Region: federate="+moniker(federate)+
			              ", token="+regionToken );
		}

		// try to locate the region first
		if( regions.containsRegion(regionToken) == false )
			throw new JRegionNotKnown( "token: " + regionToken );
		
		// remove the region
		regions.removeRegion( regionToken );

		if( logger.isInfoEnabled() )
		{
			logger.info( "REMOVED Deleted Region: federate="+moniker(federate)+
			             ", token="+regionToken );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
