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

import org.portico2.rti.cli.RtiCli;

/**
 * A command that can be executed by the {@link RtiCli}
 */
public interface ICommand
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Executes a command in the {@link RtiCli} container
	 * 
	 * @param container the {@link RtiCli} that the command is to be run in
	 * @param args the arguments to the command
	 */
	public void execute( RtiCli container, String... args );

}
