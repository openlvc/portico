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
import org.portico.lrc.compat.JSpaceNotDefined;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.model.Space;
import org.portico.lrc.services.ddm.msg.CreateRegion;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Handles incoming notifications about the creation of a new region. Take the information about
 * the region and store it locally.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516"},
                sinks="incoming",
                priority=7, // we want to handle it before any callback handler
                messages=CreateRegion.class)
public class NewRegionHandler extends LRCMessageHandler
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
		CreateRegion notice = context.getRequest( CreateRegion.class, this );
		vetoIfMessageFromUs( notice ); // throws VetoException if its our message
		int federate = notice.getSourceFederate();
		int regionToken = notice.getRegionToken();
		int spaceHandle = notice.getSpaceHandle();
		int extentCount = notice.getExtentCount();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE New Region: federate="+moniker(federate)+", token="+regionToken+
			              ", space="+spaceMoniker(spaceHandle)+", extents="+extentCount );
		}
		
		// validate that we know about the space
		Space space = fom().getSpace( spaceHandle );
		if( space == null )
			throw new JSpaceNotDefined( "space handle: " + spaceHandle );
		
		// create and store the region locally and store it
		RegionInstance region = new RegionInstance( federate, regionToken, space, extentCount );
		regions.addRegion( region );
		
		if( logger.isInfoEnabled() )
		{
			logger.info( "STORED  New region: federate="+moniker(federate)+", token="+regionToken+
			              ", space="+spaceMoniker(spaceHandle)+", extents="+extentCount );
		}

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
