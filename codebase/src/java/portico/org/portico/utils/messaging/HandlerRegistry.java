/*
 *   Copyright 2015 The Portico Project
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
package org.portico.utils.messaging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.portico.impl.hla13.handlers.AssumptionRequestCallbackHandler;
import org.portico.impl.hla13.handlers.AttributeOwnershipQueryCallbackHandler;
import org.portico.impl.hla13.handlers.AttributesAcquiredCallbackHandler;
import org.portico.impl.hla13.handlers.AttributesUnavailableCallbackHandler;
import org.portico.impl.hla13.handlers.CancelOwnershipRequestCallbackHandler;
import org.portico.impl.hla13.handlers.DiscoverObjectCallbackHandler;
import org.portico.impl.hla13.handlers.DivestConfirmationCallbackHandler;
import org.portico.impl.hla13.handlers.InitiateSaveCallbackHandler;
import org.portico.impl.hla13.handlers.ProvideUpdateCallbackHandler;
import org.portico.impl.hla13.handlers.ReceiveInteractionCallbackHandler;
import org.portico.impl.hla13.handlers.ReflectAttributesCallbackHandler;
import org.portico.impl.hla13.handlers.RemoveObjectCallbackHandler;
import org.portico.impl.hla13.handlers.RequestAttributeReleaseCallbackHandler;
import org.portico.impl.hla13.handlers.RestoreBegunCallbackHandler;
import org.portico.impl.hla13.handlers.RestoreCompleteCallbackHandler;
import org.portico.impl.hla13.handlers.RestoreInitiateCallbackHandler;
import org.portico.impl.hla13.handlers.RestoreRequestCallbackHandler;
import org.portico.impl.hla13.handlers.SaveCompleteCallbackHandler;
import org.portico.impl.hla13.handlers.SyncAchievedCallbackHandler;
import org.portico.impl.hla13.handlers.SyncAnnounceCallbackHandler;
import org.portico.impl.hla13.handlers.SyncRegResultCallbackHandler;
import org.portico.impl.hla13.handlers.TimeAdvanceGrantCallbackHandler;
import org.portico.impl.hla13.handlers.TimeConstrainedEnabledCallbackHandler;
import org.portico.impl.hla13.handlers.TimeRegulationEnabledCallbackHandler;
import org.portico.impl.hla1516e.handlers.ObjectNameReservationCallbackHandler;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.services.ddm.handlers.incoming.ModifiedRegionHandler;
import org.portico.lrc.services.ddm.handlers.incoming.NewRegionHandler;
import org.portico.lrc.services.ddm.handlers.incoming.RegionAssociatedHandler;
import org.portico.lrc.services.ddm.handlers.incoming.RegionUnassociatedHandler;
import org.portico.lrc.services.ddm.handlers.incoming.RemoveRegionHandler;
import org.portico.lrc.services.ddm.handlers.outgoing.AssociateRegionHandler;
import org.portico.lrc.services.ddm.handlers.outgoing.CreateRegionHandler;
import org.portico.lrc.services.ddm.handlers.outgoing.DeleteRegionHandler;
import org.portico.lrc.services.ddm.handlers.outgoing.ModifyRegionHandler;
import org.portico.lrc.services.ddm.handlers.outgoing.UnassociateRegionHandler;
import org.portico.lrc.services.federation.handlers.incoming.ResignNotificationHandler;
import org.portico.lrc.services.federation.handlers.incoming.RoleCallHandler;
import org.portico.lrc.services.federation.handlers.outgoing.CreateFederationHandler;
import org.portico.lrc.services.federation.handlers.outgoing.DestroyFederationHandler;
import org.portico.lrc.services.federation.handlers.outgoing.JoinFederationHandler;
import org.portico.lrc.services.federation.handlers.outgoing.ResignFederationHandler;
import org.portico.lrc.services.object.handlers.incoming.DiscoverObjectHandler;
import org.portico.lrc.services.object.handlers.incoming.ReceiveInteractionHandler;
import org.portico.lrc.services.object.handlers.incoming.ReflectAttributesHandler;
import org.portico.lrc.services.object.handlers.incoming.RemoveObjectHandler;
import org.portico.lrc.services.object.handlers.incoming.RequestClassUpdateIncomingHandler;
import org.portico.lrc.services.object.handlers.incoming.RequestObjectUpdateIncomingHandler;
import org.portico.lrc.services.object.handlers.outgoing.DeleteObjectHandler;
import org.portico.lrc.services.object.handlers.outgoing.LocalDeleteHandler;
import org.portico.lrc.services.object.handlers.outgoing.RegisterObjectHandler;
import org.portico.lrc.services.object.handlers.outgoing.RequestClassUpdateHandler;
import org.portico.lrc.services.object.handlers.outgoing.RequestObjectUpdateHandler;
import org.portico.lrc.services.object.handlers.outgoing.ReserveObjectNameHandler;
import org.portico.lrc.services.object.handlers.outgoing.SendInteractionHandler;
import org.portico.lrc.services.object.handlers.outgoing.UpdateAttributesHandler;
import org.portico.lrc.services.ownership.handlers.incoming.AcquireOwnershipIncomingHandler;
import org.portico.lrc.services.ownership.handlers.incoming.AttributeReleaseIncomingHandler;
import org.portico.lrc.services.ownership.handlers.incoming.CancellationConfirmationIncomingHandler;
import org.portico.lrc.services.ownership.handlers.incoming.DivestOwnershipIncomingHandler;
import org.portico.lrc.services.ownership.handlers.incoming.OwnershipAcquiredIncomingHandler;
import org.portico.lrc.services.ownership.handlers.incoming.RequestCancelAcquisitionIncomingHandler;
import org.portico.lrc.services.ownership.handlers.outgoing.AcquireOwnershipHandler;
import org.portico.lrc.services.ownership.handlers.outgoing.AttributeReleaseResponseHandler;
import org.portico.lrc.services.ownership.handlers.outgoing.CancelAcquisitionHandler;
import org.portico.lrc.services.ownership.handlers.outgoing.CancelDivestHandler;
import org.portico.lrc.services.ownership.handlers.outgoing.DivestAttributesHandler;
import org.portico.lrc.services.ownership.handlers.outgoing.QueryOwnershipHandler;
import org.portico.lrc.services.pubsub.handlers.incoming.PublishInteractionClassIncomingHandler;
import org.portico.lrc.services.pubsub.handlers.incoming.PublishObjectClassIncomingHandler;
import org.portico.lrc.services.pubsub.handlers.incoming.SubscribeInteractionClassIncomingHandler;
import org.portico.lrc.services.pubsub.handlers.incoming.SubscribeObjectClassIncomingHandler;
import org.portico.lrc.services.pubsub.handlers.incoming.UnpublishInteractionClassIncomingHandler;
import org.portico.lrc.services.pubsub.handlers.incoming.UnpublishObjectClassIncomingHandler;
import org.portico.lrc.services.pubsub.handlers.incoming.UnsubscribeInteractionClassIncomingHandler;
import org.portico.lrc.services.pubsub.handlers.incoming.UnsubscribeObjectClassIncomingHandler;
import org.portico.lrc.services.pubsub.handlers.outgoing.PublishInteractionClassHandler;
import org.portico.lrc.services.pubsub.handlers.outgoing.PublishObjectClassHandler;
import org.portico.lrc.services.pubsub.handlers.outgoing.SubscribeInteractionClassHandler;
import org.portico.lrc.services.pubsub.handlers.outgoing.SubscribeObjectClassHandler;
import org.portico.lrc.services.pubsub.handlers.outgoing.UnpublishInteractionClassHandler;
import org.portico.lrc.services.pubsub.handlers.outgoing.UnpublishObjectClassHandler;
import org.portico.lrc.services.pubsub.handlers.outgoing.UnsubscribeInteractionClassHandler;
import org.portico.lrc.services.pubsub.handlers.outgoing.UnsubscribeObjectClassHandler;
import org.portico.lrc.services.saverestore.handlers.incoming.RestoreCompleteIncomingHandler;
import org.portico.lrc.services.saverestore.handlers.incoming.RestoreFederationIncomingHandler;
import org.portico.lrc.services.saverestore.handlers.incoming.RestoreRequestIncomingHandler;
import org.portico.lrc.services.saverestore.handlers.incoming.SaveBegunIncomingHandler;
import org.portico.lrc.services.saverestore.handlers.incoming.SaveCompleteIncomingHandler;
import org.portico.lrc.services.saverestore.handlers.incoming.SaveRequestIncomingHandler;
import org.portico.lrc.services.saverestore.handlers.outgoing.RestoreCompleteHandler;
import org.portico.lrc.services.saverestore.handlers.outgoing.RestoreRequestHandler;
import org.portico.lrc.services.saverestore.handlers.outgoing.SaveBegunHandler;
import org.portico.lrc.services.saverestore.handlers.outgoing.SaveCompleteHandler;
import org.portico.lrc.services.saverestore.handlers.outgoing.SaveRequestHandler;
import org.portico.lrc.services.sync.handlers.incoming.SyncAnnounceHandler;
import org.portico.lrc.services.sync.handlers.incoming.SyncRegistrationRequestHandler;
import org.portico.lrc.services.sync.handlers.outgoing.RegisterSyncPointHandler;
import org.portico.lrc.services.sync.handlers.outgoing.SyncAchievedHandler;
import org.portico.lrc.services.time.handlers.incoming.DisableConstrainedIncomingHandler;
import org.portico.lrc.services.time.handlers.incoming.DisableRegulationIncomingHandler;
import org.portico.lrc.services.time.handlers.incoming.EnableConstrainedIncomingHandler;
import org.portico.lrc.services.time.handlers.incoming.EnableRegulationIncomingHandler;
import org.portico.lrc.services.time.handlers.incoming.ModifyLookaheadIncomingHandler;
import org.portico.lrc.services.time.handlers.incoming.TimeAdvanceGrantedIncomingHandler;
import org.portico.lrc.services.time.handlers.incoming.TimeAdvanceRequestIncomingHandler;
import org.portico.lrc.services.time.handlers.outgoing.DisableAsyncDeliveryHandler;
import org.portico.lrc.services.time.handlers.outgoing.DisableTimeConstrainedHandler;
import org.portico.lrc.services.time.handlers.outgoing.DisableTimeRegulationHandler;
import org.portico.lrc.services.time.handlers.outgoing.EnableAsyncDeliveryHandler;
import org.portico.lrc.services.time.handlers.outgoing.EnableTimeConstrainedHandler;
import org.portico.lrc.services.time.handlers.outgoing.EnableTimeRegulationHandler;
import org.portico.lrc.services.time.handlers.outgoing.FlushQueueRequestHandler;
import org.portico.lrc.services.time.handlers.outgoing.ModifyLookaheadHandler;
import org.portico.lrc.services.time.handlers.outgoing.NextEventRequestHandler;
import org.portico.lrc.services.time.handlers.outgoing.TimeAdvanceRequestHandler;

/**
 * The HandlerRegistry exists to simplify the process of locating and loading handlers.
 * During the initialization of an {@link LRC} will ask the registry for all registered
 * {@link MessageHandler} classes before instantiating them and adding the new instances
 * to its local {@link MessageSink}s.
 * 
 * Implementation Note: The registry has been created to replace the previous classpath-inspecting
 * lookup mechanism that was designed to automatically find implementing handlers anywhere on
 * the classpath. Unfortunately that approach was typically over-engineered: "magic" when it 
 * worked, and frequently broken. This simple approach may have fewer fairies, but it is simple
 * and explicit.
 */
