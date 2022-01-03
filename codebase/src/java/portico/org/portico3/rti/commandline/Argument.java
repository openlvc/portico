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

import org.portico3.common.compatibility.JConfigurationException;

/**
 * Represents a command line argument and encapsulates any important documentation about it
 */
public enum Argument
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	RidFile( "--rid-file", "[file]", "Path to the RID config file to load" ),
	LogLevel( "--log-level", "[string]", "Set the log level used by the RTI" ),
	LogDir( "--log-dir", "[path]", "Set the directory to put log files into" ),
	SysInfo( "--sys-info", "", "Print information about the system" );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String argumentName;
	private String paramTypeHint;
	private String argumentDescription;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Argument( String argumentName, String paramTypeHint, String argumentDescription )
	{
		this.argumentName = argumentName;
		this.paramTypeHint = paramTypeHint;
		this.argumentDescription = argumentDescription;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public boolean requiresParam()
	{
		return paramTypeHint != null;
	}

	public String getName()
	{
		return this.argumentName;
	}
	
	public String getParamTypeHint()
	{
		return paramTypeHint == null ? "" : paramTypeHint;
	}
	
	public String getDescription()
	{
		return this.argumentDescription;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Get the {@link Argument} that corresponds to the given parameter. If we can't match one,
	 * then throw an exception.
	 * 
	 * @param argument The command line argument we want to match to an {@link Argument}. The
	 *                 given name must match the value of {@link Argument#getArgumentName()}.
	 * @return The {@link Argument} matched to the given name 
	 * @throws JConfigurationException If there is no argument with the given name
	 */
	protected static Argument getArgument( String argument ) throws JConfigurationException
	{
		for( Argument potential : Argument.values() )
		{
			if( potential.argumentName.equalsIgnoreCase(argument) )
				return potential;
		}
		
		throw new JConfigurationException( "Unknown argument: "+argument );
	}
	
}
