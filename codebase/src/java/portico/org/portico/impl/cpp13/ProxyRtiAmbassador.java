/*
 *   Copyright 2007 The Portico Project
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
package org.portico.impl.cpp13;

import static org.portico.lrc.PorticoConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.lang.String;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.impl.hla13.Rti13Ambassador;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;
import org.portico.impl.hla13.types.HLA13AttributeHandleSet;
import org.portico.impl.hla13.types.HLA13FederateHandleSet;
import org.portico.impl.hla13.types.HLA13SuppliedAttributes;
import org.portico.impl.hla13.types.HLA13SuppliedParameters;

import hla.rti.CouldNotOpenFED;
import hla.rti.EventRetractionHandle;
import hla.rti.FederateHandleSet;
import hla.rti.LogicalTime;
import hla.rti.LogicalTimeInterval;
import hla.rti.Region;

/**
 * This class is the RTIambassador implementation called by the C++ interface. It is a thin wrapper
 * over the existing HLA 1.3 RTIambassador, serving only to do any necessary deserialization of
 * information as it comes across the wire.
 */
public class ProxyRtiAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Rti13Ambassador rtiamb;
	private ProxyFederateAmbassador fedamb;
	private int id; // an id that represents the C++ JavaRTI object
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new RTIambassador that will be used by a federate on the C++ side of the bindings.
	 */
	public ProxyRtiAmbassador( int id ) throws Exception
	{
		// store the id
		this.id = id;
		
		// create the ambassador
		// this may throw an exception, which we would normally push through
		// the ExceptionManager. However, the exception manager depends on the
		// c++ instance that triggered the constructor to be in an accessible
		// map. However, this only happenes after the consturction of that instance.
		// In this case, we fall back on the low-level JNI exception handling
		// mechanics, hence the reason we don't catch the exception and just let
		// it flow on through to the C++ side
		this.rtiamb = new Rti13Ambassador();
		
		// fetch the LRC logger so that we have somewhere to notify of our events
		this.logger = LogManager.getFormatterLogger( "portico.lrc.cpp13" );
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
	private HLA13SuppliedParameters convertParameters( int[] handles, byte[][] values )
	{
		HLA13SuppliedParameters theParameters = new HLA13SuppliedParameters();
		for( int i = 0; i < handles.length; ++i )
		{
			theParameters.add( handles[i], values[i] );
		}
		
		return theParameters;
	}

	/**
	 * Converts the form that attribute values are sent over from C++ in into the form
	 * expected by the java side of the bindings.
	 */
	private HLA13SuppliedAttributes convertAttributes( int[] handles, byte[][] values )
	{
		HLA13SuppliedAttributes theAttributes = new HLA13SuppliedAttributes();
		for( int i = 0; i < handles.length; ++i )
		{
			theAttributes.add( handles[i], values[i] );
		}
		
		return theAttributes;
	}

	/**
	 * Converts the form that attribute handles are sent over from C++ in into the form expected
	 * by the java side of the bindings
	 */
	private HLA13AttributeHandleSet convertHandles( int[] handles )
	{
		HLA13AttributeHandleSet handleSet = new HLA13AttributeHandleSet();
		for( int handle : handles )
		{
			handleSet.add( handle );
		}
		
		return handleSet;
	}

	/**
	 * Converts an array of regions into a string form.
	 */
	private String regionsToString( Region[] array )
	{
		if( array == null || array.length == 0 )
			return "{empty}";
		
		// generate the return string
		StringBuilder builder = new StringBuilder( "{" );
		for( int i = 0; i < array.length; i++ )
		{
			builder.append( array[i] );
			if( i < array.length )
				builder.append( "," );
		}
		
		builder.append( "}" );
		return "";
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Federation Management Services //////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public void createFederationExecution( String executionName, String fed )
	{
		// log the request
		if( logger.isDebugEnabled() )
		{
			logger.debug( "[Request] createFederationExecution(): executionName=" + executionName +
			              ", fedFile=" + fed );
		}

		try
		{
			if( fed == null )
				throw new CouldNotOpenFED( "Fed location provided was null" );
			File file = new File( fed );
			this.rtiamb.createFederationExecution( executionName, file.toURI().toURL() );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void destroyFederationExecution( String executionName )
	{
		// log the request
		if( logger.isDebugEnabled() )
			logger.debug( "[Request] destroyFederationExecution(): name=" + executionName );
		
		try
		{
			this.rtiamb.destroyFederationExecution( executionName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public int joinFederationExecution( String federateType, String executionName )
	{
		// log the request
		if( logger.isDebugEnabled() )
		{
			logger.debug( "[Request] joinFederationExecution(): executionName=" + executionName +
			              ", federateName=" + federateType );
		}
		
		try
		{
			this.fedamb = new ProxyFederateAmbassador( this.id );
			return rtiamb.joinFederationExecution( federateType, executionName, this.fedamb );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public void resignFederationExecution( int resignAction )
	{
		// log the request
		if( logger.isDebugEnabled() )
			logger.debug( "[Request] resignFederationExecution(): action=" + resignAction );
		
		try
		{
			this.rtiamb.resignFederationExecution( resignAction );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void registerFederationSynchronizationPoint( String label, byte[] tag )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] registerFederationSynchronizationPoint(): label=" + label );
		
		try
		{
			this.rtiamb.registerFederationSynchronizationPoint( label, convertTag(tag) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void registerFederationSynchronizationPoint( String label, byte[] tag, int[] handles )
	{
		// log the request 
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] registerFederationSynchronizationPoint(): label=" + label +
			              ", federateHandles=" + arrayToString(handles) );
		}
		
		try
		{
    		FederateHandleSet synchronizationSet = new HLA13FederateHandleSet();
    		for( int federateHandle : handles )
    		{
    			synchronizationSet.add( federateHandle );
    		}
    
    		this.rtiamb.registerFederationSynchronizationPoint( label,
    		                                                    convertTag(tag),
    		                                                    synchronizationSet );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void synchronizationPointAchieved( String label )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] synchronizationPointAchieved(): label=" + label );
		
		try
		{
			this.rtiamb.synchronizationPointAchieved( label );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void requestFederationSave( String label, double time )
    {
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] requestFederationSave(): label=" + label + ", time=" + time );
		
		try
		{
			DoubleTime logicalTime = new DoubleTime( time );
			this.rtiamb.requestFederationSave( label, logicalTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
    }

	public void requestFederationSave( String label )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] requestFederationSave(): label=" + label );

		try
		{
			this.rtiamb.requestFederationSave( label );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void federateSaveBegun()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] federateSaveBegun()" );
		
		try
		{
			this.rtiamb.federateSaveBegun();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void federateSaveComplete()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] federateSaveComplete()" );
		
		try
		{
			this.rtiamb.federateSaveComplete();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void federateSaveNotComplete()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] federateSaveNotComplete()" );
		
		try
		{
			this.rtiamb.federateSaveNotComplete();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void requestFederationRestore( String label )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] requestFederationRestore(): label=" + label );
		
		try
		{
			this.rtiamb.requestFederationRestore( label );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void federateRestoreComplete()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] federateRestoreComplete()" );
		
		try
		{
			this.rtiamb.federateRestoreComplete();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void federateRestoreNotComplete()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] federateRestoreNotComplete()" );
		
		try
		{
			this.rtiamb.federateRestoreNotComplete();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Publish and Subscribe Services //////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////

	public void publishObjectClass( int theClass, int[] attributeHandles )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] publishObjectClass(): class=" + theClass +
			              ", handles=" + arrayToString(attributeHandles) );
		}
		
		try
		{
			this.rtiamb.publishObjectClass( theClass, convertHandles(attributeHandles) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void unpublishObjectClass( int theClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] unpublishObjectClass(): class=" + theClass );

		try
		{
			this.rtiamb.unpublishObjectClass( theClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void publishInteractionClass( int theInteraction )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] publishInteractionClass(): class=" + theInteraction );

		try
		{
			this.rtiamb.publishInteractionClass( theInteraction );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void unpublishInteractionClass( int theInteraction )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] unpublishInteractionClass(): class=" + theInteraction );

		try
		{
			this.rtiamb.unpublishInteractionClass( theInteraction );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}


	public void subscribeObjectClassAttributes( int theClass, int[] attributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] subscribeObjectClassAttributes(): class=" + theClass +
			              ", attributes=" + arrayToString(attributes) );
		}

		try
		{
    		this.rtiamb.subscribeObjectClassAttributes( theClass, convertHandles(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void subscribeObjectClassAttributesPassively( int theClass, int[] attributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] subscribeObjectClassAttributesPassively(): class=" + theClass +
			              ", attributes=" + arrayToString(attributes) );
		}

		try
		{
    		this.rtiamb.subscribeObjectClassAttributesPassively( theClass,
    		                                                     convertHandles(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void unsubscribeObjectClass( int theClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] unsubscribeObjectClass(): class=" + theClass );

		try
		{
			this.rtiamb.unsubscribeObjectClass( theClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void subscribeInteractionClass( int theClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] subscribeInteractionClass(): class=" + theClass );

		try
		{
			this.rtiamb.subscribeInteractionClass( theClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void subscribeInteractionClassPassively( int theClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] subscribeInteractionClassPassively(): class=" + theClass );

		try
		{
			this.rtiamb.subscribeInteractionClassPassively( theClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void unsubscribeInteractionClass( int theClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] unsubscribeInteractionClass(): class=" + theClass );

		try
		{
			this.rtiamb.unsubscribeInteractionClass( theClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Object Management Services ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public int registerObjectInstance( int theClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] registerObjectInstance(): class=" + theClass );
		
		try
		{
			return this.rtiamb.registerObjectInstance( theClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public int registerObjectInstance( int theClass, String theObject )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] registerObjectInstance(): class=" + theClass +
			              ", name=" + theObject );
		}
		
		try
		{
			return this.rtiamb.registerObjectInstance( theClass, theObject );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public void updateAttributeValues( int theObject, int[] handles, byte[][] values, byte[] tag )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] updateAttributeValues(RO): objectId=" + theObject +
			              ", attributes=" + arrayToStringWithSizes(handles, values) );
		}
		
		try
		{
			this.rtiamb.updateAttributeValues( theObject,
			                                   convertAttributes(handles,values),
			                                   convertTag(tag) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public int updateAttributeValues( int oHandle,
	                                  int[] handles,
	                                  byte[][] values,
	                                  byte[] tag,
	                                  double time )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] updateAttributeValues(TSO): objectId=" + oHandle +
			              ", attributes=" + arrayToStringWithSizes(handles, values) +
			              ", time=" + time );
		}
		
		try
		{
    		LogicalTime jTime = new DoubleTime( time );
    		this.rtiamb.updateAttributeValues( oHandle,
    		                                   convertAttributes(handles,values),
    		                                   convertTag(tag),
    		                                   jTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}

		return -1;
	}

	public void sendInteraction( int theInteraction, int[] handles, byte[][] values, byte[] tag )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] sendInteraction(RO): class=" + theInteraction +
			              ", parameters=" + arrayToStringWithSizes(handles, values) );
		}
		
		try
		{
			this.rtiamb.sendInteraction( theInteraction,
			                             convertParameters(handles,values),
			                             convertTag(tag) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public int sendInteraction( int iHan, int[] handles, byte[][] values, byte[] tag, double time )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] sendInteraction(TSO): class=" + iHan +
			              ", parameters=" + arrayToStringWithSizes(handles, values) + 
			              ", time=" + time );
		}
		
		try
		{
    		LogicalTime jTime = new DoubleTime( time );
    
    		this.rtiamb.sendInteraction( iHan,
    		                             convertParameters(handles,values),
    		                             convertTag(tag),
    		                             jTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
		
		return -1;
	}

	public void deleteObjectInstance( int objectHandle, byte[] tag )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] deleteObjectInstance(RO): objectId=" + objectHandle );
		
		try
		{
			this.rtiamb.deleteObjectInstance( objectHandle, convertTag(tag) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public int deleteObjectInstance( int objectHandle, byte[] tag, double theTime )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] deleteObjectInstance(TSO): objectId=" + objectHandle +
			              ", time=" + theTime );
		}
		
		try
		{
			LogicalTime federateTime = new DoubleTime( theTime );
			this.rtiamb.deleteObjectInstance( objectHandle, convertTag(tag), federateTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}

		return -1;
	}

	public void localDeleteObjectInstance( int objectHandle )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] localDeleteObjectInstance(): objectId=" + objectHandle );
		
		try
		{
			this.rtiamb.localDeleteObjectInstance( objectHandle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void changeAttributeTransportationType( int theObject, int[] theAttributes, int theType )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] changeAttributeTransportationType(): objectId=" + theObject +
			              ", type=" + theType +
			              ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
			rtiamb.changeAttributeTransportationType( theObject,
			                                          convertHandles(theAttributes),
			                                          theType );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	public void changeInteractionTransportationType( int theClass, int theType )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] changeInteractionTransportationType(): class=" + theClass +
			              ", type=" + theType );
		}
		
		try
		{
			this.rtiamb.changeInteractionTransportationType( theClass, theType );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void requestObjectAttributeValueUpdate( int theObject, int[] attributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] requestObjectAttributeValueUpdate(): objectId=" + theObject +
			              ", attributes=" + arrayToString(attributes) );
		}
		
		try
		{
    		this.rtiamb.requestObjectAttributeValueUpdate( theObject, convertHandles(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void requestClassAttributeValueUpdate( int theClass, int[] theAttributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] requestClassAttributeValueUpdate(): class=" + theClass +
			              ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
    		this.rtiamb.requestClassAttributeValueUpdate( theClass, convertHandles(theAttributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Ownership Management Services ///////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////
	public void unconditionalAttributeOwnershipDivestiture( int theObject, int[] attributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] unconditionalAttributeOwnershipDivestiture(): objectId=" +
			              theObject + ", attributes=" + arrayToString(attributes) );
		}
		
		try
		{
    		this.rtiamb.unconditionalAttributeOwnershipDivestiture( theObject,
    		                                                        convertHandles(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	public void negotiatedAttributeOwnershipDivestiture( int theObject,
	                                                     int[] theAttributes,
	                                                     byte[] theTag )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] negotiatedAttributeOwnershipDivestiture(): objectId=" +
			              theObject + ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
    		this.rtiamb.negotiatedAttributeOwnershipDivestiture( theObject,
    		                                                     convertHandles(theAttributes),
    		                                                     convertTag(theTag) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	public void attributeOwnershipAcquisition( int theObject,
	                                           int[] theAttributes,
	                                           byte[] theTag )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] attributeOwnershipAcquisition(): objectId=" + theObject +
			              ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
    		this.rtiamb.attributeOwnershipAcquisition( theObject,
    		                                           convertHandles(theAttributes),
    		                                           convertTag(theTag) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void attributeOwnershipAcquisitionIfAvailable( int theObject, int[] theAttributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] attributeOwnershipAcquisitionIfAvailable(): objectId=" +
			              theObject + ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
    		this.rtiamb.attributeOwnershipAcquisitionIfAvailable( theObject,
    		                                                      convertHandles(theAttributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public int[] attributeOwnershipReleaseResponse( int oHandle, int[] theAttributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] attributeOwnershipReleaseResponse(): objectId=" + oHandle +
			              ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
    		HLA13AttributeHandleSet retval =
    			(HLA13AttributeHandleSet)this.rtiamb.attributeOwnershipReleaseResponse(
    			                             oHandle,
    			                             convertHandles(theAttributes) );
    		
    		int[] retarray = new int[retval.size()];
    		for( int i = 0; i < retval.size(); i++ )
    		{
    			retarray[i] = retval.get(i);
    		}
    		
    		return retarray;
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return new int[]{};
		}
	}
	
	public void cancelNegotiatedAttributeOwnershipDivestiture( int theObject, int[] theAttributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] cancelNegotiatedAttributeOwnershipDivestiture(): objectId=" +
			              theObject + ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
    		this.rtiamb.cancelNegotiatedAttributeOwnershipDivestiture( theObject,
    		                                                           convertHandles(theAttributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	public void cancelAttributeOwnershipAcquisition( int theObject, int[] theAttributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] cancelAttributeOwnershipAcquisition(): objectId=" + theObject +
			              ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
    		this.rtiamb.cancelAttributeOwnershipAcquisition( theObject,
    		                                                 convertHandles(theAttributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	public void queryAttributeOwnership( int theObject, int theAttribute )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] queryAttributeOwnership(): objectId=" + theObject +
			              ", attribute=" + theAttribute );
		}
		
		try
		{
			this.rtiamb.queryAttributeOwnership( theObject, theAttribute );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public boolean isAttributeOwnedByFederate( int theObject, int theAttribute )
    {
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] isAttributeOwnedByFederate(): objectId=" + theObject +
			              ", attribute=" + theAttribute );
		}
		
		try
		{
			return this.rtiamb.isAttributeOwnedByFederate( theObject, theAttribute );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return false;
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Time Management Services /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public void enableTimeRegulation( double theFederateTime, double theLookahead )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] enableTimeRegulation(): federateTime=" + theFederateTime +
			              ", lookahead=" + theLookahead );
		}
		
		try
		{
    		LogicalTime javaFederateTime = new DoubleTime( theFederateTime );
    		LogicalTimeInterval javaLookahead = new DoubleTimeInterval( theLookahead );
    
    		this.rtiamb.enableTimeRegulation( javaFederateTime, javaLookahead );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void disableTimeRegulation()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] disableTimeRegulation()" );

		try
		{
			this.rtiamb.disableTimeRegulation();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void enableTimeConstrained()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] enableTimeConstrained()" );

		try
		{
			this.rtiamb.enableTimeConstrained();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void disableTimeConstrained()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] disableTimeConstrained()" );

		try
		{
			this.rtiamb.disableTimeConstrained();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void timeAdvanceRequest( double theTime )
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] timeAdvanceRequest(): time=" + theTime );

		try
		{
    		LogicalTime logicalTime = new DoubleTime( theTime );
    		this.rtiamb.timeAdvanceRequest( logicalTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void timeAdvanceRequestAvailable( double theTime ) 
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] timeAdvanceRequestAvailable(): time=" + theTime );

		try
		{
    		LogicalTime logicalTime = new DoubleTime( theTime );
    		this.rtiamb.timeAdvanceRequestAvailable( logicalTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void nextEventRequest( double theTime )
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] nextEventRequest(): time=" + theTime );

		try
		{
    		LogicalTime federateTime = new DoubleTime( theTime );
    		this.rtiamb.nextEventRequest( federateTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void nextEventRequestAvailable( double theTime )
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] nextEventRequestAvailable(): time=" + theTime );

		try
		{
    		LogicalTime federateTime = new DoubleTime( theTime );
    		this.rtiamb.nextEventRequestAvailable( federateTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void flushQueueRequest( double theTime )
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] flushQueueRequest(): time=" + theTime );

		try
		{
			LogicalTime federateTime = new DoubleTime( theTime );
			this.rtiamb.flushQueueRequest( federateTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void enableAsynchronousDelivery()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] enableAsynchronousDelivery()" );

		try
		{
			this.rtiamb.enableAsynchronousDelivery();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void disableAsynchronousDelivery()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] disableAsynchronousDelivery()" );

		try
		{
			this.rtiamb.disableAsynchronousDelivery();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public double queryLBTS()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] queryLBTS()" );

		try
		{
			return ((DoubleTime)this.rtiamb.queryLBTS()).getTime();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1.0;
		}
	}

	public double queryFederateTime()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] queryFederateTime()" );

		try
		{
			return ((DoubleTime)this.rtiamb.queryFederateTime()).getTime();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1.0;
		}
	}

	public double queryMinNextEventTime()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] queryMinNextEventTime()" );

		try
		{
			return ((DoubleTime)this.rtiamb.queryMinNextEventTime()).getTime();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1.0;
		}
	}

	public void modifyLookahead( double theLookahead )
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] modifyLookahead(): lookahead=" + theLookahead );

		try
		{
			LogicalTimeInterval lookahead = new DoubleTimeInterval( theLookahead );
			this.rtiamb.modifyLookahead( lookahead );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public double queryLookahead()
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] queryLookahead()" );

		try
		{
			return ((DoubleTimeInterval)this.rtiamb.queryLookahead()).getInterval();
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
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] retract(): handle=" + theHandle );

		try
		{
			EventRetractionHandle erh = null; // new HLA13EventRetractionHandle( theHandle ); 
			rtiamb.retract( erh );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void changeAttributeOrderType( int theObject, int[] theAttributes, int theType )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] changeAttributeOrderType(): objectId=" + theObject +
			              ", attributes=" + arrayToString(theAttributes) +
			              ", newType=" + theType );
		}

		try
		{
			rtiamb.changeAttributeOrderType( theObject, convertHandles(theAttributes), theType );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 8.24 
	public void changeInteractionOrderType( int theClass, int theType )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] changeInteractionOrderType(): interaction=" + theClass +
			              ", newType=" + theType );
		}

		try
		{
			rtiamb.changeInteractionOrderType( theClass, theType );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Data Distribution Management Services //////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	// 9.2 
	public Region createRegion( int space, int extents )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] createRegion(): space=" + space + ", extents=" + extents );
		}
		
		try
		{
			return rtiamb.createRegion( space, extents );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return null;
		}
	}

	// 9.3 
	public void notifyOfRegionModification( Region region )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] notifyOfRegionModification(): region=" + region );
		}
		
		try
		{
			rtiamb.notifyOfRegionModification( region );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.4 
	public void deleteRegion( Region region )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] deleteRegion(): region=" + region );
		}
		
		try
		{
			rtiamb.deleteRegion( region );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.5 
	public int registerObjectInstanceWithRegion( int theClass, int[] attributes, Region[] regions )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] registerObjectInstanceWithRegion(): class=" + theClass +
			              ", attributes=" + arrayToString(attributes) + ", regions=" +
			              regionsToString(regions) );
		}
		
		try
		{
			return rtiamb.registerObjectInstanceWithRegion( theClass, attributes, regions );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 9.5 
	public int registerObjectInstanceWithRegion( int theClass,
	                                             String theObject,
	                                             int[] attributes,
	                                             Region[] regions )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] registerObjectInstanceWithRegion(): class=" + theClass +
			              ", name=" + theObject + ", attributes=" + arrayToString(attributes) +
			              ", regions=" + regionsToString(regions) );
		}
		
		try
		{
			return rtiamb.registerObjectInstanceWithRegion( theClass, theObject, attributes, regions );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	// 9.6 
	public void associateRegionForUpdates( Region region, int theObject, int[] theAttributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] associateRegionForUpdates(): object=" + theObject +
			              ", region=" + region + ", attributes=" + arrayToString(theAttributes) );
		}
		
		try
		{
			rtiamb.associateRegionForUpdates( region, theObject, convertHandles(theAttributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.7 
	public void unassociateRegionForUpdates( Region theRegion, int theObject )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] unassociateRegionForUpdates(): object=" + theObject +
			              ",region=" + theRegion );
		}
		
		try
		{
			rtiamb.unassociateRegionForUpdates( theRegion, theObject );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	// 9.8 
	public void subscribeObjectClassAttributesWithRegion( int theClass, Region region, int[] atts )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] subscribeObjectClassAttributesWithRegion(): class=" +
			              theClass + ", region=" + region + ", attributes=" + arrayToString(atts) );
		}
		
		try
		{
			rtiamb.subscribeObjectClassAttributesWithRegion( theClass,
			                                                 region,
			                                                 convertHandles(atts) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.8 
	public void subscribeObjectClassAttributesPassivelyWithRegion( int theClass, 
	                                                               Region region,
	                                                               int[] attributes )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] subscribeObjectClassAttributesPassivelyWithRegion(): class=" +
			              theClass + ", region=" + region + ", attributes=" +
			              arrayToString(attributes) );
		}
		
		try
		{
			rtiamb.subscribeObjectClassAttributesPassivelyWithRegion( theClass,
			                                                          region,
			                                                          convertHandles(attributes) );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	// 9.9 
	public void unsubscribeObjectClassWithRegion( int theClass, Region theRegion )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] unsubscribeObjectClassWithRegion(): class=" + theClass +
			              ", region=" + theRegion );
		}
		
		try
		{
			rtiamb.unsubscribeObjectClassWithRegion( theClass, theRegion );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}
	
	// 9.10 
	public void subscribeInteractionClassWithRegion( int theClass, Region theRegion )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] subscribeInteractionClassWithRegion(): class=" + theClass +
			              ", region=" + theRegion );
		}
		
		try
		{
			rtiamb.subscribeInteractionClassWithRegion( theClass, theRegion );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.10 
	public void subscribeInteractionClassPassivelyWithRegion( int theClass, Region theRegion )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] subscribeInteractionClassPassivelyWithRegion(): class=" +
			              theClass + ", region=" + theRegion );
		}
		
		try
		{
			rtiamb.subscribeInteractionClassPassivelyWithRegion( theClass, theRegion );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.11 
	public void unsubscribeInteractionClassWithRegion( int theClass, Region theRegion )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] unsubscribeInteractionClassWithRegion(): class=" + theClass +
			              ", region=" + theRegion );
		}
		
		try
		{
			rtiamb.unsubscribeInteractionClassWithRegion( theClass, theRegion );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.12 
	public void sendInteractionWithRegion( int theInteraction,
	                                       int[] handles,
	                                       byte[][] values,
	                                       byte[] tag,
	                                       Region theRegion )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] sendInteractionWithRegion(RO): class=" + theInteraction +
			              ", parameters=" + arrayToStringWithSizes(handles, values) +
			              ", region=" + theRegion );
		}
		
		try
		{
			this.rtiamb.sendInteractionWithRegion( theInteraction,
			                                       convertParameters(handles,values),
			                                       convertTag(tag),
			                                       theRegion );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	// 9.12 
	public int sendInteractionWithRegion( int theInteraction,
	                                      int[] handles,
	                                      byte[][] values,
	                                      byte[] tag,
	                                      Region theRegion,
	                                      double time )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] sendInteractionWithRegion(TSO): class=" + theInteraction +
			              ", parameters=" + arrayToStringWithSizes(handles, values) + 
			              ", time=" + time );
		}
		
		try
		{
    		LogicalTime jTime = new DoubleTime( time );
    		this.rtiamb.sendInteractionWithRegion( theInteraction,
    		                                       convertParameters(handles,values),
    		                                       convertTag(tag),
    		                                       theRegion,
    		                                       jTime );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
		
		return -1;
	}

	// 9.13 
	public void requestClassAttributeValueUpdateWithRegion( int theClass,
	                                                        int[] attributes,
	                                                        Region region )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] requestClassAttributeValueUpdateWithRegion(): class=" +
			              theClass + ", region=" + region + ", attributes=" +
			              arrayToString(attributes) );
		}
		
		try
		{
			rtiamb.requestClassAttributeValueUpdateWithRegion( theClass,
			                                                   convertHandles(attributes),
			                                                   region );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Support Services /////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public int getObjectClassHandle( String theName )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getObjectClassHandle(): name=" + theName );
		
		try
		{
			return this.rtiamb.getObjectClassHandle( theName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public String getObjectClassName( int theHandle )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getObjectClassName(): handle=" + theHandle );
		
		try
		{
			return this.rtiamb.getObjectClassName( theHandle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public int getAttributeHandle( String theName, int whichClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] getAttributeHandle(): class=" + whichClass +
			              ", attribute=" + theName );
		}
		
		try
		{
			return this.rtiamb.getAttributeHandle( theName, whichClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return 1;
		}
	}

	public String getAttributeName( int theHandle, int whichClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] getAttributeName(): class=" + whichClass +
			              ", attributeHandle=" + theHandle );
		}
		
		try
		{
			return this.rtiamb.getAttributeName( theHandle, whichClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public int getInteractionClassHandle( String theName )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getInteractionClassHandle(): name=" + theName );

		try
		{
			return this.rtiamb.getInteractionClassHandle( theName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public String getInteractionClassName( int theHandle )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getInteractionClassName(): handle=" + theHandle );

		try
		{
			return this.rtiamb.getInteractionClassName( theHandle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public int getParameterHandle( String theName, int whichClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] getParameterHandle(): class=" + whichClass +
			              ", parameter=" + theName );
		}
		
		try
		{
			return this.rtiamb.getParameterHandle( theName, whichClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public String getParameterName( int theHandle, int whichClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] getParameterName(): class=" + whichClass +
			              ", parameterHandle=" + theHandle );
		}
		
		try
		{
			return this.rtiamb.getParameterName( theHandle, whichClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public int getObjectInstanceHandle( String theName )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getObjectInstanceHandle(): objectName=" + theName );
		
		try
		{
			return this.rtiamb.getObjectInstanceHandle( theName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public String getObjectInstanceName( int theHandle )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getObjectInstanceName(): objectHandle=" + theHandle );
		
		try
		{
			return this.rtiamb.getObjectInstanceName( theHandle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public int getRoutingSpaceHandle( String theName )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getRoutingSpaceHandle(): name=" + theName );
		
		try
		{
			return this.rtiamb.getRoutingSpaceHandle( theName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public String getRoutingSpaceName( int theHandle )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getRoutingSpaceName(): handle=" + theHandle );
		
		try
		{
			return this.rtiamb.getRoutingSpaceName( theHandle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public int getDimensionHandle( String theName, int whichSpace )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] getDimensionHandle(): name=" + theName +
			              ", spaceHandle=" + whichSpace );
		}
		
		try
		{
			return this.rtiamb.getDimensionHandle( theName, whichSpace );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public String getDimensionName( int theHandle, int whichClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] getDimensionName(): handle=" + theHandle +
			              ", spaceHandle=" + whichClass );
		}
		
		try
		{
			return this.rtiamb.getDimensionName( theHandle, whichClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public int getAttributeRoutingSpaceHandle( int theHandle, int whichClass )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] getAttributeRoutingSpaceHandle(): attributeHandle=" +theHandle+
			              ", classHandle=" + whichClass );
		}
		
		try
		{
			return this.rtiamb.getAttributeRoutingSpaceHandle( theHandle, whichClass );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public int getObjectClass( int theObject )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getObjectClass(): objectId=" + theObject );
		try
		{
			return this.rtiamb.getObjectClass( theObject );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public int getInteractionRoutingSpaceHandle( int theHandle )
	{
		// log the request
		if( logger.isTraceEnabled() )
		{
			logger.trace( "[Request] getInteractionRoutingSpaceHandle(): interactionHandle=" +
			              theHandle );
		}
		
		try
		{
			return this.rtiamb.getInteractionRoutingSpaceHandle( theHandle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public int getTransportationHandle( String theName )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getTransportationHandle(): name=" + theName );
		
		try
		{
			return this.rtiamb.getTransportationHandle( theName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public String getTransportationName( int theHandle )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getTransportationName(): handle=" + theHandle );

		try
		{
			return this.rtiamb.getTransportationName( theHandle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public int getOrderingHandle( String theName )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getOrderingHandle(): name=" + theName );

		try
		{
			return this.rtiamb.getOrderingHandle( theName );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}

	public String getOrderingName( int theHandle )
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getOrderingName(): handle=" + theHandle );

		try
		{
			return this.rtiamb.getOrderingName( theHandle );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return "exception";
		}
	}

	public void enableClassRelevanceAdvisorySwitch()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] enableClassRelevanceAdvisorySwitch()" );
		
		try
		{
			this.rtiamb.enableClassRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void disableClassRelevanceAdvisorySwitch()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] disableClassRelevanceAdvisorySwitch()" );
		
		try
		{
			this.rtiamb.disableClassRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void enableAttributeRelevanceAdvisorySwitch()
	{
		// log the request
		if( logger.isDebugEnabled() )
			logger.debug( "[Request] enableAttributeRelevanceAdvisorySwitch()" );
		
		try
		{
			this.rtiamb.enableAttributeRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void disableAttributeRelevanceAdvisorySwitch()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] disableAttributeRelevanceAdvisorySwitch()" );
		
		try
		{
			this.rtiamb.disableAttributeRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void enableAttributeScopeAdvisorySwitch()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] enableAttributeScopeAdvisorySwitch()" );
		
		try
		{
			this.rtiamb.enableAttributeScopeAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void disableAttributeScopeAdvisorySwitch()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] disableAttributeScopeAdvisorySwitch()" );
		
		try
		{
			this.rtiamb.disableAttributeScopeAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void enableInteractionRelevanceAdvisorySwitch()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] enableInteractionRelevanceAdvisorySwitch()" );
		
		try
		{
			this.rtiamb.enableInteractionRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public void disableInteractionRelevanceAdvisorySwitch()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] disableInteractionRelevanceAdvisorySwitch()" );
		
		try
		{
			this.rtiamb.disableInteractionRelevanceAdvisorySwitch();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public Region getRegion( int regionToken )
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getRegion(): token=" + regionToken );
		
		try
		{
			return this.rtiamb.getRegion( regionToken );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return null;
		}
	}
	
	public int getRegionToken( Region region )
	{
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] getRegionToken(): region=" + region );
		
		try
		{
			return this.rtiamb.getRegionToken( region );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return -1;
		}
	}
	
	public void tick()
	{
		// log the request
		//if( logger.isTraceEnabled() )
		//	logger.trace( "[Request] tick()" );
		
		try
		{
			this.rtiamb.tick();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}
	}

	public boolean tick( double min, double max )
	{
		// log the request
		//if( logger.isTraceEnabled() )
		//	logger.trace( "[Request] tick(): min=" + min + ", max=" + max );
		
		try
		{
			return rtiamb.tick( min, max );
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
			return false;
		}
	}
	
	public void kill()
	{
		// log the request
		if( logger.isTraceEnabled() )
			logger.trace( "[Request] kill() (portico-specific)" );

		try
		{
			rtiamb.getHelper().getLrc().stopLrc();
		}
		catch( Exception e )
		{
			ExceptionManager.pushException( this.id, e );
		}

		this.rtiamb = null;
		this.fedamb = null;
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
