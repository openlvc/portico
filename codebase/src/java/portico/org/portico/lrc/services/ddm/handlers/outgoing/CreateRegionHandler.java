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
package org.portico.lrc.services.ddm.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JInvalidExtents;
import org.portico.lrc.compat.JSpaceNotDefined;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.model.Space;
import org.portico.lrc.services.ddm.msg.CreateRegion;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516"},
                sinks="outgoing",
                messages=CreateRegion.class)
public class CreateRegionHandler extends LRCMessageHandler
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
		CreateRegion request = context.getRequest( CreateRegion.class, this );
		int spaceHandle = request.getSpaceHandle();
		int extentCount = request.getExtentCount();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Create region: space="+spaceMoniker(spaceHandle)+
			              ", extents="+extentCount );
		}
		
		// validate the number of extents
		if( extentCount < 1 || extentCount > PorticoConstants.MAX_NUMBER_OF_EXTENTS )
			throw new JInvalidExtents( "invalid extent: " + extentCount );

		// validate the FOM information
		Space space = fom().getSpace( spaceHandle );
		if( space == null )
			throw new JSpaceNotDefined( "space: " + spaceHandle );
		
		// create the region and store it locally, we'll broadcast out the handle as well
		int regionToken = lrcState.nextRegionToken();
		RegionInstance newRegion = new RegionInstance( lrcState.getFederateHandle(),
		                                               regionToken,
		                                               space,
		                                               extentCount );
		regions.addRegion( newRegion );
		
		// notify all the other federates
		request.setRegionToken( regionToken );
		connection.broadcast( request );
		
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Created Region: space="+spaceMoniker(spaceHandle)+", extents=" +
			             extentCount+" (token:"+regionToken+")" );
		}
		
		// return a cloned region so that changes can be made locally by the federate, but ensure
		// that they don't affect the stored region until the federate code chooses to inform the
		// RTIambassador that the values should be updated.
		context.success( newRegion.clone() );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
