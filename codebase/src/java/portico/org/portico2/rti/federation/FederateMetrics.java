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
import java.util.Set;

import org.portico2.rti.services.mom.data.InteractionCount;
import org.portico2.rti.services.mom.data.ObjectClassBasedCount;

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
	private Map<Integer,InteractionCount> interactionsReceived;
	private Map<Integer,InteractionCount> interactionsSent;
	private Set<Integer> objectsOwned;
	private Map<Integer,OCMetricTracker> reflectionsReceived;
	private Map<Integer,OCMetricTracker> updatesSent;
	private int objectsDeleted;
	private int objectsRemoved;
	private int objectsRegistered;
	private int objectsDiscovered;
	private int serviceInvocations;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederateMetrics()
	{
		this.interactionsReceived = new HashMap<Integer,InteractionCount>();
		this.interactionsSent = new HashMap<Integer,InteractionCount>();
		this.objectsOwned = new HashSet<Integer>();
		this.updatesSent = new HashMap<Integer,OCMetricTracker>();
		this.reflectionsReceived = new HashMap<Integer,OCMetricTracker>();
		this.objectsDeleted = 0;
		this.objectsRemoved = 0;
		this.objectsRegistered = 0;
		this.objectsDiscovered = 0;
		this.serviceInvocations = 0;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Federate Event Handlers   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void reflectionReceived( int classId, int instanceId )
	{
		OCMetricTracker tracker = this.reflectionsReceived.get( classId );
		if( tracker == null )
		{
			tracker = new OCMetricTracker( classId );
			this.reflectionsReceived.put( classId, tracker );
		}
		
		tracker.increment( instanceId );
	}
	
	public void sentUpdate( int classId, int instanceId )
	{
		OCMetricTracker tracker = this.updatesSent.get( classId );
		if( tracker == null )
		{
			tracker = new OCMetricTracker( classId );
			this.updatesSent.put( classId, tracker );
		}
		
		tracker.increment( instanceId );
	}
	
	public void interactionReceived( int classId )
	{
		InteractionCount tracker = this.interactionsReceived.get( classId );
		if( tracker == null )
		{
			tracker = new InteractionCount( classId, 0 );
			this.interactionsReceived.put( classId, tracker );
		}
		
		tracker.increment();
	}
	
	public void interactionSent( int classId )
	{
		InteractionCount tracker = this.interactionsSent.get( classId );
		if( tracker == null )
		{
			tracker = new InteractionCount( classId, 0 );
			this.interactionsSent.put( classId, tracker );
		}
		
		tracker.increment();
	}

	public void objectRegistered( int instanceId )
	{
		++this.objectsRegistered;
		
		// When a federate registers an object, they hold privilege to delete until such time as they 
		// yield it, or remove it 
		this.objectsOwned.add( instanceId );
	}
	
	public void objectDeleted( int instanceId )
	{
		++this.objectsDeleted;
		
		// Object is no longer owned by the federate 
		this.objectsOwned.remove( instanceId );
	}
	
	public void objectRemoved()
	{
		++this.objectsRemoved;
	}
	
	public void objectDiscovered()
	{
		++this.objectsDiscovered;
	}
	
	public int serviceInvoked()
	{
		return ++this.serviceInvocations;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the number of times the ReflectAttributeValues service has been invoked on the federate,
	 *         grouped by class Id
	 */
	public ObjectClassBasedCount[] getReflectionsReceived()
	{
		ObjectClassBasedCount[] results = new ObjectClassBasedCount[this.reflectionsReceived.size()];
		int index = 0;
		for( OCMetricTracker tracker : this.reflectionsReceived.values() )
			results[index++] = tracker.getTotal();
		
		return results;
	}
	
	public int getTotalReflectionsReceived()
	{
		ObjectClassBasedCount[] reflections = getReflectionsReceived();
		int grandTotal = 0;
		for( ObjectClassBasedCount classCount : reflections )
			grandTotal += classCount.getCount();
		
		return grandTotal;
	}
	
	public ObjectClassBasedCount[] getObjectInstancesReflected()
	{
		ObjectClassBasedCount[] results = new ObjectClassBasedCount[this.reflectionsReceived.size()];
		int index = 0;
		for( OCMetricTracker tracker : this.reflectionsReceived.values() )
			results[index++] = tracker.getTotalUniqueInstances();
		
		return results;
	}
	
	public int getTotalObjectInstancesReflected()
	{
		ObjectClassBasedCount[] reflected = this.getObjectInstancesReflected();
		int grandTotal = 0;
		for( ObjectClassBasedCount classCount : reflected )
			grandTotal += classCount.getCount();
		
		return grandTotal;
	}
	
	/**
	 * @return the number of times the federate has sent an Attribute Update, grouped by class Id
	 */
	public ObjectClassBasedCount[] getUpdatesSent()
	{
		ObjectClassBasedCount[] results = new ObjectClassBasedCount[this.updatesSent.size()];
		int index = 0;
		for( OCMetricTracker tracker : this.updatesSent.values() )
			results[index++] = tracker.getTotal();
		
		return results;
	}
	
	public int getTotalUpdatesSent()
	{
		ObjectClassBasedCount[] updated = this.getUpdatesSent();
		int grandTotal = 0;
		for( ObjectClassBasedCount classCount : updated )
			grandTotal += classCount.getCount();
		
		return grandTotal;
	}
	
	/**
	 * @return the number of times the federate has invoked the UpdateAttributeValues service
	 */
	public ObjectClassBasedCount[] getObjectInstancesUpdated()
	{
		ObjectClassBasedCount[] results = new ObjectClassBasedCount[this.updatesSent.size()];
		int index = 0;
		for( OCMetricTracker tracker : this.updatesSent.values() )
			results[index++] = tracker.getTotalUniqueInstances();
		
		return results;
	}
	
	public int getTotalObjectInstancesUpdated()
	{
		ObjectClassBasedCount[] updated = this.getObjectInstancesUpdated();
		int grandTotal = 0;
		for( ObjectClassBasedCount classCount : updated )
			grandTotal += classCount.getCount();
		
		return grandTotal;
	}
	
	/**
	 * @return the number of times the ReceiveInteraction service has been invoked on the federate for
	 *         each interaction class
	 */
	public InteractionCount[] getInteractionsReceived()
	{
		InteractionCount[] results = new InteractionCount[this.interactionsReceived.size()];
		int index = 0;
		for( InteractionCount classCount : this.interactionsReceived.values() )
			results[index++] = classCount;
		
		return results;
	}
	
	public int getTotalInteractionsReceived()
	{
		InteractionCount[] received = this.getInteractionsReceived();
		int grandTotal = 0;
		for( InteractionCount classCount : received )
			grandTotal += classCount.getCount();
		
		return grandTotal;
	}
	
	/**
	 * @return the number of times the federate has invoked the SendInteractions service for each
	 *         interaction class
	 */
	public InteractionCount[] getInteractionsSent()
	{
		InteractionCount[] results = new InteractionCount[this.interactionsSent.size()];
		int index = 0;
		for( InteractionCount classCount : this.interactionsSent.values() )
			results[index++] = classCount;
		
		return results;
	}
	
	public int getTotalInteractionsSent()
	{
		InteractionCount[] received = this.getInteractionsSent();
		int grandTotal = 0;
		for( InteractionCount classCount : received )
			grandTotal += classCount.getCount();
		
		return grandTotal;
	}
	
	/**
	 * @return the number of Object Instances for which the federate owns the HLAprivilegeToDelete 
	 *         parameter
	 */
	public Set<Integer> getObjectsOwned()
	{
		return new HashSet<>( this.objectsOwned );
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
	private class OCMetricTracker
	{
		private int classHandle;
		private int total;
		private Set<Integer> instances;
		
		public OCMetricTracker( int classHandle )
		{
			this.classHandle = classHandle;
			this.total = 0;
			this.instances = new HashSet<>();
		}
		
		public void increment( int instanceId )
		{
			++total;
			this.instances.add( instanceId );
		}
		
		public ObjectClassBasedCount getTotal()
		{
			return new ObjectClassBasedCount( classHandle, total );
		}
		
		public ObjectClassBasedCount getTotalUniqueInstances()
		{
			return new ObjectClassBasedCount( classHandle, instances.size() );
		}
	}
}
