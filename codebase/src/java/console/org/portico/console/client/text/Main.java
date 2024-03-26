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
package org.portico.console.client.text;

import com.lbf.commons.config.ConfigurationException;
import com.lbf.commons.logging.Log4jConfigurator;
import org.portico.console.client.text.config.CLConfigurator;

import org.apache.logging.log4j.Logger;

public class Main
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static Logger LOG = null;
	
	public static String[] COMMAND_LINE = null; 
	
	
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
	/* ******************************************************************* */
	/* *************************** MAIN METHOD *************************** */
	/* ******************************************************************* */
	public static void main( String[] args )
	{
		//////////////////////////////
		// 1. bootstrap the logging //
		//////////////////////////////
		Log4jConfigurator.bootstrap( MainProperties.getLog4jConfig() );
		Main.LOG = LogManager.getLogger( "portico.console" );
		
		///////////////////////////////
		// 2. parse the command line //
		///////////////////////////////
		Main.COMMAND_LINE = args;		
		TextConsole tc = null;
		
		////////////////////////////////////////
		// 3. configure the working component //
		////////////////////////////////////////
		try
		{
			CLConfigurator clconfigurator = new CLConfigurator();
			clconfigurator.runConfigurator( args );
			LOG.info( Main.initialMessage() );
			tc = new TextConsole(MainProperties.getEndpoint());
			tc.configure();
		}
		catch( ConfigurationException ce )
		{
			System.err.println( "ERROR: " + ce.getMessage() );
			printUsage();
			System.err.println( "ERROR: " + ce.getMessage() );
			return;
		}

		////////////////////////////////////////
		// 4. GAME ON!!!					  //
		////////////////////////////////////////
		tc.interpret();
		
	}
	
	private static String initialMessage()
	{
		StringBuilder buf = new StringBuilder( "\n" );
		
		buf.append( "##########################################################\n" );
		buf.append( "#                     Portico Console                    #\n" );
		buf.append( "#            Welcome to Portico for the HLA!!            #\n" );
		buf.append( "#                                                        #\n" );
		buf.append( "#     portico is distributed by The Portico Project      #\n" );
		buf.append( "#   under the terms of the provided license agreement.   #\n" );
		buf.append( "#    For a copy of the license, see the LICENSE file     #\n" );
		buf.append( "#     included in the root of the distributable you      #\n" );
		buf.append( "#                      downloaded.                       #\n" );
		buf.append( "##########################################################\n" );
		
		return buf.toString();
	}
	
	private static void printUsage()
	{
		StringBuilder buf = new StringBuilder( "Usage: rticonsole [-e,v,q,ch,ll,h]\n" );
		buf.append( "-e, --endpoint\n" );
		buf.append( "  Specifies an ipaddress or port that the rti console binding is listening\n" );
		buf.append( "  on at the RTI side\n" );
		buf.append( "-v, --verbose\n" );
		buf.append( "  Turns logging to the DEBUG level, overriding any threshold\n" );
		buf.append( "  specified by -ll or --log-level\n" );
		buf.append( "-vv, --very-verbose\n" );
		buf.append( "  Turns logging to the TRACE level, overriding any threshold\n" );
		buf.append( "  specified by -ll or --log-level\n" );
		buf.append( "-q, --quiet\n" );
		buf.append( "  Turns console logging to the ERROR level, overriding any threshold\n" );
		buf.append( "  specified by -ll or --log-level. This will not change log file level\n" );
		buf.append( "-s, --silent\n" );
		buf.append( "  Turns console logging to the OFF level, overriding any threshold\n" );
		buf.append( "  specified by -ll or --log-level. This will not change log file level\n" );
		buf.append( "-ll, --log-level <argument>\n" );
		buf.append( "  Specify the logging threshold\n" );
		buf.append( "  Possible values are: TRACE, DEBUG, INFO, WARN, ERROR or FATAL\n" );
		buf.append( "  DEFAULT: INFO\n" );
		buf.append( "-h, --help\n" );
		buf.append( " Print this help\n" );
		
		System.err.println( buf.toString() );
	}
}
