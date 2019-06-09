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
import hla.rti13.java1.ReflectedAttributes;

import java.util.HashMap;
import java.util.Map;

import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.HLA13ReflectedAttributes;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.common.services.object.msg.UpdateAttributes.FilteredAttribute;

/**
 * Generate reflectAttributeValues() callbacks to a HLA 1.3 compliant federate ambassador
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=UpdateAttributes.class)
public class ReflectAttributesCallbackHandler extends HLA13CallbackHandler
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
		UpdateAttributes request = context.getRequest( UpdateAttributes.class, this );
		int objectHandle = request.getObjectId();
		HashMap<Integer,byte[]> attributes = request.getAttributes();
		HashMap<Integer,FilteredAttribute> filteredAttributes = request.getFilteredAttributes();
		double timestamp = request.getTimestamp();

		// log the callback
		if( logger.isTraceEnabled() )
		{
			String timeInfo = ") (RO)";
			if( request.isTimestamped() )
				timeInfo = ",time:"+timestamp+") (TSO)";

			// strip the supplied attributes of those that were not filtered out - this is a hack
			// to deal with the new way of handling regions and filtered attributes
			HashMap<Integer,byte[]> filteredValuesOnly = new HashMap<Integer,byte[]>();
			for( Integer key : attributes.keySet() )
			{
				if( filteredAttributes.containsKey(key) )
					filteredValuesOnly.put(key,attributes.get(key) );
			}
			
			
			logger.trace( "CALLBACK reflectAttributeValues(object="+objectHandle+",attributes="+
			              super.acMonikerWithSizes(filteredValuesOnly)+timeInfo );
		}
		
		// do the callback
		if( isStandard() )
		{
			// convert the attributes into an appropriate form
			HLA13ReflectedAttributes reflected = new HLA13ReflectedAttributes( filteredAttributes );
			byte[] tag = request.getTag();
			
			if( request.isTimestamped() )
			{
				DoubleTime time = new DoubleTime( timestamp );
				hla13().reflectAttributeValues( objectHandle, reflected, tag, time, null );
			}
			else
			{
				hla13().reflectAttributeValues( objectHandle, reflected, tag );
			}
		}
		else
		{
			// convert the attributes into an appropriate form
			ReflectedAttributes reflected = new ReflectedAttributes( filteredAttributes );
			String tag = new String( request.getTag() );
			
			if( request.isTimestamped() )
			{
				byte[] time = EncodingHelpers.encodeDouble( timestamp );
				java1().reflectAttributeValues( objectHandle, reflected, time, tag, null );
			}
			else
			{
				java1().reflectAttributeValues( objectHandle, reflected, tag );
			}
		}

		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
