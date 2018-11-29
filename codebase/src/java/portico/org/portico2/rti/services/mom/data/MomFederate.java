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
package org.portico2.rti.services.mom.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.hla1516e.types.encoding.HLA1516eUnicodeString;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.rti.federation.Federate;
import org.portico2.rti.services.object.data.RACInstance;
import org.portico2.rti.services.object.data.ROCInstance;
import org.portico2.rti.services.time.data.TimeManager;

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
	private static final HLAVersion CANONICAL_VERSION = HLAVersion.IEEE1516e;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ROCInstance federateObject;
	private Federate federate;
	private Logger momLogger;
	private HLAVersion version;
	private TimeManager timeManager;
	private Map<String,Function<ACMetadata,byte[]>> attributeEncoders;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederate( Federate federate, 
	                    ROCInstance federateObject, 
	                    HLAVersion version,
	                    TimeManager timeManager,
	                    Logger momLogger )
	{
		this.federate = federate;
		this.federateObject = federateObject;
		this.version = version;
		this.timeManager = timeManager;
		this.momLogger = momLogger;
		
		// Map encoding functions against the canonical name of the attribute they provide data for
		this.attributeEncoders = new HashMap<>();
		this.attributeEncoders.put( "HLAfederateHandle", this::getFederateHandle );
		this.attributeEncoders.put( "HLAfederateName", this::getFederateName );
		this.attributeEncoders.put( "HLAfederateType", this::getFederateType );
		this.attributeEncoders.put( "HLAfederateHost", this::getFederateHost );
		this.attributeEncoders.put( "HLARTIversion", this::getRTIversion );
		this.attributeEncoders.put( "HLAFEDid", this::getFEDid );
		this.attributeEncoders.put( "HLAFOMmoduleDesignatorList", this::getFomModuleDesignatorList );
		this.attributeEncoders.put( "HLAtimeConstrained", this::getTimeConstrained );
		this.attributeEncoders.put( "HLAtimeRegulating", this::getTimeRegulating );
		this.attributeEncoders.put( "HLAasynchronousDelivery", this::getAsynchronousDelivery );
		this.attributeEncoders.put( "HLAfederateState", this::getFederateState );
		this.attributeEncoders.put( "HLAtimeManagerState", this::getTimeManagerState );
		this.attributeEncoders.put( "HLAlogicalTime", this::getFederateTime );
		this.attributeEncoders.put( "HLAlookahead", this::getLookahead );
		this.attributeEncoders.put( "HLALBTS", this::getLBTS );
		this.attributeEncoders.put( "HLAGALT", this::getGALT );
		this.attributeEncoders.put( "HLALITS", this::getMinNextEventTime );
		this.attributeEncoders.put( "HLAROlength", this::getROlength );
		this.attributeEncoders.put( "HLATSOlength", this::getTSOlength );
		this.attributeEncoders.put( "HLAreflectionsReceived", this::getReflectionsReceived );
		this.attributeEncoders.put( "HLAupdatesSent", this::getUpdatesSent );
		this.attributeEncoders.put( "HLAinteractionsReceived", this::getInteractionsReceived );
		this.attributeEncoders.put( "HLAinteractionsSent", this::getInteractionsSent );
		this.attributeEncoders.put( "HLAobjectInstancesThatCanBeDeleted", this::getObjectsOwned );
		this.attributeEncoders.put( "HLAobjectInstancesUpdated", this::getObjectsUpdated );
		this.attributeEncoders.put( "HLAobjectInstancesReflected", this::getObjectsReflected );
		this.attributeEncoders.put( "HLAobjectInstancesDeleted", this::getObjectInstancesDeleted );
		this.attributeEncoders.put( "HLAobjectInstancesRemoved", this::getObjectInstancesRemoved );
		this.attributeEncoders.put( "HLAobjectInstancesRegistered", this::getObjectInstancesRegistered );
		this.attributeEncoders.put( "HLAobjectInstancesDiscovered", this::getObjectInstancesDiscovered );
		this.attributeEncoders.put( "HLAtimeGrantedTime", this::getTimeGrantedTime );
		this.attributeEncoders.put( "HLAtimeAdvancingTime", this::getTimeAdvancingTime );
		
		// Not yet supported...
		// this.attributeEncoders.put( "ConveyRegionDesignatorSets", this::get );
		// this.attributeEncoders.put( "ConveyProducingFederate", this:: );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public int getFederateHandle()
	{
		return this.federate.getFederateHandle();
	}
	
	public String getFederateName()
	{
		return this.federate.getFederateName();
	}
	
	public String getFederateType()
	{
		return this.federate.getFederateType();
	}
	
	public int getFederateObjectInstanceHandle()
	{
		return this.federateObject.getHandle();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Update Generation Methods   ///////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public UpdateAttributes generateUpdate( Set<Integer> handles )
		throws JAttributeNotDefined
	{
		HashMap<Integer,byte[]> attributes = new HashMap<Integer,byte[]>();
		
		// Iterate over all attributes of the Federate object
		Set<RACInstance> attributeInstances = this.federateObject.getAllAttributes();
		for( RACInstance instance : attributeInstances )
		{
			// Has the sender requested this attribute?
			int attributeHandle = instance.getType().getHandle();
			if( handles.contains(attributeHandle) )
			{
				// Resolve the encoder for this attribute using its canonical name
				String canonicalName = Mom.getMomAttributeName( CANONICAL_VERSION, 
				                                                attributeHandle );
				Function<ACMetadata,byte[]> encoder = this.attributeEncoders.get( canonicalName );
				if( encoder != null )
				{
					// An encoder was found, so encode the value and add it to the response attribute map
					ACMetadata metadata = instance.getType();
					byte[] value = encoder.apply( metadata );
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
		
		// Send the update!
		UpdateAttributes update = new UpdateAttributes( federateObject.getHandle(),
		                                                new byte[0],
		                                                attributes );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}
	
	private byte[] getFederateHandle( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, federate.getFederateHandle() );
	}

	private byte[] getFederateType( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, federate.getFederateType() );
	}
	
	private byte[] getFederateName( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, federate.getFederateName() );
	}

	private byte[] getFederateHost( ACMetadata metadata )
	{
		return notYetSupported( "HLAfederateHost" );
	}
	
	private byte[] getFomModuleDesignatorList( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		List<FomModule> modules = federate.getRawFomModules();
		int moduleCount = modules.size();
		String[] designators = new String[moduleCount];
		for( int i = 0 ; i < moduleCount ; ++i )
			designators[i] = modules.get( i ).getDesignator();
		
		return MomEncodingHelpers.encode( type, designators );
	}

	private byte[] getRTIversion( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, 
		                                  PorticoConstants.RTI_NAME+" v"+PorticoConstants.RTI_VERSION );
	}

	private byte[] getFEDid( ACMetadata metadata )
	{
		return notYetSupported( "FEDid" );
	}

	private byte[] getTimeConstrained( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, timeManager.isConstrained(getFederateHandle()) );
	}

	private byte[] getTimeRegulating( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, timeManager.isRegulating(getFederateHandle()) );
	}

	private byte[] getAsynchronousDelivery( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		TimeStatus status = timeManager.getTimeStatus( getFederateHandle() );
		return MomEncodingHelpers.encode( type, status.isAsynchronous() );
	}

	private byte[] getFederateState( ACMetadata metadata )
	{
		return notYetSupported( "HLAfederateState" );
	}

	private byte[] getTimeManagerState( ACMetadata metadata )
	{
		return notYetSupported( "HLAtimeManagerState" );
	}

	private byte[] getFederateTime( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, timeManager.getCurrentTime(getFederateHandle()) );
	}

	private byte[] getLookahead( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, timeManager.getLookahead(getFederateHandle()) );
	}

	private byte[] getLBTS( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, timeManager.getLBTS(getFederateHandle()) );
	}

	private byte[] getMinNextEventTime( ACMetadata metadata )
	{
		return notYetSupported("HLALITS");
	}
	
	private byte[] getGALT( ACMetadata metadata )
	{
		return notYetSupported("HLAGALT");
	}

	private byte[] getROlength( ACMetadata metadata )
	{
		return notYetSupported("HLAROlength");
	}

	private byte[] getTSOlength( ACMetadata metadata )
	{
		return notYetSupported("HLATSOlength");
	}

	private byte[] getReflectionsReceived( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		int grandTotal = federate.getMetrics().getTotalReflectionsReceived();
		
		return MomEncodingHelpers.encode( type, grandTotal );
	}

	private byte[] getUpdatesSent( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		int grandTotal = federate.getMetrics().getTotalUpdatesSent();
		
		return MomEncodingHelpers.encode( type, grandTotal );
	}

	private byte[] getInteractionsReceived( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		int grandTotal = federate.getMetrics().getTotalInteractionsReceived();
		
		return MomEncodingHelpers.encode( type, grandTotal );
	}

	private byte[] getInteractionsSent( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		int grandTotal = federate.getMetrics().getTotalInteractionsSent();
		
		return MomEncodingHelpers.encode( type, grandTotal );
	}

	private byte[] getObjectsOwned( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		Set<Integer> owned = federate.getMetrics().getObjectsOwned();
		return MomEncodingHelpers.encode( type, owned.size() );
	}

	private byte[] getObjectsUpdated( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		int grandTotal = federate.getMetrics().getTotalObjectInstancesUpdated();
		
		return MomEncodingHelpers.encode( type, grandTotal );
	}

	private byte[] getObjectsReflected( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		int grandTotal = federate.getMetrics().getTotalObjectInstancesReflected();
		
		return MomEncodingHelpers.encode( type, grandTotal );
	}

	private byte[] getObjectInstancesDeleted( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, federate.getMetrics().getObjectsDeleted() );
	}

	private byte[] getObjectInstancesRemoved( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, federate.getMetrics().getObjectsRemoved() );
	}

	private byte[] getObjectInstancesRegistered( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, federate.getMetrics().getObjectsRegistered() );
	}

	private byte[] getObjectInstancesDiscovered( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, federate.getMetrics().getObjectsDiscovered() );
	}

	private byte[] getTimeGrantedTime( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, timeManager.getCurrentTime(getFederateHandle()) );
	}

	private byte[] getTimeAdvancingTime( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, timeManager.getRequestedTime(getFederateHandle()) );
	}
	
	private byte[] notYetSupported( String property )
	{
		momLogger.trace( "Requested MOM property that isn't supported yet: Federate." + property );
		String message = "property ["+property+"] not yet supported";
		switch( this.version )
		{
			case JAVA1:
			case HLA13:
				return JEncodingHelpers.encodeString( message );
			case IEEE1516:
			case IEEE1516e:
				return new HLA1516eUnicodeString( message ).toByteArray();
			default:
				throw new IllegalArgumentException( "Unknown Spec Version: "+version );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
