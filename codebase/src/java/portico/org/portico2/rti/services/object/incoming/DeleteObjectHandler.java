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
package org.portico2.rti.services.object.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JDeletePrivilegeNotHeld;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JInvalidFederationTime;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.ROCInstance;

public class DeleteObjectHandler extends RTIMessageHandler
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
		DeleteObject request = context.getRequest( DeleteObject.class, this );
		int sourceFederate = request.getSourceFederate();
		int objectHandle = request.getObjectHandle();

		if( logger.isDebugEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.debug( "ATTEMPT Fedeate [%s] deleting object [%s] %s",
			              moniker( sourceFederate ),
			              objectMoniker(objectHandle),
			              timeStatus );
		}

		// if this is a TSO message, check the time
		if( request.isTimestamped() )
		{
			Federate federate = federation.getFederate( sourceFederate );
			TimeStatus timeStatus = timeManager.getTimeStatus( sourceFederate );
			double time = request.getTimestamp();
			// check that the time is greater than or equal to the current LBTS of this federate
			if( time < timeStatus.getLbts() )
			{
				throw new JInvalidFederationTime( "Time [" + time + "] has already passed (lbts:" +
				                                  timeStatus.getLbts() + ")" );
			}
		}
		
		// check that the object exists and that we own it
		ROCInstance instance = repository.getObject( objectHandle );
		if( instance == null )
		{
			throw new JObjectNotKnown( "can't delete object ["+objectHandle+"]: unknown" );
		}
		else if( instance.isOwner(sourceFederate) == false )
		{
			throw new JDeletePrivilegeNotHeld( "can't delete object [" + objectHandle +
			                                   "]: delete privilege not held" );
		}
		
		// remove the object
		repository.deleteObject( objectHandle );
		context.success( objectHandle ); // handle needed for forwarder
		
		logMetrics( sourceFederate, instance );
		
		// find the federates that know about this object and tell them
		DeleteObject copy = request.clone( DeleteObject.class );
		queueManycast( copy, instance.getDiscoverers() );
		
		if( logger.isInfoEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			logger.debug( "SUCCESS Fedeate [%s] deleting object [%s] %s",
			              moniker( sourceFederate ),
			              objectMoniker(objectHandle),
			              timeStatus );
		}
	}

	/**
	 * Logs metrics against the federate deleting the instance, and all federates that will receive the
	 * removed notification.
	 * 
	 * @param deletor the id of the federate deleting the object instance 
	 * @param instance the object instance being deleted
	 */
	private void logMetrics( int deletor, ROCInstance instance )
	{
		// objectDeleted from sending federate
		momManager.objectDeleted( deletor, instance );
		
		// objectRemoved on all target federates
		for( int discoverer : instance.getDiscoverers() )
		{
			if( discoverer == deletor )
				continue;
			
			momManager.objectRemoved( discoverer, instance );
		}
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
