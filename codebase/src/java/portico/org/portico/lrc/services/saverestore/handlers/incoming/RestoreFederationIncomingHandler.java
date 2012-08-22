/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.saverestore.handlers.incoming;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.services.saverestore.data.SaveRestoreFailed;
import org.portico.lrc.services.saverestore.msg.RestoreBegun;
import org.portico.lrc.services.saverestore.msg.RestoreComplete;
import org.portico.lrc.services.saverestore.msg.RestoreFederation;
import org.portico.lrc.services.saverestore.msg.RestoreInitiate;
import org.portico.lrc.services.saverestore.msg.RestoreRequestResult;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=RestoreFederation.class)
public class RestoreFederationIncomingHandler extends LRCMessageHandler
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
		RestoreFederation request = context.getRequest( RestoreFederation.class, this );
		String label = request.getLabel();
		
		// NOTE! Failure notifications are not sent remotely, so if this is a failure notice
		//       then this handler must be in the same LRC that requested the point initially.
		//       As such, just let the message on through to the callback handler so that the
		//       fail can be delivered appropriately.
		if( request.getSuccessStatus() == false )
		{
			// MESSAGE IS FROM US! just let it through for callback
			return;
		}
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Confirmation to start RESTORE with label ["+label+"] from ["+
			              moniker(request.getSourceFederate())+"]" );
		}

		// make sure we have the right label, if we don't record the problem and replace the
		// existing one with this one
		if( label.equals(restoreManager.getActiveLabel()) == false )
		{
			logger.warn( "Received restore confirmation for label ["+label+
			             "] but only have pending request for label ["+
			             restoreManager.getActiveLabel()+"]: replacing existing with this one" );
			restoreManager.requestRestore( request.getSourceFederate(), label );
		}
		
		// if we were the ones who generated the initial request, make sure we get a callback
		// telling us of the result
		if( request.getSourceFederate() == federateHandle() )
		{
			RestoreRequestResult result = new RestoreRequestResult( label );
			reprocessIncoming( fill(result) );
		}
		
		// record the confirmation (this will set the pending flag to false as well)
		restoreManager.restoreBegun();
		// deliver the restore begun callback RIGHT AWAY
		RestoreBegun callback = new RestoreBegun();
		reprocessIncoming( fill(callback) );
		
		/////////////////////////////////
		// restore internal state data //
		/////////////////////////////////
		try
		{
			logger.debug( "Attempting to read LRC state from file" );
			String location = PorticoConstants.getSaveLocation( restoreManager.getActiveLabel(),
			                                                    federateName() );

			lrcState.getSerializer().restore( lrcState.getManifest(), location );
			logger.debug( "Restored internal state for federate ["+moniker()+"] from ["+location+"]" );
		}
		catch( SaveRestoreFailed srf )
		{
			logger.error( "Error while restoring internal state from file", srf );
			RestoreComplete failNotice = new RestoreComplete( false );
			this.reprocessOutgoing( failNotice );
			veto();
		}
		
		// tell the federate that it's now time for it to do its restoration
		RestoreInitiate initiate = new RestoreInitiate( restoreManager.getActiveLabel(),
		                                                federateHandle() );
		reprocessIncoming( fill(initiate) );
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
