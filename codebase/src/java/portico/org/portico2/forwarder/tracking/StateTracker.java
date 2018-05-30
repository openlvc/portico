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
import org.portico2.common.messaging.MessageType;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.Message;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.WelcomePack;

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

	private Map<Integer,MessageType> outstandingRequests;
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
		switch( message.getHeader().getMessageType() )
		{
			case CreateFederation:
			case JoinFederation:
				
				outstandingRequests.put( message.getHeader().getRequestId(),
				                         message.getHeader().getMessageType() );
				break;
			default:
				break; // no-op
		}
	}

	public final void receiveControlResponse( Message message )
	{
		// are we interested in this particular response?
		MessageType requestType = outstandingRequests.remove( message.getRequestId() );
		if( requestType == null )
			return;

		// process the response as the appropriate type
		switch( requestType )
		{
			case CreateFederation:
				createFederationResponse( message.inflateAsResponse() );
				break;
			case JoinFederation:
				joinFederationResponse( message.inflateAsResponse() );
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
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
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
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
