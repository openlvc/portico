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
package org.portico.console.shared.comms;

/**
 * This class holds constant values used by both the RTI and client sections of 
 * the JSOP console binding
 */
public final class ConsoleJSOPConstants
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The default port used by JSOP */
	public static final int DEFAULT_PORT = 20912;

	/** The ip address the multicast socket will use */
	public static final String MULTICAST_IP = "225.4.5.6";

	/** The "unique" message that LRC clients send to prompt a response from this daemon */
	public static final String MULTICAST_WELCOME = "Wordizzle";
	
	/** 
	 * The prefix used in the string pass back by this daemon to welcome requests, The port that
	 * the JSOP bootstrap is executing on it appended to the end.
	 */
	public static final String MULTICAST_PREFIX = "CONSOLEDISCOVERY:" ;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private ConsoleJSOPConstants()
	{
		
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
