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

import java.util.HashSet;
import java.util.Set;

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
	private Set<Integer> objectsOwned;
	private Set<Integer> objectsUpdated;
	private Set<Integer> objectsReflected;
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
		this.objectsOwned = new HashSet<Integer>();
		this.objectsUpdated = new HashSet<Integer>();
		this.objectsReflected = new HashSet<Integer>();
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
	public void reflectionReceived( int objectHandle )
	{
		++this.reflectionsReceived;
		this.objectsReflected.add( objectHandle );
	}
	
	public void sentUpdate( int objectHandle )
	{
		++this.updatesSent;
		this.objectsUpdated.add( objectHandle );
	}
	
	public void interactionReceived()
	{
		++this.interactionsReceived;
	}
	
	public void interactionSent()
	{
		++this.interactionsSent;
	}

	public void objectRegistered( int objectHandle )
	{
		++this.objectsRegistered;
		
		// When a federate registers an object, they hold privilege to delete until such time as they 
		// yield it, or remove it 
		this.objectsOwned.add( objectHandle );
	}
	
	public void objectDeleted( int objectHandle )
	{
		++this.objectsDeleted;
		
		// Object is no longer owned by the federate 
		this.objectsOwned.remove( objectHandle );
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
	public int getObjectsOwned()
	{
		return this.objectsOwned.size();
	}
	
	/**
	 * @return the number of Object Instances for which the federate has provided an attribute update
	 */
	public int getObjectsUpdated()
	{
		return this.objectsUpdated.size();
	}
	
	/**
	 * @return the number of Object Instances for which the federate has received an attribute reflection
	 */
	public int getObjectsReflected()
	{
		return this.objectsReflected.size();
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
}
