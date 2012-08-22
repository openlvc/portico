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
package org.portico.lrc.services.sync.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.services.sync.data.SyncPoint;
import org.portico.lrc.services.sync.data.SyncPoint.Status;
import org.portico.lrc.services.sync.msg.SyncRegistrationRequest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * This handler will process incoming synchronization point registration requests. The request
 * process is broken up into two parts: 1) label locking, 2) announcement. This handler deals
 * with the first part.
 * <p/>
 * <b>Label Locking</b>
 * <p/>
 * Before announcing a point, a federate must check to see that others haven't attempted to
 * register that point yet. If they have, there will be a potential sync point label conflict
 * (particularly if they both try to register the same label at the same time).
 * <p/>
 * When attempting to register a sync point, a {@link SyncRegistrationRequest} is sent out by the
 * federate. This handler processes these messages. If it turns out that the local federate has
 * also requested the point, then this handler will store the registration request in the sync
 * point.
 * <p/>
 * After the original federate has waited a certain period of time for other federates to send out
 * their requests, it will check to see if anyone else has tried to register the point. If it turns
 * out that others have, then it will look at their handles. The federate with the lowest handle
 * (the oldest federate) is the only one who can register the point. If any of the other handles
 * are lower, the federate will queue up a failure notice at that time, otherwise it will queue up
 * a success notice.
 * 
 * <p/>
 * <b>Peeking</b>
 * <p/>
 * When a federate attempts to register a point itself, it associates the label with the
 * {@link Status#REQUESTED} status. In order to short-circuit potential conflicts, other
 * federates that have not yet registered the point (and thus to whom all of the above doesn't
 * apply) can "peek" at the registration request messages and associate the {@link Status#PENDING}
 * with them until the actual announcement comes through. This way, there is no need to go through
 * the whole process mentioned above if another federate attempts to lock a label while it knows
 * another federate is waiting.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=SyncRegistrationRequest.class)
public class SyncRegistrationRequestHandler extends LRCMessageHandler
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
		SyncRegistrationRequest request = context.getRequest( SyncRegistrationRequest.class, this );
		// before processing, make sure this isn't a message that we broadcast out outselves :P
		vetoIfMessageFromUs( request );

		String label = request.getLabel();
		int registrant = request.getSourceFederate();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE SyncPoint Registration Request for label ["+label+
			              "] by federate ["+moniker(registrant)+"]" );
		}
		
		// check to see if we have an outstanding registation request for this or not
		SyncPoint point = syncManager.getPoint( label );
		if( point == null )
		{
			// don't know about the point, record it locally to stop us trying to register it
			point = syncManager.pointPending( label, null, request.getFederates(), registrant );
			if( logger.isDebugEnabled() )
			{
				logger.debug( "PENDING SyncPoint Registration Request now PENDING: label="+
				              label+", federate="+moniker(registrant) );
			}
		}
		else if( point.getStatus() == SyncPoint.Status.REQUESTED )
		{
			// we have also requested to register this point, the federate with
			// the lowest handle is the one that will be allowed to register it
			// yes, the code is a bit convoluted for the logging, apologies!
			int existingRegistrant = point.getRegistrant();
			point.requestedRegistration( registrant );

			// log the events
			if( logger.isDebugEnabled() && (existingRegistrant == point.getRegistrant()) )
			{
				// existing registrant has not been displaced
				logger.debug( "SyncPoint Registration Request for ["+label+"] by ["+
				              moniker(registrant)+"] DID NOT DISPLACE existing request by ["+
				              moniker(existingRegistrant)+"]" );				
			}
			else if( logger.isDebugEnabled() && (registrant == point.getRegistrant()) )
			{
				logger.debug( "SyncPoint Registration Request for ["+label+"] by ["+
				              moniker(registrant)+"] DISPLACED existing request by ["+
				              moniker(existingRegistrant)+"]" );
			}
		}
		else if( logger.isDebugEnabled() )
		{
			logger.debug( "IGNORE  SyncPoint Registration Request for label ["+label+
			              "] by federate ["+moniker(registrant)+"]: already registered" );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
