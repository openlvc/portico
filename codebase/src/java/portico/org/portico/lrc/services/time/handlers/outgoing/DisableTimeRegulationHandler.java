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
package org.portico.lrc.services.time.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JTimeRegulationWasNotEnabled;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.DisableTimeRegulation;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=DisableTimeRegulation.class)
public class DisableTimeRegulationHandler extends LRCMessageHandler
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
		// basic state validity checks
		lrcState.checkJoined();         // FederateNotExecutionMember
		lrcState.checkSave();           // SaveInProgress
		lrcState.checkRestore();        // RestoreInProgress

		DisableTimeRegulation request = context.getRequest( DisableTimeRegulation.class, this );

		if( logger.isDebugEnabled() )
			logger.debug( "REQUEST Disable time regulation for ["+moniker()+"]" );

		if( timeStatus().getRegulating() == TimeStatus.TriState.OFF )
			throw new JTimeRegulationWasNotEnabled("");
		
		timeManager.disableRegulating( lrcState.getFederateHandle() );
		
		// notify everyone else
		connection.broadcast( request );
		context.success();

		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS Disabled time regulation ["+moniker()+"]" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
