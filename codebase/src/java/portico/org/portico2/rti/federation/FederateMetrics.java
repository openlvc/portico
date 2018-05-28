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
package org.portico2.rti.federation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.portico.lrc.model.OCMetadata;
import org.portico2.rti.services.mom.data.ObjectClassBasedCount;
import org.portico2.rti.services.object.data.ROCInstance;

/**
 * This class tracks various federate metrics that are ultimately reported in the MOM class
 * <code>HLAobjectRoot.HLAmanager.HLAfederate</code>
 */
public class FederateMetrics
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int reflectionsReceived;
	private int updatesSent;
	private int interactionsReceived;
	private int interactionsSent;
	private Set<ROCInstance> objectsOwned;
	private Set<ROCInstance> objectsUpdated;
	private Set<ROCInstance> objectsReflected;
	private int objectsDeleted;
	private int objectsRemoved;
	private int objectsRegistered;
	private int objectsDiscovered;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateMetrics()
	{
		this.reflectionsReceived = 0;
		this.updatesSent = 0;
		this.interactionsReceived = 0;
		this.interactionsSent = 0;
		this.objectsOwned = new HashSet<ROCInstance>();
		this.objectsUpdated = new HashSet<ROCInstance>();
		this.objectsReflected = new HashSet<ROCInstance>();
		this.objectsDeleted = 0;
		this.objectsRemoved = 0;
		this.objectsRegistered = 0;
		this.objectsDiscovered = 0;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Federate Event Handlers   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void reflectionReceived( ROCInstance instance )
	{
		++this.reflectionsReceived;
		this.objectsReflected.add( instance );
	}
	
	public void sentUpdate( ROCInstance instance )
	{
		++this.updatesSent;
		this.objectsUpdated.add( instance );
	}
	
	public void interactionReceived()
	{
		++this.interactionsReceived;
	}
	
	public void interactionSent()
	{
		++this.interactionsSent;
	}

	public void objectRegistered( ROCInstance instance )
	{
		++this.objectsRegistered;
		
		// When a federate registers an object, they hold privilege to delete until such time as they 
		// yield it, or remove it 
		this.objectsOwned.add( instance );
	}
	
	public void objectDeleted( ROCInstance instance )
	{
		++this.objectsDeleted;
		
		// Object is no longer owned by the federate 
		this.objectsOwned.remove( instance );
	}
	
	public void objectRemoved()
	{
		++this.objectsRemoved;
	}
	
	public void objectDiscovered()
	{
		++this.objectsDiscovered;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the number of times the ReflectAttributeValues service has been invoked on the federate
	 */
	public int getReflectionsReceived()
	{
		return this.reflectionsReceived;
	}
	
	/**
	 * @return the number of times the federate has invoked the UpdateAttributeValues service
	 */
	public int getUpdatesSent()
	{
		return this.updatesSent;
	}
	
	/**
	 * @return the number of times the ReceiveInteraction service has been invoked on the federate
	 */
	public int getInteractionsReceived()
	{
		return this.interactionsReceived;
	}
	
	/**
	 * @return the number of times the federate has invoked the SendInteractions service
	 */
	public int getInteractionsSent()
	{
		return this.interactionsSent;
	}
	
	/**
	 * @return the number of Object Instances for which the federate owns the HLAprivilegeToDelete 
	 *         parameter
	 */
	public Set<ROCInstance> getObjectsOwned()
	{
		return new HashSet<>( this.objectsOwned );
	}
	
	/**
	 * @return the number of Object Instances for which the federate has provided an attribute update
	 */
	public Set<ROCInstance> getObjectsUpdated()
	{
		return new HashSet<>( this.objectsUpdated );
	}
	
	/**
	 * @return the number of Object Instances for which the federate has received an attribute reflection
	 */
	public Set<ROCInstance> getObjectsReflected()
	{
		return new HashSet<>( this.objectsReflected );
	}
	
	/**
	 * @return the number of times the federate has invoked the DeleteObjectInstance service
	 */
	public int getObjectsDeleted()
	{
		return this.objectsDeleted;
	}
	
	/**
	 * @return the number of times the RemoveObjectInstance service has been invoked on the federate
	 */
	public int getObjectsRemoved()
	{
		return this.objectsRemoved;
	}
	
	/**
	 * @return the number of times the federate has invoked the RegisterObjectInstance service
	 */
	public int getObjectsRegistered()
	{
		return this.objectsRegistered;
	}
	
	/**
	 * @return the number of times the DiscoverObjectInstance service has been invoked on the federate
	 */
	public int getObjectsDiscovered()
	{
		return this.objectsDiscovered;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Returns the number of instances of each Object Class in the provided set
	 * 
	 * @param federate the federate that the class handles should be relative to
	 * @param instances the object instances to classify
	 * @return the number of instances of each Object Class in the provided set
	 */
	public static ObjectClassBasedCount[] toClassBasedCount( Federate federate, 
	                                                         Set<ROCInstance> instances )
	{
		Map<OCMetadata,Integer> countMap = new HashMap<>();
		for( ROCInstance instance : instances )
		{
			OCMetadata type = instance.getRegisteredType();	// TODO should be discovered type for some cases?
			Integer count = countMap.get( type );
			if( count == null )
				count = new Integer( 1 );
			else
				count = new Integer( count + 1 );
			countMap.put( type, count );
		}
		
		ObjectClassBasedCount[] countArray = new ObjectClassBasedCount[countMap.size()];
		int index = 0;
		for( Entry<OCMetadata,Integer> entry : countMap.entrySet() )
			countArray[index++] = new ObjectClassBasedCount( entry.getKey(), entry.getValue() );
		
		return countArray;
	}
}
