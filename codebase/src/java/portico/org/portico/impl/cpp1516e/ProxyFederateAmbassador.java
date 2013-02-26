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

import java.util.Set;

import static org.portico.impl.hla1516e.types.HLA1516eHandle.*;

import hla.rti1516e.AttributeHandle;
import hla.rti1516e.AttributeHandleSet;
import hla.rti1516e.AttributeHandleValueMap;
import hla.rti1516e.FederateAmbassador;
import hla.rti1516e.FederateHandle;
import hla.rti1516e.FederateHandleSaveStatusPair;
import hla.rti1516e.FederateHandleSet;
import hla.rti1516e.FederateRestoreStatus;
import hla.rti1516e.FederationExecutionInformation;
import hla.rti1516e.FederationExecutionInformationSet;
import hla.rti1516e.InteractionClassHandle;
import hla.rti1516e.LogicalTime;
import hla.rti1516e.MessageRetractionHandle;
import hla.rti1516e.ObjectClassHandle;
import hla.rti1516e.ObjectInstanceHandle;
import hla.rti1516e.OrderType;
import hla.rti1516e.ParameterHandle;
import hla.rti1516e.ParameterHandleValueMap;
import hla.rti1516e.RegionHandle;
import hla.rti1516e.RegionHandleSet;
import hla.rti1516e.RestoreFailureReason;
import hla.rti1516e.SaveFailureReason;
import hla.rti1516e.SynchronizationPointFailureReason;
import hla.rti1516e.TransportationTypeHandle;
import hla.rti1516e.exceptions.FederateInternalError;
import hla.rti1516e.time.HLAfloat64Time;
import hla.rti1516e.time.HLAinteger64Time;

/**
 * When a C++ federate joins a federation, it calls into the Java layer where the
 * actual RTI logic sits. As such, to allow callbacks to process properly, we need
 * a Java Federate Ambassador to give to the Java RTI. Instances of this class provide
 * this, acting as a proxy for the C++ side of the binding. When callbacks are received,
 * they are passed on to an instance of the {@link FederateAmbassadorLink} class, which
 * has a set of native methods and acts as the link back into the C++ world.
 * <p/>
 * This class exists for two reasons. Firstly to act as a proxy for C++ federates so that
 * it can catch callsbacks and pass them over the JNI boundary correctly. Secondly, to
 * make the JNI code simpler to write, the native methods take mostly raw types (primitives,
 * arrays and the like). So the role of the proxy is to catch callbacks, translate them
 * into the basic types and pass them to the native methods in the link class.
 */
