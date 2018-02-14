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
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.services.ddm.msg.ModifyRegion;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.ddm.data.RegionStore;

/**
 * Handles incoming notifications about the alteration of a regions extent values. Store the new
 * values in the {@link RegionStore}.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516"},
                sinks="incoming",
                priority=7, // we want to handle it before any callback handler
                messages=ModifyRegion.class)
public class ModifiedRegionHandler extends LRCMessageHandler
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
		ModifyRegion notice = context.getRequest( ModifyRegion.class, this );
		vetoIfMessageFromUs( notice ); // throws VetoException if its our message
		int federate = notice.getSourceFederate();
		RegionInstance region = notice.getRegion();
		int regionToken = region.getToken();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Modify Region: federate="+moniker(federate)+
			              ", token="+regionToken );
		}

		// update the region store with the given region value
		// note that we CAN'T just replace the region we have on store as there are subscription
		// data structures that are holding references to the objects. we have to copy the new
		// values into the old region if we know about the region in the first place
		RegionInstance regionOnFile = regions.getRegion( regionToken );
		if( regionOnFile == null )
			regions.addRegion( region.clone() );
		else
			regionOnFile.copy( region );

		if( logger.isInfoEnabled() )
		{
			logger.info( "UPDATED Modified Region: federate="+moniker(federate)+
			             ", token="+regionToken );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
