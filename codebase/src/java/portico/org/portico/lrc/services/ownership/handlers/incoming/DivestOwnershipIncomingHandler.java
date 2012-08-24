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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

/**
 * Handles incoming notifications of the desire to divest a set of attributes. This handler will
 * record the request and then will check to see if the local federate can take up ownership of
 * any of the attributes. If it can, those attributes replace the ones of the request and the
 * message is left to flow through to any callback handler. If there are none that the local
 * federate can take owneship of, the request is recorded and veto'd.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7,
                messages=AttributeDivest.class)
public class DivestOwnershipIncomingHandler extends LRCMessageHandler
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
		AttributeDivest divest = context.getRequest( AttributeDivest.class, this );
		vetoIfMessageFromUs( divest );
		int sourceFederate = divest.getSourceFederate();
		int objectHandle = divest.getObjectHandle();
		Set<Integer> attributes = divest.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			String unconditional = divest.isUnconditional() ? " (unconditional)" : "";
			logger.debug( "@REMOTE Federate ["+moniker(sourceFederate)+"] is divesting attributes "+
			              acMoniker(attributes)+" of object ["+objectMoniker(objectHandle)+"]"+
			              unconditional );
		}
		
		// if this is an UNCONDITIONAL divest, just set the ownership of all attributes to
		// nobody and get out of here
		if( divest.isUnconditional() )
		{
			//////////////////////////
			// UNCONDITIONAL DIVEST //
			//////////////////////////
			OCInstance objectInstance = repository.getDiscoveredOrUndiscovered( objectHandle );
			if( objectInstance != null )
			{
				for( Integer attribute : attributes )
					objectInstance.getAttribute(attribute).unown();
			}
			
			// find out if any of these are suitable for acquisition by the local federate
			// if any are, replace the existing set with those that are and let it flow through
			// to the callback handler
			Set<Integer> suitable = getSuitableFreeAttributes( objectInstance, attributes );
			if( suitable.isEmpty() )
			{
				// none we can take up
				logger.debug( "Can't take ownership of any attributes offered to us, ignore request" );
				veto();
			}
			
			divest.setAttributes( suitable );
		}
		else
		{
			////////////////////////////
			// NEGOTIATED DIVESTITURE //
			////////////////////////////
			// record the divest notification
			ownership.requestDivestiture( objectHandle, attributes, sourceFederate );
		
			// check to see if we can take ownership of any of these attributes
			OCInstance objectInstance = repository.getInstance( objectHandle );
			Set<Integer> suitable = getSuitableFreeAttributes( objectInstance, attributes );
			if( suitable.isEmpty() )
			{
				logger.debug( "Can't take ownership of any attributes offered to us, ignore request" );
				veto();
			}

			// replace the attributes in the request with the set of those we can take
			// ownership of and let the notice flow through for callback
			divest.setAttributes( suitable );
		}
	}
	
	/**
	 * This method will go through the released attributes and find all those that are able to
	 * be taken up by the local federate. If there are none, an empty set will be returned.
	 */
	private Set<Integer> getSuitableFreeAttributes( OCInstance theObject,
	                                                Set<Integer> releasedAttributes )
	{
		Set<Integer> suitable = new HashSet<Integer>();
		int classHandle = theObject.getDiscoveredClassHandle();
		for( Integer attributeHandle : releasedAttributes )
		{
			if( interests.isAttributeClassPublished(federateHandle(),classHandle,attributeHandle) )
				suitable.add( attributeHandle );
		}
		
		return suitable;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
