/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.portico.bindings.ptalk.channel.Channel;
import org.portico.bindings.ptalk.channel.Packet;
import org.portico.bindings.ptalk.channel.Pipeline;
import org.portico.lrc.compat.JConfigurationException;

/**
 * This class provides method that will be helpful to people implementing {@link IProtocol}s.
 */
public abstract class AbstractProtocol implements IProtocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final String HEADER = "portico.ptalk.protocol.";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected Channel channel = null;
	protected Pipeline pipeline = null;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Handle a message that is being prepared to be sent to the network. Headers can be added, 
	 * content transformations made, etc...
	 */
	public abstract boolean outgoing( Packet packet ) throws RuntimeException;
	
	/**
	 * Handle a message that has been received from the network. Headers can be read so that the
	 * appropriate actions are taken. Transformations can be made on the content, etc...
	 */
	public abstract boolean incoming( Packet packet ) throws RuntimeException;
	
	public void configure( Channel channel, Map<String,Object> properties )
		throws JConfigurationException
	{
		this.channel = channel;
		this.pipeline = channel.getPipeline();
	}
	
	/**
	 * Consults the {@link Protocol} annotation for the name of the protocol and returns it. If
	 * the annotation doesn't appear on the class, "&lt;unknown&gt;" is returned.
	 */
	public String getName()
	{
		Protocol annotation = this.getClass().getAnnotation( Protocol.class );
		if( annotation == null )
			return "<unknown>";
		else
			return annotation.name();
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Protocol Configuration Helpers ////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Looks through all the system properties and checks for any that begin with the string:
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME</code> and returns them in a map. The keys 
	 * for the map will be the names of the properties WITHOUT the portico.ptalk.protocol portion.
	 */
	protected Map<String,String> getProtocolProperties()
	{
        String protocolName = getName();
		String base = HEADER + protocolName;
		Properties properties = System.getProperties();
		Map<String,String> filtered = new HashMap<String,String>();
		for( Object key : properties.keySet() )
		{
			String desired = base + ".";
			if( key.toString().startsWith( desired ) )
			{
				String reducedKey = (String)key;
				reducedKey = reducedKey.substring( 24 + protocolName.length() );
				filtered.put( reducedKey, (String)properties.get( key ) );
			}
		}

		return filtered;
	}

	/**
	 * Looks through all the system properties for one that begins with the string:
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.[param]</code> and if found, returns the value
	 * as a string. If the property is not found, then the provided default value will be returned.
	 * For example, you could call this method as <code>getProperty("timeout")</code> and the
	 * method would look for a property with the name
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.timeout</code>.
	 */
	protected String getProperty( String name, String defaultValue )
	{
		String protocolName = getName();
		return System.getProperty( HEADER+protocolName+"."+name, defaultValue );
	}

	/**
	 * Looks through all the system properties for one that begins with the string:
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.[param]</code> and if found, returns the value
	 * as an int. If the property is not found, then the provided default value will be returned.
	 * For example, you could call this method as <code>getProperty("timeout")</code> and the
	 * method would look for a property with the name
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.timeout</code>.
	 */
	protected int getIntProperty( String name, int defaultValue )
	{
		String stringValue = getProperty( name, ""+defaultValue );
		return Integer.parseInt( stringValue );
	}
	
	/**
	 * Looks through all the system properties for one that begins with the string:
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.[param]</code> and if found, returns the value
	 * as an int[]. If the property is not found, then the provided default value will be returned.
	 * For example, you could call this method as <code>getProperty("timeout")</code> and the
	 * method would look for a property with the name
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.timeout</code>.
	 * <p/>
	 * The expected format of the value for the property is "1,2,3,4,..x". This would break down
	 * into an array with the values 1, 2, 3, 4, ..
	 */
	protected int[] getIntListProperty( String name, int[] defaultValue )
	{
		String stringValue = getProperty( name, null );
		if( stringValue == null )
			return defaultValue;
		
		// break the string down
		StringTokenizer tokenizer = new StringTokenizer( stringValue, "," );
		int[] array = new int[tokenizer.countTokens()];
		int counter = 0;
		while( tokenizer.hasMoreTokens() )
		{
			array[counter] = Integer.parseInt(tokenizer.nextToken().trim());
			counter++;
		}

		return array;
	}
	
	/**
	 * Looks through all the system properties for one that begins with the string:
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.[param]</code> and if found, returns the value
	 * as a boolean. If the property is not found, then the provided default value will be returned.
	 * For example, you could call this method as <code>getProperty("timeout")</code> and the
	 * method would look for a property with the name
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.timeout</code>.
	 */
	protected boolean getBooleanProperty( String name, boolean defaultValue )
	{
		String stringValue = getProperty( name, ""+defaultValue );
		return Boolean.valueOf( stringValue );
	}
	
	/**
	 * Looks through all the system properties for one that begins with the string:
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.[param]</code> and if found, returns the value
	 * as a double. If the property is not found, then the provided default value will be returned.
	 * For example, you could call this method as <code>getProperty("timeout")</code> and the
	 * method would look for a property with the name
	 * <code>portico.ptalk.protocol.PROTOCOL_NAME.timeout</code>.
	 */
	protected double getDoubleProperty( String name, double defaultValue )
	{
		String stringValue = getProperty( name, ""+defaultValue );
		return Double.parseDouble( stringValue );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
