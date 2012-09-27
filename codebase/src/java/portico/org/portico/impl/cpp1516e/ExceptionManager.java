/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.cpp1516e;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * When the C++ language wrapper calls into Java and has the RTI do something,
 * this can naturally generate exceptions (standard HLA exceptions). Passing
 * exception information back across the Java/C++ boundary in this situation
 * however is difficult. This class exist to help do that. We have a simple static
 * method that calls directly into a native one. The local method converts and
 * formats information from an exception appropriately, before passing it to the
 * native method. The native method is implemented in C++ and takes the exception
 * information, flagging it inside the C++ binding to be picked up and trigger
 * an appropriate exception there.
 */
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
	 * is appended with the stack trace.
	 * 
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
