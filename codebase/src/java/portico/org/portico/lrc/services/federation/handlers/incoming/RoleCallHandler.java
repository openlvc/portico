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
package org.portico.lrc.services.federation.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.federation.msg.RoleCall;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;
import org.portico2.common.services.sync.msg.SyncPointAchieved;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                messages=RoleCall.class)
public class RoleCallHandler extends LRCMessageHandler
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
		vetoIfNotJoined();

		RoleCall notice = context.getRequest( RoleCall.class, this );
		int handle = notice.getSourceFederate();
		String federateName = notice.getFederateName();
		String federateType = notice.getFederateType();

		// if we already know about this federate, skip this whole process
		if( lrcState.getKnownFederate(handle) != null )
			veto();
		
		// we do not know about the federate yet, store its particulars and
		// send it back information about us
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE RoleCall received [handle:"+handle+",name:"+federateName+
			              "] by local federate ["+lrcState.getFederateName()+"]" );
		}

		///////////////////////////////////////
		// inform the notification listeners //
		///////////////////////////////////////
		// this must be done before broadcasting the role call mainly due to limitations of the
		// JVM binding. Because processing in the JVM binding happens directly (as nested calls
		// to this), if we don't add the federate first, then broadcasting out role call results
		// in other federates sending us their RoleCall information, and because we don't know
		// about them (having not notified the LRC through the NotificationManager) we think we
		// don't know about them, so we send another RoleCall. The same process happens in the
		// other federates and we eventually end with a StackOverflowException.
		//
		// FOM module merging happens in here
		notificationManager.remoteFederateJoinedFederation( notice );

		//////////////////////////////////////////////////////////////////
		// Process new Object Instance information from remote federate //
		//////////////////////////////////////////////////////////////////
		// For each of the objects the other federate controls, add them to the repository.
		// If we can discover them based on current subscription information, do so
		for( OCInstance instance : notice.getControlledObjects() )
		{
			// can the federate discover instances of this type?
			OCMetadata discoverableType =
				interests.getDiscoveryType( federateHandle(), instance.getDiscoveredClassHandle() );
			if( discoverableType == null )
			{
				// can't discover it yet, store as "undiscovered type" for later use
				repository.addUndiscoveredInstance( instance );
				if( logger.isDebugEnabled() )
				{
					logger.debug( "(RoleCall) Adding undiscoverd instance ["+objectMoniker(instance)+
					              "] following RoleCall notification" );
				}
			}
			else
			{
				// can discover it! queue a callback
				instance.setDiscoveredType( discoverableType );
				repository.discoverInstance( instance, discoverableType );
				DiscoverObject discover = new DiscoverObject( instance );
				discover.setClassHandle( discoverableType.getHandle() );
				discover.setSourceFederate( instance.getOwner() );
				lrcState.getQueue().offer( discover );
				if( logger.isDebugEnabled() )
				{
					logger.debug( "(RoleCall) Queued Discover callback for instance ["+
					              objectMoniker(instance)+"] following RoleCall notification" );
				}
			}
		}
		
		////////////////////////////////////////////////////////////////////
		// Process synchronization point information from remote federate //
		////////////////////////////////////////////////////////////////////
		// Register all unknown sync points
		Map<String,Boolean> points = notice.getSyncPointStatus();
		for( String label : points.keySet() )
		{
			// if we don't know about the point, queue an announcement
			if( syncManager.containsPoint(label) == false )
			{
				RegisterSyncPoint announce =
					new RegisterSyncPoint( label, notice.getSyncPointTags().get(label) );
				lrcState.getQueue().offer( announce );
			}
			
			// has the remote federate achieved the point?
			if( points.get(label) == true )
			{
				SyncPointAchieved achieved = new SyncPointAchieved( label );
				achieved.setSourceFederate( handle );
				lrcState.getQueue().offer( achieved );
			}
		}
		
		///////////////////////////////////////////
		// send back information about ourselves //
		///////////////////////////////////////////
		RoleCall role = new RoleCall( lrcState.getFederateHandle(),
		                              lrcState.getFederateName(),
		                              lrcState.getFederateType(),
		                              timeStatus().copy(),
		                              repository.getControlledData(lrcState.getFederateHandle()) );
		syncManager.fillRolecall( role );

		fill( role, handle );
		connection.broadcast( role );

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
