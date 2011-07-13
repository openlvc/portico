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
package org.portico.utils.messaging;

/**
 * This exception should be thrown when a handlers wants to explicitly end any further processing
 * of a message. The various messaging framework infrastructure components will be on the lookout
 * for this exception type and will not throw it onwards as general failure. See the documentation
 * for the {@link MessageChain} and {@link MessageSink} class to find information on how they
 * handle the veto.
 */
public class VetoException extends MessagingException
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
