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
package org.portico.impl.hla1516e.handlers2;

import static org.portico.impl.hla1516e.types.HLA1516eTransportationTypeHandleFactory.BEST_EFFORT;
import static org.portico.impl.hla1516e.types.HLA1516eTransportationTypeHandleFactory.RELIABLE;

import java.util.HashMap;
import java.util.Map;

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMap;
import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.SendInteraction;

import hla.rti1516e.OrderType;
import hla.rti1516e.exceptions.FederateInternalError;

public class ReceiveInteractionCallbackHandler extends LRC1516eCallbackHandler
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
	public void callback( MessageContext context ) throws FederateInternalError
	{
		SendInteraction request = context.getRequest( SendInteraction.class, this );
		int classHandle = request.getInteractionId();
		HashMap<Integer,byte[]> parameters = request.getParameters();
		double timestamp = request.getTimestamp();

		// convert the attributes into an appropriate form
		HLA1516eParameterHandleValueMap received = new HLA1516eParameterHandleValueMap(parameters);
		
		// generate the Supplemental Information
		SupplementalInfo supplement = new SupplementalInfo( request.getSourceFederate() );
		
		// do the callback
		if( request.isTimestamped() )
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK receiveInteraction(class="+classHandle+",parameters="+
				              super.pcMonikerWithSizes(parameters)+",time="+
				              timestamp+") (TSO)" );
			}
			
			fedamb().receiveInteraction( new HLA1516eHandle(classHandle),
			                             received,                  // map
			                             request.getTag(),          // tag
			                             OrderType.TIMESTAMP,       // sent order
			                             RELIABLE,                  // transport
			                             new DoubleTime(timestamp), // time 
			                             OrderType.TIMESTAMP,       // received order
			                             supplement );              // supplemental receive info
		}
		else
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK receiveInteraction(class="+classHandle+",parameters="+
				              super.pcMonikerWithSizes(parameters)+") (RO)" );
			}
			
			fedamb().receiveInteraction( new HLA1516eHandle(classHandle),
			                             received,          // map
			                             request.getTag(),  // tag
			                             OrderType.RECEIVE, // sent order
			                             BEST_EFFORT,       // transport
			                             supplement );      // supplemental receive info
		}
		
		context.success();

		if( logger.isTraceEnabled() )
			logger.trace( "         receiveInteraction() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
