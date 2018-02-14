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
import org.portico.impl.hla1516.types.HLA1516AttributeHandleValueMap;
import org.portico.impl.hla1516.types.HLA1516ObjectInstanceHandle;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.common.services.object.msg.UpdateAttributes.FilteredAttribute;

/**
 * Generate reflectAttributeValues() callbacks to a IEEE1516 compliant federate ambassador
 */
@MessageHandler(modules="lrc1516-callback",
                keywords="lrc1516",
                sinks="incoming",
                priority=3,
                messages=UpdateAttributes.class)
public class ReflectAttributesCallbackHandler extends LRCMessageHandler
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
		UpdateAttributes request = context.getRequest( UpdateAttributes.class, this );
		int objectHandle = request.getObjectId();
		HashMap<Integer,byte[]> attributes = getFilteredAttributes( request );
		double timestamp = request.getTimestamp();

		// convert the attributes into an appropriate form
		HLA1516AttributeHandleValueMap reflected = new HLA1516AttributeHandleValueMap( attributes );
		
		// do the callback
		FederateAmbassador fedamb = helper.getFederateAmbassador();
		if( request.isTimestamped() )
		{
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK reflectAttributeValues(object="+objectHandle+",attributes="+
				              PorticoConstants.mapToStringWithSizes(attributes)+
				              ",time="+timestamp+") (TSO)" );
			}
			
			fedamb.reflectAttributeValues( new HLA1516ObjectInstanceHandle(objectHandle),
			                               reflected,                 // attributes
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
				logger.trace( "CALLBACK reflectAttributeValues(object="+objectHandle+",attributes="+
				              PorticoConstants.mapToStringWithSizes(attributes)+") (RO)" );
			}
			
			fedamb.reflectAttributeValues( new HLA1516ObjectInstanceHandle(objectHandle),
			                               reflected,                 // attributes
			                               request.getTag(),          // tag
			                               null,                      // sent order
			                               null );                    // transport
		}
		
		context.success();
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
