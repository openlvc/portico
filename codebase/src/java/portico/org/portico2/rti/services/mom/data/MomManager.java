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
package org.portico2.rti.services.mom.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.object.data.ROCInstance;
import org.portico2.rti.services.object.data.Repository;
import org.portico2.rti.services.time.data.TimeManager;

/**
 * The MOM manager takes care of all the MOM related tasks for a {@link Federation}.
 * <p/>
 * When the MOM manager is created, it will create a <code>HLAmanager.HLAfederation</code> to
 * represent the federation in the federation's object repository.
 * <p/>
 * The MomManager will also create <code>HLAmanager.HLAfederate</code> for federates that join the
 * federation
 * <p/>
 * <b>NOTE:</b> The methods in this class will only run if {@link PorticoConstants#isMomEnabled()}
 * returns <code>true</code> <i>at the time the LRC was initially created</i>. This generally
 * means that the MOM has to be enabled or disabled through the RID.
 */
public class MomManager implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/**
	 * Mom elements have different names based on which version of the spec the sending federate
	 * implements. To make things more manageable, the manager normalizes all names to a canonical
	 * version for processing.
	 */
	public static final HLAVersion CANONICAL_VERSION = HLAVersion.IEEE1516e;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean enabled;
	private Federation federation;
	private HLAVersion version;
	private MomFederation momFederation;
	private Logger logger;

	// this flag is used to stop discovery notifications being sent during a federation restore
	// we re-populate the momFederation from the lrcState and use the federateJoinedFederation
	// to create the MomFedeate objects, but we don't want to send discovery notices.
	private boolean isRestore;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomManager( Federation federation )
	{
		this.enabled = PorticoConstants.isMomEnabled();
		this.logger = federation.getLogger();
		this.federation = federation;
		this.version = federation.getHlaVersion();
		this.isRestore = false;
		
		// Create the object for this Federation
		if( this.enabled )
			this.createFederationObjectInstance();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private void createFederationObjectInstance()
	{
		int federationHandle =
		    Mom.getMomObjectClassHandle( CANONICAL_VERSION, "HLAmanager.HLAfederation" );
		ObjectModel fom = this.federation.getFOM();
		OCMetadata federationClass = fom.getObjectClass( federationHandle );

		// create a HLA object instance for the federation
		Repository repository = this.federation.getRepository();
		ROCInstance instance =
		    repository.createObject( federationClass, 
		                             federation.getFederationName(),
		                             PorticoConstants.RTI_HANDLE,
		                             federationClass.getAllAttributeHandles() );
		repository.addObject( instance );

		// wrap the HLA object instance in a MomFederation so we can track it
		this.momFederation = new MomFederation( this.federation, instance, this.logger );
	}

	private void queueManycast( PorticoMessage message, Set<Integer> targets )
	{
		message.setSourceFederate( PorticoConstants.RTI_HANDLE );
		message.setTargetFederates( targets );
		federation.queueControlMessage( message );
	}
	
	/**
	 * Generates an {@link UpdateAttributes} message for the MOM object representing the
	 * federation. It only includes the attributes provided in the given set.
	 */
	public void updateMomObject( int instanceId,
	                             Set<Integer> attributes ) throws JAttributeNotDefined
	{
		if( !this.enabled )
			return;

		UpdateAttributes update = null;
		if( instanceId == this.momFederation.getObjectIntanceHandle() )
		{
			// Object instance is the federation
			update = momFederation.generateUpdate( this.version, attributes );
		}
		else
		{
			// Object instance must be a federate, so search for the Federate instance with the given 
			// handle
			MomFederate theFederate = null;
			Set<MomFederate> federates = this.momFederation.getFederates();
			for( MomFederate federate : federates )
			{
				if( federate.getFederateObjectInstanceHandle() == instanceId )
				{
					theFederate = federate;
					break;
				}
			}

			if( theFederate != null )
				update = theFederate.generateUpdate( attributes );
		}

		if( update != null )
			federation.queueDataMessage( update, null );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Federation Event Handlers   ///////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void joinedFederation( Federate federate )
	{
		if( !this.enabled )
			return;

		int federateHandle = Mom.getMomObjectClassHandle( CANONICAL_VERSION, "HLAmanager.HLAfederate" );
		ObjectModel fom = this.federation.getFOM();
		OCMetadata federateClass = fom.getObjectClass( federateHandle );

		// create a HLA object instance for the federate
		Repository repository = this.federation.getRepository();
		ROCInstance instance = repository.createObject( federateClass, 
		                                                federate.getFederateName(),
		                                                PorticoConstants.RTI_HANDLE,
		                                                federateClass.getAllAttributeHandles() );
		repository.addObject( instance );

		// wrap the HLA object instance in a MomFederation so we can track it
		MomFederate momFederate = new MomFederate( federate, 
		                                           instance, 
		                                           federation.getHlaVersion(),
		                                           federation.getTimeManager(),
		                                           this.logger );
		this.momFederation.addFederate( momFederate );

		if( this.isRestoring() )
			return;

		//
		// Step 3. Notify federates with a subscription interest
		//
		InterestManager interests = federation.getInterestManager();
		OCMetadata federateMetadata = instance.getRegisteredType();
		Map<Integer,OCMetadata> subscriptions =
		    interests.getAllSubscribersWithTypes( federateMetadata );
		
		for( Entry<Integer,OCMetadata> subscriberEntry : subscriptions.entrySet() )
		{
			int subscriberHandle = subscriberEntry.getKey();
			OCMetadata subscriberType = subscriberEntry.getValue();
			instance.discover( subscriberHandle, subscriberType );
			this.objectDiscovered( subscriberHandle, instance );
		}

		if( subscriptions.size() > 0 )
		{
			DiscoverObject discover = new DiscoverObject( instance );
			this.queueManycast( discover, subscriptions.keySet() );
		}
	}

	public void resignedFederation( Federate federate )
	{
		if( !this.enabled )
			return;

		// Remove federate object from our internal mom federation
		int federateHandle = federate.getFederateHandle();
		MomFederate removed = this.momFederation.removeFederate( federateHandle );

		// Remove the federate object instance from the repository
		Repository repository = this.federation.getRepository();
		int objectInstanceHandle = removed.getFederateObjectInstanceHandle();
		ROCInstance removeInstance = repository.deleteObject( objectInstanceHandle );

		// Notify federates with a subscription interest of the deletion
		DeleteObject delete = new DeleteObject( objectInstanceHandle, new byte[0] );
		Set<Integer> discoverers = removeInstance.getDiscoverers();
		for( int discoverer : discoverers )
			objectRemoved( discoverer, removeInstance );

		this.queueManycast( delete, removeInstance.getDiscoverers() );
	}

	public void interactionSent( int sender, ICMetadata interaction )
	{
		if( !this.enabled )
			return;

		// Interaction sent by the sender
		if( sender != PorticoConstants.RTI_HANDLE )
		{
			int handle = interaction.getHandle();
			Federate senderFederate = federation.getFederate( sender );
			if( senderFederate != null )
				senderFederate.getMetrics().interactionSent( handle );
		}
	}

	public void interactionReceived( int receiver, ICMetadata interaction )
	{
		if( !this.enabled )
			return;

		int handle = interaction.getHandle(); // TODO Handle that the receiver will receive it as?
		Federate receiverFederate = federation.getFederate( receiver );
		if( receiverFederate != null )
			receiverFederate.getMetrics().interactionReceived( handle );
	}

	public void objectRegistered( int creator, ROCInstance instance )
	{
		if( !this.enabled )
			return;

		if( creator != PorticoConstants.RTI_HANDLE )
		{
			Federate senderFederate = federation.getFederate( creator );
			if( senderFederate != null )
				senderFederate.getMetrics().objectRegistered( instance.getHandle() );
		}
	}

	public void objectDiscovered( int discoverer, ROCInstance instance )
	{
		if( !this.enabled )
			return;

		Federate subscriberFederate = federation.getFederate( discoverer );
		if( subscriberFederate != null )
			subscriberFederate.getMetrics().objectDiscovered();
	}

	public void objectUpdated( int updator, ROCInstance instance )
	{
		if( !this.enabled )
			return;

		if( updator != PorticoConstants.RTI_HANDLE )
		{
			Federate updatingFederate = federation.getFederate( updator );
			if( updatingFederate != null )
			{
				updatingFederate.getMetrics().sentUpdate( instance.getRegisteredClassHandle(),
				                                          instance.getHandle() );
			}
		}
	}

	public void objectReflected( int reflector, ROCInstance instance )
	{
		if( !this.enabled )
			return;

		Federate reflectingFederate = federation.getFederate( reflector );
		if( reflectingFederate != null )
		{
			OCMetadata discoveredAs = instance.getDiscoveredType( reflector );
			reflectingFederate.getMetrics().reflectionReceived( discoveredAs.getHandle(),
			                                                    instance.getHandle() );
		}
	}

	public void objectDeleted( int deletor, ROCInstance instance )
	{
		if( !this.enabled )
			return;

		if( deletor != PorticoConstants.RTI_HANDLE )
		{
			Federate deletingFederate = federation.getFederate( deletor );
			if( deletingFederate != null )
				deletingFederate.getMetrics().objectDeleted( instance.getHandle() );
		}
	}

	public void objectRemoved( int remover, ROCInstance instance )
	{
		if( !this.enabled )
			return;

		Federate removingFederate = federation.getFederate( remover );
		if( removingFederate != null )
			removingFederate.getMetrics().objectRemoved();
	}
	
	// NOTE: This method has not been tested yet as the necessary mechanism to trigger it is not in place
	public void federateLost( int federate, String faultDescription )
	{
		if( !this.enabled )
			return;
		
		// NOTE: Assumes the federate has not been removed yet
		TimeManager timeManager = federation.getTimeManager();
		Federate lostFederate = federation.getFederate( federate );
		
		Map<String,Object> params = new HashMap<>();
		params.put( "HLAfederate", federate );
		params.put( "HLAfederateName", lostFederate.getFederateName() );
		params.put( "HLAfederateType", lostFederate.getFederateType() );
		params.put( "HLAtimeStamp", timeManager.getCurrentTime(federate) );
		params.put( "HLAfaultDescription", faultDescription );
		int federateLostHandle = Mom.getMomInteractionHandle( CANONICAL_VERSION,
		                                                      "HLAmanager.HLAfederate.HLAreport.HLAreportFederateLost");
		ICMetadata federateLostIc = federation.getFOM().getInteractionClass( federateLostHandle );
		HashMap<Integer,byte[]> hlaParams = 
			MomEncodingHelpers.encodeInteractionParameters( federation.getHlaVersion(), 
			                                                federateLostIc, 
			                                                params );
		SendInteraction sendInteraction = new SendInteraction( federateLostHandle, null, hlaParams );
		sendInteraction.setIsFromRti( true );
		federation.queueDataMessage( sendInteraction, null );
		
		// TODO check that this is all kosher
		// Clean up the MOM object that was assigned to the lost federate and advise everyone listening
		// that it has been deleted
		this.resignedFederation( lostFederate );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Save/Restore Methods   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		// do nothing
	}

	@Override
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		if( !this.enabled )
			return;
		
		// clear the local status and for each federate in the federation (which should have
		// already been save/restored), create a new MomFederate
		try
		{
			this.isRestore = true;
			momFederation.clear();
			for( int federateHandle : federation.getFederateHandles() )
			{
				Federate federate = federation.getFederate( federateHandle );
				joinedFederation( federate );
			}
		}
		finally
		{
			this.isRestore = false;
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean isRestoring()
	{
		return this.isRestore;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
