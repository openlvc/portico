/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.ownership.handlers.outgoing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotOwned;
import org.portico.lrc.compat.JFederateWasNotAskedToReleaseAttribute;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.AttributeRelease;
import org.portico.lrc.services.ownership.msg.DivestConfirmation;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * This handles takes care of processing release-response notifications. It will validate that the
 * federate knows about all the attributes it is attempting to release, and that it has the
 * appropriate ownership to do so. After this it marks the attributes as released and broadcasts
 * the notification to the federation so they can mark it appropriately and inform their federates
 * should any of them be the new owners of any releases.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=AttributeRelease.class)
public class AttributeReleaseResponseHandler extends LRCMessageHandler
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
		
		AttributeRelease request = context.getRequest(AttributeRelease.class, this);
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Release Attributes "+acMoniker(attributes)+" of object ["+
			              objectMoniker(objectHandle)+"]" );
		}
		
		// validate the request parameters
		validateRequest( objectHandle, attributes );
		
		// everything appears valid, mark the fact that we have released the attributes locally.
		// this is a two step process. We mark them as released now, but only mark them as
		// acquired when we get that callback from the other federate.
		Map<Integer,Integer> released = ownership.releaseAttributes( objectHandle, attributes );
		OCInstance objectInstance = repository.getInstance( objectHandle );
		for( Integer attributeHandle : released.keySet() )
		{
			ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			if( attributeInstance != null )
				attributeInstance.setOwner( released.get(attributeHandle) );
		}
		
		// mark any active divestiture as complete
		completeAnyDivestiture( objectHandle, attributes );
		
		// broadcast out the notification
		connection.broadcast( request );
		
		if( logger.isInfoEnabled() )
		{
			String recipients = mapToString( released );
			logger.info( "SUCCESS Released Attributes "+acMoniker(attributes)+" of object ["+
			             objectMoniker(objectHandle)+"], recipients="+recipients );
		}
		
		context.success( released.keySet() );
	}

	/**
	 * Takes a look at the attributes that are being released and if any of them relate to an
	 * outstanding divestiture request, a divest confirmation is queued up for callback processing
	 */
	private void completeAnyDivestiture( int objectHandle, Set<Integer> attributes )
	{
		Set<Integer> divested = ownership.completeDivest( objectHandle, attributes );
		if( divested.isEmpty() == false )
		{
			DivestConfirmation divestConfirm = new DivestConfirmation( objectHandle, divested );
			lrcState.getQueue().offer( fill(divestConfirm,federateHandle()) );
			if( logger.isDebugEnabled() )
			{
				logger.debug( "Queued divest confirmation for attributes "+acMoniker(divested)+
				              " of object ["+objectMoniker(objectHandle)+"]" );
			}
		}
	}
	
	/**
	 * Make sure that the object and all the attributes exist, are locally owned and were part
	 * of a release request.
	 */
	private void validateRequest( int objectHandle, Set<Integer> attributes )
		throws JObjectNotKnown,
		       JAttributeNotDefined,
		       JAttributeNotOwned,
		       JFederateWasNotAskedToReleaseAttribute
	{
		// make sure we have discovered the object, it's not enough for it to just
		// exist, we have to know about it
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance == null )
		{
			throw new JObjectNotKnown( "can't release attributes of object "+
			                           objectMoniker(objectHandle)+": unknown (or undiscovered)" );
		}

		// make sure all the relevant attributes exist, are owned by us, and the
		// subject of a release request
		for( Integer attributeHandle : attributes )
		{
			// does the attribute exist?
			ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			if( attributeInstance == null )
			{
				throw new JAttributeNotDefined( "can't release attribute ["+attributeHandle+
				                                "]: it doesn't exist" );
			}
			
			// do we own it?
			if( attributeInstance.getOwner() != federateHandle() )
			{
				throw new JAttributeNotOwned( "can't release attribute "+acMoniker(attributeHandle)+
				                              " of object ["+objectMoniker(objectHandle)+
				                              "]: not owned by ["+moniker()+"] (owner="+
				                              moniker(attributeInstance.getOwner())+")" );
			}
			
			// is it the subject of a release request?
			if( !ownership.isAttributeUnderAcquisitionRequest(objectHandle,attributeHandle) )
			{
				throw new JFederateWasNotAskedToReleaseAttribute( "can't release attribute "+
				           acMoniker(attributeHandle)+" of object ["+objectMoniker(objectHandle)+
				           "]: release was not requested" );
			}
		}
	}

	/**
	 * Converts a Map<AttributeHandle,FederateHandle> to a String for printing in the form:
	 * "{federateMoniker=>[att1,att2,...],federateMoniker2=>...}"
	 */
	private String mapToString( Map<Integer,Integer> released )
	{
		// convert the map from [attHandle,fedHandle] to map[fedHandle,Set<attHandles>]
		Map<Integer,Set<Integer>> byFederate = new HashMap<Integer,Set<Integer>>();
		for( Integer attributeHandle : released.keySet() )
		{
			int federateHandle = released.get( attributeHandle );
			Set<Integer> set = byFederate.get( federateHandle );
			if( set == null )
			{
				set = new HashSet<Integer>();
				byFederate.put( federateHandle, set );
			}
			
			set.add( attributeHandle );
		}
		
		StringBuilder builder = new StringBuilder( "{" );
		int count = 0;
		for( Integer federateHandle : byFederate.keySet() )
		{
			builder.append( moniker(federateHandle) );
			builder.append( "=>" );
			builder.append( acMoniker(byFederate.get(federateHandle)) );
			++count;
			if( count != byFederate.size() )
				builder.append( ", " );
		}
		
		builder.append( "}" );
		return builder.toString();
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
