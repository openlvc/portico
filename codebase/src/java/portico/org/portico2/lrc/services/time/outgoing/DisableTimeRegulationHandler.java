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

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JTimeRegulationWasNotEnabled;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.common.services.time.msg.DisableTimeRegulation;
import org.portico2.lrc.LRCMessageHandler;

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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
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
		
		// send to the RTI
		connection.sendControlRequest( context );
		
		// check for error
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();
		
		// success! record that fact
		timeStatus().setRegulating( TimeStatus.TriState.OFF );
		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS Disabled time regulation ["+moniker()+"]" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
