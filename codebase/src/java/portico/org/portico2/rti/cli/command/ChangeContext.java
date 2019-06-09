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

import org.portico2.rti.RTI;
import org.portico2.rti.cli.RtiCli;
import org.portico2.rti.cli.fs.FSContext;
import org.portico2.rti.cli.fs.FSContextFactory;

/**
 * Changes the CLI context to point to another place in the pseudo file system
 * <p/>
 * <b>Expected Usage:</b> cd newcontext
 */
public class ChangeContext implements ICommand
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
		RTI rti = container.getRti();
		
		// Make sure there is always 1 argument
		if( args.length != 1 )
		{
			// if there were an incorrect number of arguments, throw an exception
			throw new IllegalArgumentException( "Incorrect number of arguments." +
			                                    " Expected usage: cd pathname");
		}
		
		// Build up the context to where the user wants to go
		FSContext destination = FSContextFactory.fromPath( container, args[0] );
		
		// Check if the destination context is valid
		if( container.isValidContext(destination) )
		{
			// if it is, set the current context to the destination context
			container.setCurrentContext( destination );
		}
		else
		{
			// otherwise, throw an exception
			throw new IllegalArgumentException("Path does not exist: " + destination.getHeirachicalName() );
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
