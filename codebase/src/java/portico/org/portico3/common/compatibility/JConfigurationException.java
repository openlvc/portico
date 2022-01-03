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
package org.portico3.common.compatibility;

public class JConfigurationException extends JRTIinternalError
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

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
	 * @param format A format string for the exception message
	 * @param values The values for the format string
	 */
	public JConfigurationException( String format, Object... values )
	{
		super( String.format(format,values) );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
