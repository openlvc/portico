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

import java.util.Map;

import org.portico.bindings.ConnectedRoster;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.services.federation.msg.JoinFederation;
import org.portico.lrc.services.federation.msg.RoleCall;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

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
		
		String federate = request.getFederateName();
		String federation = request.getFederationName();
		
		/////////////////////////////
		// perform validity checks //
		/////////////////////////////
		// check for null properties
		if( federate == null || federate.trim().equals("") )
		{
			throw new JRTIinternalError( "Can't join a federation using a null or empty federate name" );
		}
		
		if( federation == null || federation.trim().equals("") )
		{
			throw new JFederationExecutionDoesNotExist(
			    "Can't join a federation using a null or empty federation name" );
		}
		
		// check to make sure the federate isn't already joined
		if( lrcState.isJoined() )
		{
			throw new JRTIinternalError( "LRC already connected to federation [" +
			                             lrcState.getFederationName() + "] as federate ["+moniker()+ 
			                             "]: Create a new RTIambassador for a second connection" );
		}

		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Join federate ["+federate+"] to federation ["+federation+"]" );
		
		///////////////////////////////
		// connect to the federation //
		///////////////////////////////
		ConnectedRoster roster = connection.joinFederation( request );
		int federateHandle = roster.getLocalHandle();
		// send the local notification
		notificationManager.localFederateJoinedFederation( federateHandle,
		                                                   federate,
		                                                   federation,
		                                                   roster.getFOM() );
		
		//////////////////////////////////////////////////
		// notify the federation and wait for RoleCalls //
		//////////////////////////////////////////////////
		// broadcast out our information to the other federates and wait for them
		// to send us back information about themselves
		RoleCall rolecall = new RoleCall( lrcState.getFederateHandle(),
		                                  lrcState.getFederateName(),
		                                  timeStatus().copy(),
		                                  repository.getControlledData(lrcState.getFederateHandle()) );
		// don't forget the sync point data!
		syncManager.fillRolecall( rolecall );
		
		// broadcast out the notification so that other federates know we're in the federation
		rolecall.setSourceFederate( federateHandle );
		rolecall.setImmediateProcessingFlag( true );
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
				if( millisSlept > 5000 )
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
		
		logger.info( "SUCCESS Joined federate ["+federate+"] to federation ["+federation+
		             "]: handle=" + context.getSuccessResult() );
		// if FOM printing is enabled, do so
		if( PorticoConstants.isPrintFom() )
			logger.info( "FOM in use for federation ["+federation+"]:\n"+fom() );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
