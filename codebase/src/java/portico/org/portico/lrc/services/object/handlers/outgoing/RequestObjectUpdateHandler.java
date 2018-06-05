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
package org.portico.lrc.services.object.handlers.outgoing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.impl.HLAVersion;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.common.services.object.msg.UpdateAttributes;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=RequestObjectUpdate.class)
public class RequestObjectUpdateHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int momFederationHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.momFederationHandle = Mom.getMomObjectClassHandle( HLAVersion.HLA13, "Manager.Federation" );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		RequestObjectUpdate request = context.getRequest( RequestObjectUpdate.class, this );

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		int objectHandle = request.getObjectId();
		Set<Integer> attributeHandles = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Request update of object ["+objectMoniker(objectHandle)+
			              "] for attributes="+acMoniker(attributeHandles) );
		}
		
		// make sure we have discovered the object
		OCInstance instance = repository.getInstance( objectHandle );
		if( instance == null )
			throw new JObjectNotKnown( "can't request update, unknown object: handle="+objectHandle );
		
		// we only want to request updates for those attributes that we don't own.
		HashSet<Integer> nonOwnedAttributes = new HashSet<Integer>();
		for( Integer attributeHandle : attributeHandles )
		{
			// get the attribute
			ACInstance attribute = instance.getAttribute( attributeHandle );
			if( attribute == null )
			{
				throw new JAttributeNotDefined( "attribute: " +attributeHandle+
				                                " in instance: " + objectHandle );
			}
			
			// if we are the owner, remove the handle from the request
			if( attribute.getOwner() != lrcState.getFederateHandle() )
				nonOwnedAttributes.add( attributeHandle );
		}
		
		// reassign the set of nonOwnedAttributes as the set we are interested in
		request.setAttributes( nonOwnedAttributes );
		
		// if this is for the MOM federation object, just handle it locally
		if( objectHandle == this.momFederationHandle )
		{
			respondToMomFederationUpdateRequest( request.getAttributes() );
		}
		else
		{
			// broadcast the request out
			connection.broadcast( request );
		}
		
		context.success();
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Requested update of object ["+objectMoniker(objectHandle)+
			              "] for attributes="+acMoniker(attributeHandles) );
		}
	}

	/**
	 * This method handles requested updates for the MOM object representing the federation. An
	 * update message will be generated and placed on the local federate's message queue. These
	 * messages are entirely for local consumption, hence why we place them on the local queue
	 * and don't broadcast them out. Other federates will generate their own on request.
	 */
	private void respondToMomFederationUpdateRequest( Set<Integer> attributeHandles ) throws Exception
	{
		UpdateAttributes update = momManager.updateFederationMomObject( attributeHandles );
		lrcState.getQueue().offer( update );
		if( logger.isDebugEnabled() )
			logger.debug( "Sent update for MOM object representing federate ["+moniker()+"]" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
