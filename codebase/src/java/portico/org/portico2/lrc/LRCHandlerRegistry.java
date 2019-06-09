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
package org.portico2.lrc;

import java.util.HashMap;
import java.util.Map;

import org.portico.impl.hla1516e.handlers2.AttributeReleaseRequestCallbackHandler;
import org.portico.impl.hla1516e.handlers2.AttributesAcquiredCallbackHandler;
import org.portico.impl.hla1516e.handlers2.AttributesUnavailableCallbackHandler;
import org.portico.impl.hla1516e.handlers2.DiscoverObjectCallbackHandler;
import org.portico.impl.hla1516e.handlers2.FederationSynchronizedCallbackHandler;
import org.portico.impl.hla1516e.handlers2.ObjectNameReservationCallbackHandler;
import org.portico.impl.hla1516e.handlers2.ProvideUpdateCallbackHandler;
import org.portico.impl.hla1516e.handlers2.ReceiveInteractionCallbackHandler;
import org.portico.impl.hla1516e.handlers2.ReflectAttributesCallbackHandler;
import org.portico.impl.hla1516e.handlers2.RemoveObjectCallbackHandler;
import org.portico.impl.hla1516e.handlers2.SyncAnnounceCallbackHandler;
import org.portico.impl.hla1516e.handlers2.SyncRegisterResultCallbackHandler;
import org.portico.impl.hla1516e.handlers2.TimeAdvanceGrantCallbackHandler;
import org.portico.impl.hla1516e.handlers2.TimeConstrainedEnabledCallbackHandler;
import org.portico.impl.hla1516e.handlers2.TimeRegulationEnabledCallbackHandler;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.IMessageHandler;
import org.portico2.common.messaging.MessageSink;
import org.portico2.common.messaging.MessageType;
import org.portico2.lrc.services.federation.outgoing.CreateFederationHandler;
import org.portico2.lrc.services.federation.outgoing.DestroyFederationHandler;
import org.portico2.lrc.services.federation.outgoing.JoinFederationHandler;
import org.portico2.lrc.services.federation.outgoing.ListFederationsHandler;
import org.portico2.lrc.services.federation.outgoing.ResignFederationHandler;
import org.portico2.lrc.services.mom.incoming.SetExceptionReportingHandler;
import org.portico2.lrc.services.mom.incoming.SetServiceReportingHandler;
import org.portico2.lrc.services.object.incoming.DiscoverObjectHandler;
import org.portico2.lrc.services.object.incoming.ProvideObjectUpdateHandler;
import org.portico2.lrc.services.object.incoming.ReceiveInteractionHandler;
import org.portico2.lrc.services.object.incoming.ReflectAttributesHandler;
import org.portico2.lrc.services.object.incoming.RemoveObjectHandler;
import org.portico2.lrc.services.object.incoming.ReserveObjectNameResultHandler;
import org.portico2.lrc.services.object.outgoing.DeleteObjectHandler;
import org.portico2.lrc.services.object.outgoing.LocalDeleteObjectHandler;
import org.portico2.lrc.services.object.outgoing.RegisterObjectHandler;
import org.portico2.lrc.services.object.outgoing.RequestClassUpdateHandler;
import org.portico2.lrc.services.object.outgoing.RequestObjectUpdateHandler;
import org.portico2.lrc.services.object.outgoing.ReserveObjectNameHandler;
import org.portico2.lrc.services.object.outgoing.SendInteractionHandler;
import org.portico2.lrc.services.object.outgoing.UpdateAttributesHandler;
import org.portico2.lrc.services.ownership.incoming.AttributesUnavailableIncomingHandler;
import org.portico2.lrc.services.ownership.incoming.AttributeReleaseRequestIncomingHandler;
import org.portico2.lrc.services.ownership.incoming.AttributesAcquiredIncomingHandler;
import org.portico2.lrc.services.ownership.outgoing.AcquireOwnershipHandler;
import org.portico2.lrc.services.pubsub.outgoing.PublishInteractionClassHandler;
import org.portico2.lrc.services.pubsub.outgoing.PublishObjectClassHandler;
import org.portico2.lrc.services.pubsub.outgoing.SubscribeInteractionClassHandler;
import org.portico2.lrc.services.pubsub.outgoing.SubscribeObjectClassHandler;
import org.portico2.lrc.services.pubsub.outgoing.UnpublishInteractionClassHandler;
import org.portico2.lrc.services.pubsub.outgoing.UnpublishObjectClassHandler;
import org.portico2.lrc.services.pubsub.outgoing.UnsubscribeInteractionClassHandler;
import org.portico2.lrc.services.pubsub.outgoing.UnsubscribeObjectClassHandler;
import org.portico2.lrc.services.sync.outgoing.AchieveSyncPointHandler;
import org.portico2.lrc.services.sync.outgoing.RegisterSyncPointHandler;
import org.portico2.lrc.services.time.incoming.TimeAdvanceGrantHandler;
import org.portico2.lrc.services.time.outgoing.DisableAsyncDeliveryHandler;
import org.portico2.lrc.services.time.outgoing.DisableTimeConstrainedHandler;
import org.portico2.lrc.services.time.outgoing.DisableTimeRegulationHandler;
import org.portico2.lrc.services.time.outgoing.EnableAsyncDeliveryHandler;
import org.portico2.lrc.services.time.outgoing.EnableTimeConstrainedHandler;
import org.portico2.lrc.services.time.outgoing.EnableTimeRegulationHandler;
import org.portico2.lrc.services.time.outgoing.FlushQueueRequestHandler;
import org.portico2.lrc.services.time.outgoing.ModifyLookaheadHandler;
import org.portico2.lrc.services.time.outgoing.NextEventRequestHandler;
import org.portico2.lrc.services.time.outgoing.TimeAdvanceRequestHandler;

