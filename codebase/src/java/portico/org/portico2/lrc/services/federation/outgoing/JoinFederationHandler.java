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
package org.portico2.lrc.services.federation.outgoing;

import java.net.URL;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.fom.FomParser;
import org.portico2.common.PorticoConstants;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.WelcomePack;
import org.portico2.common.services.sync.msg.AnnounceSyncPoint;
import org.portico2.lrc.LRCMessageHandler;

public class JoinFederationHandler extends LRCMessageHandler
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
		JoinFederation request = context.getRequest( JoinFederation.class, this );
		
		String federateName = request.getFederateName();
		String federateType = request.getFederateType();
		String federationName = request.getFederationName();
		
		/////////////////////////////
		// perform validity checks //
		/////////////////////////////
		// check for null properties
		if( federateName == null || federateName.trim().equals("") )
			throw new JRTIinternalError( "Can't join a federation using a null or empty federate name" );
		
		if( federationName == null || federationName.trim().equals("") )
		{
			throw new JFederationExecutionDoesNotExist(
			    "Can't join a federation using a null or empty federation name" );
		}
		
		// check to make sure the federate isn't already joined
		if( lrcState.isJoined() )
		{
			throw new JFederateAlreadyExecutionMember( "Already connected to federation [" +
			                                            lrcState.getFederationName() +
			                                            "] as federate ["+moniker()+"]" );
		}

		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Join federate ["+federateName+"] to federation ["+federationName+"]" );
		
		// parse any additional FOM modules and store back in the request for processing
		if( request.getFomModuleLocations().size() > 0 )
		{
			for( URL fedLocation : request.getFomModuleLocations() )
				request.addJoinModule( fedLocation, FomParser.parse(fedLocation) );
			
			// let people know what happened
			logger.debug( "Parsed ["+request.getParsedJoinModules().size()+"] additional FOM modules" );
		}
		
		/////////////////////////////////
		// Send the request to the RTI //
		/////////////////////////////////
		connection.sendControlRequest( context );
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		WelcomePack welcome = (WelcomePack)context.getSuccessResult();
		this.lrcState.localFederateJoinedFederation( welcome.getFederateHandle(),
		                                             welcome.getFederationHandle(),
		                                             federateName,
		                                             federateType,
		                                             federationName,
		                                             welcome.getFOM() );
		
		// populate other state from the welcome pack
		populateFederationState( welcome );
		
		// replace the results with only the federate handle
		context.success( welcome.getFederateHandle() );
		
		logger.info( "SUCCESS Federate [%s] joined to federation [%s] with handle [%s]",
		             federateName, federationName, welcome.getFederateHandle() );

		// if FOM printing is enabled, do so
		if( PorticoConstants.isPrintFom() )
			logger.info( "FOM in use for federation ["+federationName+"]:\n"+fom() );
	}

	/**
	 * The purpose of this call is to generate callbacks or populate local LRC state based
	 * on information received from the RTI as part of the join process. When we are a late
	 * joiner to a federation, there is a bunch of state we may need to know about that is
	 * already in place (sync points that need to be locally announced for example). This
	 * method handles that.
	 * 
	 * @param welcome The welcome packet information recevied from the RTI
	 */
	private void populateFederationState( WelcomePack welcome )
	{
		// queue sync point announcements
		for( String label : welcome.getSyncPoints() )
		{
			logger.debug( "{LATE JOIN} Queuing SyncPointAnnounce: %s", label );
			AnnounceSyncPoint message = new AnnounceSyncPoint( label );
			lrcQueue.offer( message );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
