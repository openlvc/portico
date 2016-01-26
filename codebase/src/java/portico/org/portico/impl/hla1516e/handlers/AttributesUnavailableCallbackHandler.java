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

import java.util.Map;
import java.util.Set;

import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.lrc.services.ownership.msg.AttributesUnavailable;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc1516e-callback",
                keywords= {"lrc1516e"},
                sinks="incoming",
                priority=3,
                messages=AttributesUnavailable.class)
public class AttributesUnavailableCallbackHandler extends HLA1516eCallbackHandler
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
		AttributesUnavailable unavailable = context.getRequest( AttributesUnavailable.class, this );
		vetoUnlessFromUs( unavailable );
		int objectHandle = unavailable.getObjectHandle();
		Set<Integer> attributes = unavailable.getAttributeHandles();
		
		if( logger.isTraceEnabled() )
		{
			logger.trace( "CALLBACK attributeOwnershipUnavailable(object="+objectHandle+
			              ",attributes="+attributes+")" );
		}

		HLA1516eAttributeHandleSet handleSet = new HLA1516eAttributeHandleSet( attributes );
		fedamb().attributeOwnershipUnavailable( new HLA1516eHandle(objectHandle), handleSet );
		
		context.success();
		
		logger.trace( "         attributeOwnershipUnavailable() callback complete" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
