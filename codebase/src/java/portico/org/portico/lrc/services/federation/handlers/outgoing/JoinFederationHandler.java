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
package org.portico.lrc.services.federation.handlers.outgoing;

import java.net.URL;
import java.util.Map;

import org.portico.bindings.ConnectedRoster;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.fom.FomParser;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.RoleCall;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=JoinFederation.class)
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

	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
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
		{
			throw new JRTIinternalError( "Can't join a federation using a null or empty federate name" );
		}
		
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
		
		///////////////////////////////
		// connect to the federation //
		///////////////////////////////
		ConnectedRoster roster = connection.joinFederation( request );
		int federateHandle = roster.getLocalHandle();
		// send the local notification
		notificationManager.localFederateJoinedFederation( federateHandle,
		                                                   federateName,
		                                                   federateType,
		                                                   federationName,
		                                                   roster.getFOM() );
		
		//////////////////////////////////////////////////
		// notify the federation and wait for RoleCalls //
		//////////////////////////////////////////////////
		// broadcast out our information to the other federates and wait for them
		// to send us back information about themselves
		RoleCall rolecall = new RoleCall( lrcState.getFederateHandle(),
		                                  lrcState.getFederateName(),
		                                  lrcState.getFederateType(),
		                                  timeStatus().copy(),
		                                  repository.getControlledData(lrcState.getFederateHandle()) );
		// don't forget the sync point data!
		syncManager.fillRolecall( rolecall );
		
		// broadcast out the notification so that other federates know we're in the federation
		rolecall.setSourceFederate( federateHandle );
		rolecall.setImmediateProcessingFlag( true );
		rolecall.addAdditionalFomModules( request.getParsedJoinModules() );
		connection.broadcast( rolecall );
		
		// wait until we have gotten a RoleCall from everyone, this ensures we don't end
		// up with partial state problems as we start processing requests while other federates
		// try to tell us they're here
		logger.trace( "joined federation, waiting for RoleCalls from "+roster.getRemoteHandles() );
		for( Integer remoteHandle : roster.getRemoteHandles() )
		{
			if( remoteHandle == PorticoConstants.NULL_HANDLE )
				continue;
			
			long millisSlept = 0;
			while( lrcState.getKnownFederate(remoteHandle) == null )
			{
				// make sure we don't wait forever
				if( millisSlept > 5000 ) // have to bump this up if debugging in Eclipse
				{
					throw new JRTIinternalError( "Waited 5 seconds for RoleCall from federate ["+
					                             remoteHandle+
					                             "], none received, connection error");
				}
				
				// wait a little
				PorticoConstants.sleep( 5 );
				millisSlept += 5;
			}
		}
		
		// set the result of the message context to be the federate handle
		context.success( federateHandle );
		
		logger.info( "SUCCESS Joined federate ["+federateName+"] to federation ["+federationName+
		             "]: handle=" + context.getSuccessResult() );
		// if FOM printing is enabled, do so
		if( PorticoConstants.isPrintFom() )
			logger.info( "FOM in use for federation ["+federationName+"]:\n"+fom() );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
