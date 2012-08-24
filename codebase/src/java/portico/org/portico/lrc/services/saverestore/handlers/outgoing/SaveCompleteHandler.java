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
package org.portico.lrc.services.saverestore.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JSaveNotInitiated;
import org.portico.lrc.services.saverestore.data.SaveRestoreFailed;
import org.portico.lrc.services.saverestore.msg.SaveComplete;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Handles notifications from the local federate that it has completed saving. If the local federate
 * was successful, this handler will then try and save the local LRC state to a file. Should this
 * fail, the success notice will be switched to a failure before broadcasting it out to the other
 * federates.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=SaveComplete.class)
public class SaveCompleteHandler extends LRCMessageHandler
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
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkRestore();

		// check to make sure there is an active save request
		if( saveManager.isInProgress() == false )
			throw new JSaveNotInitiated( "Can't complete federate save, no save initiated" );

		SaveComplete request = context.getRequest( SaveComplete.class, this );
		boolean success = request.isSuccessful();
		
		if( logger.isDebugEnabled() )
		{
			String result = success ? "SUCCESSFUL" : "UNSUCCESSFUL";
			logger.debug( "NOTICE  Local federate ["+moniker()+"] has completed save, it was "+result );
		}
		
		if( request.isSuccessful() == false )
		{
			// local federate wasn't successful in its save attempt, don't both trying to save
			// out our LRC state.
			saveManager.federateSaveNotComplete( federateHandle() );
		}
		else
		{
			//////////////////////////////////////////////////////
			// the user save was a success, try to save locally //
			//////////////////////////////////////////////////////
			if( logger.isDebugEnabled() )
				logger.debug( "Attempting to write LRC state to file" );

			String location = PorticoConstants.getSaveLocation( saveManager.getActiveLabel(),
			                                                    federateName() );
			try
			{
				lrcState.getSerializer().save( lrcState.getManifest(), location );
				saveManager.federateSaveComplete( federateHandle() );
				
				if( logger.isInfoEnabled() )
					logger.info( "SUCCESS Federate ["+moniker()+"] state has been saved, notify federation" );
			}
			catch( SaveRestoreFailed srf )
			{
				// FAILURE!
				// mark the save as incomplete and replace the existing request with info about why
				logger.error( "Save Failed", srf );
				saveManager.federateSaveNotComplete( federateHandle() );
				request.setFailure();
			}
		}
		
		// pass the request on to the federation
		connection.broadcast( request );
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
