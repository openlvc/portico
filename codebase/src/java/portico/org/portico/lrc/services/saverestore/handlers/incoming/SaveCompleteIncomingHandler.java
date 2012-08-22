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
import org.portico.lrc.services.saverestore.msg.SaveComplete;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=SaveComplete.class)
public class SaveCompleteIncomingHandler extends LRCMessageHandler
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
		SaveComplete request = context.getRequest( SaveComplete.class, this );
		boolean success = request.isSuccessful();
		String result = success ? "successful" : "not successful";
		
		// NOTE: When a federate resigns, we need to reassess whether or not an outstanding
		//       save can now be completed. For this reason, if a SaveComplete message comes
		//       into the incoming sink with a source of NULL_HANDLE, we known it is a dummy
		//       for the purposes of rechecking
		if( request.getSourceFederate() != PorticoConstants.NULL_HANDLE &&
			request.getSourceFederate() != federateHandle() )
		{
			if( logger.isDebugEnabled() )
			{
				logger.debug( "@REMOTE Federate save COMPLETE ("+result+") by ["+
				              moniker(request.getSourceFederate())+"]" );
			}
			
			// store the active save, if there is already an active save, log a big error message
			if( success )
				saveManager.federateSaveComplete( request.getSourceFederate() );
			else
				saveManager.federateSaveNotComplete( request.getSourceFederate() );
		}
		
		// Check to see if the entire federation has finished, if so, let the message through
		// so it can trigger a callback. So that we can reset the save status here, if the save
		// is complete we will set the "success" property of the request to true  (or the opposite
		// if unsuccessful) and the callback handler can use that as a guide to figuring out which
		// callback method to call. The SaveManager status is wiped if the call is successful
		if( saveManager.isSaveComplete() )
		{
			if( logger.isInfoEnabled() )
			{
				logger.info( "NOTICE  Federation has completed save("+result+") for label ["+
				             saveManager.getActiveLabel()+"]" );
			}
			
			// set status on the request and let it through for callback
			request.setSuccessful( saveManager.isSaveCompleteSuccessful() );

			// wipe the status
			saveManager.reset();
		}
		else
		{
			// shouldn't let this through for a callback yet, federation save not complete
			if( logger.isDebugEnabled() )
				logger.debug( "Federation not yet saved, still waiting on more federates" );

			veto();
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
