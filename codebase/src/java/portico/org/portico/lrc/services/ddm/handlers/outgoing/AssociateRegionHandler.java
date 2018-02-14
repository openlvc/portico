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
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.services.ddm.msg.AssociateRegion;
import org.portico.lrc.services.ddm.msg.UnassociateRegion;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.ddm.data.RegionStore;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516"},
                sinks="outgoing",
                messages=AssociateRegion.class)
public class AssociateRegionHandler extends LRCMessageHandler
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
		AssociateRegion request = context.getRequest( AssociateRegion.class, this );
		int regionToken = request.getRegionToken();
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		//////////////////////////////////////////////////////////////////////
		// check to see if this is actually an implicit unassociate request //
		//////////////////////////////////////////////////////////////////////
		if( attributes.size() == 0 )
		{
			// replace the request with an unassociate request and reprocess
			if( logger.isDebugEnabled() )
			{
				logger.debug( "NOTICE  Implicit unassociate region [token:" + regionToken +
							  "] for object instance [" +objectMoniker(objectHandle)+ "]" );
			}
			
			UnassociateRegion unassociate = new UnassociateRegion( regionToken, objectHandle );
			reprocessOutgoing( unassociate );
			veto();
		}
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "REQUEST associate region [token:"+regionToken+"] with attributes "+
			              acMoniker(attributes)+" of object ["+objectMoniker(objectHandle)+"]" );
		}

		////////////////////////////////////////////////////////////
		// make the association between the region and attributes //
		////////////////////////////////////////////////////////////
		// validate that we know about the region
		RegionInstance region = regions.getRegion( regionToken );
		if( region == null )
			throw new JRegionNotKnown( "token: " + regionToken );

		// make sure we created this region
		if( region.getFederateHandle() != lrcState.getFederateHandle() )
			throw new JRegionNotKnown( "Region not created by this federate: token="+regionToken );
		
		// check that that object exists
		OCInstance theObject = repository.getInstance( objectHandle );
		if( theObject == null )
			throw new JObjectNotKnown( "object handle: " + objectHandle );
		OCMetadata objectClass = fom().getObjectClass( theObject.getDiscoveredClassHandle() );

		// check that each of the attributes is valid for the object class
		for( Integer givenAttributeHandle : attributes )
		{
			if( objectClass.getAttribute(givenAttributeHandle) == null )
			{
				throw new JAttributeNotDefined( "attribute " +acMoniker(givenAttributeHandle) +
				                                " of class ["+ocMoniker(objectClass)+ "]" );
			}
		}
		
		// make the association
		RegionStore.associateForUpdates( region, theObject, objectClass, attributes );
		
		///////////////////////////
		// notify the federation //
		///////////////////////////
		connection.broadcast( request );
		
		context.success();
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS associate region [token:"+regionToken +"] with attributes "+
			             acMoniker(attributes)+" of object instance ["+objectMoniker(objectHandle)+"]" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
