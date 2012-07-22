/*
 *   Copyright 2008 The Portico Project
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
package org.portico.bindings.jgroups;

public class JGProperties
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	///// configuration system properties ////////////////////////////////////////////////////
	/** The system property that carries the log level to use for the JGroups logger */
	public static final String PROP_JGROUPS_LOGLEVEL = "portico.jgroups.loglevel";
	
	/** The system property that is checked for the address to use for UDP Multicast, defaults
	    to 228.10.10.10, the XML files jgroups uses for configuration will pick this up */
	public static final String PROP_JGROUPS_UDP_ADDRESS = "portico.jgroups.udp.address";
	
	/** The system property that is checked for the port to use for UDP Multicast, default: 20913.
	    The XML files that jgroups uses for configuration will pick up this value */
	public static final String PROP_JGROUPS_UDP_PORT = "portico.jgroups.udp.port";
	
	/** The system property that specifies whether or not the JGroups threads should be daemons */
	public static final String PROP_JGROUPS_DAEMON = "portico.jgroups.daemon";
	
	/** The system property that specifies the timeout value is used when waiting for response
	    messages (in milliseconds), default value is 1000 */
	public static final String PROP_JGROUPS_TIMEOUT = "portico.jgroups.timeout";

	///// jgroups properties /////////////////////////////////////////////////////////////////
	/** The amount of time (in milliseconds) to wait for a response to a request, defaults to 1000,
	    controllable through system property {@link #PROP_JGROUPS_TIMEOUT}  */
	public static long RESPONSE_TIMEOUT =
		Long.parseLong(System.getProperty(PROP_JGROUPS_TIMEOUT,"1000") );
	
	///// additional jgroups flags ///////////////////////////////////////////////////////////
	/** Signals that a message is a create federation message and contains an object model */
	public static final byte MSG_CREATE = 8;
	
	/** Signals that the source connection has joined the federation */
	public static final byte MSG_JOINED = 16;
	
	/** Signals that the source connection has resigned from the federation */
	public static final byte MSG_RESIGNED = 32;
	
	/** Signals that the source connection has destroyed the federation */
	public static final byte MSG_DESTROY = 64;

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

}
