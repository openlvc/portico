/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.lrc.services.pubsub.outgoing;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.pubsub.msg.PublishObjectClass;
import org.portico2.common.services.pubsub.msg.UnpublishObjectClass;
import org.portico2.lrc.LRCMessageHandler;

public class PublishObjectClassHandler extends LRCMessageHandler
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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		PublishObjectClass request = context.getRequest( PublishObjectClass.class, this );
		int classHandle = request.getClassHandle();
		Set<Integer> attributes = request.getAttributes();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Publish ["+ocMoniker(classHandle)+"] with attributes " +
			              acMoniker(attributes) );
		}
		
		// if this is a is a request with 0-attributes, it is an implicit unpublish //
		if( attributes.isEmpty() )
		{
			////////////////////////
			// IMPLICIT UNPUBLISH //
			////////////////////////
			// Reprocess the request as an unpublish one.
			// We need to pass this back through the sink so that it gets the full treatment.
			logger.debug( "NOTICE  Publish with 0 attributes. Send as implicit unpublish request." );
			UnpublishObjectClass unpublish = fill( new UnpublishObjectClass(classHandle) );
			context.setRequest( unpublish );
			lrc.getOutgoingSink().process( context );
			return;
		}
		
		////////////////////////////////////////////////////////////////////
		// FIX: PORT-60: Must add privilegeToDelete to any non-empty set  //
		// FIX: PORT-363: Use getPrivToDelete() because getting by name   //
		//                 doesn't work for differing HLA versions.       //
		// 1. get the handle for ptd (this will work for 1.3 OR 1516)     //
		int ptdHandle = lrcState.getFOM().getPrivilegeToDelete();
		//int ptdHandle = state.getFOM().getObjectRoot().getAttributeHandle( "privilegeToDelete" );
		// 2. check the set for it                                        //
		if( attributes.contains(ptdHandle) == false )                     //
		{                                                                 //
			// it's not there, add it                                     //
			attributes.add( ptdHandle );                                  //
			logger.trace( "NOTICE  Implicitly adding privToDelete" );     //
		}                                                                 //
		////////////////////////////////////////////////////////////////////

		connection.sendControlRequest( context );
		
		if( context.isSuccessResponse() )
		{
			// record the publication
			interests.publishObjectClass( federateHandle(), classHandle, attributes );

			logger.info( "SUCCESS Published [%s] with attributes %s",
			             ocMoniker(classHandle),
			             acMoniker(attributes) );
		}
		else
		{
			throw context.getErrorResponseException();
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
