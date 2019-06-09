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
package org.portico2.common.messaging;

/**
 * When more than one handler is added to a {@link MessageSink} for a specific type they will be
 * arranged into a chain and passed to each, one by one. Handlers can throw this exception to
 * prevent any further processing on a message. Throwing this exception will also not cause any
 * change to the response object of a message context (whereas throwing a general exception will
 * cause it to be wrapped up in the response of the context).
 */
public class VetoException extends RuntimeException
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
	public VetoException()
	{
		super();
	}

	/**
	 * @param message The message to create the exception with
	 */
	public VetoException( String message )
	{
		super( message );
	}

	/**
	 * @param cause The cause of the exception
	 */
	public VetoException( Throwable cause )
	{
		super( cause );
	}

	/**
	 * @param message The message to create the exception with
	 * @param cause The cause of the exception
	 */
	public VetoException( String message, Throwable cause )
	{
		super( message, cause );
	}

	/**
	 * @param formatString A format string to use for the message
	 * @param objects Arguments to apply to the format string
	 */
	public VetoException( String formatString, Object... objects )
	{
		super( String.format( formatString, objects ) );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
