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
import org.portico.lrc.compat.JAttributeDivestitureWasNotRequested;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotOwned;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.CancelDivest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=CancelDivest.class)
public class CancelDivestHandler extends LRCMessageHandler
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

		CancelDivest request = context.getRequest( CancelDivest.class, this );
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Cancel ownership divestiture for attributes "+
			              acMoniker(attributes)+" of object ["+objectMoniker(objectHandle)+"]" );
		}
		
		// validate the request
		validate( objectHandle, attributes );
		
		// expunge the divest notification from the record
		ownership.cancelDivest( objectHandle, attributes );
		
		// there is nothing else to do. no cancellation notifications are sent out.
		// if a federate has seen the offer of ownership relating to the initial divest and then
		// gone and requested ownership, that request is now treated like a normal pull-style
		// acquisition request that would have happened in the absence of the initial divest.
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Cancelled ownership divestiture for attributes "+
			             acMoniker(attributes)+" of object ["+objectMoniker(objectHandle)+"]" );
		}

		context.success();
	}

	/**
	 * Check the object and each of the attributes to make sure that a) the object exists, b) the
	 * attributes exist, c) the attributes are owned by the local federate and d) the attributes
	 * are part of an outstanding attribute ownership divest request.
	 */
	private void validate( int objectHandle, Set<Integer> attributes )
		throws JObjectNotKnown,
		       JAttributeNotDefined,
		       JAttributeNotOwned,
		       JAttributeDivestitureWasNotRequested
	{
		// validate that the object exists
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance == null )
		{
			throw new JObjectNotKnown( "can't cancel divest for attributes of object ["+
			                           objectMoniker(objectHandle)+"]: unknown or undiscovered" );
		}
		
		// validate that the attributes exist, are not owned and that we have requested them
		for( Integer attributeHandle : attributes )
		{
			ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			if( attributeInstance == null )
			{
				throw new JAttributeNotDefined( "can't cancel divest for attribute ["+
				                                attributeHandle+"] of object ["+
				                                objectMoniker(objectHandle)+"]: doesn't exist" );
			}
			
			if( attributeInstance.isOwnedBy(federateHandle()) == false )
			{
				throw new JAttributeNotOwned( "can't cancel acquisition for attribute "+
				                              acMoniker(attributeHandle)+" of object ["+
				                              objectMoniker(objectHandle)+"]: not owned" );
			}
			
			if( ownership.isAttributeUnderDivestRequest(objectHandle,attributeHandle) == false )
			{
				throw new JAttributeDivestitureWasNotRequested(
				    "can't cancel divestiture for attribute "+acMoniker(attributeHandle)+
				    " of object ["+objectMoniker(objectHandle)+"]: not under divest request" );
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
