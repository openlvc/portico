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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JResignAction;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.federation.msg.ResignFederation;
import org.portico.lrc.services.object.msg.DeleteObject;
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.lrc.services.saverestore.msg.RestoreComplete;
import org.portico.lrc.services.saverestore.msg.SaveComplete;
import org.portico.lrc.services.sync.data.SyncPoint;
import org.portico.lrc.services.sync.msg.SyncPointAchieved;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Handles incoming notifications of the resignation of a remote federate. This will remove the
 * information stored about that remote federate from the LRC so that it is no longer used for
 * processing by the local federate.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                messages=ResignFederation.class)
public class ResignNotificationHandler extends LRCMessageHandler
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
		ResignFederation request = context.getRequest( ResignFederation.class, this );
		// don't process our own resign (this could happen as the immediate processing flag for
		// the resign message is set to true)
		vetoIfMessageFromUs( request );  

		// remove the information about the federate for LRC use
		String federateName = moniker( request );
		int federateHandle = request.getSourceFederate();
		logger.debug( "@REMOTE Federate ["+federateName+"] resigned from federation ["+
		              request.getFederationName()+"]: action=" + request.getResignAction() );

		// inform the notification manager
		// we must do this here so that the checks occurring below (say, to see if a time advance
		// can be granted) do *not* include the resigned federate in their calculations
		notificationManager.remoteFederateResignedFromFederation( federateHandle, federateName );

		///////////////////////////////
		// process the resign action //
		///////////////////////////////
		processResignAction( request.getSourceFederate(), federateName, request.getResignAction() );
		
		////////////////////////////////////////////
		// run any needed post resignation checks //
		////////////////////////////////////////////
		for( SyncPoint point : syncManager.getAllPoints() )
		{
			if( point.getStatus() == SyncPoint.Status.ACHIEVED )
			{
				// do a fake sync achieved if we have achieved this point. if we haven't
				// achieved it yet, it won't matter as we'll check again when we do
				SyncPointAchieved notice = new SyncPointAchieved( point.getLabel() );
				notice.setSourceFederate( request.getSourceFederate() );
				reprocessIncoming( notice );
			}
		}

		//////////////////////////////////////////////////////////////////
		// notify the time manager and re-run time advance grant checks //
		//////////////////////////////////////////////////////////////////
		timeManager.resignedFederation( request.getSourceFederate() );
		// recalculate the status for all federates and see if we have advanced
		queueDummyAdvance();
		
		///////////////////////////////////////////////////////////////////////////
		// notify the save and restore managers and re-run "s/r complete" checks //
		///////////////////////////////////////////////////////////////////////////
		saveManager.resignedFederation( request.getSourceFederate() );
		if( saveManager.isInProgress() )
			lrcState.getQueue().offer( new SaveComplete(true) );
		
		restoreManager.resignedFederation( request.getSourceFederate() );
		if( restoreManager.isInProgress() )
			lrcState.getQueue().offer( new RestoreComplete(true) );
		
		context.success();
	}
	
	private void processResignAction( int federate, String federateName, JResignAction action )
		throws Exception
	{
		if( action == JResignAction.NO_ACTION )
			return;
		
		for( OCInstance instance : repository.getAllInstances() )
		{
			if( instance.isOwner(federate) )
			{
				// we are the owner, delete it if needed
				if( action == JResignAction.DELETE_OBJECTS ||
					action == JResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES )
				{
					// DELETE OBJECTS
					// delete the object, no need to release any attributes if we're doing this :P
					if( logger.isDebugEnabled() )
					{
						logger.debug( "Issuing delete for object ["+objectMoniker(instance)+
						              "] after resign of federate ["+federate+"]" );
					}

					DeleteObject delete = new DeleteObject( instance.getHandle(), new byte[0] );
					delete.setSourceFederate( federate );
					connection.broadcast( delete );
					continue; // no need for more processing of this object
				}
				else
				{
					// RELEASE ATTRIBUTES
					releaseAttributes( federate, instance );
				}
			}
			else
			{
				if( action == JResignAction.RELEASE_ATTRIBUTES ||
					action == JResignAction.DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES )
				{
					// RELEASE ATTRIBUTES
					releaseAttributes( federate, instance );
				}
			}
		}
	}
	
	private void releaseAttributes( int federateHandle, OCInstance objectInstance )
	{
		Set<Integer> released = new HashSet<Integer>();
		for( ACInstance attribute : objectInstance.getAllAttributes() )
		{
			// if the federate is the owner of this attribute, release it
			if( attribute.getOwner() == federateHandle )
			{
				released.add( attribute.getHandle() );
				//attribute.unown(); --this is done in response to the release message below
			}
		}

		// spit out an unconditional divest notification for any attributes that were released
		AttributeDivest release = new AttributeDivest( objectInstance.getHandle(), released, true );
		reprocessIncoming( release );
		
		// log a helpful message
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Releasing attributes ["+acMoniker(released)+"] of object ["+
			              objectMoniker(objectInstance.getHandle())+"] after resign of federate ["+
			              moniker(federateHandle)+"]" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
