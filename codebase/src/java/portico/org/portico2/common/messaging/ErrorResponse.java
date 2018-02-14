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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents an error response to a {@link PorticoMessage}. Unlike the general
 * {@link ResponseMessage} it can contain additional error information in the form of a
 * {@link Throwable} object. If there is no explicit exception, an {@link ErrorResponse} can
 * be constructed with a basic error string (that is wrapped in an {@link Exception} internally.
 */
public class ErrorResponse extends ResponseMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Throwable cause;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ErrorResponse()
	{
		super( true );
	}

	public ErrorResponse( String errorMessage )
	{
		super( true );
		this.cause = new Exception( errorMessage );
	}
	
	public ErrorResponse( Throwable cause )
	{
		super( true );
		this.cause = cause;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public Throwable getCause()
	{
		return this.cause;
	}

	public void setCause( Throwable cause )
	{
		this.cause = cause;
	}

	public String toString()
	{
		if( cause == null )
		{
			return "ExceptionMessage has no cause";
		}
		else
		{
			StringBuilder builder = new StringBuilder( "(" );
			builder.append( cause.getClass() );
			builder.append( ") " );
			builder.append( cause.getMessage() );
			return builder.toString();
		}
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		this.isError = true;
		cause = (Throwable)input.readObject();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		output.writeObject( cause );
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
