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
import org.portico.lrc.services.saverestore.msg.RestoreComplete;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=RestoreComplete.class)
public class RestoreCompleteIncomingHandler extends LRCMessageHandler
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
		RestoreComplete request = context.getRequest( RestoreComplete.class, this );
		int sourceFederate = request.getSourceFederate();
		boolean success = request.isSuccessful();

		String result = success ? "successful" : "not successful";
		
		// NOTE: When a federate resigns, we need to reassess whether or not an outstanding
		//       restore can now be completed. For this reason, if a RestoreComplete message comes
		//       into the incoming sink with a source of NULL_HANDLE, we known it is a dummy
		//       for the purposes of rechecking
		if( request.getSourceFederate() != PorticoConstants.NULL_HANDLE )
		{
			if( logger.isDebugEnabled() )
			{
				logger.debug( "@REMOTE Federate restore COMPLETE ("+result+") by ["+
				              moniker(request.getSourceFederate())+"]" );
			}
			
			if( success )
				restoreManager.federateRestoreComplete( sourceFederate );
			else
				restoreManager.federateRestoreNotComplete( sourceFederate );
		}
		
		// Check to see if the entire federation has finished, if so, let the message through
		// so it can trigger a callback. So that we can reset the restore status here, if the
		// restore is complete we will set the "success" property of the request to true  (or
		// the opposite if unsuccessful) and the callback handler can use that as a guide to
		// figuring out which callback method to call. The RestoreManager status is wiped if
		// the call is successful
		if( restoreManager.isRestoreComplete() )
		{
			if( logger.isInfoEnabled() )
			{
				logger.info( "NOTICE  Federation has completed restore ("+result+") for label ["+
				             restoreManager.getActiveLabel()+"]" );
			}
			
			// set status on the request and let it through for callback
			request.setSuccessful( restoreManager.isRestoreCompleteSuccessful() );

			// wipe the status
			restoreManager.reset();
		}
		else
		{
			// shouldn't let this through for a callback yet, federation restore not complete
			if( logger.isDebugEnabled() )
				logger.debug( "Federation not yet restored, still waiting on more federates" );

			veto();
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
