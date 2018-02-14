/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.hla1516e.handlers;

import hla.rti1516e.OrderType;

import java.util.HashMap;
import java.util.Map;

import static org.portico.impl.hla1516e.types.HLA1516eTransportationTypeHandleFactory.*;

import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.impl.hla1516e.handlers2.SupplementalInfo;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMap;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.SendInteraction;

/**
 * Generate receiveInteraction() callbacks to a IEEE1516e compliant federate ambassador
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords="lrc1516e",
                sinks="incoming",
                priority=3,
                messages=SendInteraction.class)
public class ReceiveInteractionCallbackHandler extends HLA1516eCallbackHandler
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
		SendInteraction request = context.getRequest( SendInteraction.class, this );
		int classHandle = request.getInteractionId();
		HashMap<Integer,byte[]> parameters = request.getParameters();
		double timestamp = request.getTimestamp();

		// convert the attributes into an appropriate form
		HLA1516eParameterHandleValueMap received = new HLA1516eParameterHandleValueMap(parameters);
		
		// generate the Supplemental Information
		SupplementalInfo supplement = null;//new SupplementalInfo( request.getSourceFederate() );
		
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
