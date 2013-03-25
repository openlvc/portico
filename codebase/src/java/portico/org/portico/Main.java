/*
 *   Copyright 2013 The Portico Project
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
package org.portico;

import org.portico.container.Container;
import org.portico.lrc.PorticoConstants;
import org.portico.utils.SystemInformation;

/**
 * This class exists inside the Portico jar file to provide diagnoistic information as
 * output when invoked. Portico is a library, not an application, so when someone invokes
 * the jar file directly, the main method from this class will run and will print
 * version and diagnostics information. Handy for quickly getting details about the
 * version of Portico a user is running.
 */
public class Main
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
	public static void main( String[] args )
	{
		// load the Container which loads the RID file
		Container.instance();

		// get the system information
		SystemInformation info = SystemInformation.LOCAL;
		StringBuilder buf = new StringBuilder( "\n" );

		buf.append( "##########################################################\n" );
		buf.append( "#                   Portico Open RTI                     #\n" );
		buf.append( "#            Welcome to Portico for the HLA!             #\n" );
		buf.append( "#                                                        #\n" );
		buf.append( "#     Portico is distributed by under the terms of       #\n" );
		buf.append( "#    the Common Development and Distribution License.    #\n" );
		buf.append( "#    For a copy of the license, see the LICENSE file     #\n" );
		buf.append( "#     included in the root of the distributable you      #\n" );
		buf.append( "#                      downloaded.                       #\n" );
		buf.append( "##########################################################\n" );
		buf.append( "#                                                        #\n" );
		buf.append( "#                    System Information                  #\n" );
		buf.append( "#                                                        #\n" );
		buf.append( pad( "# Portico Version:          " + PorticoConstants.RTI_VERSION ) );
		buf.append( pad( "# Platform Architecture:    " + info.getPlatform() ) );
		buf.append( pad( "# CPUs:                     " + info.getCPUCount() ) );
		buf.append( pad( "# Operating System:         " + info.getOS() ) );
		buf.append( pad( "# Operating System Version: " + info.getOSVersion() ) );
		buf.append( pad( "# Java Version:             " + info.getJavaVersion() ) );
		buf.append( pad( "# Java Vendor:              " + info.getJavaVendor() ) );
		buf.append( "#                                                        #\n" );
		buf.append( pad( "# Startup Time:             "+info.getStartupTime() ) );
		buf.append( pad( "# RID File:                 "+PorticoConstants.getRidFileLocation()) );
		buf.append( pad( "# Log Level:                "+PorticoConstants.PORTICO_LOG_LEVEL) );
		buf.append( "#                                                        #\n" );
		buf.append( "##########################################################\n" );
		buf.append( " => RTI Home: "+PorticoConstants.getRtiHome() );

		System.out.println( buf.toString() );
	}
	
	private static String pad( String initial )
	{
		// at the moment the length of the main delimiters for the licence info
		// is 54 characters. Pad it out to that
		int count = initial.length();
		StringBuffer buf = new StringBuffer( initial );
		while( count < 57 )
		{
			buf.append( " " );
			++count;
		}

		buf.append( "#\n" );
		return buf.toString();
	}
}
