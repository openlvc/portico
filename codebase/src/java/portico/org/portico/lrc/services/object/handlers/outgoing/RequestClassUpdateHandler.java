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

import java.util.Map;
import java.util.Set;

import org.portico.impl.HLAVersion;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCMetadata;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.RequestClassUpdate;
import org.portico2.common.services.object.msg.UpdateAttributes;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=RequestClassUpdate.class)
public class RequestClassUpdateHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int managerClassHandle;
	private int federationClassHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		this.managerClassHandle = Mom.getMomObjectClassHandle( HLAVersion.IEEE1516e, "HLAmanager" );
		this.federationClassHandle = Mom.getMomObjectClassHandle( HLAVersion.IEEE1516e, "HLAmanager.HLAfederation" );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		RequestClassUpdate request = context.getRequest( RequestClassUpdate.class, this );

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		int classHandle = request.getClassHandle();
		Set<Integer> attributes = request.getAttributes();
		int regionToken = request.getRegionToken();
		
		if( logger.isDebugEnabled() )
		{
			String ddmStatus = request.usesDDM() ? ", regionToken="+regionToken : "";
			logger.debug( "ATTEMPT Request class update ["+ocMoniker(classHandle)+
			              "] for attributes="+acMoniker(attributes) + ddmStatus );
		}
		
		// make sure we can find the class
		OCMetadata metadata = getObjectClass( classHandle );
		if( metadata == null )
			throw new JObjectClassNotDefined( "can't request update, unknown class: handle="+classHandle );

		// make sure each of the attributes is valid
		for( Integer attributeHandle : attributes )
		{
			if( metadata.hasAttribute(attributeHandle) == false )
			{
				throw new JAttributeNotDefined( "attribute ["+acMoniker(attributeHandle)+
				                                "] not defined in class ["+ocMoniker(classHandle)+"]" );
			}
		}
		
		// validate that the region exists
		if( request.usesDDM() && regions.getRegion(regionToken) == null )
			throw new JRegionNotKnown( "token: " + regionToken );
		
		// there are a couple of possible combinations of actions here:
		//  1. Class handle is MOM Federation object
		//      -Just handle it locally, don't broadcast it, everyone handles this themselves
		//  2. Class handle is MOM Manager object
		//      -Federation and Federate are both Manager subclasses, so we want to handle the
		//       federation part locally and broadcast for the Federate parts
		//  3. Class handle isn't part of the MOM or if for Federate
		//      -We want to broadcast it. If it's non-mom, we're fine, if it's Mom-Federate, then
		//       we need to notify the appropriate LRC
		if( classHandle == this.federationClassHandle )
		{
			// it's just Manager.Federation, handle locally, no broadcast
			respondToMomFederationUpdateRequest( request.getAttributes() );
		}
		else
		{
			// if it's for manager, handle the federation part locally and broadcast as per
			// normal for the federation part
			if( classHandle == this.managerClassHandle )
				respondToMomFederationUpdateRequest( request.getAttributes() );
			
			connection.broadcast( request );
		}

		context.success();
		if( logger.isInfoEnabled() )
		{
			String ddmStatus = request.usesDDM() ? ", regionToken="+regionToken : "";
			logger.info( "SUCCESS Request class update ["+ocMoniker(classHandle)+
			             "] for attributes="+acMoniker(attributes) + ddmStatus );
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
