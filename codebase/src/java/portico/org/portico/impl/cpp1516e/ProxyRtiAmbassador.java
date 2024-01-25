/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.cpp1516e;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
 

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.CallbackModel;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.MessageRetractionReturn;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ResignAction;
import hla.rti1516e.TimeQueryReturn;
import hla.rti1516e.exceptions.AlreadyConnected;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.impl.hla1516e.Rti1516eAmbassadorEx;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleValueMap;
import org.portico.impl.hla1516e.types.HLA1516eDimensionHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eFederateHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMap;
import org.portico.impl.hla1516e.types.HLA1516eRegionHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eTransportationTypeHandleFactory;
import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.impl.hla1516e.types.time.DoubleTimeInterval;

import org.portico.lrc.model.datatype.*;
 

/**
 * This class is provided as the simplified JNI link to C++ code in the interface binding.
 * When a new "JavaRTI" is created in the C++ binding, it instantiates a new instance of
 * this class. The methods provided by this class are simplified versions of those provided
 * in the RTIambassdor. They pass basic primitive types in an effort to make the JNI code
 * on the C++ side simpler.
 * <p/>
 * Inside this class is an actual Java RTIambassador, with calls to the methods of this class
 * being translated approprirately before being passed to it.
 * <p/>
 * <b>The Federate Ambassador</b>
 * <p/>
 * When a new proxy is created and the C++ side attempts to join a federation, passing a
 * reference to its FederateAmbassador, that reference will NOT be passed over the JNI
 * boundary. Rather, an ID representing the C++ JavaRTI instance will be provided, and a
 * new {@link ProxyFederateAmbassador}} created with that ID. When the local java proxy
 * receives callbacks, it passes them over the JNI boundary with the ID so that on the C++
 * side we can locate the associated JavaRTI, and through it, the C++ FederateAmbassador.
 * The callback is then made.
 */