public class ProxyFederateAmbassador implements FederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private FederateAmbassadorLink link;
	private int ambassadorId;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ProxyFederateAmbassador( int id )
	{
		this.link = new FederateAmbassadorLink();
		this.ambassadorId = id;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// Helper Methods //////////////////////////////////// 
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return The value of the time object as a double if it is a {@link HLAfloat64Time}.
	 *         Otherwise, return -1.0.
	 */
	private double toDouble( LogicalTime time )
	{
		if( time instanceof HLAfloat64Time )
			return ((HLAfloat64Time)time).getValue();
		else
			return -1.0;
	}
	
	/**
	 * @return The value of the time object as a long if it is a {@link HLAinteger64Time}.
	 *         Otherwise, return -1.0.
	 */
	private long toLong( LogicalTime time )
	{
		if( time instanceof HLAinteger64Time )
			return ((HLAinteger64Time)time).getValue();
		else
			return -1;
	}

	private int[] convert( FederateHandleSet set )
	{
		int[] handles = new int[set.size()];
		int count = -1;
		for( FederateHandle handle : set )
			handles[++count] = fromHandle(handle);

		return handles;
	}
	
	private int[] convert( RegionHandleSet set )
	{
		int[] handles = new int[set.size()];
		int count = -1;
		for( RegionHandle handle : set )
			handles[++count] = fromHandle(handle);

		return handles;
	}
	
	private int[] convert( AttributeHandleSet set )
	{
		int[] handles = new int[set.size()];
		int count = -1;
		for( AttributeHandle handle : set )
			handles[++count] = fromHandle(handle);

		return handles;
	}
	
	private int convert( OrderType type )
	{
		// have to add one to the end because the STUPID IDIOT STANDARD DECLARES CONSTANT
		// VALUES FOR THESE THINGS BUT DOES NOT PROVIDE A MEANS TO ACCESS THAT VALUE.
		return type.ordinal()+1;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Federation Management Methods ////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void connectionLost( String faultDescription ) throws FederateInternalError
	{
		link.connectionLost( ambassadorId, faultDescription );
	}

	//4.7
	public void synchronizationPointRegistrationSucceeded( String label )
	    throws FederateInternalError
	{
		link.synchronizationPointRegistrationSucceeded( ambassadorId, label );
	}

	public void synchronizationPointRegistrationFailed( String label,
	                                                    SynchronizationPointFailureReason reason )
	    throws FederateInternalError
	{
		link.synchronizationPointRegistrationFailed( ambassadorId, label, reason.name() );
	}

	//4.8
	public void announceSynchronizationPoint( String label, byte[] tag )
		throws FederateInternalError
	{
		link.announceSynchronizationPoint( ambassadorId, label, tag );
	}

	//4.10
	public void federationSynchronized( String label, FederateHandleSet failedSet )
	    throws FederateInternalError
	{
		link.federationSynchronized( ambassadorId, label, convert(failedSet) );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	//4.12
	public void initiateFederateSave( String label ) throws FederateInternalError
	{
		link.initiateFederateSave( ambassadorId, label );
	}

	public void initiateFederateSave( String label, LogicalTime time ) throws FederateInternalError
	{
		if( time instanceof HLAfloat64Time )
			link.initiateFederateSave( ambassadorId, label, toDouble(time) );
		else
			link.initiateFederateSave( ambassadorId, label, toLong(time) );
	}

	// 4.15
	public void federationSaved() throws FederateInternalError
	{
		link.federationSaved( ambassadorId );
	}

	public void federationNotSaved( SaveFailureReason reason ) throws FederateInternalError
	{
		link.federationNotSaved( ambassadorId, reason.name() );
	}

	// 4.17
	public void federationSaveStatusResponse( FederateHandleSaveStatusPair[] response )
	    throws FederateInternalError
	{
		int[] handles = new int[response.length];
		String[] statuses = new String[response.length];
		for( int i = 0; i < response.length; i++ )
		{
			handles[i] = fromHandle( response[i].handle );
			statuses[i] = response[i].status.name();
		}
		
		link.federationSaveStatusResponse( ambassadorId, handles, statuses );
	}

	// 4.19
	public void requestFederationRestoreSucceeded( String label ) throws FederateInternalError
	{
		link.requestFederationRestoreSucceeded( ambassadorId, label );
	}

	public void requestFederationRestoreFailed( String label ) throws FederateInternalError
	{
		link.requestFederationRestoreFailed( ambassadorId, label );
	}

	// 4.20
	public void federationRestoreBegun() throws FederateInternalError
	{
		link.federationRestoreBegun( ambassadorId );
	}

	// 4.21
	public void initiateFederateRestore( String label,
	                                     String federateName,
	                                     FederateHandle federateHandle )
	    throws FederateInternalError
	{
		link.initiateFederateRestore( ambassadorId,
		                              label,
		                              federateName,
		                              fromHandle(federateHandle) );
	}

	// 4.23
	public void federationRestored() throws FederateInternalError
	{
		link.federationRestored( ambassadorId );
	}

	public void federationNotRestored( RestoreFailureReason reason ) throws FederateInternalError
	{
		link.federationNotRestored( ambassadorId, reason.name() );
	}

	// 4.25
	public void federationRestoreStatusResponse( FederateRestoreStatus[] response )
	    throws FederateInternalError
	{
		int[] preHandles = new int[response.length];
		int[] postHandles = new int[response.length];
		String[] statuses = new String[response.length];
		for( int i = 0; i < response.length; i++ )
		{
			preHandles[i] = fromHandle( response[i].preRestoreHandle );
			postHandles[i] = fromHandle( response[i].postRestoreHandle );
			statuses[i] = response[i].status.name();
		}
		
		link.federationRestoreStatusResponse( ambassadorId, preHandles, postHandles, statuses );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Reporting and Registration Methods ////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	public void reportFederationExecutions( FederationExecutionInformationSet set )
	    throws FederateInternalError
	{
		String[] federations = new String[set.size()];
		String[] implementations = new String[set.size()];
		int count = 0;
		for( FederationExecutionInformation info : set )
		{
			federations[count] = info.federationExecutionName;
			implementations[count] = info.logicalTimeImplementationName;
			count++;
		}
		
		link.reportFederationExecutions( ambassadorId, federations, implementations );
	}

	// 5.10
	public void startRegistrationForObjectClass( ObjectClassHandle theClass )
	    throws FederateInternalError
	{
		link.startRegistrationForObjectClass( ambassadorId, fromHandle(theClass) );
	}

	// 5.11
	public void stopRegistrationForObjectClass( ObjectClassHandle theClass )
	    throws FederateInternalError
	{
		link.stopRegistrationForObjectClass( ambassadorId, fromHandle(theClass) );
	}

	// 5.12
	public void turnInteractionsOn( InteractionClassHandle theHandle ) throws FederateInternalError
	{
		link.turnInteractionsOn( ambassadorId, fromHandle(theHandle) );
	}

	// 5.13
	public void turnInteractionsOff( InteractionClassHandle theHandle )
	    throws FederateInternalError
	{
		link.turnInteractionsOff( ambassadorId, fromHandle(theHandle) );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Object Name Methods ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 6.3
	public void objectInstanceNameReservationSucceeded( String objectName )
	    throws FederateInternalError
	{
		link.objectInstanceNameReservationSucceeded( ambassadorId, objectName );
	}

	public void multipleObjectInstanceNameReservationSucceeded( Set<String> objectNames )
	    throws FederateInternalError
	{
		link.multipleObjectInstanceNameReservationSucceeded( ambassadorId,
		                                                     objectNames.toArray(new String[]{}) );
	}

	public void objectInstanceNameReservationFailed( String objectName )
	    throws FederateInternalError
	{
		link.objectInstanceNameReservationFailed( ambassadorId, objectName );
	}

	public void multipleObjectInstanceNameReservationFailed( Set<String> objectNames )
	    throws FederateInternalError
	{
		link.multipleObjectInstanceNameReservationFailed( ambassadorId,
		                                                  objectNames.toArray(new String[]{}) );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Object Discovery Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 6.5
	public void discoverObjectInstance( ObjectInstanceHandle theObject,
	                                    ObjectClassHandle theObjectClass,
	                                    String objectName )
	    throws FederateInternalError
	{
		link.discoverObjectInstance( ambassadorId,
		                             fromHandle(theObject),
		                             fromHandle(theObjectClass),
		                             objectName );
	}

	public void discoverObjectInstance( ObjectInstanceHandle theObject,
	                                    ObjectClassHandle theObjectClass,
	                                    String objectName,
	                                    FederateHandle producingFederate )
	    throws FederateInternalError
	{
		link.discoverObjectInstance( ambassadorId,
		                             fromHandle(theObject),
		                             fromHandle(theObjectClass),
		                             objectName,
		                             fromHandle(producingFederate) );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Reflect Attributes Methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 6.7
	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] tag,
	                                    OrderType sentOrdering,
	                                    TransportationTypeHandle theTransport,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
		// convert the AttributeHandleValueMap
		int[] attributeHandles = new int[theAttributes.size()];
		byte[][] attributeValues = new byte[theAttributes.size()][];
		int count = 0;
		for( AttributeHandle handle : theAttributes.keySet() )
		{
			attributeHandles[count] = fromHandle(handle);
			attributeValues[count] = theAttributes.get(handle);
			count++;
		}
		
		link.reflectAttributeValues( ambassadorId,
		                             fromHandle(theObject),
		                             attributeHandles,
		                             attributeValues,
		                             tag,
		                             convert(sentOrdering),
		                             fromHandle(theTransport),
		                             fromHandle(reflectInfo.getProducingFederate()),
		                             convert(reflectInfo.getSentRegions()) );
	}

	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] tag,
	                                    OrderType sentOrdering,
	                                    TransportationTypeHandle theTransport,
	                                    LogicalTime theTime,
	                                    OrderType receivedOrdering,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
		// convert the AttributeHandleValueMap
		int[] attributeHandles = new int[theAttributes.size()];
		byte[][] attributeValues = new byte[theAttributes.size()][];
		int count = 0;
		for( AttributeHandle handle : theAttributes.keySet() )
		{
			attributeHandles[count] = fromHandle(handle);
			attributeValues[count] = theAttributes.get(handle);
			count++;
		}

		if( theTime instanceof HLAfloat64Time )
		{
    		link.reflectAttributeValues( ambassadorId,
    		                             fromHandle(theObject),
    		                             attributeHandles,
    		                             attributeValues,
    		                             tag,
    		                             convert(sentOrdering),
    		                             fromHandle(theTransport),
    		                             toDouble(theTime),
    		                             convert(receivedOrdering),
    		                             fromHandle(reflectInfo.getProducingFederate()),
    		                             convert(reflectInfo.getSentRegions()) );
		}
		else
		{
    		link.reflectAttributeValues( ambassadorId,
    		                             fromHandle(theObject),
    		                             attributeHandles,
    		                             attributeValues,
    		                             tag,
    		                             convert(sentOrdering),
    		                             fromHandle(theTransport),
    		                             toLong(theTime),
    		                             convert(receivedOrdering),
    		                             fromHandle(reflectInfo.getProducingFederate()),
    		                             convert(reflectInfo.getSentRegions()) );
		}
	}

	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] tag,
	                                    OrderType sentOrdering,
	                                    TransportationTypeHandle theTransport,
	                                    LogicalTime theTime,
	                                    OrderType receivedOrdering,
	                                    MessageRetractionHandle retractionHandle,
	                                    SupplementalReflectInfo reflectInfo )
	    throws FederateInternalError
	{
		// convert the AttributeHandleValueMap
		int[] attributeHandles = new int[theAttributes.size()];
		byte[][] attributeValues = new byte[theAttributes.size()][];
		int count = 0;
		for( AttributeHandle handle : theAttributes.keySet() )
		{
			attributeHandles[count] = fromHandle(handle);
			attributeValues[count] = theAttributes.get(handle);
			count++;
		}

		if( theTime instanceof HLAfloat64Time )
		{
        	link.reflectAttributeValues( ambassadorId,
        	                             fromHandle(theObject),
        	                             attributeHandles,
        	                             attributeValues,
        	                             tag,
        	                             convert(sentOrdering),
        	                             fromHandle(theTransport),
        	                             toDouble(theTime),
        	                             convert(receivedOrdering),
        	                             fromHandle(retractionHandle),
        	                             fromHandle(reflectInfo.getProducingFederate()),
        	                             convert(reflectInfo.getSentRegions()) );
		}
		else
		{
        	link.reflectAttributeValues( ambassadorId,
        	                             fromHandle(theObject),
        	                             attributeHandles,
        	                             attributeValues,
        	                             tag,
        	                             convert(sentOrdering),
        	                             fromHandle(theTransport),
        	                             toLong(theTime),
        	                             convert(receivedOrdering),
        	                             fromHandle(retractionHandle),
        	                             fromHandle(reflectInfo.getProducingFederate()),
        	                             convert(reflectInfo.getSentRegions()) );
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Receive Interaction Methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 6.9
	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle theTransport,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		// convert the ParameterHandleValueMap
		int[] parameterHandles = new int[theParameters.size()];
		byte[][] parameterValues = new byte[theParameters.size()][];
		int count = 0;
		for( ParameterHandle handle : theParameters.keySet() )
		{
			parameterHandles[count] = fromHandle(handle);
			parameterValues[count] = theParameters.get(handle);
			count++;
		}

		link.receiveInteraction( ambassadorId,
		                         fromHandle(interactionClass),
		                         parameterHandles,
		                         parameterValues,
		                         tag,
		                         convert(sentOrdering),
		                         fromHandle(theTransport),
		                         fromHandle(receiveInfo.getProducingFederate()),
		                         convert(receiveInfo.getSentRegions()) );
	}

	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle theTransport,
	                                LogicalTime theTime,
	                                OrderType receivedOrdering,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		// convert the ParameterHandleValueMap
		int[] parameterHandles = new int[theParameters.size()];
		byte[][] parameterValues = new byte[theParameters.size()][];
		int count = 0;
		for( ParameterHandle handle : theParameters.keySet() )
		{
			parameterHandles[count] = fromHandle(handle);
			parameterValues[count] = theParameters.get(handle);
			count++;
		}

		if( theTime instanceof HLAfloat64Time )
		{
    		link.receiveInteraction( ambassadorId,
    		                         fromHandle(interactionClass),
    		                         parameterHandles,
    		                         parameterValues,
    		                         tag,
    		                         convert(sentOrdering),
    		                         fromHandle(theTransport),
    		                         toDouble(theTime),
    		                         convert(receivedOrdering),
    		                         fromHandle(receiveInfo.getProducingFederate()),
    		                         convert(receiveInfo.getSentRegions()) );
		}
		else
		{
    		link.receiveInteraction( ambassadorId,
    		                         fromHandle(interactionClass),
    		                         parameterHandles,
    		                         parameterValues,
    		                         tag,
    		                         convert(sentOrdering),
    		                         fromHandle(theTransport),
    		                         toLong(theTime),
    		                         convert(receivedOrdering),
    		                         fromHandle(receiveInfo.getProducingFederate()),
    		                         convert(receiveInfo.getSentRegions()) );
		}
	}

	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters,
	                                byte[] tag,
	                                OrderType sentOrdering,
	                                TransportationTypeHandle theTransport,
	                                LogicalTime theTime,
	                                OrderType receivedOrdering,
	                                MessageRetractionHandle retractionHandle,
	                                SupplementalReceiveInfo receiveInfo )
	    throws FederateInternalError
	{
		// convert the ParameterHandleValueMap
		int[] parameterHandles = new int[theParameters.size()];
		byte[][] parameterValues = new byte[theParameters.size()][];
		int count = 0;
		for( ParameterHandle handle : theParameters.keySet() )
		{
			parameterHandles[count] = fromHandle(handle);
			parameterValues[count] = theParameters.get(handle);
			count++;
		}
		
		if( theTime instanceof HLAfloat64Time )
		{
    		link.receiveInteraction( ambassadorId,
    		                         fromHandle(interactionClass),
    		                         parameterHandles,
    		                         parameterValues,
    		                         tag,
    		                         convert(sentOrdering),
    		                         fromHandle(theTransport),
    		                         toDouble(theTime),
    		                         convert(receivedOrdering),
    		                         fromHandle(retractionHandle),
    		                         fromHandle(receiveInfo.getProducingFederate()),
    		                         convert(receiveInfo.getSentRegions()) );
		}
		else
		{
    		link.receiveInteraction( ambassadorId,
    		                         fromHandle(interactionClass),
    		                         parameterHandles,
    		                         parameterValues,
    		                         tag,
    		                         convert(sentOrdering),
    		                         fromHandle(theTransport),
    		                         toLong(theTime),
    		                         convert(receivedOrdering),
    		                         fromHandle(retractionHandle),
    		                         fromHandle(receiveInfo.getProducingFederate()),
    		                         convert(receiveInfo.getSentRegions()) );
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Remove Object Methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 6.11
	public void removeObjectInstance( ObjectInstanceHandle theObject,
	                                  byte[] tag,
	                                  OrderType sentOrdering,
	                                  SupplementalRemoveInfo removeInfo )
	    throws FederateInternalError
	{
		link.removeObjectInstance( ambassadorId,
		                           fromHandle(theObject),
		                           tag,
		                           convert(sentOrdering),
		                           fromHandle(removeInfo.getProducingFederate()) );
	}

	public void removeObjectInstance( ObjectInstanceHandle theObject,
	                                  byte[] tag,
	                                  OrderType sentOrdering,
	                                  LogicalTime theTime,
	                                  OrderType receivedOrdering,
	                                  SupplementalRemoveInfo removeInfo )
	    throws FederateInternalError
	{
		if( theTime instanceof HLAfloat64Time )
		{
			link.removeObjectInstance( ambassadorId,
			                           fromHandle(theObject),
			                           tag,
			                           convert(sentOrdering),
			                           toDouble(theTime),
			                           convert(receivedOrdering),
			                           fromHandle(removeInfo.getProducingFederate()) );
		}
		else
		{
			link.removeObjectInstance( ambassadorId,
			                           fromHandle(theObject),
			                           tag,
			                           convert(sentOrdering),
			                           toLong(theTime),
			                           convert(receivedOrdering),
			                           fromHandle(removeInfo.getProducingFederate()) );
		}
	}

	public void removeObjectInstance( ObjectInstanceHandle theObject,
	                                  byte[] tag,
	                                  OrderType sentOrdering,
	                                  LogicalTime theTime,
	                                  OrderType receivedOrdering,
	                                  MessageRetractionHandle retractionHandle,
	                                  SupplementalRemoveInfo removeInfo )
	    throws FederateInternalError
	{
		if( theTime instanceof HLAfloat64Time )
		{
			link.removeObjectInstance( ambassadorId,
			                           fromHandle(theObject),
			                           tag,
			                           convert(sentOrdering),
			                           toDouble(theTime),
			                           convert(receivedOrdering),
			                           fromHandle(retractionHandle),
			                           fromHandle(removeInfo.getProducingFederate()) );
		}
		else
		{
			link.removeObjectInstance( ambassadorId,
			                           fromHandle(theObject),
			                           tag,
			                           convert(sentOrdering),
			                           toLong(theTime),
			                           convert(receivedOrdering),
			                           fromHandle(retractionHandle),
			                           fromHandle(removeInfo.getProducingFederate()) );
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Relevancy Advisory Methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 6.15
	public void attributesInScope( ObjectInstanceHandle theObject, AttributeHandleSet theAttributes )
	    throws FederateInternalError
	{
		link.attributesInScope( ambassadorId, fromHandle(theObject), convert(theAttributes) );
	}

	// 6.16
	public void attributesOutOfScope( ObjectInstanceHandle theObject,
	                                  AttributeHandleSet theAttributes )
	    throws FederateInternalError
	{
		link.attributesOutOfScope( ambassadorId, fromHandle(theObject), convert(theAttributes) );
	}

	// 6.18
	public void provideAttributeValueUpdate( ObjectInstanceHandle theObject,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] tag ) throws FederateInternalError
	{
		link.provideAttributeValueUpdate( ambassadorId,
		                                  fromHandle(theObject),
		                                  convert(theAttributes),
		                                  tag );
	}

	// 6.19
	public void turnUpdatesOnForObjectInstance( ObjectInstanceHandle theObject,
	                                            AttributeHandleSet theAttributes )
	    throws FederateInternalError
	{
		link.turnUpdatesOnForObjectInstance( ambassadorId,
		                                     fromHandle(theObject),
		                                     convert(theAttributes) );
	}

	public void turnUpdatesOnForObjectInstance( ObjectInstanceHandle theObject,
	                                            AttributeHandleSet theAttributes,
	                                            String updateRateDesignator )
	    throws FederateInternalError
	{
		link.turnUpdatesOnForObjectInstance( ambassadorId,
		                                     fromHandle(theObject),
		                                     convert(theAttributes),
		                                     updateRateDesignator );
	}

	// 6.20
	public void turnUpdatesOffForObjectInstance( ObjectInstanceHandle theObject,
	                                             AttributeHandleSet theAttributes )
	    throws FederateInternalError
	{
		link.turnUpdatesOffForObjectInstance( ambassadorId,
		                                      fromHandle(theObject),
		                                      convert(theAttributes) );
	}

	// 6.20
	public void confirmAttributeTransportationTypeChange( ObjectInstanceHandle theObject,
	                                                      AttributeHandleSet theAttributes,
	                                                      TransportationTypeHandle theTransport )
	    throws FederateInternalError
	{
		link.confirmAttributeTransportationTypeChange( ambassadorId,
		                                               fromHandle(theObject),
		                                               convert(theAttributes),
		                                               fromHandle(theTransport) );
	}

	// 6.20
	public void confirmInteractionTransportationTypeChange( InteractionClassHandle theInteraction,
	                                                        TransportationTypeHandle theTransport )
	    throws FederateInternalError
	{
		link.confirmInteractionTransportationTypeChange( ambassadorId,
		                                                 fromHandle(theInteraction),
		                                                 fromHandle(theTransport) );
	}

	// 6.20
	public void reportAttributeTransportationType( ObjectInstanceHandle theObject,
	                                               AttributeHandle theAttribute,
	                                               TransportationTypeHandle theTransport )
	    throws FederateInternalError
	{
		link.reportAttributeTransportationType( ambassadorId,
		                                        fromHandle(theObject),
		                                        fromHandle(theAttribute),
		                                        fromHandle(theTransport) );
	}

	// 6.20
	public void reportInteractionTransportationType( FederateHandle theFederate,
	                                                 InteractionClassHandle theInteraction,
	                                                 TransportationTypeHandle theTransport )
	    throws FederateInternalError
	{
		link.reportInteractionTransportationType( ambassadorId,
		                                          fromHandle(theFederate),
		                                          fromHandle(theInteraction),
		                                          fromHandle(theTransport) );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Ownership Management Methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 7.4
	public void requestAttributeOwnershipAssumption( ObjectInstanceHandle theObject,
	                                                 AttributeHandleSet offeredAttributes,
	                                                 byte[] tag )
	    throws FederateInternalError
	{
		link.requestAttributeOwnershipAssumption( ambassadorId,
		                                          fromHandle(theObject),
		                                          convert(offeredAttributes),
		                                          tag );
	}

	// 7.5
	public void requestDivestitureConfirmation( ObjectInstanceHandle theObject,
	                                            AttributeHandleSet offeredAttributes )
	    throws FederateInternalError
	{
		link.requestDivestitureConfirmation( ambassadorId,
		                                     fromHandle(theObject),
		                                     convert(offeredAttributes) );
	}

	// 7.7
	public void attributeOwnershipAcquisitionNotification( ObjectInstanceHandle theObject,
	                                                       AttributeHandleSet securedAttributes,
	                                                       byte[] tag )
	    throws FederateInternalError
	{
		link.attributeOwnershipAcquisitionNotification( ambassadorId,
		                                                fromHandle(theObject),
		                                                convert(securedAttributes),
		                                                tag );
	}

	// 7.10
	public void attributeOwnershipUnavailable( ObjectInstanceHandle theObject,
	                                           AttributeHandleSet theAttributes )
	    throws FederateInternalError
	{
		link.attributeOwnershipUnavailable( ambassadorId,
		                                    fromHandle(theObject),
		                                    convert(theAttributes) );
	}

	// 7.11
	public void requestAttributeOwnershipRelease( ObjectInstanceHandle theObject,
	                                              AttributeHandleSet candidateAttributes,
	                                              byte[] tag )
	    throws FederateInternalError
	{
		link.requestAttributeOwnershipRelease( ambassadorId,
		                                       fromHandle(theObject),
		                                       convert(candidateAttributes),
		                                       tag );
	}

	// 7.15
	public void confirmAttributeOwnershipAcquisitionCancellation( ObjectInstanceHandle theObject,
	                                                              AttributeHandleSet theAttributes )
	    throws FederateInternalError
	{
		link.confirmAttributeOwnershipAcquisitionCancellation( ambassadorId,
		                                                       fromHandle(theObject),
		                                                       convert(theAttributes) );
	}

	// 7.17
	public void informAttributeOwnership( ObjectInstanceHandle theObject,
	                                      AttributeHandle theAttribute,
	                                      FederateHandle theOwner )
	    throws FederateInternalError
	{
		link.informAttributeOwnership( ambassadorId,
		                               fromHandle(theObject),
		                               fromHandle(theAttribute),
		                               fromHandle(theOwner) );
	}

	public void attributeIsNotOwned( ObjectInstanceHandle theObject, AttributeHandle theAttribute )
	    throws FederateInternalError
	{
		link.attributeIsNotOwned( ambassadorId, fromHandle(theObject), fromHandle(theAttribute) );
	}

	public void attributeIsOwnedByRTI( ObjectInstanceHandle theObject, AttributeHandle theAttribute )
	    throws FederateInternalError
	{
		link.attributeIsOwnedByRTI( ambassadorId, fromHandle(theObject), fromHandle(theAttribute) );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Time Management Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 8.3
	public void timeRegulationEnabled( LogicalTime time ) throws FederateInternalError
	{
		if( time instanceof HLAfloat64Time )
			link.timeRegulationEnabled( ambassadorId, toDouble(time) );
		else
			link.timeRegulationEnabled( ambassadorId, toLong(time) );
	}

	// 8.6
	public void timeConstrainedEnabled( LogicalTime time ) throws FederateInternalError
	{
		if( time instanceof HLAfloat64Time )
			link.timeConstrainedEnabled( ambassadorId, toDouble(time) );
		else
			link.timeConstrainedEnabled( ambassadorId, toLong(time) );
	}

	// 8.13
	public void timeAdvanceGrant( LogicalTime time ) throws FederateInternalError
	{
		if( time instanceof HLAfloat64Time )
			link.timeAdvanceGrant( ambassadorId, toDouble(time) );
		else
			link.timeAdvanceGrant( ambassadorId, toLong(time) );
	}

	// 8.22
	public void requestRetraction( MessageRetractionHandle theHandle ) throws FederateInternalError
	{
		link.requestRetraction( ambassadorId, fromHandle(theHandle) );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