public class HandlerRegistry
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String,Module> modules;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public HandlerRegistry()
	{
		this.modules = new HashMap<String,Module>();
		loadBaseHandlers();
		loadHla13CallbackHandlers();
		loadIeee1516CallbackHandlers();
		loadIeee1516eCallbackHandlers();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	private void loadBaseHandlers()
	{
		///////////////////////////////////////////////////////
		// Federation Management   ////////////////////////////
		///////////////////////////////////////////////////////
		// Outgoing
		register( CreateFederationHandler.class );
		register( JoinFederationHandler.class );
		register( ResignFederationHandler.class );
		register( DestroyFederationHandler.class );
		register( RegisterSyncPointHandler.class );
		register( SyncAchievedHandler.class );
		
		// Incoming
		register( ResignNotificationHandler.class );
		register( RoleCallHandler.class );
		register( org.portico.lrc.services.sync.handlers.incoming.SyncAchievedHandler.class );
		register( SyncAnnounceHandler.class );
		register( SyncRegistrationRequestHandler.class );

		///////////////////////////////////////////////////////
		// Publication / Subscription   ///////////////////////
		///////////////////////////////////////////////////////
		// Outgoing
		register( PublishObjectClassHandler.class );
		register( PublishInteractionClassHandler.class );
		register( SubscribeObjectClassHandler.class );
		register( SubscribeInteractionClassHandler.class );
		register( UnpublishObjectClassHandler.class );
		register( UnpublishInteractionClassHandler.class );
		register( UnsubscribeObjectClassHandler.class );
		register( UnsubscribeInteractionClassHandler.class );
		
		// Incoming
		register( PublishInteractionClassIncomingHandler.class );
		register( PublishObjectClassIncomingHandler.class );
		register( SubscribeInteractionClassIncomingHandler.class );
		register( SubscribeObjectClassIncomingHandler.class );
		register( UnpublishInteractionClassIncomingHandler.class );
		register( UnpublishObjectClassIncomingHandler.class );
		register( UnsubscribeInteractionClassIncomingHandler.class );
		register( UnsubscribeObjectClassIncomingHandler.class );
		
		///////////////////////////////////////////////////////
		// Object Management   ////////////////////////////////
		///////////////////////////////////////////////////////
		// Outgoing
		register( DeleteObjectHandler.class );
		register( LocalDeleteHandler.class );
		register( RegisterObjectHandler.class );
		register( RequestClassUpdateHandler.class );
		register( RequestObjectUpdateHandler.class );
		register( ReserveObjectNameHandler.class );
		register( SendInteractionHandler.class );
		register( UpdateAttributesHandler.class );
		
		// Incoming
		register( DiscoverObjectHandler.class );
		register( ReceiveInteractionHandler.class );
		register( ReflectAttributesHandler.class );
		register( RemoveObjectHandler.class );
		register( RequestClassUpdateIncomingHandler.class );
		register( RequestObjectUpdateIncomingHandler.class );
		register( org.portico.lrc.services.object.handlers.incoming.ReserveObjectNameHandler.class );

		///////////////////////////////////////////////////////
		// Time Management   //////////////////////////////////
		///////////////////////////////////////////////////////
		// Outgoing
		register( DisableAsyncDeliveryHandler.class );
		register( DisableTimeConstrainedHandler.class );
		register( DisableTimeRegulationHandler.class );
		register( EnableAsyncDeliveryHandler.class );
		register( EnableTimeConstrainedHandler.class );
		register( EnableTimeRegulationHandler.class );
		register( FlushQueueRequestHandler.class );
		register( ModifyLookaheadHandler.class );
		register( NextEventRequestHandler.class );
		register( TimeAdvanceRequestHandler.class );
		
		// Incoming
		register( DisableConstrainedIncomingHandler.class );
		register( DisableRegulationIncomingHandler.class );
		register( EnableConstrainedIncomingHandler.class );
		register( EnableRegulationIncomingHandler.class );
		register( ModifyLookaheadIncomingHandler.class );
		register( TimeAdvanceGrantedIncomingHandler.class );
		register( TimeAdvanceRequestIncomingHandler.class );

		///////////////////////////////////////////////////////
		// Save/Restore   /////////////////////////////////////
		///////////////////////////////////////////////////////
		// Outgoing
		register( RestoreCompleteHandler.class );
		register( RestoreRequestHandler.class );
		register( SaveBegunHandler.class );
		register( SaveCompleteHandler.class );
		register( SaveRequestHandler.class );
		
		// Incoming
		register( RestoreCompleteIncomingHandler.class );
		register( RestoreFederationIncomingHandler.class );
		register( RestoreRequestIncomingHandler.class );
		register( SaveBegunIncomingHandler.class );
		register( SaveCompleteIncomingHandler.class );
		register( SaveRequestIncomingHandler.class );

		///////////////////////////////////////////////////////
		// Ownership Management   /////////////////////////////
		///////////////////////////////////////////////////////
		// Outgoing
		register( AcquireOwnershipHandler.class );
		register( AttributeReleaseResponseHandler.class );
		register( CancelAcquisitionHandler.class );
		register( CancelDivestHandler.class );
		register( DivestAttributesHandler.class );
		register( QueryOwnershipHandler.class );
		
		// Incoming
		register( AcquireOwnershipIncomingHandler.class );
		register( AttributeReleaseIncomingHandler.class );
		register( CancellationConfirmationIncomingHandler.class );
		register( DivestOwnershipIncomingHandler.class );
		register( OwnershipAcquiredIncomingHandler.class );
		register( RequestCancelAcquisitionIncomingHandler.class );

		///////////////////////////////////////////////////////
		// Data Distribution Management   /////////////////////
		///////////////////////////////////////////////////////
		// Outgoing
		register( AssociateRegionHandler.class );
		register( CreateRegionHandler.class );
		register( DeleteRegionHandler.class );
		register( ModifyRegionHandler.class );
		register( UnassociateRegionHandler.class );
		
		// Incoming
		register( ModifiedRegionHandler.class );
		register( NewRegionHandler.class );
		register( RegionAssociatedHandler.class );
		register( RegionUnassociatedHandler.class );
		register( RemoveRegionHandler.class );
	}

	private void loadHla13CallbackHandlers()
	{
		register( AssumptionRequestCallbackHandler.class );
		register( AttributeOwnershipQueryCallbackHandler.class );
		register( AttributesAcquiredCallbackHandler.class );
		register( AttributesUnavailableCallbackHandler.class );
		register( CancelOwnershipRequestCallbackHandler.class );
		register( DiscoverObjectCallbackHandler.class );
		register( DivestConfirmationCallbackHandler.class );
		register( InitiateSaveCallbackHandler.class );
		register( ProvideUpdateCallbackHandler.class );
		register( ReceiveInteractionCallbackHandler.class );
		register( ReflectAttributesCallbackHandler.class );
		register( RemoveObjectCallbackHandler.class );
		register( RequestAttributeReleaseCallbackHandler.class );
		register( RestoreBegunCallbackHandler.class );
		register( RestoreCompleteCallbackHandler.class );
		register( RestoreInitiateCallbackHandler.class );
		register( RestoreRequestCallbackHandler.class );
		register( SaveCompleteCallbackHandler.class );
		register( SyncAchievedCallbackHandler.class );
		register( SyncAnnounceCallbackHandler.class );
		register( SyncRegResultCallbackHandler.class );
		register( TimeAdvanceGrantCallbackHandler.class );
		register( TimeConstrainedEnabledCallbackHandler.class );
		register( TimeRegulationEnabledCallbackHandler.class );
	}
	
	private void loadIeee1516CallbackHandlers()
	{
		register( org.portico.impl.hla1516.handlers.DiscoverObjectCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.ProvideUpdateCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.ReceiveInteractionCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.ReflectAttributesCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.RemoveObjectCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.SyncAchievedCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.SyncAnnounceCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.SyncRegResultCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.TimeAdvanceGrantCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.TimeConstrainedEnabledCallbackHandler.class );
		register( org.portico.impl.hla1516.handlers.TimeRegulationEnabledCallbackHandler.class );
	}
	
	private void loadIeee1516eCallbackHandlers()
	{
		register( org.portico.impl.hla1516e.handlers.AssumptionRequestCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.AttributeOwnershipQueryCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.AttributesAcquiredCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.AttributesUnavailableCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.CancelOwnershipRequestCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.DiscoverObjectCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.InitiateSaveCallbackHandler.class );
		register( ObjectNameReservationCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.ProvideUpdateCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.ReceiveInteractionCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.ReflectAttributesCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.RemoveObjectCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.RequestAttributeReleaseCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.RestoreBegunCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.RestoreCompleteCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.RestoreInitiateCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.RestoreRequestCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.SaveCompleteCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.SyncAchievedCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.SyncAnnounceCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.SyncRegResultCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.TimeAdvanceGrantCallbackHandler.class );
//		register( org.portico.impl.hla1516e.handlers2.TimeConstrainedEnabledCallbackHandler.class );
		register( org.portico.impl.hla1516e.handlers.TimeRegulationEnabledCallbackHandler.class );
	}
	
	/**
	 * Register the given {@link IMessageHandler} with the registry.
	 * 
	 * This method will inspect the class, via the {@link MessageHandler} annotation,
	 * and add it to the associated {@link Module} internally.
	 * 
	 * @param clazz The message handler class to register
	 * @throws JConfigurationException Class does not declare the {@link MessageHandler} annotation.
	 */
	private void register( Class<? extends IMessageHandler> clazz ) throws JConfigurationException
	{
		// Check to make sure the MessageHandler annotation is present
		MessageHandler annotation = clazz.getAnnotation( MessageHandler.class );
		if( annotation == null )
		{
			throw new JConfigurationException( "Class does not declare MessageHandler annotation: "+
			                                   clazz.getCanonicalName() );
		}

		// find out which modules the handler wants to be associated with
		for( String moduleName : annotation.modules() )
		{
			// does a module of this name already exist? if so, add to it, if not create it
			if( modules.containsKey(moduleName) )
			{
				modules.get(moduleName).addHandler( clazz );
			}
			else
			{
				Module newModule = new Module( moduleName );
				newModule.addHandler( clazz );
				modules.put( moduleName, newModule );
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	/// Accessor and Mutator Methods   ///////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////
	public Collection<Module> getAllModules()
	{
		return modules.values();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
