/*
 *   Copyright 2007 The Portico Project
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
package org.portico.impl.cpp13;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionManager
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * The same as the native version of push exception, except that the message
	 * is appended with the stack trace
	 * @param rtiid The id of the C++ JavaRTI this is associated with
	 * @param exception The exception to be pushed.
	 */
	public static void pushException( int rtiid, Throwable exception )
	{
		// get a string representation of the stack trace
		StringWriter writer = new StringWriter();
		exception.printStackTrace( new PrintWriter(writer) );

		// push the exception to c++
		pushException( rtiid, exception.getClass().getSimpleName(), writer.toString() );
	}

	/**
	 * Native method that takes an exception that occurred in the java-side of the interface
	 */
	public static native void pushException( int rtiid, String name, String reason );
}
