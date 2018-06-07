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
package org.portico2.common.network.configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.portico2.common.utils.NetworkUtils;

public class MulticastConfiguration extends ConnectionConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_ADDRESS     = "address";
	public static final String KEY_PORT        = "port";
	public static final String KEY_NIC         = "nic";
	
	
	public static final String DEFAULT_ADDRESS = "239.1.2.3";
	public static final int    DEFAULT_PORT    = 20913;
	public static final String DEFAULT_NIC     = "SITE_LOCAL";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String address;
	private int port;
	private String nic;

	private CryptoConfiguration cryptoConfiguration;
	private AuthConfiguration authConfiguration;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MulticastConfiguration( String name )
	{
		super( name );
		this.address = DEFAULT_ADDRESS;
		this.port    = DEFAULT_PORT;
		this.nic     = DEFAULT_NIC;
		
		this.cryptoConfiguration = new CryptoConfiguration();
		this.authConfiguration = new AuthConfiguration();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Read Only
	 */
	@Override
	public TransportType getTransportType()
	{
		return TransportType.Multicast;
	}

	@Override
	public CryptoConfiguration getCryptoConfiguration()
	{
		return this.cryptoConfiguration;
	}

	@Override
	public AuthConfiguration getAuthConfiguration()
	{
		return this.authConfiguration;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void parseConfiguration( String prefix, Properties properties )
	{
		this.cryptoConfiguration.parseConfiguration( prefix, properties );
		
		prefix += ".";
		String temp = properties.getProperty( prefix+KEY_ADDRESS );
		if( temp != null )
			setAddress( temp );
		
		temp = properties.getProperty( prefix+KEY_PORT );
		if( temp != null )
			setPort( Integer.parseInt(temp) );
		
		temp = properties.getProperty( prefix+KEY_NIC );
		if( temp != null )
			setNic( temp );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the address property as a string. Note that this may be an actual IP address or
	 * or it may be one of the symbolic names: LOOPBACK, LINK_LOCAL, SITE_LOCAL or GLOBAL.
	 */
	public String getAddressString()
	{
		return this.address;
	}

	/**
	 * @return the InetAddress for the multicast group to use
	 */
	public InetAddress getAddress()
	{
		try
		{
			return InetAddress.getByName( address );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * Set the address to use for this connection. This must be a valid multicast address.
	 * 
	 * @param address The address to use 
	 */
	public void setAddress( String address )
	{
		try
		{
			InetAddress temp = InetAddress.getByName( address );
			if( temp.isMulticastAddress() == false )
				throw new IllegalArgumentException( address+" is not a valid multicast address" );
			else
				this.address = address;
		}
		catch( UnknownHostException e )
		{
			throw new IllegalArgumentException( address+" is not a valid multicast address" );
		}
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

	/**
	 * @return String representing the NIC that this connection should be bound to. Note that
	 *         this may be an actual IP address, a NIC name or one of the defined symbolic names:
	 *         LOOPBACK, LINK_LOCAL, SITE_LOCAL or GLOBAL.
	 */
	public String getNicString()
	{
		return this.nic;
	}

	/**
	 * @return The IP address of the NIC that we are using for tranmitting multicast traffic
	 */
	public InetAddress getNicAddress()
	{
		return NetworkUtils.resolveInetAddress( nic );
	}

	/**
	 * Set the NIC that we should be using for multicast communications. This may be the IP address
	 * of the NIC to use, or one of the symbolic names: LOOPBACK, LINK_LOCAL, SITE_LOCAL, GLOBAL.
	 * 
	 * @param string Name/IP of NIC to be using
	 */
	public void setNic( String string )
	{
		if( string.equalsIgnoreCase("LOOPBACK") ||
			string.equalsIgnoreCase("LINK_LOCAL") ||
			string.equalsIgnoreCase("SITE_LOCAL") ||
			string.equalsIgnoreCase("GLOBAL") )
		{
			; // no-op (yet) drop through to final set property
		}
		else
		{
			try
			{
				InetAddress.getByName( string );
			}
			catch( UnknownHostException uhe )
			{
				throw new IllegalArgumentException( "NIC must be set to LOOPBACK, LINK_LOCAL, "+
				                                    "SITE_LOCAL, GLOBAL or a valid IP address" );
			}
		}
		
		this.nic = string;
	}

	@Override
	public String toString()
	{
		return String.format( "[Multicast: address=%s, port=%d, nic=%s]", address, port, nic ); 
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
