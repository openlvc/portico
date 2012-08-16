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
package org.portico.console.shared.msg;


/**
 * This message is a request to the console RTI binding to provide basic information about the RTI
 */
public class CONSOLE_GetRTIInfo extends CONSOLE_RequestMessage
{

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	public static final String KEY_CPU_COUNT = "cpu_count";
	public static final String KEY_IP_ADDRESS = "ip_address";
	public static final String KEY_JAVA_VENDOR = "java_vender";
	public static final String KEY_JAVA_VERSION = "java_version";
	public static final String KEY_LAUNCH_DIR = "launch_dir";
	public static final String KEY_OS = "os";
	public static final String KEY_OS_VERSION = "os_version";
	public static final String KEY_PLATFORM = "platform";
	public static final String KEY_START_TIME = "start_time";
		
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public CONSOLE_GetRTIInfo()
	{
		super();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}


