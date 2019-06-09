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
package org.portico2.lrc.services.object.outgoing;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.OCMetadata;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.RegisterObject;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.lrc.services.object.data.LOCInstance;

public class RegisterObjectHandler extends LRCMessageHandler
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

		RegisterObject request = context.getRequest( RegisterObject.class, this );
		int classHandle = request.getClassHandle();
		String objectName = request.getObjectName();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Register object of class [%s]%s, name=%s",
			              ocMoniker(classHandle),
			              request.usesDDM() ? " (using ddm)" : "",
			              objectName );
		}

		// make sure the object class exists AND that we are publishing it
		OCMetadata objectClass = checkPublished( classHandle );

		// Send the request to the RTI for processing
		connection.sendControlRequest( context );
		
		// Check to see if we got an error and then bug out if we did
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		// Extract the object information from the response and generate
		// our own OCInstance locally with the information
		int givenHandle = context.getSuccessResultAsInt( RegisterObject.KEY_RETURN_HANDLE );
		String givenName = context.getSuccessResultAsString( RegisterObject.KEY_RETURN_NAME );

		// Create and populate the instance
		int federateHandle = federateHandle();
		Set<Integer> published = interests.getPublishedAttributes( federateHandle, classHandle );
		LOCInstance instance = repository.createObject( objectClass,
		                                                givenHandle,
		                                                givenName,
		                                                federateHandle,
		                                                published );

		// Store the instance
		repository.addObject( instance );
		
		// The RtiAmb implementations expect to extract the created OCInstance from the
		// context, so replace the return information with that
		context.success( instance );
		
		///////////////////////////////////////////
		// run the ddm checks and apply ddm data //
		///////////////////////////////////////////
		// check the ddm properties of the request
//		if( request.usesDDM() )
//			applyDdm( instance, request.getAttributes(), request.getRegionTokens() );

		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Register object of class [%s]%s, handle=%d",
			             ocMoniker(classHandle),
			             request.usesDDM() ? " (using ddm)" : "",
			             instance.getHandle() );
		}
	}

	/**
	 * This method will try to find the {@link OCMetadata} for the object class with the given
	 * handle, validate that this federate is publishing that class and then return the metadata.
	 */
	private OCMetadata checkPublished( int classHandle )
		throws JObjectClassNotDefined,
		       JObjectClassNotPublished,
		       JRTIinternalError
	{
		// validate that the class exists in the FOM
		OCMetadata oMetadata = lrcState.getFOM().getObjectClass( classHandle );
		if( oMetadata == null )
		{
			// there is no such object class, ObjectClassNotDefined
			throw new JObjectClassNotDefined( "Class [" + classHandle + "] not in FOM" );
		}

		// validate that we are a publisher of this class
		if( interests.isObjectClassPublished( federateHandle(), classHandle ) == false )
		{
			// we are not a publisher
			throw new JObjectClassNotPublished( "Class [" + classHandle + "] not published by [" +
			                                    federateName() + "]" );
		}

		return oMetadata;
	}


	/**
	 * This method will run DDM related checks on the provided information. It will validate that
	 * each of the attributes exists, that each of the regions is known and can be associated with
	 * attributes of the type it is trying to be linked with, and will remove redundant
	 * information from the lists (e.g. if there are multiple entries for a single attribute, all
	 * but the first will be removed).
	 */
//	private void applyDdm( OCInstance instance,
//	                       List<Integer> attributes,
//	                       List<Integer> regionTokens )
//		throws JAttributeNotDefined,
//		       JAttributeNotPublished,
//		       JRegionNotKnown,
//		       JInvalidRegionContext
//	{
//		// somewhere to store each of the attributes we process so we can determine if
//		// an attribute has been provided more than once
//		HashSet<Integer> encounteredAttributes = new HashSet<Integer>();
//		
//		OCMetadata objectClass = instance.getDiscoveredType();
//		for( int i = 0; i < attributes.size(); i++ )
//		{
//			int attributeHandle = attributes.get( i );
//			int regionToken = regionTokens.get( i );
//
//			// have we encountered this attribute yet?
//			if( encounteredAttributes.contains(attributeHandle) )
//				continue;
//			else
//				encounteredAttributes.add( attributeHandle );
//
//			// validate that the attribute exists
//			ACMetadata attributeClass = objectClass.getAttribute( attributeHandle );
//			if( attributeClass == null )
//				throw new JAttributeNotDefined( "attribute="+attributeHandle+", class="+objectClass );
//
//			// validate that the attribute is published
//			if( !interests.isAttributeClassPublished(lrcState.getFederateHandle(),
//			                                         objectClass.getHandle(),
//			                                         attributeHandle) )
//			{
//				throw new JAttributeNotPublished( "attribute="+attributeHandle+", class="+objectClass );
//			}
//
//			// validate that we know about the region
//			RegionInstance region = regions.getRegion( regionToken );
//			if( region == null )
//				throw new JRegionNotKnown( "token: " + regionToken );
//
//			// validate that the region can be associated with attributes of this type
//			if( attributeClass.getSpace() == null ||
//			    attributeClass.getSpace().getHandle() != region.getSpaceHandle() )
//			{
//				// routing space for region not valid for attribute name
//				throw new JInvalidRegionContext( "routing space for region [" + region.getToken() +
//				    "] not valid for attribute [" + attributeClass + "]" );
//			}
//			
//			// apply the region to the attribute
//			instance.getAttribute(attributeHandle).setRegion( region );
//		}
//	}
	
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
