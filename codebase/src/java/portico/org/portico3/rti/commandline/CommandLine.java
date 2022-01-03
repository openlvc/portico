/*
 *   Copyright 2022 The Portico Project
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
package org.portico3.rti.commandline;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.portico3.common.compatibility.JConfigurationException;

/**
 * Parses and holds values represented on a Command Line. The values that are supported come from
 * the {@link Argument} enumeration.
 */
public class CommandLine
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String[] commandline;
	private Map<Argument,String> values;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * When constructing a {@link CommandLine}, the given array will be parsed to extract the
	 * {@link Argument}s on it. If an argument is included twice, or there is a problem parsing
	 * the command line (such as a required parameter to an argument not being present) an 
	 * exception will be thrown.
	 * 
	 * @param commandline The string command line to populate ourselves from
	 * @throws JConfigurationException If there is a problem during parsing
	 */
	public CommandLine( String[] commandline ) throws JConfigurationException
	{
		this.commandline = commandline;
		
		// parse the command line
		this.values = new HashMap<>();
		for( int i = 0; i < commandline.length; i++ )
		{
			Argument argument = Argument.getArgument( commandline[i] );
			
			// make sure we don't already have the argument
			if( this.values.containsKey(argument) )
				throw new JConfigurationException( "Argument passed twice: %s", argument.getName() );
			
			// store the argument, optionally with an additional parameter if it is required
			if( argument.requiresParam() == false )
			{
				this.values.put( argument, null );
			}
			else
			{
				// make sure there is a next argument to be had
				if( (i+1) >= commandline.length )
				{
					String format = "%s requires a parameter, but none was found.";
					throw new JConfigurationException( format, argument.getName() );
				}
				
				String nextValue = commandline[++i];
				if( nextValue.startsWith("--") )
				{
					String format = "%s requires a parameter, but we found the next argument [%s]";
					throw new JConfigurationException( format, argument.getName(), nextValue );
				}
				else
				{
					this.values.put( argument, nextValue );
				}
			}
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private final void verifyPresent( Argument argument ) throws JConfigurationException
	{
		if( isPresent(argument) == false )
			throw new JConfigurationException( "Argument not present: "+argument.getName() );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessor and Mutator Methods   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public boolean isPresent( Argument argument )
	{
		return values.containsKey( argument );
	}
	
	/**
	 * For any arguments that have paremters, the methods can be used to get those values.
	 * For arguments that have no parameters, the value returned will be <code>null</code>,
	 * but {@link #isPresent(Argument)} will indicate that the argument was found.
	 * 
	 * @param argument The argument to get the value for
	 * @return The value of the argument as a string
	 */
	public String getValue( Argument argument ) throws JConfigurationException
	{
		verifyPresent( argument );
		return values.get( argument );
	}

	/**
	 * Look up the value for the given argument and try to convert it to an integer and return
	 * 
	 * @param argument The argument to look up 
	 * @return The value of the argument's parameter, as an integer
	 * @throws JConfigurationException If argument isn't present, or cannot be converted
	 */
	public int getValueAsInt( Argument argument ) throws JConfigurationException
	{
		verifyPresent( argument );
		
		try
		{
			return Integer.parseInt( getValue(argument) );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Argument value not an integer: arg=%s, values=%s",
			                                   argument, getValue(argument) );
		}
	}
	
	/**
	 * Look up the value for the given argument and try to convert it to a double and return
	 * 
	 * @param argument The argument to look up 
	 * @return The value of the argument's parameter, as a double
	 * @throws JConfigurationException If argument isn't present, or cannot be converted
	 */
	public double getValueAsDobule( Argument argument ) throws JConfigurationException
	{
		verifyPresent( argument );
		
		try
		{
			return Double.parseDouble( getValue(argument) );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Argument value not a double: arg=%s, values=%s",
			                                   argument, getValue(argument) );
		}
	}
	
	/**
	 * Look up the value for the given argument and try to convert it to a File and return
	 * 
	 * @param argument The argument to look up 
	 * @return The value of the argument's parameter, as a File
	 * @throws JConfigurationException If argument isn't present, or cannot be converted
	 */
	public File getValueAsPath( Argument argument ) throws JConfigurationException
	{
		verifyPresent( argument );
		
		try
		{
			return new File( getValue(argument) );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Argument value not a file: arg=%s, values=%s",
			                                   argument, getValue(argument) );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	public static final String getUsage()
	{
		// find the longest name that we have, so that we can justify on it
		int longestName = 0;
		for( Argument argument : Argument.values() )
		{
			if( argument.getName().length() > longestName )
				longestName = argument.getName().length();
		}
		
		// print the header
		StringBuilder builder = new StringBuilder();
		builder.append( "usage: rti.sh --argument [value] --argument [value]...\n" );
		builder.append( "\n" );
		
		// argument information
		String formatString = "    %-"+(longestName+2)+"s  %10s   %s\n";
		for( Argument argument : Argument.values() )
		{
			builder.append( String.format(formatString,
			                              argument.getName(),
			                              argument.getParamTypeHint(),
			                              argument.getDescription()) );
		}
		
		return builder.toString();
	}
	
	
}
