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
package org.portico2.common.network.configuration.transport;

import java.net.InetAddress;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.RID;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.transport.TransportType;
import org.portico2.common.utils.NetworkUtils;
import org.w3c.dom.Element;

public class TcpConfiguration extends TransportConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_ADDRESS     = "address";
	public static final String KEY_PORT        = "port";

	public static final String DEFAULT_ADDRESS = "SITE_LOCAL";
	public static final int    DEFAULT_PORT    = 52295;
	
	// Bundling Properties
	public static final String KEY_BUNDLING_ENABLED  = "bundling";
	public static final String KEY_BUNDLING_MAX_SIZE = "bundling.maxSize";
	public static final String KEY_BUNDLING_MAX_TIME = "bundling.maxTime";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private TransportType type;
	private String address;
	private int port;
	
	private boolean recordMetrics;

	// Bundling
	private boolean isBundling;
	private int bundlingMaxSize;
	private int bundlingMaxTime;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new TcpConfiguration, which can be of type either {@link TransportType#TcpClient}
	 * or {@link TransportType#TcpServer}.
	 * 
	 * @param connectionConfiguration The configuration for the connection we are the transport for
	 * @param transportType The specific transport type we're creating
	 */
	public TcpConfiguration( ConnectionConfiguration connectionConfiguration, TransportType transportType )
	{
		super( connectionConfiguration );
		if( transportType != TransportType.TcpClient && transportType != TransportType.TcpServer )
			throw new JConfigurationException( "TCP Configuration must have type of either tcp-client or tcp-server" );
		
		this.type    = transportType;
		this.address = DEFAULT_ADDRESS;
		this.port    = DEFAULT_PORT;
		
		this.recordMetrics = true;
		
		// Bundling
		this.isBundling = false;
		this.bundlingMaxSize = 64000; // 64k
		this.bundlingMaxTime = 20000; // 20ms
		
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Can be {@link TransportType#TcpClient} or {@link TransportType#TcpServer}.
	 */
	@Override
	public TransportType getTransportType()
	{
		return this.type;
	}

	@Override
	public String toString()
	{
		return String.format( "[TCP: name=%s, enabled=%s, transport=%s, address=%s, port=%d]",
		                      super.name, super.enabled, type, address, port );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void parseConfiguration( RID rid, Element element )
	{
		///////////////////////////////////
		// Transport-Specific Properties //
		///////////////////////////////////
		// get the standard properties
		if( element.hasAttribute("address") )
			this.setAddress( element.getAttribute("address") );
		
		if( element.hasAttribute("port") )
			this.setPort( Integer.parseInt(element.getAttribute("port")) );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void setTransportType( TransportType type )
	{
		if( type == TransportType.TcpServer || type == TransportType.TcpClient )
			this.type = type;
		else
			throw new JConfigurationException( "TCP connections must have the type 'tcp-server' or 'tcp-client': "+type );
	}
	
	public String getAddressString()
	{
		return this.address;
	}
	
	public InetAddress getAddress()
	{
		return NetworkUtils.resolveInetAddress( this.address );
	}
	
	/**
	 * Set the address to connect to or listen on. This can be an IP address or a hostname.
	 * 
	 * @param address The address or hostname to use for connection or listening.
	 */
	public void setAddress( String address )
	{
		// make sure we can resolve the address first
		NetworkUtils.resolveInetAddress( address );
		this.address = address;
	}
	
	public int getPort()
	{
		return this.port;
	}
	
	public void setPort( int port )
	{
		if( port > 65536 )
			throw new IllegalArgumentException( "Port must be in range 0-65536" );
		else
			this.port = port;
	}

	public boolean isRecordMetrics()
	{
		return this.recordMetrics;
	}
	
	public void setRecordMetrics( boolean record )
	{
		this.recordMetrics = record;
	}


	////////////////////////////////////////////////////////////////////////////////////////////
	/// Bundling Settings   ////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/** Is bundling turned on? Default to false */
	public boolean isBundlingEnabled()
	{
		return this.isBundling;
	}
	
	/**
	 * Return the maximum size a bundle should grow to (bytes) before it is flushed.
	 * Default is 64K.
	 */
	public int getBundleMaxSize()
	{
		return this.bundlingMaxSize;
	}

	/**
	 * Sets the max size the bundler buffer can get to before it flushes. Works in tandem with
	 * {@link #setBundleMaxTime(int)}.
	 * 
	 * @param value The size as a stirng. Can be a straight value in bytes, or can be an abbreviation
	 *              such as "16b, 16k, 16m, ...
	 */
	public void setBundleMaxSize( String value )
	{
		if( value == null )
			throw new IllegalArgumentException( "Cannot pass null to setBundleMaxSize()" );

		value = value.trim().toLowerCase();

		try
		{
			if( value.endsWith("k") )
			{
				int size = Integer.parseInt( value.substring(0,value.length()-1) );
				this.bundlingMaxSize = size*1000;
			}
			else if( value.endsWith("m") )
			{
				int size = Integer.parseInt( value.substring(0,value.length()-1) );
				this.bundlingMaxSize = size*1000*1000;
			}
			else if( value.endsWith("g") )
			{
				int size = Integer.parseInt( value.substring(0,value.length()-1) );
				throw new JConfigurationException( "A max bundle size of " + size +
				                                   "GB? Go home. You're drunk." );
			}
			else if( value.endsWith("b") )
			{
				int size = Integer.parseInt( value.substring(0,value.length()-1) );
				this.bundlingMaxSize = size;
			}
			else
			{
				this.bundlingMaxSize = Integer.parseInt( value );
			}
		}
		catch( NumberFormatException e )
		{
			throw new JConfigurationException( "Could not parse max bundle size: "+value );
		}
	}
	
	/**
	 * Return the maximum amount of time (millis) the bundler should hold a message for
	 * before flushing, regardless of bundled size.
	 * Default: 20ms
	 */
	public int getBundleMaxTime()
	{
		return this.bundlingMaxTime;
	}
	
	public void setBundleMaxTime( int millis )
	{
		this.bundlingMaxTime = millis;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
