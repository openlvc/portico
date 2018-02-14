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
package org.portico.lrc.services.pubsub.handlers.outgoing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JOwnershipAcquisitionPending;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.pubsub.msg.UnpublishObjectClass;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=UnpublishObjectClass.class)
public class UnpublishObjectClassHandler extends LRCMessageHandler
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

		UnpublishObjectClass request = context.getRequest( UnpublishObjectClass.class, this );
		int classHandle = request.getClassHandle();

		if( logger.isDebugEnabled() )
			logger.debug( "ATTEMPT Unpublish object class ["+ocMoniker(classHandle)+"]" );
		
		// make sure that we don't have any outstanding ownership acquisition requests
		checkOwnershipAcquisitions( classHandle );
		
		// store the interest information
		interests.unpublishObjectClass( request.getSourceFederate(), classHandle );
		// forward the information to the rest of the federation in case they want it
		connection.broadcast( request );

		// release any attributes we now can no longer control
		releaseAttributes( federateHandle(), classHandle );
		
		context.success();

		if( logger.isInfoEnabled() )
			logger.info( "SUCCESS Unublished object class ["+ocMoniker(classHandle)+"]" );
	}

	/**
	 * This method checks all outstanding ownership acquisition requests for this class to make
	 * sure that there are none that would be made invalid by an unpublish call. If there are,
	 * an exception is thrown. 
	 */
	private void checkOwnershipAcquisitions( int classHandle ) throws JOwnershipAcquisitionPending
	{
		for( OCInstance instance : repository.getAllInstances() )
		{
			if( instance.getDiscoveredClassHandle() != classHandle )
				continue;
			
			Set<Integer> set = ownership.getAttributesUnderAcquisitionRequest( instance.getHandle(),
			                                                                   federateHandle() );
			if( set.isEmpty() == false )
			{
				throw new JOwnershipAcquisitionPending( "pending acquisition requests for "+
				                                        acMoniker(set)+", in object ["+
				                                        ocMoniker(instance.getHandle())+"]" );
			}
		}
	}

	/**
	 * Release all the attributes that we did control but can no longer control due to the
	 * unpublication.
	 */
	private void releaseAttributes( int federate, int classHandle )
	{
		for( OCInstance objectInstance : repository.getAllInstances(classHandle) )
		{
			Set<Integer> released = new HashSet<Integer>();
			for( ACInstance attributeInstance : objectInstance.getAllOwnedAttributes(federate) )
			{
				released.add( attributeInstance.getHandle() );
			}
			
			// spit out an unconditional divest notification for any attributes that were released
			AttributeDivest release = new AttributeDivest( objectInstance.getHandle(),
			                                               released,
			                                               true );
			reprocessIncoming( release );
			
			// log a helpful message
			if( logger.isDebugEnabled() )
			{
				logger.debug( "Releasing attributes ["+acMoniker(released)+"] of object ["+
				              objectMoniker(objectInstance.getHandle())+
				              "] after unpublish of class ["+ocMoniker(classHandle)+
				              "] by federate ["+moniker(federate)+"]" );
			}
		}
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
