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

import org.apache.logging.log4j.Logger;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.management.Federation;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
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

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Federation federation;
	protected Map<Integer,MomFederate> federates;
	protected OCInstance federationObject;
	protected Logger momLogger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederation( Federation federation, OCInstance federationObject, Logger lrcLogger )
	{
		this.federation = federation;
		this.federates = new HashMap<Integer,MomFederate>();
		this.federationObject = federationObject;
		this.momLogger = lrcLogger;
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
	private byte[] getFederationName()
	{
		return JEncodingHelpers.encodeString( federation.getFederationName() );
	}
	
	private byte[] getFederatesInFederation()
	{
		//FIXME yeah, clearly in need of fixing :P
		return notYetSupported( "FederatesInFederation" );
	}
	
	private byte[] getRtiVersion()
	{
		return JEncodingHelpers.encodeString( PorticoConstants.RTI_NAME +
		                                      " v" +
		                                      PorticoConstants.RTI_VERSION );
	}
	
	private byte[] getFedID()
	{
		//FIXME Obviously
		return notYetSupported( "FedID" );
	}
	
	private byte[] getLastSaveName()
	{
		return notYetSupported( "LastSaveName" );
	}
	
	private byte[] getLastSaveTime()
	{
		//return JEncodingHelpers.encodeDouble( 0.0 );
		return notYetSupported( "LastSaveTime" );
	}
	
	private byte[] getNextSaveName()
	{
		return notYetSupported( "NextSaveName" );
	}
	
	private byte[] getNextSaveTime()
	{
		//return JEncodingHelpers.encodeDouble( 0.0 );
		return notYetSupported( "NextSaveTime" );
	}

	/////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Update Generating Methods ///////////////////////////
	/////////////////////////////////////////////////////////////////////////////////
	public UpdateAttributes generateUpdate( Set<Integer> handles ) throws JAttributeNotDefined
	{
		HashMap<Integer,byte[]> attributes = new HashMap<Integer,byte[]>();

		// loop through the attributes and get the appropriate values
		for( Integer attributeHandle : handles )
		{
			Mom.Federation enumValue = Mom.Federation.forHandle( attributeHandle );
			switch( enumValue )
			{
				case FederationName:
					attributes.put( attributeHandle, getFederationName() );
					break;
				case FederatesInFederation:
					attributes.put( attributeHandle, getFederatesInFederation() );
					break;
				case RtiVersion:
					attributes.put( attributeHandle, getRtiVersion() );
					break;
				case FedID:
					attributes.put( attributeHandle, getFedID() );
					break;
				case LastSaveName:
					attributes.put( attributeHandle, getLastSaveName() );
					break;
				case LastSaveTime:
					attributes.put( attributeHandle, getLastSaveTime() );
					break;
				case NextSaveName:
					attributes.put( attributeHandle, getNextSaveName() );
					break;
				case NextSaveTime:
					attributes.put( attributeHandle, getNextSaveTime() );
					break;
				case AutoProvide:
					attributes.put( attributeHandle, notYetSupported("AutoProvide") );
					break;
				case ConveyRegionDesignatorSets:
					attributes.put( attributeHandle, notYetSupported("ConveyRegionDesignatorSets") );
					break;
				default:
					break;
			}
		}
		
		UpdateAttributes update = new UpdateAttributes( federationObject.getHandle(),
		                                                new byte[0],
		                                                attributes );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}

	private byte[] notYetSupported( String property )
	{
		momLogger.trace( "Requeted MOM property that isn't supported yet: Federation." + property );
		return JEncodingHelpers.encodeString( "property ["+property+"] not yet supported" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
