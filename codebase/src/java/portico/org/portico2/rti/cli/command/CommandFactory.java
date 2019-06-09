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

/**
 * Quick and dirty command factory, that creates {@link ICommand} instances based on a command name
 */
public class CommandFactory
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

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static ICommand create( String command )
	{
		if( command.equals("cd") )
			return new ChangeContext();
		else if( command.equals("echo") )
			return new Echo();
		else if( command.equals("env") )
			return new ListEnvironmentVariables();
		else if( command.equals("exit") )
			return new Shutdown();
		else if( command.equals("quit") )
			return new Shutdown();
		else if( command.equals("ls") )
			return new ListContextContents();
		else if( command.equals("mkfederation") )
			return new CreateFederation();
		else if( command.equals("mominfo") )
			return new MomInfo();
		else if( command.equals("pubs") )
			return new PubInfo();
		else if( command.equals("rtiinfo") )
			return new RtiInfo();
		else if( command.equals("rm") )
			return new RemoveContext();
		else if( command.equals("subs") )
			return new SubInfo();
		else
			throw new IllegalArgumentException( "Unknown command: " + command );
	}
}
