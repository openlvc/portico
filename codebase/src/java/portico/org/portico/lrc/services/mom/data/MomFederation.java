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
import org.portico.impl.hla1516e.types.encoding.HLA1516eUnicodeString;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.management.Federation;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.object.msg.UpdateAttributes;

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
	//
	// Names and types from 1516e MIM
	// HLAfederationName            // HLAunicodeString
	// HLAfederatesInFederation     // HLAhandleList
	// HLARTIversion                // HLAunicodeString
	// HLAMIMdesignator             // HLAunicodeString
	// HLAFOMmoduleDesignatorList   // HLAmoduleDesignatorList
	// HLAcurrentFDD                // HLAunicodeString
	// HLAtimeImplementationName    // HLAunicodeString
	// HLAlastSaveName              // HLAunicodeString
	// HLAlastSaveTime              // HLAlogicalTime
	// HLAnextSaveName              // HLAunicodeString
	// HLAnextSaveTime              // HLAlogicalTime
	// HLAautoProvide               // HLAswitch

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Federation federation;
	protected Map<Integer,MomFederate> federates;
	protected OCInstance federationObject;
	protected Logger momLogger;
	private Map<Integer,IMomVariable> handleMap;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederation( HLAVersion version,
	                      Federation federation,
	                      OCInstance federationObject,
	                      Logger lrcLogger )
	{
		this.federation = federation;
		this.federates = new HashMap<Integer,MomFederate>();
		this.federationObject = federationObject;
		this.momLogger = lrcLogger;
		
		// populate the handles map from the object model
		if( version == HLAVersion.HLA13 )
			this.handleMap = this.loadHla13HandlesFrom( federationObject.getRegisteredType() );
		else
			this.handleMap = this.loadHla1516HandlesFrom( federationObject.getRegisteredType() );
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
		
		UpdateAttributes update = new UpdateAttributes( federationObject.getHandle(),
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
		map.put( type.getAttributeHandle("HLAfederationName"), this::getFederationName );
		map.put( type.getAttributeHandle("HLAfederatesInFederation"), this::getFederatesInFederation );
		map.put( type.getAttributeHandle("HLARTIversion"), this::getRtiVersion );
		map.put( type.getAttributeHandle("HLAMIMdesignator"), this::getMimDesignator );
		map.put( type.getAttributeHandle("HLAFOMmoduleDesignatorList"), this::getFomModuleDesignatorList );
		map.put( type.getAttributeHandle("HLAcurrentFDD"), this::getCurrentFdd );
		map.put( type.getAttributeHandle("HLAtimeImplementationName"), this::getTimeImplementationName );
		map.put( type.getAttributeHandle("HLAlastSaveName"), this::getLastSaveName );
		map.put( type.getAttributeHandle("HLAlastSaveTime"), this::getLastSaveTime );
		map.put( type.getAttributeHandle("HLAnextSaveName"), this::getNextSaveName );
		map.put( type.getAttributeHandle("HLAnextSaveTime"), this::getNextSaveTime );
		map.put( type.getAttributeHandle("HLAautoProvide"), this::getAutoProvide );

		// remove any that were invalid
		map.remove( ObjectModel.INVALID_HANDLE );
		return map;
	}
	
	public HashMap<Integer,IMomVariable> loadHla13HandlesFrom( OCMetadata type )
	{
		HashMap<Integer,IMomVariable> map = new HashMap<>();
		map.put( type.getAttributeHandle("FederationName"), this::getFederationName );
		map.put( type.getAttributeHandle("FederatesInFederation"), this::getFederatesInFederation );
		map.put( type.getAttributeHandle("RTIversion"), this::getRtiVersion );
		map.put( type.getAttributeHandle("FEDid"), this::getCurrentFdd );
		map.put( type.getAttributeHandle("HLAtimeImplementationName"), this::getTimeImplementationName );
		map.put( type.getAttributeHandle("LastSaveName"), this::getLastSaveName );
		map.put( type.getAttributeHandle("LastSaveTime"), this::getLastSaveTime );
		map.put( type.getAttributeHandle("NextSaveName"), this::getNextSaveName );
		map.put( type.getAttributeHandle("NextSaveTime"), this::getNextSaveTime );

		// remove any that were invalid
		map.remove( ObjectModel.INVALID_HANDLE );
		return map;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	///  Encoding Helper Methods   ///////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private byte[] notYetSupported( HLAVersion version, String property )
	{
		momLogger.trace( "Requeted MOM property that isn't supported yet: Federation." + property );
		//eturn encodeString( version, "property ["+property+"] not yet supported" );
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
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
