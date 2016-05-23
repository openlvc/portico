/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups.wan.global;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class Configuration
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	public enum Argument
	{
		Address( "address",   1, "IP or DNS name to bind to. Also supports symbols: 'LOOPBACK', 'LINK_LOCAL', 'SITE_LOCAL', 'GLOBAL' (default: 127.0.0.1)"),
		Port(       "port",   1, "Port to communicate on (default: 23114)"),
		Metrics( "metrics",   1, "Dump metrics to CSV file as client disconnects (default:false)");
		
		private String name;
		private int valueCount;
		private String description;
		private Argument( String name, int valueCount, String description )
		{
			this.name = name;
			this.valueCount = valueCount;
			this.description = description;
		}
		
		public String getName() { return this.name; }
		public int getValueCount() { return this.valueCount; }
		public String getDescription() { return this.description; }

		public static Argument find( String name )
		{
			for( Argument arg : Argument.values() )
			{
				if( arg.name.equals(name) )
					return arg;
			}
			
			return null;
		}
		
		public static String toHelpString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append( "Usage: ./wanrouter [arguments]\n" );
			builder.append( "" );
			for( Argument argument: values() )
				builder.append( String.format("%14s %s\n","--"+argument.name,argument.description) );
			
			builder.append( "" );
			return builder.toString();
		}
	}

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private InetAddress address;
	private int port;
	private boolean recordMetrics = false;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Configuration()
	{
		// set defaults
		this.setAddress( "127.0.0.1" );
		this.port = 23114;
		this.recordMetrics = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public InetAddress getAddress()
	{
		return this.address;
	}
	
	public void setAddress( String address )
	{
		//
		// check for the supported symbolic names
		//
		if( address.equalsIgnoreCase("LOOPBACK") )
		{
			this.address = getNicAddress( "LOOPBACK" );
			return;
		}
		else if( address.equalsIgnoreCase("LINK_LOCAL") )
		{
			this.address = getNicAddress( "LINK_LOCAL" );
			return;
		}
		else if( address.equalsIgnoreCase("SITE_LOCAL") )
		{
			this.address = getNicAddress( "SITE_LOCAL" );
			return;
		}
		else if( address.equalsIgnoreCase("GLOBAL") )
		{
			this.address = getNicAddress( "GLOBAL" );
			return;
		}

		//
		// check the name directly
		//
		try
		{
			this.address = InetAddress.getByName( address );
		}
		catch( Exception e )
		{
			throw new RuntimeException( e.getMessage(), e );
		}
	}
	
	public int getPort() { return this.port; }
	public void setPort( int port ) { this.port = port; }

	public boolean recordMetrics() { return this.recordMetrics; }
	public void setRecordMetrics( boolean record ) { this.recordMetrics = record; }

	//
	// Util Methods
	//
	/**
	 * Find the first InetAddress (IPv4 only) for the given type where type is either:
	 *  - `LOOPBACK`
	 *  - `LINK_LOCAL`
	 *  - `SITE_LOCAL`
	 *  - `GLOBAL`
	 * 
	 * If none is found, throw an exception
	 */
	private InetAddress getNicAddress( String type ) throws RuntimeException
	{
		Set<InterfaceAddress> addresses = new HashSet<InterfaceAddress>();
		try
		{
			Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
			while( nics.hasMoreElements() )
			{
				NetworkInterface nic = nics.nextElement();
				for( InterfaceAddress temp : nic.getInterfaceAddresses() )
				{
					InetAddress address = temp.getAddress();

					// skip ipv6
					if( address instanceof Inet6Address )
						continue;

					// check it against the desired type
					if( address.isLinkLocalAddress() )
					{
						if( type.equals("LINK_LOCAL") )
							return temp.getAddress();
					}
					else if( address.isSiteLocalAddress() )
					{
						if( type.equals("SITE_LOCAL") )
							return temp.getAddress();
					}
					else if( address.isLoopbackAddress() )
					{
						if( type.equals("LOOPBACK") )
							return temp.getAddress();
					}
					else
					{
						// not link-local, site-local or loopback - must be global!
						if( type.equals("GLOBAL") )
							return temp.getAddress();
					}
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		// if we get here we never found a match
		throw new RuntimeException( "Could not find an address for "+type );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static Configuration parse( String[] args ) throws Exception
	{
		Configuration configuration = new Configuration();

		for( int i = 0; i < args.length; i++ )
		{
			String potential = args[i];
			if( potential.startsWith("--") == false )
				continue;
			
			String name = potential.substring( 2 );
			Argument argument = Argument.find( name );
			if( argument == null )
				throw new Exception( "Unknown argument: "+potential );
			
			switch( argument )
			{
				case Address:
					configuration.setAddress( args[i+1] );
					break;
				case Port:
					configuration.setPort( Integer.parseInt(args[i+1]) );
					break;
				case Metrics:
					configuration.setRecordMetrics( Boolean.valueOf(args[i+1]) );
			}
			
			i += argument.valueCount;
		}
		
		return configuration;
	}

	/**
	 * Returns a string listing all the available IP Addresses you can connect the router up
	 * to for this computer. Intended to be used for printing user help.
	 */
	public static String getAvailableAddresses()
	{
		try
		{
			HashSet<String> strings = new HashSet<String>();
    		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    		while( interfaces.hasMoreElements() )
    		{
    			NetworkInterface nic = interfaces.nextElement();
    			for( InterfaceAddress address : nic.getInterfaceAddresses() )
    			{
    				strings.add( address.getAddress().toString() );
    			}
    		}

    		return "Available Addresses: "+strings;
		}
		catch( Exception e )
		{
			e.printStackTrace();
			return "Available Addresses: Error - "+e.getCause().getClass().getSimpleName();
		}
	}
}
