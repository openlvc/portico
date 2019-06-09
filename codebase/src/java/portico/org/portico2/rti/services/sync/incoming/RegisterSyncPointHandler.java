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
package org.portico2.rti.services.sync.incoming;

import java.util.HashSet;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.sync.msg.AnnounceSyncPoint;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.sync.data.SyncPoint;

public class RegisterSyncPointHandler extends RTIMessageHandler
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
		RegisterSyncPoint request = context.getRequest( RegisterSyncPoint.class, this );
		int source = request.getSourceFederate();
		String label = request.getLabel();
		HashSet<Integer> syncset = request.getFederateSet();
		
		/////////////////////////////////////////////
		// Run the message/request validity checks //
		/////////////////////////////////////////////
		logger.debug( "ATTEMPT Register sync point ["+label+"] by ["+moniker(source)+"] ("+syncset+")" );
		
		// check for a null label
		if( label == null || label.trim().equals("") )
			throw new JRTIinternalError( "Can't register sync point with null or empty label" );
		
		// if the sync set exists, but is empty, set it to null so as to indicate that this point
		// is a federation-wide point (empty set == every federate, every federate denoted by null)
		if( syncset != null && syncset.isEmpty() )
		{
			syncset = null;
			request.makeFederationWide();
		}

		// validate that we know about each of the handles that have been requested
		validateGroupHandles( syncset );
		
		// try and create the sync point
		SyncPoint point = syncManager.registerSyncPoint( label, request.getTag(), syncset, source );
		
		// Queue an announcement message for processing
		AnnounceSyncPoint announcement = new AnnounceSyncPoint( point );
		super.queueManycast( announcement, syncset );
		
		// Set the response to successful and return
		context.success();
		
		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS Registered sync point ["+label+"] by ["+moniker(source)+"]" );
	}

	/**
	 * Check the set of handles that were identified for the synchronization set and check to
	 * ensure that they exist in the federation. If they don't, throw an exception. If the given
	 * set is null or empty, ignore the check.
	 */
	private void validateGroupHandles( HashSet<Integer> groupHandles )
	{
		// a null group means this is a federation wide syncpoint, no need to validate handles
		if( groupHandles == null )
			return;

		for( Integer federateHandle : groupHandles )
		{
			if( federation.containsFederate(federateHandle) == false )
				throw new JRTIinternalError( "Invalid fedeate handle in sync-set, handle="+federateHandle );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
