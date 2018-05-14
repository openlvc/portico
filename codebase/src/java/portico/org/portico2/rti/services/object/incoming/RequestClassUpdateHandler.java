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
package org.portico2.rti.services.object.incoming;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.RequestClassUpdate;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.RACInstance;
import org.portico2.rti.services.object.data.ROCInstance;

public class RequestClassUpdateHandler extends RTIMessageHandler
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
		RequestClassUpdate notice = context.getRequest( RequestClassUpdate.class, this );
		int classHandle = notice.getClassHandle();
		Set<Integer> requested = notice.getAttributes();
		int regionToken = notice.getRegionToken();
		
		if( logger.isDebugEnabled() )
		{
			String ddmStatus = notice.usesDDM() ? ", regionToken="+regionToken : "";
			logger.debug( "@REMOTE Request class update: federate=%s, class=%s, attributes=%s%s",
			              moniker(notice.getSourceFederate()),
			              ocMoniker(classHandle),
			              acMoniker(requested),
			              ddmStatus );
		}

		// get the region, this will be null if if DDM is not used
		// FIXME Implement DDM
		RegionInstance region = null; //regions.getRegion( regionToken );
		
		// get the OCMetadata instance representing the class and start to process
		// the request, recursing up the inheritance hierarchy
		OCMetadata clazz = fom().getObjectClass( classHandle );
		check( clazz, requested, region );
		context.success();
	}

	// need this in a separate method so we can call it recursively
	private void check( OCMetadata type, Set<Integer> requested, RegionInstance region )
	{
		// For the class (and each of its children) we have to
		//   - Locate all instances of the type
		//   - Issue an update request for the object with each of the handles that we own
		processClass( type.getHandle(), requested, region );
		for( OCMetadata child : type.getChildTypes() )
			check( child, requested, region );
	}

	private void processClass( int classHandle, Set<Integer> requested, RegionInstance region )
	{
		// Find all the objects of this class
		Set<ROCInstance> objects = repository.getAllInstances( classHandle );

		// For each object, find the set of attributes that are owned by independent federates
		for( ROCInstance object : objects )
		{
			// Somewhere to associate attributes with owners
			Map<Integer,Set<Integer>> ownermap = new HashMap<>();

			for( int attributeHandle : requested )
			{
				// Get the attribute information for the requested handle
				RACInstance attribute = object.getAttribute( attributeHandle );

				// If DDM is used, make sure that the attribute is associated with a region
				// and that the region overlaps with the provided region
				if( region != null )
				{
					RegionInstance attributeRegion = attribute.getRegion();
					if( attributeRegion == null ||
						region.overlapsWith(attributeRegion) == false )
					{
						continue;
					}
				}
				
				int federateHandle = attribute.getOwner();
				if( ownermap.containsKey(federateHandle) == false )
					ownermap.put( federateHandle, new HashSet<>() );
				
				ownermap.get(federateHandle).add( attribute.getHandle() );
			}
			
			// If needed, send an update request to the owners
			if( ownermap.isEmpty() )
				continue;
			
			for( Integer owner : ownermap.keySet() )
			{
				Set<Integer> owned = ownermap.get( owner );
				if( logger.isDebugEnabled() )
				{
					logger.debug( "Requesting attribute update: object=%s, attributes=%s, owner=%s",
					              objectMoniker(object.getHandle()),
					              acMoniker(owned),
					              moniker(owner) );
				}

				if( owner == PorticoConstants.RTI_HANDLE )
				{
					momManager.updateMomObject( object.getHandle(), owned );
				}
				else
				{
					RequestObjectUpdate request = new RequestObjectUpdate( object.getHandle(), owned );
					super.queueUnicast( request, owner );
				}
			}
		}

	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
