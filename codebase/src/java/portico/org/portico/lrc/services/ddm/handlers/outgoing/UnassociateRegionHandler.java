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
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.services.ddm.msg.UnassociateRegion;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.ddm.data.RegionStore;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516"},
                sinks="outgoing",
                messages=UnassociateRegion.class)
public class UnassociateRegionHandler extends LRCMessageHandler
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
		UnassociateRegion request = context.getRequest( UnassociateRegion.class, this );
		int regionToken = request.getRegionToken();
		int objectHandle = request.getObjectHandle();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT unassociate region [token:" + regionToken + "] with object [" +
			              objectMoniker(objectHandle) + "]" );
		}
		
		// validate that the object exists
		OCInstance theObject = repository.getInstance( objectHandle );
		if( theObject == null )
			throw new JObjectNotKnown( "object handle: " + objectHandle );
		
		// validate that the region exists
		RegionInstance theRegion = regions.getRegion( regionToken );
		if( theRegion == null )
			throw new JRegionNotKnown( "token: " + regionToken );
		
		// unlink the region for each attribute associated with it
		RegionStore.unassociateForUpdates( theRegion, theObject );

		// notify the federation
		connection.broadcast( request );

		context.success();
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS unassociate region [token:"+regionToken+"] with object [" +
			             objectMoniker(objectHandle) + "]" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
