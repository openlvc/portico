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
package org.portico2.rti.services.pubsub.incoming;

import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JOwnershipAcquisitionPending;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.pubsub.msg.UnpublishObjectClass;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.ROCInstance;

public class UnpublishObjectClassHandler extends RTIMessageHandler
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
		UnpublishObjectClass request = context.getRequest( UnpublishObjectClass.class, this );
		int federateHandle = request.getSourceFederate();
		int classHandle = request.getClassHandle();
		Set<Integer> attributes = request.getAttributes();

		if( logger.isDebugEnabled() )
		{
			logger.debug( "ATTEMPT Federate [%s] unpublishing [%s] with attributes %s",
			              federateName(federateHandle),
			              ocMoniker(classHandle),
			              acMoniker(request.getAttributes()) );
		}

		// make sure that we don't have any outstanding ownership acquisition requests
		checkOwnershipAcquisitions( federateHandle, classHandle );
		
		// store the interest information
		interests.unpublishObjectClass( federateHandle, classHandle, attributes );

		// release any attributes we now can no longer control
		releaseAttributes( federateHandle, classHandle );
		
		context.success();
		
		if( logger.isInfoEnabled() )
		{
			logger.info( "SUCCESS Federate [%s] unpublished  [%s] with attributes %s",
			             federateName(federateHandle),
			             ocMoniker(classHandle),
			             acMoniker(request.getAttributes()) );
		}
	}

	/**
	 * This method checks all outstanding ownership acquisition requests for this class to make
	 * sure that there are none that would be made invalid by an unpublish call. If there are,
	 * an exception is thrown. 
	 */
	private void checkOwnershipAcquisitions( int federateHandle, int classHandle )
		throws JOwnershipAcquisitionPending
	{
		for( ROCInstance instance : repository.getAllInstances() )
		{
			// FIXME I just changed this from getDiscoveredClassHandle() to get*REGISTERED*...
			//       as part of the split of OCInstance into ROCInstance and LOCInstance.
			//       Need to revisit this particular ownership edge case to ensure it still
			//       makes sense.
			if( instance.getRegisteredClassHandle() != classHandle )
				continue;
			
			Set<Integer> set = ownership.getAttributesUnderAcquisitionRequest( instance.getHandle(),
			                                                                   federateHandle );
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
// TODO Complete this
//		for( OCInstance objectInstance : repository.getAllInstances(classHandle) )
//		{
//			Set<Integer> released = new HashSet<Integer>();
//			for( ACInstance attributeInstance : objectInstance.getAllOwnedAttributes(federate) )
//			{
//				released.add( attributeInstance.getHandle() );
//			}
//			
//			// spit out an unconditional divest notification for any attributes that were released
//			AttributeDivest release = new AttributeDivest( objectInstance.getHandle(),
//			                                               released,
//			                                               true );
//			reprocessIncoming( release );
//			
//			// log a helpful message
//			if( logger.isDebugEnabled() )
//			{
//				logger.debug( "Releasing attributes ["+acMoniker(released)+"] of object ["+
//				              objectMoniker(objectInstance.getHandle())+
//				              "] after unpublish of class ["+ocMoniker(classHandle)+
//				              "] by federate ["+moniker(federate)+"]" );
//			}
//		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
