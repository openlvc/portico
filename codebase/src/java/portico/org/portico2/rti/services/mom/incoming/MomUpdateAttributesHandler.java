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
package org.portico2.rti.services.mom.incoming;

import java.util.Set;

import org.portico.lrc.compat.JException;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.rti.RtiConnection;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.object.data.ROCInstance;

/**
 * This handler receives all {@link UpdateAttributes} messages reflected into the Federation's internal
 * message sink when {@link Federation#queueDataMessage(PorticoMessage, RtiConnection)} is called.
 * <p/>
 * Metrics are logged against the sender that is updating the object instance, and all federates 
 * that will receive the corresponding reflection. 
 */
public class MomUpdateAttributesHandler extends RTIMessageHandler
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
	public void process( MessageContext context ) throws JException
	{
		ObjectModel objectModel = federation.getFOM();
		UpdateAttributes request = context.getRequest( UpdateAttributes.class );
		
		// Log update metrics
		int sendingFederate = request.getSourceFederate();
		int objectHandle = request.getObjectId();
		ROCInstance instance = repository.getObject( objectHandle );
		momManager.objectUpdated( sendingFederate, instance );
		
		// Log reflect metrics
		InterestManager interests = federation.getInterestManager();
		Set<Integer> subscribers = interests.getAllSubscribers( instance.getRegisteredType() );
		for( int subscriber : subscribers )
		{
			if( subscriber == sendingFederate )
				continue;
			
			momManager.objectReflected( subscriber, instance );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
