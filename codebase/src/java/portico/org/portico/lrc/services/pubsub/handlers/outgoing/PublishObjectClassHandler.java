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
package org.portico.lrc.services.pubsub.handlers.outgoing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JOwnershipAcquisitionPending;
import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.pubsub.msg.PublishObjectClass;
import org.portico2.common.services.pubsub.msg.UnpublishObjectClass;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=PublishObjectClass.class)
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
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
			// queue an unpublish request
			logger.debug( "NOTICE  Publish with 0 attributes. Queue implicit unpublish request" );
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

		// check to see if an existing publication exists, if it does and the existing and
		// new handle sets do not match, we have to see if we have any outstanding acquisition
		// requests for the attributes that are being implicitly unpublished
		checkOwnership( classHandle, attributes );
		
		// store the interest information
		interests.publishObjectClass( request.getSourceFederate(), classHandle, attributes );
		// forward the information to the rest of the federation in case they want it
		connection.broadcast( request );
		context.success();

		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Published [" +ocMoniker(classHandle)+ "] with attributes "+
			             acMoniker(attributes) );
		}
	}
	
	/**
	 * If there is an existing publication and the new set of attributes differs from the old set,
	 * this method will check to see if any of the ones that don't exist in the new set (and are
	 * thus being implicitly unpublished) have any associated acquisition requests by the local
	 * federate. If this is the case, an exception will be thrown, otherwise the method will return
	 * happily.
	 */
	private void checkOwnership( int classHandle, Set<Integer> newAttributes )
		throws JOwnershipAcquisitionPending
	{
		Set<Integer> existing = null;
		
		// is there an exiting publication for this class?
		try
		{
			existing = interests.getPublishedAttributes( federateHandle(), classHandle );
		}
		catch( JObjectClassNotPublished notPublished )
		{
			// everything is OK then, we can't have outstanding requests in this case
			return;
		}
		catch( JObjectClassNotDefined notDefined )
		{
			// everythign is also OK, we can't have outstanding requests in this case
			return;
		}
		
		// there is an existing publication, find out which we are not implicitly unpublishing
		existing = new HashSet<Integer>( existing ); // the current version is unmodifiable
		for( Integer attributeHandle : newAttributes )
			existing.remove( attributeHandle );
		
		// are there any being implicitly unpublished?
		if( existing.isEmpty() )
			return;
		
		// check to see if there is any outstanding acquisition request for any of those atts
		for( OCInstance instance : repository.getAllInstances() )
		{
			if( instance.getDiscoveredClassHandle() != classHandle )
				continue;
			
			Set<Integer> set = ownership.getAttributesUnderAcquisitionRequest( instance.getHandle(),
			                                                                   federateHandle() );
			if( set.isEmpty() == false )
			{
				throw new JOwnershipAcquisitionPending( "can't implicitly unpublish attributes "+
				    acMoniker(existing)+" through re-publication of class ["+ocMoniker(classHandle)+
				    "]: there are outstanding acquistion requests for some of these attributes" );
			}
		}
		
		// if we get here then we are good and there are no outstanding acquisition requests
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
