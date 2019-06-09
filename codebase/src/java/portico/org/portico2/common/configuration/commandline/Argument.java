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

import org.portico.lrc.compat.JConfigurationException;

public enum Argument
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	// General Configuration
	Help       ( "help",        "",          "",         "Show this help" ),
	GUI        ( "gui",         "",          "",         "Show the Server Control GUI (off by default)" ),
	RidFile    ( "rid",         "rid.file",  "[file]",   "Path to RTI Initialization File (default: ./RTI.rid)"),
	RtiHome    ( "rtihome",     "rit.home",  "[file]",   "Path to RTI_HOME diretory (default: ./" ),
	
	// Logging Configuration
	LogLevel   ( "log-level",   "log.level", "[string]", "Threshold for logging. OFF for none. (default: INFO)" ),
	LogFile    ( "log-dir",     "log.dir",   "[dir]",    "The directory to put log files in" );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;             // token to look for
	private String propertyName;     // name of the equivalent property when loading from properties
	private String typeDescription;  // description of expected arguments
	private String textDescription;  // documentation on purpose of the setting

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Argument( String name,
	                  String property,
	                  String typeDesc,
	                  String textDesc )
	{
		this.name = name;
		this.typeDescription = typeDesc;
		this.textDescription = textDesc;
	}
	                  
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * @return The name of this command line argument without the "--"
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * @return A brief description of the type we expect as an argument to the command line option.
	 *         Empty string if none is expected.
	 */
	public String getTypeDescription()
	{
		return this.typeDescription;
	}
	
	/**
	 * @return A human readable description of the argument to print when --help is invokved.
	 */
	public String getTextDescription()
	{
		return this.textDescription;
	}

	/** Returns true if this argument expects a parameter. We tell this by looking at the
	    parameter type description. If it's null or empty, no parameter is expected, otherwise,
	    one parameter is expected */
	public boolean requiresParam()
	{
		if( this.typeDescription == null || this.typeDescription.trim().equals("") )
			return false;
		else
			return true;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/** Argument must come in the form "--argumentName" */
	public static Argument getArgument( String argument ) throws JConfigurationException
	{
		if( argument.startsWith("--") == false )
			throw new JConfigurationException( "Arguments must have the form --name. Found [%s].", argument );
		
		argument = argument.substring(2).trim();
		for( Argument potential : Argument.values() )
		{
			if( potential.name.equals(argument) )
				return potential;
		}
		
		throw new JConfigurationException( "Unknown argument: --"+argument );
	}

	/**
	 * Get the argument that is represented by the specified property. Each argument may
	 * have an associated property name, and that is what we look up against.
	 * 
	 * @param propertyName The property to get the related argument for
	 * @return The argument related to property
	 */
	public static Argument getArgumentForProperty( String propertyName )
		throws JConfigurationException
	{
		if( propertyName == null || propertyName.trim().equals("") )
			return null;
		
		for( Argument potential : Argument.values() )
		{
			if( propertyName.equalsIgnoreCase(potential.propertyName) )
				return potential;
		}
		
		throw new JConfigurationException( "Unknown argument (property): "+propertyName );
	}
	
	/**
	 * Returns a formatted string that can be printed to the command line outlining the various
	 * arguments available and their use.
	 */
	public static String getCommandLineHelp()
	{
		int longestName = 0;
		for( Argument argument : Argument.values() )
		{
			if( (argument.textDescription != null) && (argument.name.length() > longestName) )
				longestName = argument.name.length();
		}

		// header
		StringBuilder builder = new StringBuilder();
		builder.append( "usage: rtiexec.sh [--arg <values...>]\n" );
		builder.append( "\n" );
		
		// argument information
		String formatString = "    %-"+(longestName+2)+"s  %10s   %s\n";
		for( Argument argument : Argument.values() )
		{
			if( argument.textDescription == null )
				continue;

			String string = String.format( formatString,
			                               "--"+argument.name,
			                               argument.typeDescription,
			                               argument.textDescription );
			builder.append( string );
		}

		return builder.toString();
	}
	
}
