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
package org.portico2.rti.services;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.IMessageHandler;
import org.portico2.common.messaging.MessageSink;
import org.portico2.common.messaging.MessageType;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.federation.incoming.JoinFederationHandler;
import org.portico2.rti.services.federation.incoming.ResignFederationHandler;
import org.portico2.rti.services.mom.incoming.MomSendInteractionHandler;
import org.portico2.rti.services.mom.incoming.MomUpdateAttributesHandler;
import org.portico2.rti.services.object.incoming.DeleteObjectHandler;
import org.portico2.rti.services.object.incoming.RegisterObjectHandler;
import org.portico2.rti.services.object.incoming.RequestClassUpdateHandler;
import org.portico2.rti.services.object.incoming.RequestObjectUpdateHandler;
import org.portico2.rti.services.object.incoming.ReserveObjectNameHandler;
import org.portico2.rti.services.pubsub.incoming.PublishInteractionClassHandler;
import org.portico2.rti.services.pubsub.incoming.PublishObjectClassHandler;
import org.portico2.rti.services.pubsub.incoming.SubscribeInteractionClassHandler;
import org.portico2.rti.services.pubsub.incoming.SubscribeObjectClassHandler;
import org.portico2.rti.services.pubsub.incoming.UnpublishInteractionClassHandler;
import org.portico2.rti.services.pubsub.incoming.UnpublishObjectClassHandler;
import org.portico2.rti.services.pubsub.incoming.UnsubscribeInteractionClassHandler;
import org.portico2.rti.services.pubsub.incoming.UnsubscribeObjectClassHandler;
import org.portico2.rti.services.sync.incoming.RegisterSyncPointHandler;
import org.portico2.rti.services.sync.incoming.AchieveSyncPointHandler;
import org.portico2.rti.services.time.incoming.DisableTimeConstrainedHandler;
import org.portico2.rti.services.time.incoming.DisableTimeRegulationHandler;
import org.portico2.rti.services.time.incoming.EnableAsyncDeliveryHandler;
import org.portico2.rti.services.time.incoming.EnableTimeConstrainedHandler;
import org.portico2.rti.services.time.incoming.EnableTimeRegulationHandler;
import org.portico2.rti.services.time.incoming.ModifyLookaheadHandler;
import org.portico2.rti.services.time.incoming.TimeAdvanceRequestHandler;

public class RTIHandlerRegistry
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
	private static void loadHla13( Federation federation )
	{
		
	}
	
	private static void loadIeee1516( Federation federation )
	{
		
	}
	
	private static void loadIeee1516e( Federation federation )
	{
		// object map for configuration of handlers - stuffed with a reference to the LRC
		Map<String,Object> settings = new HashMap<>();
		settings.put( IMessageHandler.KEY_RTI_FEDERATION, federation );
		
		///////////////////////////////////////////////////////////////////////////
		///  Outgoing Handlers   //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		//MessageSink out = federation.getOutgoingSink();
		
		// Configure all handlers in the sink
		//out.configure( settings );
		
		///////////////////////////////////////////////////////////////////////////
		///  Incoming Handlers   //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		MessageSink in = federation.getIncomingSink();
		in.register( MessageType.JoinFederation,    new JoinFederationHandler() );
		in.register( MessageType.ResignFederation,  new ResignFederationHandler() );
		
		// Synchronization Points
		in.register( MessageType.RegisterSyncPoint, new RegisterSyncPointHandler() );
		in.register( MessageType.AchieveSyncPoint,  new AchieveSyncPointHandler() );
		
		// Publication and Subscription
		in.register( MessageType.PublishObjectClass,     new PublishObjectClassHandler() );
		in.register( MessageType.SubscribeObjectClass,   new SubscribeObjectClassHandler() );
		in.register( MessageType.PublishInteraction,     new PublishInteractionClassHandler() );
		in.register( MessageType.SubscribeInteraction,   new SubscribeInteractionClassHandler() );
		in.register( MessageType.UnpublishObjectClass,   new UnpublishObjectClassHandler() );
		in.register( MessageType.UnsubscribeObjectClass, new UnsubscribeObjectClassHandler() );
		in.register( MessageType.UnpublishInteraction,   new UnpublishInteractionClassHandler() );
		in.register( MessageType.UnsubscribeInteraction, new UnsubscribeInteractionClassHandler() );
		
		// Object Management
		in.register( MessageType.RegisterObject,         new RegisterObjectHandler() );
		in.register( MessageType.DeleteObject,           new DeleteObjectHandler() );
		in.register( MessageType.RequestObjectUpdate,    new RequestObjectUpdateHandler() );
		in.register( MessageType.RequestClassUpdate,     new RequestClassUpdateHandler() );
		in.register( MessageType.ReserveObjectName,      new ReserveObjectNameHandler() );
		
		// Time Management
		in.register( MessageType.EnableTimeConstrained,  new EnableTimeConstrainedHandler() );
		in.register( MessageType.DisableTimeConstrained, new DisableTimeConstrainedHandler() );
		in.register( MessageType.EnableTimeRegulation,   new EnableTimeRegulationHandler() );
		in.register( MessageType.DisableTimeRegulation,  new DisableTimeRegulationHandler() );
		in.register( MessageType.EnableAsynchDelivery,   new EnableAsyncDeliveryHandler() );
		in.register( MessageType.TimeAdvanceRequest,     new TimeAdvanceRequestHandler() );
		in.register( MessageType.ModifyLookahead,        new ModifyLookaheadHandler() );
		
		// MOM Interaction Handling and Metric Collection
		if( PorticoConstants.isMomEnabled() )
		{
			in.register( MessageType.SendInteraction, new MomSendInteractionHandler() );
			in.register( MessageType.UpdateAttributes, new MomUpdateAttributesHandler() );
		}
		
		// Configure all handlers in the sink
		in.configure( settings );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This method will load a {@link Federation} within an RTI with the appropriate set of
	 * incoming/outgoing message handlers it should use based on the version of the HLA
	 * interface used.
	 * 
	 * @param federation The Federation to load the appropriate handler set into
	 * @throws JConfigurationException If the version is unknown or any of the handlers experience
	 *                                 an error as they are starting up
	 */
	public static void loadHandlers( Federation federation ) throws JConfigurationException
	{
		switch( federation.getHlaVersion() )
		{
			case HLA13:
				loadHla13( federation );
				break;
			case IEEE1516:
				loadIeee1516( federation );
				break;
			case IEEE1516e:
				loadIeee1516e( federation );
				break;
			default:
				throw new JConfigurationException( "Cannot load RTI handlers for unknown version: %s",
				                                   federation.getHlaVersion() );
		}		
	}
}
