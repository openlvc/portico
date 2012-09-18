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
import java.util.Locale;
import java.util.Properties;

import hla.rti1516e.exceptions.RTIinternalError;

import org.apache.log4j.Logger;
import org.portico.impl.hla1516e.Rti1516eAmbassador;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleValueMap;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMap;

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
	private Rti1516eAmbassador rtiamb;
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
		this.rtiamb = new Rti1516eAmbassador();

		// fetch the LRC logger so that we have somewhere to notify of our events
		this.logger = Logger.getLogger( "portico.c++" );
		this.logger.debug( "C++ ProxyRtiAmbassador.class created (java-side)" );

		// load the C++ libraries
		loadCppLibraries();
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
	////////////////////////////////////////////////////////////////////////////////////////////
	// 4.2
	public void connect( String callbackModel )
	{
logger.error( "connect("+callbackModel+")" );
	}

	// 4.3
	public void disconnect()
	{
		
	}

	//4.5
	public void createFederationExecution( String federationName, String fomModule, String timeName )
	{
		logger.error( "createFederationExecution(1)" );
	}

	//4.5
	public void createFederationExecution( String federationName,
	                                       String[] fomModules,
	                                       String timeName )
	{
		logger.error( "createFederationExecution(2)" );
	}

	//4.5
	public void createFederationExecutionWithMIM( String federationName,
	                                              String[] fomModules,
	                                              String mimModule,
	                                              String timeName )
	{
		logger.error( "createFederationExecution(3)" );
	}

	//4.6
	public void destroyFederationExecution( String federationName )
	{
		
	}

	// 4.7
	public void listFederationExecutions()
	{
		
	}

	//4.9
	public int joinFederationExecution( String federateName,
	                                    String federateType,
	                                    String federationName,
	                                    String[] additionalFomModules )
	{
		return 1;
	}

	//4.9
	public int joinFederationExecution( String federateType,
	                                    String federationName,
	                                    String[] additionalFomModules )
	{
		return 1;
	}

	//4.9
	public int joinFederationExecution( String federateName,
	                                    String federateType,
	                                    String federationName )
	{
		return 1;	
	}

	//4.9
	public int joinFederationExecution( String federateType, String federationName )
	{
		return 1;
	}

	//4.10
	public void resignFederationExecution( String resignAction )
	{
		
	}

	//4.11
	public void registerFederationSynchronizationPoint( String label, byte[] tag )
	{
		
	}

	//4.11
	public void registerFederationSynchronizationPoint( String label,
	                                                    byte[]tag,
	                                                    int[] synchronizationSet )
	{
		
	}

	//4.14
	public void synchronizationPointAchieved( String label )
	{
		
	}

	//4.14
	public void synchronizationPointAchieved( String label, boolean wasSuccessful )
	{
		
	}

	// 4.16
	public void requestFederationSave( String label )
	{
		
	}

	// 4.16
	public void requestFederationSave( String label, double theTime )
	{
		
	}

	// 4.18
	public void federateSaveBegun()
	{
		
	}

	// 4.19
	public void federateSaveComplete()
	{
		
	}

	// 4.19
	public void federateSaveNotComplete()
	{
		
	}

	// 4.21
	public void abortFederationSave()
	{
		
	}

	// 4.22
	public void queryFederationSaveStatus()
	{
		
	}

	// 4.24
	public void requestFederationRestore( String label )
	{
		
	}

	// 4.28
	public void federateRestoreComplete()
	{
		
	}

	// 4.28
	public void federateRestoreNotComplete()
	{
		
	}

	// 4.30
	public void abortFederationRestore()
	{
		
	}

	// 4.31
	public void queryFederationRestoreStatus()
	{
		
	}


	/////////////////////////////////////
	// Declaration Management Services //
	/////////////////////////////////////

	// 5.2
	public void publishObjectClassAttributes( int theClass, int[] attributeList )
	{
		
	}

	// 5.3
	public void unpublishObjectClass( int theClass )
	{
		
	}


	// 5.3
	public void unpublishObjectClassAttributes( int theClass, int[] attributeList )
	{
		
	}

	// 5.4
	public void publishInteractionClass( int theInteraction )
	{
		
	}

	// 5.5
	public void unpublishInteractionClass( int theInteraction )
	{
		
	}

	// 5.6
	public void subscribeObjectClassAttributes( int theClass, int[] attributeList )
	{
		
	}

	// 5.6
	public void subscribeObjectClassAttributes( int theClass, int[] attributes, String updateRate )
	{
		
	}

	// 5.6
	public void subscribeObjectClassAttributesPassively( int theClass, int[] attributes )
	{
		
	}

	// 5.6
	public void subscribeObjectClassAttributesPassively( int theClass,
	                                                     int[] attributeList,
	                                                     String updateRate )
	{
		
	}

	// 5.7
	public void unsubscribeObjectClass( int theClass )
	{
		
	}

	// 5.7
	public void unsubscribeObjectClassAttributes( int theClass, int[] attributeList )
	{
		
	}

	// 5.8
	public void subscribeInteractionClass( int theClass )
	{
		
	}

	// 5.8
	public void subscribeInteractionClassPassively( int theClass )
	{
		
	}

	// 5.9
	public void unsubscribeInteractionClass( int theClass )
	{
		
	}

	////////////////////////////////
	// Object Management Services //
	////////////////////////////////

	// 6.2
	public void reserveObjectInstanceName( String objectName )
	{
		
	}

	// 6.4
	public void releaseObjectInstanceName( String objectName )
	{
		
	}

	// 6.5
	public void reserveMultipleObjectInstanceName( String[] objectNames )
	{
		
	}

	// 6.7
	public void releaseMultipleObjectInstanceName( String[] objectNames )
	{
		
	}

	// 6.8
	int registerObjectInstance( int theClass )
	{
		return 1;
	}

	// 6.8
	int registerObjectInstance( int theClass, String objectName )
	{
		return 1;
	}

	// 6.10
	public void updateAttributeValues( int theObject,
	                                   int[] attributes,
	                                   byte[][] values,
	                                   byte[] tag )
	{
		
	}

	// 6.10
	int updateAttributeValues( int theObject,
	                           int[] attributes,
	                           byte[][] values,
	                           byte[] tag,
	                           double theTime )
	{
		return 1;
	}

	// 6.12
	public void sendInteraction( int theInteraction,
	                             int[] parameters,
	                             byte[][] values,
	                             byte[] tag )
	{
		
	}

	// 6.12
	int sendInteraction( int theInteraction,
	                     int[] parameters,
	                     byte[][] values,
	                     byte[] tag,
	                     double time )
	{
		return 1;
	}

	// 6.14
	public void deleteObjectInstance( int objectHandle, byte[] tag )
	{
		
	}

	// 6.14
	int deleteObjectInstance( int objectHandle, byte[] tag, double time )
	{
		return 1;
	}

	// 6.16
	public void localDeleteObjectInstance( int objectHandle )
	{
		
	}

	// 6.19
	public void requestAttributeValueUpdate( int theObject,
	                                         int[] attributes,
	                                         byte[] tag )
	{
		
	}

	// 6.19
	public void requestAttributeValueUpdateClass( int theClass, int[] attributes, byte[] tag )
	{
		
	}

	// 6.23
	public void requestAttributeTransportationTypeChange( int theObject,
	                                                      int[] theAttributes,
	                                                      String transport )
	{
		
	}

	// 6.25
	public void queryAttributeTransportationType( int theObject, int[] theAttribute )
	{
		
	}

	// 6.27
	public void requestInteractionTransportationTypeChange( int theClass, String transport )
	{
		
	}

	// 6.29
	public void queryInteractionTransportationType( int theFederate, int interactionClass )
	{
		
	}

	///////////////////////////////////
	// Ownership Management Services //
	///////////////////////////////////

	// 7.2
	public void unconditionalAttributeOwnershipDivestiture( int theObject, int[] attributes )
	{
		
	}

	// 7.3
	public void negotiatedAttributeOwnershipDivestiture( int theObject,
	                                                     int[] attributes,
	                                                     byte[] tag )
	{
		
	}

	// 7.6
	public void confirmDivestiture( int theObject, int[] attributes, byte[] tag )
	{
		
	}

	// 7.8
	public void attributeOwnershipAcquisition( int theObject, int[] attributes, byte[] tag )
	{
		
	}

	// 7.9
	public void attributeOwnershipAcquisitionIfAvailable( int theObject, int[] attributes )
	{
		
	}

	// 7.12
	public void attributeOwnershipReleaseDenied( int theObject, int[] theAttributes )
	{
		
	}

	// 7.13
	int[] attributeOwnershipDivestitureIfWanted( int theObject, int[] attributes )
	{
		return null;
	}

	// 7.14
	public void cancelNegotiatedAttributeOwnershipDivestiture( int theObject, int[] attributes )
	{
		
	}

	// 7.15
	public void cancelAttributeOwnershipAcquisition( int theObject, int[] theAttributes )
	{
		
	}

	// 7.17
	public void queryAttributeOwnership( int theObject, int[] theAttribute )
	{
		
	}

	// 7.19
	boolean isAttributeOwnedByFederate( int theObject, int[] theAttribute )
	{
		return false;
	}

	//////////////////////////////
	// Time Management Services //
	//////////////////////////////

	// 8.2
	public void enableTimeRegulation( double theLookahead )
	{
		
	}

	// 8.4
	public void disableTimeRegulation()
	{
		
	}

	// 8.5
	public void enableTimeConstrained()
	{
		
	}

	// 8.7
	public void disableTimeConstrained()
	{
		
	}
	
	// 8.8
	public void timeAdvanceRequest( double theTime )
	{
		
	}

	// 8.9
	public void timeAdvanceRequestAvailable( double theTime )
	{
		
	}

	// 8.10
	public void nextMessageRequest( double theTime )
	{
		
	}

	// 8.11
	public void nextMessageRequestAvailable( double theTime )
	{
		
	}

	// 8.12
	public void flushQueueRequest( double theTime )
	{
		
	}

	// 8.14
	public void enableAsynchronousDelivery()
	{
		
	}

	// 8.15
	public void disableAsynchronousDelivery()
	{
		
	}

	// 8.16
	boolean queryGALT()
	{
		return false;
	}
	
	// 8.17
	public double queryLogicalTime()
	{
		return 0.0;
	}

	// 8.18
	public boolean queryLITS()
	{
		return false;
	}

	// 8.19
	public void modifyLookahead( double theLookahead )
	{
		
	}

	// 8.20
	public double queryLookahead()
	{
		return 0.0;
	}

	// 8.21
	public void retract( int theHandle )
	{
		
	}

	// 8.23
	public void changeAttributeOrderType( int theObject, int[] attributes, int orderType )
	{
		
	}

	// 8.24
	public void changeInteractionOrderType( int theClass, int orderType )
	{
		
	}

	//////////////////////////////////
	// Data Distribution Management //
	//////////////////////////////////

	// 9.2
	int createRegion( int[] dimensions )
	{
		return 1;
	}

	// 9.3
	public void commitRegionModifications( int[] regions )
	{
		
	}

	// 9.4
	public void deleteRegion( int theRegion )
	{
		
	}

	//9.5
	int registerObjectInstanceWithRegions( int theClass, int[] attributes, int[] regions )
	{
		return 1;
	}

	//9.5
	int registerObjectInstanceWithRegions( int theClass,
	                                       int[] attributes,
	                                       int[] regions,
	                                       String objectName )
	{
		return 1;
	}

	// 9.6
	public void associateRegionsForUpdates( int theObject,
	                                        int[] attributes,
	                                        int[] regions )
	{
		
	}

	// 9.7
	public void unassociateRegionsForUpdates( int theObject,
	                                          int[] attributes,
	                                          int[] regions )
	{
		
	}

	// 9.8
	public void subscribeObjectClassAttributesWithRegions( int  theClass,
	                                                       int[] attributes,
	                                                       int[] regions )
	{
		
	}

	// 9.8
	public void subscribeObjectClassAttributesWithRegions( int theClass,
	                                                       int[] attributes,
	                                                       int[] regions,
	                                                       String updateRate )
	{
		
	}

	// 9.8
	public void subscribeObjectClassAttributesPassivelyWithRegions( int theClass,
	                                                                int[] attributes,
	                                                                int[] regions )
	{
		
	}

	// 9.8
	public void subscribeObjectClassAttributesPassivelyWithRegions( int theClass,
	                                                                int[] attributes,
	                                                                int[] regions,
	                                                                String updateRate )
	{
		
	}

	// 9.9
	public void unsubscribeObjectClassAttributesWithRegions( int theClass,
	                                                         int[] attributes,
	                                                         int[] regions )
	{
		
	}

	// 9.10
	public void subscribeInteractionClassWithRegions( int theClass, int[] regions )
	{
		
	}

	// 9.10
	public void subscribeInteractionClassPassivelyWithRegions( int theClass, int[] regions )
	{
		
	}

	// 9.11
	public void unsubscribeInteractionClassWithRegions( int theClass, int[] regions )
	{
		
	}

	//9.12
	public void sendInteractionWithRegions( int theInteraction,
	                                        int[] parameters,
	                                        byte[][] regions,
	                                        byte[] tag )
	{
		
	}

	//9.12
	int sendInteractionWithRegions( int theInteraction,
	                                int[] parameters,
	                                byte[][] values,
	                                int[] regions,
	                                byte[] tag,
	                                double time )
	{
		return 1;
	}

	// 9.13
	public void requestAttributeValueUpdateWithRegions( int theClass,
	                                                    int[] attributes,
	                                                    int[] regions,
	                                                    byte[] tag )
	{
		
	}

	//////////////////////////
	// RTI Support Services //
	//////////////////////////

	// 10.2
	String getAutomaticResignDirective()
	{
		return "NO_ACTION";
	}


	// 10.3
	public void setAutomaticResignDirective( String resignAction )
	{
		
	}

	// 10.4
	int getFederateHandle( String theName )
	{
		return 1;
	}


	// 10.5
	String getFederateName( int theHandle )
	{
		return "";
	}

	// 10.6
	int getObjectClassHandle( String theName )
	{
		return 1;
	}

	// 10.7
	String getObjectClassName( int theHandle )
	{
		return "";
	}

	// 10.8
	int getKnownObjectClassHandle( int theObject )
	{
		return 1;
	}

	// 10.9
	int getObjectInstanceHandle( String theName )
	{
		return 1;
	}

	// 10.10
	String getObjectInstanceName( int theHandle )
	{
		return "";
	}

	// 10.11
	int getAttributeHandle( int whichClass, String theName )
	{
		return 1;
	}

	// 10.12
	String getAttributeName( int whichClass, int theHandle )
	{
		return "";
	}

	// 10.13
	double getUpdateRateValue( String updateRateDesignator )
	{
		return 0.0;
	}

	// 10.14
	double getUpdateRateValueForAttribute( int theObject, int theAttribute )
	{
		return 0.0;
	}

	// 10.15
	int getInteractionClassHandle( String theName )
	{
		return 1;
	}

	// 10.16
	String getInteractionClassName( int theHandle )
	{
		return "";
	}

	// 10.17
	int getParameterHandle( int whichClass, String theName )
	{
		return 1;
	}

	// 10.18
	String getParameterName( int whichClass, int theHandle )
	{
		return  "";
	}

	// 10.19
	String getOrderType( String theName )
	{
		return "";
	}

	// 10.20
	String getOrderName( String theType )
	{
		return "";
	}

	// 10.21
	String getTransportationTypeHandle( String theName )
	{
		return "";
	}

	// 10.22
	String getTransportationTypeName( String theHandle )
	{
		return "";
	}

	// 10.23
	int[] getAvailableDimensionsForClassAttribute( int whichClass, int theHandle )
	{
		return null;
	}

	// 10.24
	int[] getAvailableDimensionsForInteractionClass( int theHandle )
	{
		return null;
	}

	// 10.25
	int getDimensionHandle( String theName )
	{
		return 1;
	}

	// 10.26
	String getDimensionName( int theHandle )
	{
		return "";
	}

	// 10.27
	long getDimensionUpperBound( int theHandle )
	{
		return 1;
	}

	// 10.28
	int[] getDimensionHandleSet( int region )
	{
		return new int[]{};
	}

	// 10.29
	// RangeBounds => long[2]{ upper, lower }
	long[] getRangeBounds( int region, int dimension )
	{
		return new long[]{ 1, 1 };
	}

	// 10.30
	// int, int, RangeBounds
	public void setRangeBounds( int region, int dimension, long upperBound, long lowerBound )
	{
		
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
		
	}

	// 10.34
	public void disableObjectClassRelevanceAdvisorySwitch()
	{
		
	}

	// 10.35
	public void enableAttributeRelevanceAdvisorySwitch()
	{
		
	}

	// 10.36
	public void disableAttributeRelevanceAdvisorySwitch()
	{
		
	}

	// 10.37
	public void enableAttributeScopeAdvisorySwitch()
	{
		
	}

	// 10.38
	public void disableAttributeScopeAdvisorySwitch()
	{
		
	}

	// 10.39
	public void enableInteractionRelevanceAdvisorySwitch()
	{
		
	}

	// 10.40
	public void disableInteractionRelevanceAdvisorySwitch()
	{
		
	}

	// 10.41
	boolean evokeCallback( double minSeconds )
	{
logger.error( "evokeCallback("+minSeconds+")" );
		return false;
	}

	// 10.42
	boolean evokeMultipleCallbacks( double minSeconds, double maxSeconds )
	{
logger.error( "evokeMultipleCallbacks("+minSeconds+","+maxSeconds+")" );
		return false;
	}

	// 10.43
	public void enableCallbacks()
	{
		
	}

	// 10.44
	public void disableCallbacks()
	{
		
	}

	public String getHLAversion()
	{
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// C++ Library Loading Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	private void loadCppLibraries() throws RTIinternalError
	{
		// Load the C++ library so that we have a facility to call
		// back into C++ federates. All other classes where this could be
		// loaded should defer to here rather than reimplement this logic.
		boolean windows =
			System.getProperty("os.name").toUpperCase(Locale.ENGLISH).startsWith("WINDOWS");

		// set the initial log level for the C++ impl side logger
		Logger logger = Logger.getLogger( "cpp" );
		logger.setLevel( org.apache.log4j.Level.ERROR );
		
		// Try and load the Portico C++ library using the library names defined in the standard
		if( windows )
			loadCppLibrariesWindows();
		else
			loadCppLibrariesUnix();
	}

	private void loadCppLibrariesWindows() throws RTIinternalError
	{
		// WINDOWS //
		// let's assume that the library paths are set up properly in the first instance
		logger.debug( "Attempting to load librti1516e.dll from system path" );
		try
		{
			System.loadLibrary( "librti1516e" );
			return;
		}
		catch( UnsatisfiedLinkError ule )
		{
			// dammit!
			logger.debug( "Could not find librti1516e.dll on system path, searching RTI_HOME directly" );
		}
		catch( Throwable throwable )
		{
			logger.error( "An unknown error ("+throwable.getClass().getName()+
			              ") occurred trying to load librti1516e.dll from Java", throwable );
		}

		// try and load the library from RTI_HOME directly
		String rtihome = System.getenv( "RTI_HOME" );
		if( rtihome != null )
		{
			try
			{
				System.load( rtihome+"\\bin\\librti1516e.dll" );
			}
			catch( UnsatisfiedLinkError ule )
			{
				// dammit again!
			}
			catch( Throwable throwable )
			{
				logger.error( "ERROR An unknown error ("+throwable.getClass().getName()+
                    ") occurred trying to load librti1516e.dll from Java", throwable );
			}
		}
		
		// fail!
		logger.error( "Fatal error: RTI_HOME not set and librti1516e.dll not on system path" );
		logger.error( "Make sure %RTI_HOME% is set and/or %RTI_HOME%\\bin is on you %PATH%" );
		throw new RTIinternalError( "RTI_HOME not set and librti1516e.dll not on system path" );
	}

	private void loadCppLibrariesUnix() throws RTIinternalError
	{
		// let's assume that the library paths are set up properly in the first instance
		logger.debug( "Attempting to load librti1516e.so from system library path" );
		try
		{
			System.loadLibrary( "rti1516e" );
			return;
		}
		catch( UnsatisfiedLinkError ule )
		{
			// ignore for now, we'll log about this below
			logger.debug( "Could not find librti1516e.so on system path, searching RTI_HOME directly" );
		}
		catch( Throwable throwable )
		{
			logger.error( "An unknown error ("+throwable.getClass().getName()+
			              ") occurred trying to load librti1516e.so from Java", throwable );
		}
		
		// try and load the library from RTI_HOME directly
		String rtihome = System.getenv( "RTI_HOME" );
		if( rtihome != null )
		{
			try
			{
				System.load( rtihome+"/lib/librti1516e.so" );
			}
			catch( UnsatisfiedLinkError ule )
			{
				// dammit again!
			}
			catch( Throwable throwable )
			{
				logger.error( "ERROR An unknown error ("+throwable.getClass().getName()+
                    ") occurred trying to load librti1516e.so from Java", throwable );
			}
		}
		
		// fail!
		logger.error( "Fatal error: RTI_HOME not set and librti1516e.so not on library path" );
		logger.error( "Make sure $RTI_HOME is set and/or $RTI_HOME%/lib is on you LD_LIBRARY_PATH" );
		throw new RTIinternalError( "RTI_HOME not set and librti1516e.so not on library path" );
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
