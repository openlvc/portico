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
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.lrc.services.saverestore.msg.RestoreComplete;
import org.portico.lrc.services.saverestore.msg.SaveComplete;
import org.portico.lrc.services.sync.data.SyncPoint;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.federation.msg.ResignFederation;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.common.services.sync.msg.SyncPointAchieved;

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

		// see method comment for the full, hairy details of why this exists
		dealWithJGroupsResignHack( request.getSourceFederate() );
		
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

	/**
	 * Make sure we know about the federate (this is to get around a solution to a separate
	 * problem with the JGroups binding where the connection.resignFederation() implementation was
	 * used to pass the resign message to federations. Normally, the federate would broadcast out
	 * that message, then the connection would be told to resign and would just do
	 * connection-specific resignation stuff. However, under some conditions in the JGroups binding,
	 * the connection was being terminated after the regular broadcast of the resign has been given
	 * to it, but before it has progressed all the way through the protocol stack and been sent.
	 * Thus, remote federates were never receiving the resign message into their LRCQueues.
	 * <p/>
	 * To solve this problem I had the connection.resignFederation() method in JGroups serialize
	 * and send the message and then drop it on the incoming handler on the other side. The resign
	 * control message always got through, because it was an OOB/High Priority message that all
	 * other federates had to respond to. However, this began mixing concerns. Connections shouldn't
	 * have to deal with message creation, and for the other big-4 methods (create/destroy/join)
	 * they didn't. I didn't want to have this one be a special case, so I decided to just add this
	 * check here. If when using JGroups, the resign message does get through before the
	 * connection control message, two resign messages for a particular federate will be generated.
	 * This method checks to see if the resigning federate is known, and if it isn't, the message
	 * is veto'd. This way, if two messages do get through, the second is ignored and the process
	 * of only mixing HLA and connection implementation remains confined to one call inside the
	 * JGroups implementation and not the mandated way all connection implementations have to deal
	 * with resignations. 
	 */
	private void dealWithJGroupsResignHack( int federateHandle )
	{
		vetoIfSourceNotJoined( federateHandle );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
