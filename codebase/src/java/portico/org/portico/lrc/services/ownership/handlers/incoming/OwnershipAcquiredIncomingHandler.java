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
package org.portico.lrc.services.ownership.handlers.incoming;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.OwnershipAcquired;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * This handle processing incoming notifications of successful ownership transfers. It records the
 * data in the local store so that it is available for use by the local federate.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7,
                messages=OwnershipAcquired.class)
public class OwnershipAcquiredIncomingHandler extends LRCMessageHandler
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
		OwnershipAcquired request = context.getRequest( OwnershipAcquired.class, this );
		int federateHandle = request.getSourceFederate();
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributeHandles();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Ownership of attributes "+acMoniker(attributes)+" for object ["+
			              objectMoniker(objectHandle)+"] passed to ["+moniker(federateHandle)+"]" );
		}
		
		// record the ownership change for the attributes
		if( request.isIfAvailable() )
		{
			ownership.completeAcquisitionIfAvailable( objectHandle, federateHandle );
		}
		else
		{
			ownership.completeAcquisition( objectHandle, federateHandle );
		}
		
		/////////////////////////////////////////////////////////////////////////////
		// if the message is NOT from us, change the ownership of the attributes   //
		// if it is from us we will already have done this when it was appropriate //
		/////////////////////////////////////////////////////////////////////////////
		if( federateHandle != federateHandle() )
		{
			changeOwnership( objectHandle, attributes, federateHandle );
		}
		
		// unless this request is from us, no more processing! if it is from us, let it flow
		// through to the callback handler
		vetoUnlessFromUs( request );
	
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Acquired ownership of attributes "+acMoniker(attributes)+
			             " of object ["+objectMoniker(objectHandle)+"]" );
		}

		context.success();
	}

	/**
	 * Changes the owner of the attributes inside the object with the given handle to be the
	 * given federatehandle . If the object or any of the attributes cannot be found, a warning
	 * is logged but no exception is thrown.
	 */
	private void changeOwnership( int objectHandle, Set<Integer> attributes, int federateHandle )
	{
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance == null )
		{
			logger.warn( "Can't change owner of attributes "+acMoniker(attributes)+" to ["+
			             moniker(federateHandle)+"]: object unknown or undiscovered" );
		}
		
		for( Integer attributeHandle : attributes )
		{
			ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			if( attributeInstance != null )
			{
				attributeInstance.setOwner( federateHandle );
			}
			else
			{
				logger.warn( "Can't change owner of attribute "+acMoniker(attributeHandle)+" to ["+
				             moniker(federateHandle)+"]: can't find attribute in ["+
				             objectMoniker(objectHandle)+"]" );
				continue;
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
