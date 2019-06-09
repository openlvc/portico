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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.model.XmlRenderer;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.PorticoConstants;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.services.mom.msg.SetExceptionReporting;
import org.portico2.common.services.mom.msg.SetServiceReporting;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.rti.RtiConnection;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.federation.FederateMetrics;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.RTIMessageHandler;
import org.portico2.rti.services.mom.data.FomModule;
import org.portico2.rti.services.mom.data.InteractionCount;
import org.portico2.rti.services.mom.data.InteractionSubscription;
import org.portico2.rti.services.mom.data.MomEncodingHelpers;
import org.portico2.rti.services.mom.data.MomException;
import org.portico2.rti.services.mom.data.ObjectClassBasedCount;
import org.portico2.rti.services.mom.data.SynchPointFederate;
import org.portico2.rti.services.object.data.RACInstance;
import org.portico2.rti.services.object.data.ROCInstance;
import org.portico2.rti.services.object.data.Repository;
import org.portico2.rti.services.sync.data.SyncPoint;
import org.portico2.rti.services.sync.data.SyncPointManager;
import org.w3c.dom.Document;

/**
 * This handler receives all {@link SendInteraction} messages reflected into the Federation's internal
 * message sink when {@link Federation#queueDataMessage(PorticoMessage, RtiConnection)} is called.
 * <p/>
 * The purpose of the handler is two-fold:
 * <ol>
 *  <li>Collect metrics on interactions sent and received for ALL interactions</code>
 *  <li>Process and respond to MOM interactions</code>
 * </ol>
 * Handler methods for MOM interactions can be registered by calling the 
 * {@link #registerMomInteractionHandler(String, ConsumerWithException)} method 
 */
