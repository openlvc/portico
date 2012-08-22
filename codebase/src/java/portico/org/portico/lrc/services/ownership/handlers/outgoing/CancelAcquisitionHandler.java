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
package org.portico.lrc.services.ownership.handlers.outgoing;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JAttributeAcquisitionWasNotRequested;
import org.portico.lrc.compat.JAttributeAlreadyOwned;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.CancelAcquire;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=CancelAcquire.class)
public class CancelAcquisitionHandler extends LRCMessageHandler
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
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		CancelAcquire request = context.getRequest( CancelAcquire.class, this );
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Cancel ownership acquisition for attributes "+
			              acMoniker(attributes)+" of object ["+objectMoniker(objectHandle)+"]" );
		}
		
		// validate the request
		validate( objectHandle, attributes );
		
		// there might already be some ownership acquired responses on the way. in this case
		// we will have to throw an exception, so we can't return right away, we have to wait
		// a reasonable time for these responses to sift in
		// broadcast our request cancellation and wait for a while to let them come in
		if( logger.isTraceEnabled() )
		{
			logger.trace( "Broadcasting cancellation request and waiting for any "+
			              "in-transit acquisition notifications" );
		}
		
		connection.broadcastAndSleep( request );
		
		// that should have given long enough for our cancel notification to reach any federates
		// that own the attributes we've requested (and thus stopped them releasing attributes to
		// us) and for any in-transit notifications to reach us. Check to see if we got ownership
		// of any attributes in the mean time
		validate( objectHandle, attributes ); // validate again, object could have been deleted!
		
		// note that the ownership cancellation callbacks will be delivered to the federates
		// just as soon as that notification is received from the current owner
		context.success();
	}
	
	/**
	 * Check the object and each of the attributes to make sure that a) the object exists, b) the
	 * attributes exist, c) the attributes are not already owned by the local federate and d) the
	 * attributes are part of an outstanding attribute ownership acquisition request.
	 */
	private void validate( int objectHandle, Set<Integer> attributes )
		throws JObjectNotKnown,
		       JAttributeNotDefined,
		       JAttributeAlreadyOwned,
		       JAttributeAcquisitionWasNotRequested
	{
		// validate that the object exists
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance == null )
		{
			throw new JObjectNotKnown( "can't cancel acquisition for attributes of object ["+
			                           objectMoniker(objectHandle)+"]: unknown or undiscovered" );
		}
		
		// validate that the attributes exist, are not owned and that we have requested them
		for( Integer attributeHandle : attributes )
		{
			ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			if( attributeInstance == null )
			{
				throw new JAttributeNotDefined( "can't cancel acquisition for attribute ["+
				                                attributeHandle+"] of object ["+
				                                objectMoniker(objectHandle)+"]: doesn't exist" );
			}
			
			if( attributeInstance.isOwnedBy(federateHandle()) )
			{
				throw new JAttributeAlreadyOwned( "can't cancel acquisition for attribute "+
				                                  acMoniker(attributeHandle)+" of object ["+
				                                  objectMoniker(objectHandle)+"]: already owned" );
			}
			
			if(ownership.isAttributeUnderAcquisitionRequest(objectHandle,attributeHandle) == false)
			{
				throw new JAttributeAcquisitionWasNotRequested(
				    "can't cancel acquisition for attribute "+acMoniker(attributeHandle)+
				    " of object ["+objectMoniker(objectHandle)+"]: not under acquisition request" );
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
