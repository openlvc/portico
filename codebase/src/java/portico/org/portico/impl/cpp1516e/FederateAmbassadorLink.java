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

/**
 * This class provides a set of native methods that allow the RTI to call back to
 * C++ based federate ambassadors. For each C++ federate, we create an intermediate
 * {@link ProxyFederateAmbassador} that we give to the RTI. That object has a reference
 * to an instance of this class. When it gets callbacks, it calls this instance which
 * in turns hands the callbacks over to the native C++ implementation.
 * <p/>
 * This class exists for two reasons. Firstly, having separate classes for our proxy
 * federate ambassador and the native link allows us to reuse method names without
 * cluttering up the one class (collecting all the natives together here make things
 * a bit nearter). Secondly, to make the JNI code simpler to write and work with on
 * the C++ side, the native methods take mostly raw types (primitives, arrays and the
 * like). So the role of the proxy is to catch callbacks, translate them and pass them
 * to the native methods of this class in their simplified form.
 * <p/>
 * <b>Logical Time Notes</b>
 * <p/>
 * Because it's annoying, the HLA provides an abstraction for time. In HLA 1.3 we just 
 * provided a single time type for the user and refused to take anything else. Life was
 * crap but we got over it. In 1516e there are now two standard time types, one backed
 * by a 64-bit float, the other a 64-bit integer. Now we have to support both types, but
 * I don't want to double up on overloads for each time related method (one for each).
 * So, as a horrible compromise I've done the following: Everywhere time can be passed
 * I've expaneded this out into two arguments, on for a double and one for a long. If the
 * double time type is used, it's value is passed and -1 is provided for the long (and
 * vice versa), thus allowing the C++ side to determine which type is in use. Fun, no?
 */
