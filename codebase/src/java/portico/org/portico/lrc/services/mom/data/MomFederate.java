/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.mom.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.encoding.HLA1516eBoolean;
import org.portico.impl.hla1516e.types.encoding.HLA1516eUnicodeString;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.management.Federate;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.object.msg.UpdateAttributes;

/**
 * Contains links between the {@link OCInstance} used to represent the federate in the federation
 * and the {@link Federate} management object that holds all the information. This class also
 * serializes the information for updates when they are requested.
 */
public class MomFederate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	//
	// Names and types from 1516e MIM
	//
	// HLAfederateHandle,                  // HLAhandle
	// HLAfederateName,                    // 1516e, HLAunicodeString
	// HLAfederateType,                    // HLAunicodeString
	// HLAfederateHost,                    // HLAunicodeString
	// HLARTIversion,                      // HLAunicodeString
	// HLAFOMmoduleDesignatorList,         // 1516e, HLAmoduleDesignatorList
	// FedID, // Not in 1516e?
	// HLAtimeConstrained,                 // HLAboolean
	// HLAtimeRegulating,                  // HLAboolean
	// HLAasynchronousDelivery,            // HLAboolean
	// HLAfederateState,                   // HLAfederateState
	// HLAtimeManagerState,                // HLAtimeState
	// HLAlogicalTime,                     // HLAlogicalTime
	// HLAlookahead,                       // HLAtimeInterval
	// LBTS, // synonym for LITS
	// HLAGALT,                            // HLAlogicalTime
	// HLALITS,                            // HLAlogicalTime; NextMinEventTime in 1.3,
	// HLAROlength,                        // HLAcount
	// HLATSOlength,                       // HLAcount
	// HLAreflectionsReceived,             // HLAcount
	// HLAupdatesSent,                     // HLAcount
	// HLAinteractionsReceived,            // HLAcount
	// HLAinteractionsSent,                // HLAcount
	// HLAobjectInstancesThatCanBeDeleted, // HLAcount; ObjectsOwned in 1.3
	// HLAobjectInstancesUpdated,          // HLAcount; ObjectsUpdated in 1.3
	// HLAobjectInstancesReflected,        // HLAcount; ObjectsReflected in 1.3
	// HLAobjectInstancesDeleted,          // HLAcount
	// HLAobjectInstancesRemoved,          // HLAcount
	// HLAobjectInstancesRegistered,       // HLAcount
	// HLAobjectInstancesDiscovered,       // HLAcount
	// HLAtimeGrantedTime,                 // HLAmsec; Wallclock time reference - see OMT
	// HLAtimeAdvancingTime;               // HLAmsec; Wallclock time reference - see OMT
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected OCInstance federateObject;
	protected Federate federate;
	protected Logger momLogger;
	private Map<Integer,IMomVariable> handleMap;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected MomFederate( HLAVersion hlaVersion,
	                       Federate federate,
	                       OCInstance federateObject,
	                       Logger momLogger )
	{
		this.federate = federate;
		this.federateObject = federateObject;
		this.momLogger = momLogger;
		
		// populate the handles map from the object model
		if( hlaVersion == HLAVersion.HLA13 )
			this.handleMap = loadHla13HandlesFrom( federateObject.getRegisteredType() );
		else
			this.handleMap = loadHla1516HandlesFrom( federateObject.getRegisteredType() );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private byte[] getFederateHandle( HLAVersion version )
	{
		return encodeHandle( version, federate.getFederateHandle() );
	}

	private byte[] getFederateType( HLAVersion version )
	{
		return encodeString( version, federate.getFederateName() ); // wrong in 1516e
	}
	
	private byte[] getFederateName( HLAVersion version )
	{
		return encodeString( version, federate.getFederateName() );
	}

	private byte[] getFederateHost( HLAVersion version )
	{
		return notYetSupported( version, "FederateHost" );
	}
	
	private byte[] getFomModuleDesignatorList( HLAVersion version )
	{
		return notYetSupported( version, "FomModuleDesignatorList" );
	}

	private byte[] getRTIversion( HLAVersion version )
	{
		return encodeString( version, PorticoConstants.RTI_NAME+" v"+PorticoConstants.RTI_VERSION );
	}

	private byte[] getFEDid( HLAVersion version )
	{
		return notYetSupported( version, "FDDID" );
	}

	private byte[] getTimeConstrained( HLAVersion version )
	{
		return encodeBoolean( version, federate.getTimeStatus().isConstrained() );
	}

	private byte[] getTimeRegulating( HLAVersion version )
	{
		return encodeBoolean( version, federate.getTimeStatus().isRegulating() );
	}

	private byte[] getAsynchronousDelivery( HLAVersion version )
	{
		return encodeBoolean( version, federate.getTimeStatus().isAsynchronous() );
	}

	private byte[] getFederateState( HLAVersion version )
	{
		return notYetSupported( version, "FederateState" );
	}

	private byte[] getTimeManagerState( HLAVersion version )
	{
		return notYetSupported( version, "TimeManagerState" );
	}

	private byte[] getFederateTime( HLAVersion version )
	{
		return encodeTime( version, federate.getTimeStatus().getCurrentTime() );
	}

	private byte[] getLookahead( HLAVersion version )
	{
		return encodeTime( version, federate.getTimeStatus().getLookahead() );
	}

	private byte[] getLBTS( HLAVersion version )
	{
		return encodeTime( version, federate.getTimeStatus().getLbts() );
	}

	private byte[] getMinNextEventTime( HLAVersion version )
	{
		return getLBTS( version );
	}
	
	private byte[] getGALT( HLAVersion version )
	{
		return notYetSupported(version,"GALT");
	}

	private byte[] getLITS( HLAVersion version )
	{
		return notYetSupported(version,"LITS");
	}

	private byte[] getROlength( HLAVersion version )
	{
		return notYetSupported(version,"ROlength");
	}

	private byte[] getTSOlength( HLAVersion version )
	{
		return notYetSupported(version,"TSOlength");
	}

	private byte[] getReflectionsReceived( HLAVersion version )
	{
		return notYetSupported(version,"ReflectionsReceived");
	}

	private byte[] getUpdatesSent( HLAVersion version )
	{
		return notYetSupported(version,"UpdatesSent");
	}

	private byte[] getInteractionsReceived( HLAVersion version )
	{
		return notYetSupported(version,"InteractionsReceived");
	}

	private byte[] getInteractionsSent( HLAVersion version )
	{
		return notYetSupported(version,"InteractionsSent");
	}

	private byte[] getObjectsOwned( HLAVersion version )
	{
		return notYetSupported(version,"ObjectOwned");
	}

	private byte[] getObjectsUpdated( HLAVersion version )
	{
		return notYetSupported(version,"ObjectsUpdated");
	}

	private byte[] getObjectsReflected( HLAVersion version )
	{
		return notYetSupported(version,"ObjectsReflected");
	}

	private byte[] getObjectInstancesDeleted( HLAVersion version )
	{
		return notYetSupported( version, "ObjectInstancedDeleted" );
	}

	private byte[] getObjectInstancesRemoved( HLAVersion version )
	{
		return notYetSupported( version, "ObjectInstancedRemoved" );
	}

	private byte[] getObjectInstancesRegistered( HLAVersion version )
	{
		return notYetSupported( version, "ObjectInstancedRegistered" );
	}

	private byte[] getObjectInstancesDiscovered( HLAVersion version )
	{
		return notYetSupported( version, "ObjectInstancedDiscovered" );
	}

	private byte[] getTimeGrantedTime( HLAVersion version )
	{
		return encodeTime( version, federate.getTimeStatus().getCurrentTime() );
	}

	private byte[] getTimeAdvancingTime( HLAVersion version )
	{
		return encodeTime( version, federate.getTimeStatus().getRequestedTime() );
	}

	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Update Generating Methods ///////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	public UpdateAttributes generateUpdate( HLAVersion version, Set<Integer> handles )
		throws JAttributeNotDefined
	{
		HashMap<Integer,byte[]> attributes = new HashMap<Integer,byte[]>();

		// loop through the attributes and get the appropriate values
		for( Integer attributeHandle : handles )
		{
			IMomVariable variable = this.handleMap.get( attributeHandle );
			if( variable != null )
			{
				byte[] value = variable.getValue( version );
				if( value != null )
					attributes.put( attributeHandle, value );
			}
		}
		
		UpdateAttributes update = new UpdateAttributes( federateObject.getHandle(),
		                                                new byte[0],
		                                                attributes );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///  Handle Mapping Methods   ////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private HashMap<Integer,IMomVariable> loadHla1516HandlesFrom( OCMetadata type )
	{
		//
		// CURRENTLY ONLY SUPPORTS 1516e
		//
		HashMap<Integer,IMomVariable> map = new HashMap<>();
		map.put( type.getAttributeHandle("HLAfederateHandle"), this::getFederateHandle );
		map.put( type.getAttributeHandle("HLAfederateName"), this::getFederateName );
		map.put( type.getAttributeHandle("HLAfederateType"), this::getFederateType );
		map.put( type.getAttributeHandle("HLAfederateHost"), this::getFederateHost );
		map.put( type.getAttributeHandle("HLARTIversion"), this::getRTIversion );
		map.put( type.getAttributeHandle("HLAFOMmoduleDesignatorList"), this::getFomModuleDesignatorList );
		map.put( type.getAttributeHandle("HLAtimeConstrained"), this::getTimeConstrained );
		map.put( type.getAttributeHandle("HLAtimeRegulating"), this::getTimeRegulating );
		map.put( type.getAttributeHandle("HLAasynchronousDelivery"), this::getAsynchronousDelivery );
		map.put( type.getAttributeHandle("HLAfederateState"), this::getFederateState );
		map.put( type.getAttributeHandle("HLAtimeManagerState"), this::getTimeManagerState );
		map.put( type.getAttributeHandle("HLAlogicalTime"), this::getFederateTime );
		map.put( type.getAttributeHandle("HLAlookahead"), this::getLookahead );
		//map.put( type.getAttributeHandle(""), this::getLBTS ); -- this is LITS
		map.put( type.getAttributeHandle("HLAGALT"), this::getGALT );
		map.put( type.getAttributeHandle("HLALITS"), this::getLITS );
		map.put( type.getAttributeHandle("HLAROlength"), this::getROlength );
		map.put( type.getAttributeHandle("HLATSOlength"), this::getTSOlength );
		map.put( type.getAttributeHandle("HLAreflectionsReceived"), this::getReflectionsReceived );
		map.put( type.getAttributeHandle("HLAupdatesSent"), this::getUpdatesSent );
		map.put( type.getAttributeHandle("HLAinteractionsReceived"), this::getInteractionsReceived );
		map.put( type.getAttributeHandle("HLAinteractionsSent"), this::getInteractionsSent );
		//map.put( type.getAttributeHandle("HLAobjectInstancesThatCanBeDeleted"), NOT SUPPORTED );
		map.put( type.getAttributeHandle("HLAobjectInstancesUpdated"), this::getObjectsUpdated );
		map.put( type.getAttributeHandle("HLAobjectInstancesReflected"), this::getObjectsReflected );
		map.put( type.getAttributeHandle("HLAobjectInstancesDeleted"), this::getObjectInstancesDeleted );
		map.put( type.getAttributeHandle("HLAobjectInstancesRemoved"), this::getObjectInstancesRemoved );
		map.put( type.getAttributeHandle("HLAobjectInstancesRegistered"), this::getObjectInstancesRegistered );
		map.put( type.getAttributeHandle("HLAobjectInstancesDiscovered"), this::getObjectInstancesDiscovered );
		map.put( type.getAttributeHandle("HLAtimeGrantedTime"), this::getTimeGrantedTime );
		map.put( type.getAttributeHandle("HLAtimeAdvancingTime"), this::getTimeAdvancingTime );
		
		// remove any that were invalid
		map.remove( ObjectModel.INVALID_HANDLE );
		return map;
	}
	
	private HashMap<Integer,IMomVariable> loadHla13HandlesFrom( OCMetadata type )
	{
		HashMap<Integer,IMomVariable> map = new HashMap<>();
		map.put( type.getAttributeHandle("FederateHandle"), this::getFederateHandle );
		map.put( type.getAttributeHandle("FederateType"), this::getFederateType );
		map.put( type.getAttributeHandle("FederateHost"), this::getFederateHost );
		map.put( type.getAttributeHandle("RTIversion"), this::getRTIversion );
		map.put( type.getAttributeHandle("TimeConstrained"), this::getTimeConstrained );
		map.put( type.getAttributeHandle("TimeRegulating"), this::getTimeRegulating );
		map.put( type.getAttributeHandle("AsynchronousDelivery"), this::getAsynchronousDelivery );
		map.put( type.getAttributeHandle("FederateState"), this::getFederateState );
		map.put( type.getAttributeHandle("TimeManagerState"), this::getTimeManagerState );
		map.put( type.getAttributeHandle("FederateTime"), this::getFederateTime );
		map.put( type.getAttributeHandle("Lookahead"), this::getLookahead );
		map.put( type.getAttributeHandle("LBTS"), this::getLBTS );
		map.put( type.getAttributeHandle("MinNextEventTime"), this::getMinNextEventTime );
		map.put( type.getAttributeHandle("ROlength"), this::getROlength );
		map.put( type.getAttributeHandle("TSOlength"), this::getTSOlength );
		map.put( type.getAttributeHandle("ReflectionsReceived"), this::getReflectionsReceived );
		map.put( type.getAttributeHandle("UpdatesSent"), this::getUpdatesSent );
		map.put( type.getAttributeHandle("InteractionsReceived"), this::getInteractionsReceived );
		map.put( type.getAttributeHandle("InteractionsSent"), this::getInteractionsSent );
		
		map.put( type.getAttributeHandle("ObjectsOwned"), this::getObjectsOwned );
		map.put( type.getAttributeHandle("ObjectsUpdated"), this::getObjectsUpdated );
		map.put( type.getAttributeHandle("ObjectsReflected"), this::getObjectsReflected );
		
		// remove any that were invalid
		map.remove( ObjectModel.INVALID_HANDLE );
		return map;
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	///  Encoding Helper Methods   ///////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private byte[] notYetSupported( HLAVersion version, String property )
	{
		momLogger.trace( "Requeted MOM property that isn't supported yet: Federate." + property );
		//return encodeString( version, "property ["+property+"] not yet supported" );
		return null; // this is a signal that it isn't supported
	}

	private byte[] encodeString( HLAVersion version, String string )
	{
		switch( version )
		{
			case HLA13:
				return JEncodingHelpers.encodeString( string );
			case IEEE1516e:
				return new HLA1516eUnicodeString(string).toByteArray();
			case IEEE1516:
				return new HLA1516eUnicodeString(string).toByteArray();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+version );
		}
	}
	
	private byte[] encodeHandle( HLAVersion version, int handle )
	{
		switch( version )
		{
			case HLA13:
				return JEncodingHelpers.encodeString( ""+handle );
			case IEEE1516e:
				return new HLA1516eHandle(handle).getBytes();
			case IEEE1516:
				return new HLA1516eHandle(handle).getBytes();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+version );
		}
	}

	private byte[] encodeBoolean( HLAVersion version, boolean value )
	{
		switch( version )
		{
			case HLA13:
				return JEncodingHelpers.encodeString( ""+value );
			case IEEE1516e:
				return new HLA1516eBoolean(value).toByteArray();
			case IEEE1516:
				return new HLA1516eBoolean(value).toByteArray();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+version );
		}
	}
	
	private byte[] encodeTime( HLAVersion version, double time )
	{
		switch( version )
		{
			case HLA13:
				return JEncodingHelpers.encodeString( ""+time );
			case IEEE1516e:
				return new org.portico.impl.hla1516e.types.time.DoubleTime(time).toByteArray();
			case IEEE1516:
				return new org.portico.impl.hla1516.types.DoubleTime(time).toByteArray();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+version );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
