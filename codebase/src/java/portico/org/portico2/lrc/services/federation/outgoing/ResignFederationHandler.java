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
package org.portico2.lrc.services.federation.outgoing;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.federation.msg.ResignFederation;
import org.portico2.lrc.LRCMessageHandler;

public class ResignFederationHandler extends LRCMessageHandler
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
		ResignFederation request = context.getRequest( ResignFederation.class, this );
		
		lrcState.checkJoined();
		
		// set the federate and federation name
		String federateName = lrcState.getFederateName();
		String federateType = lrcState.getFederateType();
		String federationName = lrcState.getFederationName();
		request.setFederateName( federateName );
		request.setFederateType( federateType );
		request.setFederationName( federationName );
		
		// log the request and pass it on to the connection
		logger.debug( "ATTEMPT Resign federate ["+federateName+
		              "] from federation ["+federationName+
		              "]: action="+request.getResignAction() );

		// send the resign notification to the connection and the federation
		connection.sendControlRequest( context );
		if( context.isErrorResponse() )
			throw context.getErrorResponseException();

		// notify the notification manager
		lrcState.localFederateResignedFromFederation();
		
		logger.info( "SUCCESS Resigned federate ["+federateName+
		             "] from federation ["+federationName+
		             "]: action="+request.getResignAction() );
	}
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
