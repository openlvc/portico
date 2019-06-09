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
package org.portico2.rti.cli.command;

import org.portico2.common.utils.TextDevice;
import org.portico2.rti.RTI;
import org.portico2.rti.cli.RtiCli;

/**
 * Lists RTI information
 */
public class RtiInfo implements ICommand
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
	@Override
	public void execute( RtiCli container, String... args )
	{
		printRtiStatus( container );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void printRtiStatus( RtiCli container )
	{
		TextDevice console = container.getConsole();
		RTI rti = container.getRti();
		String connections = String.join( ", ", rti.getConnectionManager().getConnectionDescriptions() );
		console.printf( "%-16s%s\n", "Version:", container.getEnvironmentVariables().get("RTI_VERSION") );
		console.printf( "%-16s%s\n", "Connections:", connections );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
