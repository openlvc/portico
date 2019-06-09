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

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.sync.msg.FederationSynchronized;
import org.portico2.common.services.sync.msg.SyncPointAchieved;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.sync.data.SyncPoint;

public class AchieveSyncPointHandler extends RTIMessageHandler
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
		SyncPointAchieved achieved = context.getRequest( SyncPointAchieved.class, this );
		int federateHandle = achieved.getSourceFederate();
		String label = achieved.getLabel();

		// Mark the federate as having achieved this point
		// This will throw an exception if the point is unknown
		SyncPoint point = syncManager.achieveSyncPoint( label, federateHandle );

		if( logger.isInfoEnabled() )
			logger.info( "Federate [%s] achieved sync point [%s]", moniker(federateHandle), label );

		
		// If the federation is now synchronized, send that information out to everyone
		if( point.isSynchronized() )
		{
			if( logger.isInfoEnabled() )
				logger.info( "Federation has synchronized on point [%s]", label );

			FederationSynchronized message = new FederationSynchronized( point );
			super.queueManycast( message, point.getFederates() );
		}
		else
		{
			if( logger.isInfoEnabled() )
				logger.info( "Federation still not synchronized on point [%s]", label );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
