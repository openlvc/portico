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
package org.portico.lrc.services.sync.handlers.outgoing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.services.sync.data.SyncPoint;
import org.portico.lrc.services.sync.msg.SyncRegistrationRequest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;
import org.portico2.common.services.sync.msg.RegisterSyncPointResult;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=RegisterSyncPoint.class)
public class RegisterSyncPointHandler extends LRCMessageHandler
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
		
		RegisterSyncPoint announcement = context.getRequest( RegisterSyncPoint.class, this );
		String label = announcement.getLabel();
		HashSet<Integer> syncset = announcement.getFederateSet();
		
		/////////////////////////////////////////////
		// Run the message/request validity checks //
		/////////////////////////////////////////////
		lrcState.checkJoined();
		logger.debug( "ATTEMPT Register sync point ["+label+"] by ["+moniker()+"]" );
		
		// check for a null label
		if( label == null || label.trim().equals("") )
			throw new JRTIinternalError( "Can't register sync point with null or empty label" );
		
		// validate that we know about each of the handles that have been requested
		if( validateGroupHandles(label,syncset) == false )
		{
			// Some handle in the set isn't valid, don't process the rest of the request.
			// The validateGroupHandles() method will have queued the appropriate sync-point
			// registration failure notice containing information about the invalid handle, so
			// there is no need to do that here. Just mark the request as handled and get out.
			veto();
		}

		// if the sync set exists, but is empty, set it to null so as to indicate that this point
		// is a federation-wide point (empty set == every federate, every federate denoted by null)
		if( syncset != null && syncset.isEmpty() )
		{
			syncset = null;
			announcement.makeFederationWide();
		}

		// try and create the sync point, if it already exists, queue up an error message
		SyncPoint syncPoint = null;
		try
		{
			syncPoint = syncManager.registerPoint( label,
			                                       announcement.getTag(),
			                                       syncset,
			                                       lrcState.getFederateHandle() );
		}
		catch( RuntimeException re )
		{
			// fail! :(
			queueFailure( label, "label already registered" );
			veto();
		}
		
		//////////////////////////////////////
		// Send out the intent notification //
		//////////////////////////////////////
		// broadcast out the registration request (to secure the label) and wait. we don't
		// actually EXPECT any responses, we just want to wait for an amount of time that
		// is appropriate as defined by the CONNECTION, not arbitrarily defined by us. Once
		// the time is up, we'll see if anyone else has requested registration of the point
		SyncRegistrationRequest registrationRequest = new SyncRegistrationRequest( label );
		registrationRequest.setSourceFederate( lrcState.getFederateHandle() );
		logger.debug( "PENDING Register synchronization point ["+label+"] by ["+moniker()+"]" );
		connection.broadcastAndSleep( registrationRequest );

		// first, check to see if the point has been announced in the mean time!
		if( syncPoint.getStatus() == SyncPoint.Status.ANNOUNCED )
		{
			queueFailure( label, "already announced" );
			context.success();
			veto();
		}

		// check to see if anyone else has tried to register the point
		int registrant = syncPoint.getRegistrant();
		if( registrant != federateHandle() )
		{
			// FAIL, someone else with a lower handle has tried to register the point, they win
			queueFailure( label, "point already registered by ["+federateName(registrant)+"]" );
			syncPoint.setStatus( SyncPoint.Status.PENDING );
			veto();
		}
		
		// SUCCESS
		// if we get here then we can register the sync point
		lrcState.getQueue().offer( fill(new RegisterSyncPointResult(label,announcement.getTag())) );
		syncPoint.setStatus( SyncPoint.Status.ANNOUNCED );
		connection.broadcast( announcement );
		context.success();
		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS Registered sync point ["+label+"] by ["+moniker()+"]" );
	}

	/**
	 * Check the set of handles that were given for the synchronization group and validate them.
	 * If the handle set is null, there was no group, so just return true. If the handle set
	 * exists, but contains the handle of a federate we don't know about, queue an appropriate
	 * sync point failure callback notice and return false.
	 */
	private boolean validateGroupHandles( String label, HashSet<Integer> groupHandles )
	{
		// a null group means this is a federation wide syncpoint, no need to validate handles
		if( groupHandles == null )
			return true;

		Set<Integer> federation = lrcState.getFederation().getFederateHandles();
		for( Integer federateHandle : groupHandles )
		{
			if( federation.contains(federateHandle) == false )
			{
				queueFailure( label, "invalid fedeate handle in sync-set, handle="+federateHandle );
				return false;
			}
		}
		
		// everything looks good!
		return true;
	}

	private void queueFailure( String label, String message )
	{
		RegisterSyncPointResult result = new RegisterSyncPointResult( label, message );
		fill( result, lrcState.getFederateHandle() );
		lrcState.getQueue().offer( result );
		logger.debug( "FAILURE Register sync point ["+label+"] by ["+moniker()+"]: " + message );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
