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
package org.portico2.common.configuration;

import java.util.concurrent.TimeUnit;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.utils.XmlUtils;
import org.w3c.dom.Element;

public class LrcConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ConnectionConfiguration connectionConfiguration;
	
	// Tick Processing
	// Timeout to wait when calling tick() is no callbacks to process. Stores in ns.
	private long tickTimeout;
	
	// Number of messages in queue before the LRC starts issuing warnings about not ticking enough.
	private int queueWarningCountSize;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected LrcConfiguration()
	{
		this.connectionConfiguration = null; // set in parseProperties()
		this.tickTimeout = TimeUnit.MILLISECONDS.toNanos( 5 );
		this.queueWarningCountSize = 500;
		
		// defaults
		this.connectionConfiguration = new ConnectionConfiguration( "lrc" );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionConfiguration getConnectionConfiguration()
	{
		return this.connectionConfiguration;
	}

	public void setConnectionConfiguration( ConnectionConfiguration connectionConfiguration )
	{
		this.connectionConfiguration = connectionConfiguration;
	}

	/**
	 * @return Nanos the LRC should wait for messages if there are none ready when the
	 *         tick() methods are called. 
	 */
	public long getTickTimeoutNanos()
	{
		return this.tickTimeout;
	}

	/**
	 * Set the number of millis that the LRC should wait for messages if there are none
	 * to process when federate code calls the tick() method.
	 * 
	 * @param tickTimeout Timeout period in millis
	 */
	public void setTickTimeoutMs( long tickTimeout )
	{
		this.tickTimeout = TimeUnit.MILLISECONDS.toNanos( tickTimeout );
	}

	/**
	 * Set the number of nanos that the LRC should wait for messages if there are none
	 * to process when federate code calls the tick() method.
	 * 
	 * @param tickTimeout Timeout period in nanos
	 */
	public void setTickTimeoutNanos( long tickTimeout )
	{
		this.tickTimeout = tickTimeout;
	}

	/**
	 * @return The maximum size the queue can get to before we start emitting warnings about
	 *         starvation and not ticking too much. Default: 500.
	 */
	public int getQueueSizeWarningLimit()
	{
		return this.queueWarningCountSize;
	}

	/**
	 * Set the max number of messages allowed in the queue before we start issuing starvation
	 * log message and asking someone to just call tick() dammit!
	 * 
	 * @param queueSize Max queue size before warnings are issues
	 */
	public void setQueueSizeWarningLimit( int queueSize )
	{
		this.queueWarningCountSize = queueSize;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Parsing Methods   ////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected void parseConfiguration( RID rid, Element lrcElement ) throws JConfigurationException
	{
		// Fetch the Network Properties
		Element networkElement = XmlUtils.getChild( lrcElement, "network", true );
		Element connectionElement = XmlUtils.getChild( networkElement, "connection", true );
		
		this.connectionConfiguration.parseConfiguration( rid, connectionElement );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
