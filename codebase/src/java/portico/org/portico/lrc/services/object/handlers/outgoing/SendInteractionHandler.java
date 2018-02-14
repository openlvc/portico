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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JInteractionClassNotDefined;
import org.portico.lrc.compat.JInteractionClassNotPublished;
import org.portico.lrc.compat.JInteractionParameterNotDefined;
import org.portico.lrc.compat.JInvalidRegionContext;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.model.Space;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.SendInteraction;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=SendInteraction.class)
public class SendInteractionHandler extends LRCMessageHandler
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
		SendInteraction request = context.getRequest( SendInteraction.class, this );
		int classHandle = request.getInteractionId();
		HashMap<Integer,byte[]> parameters = request.getParameters();
		int regionToken = request.getRegionToken(); // optional:default PorticoConstants.NULL_HANDLE

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		if( logger.isDebugEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			String ddmStatus = request.usesDDM() ? ", regionToken="+regionToken : "";
			logger.debug( "ATTEMPT Send interaction ["+icMoniker(classHandle)+"] with parameters "+
			              pcMoniker(parameters.keySet()) + ddmStatus + timeStatus );
		}

		// if this is a TSO message, check the time
		if( request.isTimestamped() )
			lrcState.checkValidTime( request.getTimestamp() );
		
		// make sure the federate publishes the interaction class
		ICMetadata interactionClass =
			validatePublication( request.getSourceFederate(), classHandle, parameters.keySet() );
		
		// validate region data if required
		if( request.usesDDM() )
			validateRegion( interactionClass, regionToken );
		
		// everything is OK here, broadcast out the update
		connection.broadcast( request );
		context.success();
		
		if( logger.isInfoEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			String ddmStatus = request.usesDDM() ? ", regionToken="+regionToken : "";
			logger.info( "SUCCESS Sent interaction ["+icMoniker(classHandle)+"] with parameters "+
			             pcMoniker(parameters.keySet()) + ddmStatus + timeStatus );
		}
	}
	
	/**
	 * Validate that the interaction class and each of the parameters exists in the FOM and that
	 * the identified federate publishes the interaction class. If any of these is not true, throw
	 * the appriorate exception, otherwise, return the metadata of the interaction.
	 */
	private ICMetadata validatePublication( int federateHandle,
	                                        int classHandle,
	                                        Set<Integer> parameters )
		throws JInteractionClassNotDefined,
		       JInteractionClassNotPublished,
		       JInteractionParameterNotDefined
	{
		// find the interaction class
		ICMetadata interactionClass = lrcState.getFOM().getInteractionClass( classHandle );
		if( interactionClass == null )
		{
			throw new JInteractionClassNotDefined( "interaction class handle: " + classHandle );
		}
		
		// check that we are publishing the class
		if( interests.isInteractionClassPublished(federateHandle,classHandle) == false )
		{
			throw new JInteractionClassNotPublished( "not published: " + icMoniker(classHandle) );
		}
		
		// check that all the parameters exist
		for( int parameterHandle : parameters )
		{
			if( interactionClass.getParameter(parameterHandle) == null )
			{
				throw new JInteractionParameterNotDefined( "parameter ["+parameterHandle+
				                                           "] not defined in ["+
				                                           icMoniker(classHandle)+"]" );
			}
		}
		
		return interactionClass;
	}

	/**
	 * Validate that the region is known to the federate, and that it is valid for the class
	 * of interaction that is being sent. If all is OK, this method will return without incident,
	 * if not, an exception will be thrown.
	 * 
	 * @throws JRegionNotKnown If the regionToken doesn't correspond to a region that was created
	 *                         by the federate and is known to it.
	 * @throws JInvalidRegionContext If the space associated with the region is different to the
	 *                               space that the FOM associates with the interaction class (or
	 *                               there is no space associated with the interaction class in
	 *                               the FOM).
	 */
	private void validateRegion( ICMetadata interactionClass, int regionToken )
		throws JRegionNotKnown, JInvalidRegionContext
	{
		// find the region instance
		RegionInstance region = regions.getRegion( regionToken );
		if( region == null )
			throw new JRegionNotKnown( "Can't find region: token=" + regionToken );
		
		// make sure we created this region
		if( region.getFederateHandle() != lrcState.getFederateHandle() )
			throw new JRegionNotKnown( "Region not created by this federate: token="+regionToken );
		
		// validate that the space associated with the region is the same
		// as that associated with the interaction class in the FOM
		Space space = interactionClass.getSpace();
		if( space == null || space.getHandle() != region.getSpaceHandle() )
		{
			throw new JInvalidRegionContext( "The routing space for the region is different from " +
			               "the routing space associated with the interaction class in the FOM" );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