/**
 * The {@link HandlerRegistry} is the central point through which all handlers for the various
 * interface versions (HLA 1.3, 1516 or 1516e) are loaded.
 */
public class LRCHandlerRegistry
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
	private static void loadHla13( LRC lrc )
	{
		
	}
	
	private static void loadIeee1516( LRC lrc )
	{
		
	}
	
	private static void loadIeee1516e( LRC lrc )
	{
		// object map for configuration of handlers - stuffed with a reference to the LRC
		Map<String,Object> settings = new HashMap<>();
		settings.put( IMessageHandler.KEY_LRC, lrc );
		
		///////////////////////////////////////////////////////////////////////////
		///  Outgoing Handlers   //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		MessageSink out = lrc.getOutgoingSink();
		out.register( MessageType.CreateFederation,  new CreateFederationHandler() );
		out.register( MessageType.JoinFederation,    new JoinFederationHandler() );
		out.register( MessageType.ResignFederation,  new ResignFederationHandler() );
		out.register( MessageType.DestroyFederation, new DestroyFederationHandler() );
		out.register( MessageType.ListFederations,   new ListFederationsHandler() );
		
		// Synchronization Point Management
		out.register( MessageType.RegisterSyncPoint, new RegisterSyncPointHandler() );
		out.register( MessageType.AchieveSyncPoint,  new AchieveSyncPointHandler() );
		
		// Publication and Subscription
		out.register( MessageType.PublishObjectClass,     new PublishObjectClassHandler() );
		out.register( MessageType.SubscribeObjectClass,   new SubscribeObjectClassHandler() );
		out.register( MessageType.PublishInteraction,     new PublishInteractionClassHandler() );
		out.register( MessageType.SubscribeInteraction,   new SubscribeInteractionClassHandler() );
		out.register( MessageType.UnpublishObjectClass,   new UnpublishObjectClassHandler() );
		out.register( MessageType.UnsubscribeObjectClass, new UnsubscribeObjectClassHandler() );
		out.register( MessageType.UnpublishInteraction,   new UnpublishInteractionClassHandler() );
		out.register( MessageType.UnsubscribeInteraction, new UnsubscribeInteractionClassHandler() );
		
		// Objects and Interactions
		out.register( MessageType.SendInteraction,        new SendInteractionHandler() );
		out.register( MessageType.RegisterObject,         new RegisterObjectHandler() );
		out.register( MessageType.UpdateAttributes,       new UpdateAttributesHandler() );
		out.register( MessageType.DeleteObject,           new DeleteObjectHandler() );
		
		out.register( MessageType.LocalDeleteObject,      new LocalDeleteObjectHandler() );
		out.register( MessageType.RequestObjectUpdate,    new RequestObjectUpdateHandler() );
		out.register( MessageType.RequestClassUpdate,     new RequestClassUpdateHandler() );
		out.register( MessageType.ReserveObjectName,      new ReserveObjectNameHandler() );
		

		// Time Management
		out.register( MessageType.EnableTimeConstrained,  new EnableTimeConstrainedHandler() );
		out.register( MessageType.EnableTimeRegulation,   new EnableTimeRegulationHandler() );
		out.register( MessageType.EnableAsynchDelivery,   new EnableAsyncDeliveryHandler() );
		out.register( MessageType.DisableTimeConstrained, new DisableTimeConstrainedHandler() );
		out.register( MessageType.DisableTimeRegulation,  new DisableTimeRegulationHandler() );
		out.register( MessageType.DisableAsynchDelivery,  new DisableAsyncDeliveryHandler() );
		out.register( MessageType.TimeAdvanceRequest,     new TimeAdvanceRequestHandler() );
		out.register( MessageType.ModifyLookahead,        new ModifyLookaheadHandler() );
		out.register( MessageType.NextEventRequest,       new NextEventRequestHandler() );
		out.register( MessageType.FlushQueueRequest,      new FlushQueueRequestHandler() );
		
		// Ownership Management
		out.register( MessageType.AttributeAcquire,       new AcquireOwnershipHandler() );
		
		// Configure all the registered handlers
		out.configure( settings );
		
		
		///////////////////////////////////////////////////////////////////////////
		///  Incoming Handlers   //////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////
		MessageSink in = lrc.getIncomingSink();

		// Synchronization Points
		in.register( MessageType.AnnounceSyncPoint,       new SyncAnnounceCallbackHandler() );
		in.register( MessageType.RegisterSyncPointResult, new SyncRegisterResultCallbackHandler() );
		in.register( MessageType.FederationSynchronized,  new FederationSynchronizedCallbackHandler() );
		
		// Objects and Interactions
		in.register( MessageType.SendInteraction,         new ReceiveInteractionHandler() );
		in.register( MessageType.SendInteraction,         new ReceiveInteractionCallbackHandler() );
		in.register( MessageType.DiscoverObject,          new DiscoverObjectHandler() );
		in.register( MessageType.DiscoverObject,          new DiscoverObjectCallbackHandler() );
		in.register( MessageType.UpdateAttributes,        new ReflectAttributesHandler() );
		in.register( MessageType.UpdateAttributes,        new ReflectAttributesCallbackHandler() );
		in.register( MessageType.DeleteObject,            new RemoveObjectHandler() );
		in.register( MessageType.DeleteObject,            new RemoveObjectCallbackHandler() );
		in.register( MessageType.RequestObjectUpdate,     new ProvideObjectUpdateHandler() );
		in.register( MessageType.RequestObjectUpdate,     new ProvideUpdateCallbackHandler() );
		in.register( MessageType.ReserveObjectNameResult, new ReserveObjectNameResultHandler() );
		in.register( MessageType.ReserveObjectNameResult, new ObjectNameReservationCallbackHandler() );
		
		// Time Management
		in.register( MessageType.EnableTimeConstrained,   new TimeConstrainedEnabledCallbackHandler() );
		in.register( MessageType.EnableTimeRegulation,    new TimeRegulationEnabledCallbackHandler() );
		in.register( MessageType.TimeAdvanceGrant,        new TimeAdvanceGrantHandler() );
		in.register( MessageType.TimeAdvanceGrant,        new TimeAdvanceGrantCallbackHandler() );

		// Ownership Management
		in.register( MessageType.AttributeAcquire,        new AttributeReleaseRequestIncomingHandler() );
		in.register( MessageType.AttributeAcquire,        new AttributeReleaseRequestCallbackHandler() );
		
		in.register( MessageType.AttributesUnavailable,   new AttributesUnavailableIncomingHandler() );
		in.register( MessageType.AttributesUnavailable,   new AttributesUnavailableCallbackHandler() );

		in.register( MessageType.OwnershipAcquired,       new AttributesAcquiredIncomingHandler() );
		in.register( MessageType.OwnershipAcquired,       new AttributesAcquiredCallbackHandler() );
		
		// MOM
		in.register( MessageType.SetServiceReporting,     new SetServiceReportingHandler() );
		in.register( MessageType.SetExceptionReporting,   new SetExceptionReportingHandler() );
		
		// Configure all the registered handlers
		in.configure( settings );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This method will load the LRC with the appropriate set of incoming/outgoing message
	 * handlers it should use based on the version of the HLA interface used.
	 * 
	 * @param lrc The LRC to load the appropriate handler set into
	 * @throws JConfigurationException If the version is unknown or any of the handlers experience
	 *                                 an error as they are starting up
	 */
	public static void loadHandlers( LRC lrc ) throws JConfigurationException
	{
		switch( lrc.getHlaVersion() )
		{
			case HLA13:
				loadHla13( lrc );
				break;
			case IEEE1516:
				loadIeee1516( lrc );
				break;
			case IEEE1516e:
				loadIeee1516e( lrc );
				break;
			default:
				throw new JConfigurationException( "Cannot load LRC handlers for unknown version: %s", lrc.getHlaVersion() );
		}		
	}
}
