/*
 *   Copyright 2006 The Portico Project
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


/**
 * <b>This is the Portico version of RTIexception</b> (and should be treated as such). Sadly, the
 * DLC specified exception don't include any way to set a root cause (something I consider
 * a cardinal sin - when dealing with errors you need as much information as possible!).This class
 * includes a method to get an RTIinternalError instance from it to save anyone doing any
 * conversion between it and RTI problems.
 */
public class JException extends RuntimeException
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
	public JException()
	{
		super();
	}

	/**
	 * @param message The message to create the exception with
	 */
	public JException( String message )
	{
		super( message );
	}

	/**
	 * @param cause The cause of the exception
	 */
	public JException( Throwable cause )
	{
		super( cause );
	}

	/**
	 * @param message The message to create the exception with
	 * @param cause The cause of the exception
	 */
	public JException( String message, Throwable cause )
	{
		super( message, cause );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public JRTIinternalError asRTIError()
	{
		return new JRTIinternalError( this.getMessage() );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
