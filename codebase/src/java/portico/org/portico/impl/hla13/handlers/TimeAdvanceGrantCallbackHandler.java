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
package org.portico.impl.hla13.handlers;

import hla.rti13.java1.EncodingHelpers;

import java.util.Map;

import org.portico.impl.hla13.types.DoubleTime;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.time.msg.TimeAdvanceGrant;

@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=TimeAdvanceGrant.class)
public class TimeAdvanceGrantCallbackHandler extends HLA13CallbackHandler
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
		TimeAdvanceGrant grant = context.getRequest( TimeAdvanceGrant.class, this );
		if( logger.isTraceEnabled() )
			logger.trace( "CALLBACK timeAdvanceGrant(time="+grant.getTime()+")" );
		
		if( isStandard() )
		{
			DoubleTime newTime = new DoubleTime( grant.getTime() );
			hla13().timeAdvanceGrant( newTime );
		}
		else
		{
			byte[] newTime = EncodingHelpers.encodeDouble( grant.getTime() );
			java1().timeAdvanceGrant( newTime );
		}

		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
