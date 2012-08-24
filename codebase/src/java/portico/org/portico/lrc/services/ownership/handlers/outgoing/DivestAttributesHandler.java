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
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeAlreadyBeingDivested;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotOwned;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.lrc.services.ownership.msg.AttributeRelease;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=AttributeDivest.class)
public class DivestAttributesHandler extends LRCMessageHandler
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
		
		// get the request information
		AttributeDivest request = context.getRequest( AttributeDivest.class, this );
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			String condition = request.isUnconditional() ? " (unconditional)" : "";
			logger.debug( "ATTEMPT Divest attributes "+acMoniker(attributes)+" of object ["+
			              objectMoniker(objectHandle)+"]"+condition );
		}
		
		// validate the request information
		validateRequest( objectHandle, attributes );
		
		if( request.isUnconditional() )
		{
			handleUnconditionalDivest( request, objectHandle, attributes );
		}
		else
		{
			handleNegotiatedDivest( request, objectHandle, attributes );
		}
		
		context.success();
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Negotiated Divest Methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method goes through each of the given attributes and starts the process of a negotiated
	 * attribute divestiture. It will automatically release any attributes for which an outstanding
	 * acquisition release request exists. For the remainder, a notice will be sent to the
	 * federation telling it about the attributes that are now available.
	 */
	private void handleNegotiatedDivest( AttributeDivest request,
	                                     int objectHandle,
	                                     Set<Integer> attributes )
		throws Exception
	{
		// FURTHER VALIDATION
		// make sure none of the attributes are under a divest request already. this is NOT
		// part of of the request validation because it is only relevant to a negotiated divest
		Set<Integer> alreadyDivesting =
			ownership.getAttributesOfferedForDivest( objectHandle, attributes, federateHandle() );
		if( alreadyDivesting.isEmpty() == false )
		{
			throw new JAttributeAlreadyBeingDivested( "attributes "+acMoniker(attributes)+
			                                          " of object ["+objectMoniker(objectHandle)+
			                                          "] already being divested" );
		}
		
		// check to see if any of these attributes are currently under an attribute release request
		// if they are, deliver a release notification for those attributes and remove them from
		// group that the divest request notification will be sent out for
		Set<Integer> released = releaseAttributesUnderAcquisitionRequest( objectHandle, attributes );
		attributes.removeAll( released ); // REMOVE any we're released
		if( attributes.isEmpty() )
			return;

		// register the divest notification with the ownership manager
		ownership.requestDivestiture( objectHandle, attributes, federateHandle() );
		
		// broadcast the notification, from there, the other federates will record the offer
		// and deliver a request ownership assumption messages if relevant. the handing over
		// process is handled by a typical attribute release
		if( logger.isInfoEnabled() )
		{
			logger.info( "Broadcast negotiated divest of attributes "+acMoniker(attributes)+
			             " in object ["+objectMoniker(objectHandle)+"] to federation" );
		}

		connection.broadcast( request );
	}
	
	/**
	 * For each of the given attributes, find out which ones are under an active attribute release
	 * request and send a release notification for them. This method returns the set of those that
	 * were released. If none were released, an empty set is returned.
	 */
	private Set<Integer> releaseAttributesUnderAcquisitionRequest( int objectHandle,
	                                                               Set<Integer> attributes )
	{
		Map<Integer,Integer> underRequest =
			ownership.getAttributesUnderAcquisitionRequest( objectHandle, attributes );

		if( underRequest.isEmpty() )
			return underRequest.keySet();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Some divesting attributes under outstanding release requests: "+
			              mapToString(underRequest) );
		}

		try
		{
			// we have to create a new set because the keyset isn't serializable
			Set<Integer> newSet = new HashSet<Integer>( underRequest.keySet() );
			AttributeRelease release = new AttributeRelease( objectHandle, newSet );
			reprocessOutgoing( fill(release) );
		}
		catch( Exception e )
		{
			logger.error( "Exception while sending release notification for attributes "+
			              acMoniker(underRequest.keySet())+" of object ["+
			              objectMoniker(objectHandle)+"]: "+e.getMessage(), e );
		}

		// return those that were released
		return underRequest.keySet();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Unconditional Divest Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Handles the process involved in an unconditional ownership divestiture. This is quite a
	 * simple process process, just setting the owner of each of the attributes locally to the
	 * {@link PorticoConstants#NULL_HANDLE} locally and broadcasting out the request to notify
	 * the other federates of the change. Any effects this has on outstanding ownership requests
	 * is handled in the incoming handler.
	 */
	private void handleUnconditionalDivest( AttributeDivest request,
	                                        int objectHandle,
	                                        Set<Integer> attributes )
		throws Exception
	{
		// release those attributes that are under an acquisition request
		Set<Integer> released = releaseAttributesUnderAcquisitionRequest( objectHandle, attributes );
		attributes.removeAll( released );
		if( attributes.isEmpty() )
			return;
		
		// mark the remaining attributes as unowned now that we're giving them up
		OCInstance object = repository.getInstance( objectHandle );
		for( Integer attributeHandle : attributes )
		{
			ACInstance attribute = object.getAttribute( attributeHandle );
			if( attribute != null )
				attribute.unown();
		}
		
		// broadcast the notification telling other federates that the attributes are out there now
		connection.broadcast( request );
		
		if( logger.isInfoEnabled() )
		{
			logger.debug( "SUCCESS Unconditionally divested attributes "+
			              acMoniker(attributes)+" of object ["+objectMoniker(objectHandle)+"]" );
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Helper Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Validates the information given in the request. This method will check to see that the
	 * object exists, that all the attributes exist and are owned by the local federate
	 */
	private void validateRequest( int objectHandle, Set<Integer> attributes )
		throws JAttributeNotDefined,
		       JAttributeNotOwned,
		       JObjectNotKnown
	{
		// make sure we have discovered the object, it's not enough for it to just
		// exist, we have to know about it
		OCInstance instance = repository.getInstance( objectHandle );
		if( instance == null )
		{
			throw new JObjectNotKnown( "can't divest attributes of object "+
			                           objectMoniker(objectHandle)+": unknown (or undiscovered)" );
		}

		// make sure each of the attributes exist and we own them
		for( Integer attributeHandle : attributes )
		{
			ACInstance attributeInstance = instance.getAttribute( attributeHandle );
			if( attributeInstance == null )
			{
				throw new JAttributeNotDefined( "can't divest attribute "+acMoniker(attributeHandle)+
				                                ": not valid for object type ["+
				                                ocMoniker(instance.getDiscoveredClassHandle())+"]" );
			}
			
			if( attributeInstance.isOwnedBy(federateHandle()) == false )
			{
				throw new JAttributeNotOwned( "can't divest attribute "+acMoniker(attributeHandle)+
				                              ": not owned (owned by ["+
				                              moniker(attributeInstance.getOwner())+"])" );
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
