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
package org.portico2.lrc.services.ownership.outgoing;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotPublished;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateOwnsAttributes;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.ownership.msg.AttributeAcquire;
import org.portico2.lrc.LRCMessageHandler;
import org.portico2.lrc.services.object.data.LACInstance;
import org.portico2.lrc.services.object.data.LOCInstance;

/**
 * This handler processes requests to acquire attributes, be it through direct solicitation (in
 * service to the attributeOwnershipAcquisition() call) or only if they're available (in service
 * to the attributeOwnershipAcquisitionIfAvailable() call).
 */
public class AcquireOwnershipHandler extends LRCMessageHandler
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
		
		// get the request information
		AttributeAcquire request = context.getRequest( AttributeAcquire.class, this );
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			String available = request.isIfAvailable() ? "(if available)" : "";
			logger.debug( "ATTEMPT Acquire ownership of attributes %s in object [%s] %s",
			              acMoniker(attributes), objectMoniker(objectHandle), available );
		}
		
		// validate the information given in the request
		validateRequest( objectHandle, attributes );
		
		// send the request off to the RTI for processing
		connection.sendControlRequest( context );
		
		// was there a problem?
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		if( logger.isDebugEnabled() )
		{
			String available = request.isIfAvailable() ? "(if available)" : "";
			logger.debug( "SUCCESS RTI accepted acquire ownership request and forwarded (attributes=%s,object=[%s] %s)",
			              acMoniker(attributes), objectMoniker(objectHandle), available );
		}	

		context.success();
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Helper Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Validates the information given in the request. This method will check to see that all
	 * the attributes exist, that they are all published by the local federate and  that none
	 * are currently owned by the local federate 
	 */
	private void validateRequest( int objectHandle, Set<Integer> attributes )
		throws JAttributeNotDefined,
		       JFederateOwnsAttributes,
		       JAttributeNotPublished,
		       JObjectClassNotPublished,
		       JObjectClassNotDefined,
		       JObjectNotKnown
	{
		// make sure we have discovered the object, it's not enough for it to just
		// exist, we have to know about it
		LOCInstance instance = repository.getObject( objectHandle );
		if( instance == null )
		{
			throw new JObjectNotKnown( "can't aquire attributes of object "+
			                           objectMoniker(objectHandle)+": unknown (or undiscovered)" );
		}

		int classHandle = instance.getDiscoveredClassHandle();
		// are we publishing this particular attribute?
		if( !interests.isObjectClassPublished(federateHandle(), classHandle) )
		{
			throw new JObjectClassNotPublished( "can't aquire attributes of ["+
			                                    ocMoniker(classHandle)+"]: not published" );
		}
		
		/////////////////////////////////////////////////////////
		// check: valid attributes and current not owned by us //
		/////////////////////////////////////////////////////////
		// make sure all the attributes are valid and that we don't already own them
		for( Integer expected : attributes )
		{
			LACInstance attributeInstance = instance.getAttribute( expected );
			if( attributeInstance == null )
			{
				throw new JAttributeNotDefined( "can't aquire attribute "+acMoniker(expected)+
				                                ": not valid for object type "+ocMoniker(classHandle) );
			}
			
			// do we already own them?
			if( attributeInstance.getOwner() == federateHandle() )
			{
				throw new JFederateOwnsAttributes( "can't aquire attribute "+acMoniker(expected)+
				                                   ": federate already owns them" );
			}
			
			if( !interests.isAttributeClassPublished(federateHandle(),classHandle,expected) )
			{
				throw new JAttributeNotPublished( "can't aquire attribute "+acMoniker(expected)+
				                                  ": not published" );
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
