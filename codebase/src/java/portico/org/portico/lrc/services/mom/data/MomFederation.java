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
import org.portico.impl.hla1516e.types.encoding.HLA1516eUnicodeString;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.management.Federation;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
import org.portico2.common.services.object.msg.UpdateAttributes;

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

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Federation federation;
	protected Map<Integer,MomFederate> federates;
	protected OCInstance federationObject;
	protected Logger momLogger;
	private Map<String,Function<HLAVersion,byte[]>> attributeEncoders;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederation( Federation federation, OCInstance federationObject, Logger lrcLogger )
	{
		this.federation = federation;
		this.federates = new HashMap<Integer,MomFederate>();
		this.federationObject = federationObject;
		this.momLogger = lrcLogger;
		
		this.attributeEncoders = new HashMap<>();
		this.attributeEncoders.put( "FederationName", this::getFederationName );
		this.attributeEncoders.put( "FederatesInFederation", this::getFederatesInFederation );
		this.attributeEncoders.put( "RTIversion", this::getRtiVersion );
		this.attributeEncoders.put( "FEDid", this::getFedID );
		this.attributeEncoders.put( "MIMdesignator", this::getMimDesignator );
		this.attributeEncoders.put( "FOMmoduleDesignatorList", this::getFomModuleDesignatorList );
		this.attributeEncoders.put( "CurrentFDD", this::getCurrentFdd );
		this.attributeEncoders.put( "TimeImplementationName", this::getTimeImplementationName );
		this.attributeEncoders.put( "LastSaveName", this::getLastSaveName );
		this.attributeEncoders.put( "LastSaveTime", this::getLastSaveTime );
		this.attributeEncoders.put( "NextSaveName", this::getNextSaveName );
		this.attributeEncoders.put( "NextSaveTime", this::getNextSaveTime );
		this.attributeEncoders.put( "ConveyRegionDesignatorSets", this::getConveyRegionDesignatorSets );
		this.attributeEncoders.put( "AutoProvide", this::getAutoProvide );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void addFederate( MomFederate momFederate )
	{
		this.federates.put( momFederate.federate.getFederateHandle(), momFederate );
	}
	
	public MomFederate removeFederate( int federateHandle )
	{
		return this.federates.remove( federateHandle );
	}
	
	public MomFederate getFederate( int federateHandle )
	{
		return this.federates.get( federateHandle );
	}

	public void clear()
	{
		this.federates.clear();
	}

	/////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Property Serialization Methods /////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	private byte[] getFederationName( HLAVersion version )
	{
		return encodeString( version, federation.getFederationName() );
	}
	
	private byte[] getFederatesInFederation( HLAVersion version )
	{
		//FIXME yeah, clearly in need of fixing :P
		return notYetSupported( version, "FederatesInFederation" );
	}
	
	private byte[] getRtiVersion( HLAVersion version )
	{
		return encodeString( version, PorticoConstants.RTI_NAME+" v"+PorticoConstants.RTI_VERSION );
	}

	private byte[] getMimDesignator( HLAVersion version )
	{
		return notYetSupported( version, "MimDesignator" );
	}

	private byte[] getFomModuleDesignatorList( HLAVersion version )
	{
		return notYetSupported( version, "HLAversion" );
	}

	private byte[] getCurrentFdd( HLAVersion version )
	{
		return notYetSupported( version, "CurrentFDD" );
	}

	private byte[] getTimeImplementationName( HLAVersion version )
	{
		return notYetSupported( version, "TimeImplementation" );
	}

	private byte[] getFedID( HLAVersion version )
	{
		//FIXME Obviously
		return notYetSupported( version, "FedID" );
	}
	
	private byte[] getLastSaveName( HLAVersion version )
	{
		return notYetSupported( version, "LastSaveName" );
	}
	
	private byte[] getLastSaveTime( HLAVersion version )
	{
		//return JEncodingHelpers.encodeDouble( 0.0 );
		return notYetSupported( version, "LastSaveTime" );
	}
	
	private byte[] getNextSaveName( HLAVersion version )
	{
		return notYetSupported( version, "NextSaveName" );
	}
	
	private byte[] getNextSaveTime( HLAVersion version )
	{
		//return JEncodingHelpers.encodeDouble( 0.0 );
		return notYetSupported( version, "NextSaveTime" );
	}
	
	private byte[] getConveyRegionDesignatorSets( HLAVersion version )
	{
		return notYetSupported( version, "ConveyRegionDesignatorSets" );
	}
	
	private byte[] getAutoProvide( HLAVersion version )
	{
		return notYetSupported( version, "AutoProvide" );
	}

	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Update Generating Methods ///////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	public UpdateAttributes generateUpdate( HLAVersion version, Set<Integer> handles )
		throws JAttributeNotDefined
	{
		HashMap<Integer,byte[]> attributes = new HashMap<Integer,byte[]>();
		Set<ACInstance> attributeInstances = this.federationObject.getAllAttributes();
		for( ACInstance instance : attributeInstances )
		{
			int attributeHandle = instance.getType().getHandle();
			if( handles.contains(attributeHandle) )
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
		
		UpdateAttributes update = new UpdateAttributes( federationObject.getHandle(),
		                                                new byte[0],
		                                                attributes );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}

	private byte[] notYetSupported( HLAVersion version, String property )
	{
		momLogger.trace( "Requeted MOM property that isn't supported yet: Federation." + property );
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
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
