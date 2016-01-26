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

import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.lrc.services.ownership.msg.QueryOwnershipResponse;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Generates attributeIsNotOwned(), attributeOwnedByRTI() and informAttributeOwnership() callbacks.
 */
@MessageHandler(modules="lrc1516e-callback",
                keywords= {"lrc1516e"},
                sinks="incoming",
                priority=3,
                messages=QueryOwnershipResponse.class)
public class AttributeOwnershipQueryCallbackHandler extends HLA1516eCallbackHandler
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
		QueryOwnershipResponse callback = context.getRequest( QueryOwnershipResponse.class, this );
		int objectHandle = callback.getObjectHandle();
		int attributeHandle = callback.getAttributeHandle();
		int owner = callback.getOwner();

		if( callback.isUnowned() )
		{
			//////////////////////////
			// attribute is UNOWNED //
			//////////////////////////
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK attributeIsNotOwned(object="+objectHandle+",attribute="+
				              attributeHandle+")" );
			}
			
			fedamb().attributeIsNotOwned( new HLA1516eHandle(objectHandle),
			                              new HLA1516eHandle(attributeHandle) );
			
			logger.trace( "         attributeIsNotOwned() callback complete" );
		}
		else if( callback.isOwnedByRti() )
		{
			////////////////////////////
			// attribute OWNED BY RTI //
			////////////////////////////
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK attributeOwnedByRTI(object:"+objectHandle+",attribute:"+
				              attributeHandle+")" );
			}
			
			// owned by the RTI
			fedamb().attributeIsOwnedByRTI( new HLA1516eHandle(objectHandle),
			                                new HLA1516eHandle(attributeHandle) );
			
			logger.trace( "         attributeOwnedByRTI() callback complete" );
		}
		else
		{
			/////////////////////////////////
			// attribute OWNED BY FEDERATE //
			/////////////////////////////////
			if( logger.isTraceEnabled() )
			{
				logger.trace( "CALLBACK informAttributeOwnership(object:"+objectHandle+
				              ",attribute:"+attributeHandle+",owner:"+owner+")" );
			}

			fedamb().informAttributeOwnership( new HLA1516eHandle(objectHandle),
			                                   new HLA1516eHandle(attributeHandle),
			                                   new HLA1516eHandle(owner) );
			
			logger.trace( "         informAttributeOwnership() callback compelte" );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
