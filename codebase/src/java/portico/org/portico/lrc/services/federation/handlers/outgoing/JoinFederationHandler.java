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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.bindings.ConnectedRoster;
import org.portico.bindings.jgroups.Configuration;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JResignAction;
import org.portico.lrc.services.federation.msg.JoinFederation;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.lrc.services.federation.msg.RoleCall;
import org.portico.utils.fom.FomParser;
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
			throw new JFederateAlreadyExecutionMember( "Already connected to federation [" +
			                                            lrcState.getFederationName() +
			                                            "] as federate ["+moniker()+"]" );
		}

		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Join federate ["+federate+"] to federation ["+federation+"]" );
		
		// parse any additional FOM modules and store back in the request for processing
		if( request.getFomModules().size() > 0 )
		{
			for( URL fedLocation : request.getFomModules() )
				request.addJoinModule( FomParser.parse(fedLocation) );
			
			// let people know what happened
			logger.debug( "Parsed ["+request.getJoinModules().size()+"] additional FOM modules" );
		}
		
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
		
		// Wait until we can confirm that we have processed the join notification just
		// in case we run off and return from this method before our LRC State has been
		// able to flip the "joined" flag
		long maxWait = System.currentTimeMillis() + 100;
		while( lrcState.isJoined() == false && maxWait > System.currentTimeMillis() )
			PorticoConstants.sleep( 10 );
		
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

		// wait for the role call
		try
		{
			waitForRoleCall( roster );
		}
		catch( Exception e )
		{
			// The role call went bad, so now we need to back out gracefully if we can.
			// Send a resign so that people know we just can't bear to continue.
			ResignFederation message = fill( new ResignFederation(JResignAction.NO_ACTION) );
			connection.resignFederation( message );
			
			// Throw our exception.
			throw e;
		}
		
		// set the result of the message context to be the federate handle
		context.success( federateHandle );
		
		logger.info( "SUCCESS Joined federate ["+federate+"] to federation ["+federation+
		             "]: handle=" + context.getSuccessResult() );
		// if FOM printing is enabled, do so
		if( PorticoConstants.isPrintFom() )
			logger.info( "FOM in use for federation ["+federation+"]:\n"+fom() );
	}

	/**
	 * Wait for responses to our role call request, throwing an exception if we don't get them all.
	 * We'll wait up to the configured JGroups response timeout from the RID for all the responses
	 * to come back. If they don't all come back in time, we've got to bail.
	 * 
	 * @param roster The roster of everyone we're waiting for
	 * @throws JRTIinternalError Didn't get all the responses we need - time to bail.
	 */
	private void waitForRoleCall( ConnectedRoster roster ) throws JRTIinternalError
	{
		// We've sent a RoleCall notification out - now we just need to wait until everyone
		// has responded to us.
		logger.debug( "joined federation, waiting for RoleCalls from "+roster.getRemoteHandles() );
		
		// get a list of everyone we're waiting for that we can freely edit
		Set<Integer> waitingFor = new HashSet<>( roster.getRemoteHandles() );
		waitingFor.remove( PorticoConstants.NULL_HANDLE ); // just to be sure
		int startingCount = waitingFor.size();
		
		// wait for responses
		long deadline = System.currentTimeMillis() + Configuration.getResponseTimeout();
		while( waitingFor.isEmpty() == false )
		{
			// sleep, then remove anyone who answered from the waiting list
			PorticoConstants.sleep( 5 );
			
			// remove anyone we've discovered from the list we're waiting for
			waitingFor.removeAll( lrcState.getFederation().getFederateHandles() );
			
			// has everyone responded, or have we waited long enough?
			if( waitingFor.isEmpty() || deadline < System.currentTimeMillis() )
				break;
		}
		
		// did time elapse before everyone had a chance to respond?
		if( waitingFor.isEmpty() == false )
		{
			String missing = waitingFor.size() == startingCount ? "All" : ""+waitingFor.size();
			throw new JRTIinternalError( "Waited "+Configuration.getResponseTimeout()+
			                             "ms for RoleCall responses, but still missing ["+
			                             missing+"]. Connection error, must resign." );
		}
		else
		{
			return;
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
