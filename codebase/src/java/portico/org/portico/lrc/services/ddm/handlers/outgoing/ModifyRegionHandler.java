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
package org.portico.lrc.services.ddm.handlers.outgoing;

import java.util.Map;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JInvalidExtents;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.Dimension;
import org.portico.lrc.model.Extent;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.model.Space;
import org.portico.lrc.services.ddm.msg.ModifyRegion;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516"},
                sinks="outgoing",
                messages=ModifyRegion.class)
public class ModifyRegionHandler extends LRCMessageHandler
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
		ModifyRegion request = context.getRequest( ModifyRegion.class, this );
		RegionInstance region = request.getRegion();
		int regionToken = region.getToken();
		int federate = lrcState.getFederateHandle();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		// log the request, complete with detailed information
		if( logger.isDebugEnabled() )
			logRegionRequest( region );

		// validate that the region exists and we are the creator
		RegionInstance regionOnFile = regions.getRegion( regionToken );
		if( regionOnFile == null )
		{
			throw new JRegionNotKnown( "token: " + regionToken );
		}
		else if( regionOnFile.getFederateHandle() != federate )
		{
			throw new JRegionNotKnown( "federate ["+moniker()+"] didn't create region ["+
			                           regionToken+"]" );
		}
		
		// find the space -- shouldn't need to check this, if we already have the region
		// the space handle will have been checked on creation
		Space space = fom().getSpace( regionOnFile.getSpaceHandle() );

		//////////////////////////
		// validate the extents //
		//////////////////////////
		for( int i = 0; i < region.getNumberOfExtents(); i++ )
		{
			for( Dimension dimension : space.getDimensions() )
			{
				long lowerBound = region.getRangeLowerBound( i, dimension.getHandle() );
				long upperBound = region.getRangeUpperBound( i, dimension.getHandle() );
				
				// check that the bounds don't go below or above the max/min values
				if( lowerBound < PorticoConstants.MIN_EXTENT )
				{
					throw new JInvalidExtents( "lower bound outside valid range (min:" + 
					                           PorticoConstants.MIN_EXTENT + ",value:" +
					                           lowerBound + ")" );
				}
				else if( upperBound > PorticoConstants.MAX_EXTENT )
				{
					throw new JInvalidExtents( "upper bound outside valid range (max:" + 
					                           PorticoConstants.MAX_EXTENT + ",value:" +
					                           upperBound + ")" );
				}
				
				// check that the lowerBound is less than the upperBound
				if( lowerBound > upperBound )
				{
					// invalid extents
					throw new JInvalidExtents( "lower bound is less than upper bound" );
				}
			}
		}

		// copy the values into the existing region. we can't just clone it and store the copy
		// because various interest management data structures will be holding references to the
		// original instance.
		regionOnFile.copy( region );
		
		// broacast out the change to the federation
		connection.broadcast( request );
		
		// replace the region in the request with a clone so that future
		// changes to the given instance don't affect the internal RTI
		// state (this is mainly a problem in the JVM bindings)
		RegionInstance regionClone = region.clone();
		request.setRegion( regionClone );

		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS Modified Region: token=" + regionToken );
		context.success();
	}

	/**
	 * This method will log (at the DEBUG level) all the data associated with the given region.
	 * At logging this information is especially verbose in a code-sense, I've split it off into
	 * this method.
	 */
	private void logRegionRequest( RegionInstance region ) throws Exception
	{
		// log the basic request
		logger.debug( "ATTEMPT Modify Region: token=" + region.getToken() +
		              ", extents=" + region.getSize() );
		
		// log the ranges for each of the extents
		for( int i = 0; i < region.getSize(); i++ )
		{
			// get each of the ranges
			Map<Integer,Extent.Range> ranges = region.getExtent(i).getAllRanges();
			// log the ranges
			logger.trace( "        extents[" + i + "]: " );
			for( Integer dimensionHandle : ranges.keySet() )
			{
				logger.trace( "          (range) dimension=" + dimensionHandle +
				              ", lower=" + ranges.get(dimensionHandle).lowerBound +
				              ", upper=" + ranges.get(dimensionHandle).upperBound );
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
