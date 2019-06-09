/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.lrc.services.time.outgoing;

import java.util.Map;

import org.portico.lrc.compat.JAsynchronousDeliveryAlreadyEnabled;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.lrc.LRCMessageHandler;

public class EnableAsyncDeliveryHandler extends LRCMessageHandler
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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		// basic state validity checks
		lrcState.checkJoined();          // FederateNotExecutionMember
		lrcState.checkSave();            // SaveInProgress
		lrcState.checkRestore();         // RestoreInProgress

		if( logger.isDebugEnabled() )
			logger.debug( "REQUEST Enable asynchronous delivery for [%s]", moniker() );
		
		if( timeStatus().isAsynchronous() )
			throw new JAsynchronousDeliveryAlreadyEnabled();
		
		// send the request to the RTI for processing
		connection.sendControlRequest( context );
		
		// did it generate an error?
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		// everything was fine, so record that async has been enabled
		timeStatus().setAsynchronous( true );
		if( logger.isInfoEnabled() )
			logger.debug( "SUCCESS Enabled asynchronous delivery for [%s]", moniker() );

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