public class MomSendInteractionHandler extends RTIMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/**
	 * Mom elements have different names based on which version of the spec the sending federate 
	 * implements. To make things more manageable, the manager normalizes all names to a canonical
	 * version for processing.
	 */
	private static final HLAVersion CANONICAL_VERSION = HLAVersion.IEEE1516e;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Integer,ConsumerWithException<Map<String,Object>,MomException>> handlers;
	private boolean supportsMomException;
	private ICMetadata reportMomExceptionMetadata;
	private PCMetadata federateMetadata;
	private PCMetadata serviceMetadata;
	private PCMetadata exceptionMetadata;
	private PCMetadata parameterErrorMetadata;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomSendInteractionHandler()
	{
		// Register handlers for requests that we will respond to
		this.handlers = new HashMap<>();
		
		//
		// HLAfederate
		//
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLAadjust.HLAsetServiceReporting", 
		                                    this::handleFederateSetServiceReporting );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLAadjust.HLAsetExceptionReporting", 
		                                    this::handleFederateSetExceptionReporting );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestPublications", 
		                                    this::handleFederateRequestPublications );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestSubscriptions", 
		                                    this::handleFederateRequestSubscriptions );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesThatCanBeDeleted", 
		                                    this::handleFederateRequestObjectInstancesThatCanBeDeleted );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesUpdated", 
		                                    this::handleFederateRequestObjectInstancesUpdated );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstancesReflected", 
		                                    this::handleFederateRequestObjectInstancesReflected );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestUpdatesSent", 
		                                    this::handleFederateRequestUpdatesSent );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestInteractionsSent", 
		                                    this::handleFederateRequestInteractionsSent );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestReflectionsReceived", 
		                                    this::handleFederateRequestReflectionsReceived );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestInteractionsReceived", 
		                                    this::handleFederateRequestInteractionsReceived );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestObjectInstanceInformation", 
		                                    this::handleFederateRequestObjectInstanceInformation );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederate.HLArequest.HLArequestFOMmoduleData", 
		                                    this::handleFederateRequestFomModuleData );
		
		//
		// HLAfederation
		//
		this.registerMomInteractionHandler( "HLAmanager.HLAfederation.HLArequest.HLArequestSynchronizationPoints", 
		                                    this::handleFederationRequestSynchronizationPoints );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederation.HLArequest.HLArequestSynchronizationPointStatus", 
		                                    this::handleFederationRequestSynchronizationPointStatus );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederation.HLArequest.HLArequestFOMmoduleData", 
		                                    this::handleFederationRequestFomModuleData );
		this.registerMomInteractionHandler( "HLAmanager.HLAfederation.HLArequest.HLArequestMIMdata", 
		                                    this::handleFederationRequestMIMData );
		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		super.configure( properties );
		this.cacheReportMomExceptionMetadata();
	}
	
	@Override
	public void process( MessageContext context ) throws JException
	{
		ObjectModel objectModel = federation.getFOM();
		SendInteraction request = context.getRequest( SendInteraction.class );
		
		// Record metrics for the interaction sender
		int interactionId = request.getInteractionId();
		ICMetadata interactionClass = objectModel.getInteractionClass( interactionId );
		int sender = request.getSourceFederate();
		momManager.interactionSent( sender, interactionClass );
		
		// Record metrics for all interaction receivers
		InterestManager interests = federation.getInterestManager();
		Set<Integer> subscribers = interests.getAllSubscribers( interactionClass );
		for( int subscriber : subscribers )
		{
			if( subscriber != sender )
				momManager.interactionReceived( subscriber, interactionClass );
		}
		
		// If the incoming interaction is a MOM interaction, then handle it
		if( interactionId < ObjectModel.MAX_MOM_HANDLE && 
			request.getSourceFederate() != PorticoConstants.RTI_HANDLE )
		{
			processMomInteraction( request );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  MOM Interaction Handlers   ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void handleFederateSetServiceReporting( Map<String,Object> requestParams )
		throws MomException
	{
		// TODO This has potential for race conditions between the target federate subscribing to
		// HLAreportServiceInvocation, and the requesting federate sending HLAsetServiceReporting. If
		// all requests are processed in a single thread within the RTI then it should all be ok.
		
		boolean reportingState = (boolean)requestParams.get( "HLAreportingState" );
		
		Federate federate = getRequestFederate( requestParams );
		int federateHandle = federate.getFederateHandle();
		InterestManager interests = federation.getInterestManager();
		
		int rsiHandle = Mom.getMomInteractionHandle( CANONICAL_VERSION, 
		                                             "HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation" );
		boolean isSubscribedToRsi = interests.isInteractionClassSubscribed( federateHandle, 
		                                    		                        rsiHandle ); 
		if( reportingState  )
		{
			// Spec only allows service reporting to be enabled on federates that DO NOT subscribe to
			// HLAreportServiceInvocation
			if( !isSubscribedToRsi )
			{
				SetServiceReporting enableReporting = new SetServiceReporting( true );
				queueUnicast( enableReporting, federateHandle );
				
				logger.debug( "Asking " + federate.getFederateName()+" ["+federateHandle+"]" +
				              " LRC to ENABLE service reporting" );
			}
			else
			{
				throw new MomException( "federate "+federate.getFederateHandle() + 
				                        " subscribes to HLAmanager.HLAfederate.HLAreport.HLAreportServiceInvocation", 
				                        false );
			}
		}
		else
		{
			SetServiceReporting disableReporting = new SetServiceReporting( false );
			disableReporting.setTargetFederate( federateHandle );
			queueUnicast( disableReporting, federateHandle );
			logger.debug( "Asking " + federate.getFederateName()+" ["+federateHandle+"]" +
	              " LRC to DISABLE service reporting" );
		}
	}
	
	private void handleFederateSetExceptionReporting( Map<String,Object> requestParams )
		throws MomException
	{
		boolean reportingState = (boolean)requestParams.get( "HLAreportingState" );
		
		Federate federate = getRequestFederate( requestParams );
		int federateHandle = federate.getFederateHandle();
		
		SetExceptionReporting enableReporting = new SetExceptionReporting( reportingState );
		queueUnicast( enableReporting, federateHandle );
		
		logger.debug( "Exception reporting for " +
		              federate.getFederateName()+" ["+federateHandle+"]" +
		              " has been " + (reportingState ? "ENABLED" : "DISABLED") );
		
	}
	
	private void handleFederateRequestPublications( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		int federateHandle = federate.getFederateHandle();
		
		//
		// Objects
		//
		InterestManager interests = federation.getInterestManager();
		
		Set<Integer> publishedOCs = interests.getAllPublishedObjectClasses( federateHandle );
		Map<String,Object> ocResponseParams = new HashMap<String,Object>();
		ocResponseParams.put( "HLAfederate", federate.getFederateHandle() );
		ocResponseParams.put( "HLAnumberOfClasses", publishedOCs.size() );
		
		if( publishedOCs.size() > 0 )
		{
			// Send one interaction for each object class published
			for( int classHandle : publishedOCs )
			{
				Set<Integer> attributes = interests.getPublishedAttributes( federateHandle, classHandle );
				ocResponseParams.put( "HLAobjectClass", classHandle );
				ocResponseParams.put( "HLAattributeList", attributes );
				// Send response for this object class
				sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectClassPublication", 
				              ocResponseParams );
			}
		}
		else
		{
			// Send NULL response if no object classes are published
			sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectClassPublication", 
			              ocResponseParams );	
		}
		
		//
		// Interactions
		//
		Set<Integer> publishedICs = interests.getAllPublishedInteractionClasses( federateHandle );
		
		Map<String,Object> icResponseParams = new HashMap<String,Object>();
		icResponseParams.put( "HLAfederate", federate.getFederateHandle() );
		icResponseParams.put( "HLAinteractionClassList", publishedICs );
		
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionPublication", 
		              icResponseParams );
	}
	
	private void handleFederateRequestSubscriptions( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		int federateHandle = federate.getFederateHandle();
		
		//
		// Objects
		//
		InterestManager interests = federation.getInterestManager();
		
		Set<Integer> subscribedOCs = interests.getAllSubscribedObjectClasses( federateHandle );
		Map<String,Object> ocResponseParams = new HashMap<String,Object>();
		ocResponseParams.put( "HLAfederate", federate.getFederateHandle() );
		ocResponseParams.put( "HLAnumberOfClasses", subscribedOCs.size() );
		
		if( subscribedOCs.size() > 0 )
		{
			// Send one interaction for each object class subscribed to
			for( int classHandle : subscribedOCs )
			{
				Set<Integer> attributes = interests.getSubscribedAttributes( federateHandle, classHandle );
				ocResponseParams.put( "HLAobjectClass", classHandle );
				ocResponseParams.put( "HLAactive", true );               // TODO Not supported yet
				ocResponseParams.put( "HLAmaxUpdateRate", "None" );      // TODO Not supported yet
				ocResponseParams.put( "HLAattributeList", attributes );
				// Send response for this object class
				sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectClassSubscription", 
				              ocResponseParams );
			}
		}
		else
		{
			// Send NULL response if there are no object class subscriptions
			sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectClassSubscription", 
			              ocResponseParams );	
		}
		
		//
		// Interactions
		//
		Set<Integer> subscribedICs = interests.getAllSubscribedInteractionClasses( federateHandle );
		InteractionSubscription[] icSubscriptions = new InteractionSubscription[subscribedICs.size()];
		int index = 0;
		for( int subscribedIC : subscribedICs )
			icSubscriptions[index++] = new InteractionSubscription( subscribedIC );
		
		Map<String,Object> icResponseParams = new HashMap<String,Object>();
		icResponseParams.put( "HLAfederate", federate.getFederateHandle() );
		icResponseParams.put( "HLAinteractionClassList", icSubscriptions );
		
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionSubscription", 
		              icResponseParams );
		
	}
	
	private void handleFederateRequestObjectInstancesThatCanBeDeleted( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		FederateMetrics metrics = federate.getMetrics();
		
		Map<Integer,Integer> ownedInstancesPerClass = new HashMap<>();
		Repository repository = federation.getRepository();
		for( int instanceId : metrics.getObjectsOwned() )
		{
			ROCInstance instance = repository.getObject( instanceId );
			int classId = instance.getRegisteredClassHandle();
			Integer count = ownedInstancesPerClass.get( classId );
			if( count == null )
				count = 1;
			else
				count = count + 1;
			ownedInstancesPerClass.put( classId, count );
		}
		
		ObjectClassBasedCount[] ownedCounts = new ObjectClassBasedCount[ownedInstancesPerClass.size()];
		int index = 0;
		for( Entry<Integer,Integer> entry : ownedInstancesPerClass.entrySet() )
			ownedCounts[index++] = new ObjectClassBasedCount( entry.getKey(), entry.getValue() );
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federate.getFederateHandle() );
		responseParams.put( "HLAobjectInstanceCounts", ownedCounts );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesThatCanBeDeleted", 
		              responseParams );
	}

	private void handleFederateRequestObjectInstancesUpdated( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		FederateMetrics metrics = federate.getMetrics();
		ObjectClassBasedCount[] updatedCounts = metrics.getObjectInstancesUpdated();
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federate.getFederateHandle() );
		responseParams.put( "HLAobjectInstanceCounts", updatedCounts );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesUpdated", 
		              responseParams );
	}
	
	private void handleFederateRequestObjectInstancesReflected( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		FederateMetrics metrics = federate.getMetrics();
		ObjectClassBasedCount[] reflectedCounts = metrics.getObjectInstancesReflected();
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federate.getFederateHandle() );
		responseParams.put( "HLAobjectInstanceCounts", reflectedCounts );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstancesReflected", 
		              responseParams );
	}
	
	private void handleFederateRequestUpdatesSent( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		FederateMetrics metrics = federate.getMetrics();
		ObjectClassBasedCount[] updateCounts = metrics.getUpdatesSent();
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federate.getFederateHandle() );
		responseParams.put( "HLAtransportation", "HLAreliable" );
		responseParams.put( "HLAupdateCounts", updateCounts );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportUpdatesSent", 
		              responseParams );
	}
	
	private void handleFederateRequestInteractionsSent( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		FederateMetrics metrics = federate.getMetrics();
		InteractionCount[] sentCounts = metrics.getInteractionsSent();
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federate.getFederateHandle() );
		responseParams.put( "HLAtransportation", "HLAreliable" );
		responseParams.put( "HLAinteractionCounts", sentCounts );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionsSent", 
		              responseParams );
	}
	
	private void handleFederateRequestReflectionsReceived( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		FederateMetrics metrics = federate.getMetrics();
		ObjectClassBasedCount[] updateCounts = metrics.getReflectionsReceived();
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federate.getFederateHandle() );
		responseParams.put( "HLAtransportation", "HLAreliable" );
		responseParams.put( "HLAreflectCounts", updateCounts );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportReflectionsReceived", 
		              responseParams );
	}
	
	private void handleFederateRequestInteractionsReceived( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		FederateMetrics metrics = federate.getMetrics();
		InteractionCount[] sentCounts = metrics.getInteractionsReceived();
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federate.getFederateHandle() );
		responseParams.put( "HLAtransportation", "HLAreliable" );
		responseParams.put( "HLAinteractionCounts", sentCounts );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportInteractionsReceived", 
		              responseParams );
	}
	

	private void handleFederateRequestObjectInstanceInformation( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		int federateHandle = federate.getFederateHandle(); 
		int instanceHandle = (Integer)requestParams.get( "HLAobjectInstance" );
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federateHandle );
		responseParams.put( "HLAobjectInstance", instanceHandle );
		
		Repository repository = federation.getRepository(); 
		ROCInstance instance = repository.getObject( instanceHandle );
		
		if( instance != null )
		{
			OCMetadata knownAs = instance.getDiscoveredType( federateHandle );
			OCMetadata registeredAs = instance.getRegisteredType();
			if( knownAs != null )
			{
				responseParams.put( "HLAregisteredClass", registeredAs.getHandle() );
				responseParams.put( "HLAknownClass", knownAs.getHandle() );
			}
			else if( instance.isOwner(federateHandle) )
			{
				responseParams.put( "HLAregisteredClass", registeredAs.getHandle() );
				responseParams.put( "HLAknownClass", ObjectModel.INVALID_HANDLE );
			}
			
			Set<RACInstance> ownedAttributes = instance.getAllAttributesOwnedBy( federateHandle );
			int[] ownedAttributeHandles = new int[ownedAttributes.size()];
			int index = 0;
			for( RACInstance ownedAttribute : ownedAttributes )
				ownedAttributeHandles[index++] = ownedAttribute.getHandle();
			
			responseParams.put( "HLAownedInstanceAttributeList", ownedAttributeHandles );
		}
		
		if( !responseParams.containsKey("HLAownedInstanceAttributeList") )
			responseParams.put( "HLAownedInstanceAttributeList", null );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportObjectInstanceInformation", 
		              responseParams );
	}
	
	private void handleFederateRequestFomModuleData( Map<String,Object> requestParams )
		throws MomException
	{
		Federate federate = getRequestFederate( requestParams );
		
		// Check to see if the provided module index is within bounds
		int fomModuleIndex = (Integer)requestParams.get( "HLAFOMmoduleIndicator" );
		List<FomModule> modules = federate.getRawFomModules();
		int moduleCount = modules.size();
		if( fomModuleIndex > moduleCount - 1 )
			throw new MomException( "HLAFOMmoduleIndicator " + fomModuleIndex + " out of bounds", true );
		
		// Get the requested module
		FomModule module = modules.get( fomModuleIndex );
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAfederate", federate.getFederateHandle() );
		responseParams.put( "HLAFOMmoduleIndicator", fomModuleIndex );
		responseParams.put( "HLAFOMmoduleData", module.getContent() );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederate.HLAreport.HLAreportFOMmoduleData", 
		              responseParams );
	}
	
	private void handleFederationRequestSynchronizationPoints( Map<String,Object> requestParams )
		throws MomException
	{
		// Get the labels of all "in-progress" syncpoints 
		SyncPointManager syncPointMan = this.federation.getSyncPointManager();
		Collection<SyncPoint> syncPoints = syncPointMan.getAllPoints();
		syncPoints.removeIf( (SyncPoint sp)-> sp.isSynchronized() );
		
		String[] labels = new String[syncPoints.size()];
		int index = 0;
		for( SyncPoint syncPoint : syncPoints )
			labels[index++] = syncPoint.getLabel();
		
		Map<String,Object> responseParams = new HashMap<String,Object>();
		responseParams.put( "HLAsyncPoints", labels );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederation.HLAreport.HLAreportSynchronizationPoints", 
		              responseParams );
	}
	
	private void handleFederationRequestSynchronizationPointStatus( Map<String,Object> requestParams )
		throws MomException
	{
		Map<String,Object> responseParams = new HashMap<>();
		
		String name = (String)requestParams.get( "HLAsyncPointName" );
		responseParams.put( "HLAsyncPointName", name );
		
		// Get the syncpoint status for each federate
		SyncPointManager syncPointMan = this.federation.getSyncPointManager();
		SyncPoint syncPoint = syncPointMan.getPoint( name );
		
		// Spec says that if there is no syncpoint for the name, then the HLAsyncPointFederates response
		// parameter is left undefined
		if( syncPoint != null )
		{
			SynchPointFederate[] syncPointFederates = SynchPointFederate.forSyncPoint( federation, 
			                                                                           syncPoint );
			responseParams.put( "HLAsyncPointFederates", syncPointFederates );
		}
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederation.HLAreport.HLAreportSynchronizationPointStatus", 
		              responseParams );
	}
	
	private void handleFederationRequestFomModuleData( Map<String,Object> requestParams )
		throws MomException
	{
		int fomModuleIndex = (Integer)requestParams.get( "HLAFOMmoduleIndicator" );
		
		// Check to see if the provided module index is within bounds
		List<FomModule> modules = federation.getRawFomModules();
		int moduleCount = modules.size();
		if( fomModuleIndex > moduleCount - 1 )
			throw new MomException( "HLAFOMmoduleIndicator " + fomModuleIndex + " out of bounds", true );
		
		// Get the requested module
		FomModule module = modules.get( fomModuleIndex );
		
		Map<String,Object> responseParams = new HashMap<>();
		responseParams.put( "HLAFOMmoduleIndicator", fomModuleIndex );
		responseParams.put( "HLAFOMmoduleData", module.getContent() );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederation.HLAreport.HLAreportFOMmoduleData", 
		              responseParams );
	}
	
	private void handleFederationRequestMIMData( Map<String,Object> requestParams )
		throws MomException
	{
		// Create an dummy ObjectModel and inject the MIM into it
		ObjectModel mim = new ObjectModel( federation.getHlaVersion() );
		
		// All this stuff needs to be in the ObjectModel before we call ObjectModel.mommify()
		OCMetadata ocRoot = mim.newObject( "HLAobjectRoot" );
		mim.addObjectClass( ocRoot );
		mim.setObjectRoot( ocRoot );
		OCMetadata ocHlaManager = mim.newObject( "HLAmanager" );
		ocHlaManager.setParent( ocRoot );
		mim.addObjectClass( ocHlaManager );
		
		ICMetadata icRoot = mim.newInteraction( "HLAinteractionRoot" );
		mim.addInteractionClass( icRoot );
		mim.setInteractionRoot( icRoot );
		ICMetadata icHlaManager = mim.newInteraction( "HLAmanager" );
		icHlaManager.setParent( icRoot );
		mim.addInteractionClass( icHlaManager );
		
		ObjectModel.mommify( mim );
		ObjectModel.resolveSymbols( mim );
		
		// Create an XML representation of the object model
		Document mimXml = new XmlRenderer().renderFOM( mim );
		String mimString = XmlRenderer.xmlToString( mimXml );
		
		Map<String,Object> responseParams = new HashMap<>();
		responseParams.put( "HLAMIMdata", mimString );
		
		// Send the response
		sendResponse( "HLAmanager.HLAfederation.HLAreport.HLAreportMIMdata", 
		              responseParams );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Helper Convenience Methods   //////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the Federate whose handle is contained in the <code>requestParams</code> map under the
	 * "HLAfederate" key.
	 * 
	 * @param requestParams the map to search for the federate handle in
	 * @return the Federate whose handle is contained in the <code>requestParams</code> map
	 * @throws MomException if the federation does not contain a federate with the given handle
	 */
	private Federate getRequestFederate( Map<String,Object> requestParams )
		throws MomException
	{
		int federateHandle = (Integer)requestParams.get( "HLAfederate" );
		Federate federate = federation.getFederate( federateHandle );
		if( federate == null )
			throw new MomException( "invalid federate " + federateHandle, true );
		
		return federate;
	}
	
	
	/**
	 * Caches interaction metadata for HLAreportMomException.
	 * <p/>
	 * This interaction is sent if an exception was raised while trying to process MOM request 
	 * interactions from a federate.
	 */
	private void cacheReportMomExceptionMetadata()
	{
		// Get the handle from the canonical name
		int reportExceptionId = Mom.getMomInteractionHandle( CANONICAL_VERSION, 
		                                                     "HLAmanager.HLAfederate.HLAreport.HLAreportMOMexception" );
		
		// Get the interaction metadata from the federation's object model
		this.reportMomExceptionMetadata = fom().getInteractionClass( reportExceptionId );
		if( reportMomExceptionMetadata != null )
		{
			this.supportsMomException = true;
			
			// Cache parameter metadata
			Set<PCMetadata> reportParamMetadata = reportMomExceptionMetadata.getAllParameters();
			Map<Integer,byte[]> params = new HashMap<Integer,byte[]>();
			for( PCMetadata paramMetadata : reportParamMetadata )
			{
				int paramId = paramMetadata.getHandle();
				String canonicalName = Mom.getMomParameterName( CANONICAL_VERSION, 
				                                                paramId );
				if( canonicalName.equals("HLAfederate") )
					this.federateMetadata = paramMetadata;
				if( canonicalName.equals("HLAservice") )
					this.serviceMetadata = paramMetadata;
				else if( canonicalName.equals("HLAexception") )
					this.exceptionMetadata = paramMetadata;
				else if( canonicalName.equals("HLAparameterError") )
					this.parameterErrorMetadata = paramMetadata;
			}
		}
		else
		{
			// HLAreportMomException not supported under this HLA Version
			this.supportsMomException = false;
		}
	}
	
	/**
	 * Registers a handler for the provided MOM interaction name
	 * @param name the fully qualified, canonical name of the interaction (relative to HLAinteractionRoot)
	 * @param handler the handler for the interaction
	 */
	private void registerMomInteractionHandler( String name, 
	                                            ConsumerWithException<Map<String,Object>,MomException> handler )
	{
		int handle = Mom.getMomInteractionHandle( CANONICAL_VERSION, name );
		if( handle == ObjectModel.INVALID_HANDLE )
			throw new JRTIinternalError( "Not a MOM interaction: " + name );
		
		this.handlers.put( handle, handler );
	}
	
	/**
	 * Processes an incoming MOM interaction.
	 * <p/>
	 * This method will query the internal <code>handlers</code> map for the handler that corresponds to
	 * the interaction provided in the <code>request</code> parameter.
	 * <p/> 
	 * If a handler is found for the interaction, then processing is deferred to it. If a 
	 * {@link MomException} is raised while processing the interaction, a corresponding 
	 * <code>HLAreportMOMexception</code> interaction is sent, detailing the error. 
	 * <p/>
	 * If no handler is found, an appropriate message is logged.
	 * 
	 * @param request the incoming {@link SendInteraction} message to process
	 */
	private void processMomInteraction( SendInteraction request )
	{
		// Is there a handler for this interaction?
		int interactionId = request.getInteractionId();
		ConsumerWithException<Map<String,Object>,MomException> handler = 
			this.handlers.get( interactionId );
		if( handler != null )
		{
			try
			{
				// Decode the incoming parameters. This will attempt to decode all parameter
				// values based on their FOM datatype, and will map them against the parameter's
				// canonical name
				Map<String,Object> params = 
					MomEncodingHelpers.decodeInteractionParameters( CANONICAL_VERSION,
					                                                fom(),
				                                                    interactionId, 
				                                                    request.getParameters() );
				
				// Pass the params to the handler for processing!
				handler.accept( params );
			}
			catch( MomException mie )
			{
				// If an exception occurred, then send a ReportMomException interaction (if the
				// current federation version allows it)
				this.reportMomException( request, mie );
			}
		}
		else
		{
			logger.warn( "No handler for incoming MOM interaction " + interactionId );
		}
	}
	
	private ICMetadata getMomInteractionMetadata( String name )
	{
		int handle = Mom.getMomInteractionHandle( CANONICAL_VERSION, name );
		
		ObjectModel fom = federation.getFOM();
		ICMetadata metadata = fom.getInteractionClass( handle );
		if( metadata == null )
			throw new JRTIinternalError( "invalid MOM interaction: " + name );
		
		return metadata;
	}
	
	/**
	 * Reports a {@link MomException} to the federate that sent the request interaction the exception was 
	 * raised in response to. 
	 * 
	 * @param request the request that raised the exception
	 * @param exception the exception that was raised
	 */
	private void reportMomException( SendInteraction request, MomException exception )
	{
		int requestId = request.getInteractionId();
		ICMetadata requestMetadata = fom().getInteractionClass( requestId );
		String service = requestMetadata.getQualifiedName();
		String message = exception.getMessage();
		
		boolean paramError = exception.isParameterError();
		
		logger.info( "Reporting MOM Exception [Service="+service+",ParamError="+paramError+"]: "+message );
		if( this.supportsMomException )
		{
			HashMap<Integer,byte[]> params = new HashMap<Integer,byte[]>();
			params.put( federateMetadata.getHandle(), 
			            MomEncodingHelpers.encode(federateMetadata.getDatatype(), request.getSourceFederate()) );
			params.put( serviceMetadata.getHandle(), 
			            MomEncodingHelpers.encode(serviceMetadata.getDatatype(), service) );
			params.put( exceptionMetadata.getHandle(), 
			            MomEncodingHelpers.encode(exceptionMetadata.getDatatype(), message) );
			params.put( parameterErrorMetadata.getHandle(), 
			            MomEncodingHelpers.encode(parameterErrorMetadata.getDatatype(), paramError) );
			
			SendInteraction response = new SendInteraction( reportMomExceptionMetadata.getHandle(), 
			                                                null, 
			                                                params );
			response.setIsFromRti( true );
			federation.queueDataMessage( response, null );
		}
		else
		{
			logger.warn( "Did not report MOM Exception as the federation version does not support it" );
		}
	}
	
	/**
	 * Sends the specified response interaction, encoding the provided parameters into wire format.
	 * 
	 * @param responseName the qualified, canonical name of the interaction to send, relative from 
	 *                     <code>HLAinteractionRoot</code>.
	 * @param params the parameter values to send, mapped against their canonical parameter names
	 */
	private void sendResponse( String responseName, Map<String,Object> params )
	{
		ICMetadata responseMetadata = getMomInteractionMetadata( responseName );
		int responseHandle = responseMetadata.getHandle();
		
		Set<Integer> interestedFederates = interests.getAllSubscribers( responseMetadata );
		if( interestedFederates.size() == 0 )
		{
			this.logger.warn( "Not sending MOM interaction " + responseMetadata + 
			                  " as no federates subscribe to it" );
			return;
		}
		
		// Encode the parameter values into wire format
		HashMap<Integer,byte[]> hlaParams =
		    MomEncodingHelpers.encodeInteractionParameters( CANONICAL_VERSION, responseMetadata, params );

		// Create the response object
		SendInteraction response = new SendInteraction( responseHandle, null, hlaParams );
		response.setIsFromRti( true );
		response.setTargetFederates( interestedFederates );

		if( logger.isDebugEnabled() )
		{
			logger.debug( "Sent MOM response interaction " +
			              responseMetadata.getQualifiedName() + " with " + hlaParams.size() +
			              " param(s)" );
		}

		// Send the response!
		federation.queueDataMessage( response, null );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	/**
	 * Consumer extension that allows the consumer method to throw a checked exception.
	 * 
	 * @param <T> the datatype the consumer accepts
	 * @param <E> the checked exception that the consumer may throw
	 */
	@FunctionalInterface
	private interface ConsumerWithException<T,E extends Exception>
	{
		public void accept( T t ) throws E;
	}	
}
