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
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.services.ddm.msg.AssociateRegion;
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
                messages=AssociateRegion.class)
public class RegionAssociatedHandler extends LRCMessageHandler
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
		AssociateRegion notice = context.getRequest( AssociateRegion.class, this );
		vetoIfMessageFromUs( notice ); // throws VetoException if its our message
		int federate = notice.getSourceFederate();
		int regionToken = notice.getRegionToken();
		int objectHandle = notice.getObjectHandle();
		Set<Integer> attributes = notice.getAttributes();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Associate Region: federate="+moniker(federate)+", token="+
			              regionToken+", object="+objectMoniker(objectHandle)+
			              ", attributes="+acMoniker(attributes) );
		}
		
		////////////////////////////////////////////////////////////
		// make the association between the region and attributes //
		////////////////////////////////////////////////////////////
		// validate that we know about the region
		RegionInstance region = regions.getRegion( regionToken );
		if( region == null )
			throw new JRegionNotKnown( "token: " + regionToken );

		// check that that object exists
		OCInstance theObject = repository.getInstance( objectHandle );
		if( theObject == null )
			throw new JObjectNotKnown( "object handle: " + objectHandle );
		OCMetadata objectClass = fom().getObjectClass( theObject.getDiscoveredClassHandle() );
		
		// make the association
		RegionStore.associateForUpdates( region, theObject, objectClass, attributes );
		
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Associated Region: federate="+moniker(federate)+", token="+
			             regionToken+", object="+objectMoniker(objectHandle)+", attributes="+
			             acMoniker(attributes) );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
