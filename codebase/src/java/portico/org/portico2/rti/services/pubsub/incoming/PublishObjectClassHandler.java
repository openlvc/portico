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
package org.portico2.rti.services.pubsub.incoming;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JOwnershipAcquisitionPending;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.pubsub.msg.PublishObjectClass;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.ROCInstance;

public class PublishObjectClassHandler extends RTIMessageHandler
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
		PublishObjectClass request = context.getRequest( PublishObjectClass.class, this );
		int federateHandle = request.getSourceFederate();
		int classHandle = request.getClassHandle();
		Set<Integer> attributes = request.getAttributes();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Federate [%s] publishing [%s] with attributes %s",
			              federateName(federateHandle),
			              ocMoniker(classHandle),
			              acMoniker(attributes) );
		}
		
		if( attributes.isEmpty() )
			throw new JRTIinternalError( "Publication attribute set is empty - this should have been fixed in the LRC" );
		
		// check to see if an existing publication exists, if it does and the existing and
		// new handle sets do not match, we have to see if we have any outstanding acquisition
		// requests for the attributes that are being implicitly unpublished
		checkOwnership( federateHandle, classHandle, attributes );
		
		// store the interest information
		interests.publishObjectClass( request.getSourceFederate(), classHandle, attributes );
		
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Federate [%s] published  [%s] with attributes %s",
			             federateName(federateHandle),
			             ocMoniker(classHandle),
			             acMoniker(attributes) );
		}
		
		context.success();
	}

	/**
	 * If there is an existing publication and the new set of attributes differs from the old set,
	 * this method will check to see if any of the ones that don't exist in the new set (and are
	 * thus being implicitly unpublished) have any associated acquisition requests by the federate.
	 * If this is the case, an exception will be thrown, otherwise the method will return happily.
	 */
	private void checkOwnership( int federateHandle, int classHandle, Set<Integer> newAttributes )
		throws JOwnershipAcquisitionPending
	{
		Set<Integer> existing = null;
		
		// is there an exiting publication for this class?
		try
		{
			existing = interests.getPublishedAttributes( federateHandle, classHandle );
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
		for( ROCInstance instance : repository.getAllInstances() )
		{
			// FIXME I just changed this from getDiscoveredClassHandle() to get*REGISTERED*...
			//       as part of the split of OCInstance into ROCInstance and LOCInstance.
			//       Need to revisit this particular ownership edge case to ensure it still
			//       makes sense.
			if( instance.getRegisteredClassHandle() != classHandle )
				continue;
			
			Set<Integer> set = ownership.getAttributesUnderAcquisitionRequest( instance.getHandle(),
			                                                                   federateHandle );
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
