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
package org.portico2.rti.services.federation.incoming;

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.WelcomePack;
import org.portico2.rti.RtiConnection;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.services.RTIMessageHandler;

public class JoinFederationHandler extends RTIMessageHandler
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
		// Get the bits we need and log what is happening
		JoinFederation request = context.getRequest( JoinFederation.class );
		String federateName = request.getFederateName();
		String federateType = request.getFederateType();
		String federationName = request.getFederationName();

		logger.debug( "ATTEMPT Federate [%s] join federation [%s]", federateName, federationName );

		// Get the connection that this federate is using
		RtiConnection connection = request.getConnection();
		if( connection == null )
			throw new JRTIinternalError( "Could not find the connection for the federate %s", federateName );

		// Create the federate and attach it to the federation
		Federate federate = new Federate( federateName, federateType, request.getConnection() );
		federate.addRawFomModules( request.getRawFomModules() );
		
		int federateHandle = federation.joinFederate( federate );
		
		logger.info( "SUCCESS Federate [%s] joined federation [%s] with handle [%d]",
		             federateName, federationName, federateHandle );

		// Now we need to create a Welcome Pack for the federate. This has all the information
		// they need when starting up, like their handle, the consolidated object model and so on
		WelcomePack welcome = new WelcomePack();
		welcome.setFederateHandle( federateHandle );
		welcome.setFederationHandle( federation.getFederationHandle() );
		welcome.setFederationName( federationName );
		welcome.setFederateName( federateName );
		welcome.setFederateType( federateType );
		welcome.setFOM( federation.getFOM() );
		if( federation.getFederationKey() != null )
			welcome.setFederationKey( federation.getFederationKey().getEncoded() );
		// federation state - sync points
		welcome.setSyncPoints( syncManager.getAllUnsynchronizedLabels() );
		
		
		// Do any internal house-keeping required
		timeManager.joinedFederation( federateHandle, null );
		momManager.joinedFederation( federate );

		context.success( welcome );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
