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
import java.util.Set;

import org.apache.log4j.Logger;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JEncodingHelpers;
import org.portico.lrc.management.Federate;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
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

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected OCInstance federateObject;
	protected Federate federate;
	protected Logger momLogger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MomFederate( Federate federate, OCInstance federateObject, Logger momLogger )
	{
		this.federate = federate;
		this.federateObject = federateObject;
		this.momLogger = momLogger;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public byte[] getFederateHandle()
	{
		//return JEncodingHelpers.encodeInt( federate.getHandle() );
		return JEncodingHelpers.encodeString( "" + federate.getFederateHandle() );
	}

	public byte[] getFederateType()
	{
		return JEncodingHelpers.encodeString( federate.getFederateName() ); // wrong in 1516e
	}
	
	public byte[] getFederateName()
	{
		return JEncodingHelpers.encodeString( federate.getFederateName() );
	}

	public byte[] getFederateHost()
	{
		return JEncodingHelpers.encodeString( "not currently supported" );
	}

	public byte[] getRTIversion()
	{
		return JEncodingHelpers.encodeString( PorticoConstants.RTI_NAME +
		                                      " v" +
		                                      PorticoConstants.RTI_VERSION );
	}

	public byte[] getFEDid()
	{
		return JEncodingHelpers.encodeString( "not currently supported" );
	}

	public byte[] getTimeConstrained()
	{
		//return JEncodingHelpers.encodeBoolean( federate.getTimeStatus().isConstrained() );
		return JEncodingHelpers.encodeString( "" + federate.getTimeStatus().isConstrained() );
	}

	public byte[] getTimeRegulating()
	{
		//return JEncodingHelpers.encodeBoolean( federate.getTimeStatus().isRegulating() );
		return JEncodingHelpers.encodeString( "" + federate.getTimeStatus().isRegulating() );
	}

	public byte[] getAsynchronousDelivery()
	{
		return notYetSupported("AsynchronousDelivery");
	}

	public byte[] getFederateState()
	{
		return notYetSupported("FederateState");
	}

	public byte[] getTimeManagerState()
	{
		return notYetSupported("TimeManagerState");
	}

	public byte[] getFederateTime()
	{
		//return JEncodingHelpers.encodeDouble( federate.getTimeStatus().getCurrentTime() );
		return JEncodingHelpers.encodeString( "" + federate.getTimeStatus().getCurrentTime() );
	}

	public byte[] getLookahead()
	{
		//return JEncodingHelpers.encodeDouble( federate.getTimeStatus().getLookahead() );
		return JEncodingHelpers.encodeString( "" + federate.getTimeStatus().getLookahead() );
	}

	public byte[] getLBTS()
	{
		//return JEncodingHelpers.encodeDouble( federate.getTimeStatus().getLBTS() );
		return JEncodingHelpers.encodeString( "" + federate.getTimeStatus().getLbts() );
	}

	public byte[] getMinNextEventTime()
	{
		return getLBTS();
	}
	
	public byte[] getGALT()
	{
		return notYetSupported("GALT");
	}

	public byte[] getLITS()
	{
		return notYetSupported("LITS");
	}

	public byte[] getROlength()
	{
		return notYetSupported("ROlength");
	}

	public byte[] getTSOlength()
	{
		return notYetSupported("TSOlength");
	}

	public byte[] getReflectionsReceived()
	{
		return notYetSupported("ReflectionsReceived");
	}

	public byte[] getUpdatesSent()
	{
		return notYetSupported("UpdatesSent");
	}

	public byte[] getInteractionsReceived()
	{
		return notYetSupported("InteractionsReceived");
	}

	public byte[] getInteractionsSent()
	{
		return notYetSupported("InteractionsSent");
	}

	public byte[] getObjectsOwned()
	{
		return notYetSupported("ObjectOwned");
	}

	public byte[] getObjectsUpdated()
	{
		return notYetSupported("ObjectsUpdated");
	}

	public byte[] getObjectsReflected()
	{
		return notYetSupported("ObjectsReflected");
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
			Mom.Federate enumValue = Mom.Federate.forHandle( attributeHandle );
			switch( enumValue )
			{
				case FederateName:
					attributes.put( attributeHandle, getFederateName() );
					break;
				case FederateHandle:
					attributes.put( attributeHandle, getFederateHandle() );
					break; // not yet supported
				case FederateType:
					attributes.put( attributeHandle, getFederateType() );
					break; // not yet supported
				case FederateHost:
					attributes.put( attributeHandle, getFederateHost() );
					break; // not yet supported
				case RtiVersion:
					attributes.put( attributeHandle, getRTIversion() );
					break; // not yet supported
				case FedID:
					attributes.put( attributeHandle, getFEDid() );
					break; // not yet supported
				case TimeConstrained:
					attributes.put( attributeHandle, getTimeConstrained() );
					break; // not yet supported
				case TimeRegulating:
					attributes.put( attributeHandle, getTimeRegulating() );
					break; // not yet supported
				case AsynchronousDelivery:
					attributes.put( attributeHandle, getAsynchronousDelivery() );
					break; // not yet supported
				case FederateState:
					attributes.put( attributeHandle, getFederateState() );
					break; // not yet supported
				case TimeManagerState:
					attributes.put( attributeHandle, getTimeManagerState() );
					break; // not yet supported
				case LogicalTime:
					attributes.put( attributeHandle, getFederateTime() );
					break; // not yet supported
				case Lookahead:
					attributes.put( attributeHandle, getLookahead() );
					break; // not yet supported
				case LBTS:
					attributes.put( attributeHandle, getLBTS() );
					break; // not yet supported
				case GALT:
					attributes.put( attributeHandle, getGALT() );
					break; // not yet supported
				case LITS:
					attributes.put( attributeHandle, getLITS() );
					break; // not yet supported
				case ROlength:
					attributes.put( attributeHandle, getROlength() );
					break; // not yet supported
				case TSOlength:
					attributes.put( attributeHandle, getTSOlength() );
					break; // not yet supported
				case ReflectionsReceived:
					attributes.put( attributeHandle, getReflectionsReceived() );
					break; // not yet supported
				case UpdatesSent:
					attributes.put( attributeHandle, getUpdatesSent() );
					break; // not yet supported
				case InteractionsReceived:
					attributes.put( attributeHandle, getInteractionsReceived() );
					break; // not yet supported
				case InteractionsSent:
					attributes.put( attributeHandle, getInteractionsSent() );
					break; // not yet supported
				case ObjectInstancesThatCanBeDeleted:
					attributes.put( attributeHandle, getObjectsOwned() );
					break; // not yet supported
				case ObjectInstancesUpdated:
					attributes.put( attributeHandle, getObjectsUpdated() );
					break; // not yet supported
				case ObjectInstancesReflected:
					attributes.put( attributeHandle, getObjectsReflected() );
					break; // not yet supported
				case ObjectInstancesDeleted:
					attributes.put( attributeHandle, notYetSupported("ObjectInstancesDeleted") );
					break; // not yet supported
				case ObjectInstancesRemoved:
					attributes.put( attributeHandle, notYetSupported("ObjectInstancesRemoved") );
					break; // not yet supported
				case ObjectInstancesRegistered:
					attributes.put( attributeHandle, notYetSupported("ObjectInstancesRegistered") );
					break; // not yet supported
				case ObjectInstancesDiscovered:
					attributes.put( attributeHandle, notYetSupported("ObjectInstancesDiscovered") );
					break; // not yet supported
				case TimeGrantedTime:
					attributes.put( attributeHandle, notYetSupported("TimeGrantedTime") );
					break; // not yet supported
				case TimeAdvancingTime:
					attributes.put( attributeHandle, notYetSupported("TimeAdvancingTime") );
					break; // not yet supported
				default:
					break; // ignore
			}
			
		}
		
		UpdateAttributes update = new UpdateAttributes( federateObject.getHandle(),
		                                                new byte[0],
		                                                attributes );
		update.setSourceFederate( PorticoConstants.RTI_HANDLE );
		return update;
	}
	
	private byte[] notYetSupported( String property )
	{
		momLogger.trace( "Requeted MOM property that isn't supported yet: Federate." + property );
		return JEncodingHelpers.encodeString( "property ["+property+"] not yet supported" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
