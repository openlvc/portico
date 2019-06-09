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
package org.portico2.aaa;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.portico.impl.hla1516e.types.time.DoubleTime;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.NullFederateAmbassador;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RTIambassador;
import hla.rti1516e.ResignAction;
import hla.rti1516e.RtiFactoryFactory;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.encoding.ByteWrapper;
import hla.rti1516e.exceptions.FederatesCurrentlyJoined;
import hla.rti1516e.exceptions.FederationExecutionAlreadyExists;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAfloat64TimeFactory;

public class Federate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String localSettings;
	
	private RTIambassador rtiamb;
	private FedAmb fedamb;
	private HLAfloat64TimeFactory timeFactory;
	private String federation;
	private String federate;
		
	private double federateTime;
	private double federateLookahead;
	private boolean regulating;
	private boolean constrained;
	
	private Set<String> announcedSet;
	private Set<String> synchronizedSet;
	private ObjectInstanceHandle ourObject;
	private Map<ObjectInstanceHandle,Long> discoveredObjects; // value: last updated timestamp
	private Set<ObjectInstanceHandle> deletedObjects;
	private Set<InteractionInstance> receivedInteractions; // Interactions received in last call to tick()

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Federate( String localSettings )
	{
		this.localSettings = localSettings;

		this.rtiamb = null;
		this.fedamb = new FedAmb();
		this.timeFactory = null;
		this.federation = "unknown";
		this.federate = "unknown";
		
		this.federateTime = 0.0;
		this.federateLookahead = 1.0;
		this.regulating = false;
		this.constrained = false;
		
		this.announcedSet = new HashSet<>();
		this.synchronizedSet = new HashSet<>();
		this.ourObject = null;
		this.discoveredObjects = new HashMap<>();
		this.deletedObjects = new HashSet<>();
		this.receivedInteractions = new HashSet<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////
	///  HLA Methods   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	public void createAndJoin( String federate, String federation ) throws Exception
	{
		// Create the RTIambassador
		this.rtiamb = RtiFactoryFactory.getRtiFactory().getRtiAmbassador();
		
		// Connect to the RTI
		this.rtiamb.connect( this.fedamb, CallbackModel.HLA_IMMEDIATE, this.localSettings );
		this.timeFactory = (HLAfloat64TimeFactory)rtiamb.getTimeFactory();
		
		this.federation = federation;
		this.federate = federate;
		
		// Try to create the federation
		try
		{
			URL fom = new File( "codebase/resources/test-data/fom/ieee1516e/testfom.xml" ).toURI().toURL();
			this.rtiamb.createFederationExecution( federation, fom );
		}
		catch( FederationExecutionAlreadyExists feae )
		{
			// No-op
			log( "Federation already exists: "+federation );
		}
		catch( Exception e )
		{
			throw e;
		}
		
		this.rtiamb.joinFederationExecution( federate, federation );
	}
	
	public void registerSyncPoint( String point ) throws Exception
	{
		this.rtiamb.registerFederationSynchronizationPoint( point, new byte[]{} );
	}
	
	public void waitForAnnounce( String point ) throws Exception
	{
		boolean result = tick( 5000, () -> { return announcedSet.contains(point); } );
		if( result == false )
			throw new Exception( "{"+federate+"} WAITING FOR SYNC POINT ANNOUNCE HAS FAILED!" );
	}
	
	public void achieve( String point ) throws Exception
	{
		this.rtiamb.synchronizationPointAchieved( point );
	}
	
	public void waitForSynchronized( String point ) throws Exception
	{
		long end = System.currentTimeMillis()+5000;
		while( this.synchronizedSet.contains(point) == false )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 1.0 );
			if( System.currentTimeMillis() > end )
				throw new Exception( "{"+federate+"} WAITING FOR FEDERATION SYCHRONIZE" );
		}
	}
	
	public void publishAndSubscribe() throws Exception
	{
		// Object
		ObjectClassHandle classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.A.B" );
		AttributeHandle   aaHandle    = rtiamb.getAttributeHandle( classHandle, "aa" );
		AttributeHandle   abHandle    = rtiamb.getAttributeHandle( classHandle, "ab" );
		AttributeHandle   acHandle    = rtiamb.getAttributeHandle( classHandle, "ac" );
		AttributeHandle   baHandle    = rtiamb.getAttributeHandle( classHandle, "ba" );
		AttributeHandle   bbHandle    = rtiamb.getAttributeHandle( classHandle, "bb" );
		AttributeHandle   bcHandle    = rtiamb.getAttributeHandle( classHandle, "bc" );
		AttributeHandleSet attributes = rtiamb.getAttributeHandleSetFactory().create();
		attributes.add( aaHandle );
		attributes.add( abHandle );
		attributes.add( acHandle );
		attributes.add( baHandle );
		attributes.add( bbHandle );
		attributes.add( bcHandle );

		rtiamb.publishObjectClassAttributes( classHandle, attributes );
		rtiamb.subscribeObjectClassAttributes( classHandle, attributes );
		
		// Interaction
		InteractionClassHandle iHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.X.Y" );
		rtiamb.publishInteractionClass( iHandle );
		rtiamb.subscribeInteractionClass( iHandle );
	}
	
	public void subscribeInteractionClass( String className ) throws Exception
	{
		InteractionClassHandle iHandle = rtiamb.getInteractionClassHandle( className );
		rtiamb.subscribeInteractionClass( iHandle );
	}
	
	public void publishInteractionClass( String className ) throws Exception
	{
		InteractionClassHandle iHandle = rtiamb.getInteractionClassHandle( className );
		rtiamb.publishInteractionClass( iHandle );
	}
	
	public void unpublishAndUnsubscribe() throws Exception
	{
		// Object
		ObjectClassHandle classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.A.B" );
		rtiamb.unpublishObjectClass( classHandle );
		rtiamb.unsubscribeObjectClass( classHandle );
		
		// Interaction
		InteractionClassHandle iHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.X.Y" );
		rtiamb.unpublishInteractionClass( iHandle );
		rtiamb.unsubscribeInteractionClass( iHandle );
	}
	
	public void unsubscribeInteractionClass( String className ) throws Exception
	{
		InteractionClassHandle iHandle = rtiamb.getInteractionClassHandle( className );
		rtiamb.unpublishInteractionClass( iHandle );
	}
	
	public void unpublishInteractionClass( String className ) throws Exception
	{
		InteractionClassHandle iHandle = rtiamb.getInteractionClassHandle( className );
		rtiamb.unpublishInteractionClass( iHandle );
	}
	
	public ObjectInstanceHandle registerObject() throws Exception
	{
		ObjectClassHandle classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.A.B" );
		this.ourObject = rtiamb.registerObjectInstance( classHandle, "obj-"+federate );
		log( "Registered instance: handle="+this.ourObject );
		return ourObject;
	}
	
	public void waitForDiscovery( ObjectInstanceHandle objectHandle ) throws Exception
	{
		boolean result = tick( 5000, () -> { return discoveredObjects.containsKey(objectHandle); } );
		if( result == false )
			throw new Exception( "{"+federate+"} WAITING FOR DISCOVERY OF "+objectHandle );
	}
	
	public InteractionInstance waitForInteraction( String name ) throws Exception
	{
		final InteractionClassHandle handle = this.rtiamb.getInteractionClassHandle( name );
		boolean result = tick( 5000, () -> {
			boolean hasInteraction = false;
			for( InteractionInstance interaction : receivedInteractions )
			{
				if( interaction.getInteractionClass().equals(handle) )
				{
					hasInteraction = true;
					break;
				}
			}
			
			return hasInteraction;
		});
		
		if( result )
		{
			InteractionInstance theInstance = null;
			for( InteractionInstance interaction : receivedInteractions )
			{
				if( interaction.getInteractionClass().equals(handle) )
				{
					theInstance = interaction;
					break;
				}
			}
			
			return theInstance;
		}
		else
		{
			throw new Exception( "{"+federate+"} WAITING FOR RECEIPT OF "+name );
		}
	}
	
	public void updateObject() throws Exception
	{
		///////////////////////////////////////////////
		// create the necessary container and values //
		///////////////////////////////////////////////
		// create a new map with an initial capacity - this will grow as required
		AttributeHandleValueMap attributes = rtiamb.getAttributeHandleValueMapFactory().create(6);

		ObjectClassHandle classHandle = rtiamb.getObjectClassHandle( "ObjectRoot.A.B" );
		AttributeHandle   aaHandle    = rtiamb.getAttributeHandle( classHandle, "aa" );
		AttributeHandle   abHandle    = rtiamb.getAttributeHandle( classHandle, "ab" );
		AttributeHandle   acHandle    = rtiamb.getAttributeHandle( classHandle, "ac" );
		AttributeHandle   baHandle    = rtiamb.getAttributeHandle( classHandle, "ba" );
		AttributeHandle   bbHandle    = rtiamb.getAttributeHandle( classHandle, "bb" );
		AttributeHandle   bcHandle    = rtiamb.getAttributeHandle( classHandle, "bc" );		
		attributes.put( aaHandle, "aa".getBytes() );
		attributes.put( abHandle, "ab".getBytes() );
		attributes.put( acHandle, "ac".getBytes() );
		attributes.put( baHandle, "ba".getBytes() );
		attributes.put( bbHandle, "bb".getBytes() );
		attributes.put( bcHandle, "bc".getBytes() );
		
		//////////////////////////
		// do the actual update //
		//////////////////////////
		rtiamb.updateAttributeValues( ourObject, attributes, new byte[]{} );
		
		// note that if you want to associate a particular timestamp with the
		// update. here we send another update, this time with a timestamp:
		HLAfloat64Time time = timeFactory.makeTime( federateTime + federateLookahead );
		rtiamb.updateAttributeValues( ourObject, attributes, new byte[]{}, time );
	}	

	public void waitForReflection( ObjectInstanceHandle objectHandle,
	                               long earliestTime ) throws Exception
	{
		boolean result = tick( 5000, () -> { return discoveredObjects.containsKey(objectHandle) &&
			                                        discoveredObjects.get(objectHandle) >= earliestTime; } );
		
		if( result == false )
			throw new Exception( "{"+federate+"} WAITING FOR REFLECTION OF "+objectHandle );
	}

	public void deleteObject( ObjectInstanceHandle objectHandle ) throws Exception
	{
		rtiamb.deleteObjectInstance( objectHandle, new byte[]{} );
	}
	
	public void waitForDelete( ObjectInstanceHandle objectHandle ) throws Exception
	{
		boolean result = tick( 5000, () -> { return deletedObjects.contains(objectHandle); } );
		if( result == false )
			throw new Exception( "{"+federate+"} WAITING FOR DELETE OF "+objectHandle );
	}
	
	public void sendInteraction() throws Exception
	{
		InteractionClassHandle iHandle = rtiamb.getInteractionClassHandle( "InteractionRoot.X.Y" );
		ParameterHandleValueMap parameters = rtiamb.getParameterHandleValueMapFactory().create(0);
		rtiamb.sendInteraction( iHandle, parameters, new byte[]{} );
		
		// if you want to associate a particular timestamp with the
		// interaction, you will have to supply it to the RTI. Here
		// we send another interaction, this time with a timestamp:
		HLAfloat64Time time = timeFactory.makeTime( federateTime + federateLookahead );
		rtiamb.sendInteraction( iHandle, parameters, new byte[]{}, time );
	}
	
	public void sendInteraction( String name ) throws Exception
	{
		Map<String,byte[]> parameters = new HashMap<>();
		sendInteraction( name, parameters );
	}
	
	public void sendInteraction( String name, String paramName, byte[] paramValue ) throws Exception
	{
		Map<String,byte[]> parameters = new HashMap<>();
		parameters.put( paramName, paramValue );
		sendInteraction( name, parameters );
	}
	
	public void sendInteraction( String name, Map<String,byte[]> parameters ) throws Exception
	{
		InteractionClassHandle iHandle = rtiamb.getInteractionClassHandle( name );
		
		ParameterHandleValueMap hlaParams = rtiamb.getParameterHandleValueMapFactory().create( parameters.size() );
		for( Entry<String,byte[]> entry : parameters.entrySet() )
		{
			ParameterHandle pHandle = rtiamb.getParameterHandle( iHandle, entry.getKey() );
			hlaParams.put( pHandle, entry.getValue() );
		}
		
		rtiamb.sendInteraction( iHandle, hlaParams, null );
	}
	
	public void enableTimePolicy() throws Exception
	{
		rtiamb.enableAsynchronousDelivery();
		
		rtiamb.enableTimeConstrained();
		boolean result = tick( 5000, () -> { return constrained; } );
		if( result == false )
			throw new Exception( "{"+federate+"} TIMEOUT WAITING FOR CONSTRAINED ENABLED" );

		rtiamb.enableTimeRegulation( timeFactory.makeInterval(federateLookahead) );
		result = tick( 5000, () -> { return regulating; } );
		if( result == false )
			throw new Exception( "{"+federate+"} TIMEOUT WAITING FOR REGULATING ENABLED" );
	}
	
	public void requestTimeAdvance( double next ) throws Exception
	{
		rtiamb.timeAdvanceRequest( timeFactory.makeTime(next) );
	}
	
	public void waitForTimeAdvance( double next ) throws Exception
	{
		boolean result = tick( 5000, () -> { return next <= federateTime; } );
		if( result == false )
			throw new Exception( "{"+federate+"} TIMEOUT WAITING FOR TIME ADVANCE" );
	}

	public void resignAndDestroy() throws Exception
	{
		this.rtiamb.resignFederationExecution( ResignAction.NO_ACTION );
		
		try
		{
			this.rtiamb.destroyFederationExecution( federation );
		}
		catch( FederatesCurrentlyJoined fcj )
		{
			log( "Canont destroy federation ["+federation+"] - federates are still joined" );
		}
		
		this.rtiamb.disconnect();
	}

	public void tick( long maxMillis ) throws Exception
	{
		long end = System.currentTimeMillis()+maxMillis;
		while( System.currentTimeMillis() < end )
		{
			rtiamb.evokeMultipleCallbacks( 0.1, 1.0 );
			Thread.sleep( 100 );
		}
	}
	
	public FederateHandle decodeFederateHandle( ByteWrapper wrapper ) throws Exception
	{
		FederateHandle handle = rtiamb.getFederateHandleFactory().decode( wrapper.array(), 
		                                                                  wrapper.getPos() );
		wrapper.advance( handle.encodedLength() );
		return handle;
	}
	
	/////////////////////////////////////////////////////
	/// Convenience Helper Methods  /////////////////////
	/////////////////////////////////////////////////////
	private void log( String format, String... args )
	{
		System.out.println( String.format("{%s} "+format, federate, args) );
	}

	private boolean tick( long maxMillis, BooleanSupplier test ) throws Exception
	{
		long end = System.currentTimeMillis()+maxMillis;
		while( test.getAsBoolean() == false )
		{
			this.receivedInteractions.clear();
			rtiamb.evokeMultipleCallbacks( 0.1, 1.0 );
			Thread.sleep( 100 );
			if( System.currentTimeMillis() > end )
				break;
		}
	
		return test.getAsBoolean();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////
	///  Federate Ambassador   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////
	private class FedAmb extends NullFederateAmbassador
	{
		@Override
		public void synchronizationPointRegistrationSucceeded( String label )
		{
			log( "Sync point registration SUCCESS: "+label );
		}

		@Override
		public void synchronizationPointRegistrationFailed( String label,
		                                                    SynchronizationPointFailureReason reason )
		{
			log( "Sync point registration FAILURE: "+label );
		}

		@Override
		public void announceSynchronizationPoint( String label, byte[] tag )
		{
			log( "Sync point announced: "+label );
			announcedSet.add( label );
		}

		@Override
		public void federationSynchronized( String label, FederateHandleSet failedToSyncSet )
		{
			log( "Federation synchronized: "+label );
			synchronizedSet.add( label );
		}
		
		///////////////////////////////////////////////////
		/// Interaction Handling //////////////////////////
		///////////////////////////////////////////////////
		@Override
        public void receiveInteraction( InteractionClassHandle classHandle,
                                        ParameterHandleValueMap theParameters,
                                        byte[] tag,
                                        OrderType sentOrdering,
                                        TransportationTypeHandle theTransport,
                                        SupplementalReceiveInfo receiveInfo )
        {
			log( "RECEIVED INTERACTION: "+classHandle );
			Map<String,byte[]> params = new HashMap<>();
			for( Entry<ParameterHandle,byte[]> hlaParam : theParameters.entrySet() )
			{
				try
				{
					String name = rtiamb.getParameterName( classHandle, hlaParam.getKey() );
					params.put( name, hlaParam.getValue() );
				}
				catch( Exception e )
				{
					e.printStackTrace();
				}
			}
			receivedInteractions.add( new InteractionInstance(classHandle, params) );
        }

		@Override
        public void receiveInteraction( InteractionClassHandle interactionClass,
                                        ParameterHandleValueMap theParameters,
                                        byte[] tag,
                                        OrderType sentOrdering,
                                        TransportationTypeHandle theTransport,
                                        LogicalTime theTime,
                                        OrderType receivedOrdering,
                                        SupplementalReceiveInfo receiveInfo )
        {
			receiveInteraction( interactionClass, theParameters, tag, sentOrdering, theTransport, receiveInfo );
        }
        
		@Override
        public void receiveInteraction( InteractionClassHandle interactionClass,
                                        ParameterHandleValueMap theParameters,
                                        byte[] tag,
                                        OrderType sentOrdering,
                                        TransportationTypeHandle theTransport,
                                        LogicalTime theTime,
                                        OrderType receivedOrdering,
                                         MessageRetractionHandle retractionHandle,
                                        SupplementalReceiveInfo receiveInfo )
        {
			receiveInteraction( interactionClass, theParameters, tag, sentOrdering, theTransport, receiveInfo );
        }

		///////////////////////////////////////////////////
		/// Object Handling ///////////////////////////////
		///////////////////////////////////////////////////
		@Override
		public void discoverObjectInstance( ObjectInstanceHandle theObject,
		                                    ObjectClassHandle theObjectClass,
		                                    String objectName )
		{
			log( "DISCOVER: handle="+theObject+", class="+theObjectClass+", name="+objectName );
			discoveredObjects.put( theObject, System.currentTimeMillis() );
		}

		@Override
		public void discoverObjectInstance( ObjectInstanceHandle theObject,
		                                    ObjectClassHandle theObjectClass,
		                                    String objectName,
		                                    FederateHandle producingFederate )
		{
			discoverObjectInstance( theObject, theObjectClass, objectName );
		}

		@Override
		public void reflectAttributeValues( ObjectInstanceHandle theObject,
		                                    AttributeHandleValueMap theAttributes,
		                                    byte[] tag,
		                                    OrderType sentOrdering,
		                                    TransportationTypeHandle theTransport,
		                                    SupplementalReflectInfo reflectInfo )
		{
			log( "REFLECT: handle="+theObject+", attributes="+theAttributes.keySet() );
			discoveredObjects.put( theObject, System.currentTimeMillis() );
		}

		@Override
		public void reflectAttributeValues( ObjectInstanceHandle theObject,
		                                    AttributeHandleValueMap theAttributes,
		                                    byte[] tag,
		                                    OrderType sentOrdering,
		                                    TransportationTypeHandle theTransport,
		                                    LogicalTime theTime,
		                                    OrderType receivedOrdering,
		                                    SupplementalReflectInfo reflectInfo )
		{
			reflectAttributeValues( theObject, theAttributes, tag, sentOrdering, theTransport, reflectInfo );
		}

		@Override
		public void reflectAttributeValues( ObjectInstanceHandle theObject,
		                                    AttributeHandleValueMap theAttributes,
		                                    byte[] tag,
		                                    OrderType sentOrdering,
		                                    TransportationTypeHandle theTransport,
		                                    LogicalTime theTime,
		                                    OrderType receivedOrdering,
		                                    MessageRetractionHandle retractionHandle,
		                                    SupplementalReflectInfo reflectInfo )
		{
			reflectAttributeValues( theObject, theAttributes, tag, sentOrdering, theTransport, reflectInfo );
		}

		public void removeObjectInstance( ObjectInstanceHandle theObject,
		                                  byte[] tag,
		                                  OrderType sentOrdering,
		                                  SupplementalRemoveInfo removeInfo )
		{
			log( "REMOVE: handle="+theObject );
			deletedObjects.add( theObject );
		}

		public void removeObjectInstance( ObjectInstanceHandle theObject,
		                                  byte[] tag,
		                                  OrderType sentOrdering,
		                                  LogicalTime theTime,
		                                  OrderType receivedOrdering,
		                                  SupplementalRemoveInfo removeInfo )
		{
			removeObjectInstance( theObject, tag, sentOrdering, removeInfo );
		}

		public void removeObjectInstance( ObjectInstanceHandle theObject,
		                                  byte[] tag,
		                                  OrderType sentOrdering,
		                                  LogicalTime theTime,
		                                  OrderType receivedOrdering,
		                                  MessageRetractionHandle retractionHandle,
		                                  SupplementalRemoveInfo removeInfo )
		{
			removeObjectInstance( theObject, tag, sentOrdering, removeInfo );
		}
		
		///////////////////////////////////////////////////
		/// Object Handling ///////////////////////////////
		///////////////////////////////////////////////////
		public void timeRegulationEnabled( LogicalTime time )
		{
			regulating = true;
			federateTime = ((DoubleTime)time).getTime();
			log( "REGULATING ENABLED: time="+federateTime );

		}

		public void timeConstrainedEnabled( LogicalTime time )
		{
			constrained = true;
			federateTime = ((DoubleTime)time).getTime();
			log( "CONSTRAINED ENABLED: time="+federateTime );
		}

		public void timeAdvanceGrant( LogicalTime time )
		{
			federateTime = ((DoubleTime)time).getTime();
			log( "TIME ADVANCE GRANT: time="+federateTime );
		}
	}
}
