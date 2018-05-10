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

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.PorticoConstants;

public class LrcConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_LRC_CONNECTION       = "lrc.network.connection";
	
	/** Timeout to wait when calling tick() methods if there are no callbacks to process (ms) */
	public static final String KEY_LRC_TT               = "lrc.callback.tickTimeout"; // in milliseconds
	
	/** Number of messages the queue can hold before the LRC starts issuing warnings about
    not ticking enough, default: 500 */
	public static final String KEY_LRC_QUEUE_WARN_COUNT = "lrc.callback.warningCount"; // FIXME Review name

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ConnectionConfiguration lrcConnection;
	
	// Tick Processing
	private long tickTimeout; // stored in nanos
	private int queueWarningCountSize;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LrcConfiguration()
	{
		this.lrcConnection = null; // set in parseProperties()
		this.tickTimeout   = TimeUnit.MILLISECONDS.toNanos( 5 );
		this.queueWarningCountSize = 500;
		
		// defaults
		this.lrcConnection = new MulticastConnectionConfiguration( "multicast" );
		//TcpConnectionConfiguration tcc = new TcpConnectionConfiguration( "tcp" );
		//tcc.setAddress( "SITE_LOCAL" );
		//this.lrcConnection = tcc;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Pull the configuration information we need from the given property set.
	 */
	protected void parseProperties( Properties properties ) throws JConfigurationException
	{
		//
		// Connection Configuration
		//
		if( PorticoConstants.OVERRIDE_CONNECTION != null )
		{
			String override = PorticoConstants.OVERRIDE_CONNECTION;
			this.lrcConnection = ConnectionType.fromString(override).newConfig( "hlaunit" );
		}
		else if( properties.containsKey(KEY_LRC_CONNECTION) )
		{
			String connectionName = properties.getProperty( KEY_LRC_CONNECTION );
			String connectionType = properties.getProperty( "lrc.network."+connectionName+".type" );
			ConnectionType type = ConnectionType.fromString( connectionType );
			this.lrcConnection = type.newConfig( "lrc" );
			this.lrcConnection.parseConfiguration( "lrc.network."+connectionName, properties );
		}

		//
		// Misc Settings
		//
		String property = properties.getProperty( KEY_LRC_TT, "5" );
		this.setTickTimeoutMs( Long.parseLong(property) );
		
		property = properties.getProperty( KEY_LRC_QUEUE_WARN_COUNT, "500" );
		this.setQueueSizeWarningLimit( Integer.parseInt(property) );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessors and Mutators   ///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionConfiguration getConnectionConfiguration()
	{
		return this.lrcConnection;
	}

	public void setConnectionConfiguration( ConnectionConfiguration lrcConnection )
	{
		this.lrcConnection = lrcConnection;
	}
	
	public long getTickTimeoutNanos()
	{
		return this.tickTimeout;
	}
	
	public void setTickTimeoutMs( long tickTimeout )
	{
		this.tickTimeout = TimeUnit.MILLISECONDS.toNanos( tickTimeout );
	}
	
	public void setTickTimeoutNanos( long tickTimeout )
	{
		this.tickTimeout = tickTimeout;
	}
	
	public int getQueueSizeWarningLimit()
	{
		return this.queueWarningCountSize;
	}
	
	public void setQueueSizeWarningLimit( int queueSize )
	{
		this.queueWarningCountSize = queueSize;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
