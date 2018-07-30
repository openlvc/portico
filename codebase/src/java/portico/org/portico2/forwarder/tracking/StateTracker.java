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
package org.portico2.forwarder.tracking;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.model.ObjectModel;
import org.portico2.common.configuration.ForwarderConfiguration;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.Message;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.WelcomePack;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.object.msg.RegisterObject;

public class StateTracker
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ForwarderConfiguration configuration;
	private Logger logger;

	private Map<Integer,Message> outstandingRequests;
	private Map<Integer,Federation> federations;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public StateTracker( ForwarderConfiguration configuration, Logger logger )
	{
		this.configuration = configuration;
		this.logger = logger;
		this.outstandingRequests = new HashMap<>();
		this.federations = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Processing Methods   //////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public final void receiveControlRequest( Message message )
	{
		outstandingRequests.put( message.getHeader().getRequestId(), message );
		
//		switch( message.getHeader().getMessageType() )
//		{
//			case CreateFederation:
//			case JoinFederation:
//			case RegisterObject:
//			case DeleteObject:
//				outstandingRequests.put( message.getHeader().getRequestId(),
//				                         message.getHeader().getMessageType() );
//				break;
//			default:
//				break; // no-op
//		}
	}
	
	public final void receiveNotification( Message message )
	{
		switch( message.getHeader().getMessageType() )
		{
			case DiscoverObject:
			{
				DiscoverObject notice = message.inflateAsPorticoMessage( DiscoverObject.class );
				discoverObject( message.getHeader().getFederation(),
				                notice.getObjectHandle(),
				                notice.getClassHandle() );
				break;
			}
			case DeleteObject:
			{
				DeleteObject notice = message.inflateAsPorticoMessage( DeleteObject.class );
				deleteObject( message.getHeader().getFederation(), notice.getObjectHandle() );
				break;
			}
			default:
				break; // no-op
		}
	}

	public final void receiveControlResponse( Message message )
	{
		// are we interested in this particular response?
		Message request = outstandingRequests.remove( message.getRequestId() );
		if( request == null )
			return;

		// process the response as the appropriate type
		switch( request.getHeader().getMessageType() )
		{
			case CreateFederation:
				createFederationResponse( message.inflateAsResponse() );
				break;
			case JoinFederation:
				joinFederationResponse( message.inflateAsResponse() );
				break;
			case RegisterObject:
				registerObject( request, message.inflateAsResponse() );
				break;
			case DeleteObject:
				deleteObject( request, message.inflateAsResponse() );
				break;
			default:
				break; // we don't care about it
		}
	}
	
	public final boolean isResponseWanted( int requestId )
	{
		return outstandingRequests.containsKey( requestId );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message-Specific Methods   ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void createFederationResponse( ResponseMessage response )
	{
		// make sure the response is a success, otherwise, do nothing
		if( response.isSuccess() == false )
			return;
		
		// response was good, create the federation and store it
		String federationName = response.getSuccessResultAsString( CreateFederation.KEY_FEDERATION_NAME );
		int federationHandle  = response.getSuccessResultAsInt( CreateFederation.KEY_FEDERATION_HANDLE );
		ObjectModel fom       = response.getSuccessResultAs( CreateFederation.KEY_FOM, ObjectModel.class );
		
		Federation federation = federations.get( federationHandle );
		if( federation == null )
			federation = new Federation( federationName, fom );

		logger.info( "[Join Federation] {NEW FEDERATION} Federation [%s/%d] was created",
		             federationName,
		             federationHandle );

		this.federations.put( federationHandle, federation );
	}

	
	/**
	 * When a downstream federate joins a federation we need to watch for the response
	 * message containing the welcome pack. That pack will tell us both the federation
	 * name as well as give us the full object model. This is important as we might be
	 * joining a federation created elsewhere (and so we won't know about it yet).
	 * 
	 * @param response A response message that was associated with a join
	 */
	private void joinFederationResponse( ResponseMessage response )
	{
		if( response.isSuccess() == false )
			return;
		
		WelcomePack welcome = response.getSuccessResultAs( WelcomePack.class );
		Federation federation = federations.get( welcome.getFederationHandle() );
		if( federation == null )
		{
			logger.info( "[Join Federation] {NEW FEDERATION} Federate [%s/%d] joined federation [%s/%d]",
			             welcome.getFederateName(),
			             welcome.getFederateHandle(),
			             welcome.getFederationName(),
			             welcome.getFederationHandle() );

			federation = new Federation( welcome.getFederationName(), welcome.getFOM() );
			federations.put( welcome.getFederateHandle(), federation );
		}
		else
		{
    		logger.info( "[Join Federation] Federate [%s/%d] joined federation [%s/%d]",
    		             welcome.getFederateName(),
    		             welcome.getFederateHandle(),
    		             welcome.getFederationName(),
    		             welcome.getFederationHandle() );
    		
    		// update the stored object model, because it may have now been expanded
    		federation.updateFOM( welcome.getFOM() );
		}
	}

	/**
	 * Track information about an object within the federation. We cache the qualified class
	 * name of the object for later use in matching attribute reflections against our import
	 * and export rules.
	 * 
	 * @param request  The original request that was sent
	 * @param response The response that came back
	 */
	private final void registerObject( Message request, ResponseMessage response )
	{
		if( response.isSuccess() == false )
			return;
		
		int objectHandle = response.getSuccessResultAsInt( RegisterObject.KEY_RETURN_HANDLE );
		int classHandle = response.getSuccessResultAsInt( RegisterObject.KEY_RETURN_CLASS );
		discoverObject( request.getHeader().getFederation(), objectHandle, classHandle );
	}

	/**
	 * Track information about an object within the federation. We cache the qualified class
	 * name of the object for later use in matching attribute reflections against our import
	 * and export rules.
	 * 
	 * @param federationHandle The handle of the federation the object was registered in
	 * @param objectHandle     The handle of the object that was registered
	 * @param classHandle      The handle of the class of the object that was registered
	 */
	private final void discoverObject( int federationHandle, int objectHandle, int classHandle )
	{
		Federation federation = federations.get( federationHandle );
		if( federation == null )
			return;

		// save the object information for later use
		federation.addObject( objectHandle, classHandle );
	}

	/**
	 * Convert a response message from a "DeleteObject" request so that if successful, we remove
	 * information about it from our local store.
	 * 
	 * @param request  The original request that was sent
	 * @param response The response that came back
	 */
	private final void deleteObject( Message request, ResponseMessage response )
	{
		if( response.isSuccess() == false )
			return;
		
		int objectHandle = response.getSuccessResultAsInt();
		deleteObject( request.getHeader().getFederation(), objectHandle );
	}

	/**
	 * Remove local storage information about the given object from the given federation.
	 * 
	 * @param federationHandle The handle of the federation that contains the object
	 * @param objectHandle The handle of the object that was removed
	 */
	private final void deleteObject( int federationHandle, int objectHandle )
	{
		Federation federation = federations.get( federationHandle );
		if( federation == null )
			return;
		
		// dump the object information from our store
		federation.removeObject( objectHandle );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will check our store of objects and look up the one with the given object handle.
	 * We'll the return the fully qualified name of the class it is an instance of.<p/>
	 * 
	 * If we don't know about the federation identified in the federation handle, or we don't have
	 * a record of the object, <code>null</code> will be returned.
	 * 
	 * @param federationHandle The handle of the federation to look in
	 * @param objectHandle The handle of the object to get the class name for
	 * @return The qualified class name for the identified object, or null if we can't find the
	 *         fedetation or the object
	 */
	public final String resolveObjectHandleToClassName( int federationHandle, int objectHandle )
	{
		if( federations.containsKey(federationHandle) )
			return federations.get(federationHandle).resolveObjectHandleToClassName( objectHandle );
		else
			return null;
	}
	
	/**
	 * Find the FOM for the identified federation and then look the class handle up inside it.
	 * Return the fully qualified name of the class.<p/>
	 * 
	 * If we don't know the federation, or the federation doesn't have a value for the class
	 * handle, null is returned.
	 * 
	 * @param federationHandle The handle of the federation we're looking the class up in
	 * @param classHandle      The handle of the class we want the fully qualified name for
	 * @return                 The fully qualified name of the class, or null
	 */
	public final String resolveObjectClass( int federationHandle, int classHandle )
	{
		if( federations.containsKey(federationHandle) )
			return federations.get(federationHandle).resolveClassHandleToName( classHandle );
		else
			return null;
	}

	/**
	 * Find the FOM for the identified federation and then look the class handle up inside it.
	 * Return the fully qualified name of the class.<p/>
	 * 
	 * If we don't know the federation, or the federation doesn't have a value for the class
	 * handle, null is returned.
	 * 
	 * @param federationHandle The handle of the federation we're looking the class up in
	 * @param classHandle      The handle of the class we want the fully qualified name for
	 * @return                 The fully qualified name of the class, or null
	 */
	public final String resolveInteractionClass( int federationHandle, int classHandle )
	{
		if( federations.containsKey(federationHandle) )
			return federations.get(federationHandle).resolveInteractionClassToName( classHandle );
		else
			return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
