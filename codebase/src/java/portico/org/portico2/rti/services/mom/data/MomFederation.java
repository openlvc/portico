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

import java.util.HashMap;
import java.util.HashSet;
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
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.XmlRenderer;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.services.object.data.RACInstance;
import org.portico2.rti.services.object.data.ROCInstance;
import org.w3c.dom.Document;

/**
 * Contains links between the {@link OCInstance} used to represent the federation in the federation
 * and the {@link Federation} management object that holds all the information. This class also
 * serializes the information for updates when they are requested.
 */
public class MomFederation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final HLAVersion CANONICAL_VERSION = HLAVersion.IEEE1516e;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Federation federation;
	private Map<Integer,MomFederate> federates;
	private ROCInstance federationObject;
	private Logger momLogger;
	private Map<String,Function<ACMetadata,byte[]>> attributeEncoders;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederation( Federation federation, ROCInstance federationObject, Logger lrcLogger )
	{
		this.federation = federation;
		this.federates = new HashMap<Integer,MomFederate>();
		this.federationObject = federationObject;
		this.momLogger = lrcLogger;
		
		// Map encoding functions against the canonical name of the attribute they provide data for
		this.attributeEncoders = new HashMap<>();
		this.attributeEncoders.put( "HLAfederationName", this::getFederationName );
		this.attributeEncoders.put( "HLAfederatesInFederation", this::getFederatesInFederation );
		this.attributeEncoders.put( "HLARTIversion", this::getRtiVersion );
		this.attributeEncoders.put( "HLAFEDid", this::getFedID );
		this.attributeEncoders.put( "HLAMIMdesignator", this::getMimDesignator );
		this.attributeEncoders.put( "HLAFOMmoduleDesignatorList", this::getFomModuleDesignatorList );
		this.attributeEncoders.put( "HLAcurrentFDD", this::getCurrentFdd );
		this.attributeEncoders.put( "HLAtimeImplementationName", this::getTimeImplementationName );
		this.attributeEncoders.put( "HLAlastSaveName", this::getLastSaveName );
		this.attributeEncoders.put( "HLAlastSaveTime", this::getLastSaveTime );
		this.attributeEncoders.put( "HLAnextSaveName", this::getNextSaveName );
		this.attributeEncoders.put( "HLAnextSaveTime", this::getNextSaveTime );
		this.attributeEncoders.put( "HLAconveyRegionDesignatorSets", this::getConveyRegionDesignatorSets );
		this.attributeEncoders.put( "HLAautoProvide", this::getAutoProvide );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public int getObjectIntanceHandle()
	{
		return this.federationObject.getHandle();
	}
	
	public void addFederate( MomFederate momFederate )
	{
		int handle = momFederate.getFederateHandle();
		this.federates.put( handle, momFederate );
	}
	
	public MomFederate removeFederate( int federateHandle )
	{
		return this.federates.remove( federateHandle );
	}
	
	public MomFederate removeFederate( String name )
	{
		int handle = ObjectModel.INVALID_HANDLE;
		
		for( MomFederate federate : federates.values() )
		{
			String federateName = federate.getFederateName();
			if( federateName.equals(name) )
			{
				handle = federate.getFederateHandle();
				break;
			}
		}
		
		return removeFederate( handle );
	}
	
	public MomFederate getFederate( int federateHandle )
	{
		return this.federates.get( federateHandle );
	}

	public Set<MomFederate> getFederates()
	{
		return new HashSet<MomFederate>( this.federates.values() );
	}
	
	public void clear()
	{
		this.federates.clear();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Update Generation Methods   ///////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public UpdateAttributes generateUpdate( HLAVersion version, Set<Integer> handles )
		throws JAttributeNotDefined
	{
		HashMap<Integer,byte[]> attributes = new HashMap<Integer,byte[]>();
		
		// Iterate over all attributes of the Federate object
		Set<RACInstance> attributeInstances = this.federationObject.getAllAttributes();
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
					byte[] value = encoder.apply( instance.getType() );
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
		UpdateAttributes update = new UpdateAttributes( federationObject.getHandle(),
		                                                new byte[0],
		                                                attributes );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}
	
	private byte[] getFederationName( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, federation.getFederationName() );
	}
	
	private byte[] getFederatesInFederation( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		Set<Integer> federateHandles = this.federates.keySet();
		int[] handleArray = new int[federateHandles.size()];
		int index = 0;
		for( int handle : federateHandles )
			handleArray[index++] = handle;
		
		return MomEncodingHelpers.encode( type, handleArray );
	}
	
	private byte[] getRtiVersion( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		return MomEncodingHelpers.encode( type, 
		                                  PorticoConstants.RTI_NAME+" v"+PorticoConstants.RTI_VERSION );
	}

	private byte[] getMimDesignator( ACMetadata metadata )
	{
		return notYetSupported( "HLAMIMDesignator" );
	}

	private byte[] getFomModuleDesignatorList( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		List<FomModule> modules = federation.getRawFomModules();
		int moduleCount = modules.size();
		String[] designators = new String[moduleCount];
		for( int i = 0 ; i < moduleCount ; ++i )
			designators[i] = modules.get( i ).getDesignator();
		
		return MomEncodingHelpers.encode( type, designators );
	}

	private byte[] getCurrentFdd( ACMetadata metadata )
	{
		IDatatype type = metadata.getDatatype();
		ObjectModel fom = federation.getFOM();
		Document asXml = new XmlRenderer().renderFOM( fom );
		String asString = XmlRenderer.xmlToString( asXml );
		
		return MomEncodingHelpers.encode( type, asString );
	}

	private byte[] getTimeImplementationName( ACMetadata metadata )
	{
		// We currently don't track time implementation. All time values are doubles internally
		return notYetSupported( "HLAtimeImplementationName" );
	}

	private byte[] getFedID( ACMetadata metadata )
	{
		//FIXME Obviously
		return notYetSupported( "HLAFEDid" );
	}
	
	private byte[] getLastSaveName( ACMetadata metadata )
	{
		return notYetSupported( "HLAlastSaveName" );
	}
	
	private byte[] getLastSaveTime( ACMetadata metadata )
	{
		//return JEncodingHelpers.encodeDouble( 0.0 );
		return notYetSupported( "HLAlastSaveTime" );
	}
	
	private byte[] getNextSaveName( ACMetadata metadata )
	{
		return notYetSupported( "HLAnextSaveName" );
	}
	
	private byte[] getNextSaveTime( ACMetadata metadata )
	{
		//return JEncodingHelpers.encodeDouble( 0.0 );
		return notYetSupported( "HLAnextSaveTime" );
	}
	
	private byte[] getConveyRegionDesignatorSets( ACMetadata metadata )
	{
		return notYetSupported( "HLAconveyRegionDesignatorSets" );
	}
	
	private byte[] getAutoProvide( ACMetadata metadata )
	{
		return notYetSupported( "HLAautoProvide" );
	}
	
	private byte[] notYetSupported( String property )
	{
		momLogger.trace( "Requested MOM property that isn't supported yet: Federation." + property );
		String message = "property ["+property+"] not yet supported";
		HLAVersion version = this.federation.getHlaVersion();
		switch( version )
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
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
