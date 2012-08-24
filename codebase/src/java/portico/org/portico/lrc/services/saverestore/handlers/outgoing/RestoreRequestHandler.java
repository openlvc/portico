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

import hla.rti.RTIinternalError;

import java.io.File;
import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.services.saverestore.msg.RestoreFederation;
import org.portico.lrc.services.saverestore.msg.RestoreRequest;
import org.portico.lrc.services.saverestore.msg.RestoreRequestResult;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=RestoreRequest.class)
public class RestoreRequestHandler extends LRCMessageHandler
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
		lrcState.checkSave();
		lrcState.checkRestore();
		
		// get the request and details
		RestoreRequest request = context.getRequest( RestoreRequest.class, this );
		String label = request.getLabel();
		
		// make sure we haven't got a null handle
		if( label == null )
			throw new RTIinternalError( "Restore label was null" );
		
		if( logger.isDebugEnabled() )
			logger.debug( "REQUEST Start federation RESTORE with label ["+label+"]" );

		// check to see if the restore files exist
		File file = new File( PorticoConstants.getSaveLocation(label,federateName()) );
		if( file.exists() == false )
			queueFailure( label, "Can't locate save file to restore from: "+file.getAbsolutePath() );
		
		// record our intention to kick off a restore
		restoreManager.requestRestore( federateHandle(), label );
		
		// now wait for a period of time to see if we get trumped. using sendAndWaitTimeoutOK
		// just allows us to wait for a connection-determined period rather than arbitrarily
		// defining some time that may/may-not be suitable depending on the comms mechanism in use
		connection.broadcastAndSleep( request );

		// check to see if we were trumped or not, if we were, send a failure notice
		if( restoreManager.getRegisteringFederate() != federateHandle() )
		{
			// dammit, we were beaten out
			queueFailure( label, "Restore initiated at same time by more senior federate [label="+
			              restoreManager.getActiveLabel()+",federate="+
			              moniker(restoreManager.getRegisteringFederate())+"]" );
		}
		else
		{
			if( logger.isInfoEnabled() )
				logger.info( "Broadcast request to start federation restore with label ["+label+"]" );

			// broadcast the "success"
			RestoreFederation result = new RestoreFederation( label );
			connection.broadcast( fill(result) );
		}

		context.success();
	}
	
	private void queueFailure( String label, String reason )
	{
		logger.warn( "FAILURE Request restore, reason="+reason );
		RestoreRequestResult result = new RestoreRequestResult( label, false, reason );
		lrcState.getQueue().offer( fill(result) );
		veto();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
