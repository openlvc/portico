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

import java.net.InetAddress;

public class Configuration
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	public enum Argument
	{
		Address( "address",   1, "IP Address of NIC to communicate on (default: 127.0.0.1)"),
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
				builder.append( String.format("%14s %s\n",argument.name,argument.description) );
			
			builder.append("");
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
}
