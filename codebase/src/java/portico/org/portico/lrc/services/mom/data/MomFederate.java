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
import java.util.function.Function;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.encoding.HLA1516eBoolean;
import org.portico.impl.hla1516e.types.encoding.HLA1516eUnicodeString;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.management.Federate;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
import org.portico2.common.services.object.msg.UpdateAttributes;

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

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected OCInstance federateObject;
	protected Federate federate;
	protected Logger momLogger;
	private Map<String,Function<HLAVersion,byte[]>> attributeEncoders;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederate( Federate federate, OCInstance federateObject, Logger momLogger )
	{
		this.federate = federate;
		this.federateObject = federateObject;
		this.momLogger = momLogger;
		
		this.attributeEncoders = new HashMap<>();
		this.attributeEncoders.put( "FederateHandle", this::getFederateHandle );
		this.attributeEncoders.put( "FederateName", this::getFederateName );
		this.attributeEncoders.put( "FederateType", this::getFederateType );
		this.attributeEncoders.put( "FederateHost", this::getFederateHost );
		this.attributeEncoders.put( "RTIVersion", this::getRTIversion );
		this.attributeEncoders.put( "FEDid", this::getFEDid );
		this.attributeEncoders.put( "FOMmoduleDesignatorList", this::getFomModuleDesignatorList );
		this.attributeEncoders.put( "TimeConstrained", this::getTimeConstrained );
		this.attributeEncoders.put( "TimeRegulating", this::getTimeRegulating );
		this.attributeEncoders.put( "AsynchronousDelivery", this::getAsynchronousDelivery );
		this.attributeEncoders.put( "FederateState", this::getFederateState );
		this.attributeEncoders.put( "TimeManagerState", this::getTimeManagerState );
		this.attributeEncoders.put( "FederateTime", this::getFederateTime );
		this.attributeEncoders.put( "Lookahead", this::getLookahead );
		this.attributeEncoders.put( "LBTS", this::getLBTS );
		this.attributeEncoders.put( "GALT", this::getGALT );
		this.attributeEncoders.put( "MinNextEventTime", this::getMinNextEventTime );
		this.attributeEncoders.put( "ROlength", this::getROlength );
		this.attributeEncoders.put( "TSOlength", this::getTSOlength );
		this.attributeEncoders.put( "ReflectionsReceived", this::getReflectionsReceived );
		this.attributeEncoders.put( "UpdatesSent", this::getUpdatesSent );
		this.attributeEncoders.put( "InteractionsReceived", this::getInteractionsReceived );
		this.attributeEncoders.put( "InteractionsSent", this::getInteractionsSent );
		this.attributeEncoders.put( "ObjectsOwned", this::getObjectsOwned );
		this.attributeEncoders.put( "ObjectsUpdated", this::getObjectsUpdated );
		this.attributeEncoders.put( "ObjectsReflected", this::getObjectsReflected );
		this.attributeEncoders.put( "ObjectInstancesDeleted", this::getObjectInstancesDeleted );
		this.attributeEncoders.put( "ObjectInstancesRemoved", this::getObjectInstancesRemoved );
		this.attributeEncoders.put( "ObjectInstancesRegistered", this::getObjectInstancesRegistered );
		this.attributeEncoders.put( "ObjectInstancesDiscovered", this::getObjectInstancesDiscovered );
		this.attributeEncoders.put( "TimeGrantedTime", this::getTimeGrantedTime );
		this.attributeEncoders.put( "TimeAdvancingTime", this::getTimeAdvancingTime );
		//this.attributeEncoders.put( "ConveyRegionDesignatorSets", this::get );
		//this.attributeEncoders.put( "ConveyProducingFederate", this:: );
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
		return encodeString( version, federate.getFederateType() );
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
		return getLITS( version );
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
		Set<ACInstance> attributeInstances = this.federateObject.getAllAttributes();
		for( ACInstance instance : attributeInstances )
		{
			int attributeHandle = instance.getType().getHandle();
			if( attributes.containsKey(attributeHandle) )
			{
				String canonicalName = Mom.getMomAttributeName( HLAVersion.HLA13, 
				                                                attributeHandle );
				Function<HLAVersion,byte[]> encoder = this.attributeEncoders.get( canonicalName );
				if( encoder != null )
				{
					byte[] value = encoder.apply( version );
					attributes.put( attributeHandle, value );
				}
				else
				{
					momLogger.warn( "No encoder for attribute " + 
					                attributeHandle + 
					                " [" + canonicalName + "]" );
				}
			}
		}
		
		UpdateAttributes update = new UpdateAttributes( federateObject.getHandle(),
		                                                new byte[0],
		                                                attributes );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}
	
	private byte[] notYetSupported( HLAVersion version, String property )
	{
		momLogger.trace( "Requested MOM property that isn't supported yet: Federate." + property );
		return encodeString( version, "property ["+property+"] not yet supported" );
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
