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
package org.portico.lrc.services.object.handlers.incoming;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.common.services.object.msg.UpdateAttributes;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=RequestObjectUpdate.class)
public class RequestObjectUpdateIncomingHandler extends LRCMessageHandler
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
		RequestObjectUpdate notice = context.getRequest( RequestObjectUpdate.class, this );
		int objectHandle = notice.getObjectId();
		Set<Integer> attributeHandles = notice.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "@REMOTE Request object update: object="+objectMoniker(objectHandle)+
			              ", attributes="+acMoniker(attributeHandles)+", sourceFederate="+
			              moniker(notice.getSourceFederate()) );
		}

		// check to see if the request is for our mom-federate object
		if( objectHandle == lrcState.getMomFederateObjectHandle(federateHandle()) )
		{
			// it is, fill out this update ourselves
			respondToMomFederateUpdateRequest( attributeHandles );
			veto("Update was for MOM type, automatically handled by LRC");
		}
		
		// find our local copy of the object
		OCInstance instance = repository.getInstance( objectHandle );
		if( instance == null )
		{
			// we don't know the instance, don't do anything
			logger.debug( "Object not known ["+objectHandle+"], we can't provide any update" );
			veto("object handle not known");
		}
		
		// checks to see if there are any attributes that we own, if there are we
		// need to issue an update request to the federate ambassador
		HashSet<Integer> owned = filterOwnedAttributes( instance, attributeHandles );
		// if we don't own any attributes, there is no need for an update
		if( owned.isEmpty() )
			veto("don't own any attributes");
		
		// update the set of attributes that should be part of the request to be only those
		// that we own, then let it flow through so that the callback handler can run
		notice.setAttributes( owned );
		
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Requesting update of attributes "+acMoniker(owned)+
			              ", object ["+objectMoniker(objectHandle)+"]" );
		}
		
		context.success();
	}
	
	/**
	 * Return the set of attributes in the {@link OCInstance} that are owned by the local federate
	 * *AND* that are in the given requested set.
	 */
	private HashSet<Integer> filterOwnedAttributes( OCInstance theObject, Set<Integer> requested )
	{
		int federateHandle = lrcState.getFederateHandle();
		HashSet<Integer> owned = new HashSet<Integer>();
		for( Integer attributeHandle : requested )
		{
			if( theObject.getAttribute(attributeHandle).getOwner() == federateHandle )
				owned.add( attributeHandle );
		}
		
		return owned;
	}
	
	/**
	 * This method handles requested updates for MOM objects by broadcasting out an update for
	 * the provided attributes. This method should only be called if the request is for the local
	 * federate.
	 */
	private void respondToMomFederateUpdateRequest( Set<Integer> attributeHandles ) throws Exception
	{
		if( PorticoConstants.isMomEnabled() == false )
			return;

		UpdateAttributes update = momManager.updateFederateMomObject( federateHandle(),
		                                                              attributeHandles );
		connection.broadcast( update );
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Sent update to MOM object representing federate ["+moniker()+"]" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}