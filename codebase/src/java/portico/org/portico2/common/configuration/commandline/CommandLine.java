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
package org.portico2.common.configuration.commandline;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;

public class CommandLine
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Argument,String> arguments;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public CommandLine( String... commandline ) throws JConfigurationException
	{
		this.arguments = new HashMap<>();

		for( int i = 0; i < commandline.length; i++ )
		{
			commandline[i] = commandline[i].trim();
			Argument argument = Argument.getArgument( commandline[i] );
			if( argument.requiresParam() )
				arguments.put( argument, commandline[++i] );
			else
				arguments.put( argument, "" );
		}

	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public boolean isEmpty()
	{
		return arguments.isEmpty();
	}
	
	public Set<Argument> keySet()
	{
		return arguments.keySet();
	}
	
	public String get( Argument key )
	{
		return arguments.get( key );
	}
	
	public Map<Argument,String> getArguments()
	{
		return arguments;
	}
	
	public Properties asProperties()
	{
		Properties properties = new Properties();
		for( Argument argument : arguments.keySet() )
			properties.put( argument, arguments.get(argument) );
		
		return properties;
	}

	public boolean hasArgument( String name )
	{
		return arguments.keySet().contains( Argument.getArgument(name) );
	}
	
	public boolean hasArgument( Argument argument )
	{
		return arguments.keySet().contains( argument );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