public class ProxyRtiAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Rti1516eAmbassadorEx rtiamb;
	private ProxyFederateAmbassador fedamb;
	private int id;
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new proxy RTIambassador that will be invoked on from C++
	 * 
	 * @param id The unique id representing the C++ federate
	 * @throws Exception if there is a problem constructing the RTIambassador
	 */
	public ProxyRtiAmbassador( int id ) throws Exception
	{
		// store the id
		this.id = id;

		// create the ambassador
		// this may throw an exception, which we would normally push through
		// the ExceptionManager. However, the exception manager depends on the
		// c++ instance that triggered the constructor to be in an accessible
		// map, whic only happenes after the consturction of that instance.
		// In this case, we fall back on the low-level JNI exception handling
		// mechanics, hence the reason we don't catch the exception and just let
		// it flow on through to the C++ side
		this.rtiamb = new Rti1516eAmbassadorEx();

		// fetch the LRC logger so that we have somewhere to notify of our events
		this.logger = LogManager.getFormatterLogger( "portico.lrc.cpp1516e" );
		this.logger.debug( "C++ ProxyRtiAmbassador.class created (java-side)" );

		// load the C++ libraries
		NativeLibraryLoader.load();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Helper Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public byte[] convertTag( byte[] given )
	{
		if( given == null )
			return null;
		else
			return given;
	}

	/**
	 * Converts the form that parameter values are sent over from C++ in into the form
	 * expected by the java side of the bindings.
	 */
	private HLA1516eParameterHandleValueMap convertParameters( int[] handles, byte[][] values )
	{
		HLA1516eParameterHandleValueMap theParameters = new HLA1516eParameterHandleValueMap();
		for( int i = 0; i < handles.length; ++i )
			theParameters.put( new HLA1516eHandle(handles[i]), values[i] );
		
		return theParameters;
	}

	/**
	 * Converts the form that attribute values are sent over from C++ in into the form
	 * expected by the java side of the bindings.
	 */
	private HLA1516eAttributeHandleValueMap convertAttributes( int[] handles, byte[][] values )
	{
		HLA1516eAttributeHandleValueMap theAttributes = new HLA1516eAttributeHandleValueMap();
		for( int i = 0; i < handles.length; ++i )
			theAttributes.put( new HLA1516eHandle(handles[i]), values[i] );
		
		return theAttributes;
	}

	/**
	 * Converts the form that attribute handles are sent over from C++ in into the form expected
	 * by the java side of the bindings
	 */
	private HLA1516eAttributeHandleSet convertHandles( int[] handles )
	{
		HLA1516eAttributeHandleSet handleSet = new HLA1516eAttributeHandleSet();
		for( int handle : handles )
		{
			handleSet.add( new HLA1516eHandle(handle) );
		}
		
		return handleSet;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Federation Management Services //////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	// 4.2
	public void connect( String callbackModel )
	{
		if( this.fedamb != null )
			ExceptionManager.pushException( this.id, new AlreadyConnected("Already Connected") );
		
		try
		{
			logger.trace( "connect() called" );
			this.fedamb = new ProxyFederateAmbassador( this.id );
			this.rtiamb.connect( this.fedamb, CallbackModel.valueOf(callbackModel) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 4.3
	public void disconnect()
	{
		try
		{
			logger.trace( "disconnect() called" );
			this.rtiamb.disconnect();
			
			// remove our federate ambassador reference
			this.fedamb = null;
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	//4.5
	public void createFederationExecution( String federationName, String fomModule, String timeName )
	{
		try
		{
			logger.trace( "createFederationExecution() called" );
			//URL[] modules = new URL[]{ fom.toURI().toURL() };
			rtiamb.createFederationExecution( federationName, getURL(fomModule) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	//4.5
	public void createFederationExecution( String federationName,
	                                       String[] fomModules,
	                                       String timeName )
	{
		try
		{
			logger.trace( "createFederationExecution() called" );
			
			// create an array of the modules to load
			URL[] modules = new URL[fomModules.length];
			for( int i = 0; i < fomModules.length; i++ )
			{
				modules[i] = getURL(fomModules[i]);
			}
				
			rtiamb.createFederationExecution( federationName, modules, timeName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	//4.5
	public void createFederationExecutionWithMIM( String federationName,
	                                              String[] fomModules,
	                                              String mimModule,
	                                              String timeName )
	{
		try
		{
			logger.trace( "createFederationExecution() called" );
			
			// create an array of the modules to load
			URL[] modules = new URL[fomModules.length];
			for( int i = 0; i < fomModules.length; i++ )
			{
				modules[i] = getURL(fomModules[i]);
			}
			
			rtiamb.createFederationExecution( federationName, modules, getURL(mimModule), timeName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	//4.6
	public void destroyFederationExecution( String federationName )
	{
		try
		{
			logger.trace( "destroyFederationExecution() called" );
			rtiamb.destroyFederationExecution( federationName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 4.7
	public void listFederationExecutions()
	{
		try
		{
			logger.trace( "listFederationExecutions() called" );
			rtiamb.listFederationExecutions();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}		
	}

	//4.9
	public int joinFederationExecution( String federateType,
	                                    String federationName,
	                                    String[] fomModules )
	{
		try
		{
			logger.trace( "joinFederationExecution() called" );
			
			// create an array of the modules to load
			URL[] modules = new URL[fomModules.length];
			for( int i = 0; i < fomModules.length; i++ )
			{
				modules[i] = getURL(fomModules[i]);
			}
			
			FederateHandle handle =
				rtiamb.joinFederationExecution( federateType, federationName, modules );
			
			return HLA1516eHandle.fromHandle( handle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	//4.9
	public int joinFederationExecution( String federateName,
	                                    String federateType,
	                                    String federationName,
	                                    String[] fomModules )
	{
		try
		{
			logger.trace( "joinFederationExecution() called" );
			
			// create an array of the modules to load
			URL[] modules = new URL[fomModules.length];
			for( int i = 0; i < fomModules.length; i++ )
			{
				modules[i] = getURL(fomModules[i]);
			}
			
			FederateHandle handle = rtiamb.joinFederationExecution( federateName,
			                                                        federateType,
			                                                        federationName,
			                                                        modules );
			
			return HLA1516eHandle.fromHandle( handle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	//4.10
	public void resignFederationExecution( String resignAction )
	{
		try
		{
			logger.trace( "resignFederationExecution() called" );
			rtiamb.resignFederationExecution( ResignAction.valueOf(resignAction) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	//4.11
	public void registerFederationSynchronizationPoint( String label, byte[] tag )
	{
		try
		{
			rtiamb.registerFederationSynchronizationPoint( label, tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	//4.11
	public void registerFederationSynchronizationPoint( String label,
	                                                    byte[]tag,
	                                                    int[] synchronizationSet )
	{
		try
		{
			rtiamb.registerFederationSynchronizationPoint( 
			    label,
			    tag,
			    new HLA1516eFederateHandleSet(synchronizationSet) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	//4.14
	public void synchronizationPointAchieved( String label )
	{
		try
		{
			rtiamb.synchronizationPointAchieved( label );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	//4.14
	public void synchronizationPointAchieved( String label, boolean wasSuccessful )
	{
		try
		{
			rtiamb.synchronizationPointAchieved( label, wasSuccessful );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 4.16
	public void requestFederationSave( String label )
	{
		try
		{
			rtiamb.requestFederationSave( label );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.16
	public void requestFederationSave( String label, double theTime )
	{
		try
		{
			rtiamb.requestFederationSave( label, new DoubleTime(theTime) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.18
	public void federateSaveBegun()
	{
		try
		{
			rtiamb.federateSaveBegun();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.19
	public void federateSaveComplete()
	{
		try
		{
			rtiamb.federateSaveComplete();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.19
	public void federateSaveNotComplete()
	{
		try
		{
			rtiamb.federateSaveNotComplete();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.21
	public void abortFederationSave()
	{
		try
		{
			rtiamb.abortFederationSave();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.22
	public void queryFederationSaveStatus()
	{
		try
		{
			rtiamb.queryFederationSaveStatus();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.24
	public void requestFederationRestore( String label )
	{
		try
		{
			rtiamb.requestFederationRestore( label );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.28
	public void federateRestoreComplete()
	{
		try
		{
			rtiamb.federateRestoreComplete();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.28
	public void federateRestoreNotComplete()
	{
		try
		{
			rtiamb.federateRestoreNotComplete();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.30
	public void abortFederationRestore()
	{
		try
		{
			rtiamb.abortFederationRestore();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	
	}

	// 4.31
	public void queryFederationRestoreStatus()
	{
		try
		{
			rtiamb.queryFederationRestoreStatus();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}


	/////////////////////////////////////
	// Declaration Management Services //
	/////////////////////////////////////

	// 5.2
	public void publishObjectClassAttributes( int theClass, int[] attributes )
	{
		try
		{
			rtiamb.publishObjectClassAttributes( new HLA1516eHandle(theClass),
			                                     new HLA1516eAttributeHandleSet(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.3
	public void unpublishObjectClass( int theClass )
	{
		try
		{
			rtiamb.unpublishObjectClass( new HLA1516eHandle(theClass) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.3
	public void unpublishObjectClassAttributes( int theClass, int[] attributes )
	{
		try
		{
			rtiamb.unpublishObjectClassAttributes( new HLA1516eHandle(theClass),
			                                       new HLA1516eAttributeHandleSet(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.4
	public void publishInteractionClass( int theInteraction )
	{
		try
		{
			rtiamb.publishInteractionClass( new HLA1516eHandle(theInteraction) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.5
	public void unpublishInteractionClass( int theInteraction )
	{
		try
		{
			rtiamb.unpublishInteractionClass( new HLA1516eHandle(theInteraction) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.6
	public void subscribeObjectClassAttributes( int theClass, int[] attributes, String updateRate )
	{
		try
		{
			if( updateRate.equals("") )
			{
				rtiamb.subscribeObjectClassAttributes( new HLA1516eHandle(theClass),
				                                       new HLA1516eAttributeHandleSet(attributes) );
			}
			else
			{
				rtiamb.subscribeObjectClassAttributes( new HLA1516eHandle(theClass),
				                                       new HLA1516eAttributeHandleSet(attributes),
				                                       updateRate );
			}
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.6
	public void subscribeObjectClassAttributesPassively( int theClass,
	                                                     int[] attributes,
	                                                     String updateRate )
	{
		try
		{
			if( updateRate.equals("") )
			{
    			rtiamb.subscribeObjectClassAttributesPassively(
    			    new HLA1516eHandle(theClass),
    			    new HLA1516eAttributeHandleSet(attributes) );
			}
			else
			{
    			rtiamb.subscribeObjectClassAttributesPassively(
    			    new HLA1516eHandle(theClass),
    			    new HLA1516eAttributeHandleSet(attributes),
    			    updateRate );
			}
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.7
	public void unsubscribeObjectClass( int theClass )
	{
		try
		{
			rtiamb.unsubscribeObjectClass( new HLA1516eHandle(theClass) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.7
	public void unsubscribeObjectClassAttributes( int theClass, int[] attributes )
	{
		try
		{
			rtiamb.unsubscribeObjectClassAttributes( new HLA1516eHandle(theClass),
			                                         new HLA1516eAttributeHandleSet(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.8
	public void subscribeInteractionClass( int theClass )
	{
		try
		{
			rtiamb.subscribeInteractionClass( new HLA1516eHandle(theClass) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.8
	public void subscribeInteractionClassPassively( int theClass )
	{
		try
		{
			rtiamb.subscribeInteractionClassPassively( new HLA1516eHandle(theClass) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 5.9
	public void unsubscribeInteractionClass( int theClass )
	{
		try
		{
			rtiamb.unsubscribeInteractionClass( new HLA1516eHandle(theClass) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	////////////////////////////////
	// Object Management Services //
	////////////////////////////////

	// 6.2
	public void reserveObjectInstanceName( String objectName )
	{
		try
		{
			rtiamb.reserveObjectInstanceName( objectName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.4
	public void releaseObjectInstanceName( String objectName )
	{
		try
		{
			rtiamb.releaseObjectInstanceName( objectName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.5
	public void reserveMultipleObjectInstanceName( String[] objectNames )
	{
		try
		{
			HashSet<String> set = new HashSet<String>();
			for( String temp : objectNames )
				set.add( temp );
			
			rtiamb.reserveMultipleObjectInstanceName( set );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.7
	public void releaseMultipleObjectInstanceName( String[] objectNames )
	{
		try
		{
			HashSet<String> set = new HashSet<String>();
			for( String temp : objectNames )
				set.add( temp );
			
			rtiamb.releaseMultipleObjectInstanceName( set );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.8
	public int registerObjectInstance( int theClass )
	{
		try
		{
			HLA1516eHandle classHandle = new HLA1516eHandle( theClass );
			return HLA1516eHandle.fromHandle( rtiamb.registerObjectInstance(classHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 6.8
	public int registerObjectInstance( int theClass, String objectName )
	{
		try
		{
			HLA1516eHandle classHandle = new HLA1516eHandle( theClass );
			return HLA1516eHandle.fromHandle( rtiamb.registerObjectInstance(classHandle,objectName) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 6.10
	public void updateAttributeValues( int theObject,
	                                   int[] attributes,
	                                   byte[][] values,
	                                   byte[] tag )
	{
		try
		{
			HLA1516eAttributeHandleValueMap attributeValues = new HLA1516eAttributeHandleValueMap();
			for( int i = 0; i < attributes.length; i++ )
				attributeValues.put( new HLA1516eHandle(attributes[i]), values[i] );

			rtiamb.updateAttributeValues( new HLA1516eHandle(theObject), attributeValues, tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.10
	public int updateAttributeValues( int theObject,
	                                  int[] attributes,
	                                  byte[][] values,
	                                  byte[] tag,
	                                  double theTime )
	{
		try
		{
			HLA1516eHandle objectHandle = new HLA1516eHandle( theObject );
			HLA1516eAttributeHandleValueMap attributeValues = new HLA1516eAttributeHandleValueMap();
			DoubleTime time = new DoubleTime( theTime );
			for( int i = 0; i < attributes.length; i++ )
				attributeValues.put( new HLA1516eHandle(attributes[i]), values[i] );

			MessageRetractionReturn result = rtiamb.updateAttributeValues( objectHandle,
			                                                               attributeValues,
			                                                               tag,
			                                                               time );
			return HLA1516eHandle.fromHandle( result.handle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 6.12
	public void sendInteraction( int theInteraction,
	                             int[] parameters,
	                             byte[][] values,
	                             byte[] tag )
	{
		try
		{
			HLA1516eParameterHandleValueMap parameterValues = new HLA1516eParameterHandleValueMap();
			for( int i = 0; i < parameters.length; i++ )
				parameterValues.put( new HLA1516eHandle(parameters[i]), values[i] );

			rtiamb.sendInteraction( new HLA1516eHandle(theInteraction), parameterValues, tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.12
	public int sendInteraction( int theInteraction,
	                            int[] parameters,
	                            byte[][] values,
	                            byte[] tag,
	                            double theTime )
	{
		try
		{
			HLA1516eHandle classHandle = new HLA1516eHandle( theInteraction );
			HLA1516eParameterHandleValueMap parameterValues = new HLA1516eParameterHandleValueMap();
			DoubleTime time = new DoubleTime( theTime );
			for( int i = 0; i < parameters.length; i++ )
				parameterValues.put( new HLA1516eHandle(parameters[i]), values[i] );

			MessageRetractionReturn result = rtiamb.sendInteraction( classHandle,
			                                                         parameterValues,
			                                                         tag,
			                                                         time );

			return HLA1516eHandle.fromHandle( result.handle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 6.14
	public void deleteObjectInstance( int objectHandle, byte[] tag )
	{
		try
		{
			rtiamb.deleteObjectInstance( new HLA1516eHandle(objectHandle), tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.14
	public int deleteObjectInstance( int objectHandle, byte[] tag, double theTime )
	{
		try
		{
			DoubleTime time = new DoubleTime( theTime );
			HLA1516eHandle handle = new HLA1516eHandle( objectHandle );
			MessageRetractionReturn result = rtiamb.deleteObjectInstance( handle, tag, time );
			return HLA1516eHandle.fromHandle( result.handle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 6.16
	public void localDeleteObjectInstance( int objectHandle )
	{
		try
		{
			rtiamb.localDeleteObjectInstance( new HLA1516eHandle(objectHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.19
	public void requestAttributeValueUpdate( int theObject,
	                                         int[] attributes,
	                                         byte[] tag )
	{
		try
		{
			ObjectInstanceHandle objectHandle = (ObjectInstanceHandle)new HLA1516eHandle(theObject);
			rtiamb.requestAttributeValueUpdate( objectHandle,
			                                    new HLA1516eAttributeHandleSet(attributes),
			                                    tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.19
	public void requestAttributeValueUpdateClass( int theClass, int[] attributes, byte[] tag )
	{
		try
		{
			ObjectClassHandle classHandle = (ObjectClassHandle)new HLA1516eHandle(theClass);
			rtiamb.requestAttributeValueUpdate( classHandle,
			                                    new HLA1516eAttributeHandleSet(attributes),
			                                    tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.23
	public void requestAttributeTransportationTypeChange( int theObject,
	                                                      int[] theAttributes,
	                                                      String transport )
	{
		try
		{
			rtiamb.requestAttributeTransportationTypeChange(
			    new HLA1516eHandle(theObject),
			    new HLA1516eAttributeHandleSet(theAttributes),
			    HLA1516eTransportationTypeHandleFactory.fromString(transport) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.25
	public void queryAttributeTransportationType( int theObject, int theAttribute )
	{
		try
		{
			rtiamb.queryAttributeTransportationType( new HLA1516eHandle(theObject),
			                                         new HLA1516eHandle(theAttribute) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.27
	public void requestInteractionTransportationTypeChange( int theClass, String transport )
	{
		try
		{
			rtiamb.requestInteractionTransportationTypeChange(
			    new HLA1516eHandle(theClass),
			    HLA1516eTransportationTypeHandleFactory.fromString(transport) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 6.29
	public void queryInteractionTransportationType( int theFederate, int interactionClass )
	{
		try
		{
			rtiamb.queryInteractionTransportationType( new HLA1516eHandle(theFederate),
			                                           new HLA1516eHandle(interactionClass) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	///////////////////////////////////
	// Ownership Management Services //
	///////////////////////////////////

	// 7.2
	public void unconditionalAttributeOwnershipDivestiture( int theObject, int[] attributes )
	{
		try
		{
			rtiamb.unconditionalAttributeOwnershipDivestiture( new HLA1516eHandle(theObject),
			                                                   new HLA1516eAttributeHandleSet(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.3
	public void negotiatedAttributeOwnershipDivestiture( int theObject,
	                                                     int[] attributes,
	                                                     byte[] tag )
	{
		try
		{
			rtiamb.negotiatedAttributeOwnershipDivestiture( new HLA1516eHandle(theObject),
			                                                new HLA1516eAttributeHandleSet(attributes),
			                                                tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.6
	public void confirmDivestiture( int theObject, int[] attributes, byte[] tag )
	{
		try
		{
			rtiamb.confirmDivestiture( new HLA1516eHandle(theObject),
			                           new HLA1516eAttributeHandleSet(attributes),
			                           tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.8
	public void attributeOwnershipAcquisition( int theObject, int[] attributes, byte[] tag )
	{
		try
		{
			rtiamb.attributeOwnershipAcquisition( new HLA1516eHandle(theObject),
			                                      new HLA1516eAttributeHandleSet(attributes),
			                                      tag );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.9
	public void attributeOwnershipAcquisitionIfAvailable( int theObject, int[] attributes )
	{
		try
		{
			rtiamb.attributeOwnershipAcquisitionIfAvailable( new HLA1516eHandle(theObject),
			                                                 new HLA1516eAttributeHandleSet(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.12
	public void attributeOwnershipReleaseDenied( int theObject, int[] theAttributes )
	{
		try
		{
			rtiamb.attributeOwnershipReleaseDenied( new HLA1516eHandle(theObject),
			                                        new HLA1516eAttributeHandleSet(theAttributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.13
	public int[] attributeOwnershipDivestitureIfWanted( int theObject, int[] attributes )
	{
		return null;
	}

	// 7.14
	public void cancelNegotiatedAttributeOwnershipDivestiture( int theObject, int[] attributes )
	{
		try
		{
			rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( new HLA1516eHandle(theObject),
			                                                      new HLA1516eAttributeHandleSet(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.15
	public void cancelAttributeOwnershipAcquisition( int theObject, int[] theAttributes )
	{
		try
		{
			rtiamb.cancelAttributeOwnershipAcquisition( new HLA1516eHandle(theObject),
			                                            new HLA1516eAttributeHandleSet(theAttributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.17
	public void queryAttributeOwnership( int theObject, int theAttribute )
	{
		try
		{
			rtiamb.queryAttributeOwnership( new HLA1516eHandle(theObject),
			                                new HLA1516eHandle(theAttribute) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 7.19
	public boolean isAttributeOwnedByFederate( int theObject, int theAttribute )
	{
		try
		{
			return rtiamb.isAttributeOwnedByFederate( new HLA1516eHandle(theObject),
			                                          new HLA1516eHandle(theAttribute) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return false;
		}
	}

	//////////////////////////////
	// Time Management Services //
	//////////////////////////////

	// 8.2
	public void enableTimeRegulation( double theLookahead )
	{
		try
		{
			rtiamb.enableTimeRegulation( new DoubleTimeInterval(theLookahead) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.4
	public void disableTimeRegulation()
	{
		try
		{
			rtiamb.disableTimeRegulation();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.5
	public void enableTimeConstrained()
	{
		try
		{
			rtiamb.enableTimeConstrained();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.7
	public void disableTimeConstrained()
	{
		try
		{
			rtiamb.disableTimeConstrained();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	// 8.8
	public void timeAdvanceRequest( double theTime )
	{
		try
		{
			rtiamb.timeAdvanceRequest( new DoubleTime(theTime) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.9
	public void timeAdvanceRequestAvailable( double theTime )
	{
		try
		{
			rtiamb.timeAdvanceRequestAvailable( new DoubleTime(theTime) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.10
	public void nextMessageRequest( double theTime )
	{
		try
		{
			rtiamb.nextMessageRequest( new DoubleTime(theTime) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.11
	public void nextMessageRequestAvailable( double theTime )
	{
		try
		{
			rtiamb.nextMessageRequestAvailable( new DoubleTime(theTime) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.12
	public void flushQueueRequest( double theTime )
	{
		try
		{
			rtiamb.flushQueueRequest( new DoubleTime(theTime) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.14
	public void enableAsynchronousDelivery()
	{
		try
		{
			rtiamb.enableAsynchronousDelivery();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.15
	public void disableAsynchronousDelivery()
	{
		try
		{
			rtiamb.disableAsynchronousDelivery();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.16
	public double queryGALT()
	{
		try
		{
			TimeQueryReturn result = rtiamb.queryGALT();
			if( result.timeIsValid )
				return ((DoubleTime)result.time).getTime();
			else
				return -1.0;
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1.0;
		}
	}
	
	// 8.17
	public double queryLogicalTime()
	{
		try
		{
			return ((DoubleTime)rtiamb.queryLogicalTime()).getTime();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1.0;
		}
	}

	// 8.18
	public double queryLITS()
	{
		try
		{
			TimeQueryReturn result = rtiamb.queryLITS();
			if( result.timeIsValid )
				return ((DoubleTime)result.time).getTime();
			else
				return -1.0;
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1.0;
		}
	}

	// 8.19
	public void modifyLookahead( double theLookahead )
	{
		try
		{
			rtiamb.modifyLookahead( new DoubleTimeInterval(theLookahead) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.20
	public double queryLookahead()
	{
		try
		{
			return ((DoubleTime)rtiamb.queryLogicalTime()).getTime();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1.0;
		}
	}

	// 8.21
	public void retract( int theHandle )
	{
		try
		{
			rtiamb.retract( new HLA1516eHandle(theHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.23
	public void changeAttributeOrderType( int theObject, int[] attributes, String orderType )
	{
		try
		{
			rtiamb.changeAttributeOrderType( new HLA1516eHandle(theObject),
			                                 new HLA1516eAttributeHandleSet(attributes),
			                                 OrderType.valueOf(orderType) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.24
	public void changeInteractionOrderType( int theClass, String orderType )
	{
		try
		{
			rtiamb.changeInteractionOrderType( new HLA1516eHandle(theClass),
			                                 OrderType.valueOf(orderType) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	//////////////////////////////////
	// Data Distribution Management //
	//////////////////////////////////

	// 9.2
	public int createRegion( int[] dimensions )
	{
		try
		{
			return HLA1516eHandle.fromHandle(
			           rtiamb.createRegion(new HLA1516eDimensionHandleSet(dimensions)) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 9.3
	public void commitRegionModifications( int[] regions )
	{
		try
		{
			rtiamb.commitRegionModifications( new HLA1516eRegionHandleSet(regions) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.4
	public void deleteRegion( int theRegion )
	{
		try
		{
			rtiamb.deleteRegion( new HLA1516eHandle(theRegion) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	//9.5
	public int registerObjectInstanceWithRegions( int theClass, int[] attributes, int[] regions )
	{
		notSupported( "registerObjectInstanceWithRegions()" );
		return -1;
	}

	//9.5
	public int registerObjectInstanceWithRegions( int theClass,
	                                              int[] attributes,
	                                              int[] regions,
	                                              String objectName )
	{
		notSupported( "registerObjectInstanceWithRegions()" );
		return -1;
	}

	// 9.6
	public void associateRegionsForUpdates( int theObject,
	                                        int[] attributes,
	                                        int[] regions )
	{
		notSupported( "associateRegionsForUpdates()" );
	}

	// 9.7
	public void unassociateRegionsForUpdates( int theObject,
	                                          int[] attributes,
	                                          int[] regions )
	{
		notSupported( "unassociateRegionsForUpdates()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesWithRegions( int  theClass,
	                                                       int[] attributes,
	                                                       int[] regions )
	{
		notSupported( "subscribeObjectClassAttributesWithRegions()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesWithRegions( int theClass,
	                                                       int[] attributes,
	                                                       int[] regions,
	                                                       String updateRate )
	{
		notSupported( "subscribeObjectClassAttributesWithRegions()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesPassivelyWithRegions( int theClass,
	                                                                int[] attributes,
	                                                                int[] regions )
	{
		notSupported( "subscribeObjectClassAttributesPassivelyWithRegions()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesPassivelyWithRegions( int theClass,
	                                                                int[] attributes,
	                                                                int[] regions,
	                                                                String updateRate )
	{
		notSupported( "subscribeObjectClassAttributesPassivelyWithRegions()" );
	}

	// 9.9
	public void unsubscribeObjectClassAttributesWithRegions( int theClass,
	                                                         int[] attributes,
	                                                         int[] regions )
	{
		notSupported( "unsubscribeObjectClassAttributesWithRegions()" );
	}

	// 9.10
	public void subscribeInteractionClassWithRegions( int theClass, int[] regions )
	{
		notSupported( "subscribeInteractionClassWithRegions()" );
	}

	// 9.10
	public void subscribeInteractionClassPassivelyWithRegions( int theClass, int[] regions )
	{
		notSupported( "subscribeInteractionClassPassivelyWithRegions()" );
	}

	// 9.11
	public void unsubscribeInteractionClassWithRegions( int theClass, int[] regions )
	{
		notSupported( "unsubscribeInteractionClassWithRegions()" );
	}

	//9.12
	public void sendInteractionWithRegions( int theInteraction,
	                                        int[] parameters,
	                                        byte[][] regions,
	                                        byte[] tag )
	{
		notSupported( "sendInteractionWithRegions()" );
	}

	//9.12
	public int sendInteractionWithRegions( int theInteraction,
	                                       int[] parameters,
	                                       byte[][] values,
	                                       int[] regions,
	                                       byte[] tag,
	                                       double time )
	{
		notSupported( "sendInteractionWithRegions()" );
		return -1;
	}

	// 9.13
	public void requestAttributeValueUpdateWithRegions( int theClass,
	                                                    int[] attributes,
	                                                    int[] regions,
	                                                    byte[] tag )
	{
		notSupported( "requestAttributeValueUpdateWithRegions()" );
	}

	//////////////////////////
	// RTI Support Services //
	//////////////////////////

	// 10.2
	public String getAutomaticResignDirective()
	{
		try
		{
			return rtiamb.getAutomaticResignDirective().toString();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKNOWN";
		}
	}

	// 10.3
	public void setAutomaticResignDirective( String resignAction )
	{
		try
		{
			rtiamb.setAutomaticResignDirective( ResignAction.valueOf(resignAction) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 10.4
	public int getFederateHandle( String theName )
	{
		try
		{
			return HLA1516eHandle.fromHandle( rtiamb.getFederateHandle(theName) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}


	// 10.5
	public String getFederateName( int theHandle )
	{
		try
		{
			return rtiamb.getFederateName( new HLA1516eHandle(theHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKNOWN";
		}
	}

	// 10.6
	public int getObjectClassHandle( String theName )
	{
		try
		{
			return HLA1516eHandle.fromHandle( rtiamb.getObjectClassHandle(theName) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 10.7
	public String getObjectClassName( int theHandle )
	{
		try
		{
			return rtiamb.getObjectClassName( new HLA1516eHandle(theHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKNOWN";
		}
	}

	// 10.8
	public int getKnownObjectClassHandle( int theObject )
	{
		try
		{
			return HLA1516eHandle.fromHandle( rtiamb.getKnownObjectClassHandle(new HLA1516eHandle(theObject)) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 10.9
	public int getObjectInstanceHandle( String theName )
	{
		try
		{
			return HLA1516eHandle.fromHandle( rtiamb.getObjectInstanceHandle(theName) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 10.10
	public String getObjectInstanceName( int theHandle )
	{
		try
		{
			return rtiamb.getObjectInstanceName( new HLA1516eHandle(theHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKNOWN";
		}
	}

	// 10.11
	public int getAttributeHandle( int whichClass, String theName )
	{
		try
		{
			return HLA1516eHandle.fromHandle( rtiamb.getAttributeHandle(new HLA1516eHandle(whichClass),
			                                                            theName) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 10.12
	public String getAttributeName( int whichClass, int theHandle )
	{
		try
		{
			return rtiamb.getAttributeName( new HLA1516eHandle(whichClass),
			                                new HLA1516eHandle(theHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKNOWN";
		}
	}

	// 10.13
	public double getUpdateRateValue( String updateRateDesignator )
	{
		return 0.0;
	}

	// 10.14
	public double getUpdateRateValueForAttribute( int theObject, int theAttribute )
	{
		return 0.0;
	}

	// 10.15
	public int getInteractionClassHandle( String theName )
	{
		try
		{
			return HLA1516eHandle.fromHandle( rtiamb.getInteractionClassHandle(theName) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 10.16
	public String getInteractionClassName( int theHandle )
	{
		try
		{
			return rtiamb.getInteractionClassName( new HLA1516eHandle(theHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKNOWN";
		}
	}

	// 10.17
	public int getParameterHandle( int whichClass, String theName )
	{
		try
		{
			return HLA1516eHandle.fromHandle(
			    rtiamb.getParameterHandle(new HLA1516eHandle(whichClass), theName) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 10.18
	public String getParameterName( int whichClass, int theHandle )
	{
		try
		{
			return rtiamb.getParameterName( new HLA1516eHandle(whichClass),
			                                new HLA1516eHandle(theHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKNOWN";
		}
	}

	// 10.23
	public int[] getAvailableDimensionsForClassAttribute( int whichClass, int theHandle )
	{
		return null;
	}

	// 10.24
	public int[] getAvailableDimensionsForInteractionClass( int theHandle )
	{
		return null;
	}

	// 10.25
	public int getDimensionHandle( String theName )
	{
		try
		{
			return HLA1516eHandle.fromHandle( rtiamb.getDimensionHandle(theName) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 10.26
	public String getDimensionName( int theHandle )
	{
		try
		{
			return rtiamb.getDimensionName( new HLA1516eHandle(theHandle) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKOWN";
		}
	}

	// 10.27
	public long getDimensionUpperBound( int theHandle )
	{
		notSupported( "getDimensionUpperBound()" );
		return 1;
	}

	// 10.28
	public int[] getDimensionHandleSet( int region )
	{
		notSupported( "getDimensionHandleSet()" );
		return new int[]{};
	}

	// 10.29
	// RangeBounds => long[2]{ upper, lower }
	public long[] getRangeBounds( int region, int dimension )
	{
		notSupported( "getRangeBounds()" );
		return new long[]{ 1, 1 };
	}

	// 10.30
	// int, int, RangeBounds
	public void setRangeBounds( int region, int dimension, long upperBound, long lowerBound )
	{
		notSupported( "setRangeBounds()" );
	}

	// 10.31
	// Do purely in C++
	//long normalizeFederateHandle( int federateHandle )
	//{
	//	return federateHandle;
	//}

	// 10.32
	// Do purely in C++
	//long normalizeServiceGroup( ServiceGroup group ) throws InvalidServiceGroup,
	//    FederateNotExecutionMember, NotConnected, RTIinternalError;

	// 10.33
	public void enableObjectClassRelevanceAdvisorySwitch()
	{
		try
		{
			rtiamb.enableObjectClassRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 10.34
	public void disableObjectClassRelevanceAdvisorySwitch()
	{
		try
		{
			rtiamb.disableObjectClassRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 10.35
	public void enableAttributeRelevanceAdvisorySwitch()
	{
		try
		{
			rtiamb.enableAttributeRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 10.36
	public void disableAttributeRelevanceAdvisorySwitch()
	{
		try
		{
			rtiamb.disableAttributeRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 10.37
	public void enableAttributeScopeAdvisorySwitch()
	{
		try
		{
			rtiamb.enableAttributeScopeAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	}

	// 10.38
	public void disableAttributeScopeAdvisorySwitch()
	{
		try
		{
			rtiamb.disableAttributeScopeAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	}

	// 10.39
	public void enableInteractionRelevanceAdvisorySwitch()
	{
		try
		{
			rtiamb.enableInteractionRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}	}

	// 10.40
	public void disableInteractionRelevanceAdvisorySwitch()
	{
		try
		{
			rtiamb.disableInteractionRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 10.41
	public boolean evokeCallback( double minSeconds )
	{
		try
		{
			return rtiamb.evokeCallback( minSeconds );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return false;
		}
	}

	// 10.42
	public boolean evokeMultipleCallbacks( double minSeconds, double maxSeconds )
	{
		try
		{
			return rtiamb.evokeMultipleCallbacks( minSeconds, maxSeconds );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return false;
		}
	}

	// 10.43
	public void enableCallbacks()
	{
		try
		{
			rtiamb.enableCallbacks();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 10.44
	public void disableCallbacks()
	{
		try
		{
			rtiamb.disableCallbacks();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public String getHLAversion()
	{
		try
		{
			return rtiamb.getHLAversion();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "UNKNOWN";
		}
	}
	
	public String getAttributeDatatype( int classHandle, int attributeHandle )
	{
		String name =  "" ;
		
		ObjectClassHandle newObjectClassHandle = new HLA1516eHandle( classHandle );
		AttributeHandle newAttributeHandle = new HLA1516eHandle( attributeHandle );
		try
		{
			IDatatype type = rtiamb.getAttributeDatatype( newObjectClassHandle, 
			                                              newAttributeHandle );
			name = type.getName() ;
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
		
		return name;
		 
	}
 
	public String getParameterDatatype( int classHandle, int parameterHandle )
	{
		String name = "";
		
		InteractionClassHandle newInteractionClassHandle = new HLA1516eHandle( classHandle );
		ParameterHandle newParameterHandle = new HLA1516eHandle( parameterHandle );
		
		try
		{
			IDatatype type = rtiamb.getParameterDatatype( newInteractionClassHandle, 
			                                              newParameterHandle );
			name = type.getName();
			 
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
		
		return name;
	}
	
	public String getFom()
	{
		return rtiamb.getFOM().toXmlDocument();
	}
	
	private void notSupported( String name )
	{
		logger.warn( "Method "+name+" is not yet supported by the C++ interface" );
	}

	private URL getURL( String fomModule ) throws URISyntaxException, MalformedURLException
	{
		// URLs are only allowed / as file separator
		URI uri = new URI(fomModule.replace(File.separator, "/"));
		URL retUrl = null;
		if(uri.isAbsolute())
		{
			retUrl = uri.toURL();
		}
		else
		{
			File fom = new File( fomModule );
			retUrl = fom.toURI().toURL();
		}
		
		return retUrl;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * This method is only to be called from the C++ side of the bindings.
	 * <p/>
	 * It will search for the RID file and load its contents into the system properties.
	 * If the environment variable RTI_RID_FILE is set, it will look for the RID file in
	 * that location. If there is a value in that location, it will attempt to load that
	 * file. If the file can be found, it will be loaded as a properties file. If it can't
	 * be found, the method will try to load ./RTI.rid (where the current directory is
	 * the directory from which the program was launched. If that file can't be found,
	 * the method will just return.
	 * 
	 * @return true if a file was loaded, false otherwise
	 */
	public static boolean parseCppRid()
	{
		// check for the presence of the environment variable
		String ridfile = System.getenv( "RTI_RID_FILE" );
		if( ridfile == null )
			ridfile = "RTI.rid";
		
		// does the rid file exist?
		File file = new File( ridfile );
		if( file.exists() == false )
			return false;
		
		// it exists, load it into a properties file
		Properties ridProperties = new Properties();
		try
		{
			FileInputStream fis = new FileInputStream( ridfile );
			ridProperties.load( fis );
			fis.close();
		}
		catch( Exception e )
		{
			return false;
		}

		// loaded from the file, put them in system properties
		for( Object key : ridProperties.keySet() )
		{
			System.setProperty( (String)key, (String)ridProperties.get(key) );
		}

		// the properties were loaded
		return true;
	}
}
