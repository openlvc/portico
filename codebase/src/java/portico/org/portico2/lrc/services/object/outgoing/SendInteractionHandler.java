/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.lrc.services.object.outgoing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JInteractionClassNotDefined;
import org.portico.lrc.compat.JInteractionClassNotPublished;
import org.portico.lrc.compat.JInteractionParameterNotDefined;
import org.portico.lrc.compat.JInvalidRegionContext;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.model.Space;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.lrc.LRCMessageHandler;

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
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
	}

	@Override
	public void process( MessageContext context ) throws JException
	{
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		SendInteraction request = context.getRequest( SendInteraction.class, this );
		int classHandle = request.getInteractionId();
		HashMap<Integer,byte[]> parameters = request.getParameters();
		int regionToken = request.getRegionToken(); // optional:default PorticoConstants.NULL_HANDLE

		if( logger.isDebugEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			String ddmStatus = request.usesDDM() ? ", regionToken="+regionToken : "";
			logger.debug( "ATTEMPT Send interaction [%s] with parameters %s%s%s",
			              icMoniker(classHandle),
			              pcMoniker(parameters.keySet()),
			              ddmStatus,
			              timeStatus );
		}

		// if this is a TSO message, check the time
		if( request.isTimestamped() )
			lrcState.checkValidTime( request.getTimestamp() );
		
		// make sure the federate publishes the interaction class
		ICMetadata interactionClass = validatePublication( request.getSourceFederate(),
		                                                   classHandle,
		                                                   parameters.keySet() );
		
		// validate region data if required
		if( request.usesDDM() )
			validateRegion( interactionClass, regionToken );
		
		// everything is OK here, broadcast out the update
		connection.sendDataMessage( request );
		context.success();

		if( logger.isInfoEnabled() )
		{
			String timeStatus = request.isTimestamped() ? " @"+request.getTimestamp() : " (RO)";
			String ddmStatus = request.usesDDM() ? ", regionToken="+regionToken : "";
			logger.info( "SUCCESS Send interaction [%s] with parameters %s%s%s",
			             icMoniker(classHandle),
			             pcMoniker(parameters.keySet()),
			             ddmStatus,
			             timeStatus );
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
		RegionInstance region = regionStore.getRegion( regionToken );
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
