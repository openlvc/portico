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
package org.portico.impl.hla1516.handlers;

import hla.rti1516.FederateAmbassador;

import java.util.HashMap;
import java.util.Map;

import org.portico.impl.hla1516.Impl1516Helper;
import org.portico.impl.hla1516.types.DoubleTime;
import org.portico.impl.hla1516.types.HLA1516InteractionClassHandle;
import org.portico.impl.hla1516.types.HLA1516ParameterHandleValueMap;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.SendInteraction;

/**
 * Generate receiveInteraction() callbacks to a IEEE1516 compliant federate ambassador
 */
@MessageHandler(modules="lrc1516-callback",
                keywords="lrc1516",
                sinks="incoming",
                priority=3,
                messages=SendInteraction.class)
public class ReceiveInteractionCallbackHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl1516Helper helper;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.helper = (Impl1516Helper)lrc.getSpecHelper();
	}
	
	public void process( MessageContext context ) throws Exception
	{
		SendInteraction request = context.getRequest( SendInteraction.class, this );
		int classHandle = request.getInteractionId();
		HashMap<Integer,byte[]> parameters = request.getParameters();
		double timestamp = request.getTimestamp();

		// convert the attributes into an appropriate form
		HLA1516ParameterHandleValueMap received = new HLA1516ParameterHandleValueMap( parameters );
		
		// do the callback
		FederateAmbassador fedamb = helper.getFederateAmbassador();
		if( request.isTimestamped() )
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK receiveInteraction(class="+classHandle+",parameters="+
				              PorticoConstants.mapToStringWithSizes(parameters)+",time="+
				              timestamp+") (TSO)" );
			}
			
			fedamb.receiveInteraction( new HLA1516InteractionClassHandle(classHandle),
			                           received,                  // map
			                           request.getTag(),          // tag
			                           null,                      // sent order
			                           null,                      // transport
			                           new DoubleTime(timestamp), // time 
			                           null );                    // received order
		}
		else
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK receiveInteraction(class="+classHandle+",parameters="+
				              PorticoConstants.mapToStringWithSizes(parameters)+") (RO)" );
			}
			
			fedamb.receiveInteraction( new HLA1516InteractionClassHandle(classHandle),
			                           received,          // map
			                           request.getTag(),  // tag
			                           null,              // sent order
			                           null );            // transport
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
