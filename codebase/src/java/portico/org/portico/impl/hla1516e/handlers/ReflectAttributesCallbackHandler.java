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
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleValueMap;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.common.services.object.msg.UpdateAttributes.FilteredAttribute;

/**
 * Generate reflectAttributeValues() callbacks to a IEEE1516e compliant federate ambassador
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords="lrc1516e",
                sinks="incoming",
                priority=3,
                messages=UpdateAttributes.class)
public class ReflectAttributesCallbackHandler extends HLA1516eCallbackHandler
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
		HashMap<Integer,byte[]> attributes = getFilteredAttributes( request );
		double timestamp = request.getTimestamp();

		// convert the attributes into an appropriate form
		HLA1516eAttributeHandleValueMap reflected = new HLA1516eAttributeHandleValueMap(attributes);
		SupplementalInfo supplement = null;//new SupplementalInfo( request.getSourceFederate() );
		
		
		// do the callback
		if( request.isTimestamped() )
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK reflectAttributeValues(object="+objectHandle+",attributes="+
				              acMonikerWithSizes(attributes)+
				              ",time="+timestamp+") (TSO)" );
			}
			
			fedamb().reflectAttributeValues( new HLA1516eHandle(objectHandle),
			                                 reflected,                 // attributes
			                                 request.getTag(),          // tag
			                                 OrderType.TIMESTAMP,       // sent order
			                                 RELIABLE,                  // transport
			                                 new DoubleTime(timestamp), // time
			                                 OrderType.TIMESTAMP,       // received order
			                                 supplement );              // supplemental reflect info
		}
		else
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK reflectAttributeValues(object="+objectHandle+",attributes="+
				              acMonikerWithSizes(attributes)+") (RO)" );
			}
			
			fedamb().reflectAttributeValues( new HLA1516eHandle(objectHandle),
			                                 reflected,                 // attributes
			                                 request.getTag(),          // tag
			                                 OrderType.RECEIVE,         // sent order
			                                 BEST_EFFORT,               // transport
			                                 supplement );              // supplemental reflect info
		}
		
		context.success();
		
		if( logger.isTraceEnabled() )
			logger.trace( "         reflectAttributeValues() callback complete" );
	}

	/**
	 * The set of filtered attributes that should be delivered to the federate are contained in a
	 * Map<Integer,FilteredAttribute> where FilteredAttribute contains the byte[] value and the
	 * subscription region that overlapped with the sending region. This information is put into
	 * the information returned in the callback in HLA 1.3, but isn't in 1516. This method converts
	 * that map into a Map<Integer,byte[]> we can deliver as part of a 1516 callback.
	 */
	private HashMap<Integer,byte[]> getFilteredAttributes( UpdateAttributes request )
	{
		HashMap<Integer,byte[]> filteredSet = new HashMap<Integer,byte[]>();
		HashMap<Integer,FilteredAttribute> received = request.getFilteredAttributes();
		for( Integer attribute : received.keySet() )
			filteredSet.put( attribute, received.get(attribute).value );
		
		return filteredSet;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
