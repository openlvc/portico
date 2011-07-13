/*
 *   Copyright 2009 The Portico Project
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

import java.util.Map;

import org.portico.lrc.services.ownership.msg.QueryOwnershipResponse;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Generates attributeIsNotOwned(), attributeOwnedByRTI() and informAttributeOwnership() callbacks.
 */
@MessageHandler(modules="lrc13-callback",
                keywords= {"lrc13","lrcjava1"},
                sinks="incoming",
                priority=3,
                messages=QueryOwnershipResponse.class)
public class AttributeOwnershipQueryCallbackHandler extends HLA13CallbackHandler
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
			
			if( isStandard() )
				hla13().attributeIsNotOwned( objectHandle, attributeHandle );
			else
				java1().attributeIsNotOwned( objectHandle, attributeHandle );
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
			if( isStandard() )
				hla13().attributeOwnedByRTI( objectHandle, attributeHandle );
			else
				java1().attributeOwnedByRTI( objectHandle, attributeHandle );
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
			
			if( isStandard() )
				hla13().informAttributeOwnership( objectHandle, attributeHandle, owner );
			else
				java1().informAttributeOwnership( objectHandle, attributeHandle, owner );
		}
		
		context.success();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