public class FederateAmbassadorLink
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
	public FederateAmbassadorLink()
	{		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public native void connectionLost( int id, String description );

	//4.7
	public native void synchronizationPointRegistrationSucceeded( int id, String label );

	public native void synchronizationPointRegistrationFailed( int id, String label, String reason );

	//4.8
	public native void announceSynchronizationPoint( int id, String label, byte[] tag );

	//4.10
	public native void federationSynchronized( int id, String label, int[] failedSet );

	//4.12
	public native void initiateFederateSave( int id, String label );

	public native void initiateFederateSave( int id, String label, double doubleTime );
	public native void initiateFederateSave( int id, String label, long longTime );

	// 4.15
	public native void federationSaved( int id );

	public native void federationNotSaved( int id, String reason );

	// 4.17
	// The intSaveStatusPair argument is replaced by two arrays (one for each part)
	public native void federationSaveStatusResponse( int id, int[] federates, String[] statuses );

	// 4.19
	public native void requestFederationRestoreSucceeded( int id, String label );

	public native void requestFederationRestoreFailed( int id, String label );

	// 4.20
	public native void federationRestoreBegun( int id );

	// 4.21
	public native void initiateFederateRestore( int id,
	                                            String label,
	                                            String federateName,
	                                            int federateHandle );

	// 4.23
	public native void federationRestored( int id );

	public native void federationNotRestored( int id, String reason );

	// 4.25
	// FederateRestoreStatus expanded into:
	//    -> int[] preHandles
	//    -> int[] postHandles
	//    -> String[] statuses
	public native void federationRestoreStatusResponse( int id,
	                                                    int[] preHandles,
	                                                    int[] postHandles,
	                                                    String[] statuses );
	
	public native void reportFederationExecutions( int id,
	                                               String[] federations,
	                                               String[] timeImplementations );

	// 5.10
	public native void startRegistrationForObjectClass( int id, int classHandle );

	// 5.11
	public native void stopRegistrationForObjectClass( int id, int classHandle );

	// 5.12
	public native void turnInteractionsOn( int id, int interactionHandle );

	// 5.13
	public native void turnInteractionsOff( int id, int interactionHandle );

	// 6.3
	public native void objectInstanceNameReservationSucceeded( int id, String objectName );

	public native void multipleObjectInstanceNameReservationSucceeded( int id, String[] objectNames );

	public native void objectInstanceNameReservationFailed( int id, String objectName );

	public native void multipleObjectInstanceNameReservationFailed( int id, String[] objectNames );

	// 6.5
	public native void discoverObjectInstance( int id,
	                                           int theObject,
	                                           int theObjectClass,
	                                           String objectName );

	public native void discoverObjectInstance( int id,
	                                           int theObject,
	                                           int theObjectClass,
	                                           String objectName,
	                                           int producingFederate );

	// 6.7
	// intValueMap expanded into:
	//    -> int[] attributeHandles
	//    -> byte[][] attributeValues
	//
	// SupplementalReflectInfo expanded into:
	//    -> int producingFederate (-1 indicates not provided)
	//    -> int[] regionHandle (0-length array indicates not provided)
	public native void reflectAttributeValues( int id,
	                                           int theObject,
	                                           int[] attributeHandles,   //intValueMap
	                                           byte[][] attributeValues, //intValueMap
	                                           byte[] tag,
	                                           int sentOrdering,         //OrderType
	                                           int theTransport,         //int
	                                           int producingFederate,    //SupplementalReflectInfo
	                                           int[] regionHandles );    //SupplementalReflectInfo

	//////////////////////////////////////////////////////////
	// due to time implementations we converts to two calls //
	//////////////////////////////////////////////////////////
	public native void reflectAttributeValues( int id,
	                                           int theObject,
	                                           int[] attributeHandles,   //intValueMap
	                                           byte[][] attributeValues, //intValueMap
	                                           byte[] tag,
	                                           int sentOrdering,         //OrderType
	                                           int theTransport,         //TransportationTypeHandle
	                                           double doubleTime,        //LogicalTime     
	                                           int receivedOrdering,     //OrderType
	                                           int producingFederate,    //SupplementalReflectInfo
	                                           int[] regionHandles );    //SupplementalReflectInfo

	public native void reflectAttributeValues( int id,
	                                           int theObject,
	                                           int[] attributeHandles,   //intValueMap
	                                           byte[][] attributeValues, //intValueMap
	                                           byte[] tag,
	                                           int sentOrdering,         //OrderType
	                                           int theTransport,         //TransportationTypeHandle
	                                           long longTime,            //LogicalTime
	                                           int receivedOrdering,     //OrderType
	                                           int producingFederate,    //SupplementalReflectInfo
	                                           int[] regionHandles );    //SupplementalReflectInfo

	
	//////////////////////////////////////////////////////////
	// due to time implementations we converts to two calls //
	//////////////////////////////////////////////////////////
	public native void reflectAttributeValues( int id,
	                                           int theObject,
	                                           int[] attributeHandles,   //intValueMap
	                                           byte[][] attributeValues, //intValueMap
	                                           byte[] tag,
	                                           int sentOrdering,         //OrderType
	                                           int theTransport,         //TransportationTypeHandle
	                                           double doubleTime,        //LogicalTime
	                                           int receivedOrdering,     //OrderType
	                                           int retractionHandle,
	                                           int producingFederate,    //SupplementalReflectInfo
	                                           int[] regionHandles );    //SupplementalReflectInfo

	public native void reflectAttributeValues( int id,
	                                           int theObject,
	                                           int[] attributeHandles,   //intValueMap
	                                           byte[][] attributeValues, //intValueMap
	                                           byte[] tag,
	                                           int sentOrdering,         //OrderType
	                                           int theTransport,         //TransportationTypeHandle
	                                           long longTime,            //LogicalTime
	                                           int receivedOrdering,     //OrderType
	                                           int retractionHandle,
	                                           int producingFederate,    //SupplementalReflectInfo
	                                           int[] regionHandles );    //SupplementalReflectInfo

	// 6.9
	// intValueMap expaanded into:
	//    -> int[] parameterHandles
	//    -> byte[][] parameterValues
	//
	// SupplementalReceiveInfo expanded into:
	//    -> int producingFederate (-1 indicates not provided)
	//    -> int[] regionHandle (0-length array indicates not provided)
	public native void receiveInteraction( int id,
	                                       int interactionClass,
	                                       int[] parameterHandles,       //intValueMap
	                                       byte[][] parameterValues,     //intValueMap
	                                       byte[] tag,
	                                       int sentOrdering,             //OrderType
	                                       int theTransport,             //TransportationTypeHandle
	                                       int producingFederate,        //SupplementalReceiveInfo
	                                       int[] regionHandles );        //SupplementalReceiveInfo

	//////////////////////////////////////////////////////////
	// due to time implementations we converts to two calls //
	//////////////////////////////////////////////////////////
	public native void receiveInteraction( int id,
	                                       int interactionClass,
	                                       int[] parameterHandles,       //intValueMap
	                                       byte[][] parameterValues,     //intValueMap
	                                       byte[] tag,
	                                       int sentOrdering,             //OrderType
	                                       int theTransport,             //TransportationTypeHandle
	                                       double doubleTime,            //LogicalTime
	                                       int receivedOrdering,         //OrderType
	                                       int producingFederate,        //SupplementalReceiveInfo
	                                       int[] regionHandles );        //SupplementalReceiveInfo

	public native void receiveInteraction( int id,
	                                       int interactionClass,
	                                       int[] parameterHandles,       //intValueMap
	                                       byte[][] parameterValues,     //intValueMap
	                                       byte[] tag,
	                                       int sentOrdering,             //OrderType
	                                       int theTransport,             //TransportationTypeHandle
	                                       long longTime,                //LogicalTime
	                                       int receivedOrdering,         //OrderType
	                                       int producingFederate,        //SupplementalReceiveInfo
	                                       int[] regionHandles );        //SupplementalReceiveInfo

	//////////////////////////////////////////////////////////
	// due to time implementations we converts to two calls //
	//////////////////////////////////////////////////////////
	public native void receiveInteraction( int id,
	                                       int interactionClass,
	                                       int[] parameterHandles,       //intValueMap
	                                       byte[][] parameterValues,     //intValueMap
	                                       byte[] tag,
	                                       int sentOrdering,             //OrderType
	                                       int theTransport,             //TransportationTypeHandle
	                                       double doubleTime,            //LogicalTime
	                                       int receivedOrdering,         //OrderType
	                                       int retractionHandle,
	                                       int producingFederate,        //SupplementalReceiveInfo
	                                       int[] regionHandles );        //SupplementalReceiveInfo

	public native void receiveInteraction( int id,
	                                       int interactionClass,
	                                       int[] parameterHandles,       //intValueMap
	                                       byte[][] parameterValues,     //intValueMap
	                                       byte[] tag,
	                                       int sentOrdering,             //OrderType
	                                       int theTransport,             //TransportationTypeHandle
	                                       long longTime,                //LogicalTime
	                                       int receivedOrdering,         //OrderType
	                                       int retractionHandle,
	                                       int producingFederate,        //SupplementalReceiveInfo
	                                       int[] regionHandles );        //SupplementalReceiveInfo

	// 6.11
	// intValueMap expaanded into:
	//    -> int[] parameterHandles
	//    -> byte[][] parameterValues
	//
	// SupplementalReceiveInfo expanded into:
	//    -> int producingFederate (-1 indicates not provided)
	//    -> int[] regionHandle (0-length array indicates not provided)
	public native void removeObjectInstance( int id,
	                                         int theObject,
	                                         byte[] tag,
	                                         int sentOrdering,           //OrderType
	                                         int producingFederate );    //SupplementalRemoveInfo

	//////////////////////////////////////////////////////////
	// due to time implementations we converts to two calls //
	//////////////////////////////////////////////////////////
	public native void removeObjectInstance( int id,
	                                         int theObject,
	                                         byte[] tag,
	                                         int sentOrdering,           //OrderType
	                                         double doubleTime,          //LogicalTime
	                                         int receivedOrdering,       //OrderType
	                                         int producingFederate );    //SupplementalRemoveInfo

	public native void removeObjectInstance( int id,
	                                         int theObject,
	                                         byte[] tag,
	                                         int sentOrdering,           //OrderType
	                                         long longTime,              //LogicalTime
	                                         int receivedOrdering,       //OrderType
	                                         int producingFederate );    //SupplementalRemoveInfo

	//////////////////////////////////////////////////////////
	// due to time implementations we converts to two calls //
	//////////////////////////////////////////////////////////
	public native void removeObjectInstance( int id,
	                                         int theObject,
	                                         byte[] tag,
	                                         int sentOrdering,           //OrderType
	                                         double doubleTime,          //LogicalTime
	                                         int receivedOrdering,       //OrderType
	                                         int retractionHandle,
	                                         int producingFederate );    //SupplementalRemoveInfo

	public native void removeObjectInstance( int id,
	                                         int theObject,
	                                         byte[] tag,
	                                         int sentOrdering,           //OrderType
	                                         long longTime,              //LogicalTime
	                                         int receivedOrdering,       //OrderType
	                                         int retractionHandle,
	                                         int producingFederate );    //SupplementalRemoveInfo

	
	// 6.15
	public native void attributesInScope( int id, int theObject, int[] theAttributes );

	// 6.16
	public native void attributesOutOfScope( int id, int theObject, int[] theAttributes );

	// 6.18
	public native void provideAttributeValueUpdate( int id,
	                                                int theObject,
	                                                int[] theAttributes,
	                                                byte[] tag );

	// 6.19
	public native void turnUpdatesOnForObjectInstance( int id, int theObject, int[] theAttributes );

	public native void turnUpdatesOnForObjectInstance( int id,
	                                                   int theObject,
	                                                   int[] theAttributes,
	                                                   String updateRateDesignator );

	// 6.20
	public native void turnUpdatesOffForObjectInstance( int id,
	                                                    int theObject,
	                                                    int[] theAttributes );

	// 6.20
	public native void confirmAttributeTransportationTypeChange( int id,
	                                                             int theObject,
	                                                             int[] theAttributes,
	                                                             int transportHandle );

	// 6.20
	public native void confirmInteractionTransportationTypeChange( int id,
	                                                               int theInteraction,
	                                                               int theTransport );

	// 6.20
	public native void reportAttributeTransportationType( int id,
	                                                      int theObject,
	                                                      int theAttribute,
	                                                      int theTransportation );

	// 6.20
	public native void reportInteractionTransportationType( int id,
	                                                        int theFederate,
	                                                        int theInteraction,
	                                                        int theTransport );

	// 7.4
	public native void requestAttributeOwnershipAssumption( int id,
	                                                        int theObject,
	                                                        int[] offeredAttributes,
	                                                        byte[] tag );

	// 7.5
	public native void requestDivestitureConfirmation( int id,
	                                                   int theObject,
	                                                   int[] offeredAttributes );

	// 7.7
	public native void attributeOwnershipAcquisitionNotification( int id,
	                                                              int theObject,
	                                                              int[] securedAttributes,
	                                                              byte[] tag );

	// 7.10
	public native void attributeOwnershipUnavailable( int id, int theObject, int[] theAttributes );

	// 7.11
	public native void requestAttributeOwnershipRelease( int id,
	                                                     int theObject,
	                                                     int[] candidateAttributes,
	                                                     byte[] tag );

	// 7.15
	public native void confirmAttributeOwnershipAcquisitionCancellation( int id,
	                                                                     int theObject,
	                                                                     int[] theAttributes );

	// 7.17
	public native void informAttributeOwnership( int id,
	                                             int theObject,
	                                             int theAttribute,
	                                             int theOwner );

	public native void attributeIsNotOwned( int id, int theObject, int theAttribute );

	public native void attributeIsOwnedByRTI( int id, int theObject, int theAttribute );

	//////////////////////////////////////////////////////////
	// due to time implementations we converts to two calls //
	//////////////////////////////////////////////////////////
	// 8.3
	public native void timeRegulationEnabled( int id, double doubleTime );
	public native void timeRegulationEnabled( int id, long longTime );

	// 8.6
	public native void timeConstrainedEnabled( int id, double doubleTime );
	public native void timeConstrainedEnabled( int id, long longTime );

	// 8.13
	public native void timeAdvanceGrant( int id, double doubleTime );
	public native void timeAdvanceGrant( int id, long longTime );

	// 8.22
	public native void requestRetraction( int id, int theHandle );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
