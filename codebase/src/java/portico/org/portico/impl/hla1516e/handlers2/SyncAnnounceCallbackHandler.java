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
package org.portico.impl.hla1516e.handlers2;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.sync.msg.AnnounceSyncPoint;

import hla.rti1516e.exceptions.FederateInternalError;

public class SyncAnnounceCallbackHandler extends LRC1516eCallbackHandler
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
	public void callback( MessageContext context ) throws FederateInternalError
	{
		AnnounceSyncPoint announcement = context.getRequest( AnnounceSyncPoint.class, this );

		// Check to see if we're in the announcement set
		if( announcement.getFederateSet() != null &&                                        // has an announcement set
			announcement.getFederateSet().isEmpty() == false &&                             // the set isn't empty
			announcement.getFederateSet().contains(lrcState.getFederateHandle()) == false ) // we're not in it
		{
			// we're not in the set, ignore the request
			context.success();
			return;
		}

		// We're either in the set, or there is no set (federtaion wide callback)
		String label = announcement.getLabel();
		byte[] tag = announcement.getTag();
		logger.trace( "CALLBACK announceSynchronizationPoint(label="+announcement.getLabel()+")" );
		fedamb().announceSynchronizationPoint( announcement.getLabel(), announcement.getTag() );
		
		helper.reportServiceInvocation( "announceSynchronizationPoint", true, null, label, tag );
		context.success();
		
		logger.trace( "         announceSynchronizationPoint() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
