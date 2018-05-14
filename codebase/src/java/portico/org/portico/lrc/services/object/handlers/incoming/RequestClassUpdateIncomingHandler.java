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

import org.portico.impl.HLAVersion;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.RequestClassUpdate;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.common.services.object.msg.UpdateAttributes;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="incoming",
                priority=7, // we want to handle it before the callback handler
                messages=RequestClassUpdate.class)
public class RequestClassUpdateIncomingHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int managerClassHandle;
	private int federateClassHandle;

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
		this.federateClassHandle = Mom.getMomObjectClassHandle( HLAVersion.IEEE1516e, "HLAmanager.HLAfederate" );
	}

	public void process( MessageContext context ) throws Exception
	{
		RequestClassUpdate notice = context.getRequest( RequestClassUpdate.class, this );
		int classHandle = notice.getClassHandle();
		Set<Integer> requested = notice.getAttributes();
		int regionToken = notice.getRegionToken();
		
		if( logger.isDebugEnabled() )
		{
			String ddmStatus = notice.usesDDM() ? ", regionToken="+regionToken : "";
			logger.debug( "@REMOTE Request class update: class="+ocMoniker(classHandle)+
			              ", attributes="+acMoniker(requested)+", sourceFederate="+
			              moniker(notice.getSourceFederate()) + ddmStatus );
		}

		// check to see if this is MOM related
		
		if( classHandle == this.managerClassHandle || classHandle == this.federateClassHandle )
		{
			respondToMomFederateUpdateRequest( requested );
			veto("Update was for MOM type, automatically handled by LRC");
		}

		// ignore messages that are from us
		vetoIfMessageFromUs( notice );
		
		// get the region, this will be null if if DDM is not used
		RegionInstance region = regions.getRegion( regionToken );
		
		check( getObjectClass(classHandle), requested, region );
		context.success();
	}

	// need this in a separate method so we can call it recursively
	private void check( OCMetadata type,
	                    Set<Integer> requested,
	                    RegionInstance region ) throws Exception
	{
		// for the class (and each of its children) we have to
		//   -locate all instances of the type
		//   -issue an update request for the object with each of the handles that we own
		processClass( type.getHandle(), requested, region );
		for( OCMetadata child : type.getChildTypes() )
			check( child, requested, region );
	}
	
	private void processClass( int classHandle,
	                           Set<Integer> requested,
	                           RegionInstance region ) throws Exception
	{
		// find all the objects of this class
		Set<OCInstance> objects = repository.getAllInstances( classHandle );
		// for each object, find the set of attributes that are in the requested set AND owned by us
		for( OCInstance object : objects )
		{
			// find the attributes that were both requested and are owned by us
			HashSet<Integer> owned = new HashSet<Integer>();
			for( ACInstance attribute : object.getAllAttributes() )
			{
				// if DDM is used, make sure that the attribute is associated with a region
				// and that the region overlaps with the provided region
				if( region != null )
				{
					RegionInstance attributeRegion = attribute.getRegion();
					if( attributeRegion == null ||
						region.overlapsWith(attributeRegion) == false )
					{
						continue;
					}
				}
				
				// check that we are interested in this attribute for the callback, and if we are
				// is this federate the owner
				if( requested.contains(attribute.getHandle()) &&
				    attribute.getOwner() == lrcState.getFederateHandle() )
				{
					owned.add( attribute.getHandle() );
				}
			}
			
			// if an update is needed, request it
			if( owned.isEmpty() == false )
			{
				if( logger.isDebugEnabled() )
				{
					logger.debug( "Requesting update for attributes "+acMoniker(owned)+
					              " of object ["+objectMoniker(object.getHandle())+"]" );
				}
				
				RequestObjectUpdate request = new RequestObjectUpdate( object.getHandle(), owned );
				reprocessIncoming( fill(request) );
			}
		}
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
			logger.debug( "Sent update for MOM object representing federate ["+moniker()+"]" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}