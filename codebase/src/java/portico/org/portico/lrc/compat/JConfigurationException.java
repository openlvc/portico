/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.compat;

public class JConfigurationException extends RuntimeException
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Just create an empty exception
	 */
	public JConfigurationException()
	{
		super();
	}

	/**
	 * @param message The message to create the exception with
	 */
	public JConfigurationException( String message )
	{
		super( message );
	}

	/**
	 * @param cause The cause of the exception
	 */
	public JConfigurationException( Throwable cause )
	{
		super( cause );
	}

	/**
	 * @param message The message to create the exception with
	 * @param cause The cause of the exception
	 */
	public JConfigurationException( String message, Throwable cause )
	{
		super( message, cause );
	}

	/**
	 * @param formatString A format string to use for the message via String.format()
	 * @param objects Arguments to apply to the format string
	 */
	public JConfigurationException( String formatString, Object... objects )
	{
		super( String.format(formatString,objects) );
	}

	/**
	 * @param cause The cause of the exception
	 * @param formatString A format string to use for the message via String.format()
	 * @param objects Arguments to apply to the format string
	 */
	public JConfigurationException( Throwable cause, String formatString, Object...objects )
	{
		super( String.format(formatString,objects), cause );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
