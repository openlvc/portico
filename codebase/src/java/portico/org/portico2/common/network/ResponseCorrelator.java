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
package org.portico2.common.network;


import java.util.Hashtable;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * When you want to represent synchronous semantics over asynchronous messaging, you need
 * some way to correlate requests with responses. This class provides a simple way to manage
 * that process.
 * <p/>
 * 
 * This class handles two primary functions of request/response correlation:
 * 
 * <ul>
 *   <li> Registering of a request for which a response is sought </li>
 *   <li> Allowing a sender to wait for a response to turn up (potentially timing out) </li>
 *   <li> Correlating incoming messages with their associated requests (and requestors) </li>
 * </ul>
 *   
 * To use it, a sender first registers their request via {@link #register()} and is given an ID
 * that can be used to fetch a request later on.
 * <p/>
 * 
 * The sender then sends their message via whatever medium they want. They can then call
 * {@link #waitFor(int)}, passing in the ID of the request. This method will block until
 * either a response with that ID has been provided, or a timeout period has been reached.
 * There is an overload you can specify a precise timeout with, but the default timeout
 * can be set on the correlator (in milliseconds) via {@link #setTimeout(long)}.
 * <p/>
 * 
 * On the input side, the medium receiving messages can offer them to the correlator via the
 * {@link #offer(int, Object)} method. The correlator will take and store the reference against
 * the ID if there is an outstanding request. It will then notify anybody waiting on the ID
 * that a response has arrived.
 * <p/>
 * 
 * Once someone has consumed the ID, it is available for use again, although the specific manner
 * in which IDs are generated is not specified and should not be assumed.
 * <p/>
 * 
 * This class is <b>thread-safe</b>.
 */
public class ResponseCorrelator<T>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final int MAX_REQUEST_ID = 65535;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private AtomicInteger idGenerator;
	private long timeout;
	
	private Map<Integer,Holder> responseMap;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ResponseCorrelator()
	{
		this.idGenerator = new AtomicInteger(0);
		this.timeout = 2000;
		this.responseMap = new Hashtable<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Register a new request that will require correlation. This method will return
	 * the unique ID that should be used for the request. After this call, the correlator
	 * will keep an eye out for any incoming responses with this ID.
	 * 
	 * @return The unique ID to send with the request
	 */
	public int register()
	{
		int id = idGenerator.incrementAndGet();
		if( id > MAX_REQUEST_ID )
		{
			id = 0;
			idGenerator.set( 0 );
		}

		responseMap.put( id, new Holder() );
		return id;
	}
	
	public T waitFor( int id )
	{
		return waitFor( id, timeout );
	}
	
	public T waitFor( int id, long timeout )
	{
		// Make sure we know about the request
		if( responseMap.containsKey(id) == false )
			throw new IllegalArgumentException( "ID not registered with response correlator: "+id );
		
		// Wait for a response to turn up
		Holder holder = responseMap.get( id );
		if( holder.isPresent() == false )
		{
			try
			{
				synchronized( holder )
				{
					holder.wait( timeout );
				}
			}
			catch( InterruptedException ie )
			{
				// We have been demanded to exit!
			}
		}
		
		// We either have a response or not.
		// Remove the request from the map and either return the response or null
		responseMap.remove( id );
		return holder.get();
	}
	
	
	public void offer( int id, T response )
	{
		// check to see if we are waiting for this request
		if( responseMap.containsKey(id) == false )
			return;
		
		// get the placeholder and notify anyone waiting on it
		Holder holder = responseMap.get( id );
		synchronized( holder )
		{
    		holder.set( response );
    		holder.notifyAll();
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void setTimeout( long millis )
	{
		if( millis > 0 )
			this.timeout = millis;
	}
	
	public boolean isRegistered( int id )
	{
		return responseMap.containsKey(id);
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static final int getUnregisteredRandomID()
	{
		return new Random().nextInt( MAX_REQUEST_ID );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Private Inner Class: Holder   /////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private class Holder
	{
		private T value;
		
		public boolean isPresent()
		{
			return value != null;
		}
		
		public T get()
		{
			return this.value;
		}
		
		public void set( T value )
		{
			this.value = value;
		}
	}

}
