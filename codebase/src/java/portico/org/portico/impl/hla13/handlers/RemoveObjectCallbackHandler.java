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
import org.portico2.common.services.object.msg.DeleteObject;

/**
 * Generate removeObjectInstance() callbacks to a HLA 1.3 compliant federate ambassador
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=DeleteObject.class)
public class RemoveObjectCallbackHandler extends HLA13CallbackHandler
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
		DeleteObject request = context.getRequest( DeleteObject.class, this );
		int objectHandle = request.getObjectHandle();
		double timestamp = request.getTimestamp();

		// log the callback
		if( logger.isTraceEnabled() )
		{
			String timeInfo = ") (RO)";
			if( request.isTimestamped() )
				timeInfo = ",time:"+timestamp+") (TSO)";

			logger.trace( "CALLBACK removeObjectInstance(object="+objectHandle+timeInfo );
		}

		// do the callback
		if( isStandard() )
		{
			// convert the attributes into an appropriate form
			if( request.isTimestamped() )
			{
				DoubleTime time = new DoubleTime( timestamp );
				hla13().removeObjectInstance( objectHandle, request.getTag(), time, null );
			}
			else
			{
				hla13().removeObjectInstance( objectHandle, request.getTag() );
			}
		}
		else
		{
			String tag = new String( request.getTag() );
			if( request.isTimestamped() )
			{
				byte[] time = EncodingHelpers.encodeDouble( timestamp );
				java1().removeObjectInstance( objectHandle, time, tag, null );
			}
			else
			{
				java1().removeObjectInstance( objectHandle, tag );
			}
		}

		context.success();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
