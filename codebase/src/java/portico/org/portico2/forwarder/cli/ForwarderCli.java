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
package org.portico2.forwarder.cli;

import org.portico2.common.utils.TextDevice;
import org.portico2.forwarder.Forwarder;

public class ForwarderCli implements Runnable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Forwarder forwarder;
	private TextDevice console;
	
	private Thread thread;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ForwarderCli( Forwarder forwarder )
	{
		this.forwarder = forwarder;
		this.console = TextDevice.getDefaultDevice();
		this.thread = null; // set in start()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void startup()
	{
		if( this.thread != null )
			throw new IllegalStateException( "Interpreter has already been started" );
		
		this.thread = new Thread( this, "CLI Reader" );
		this.thread.start();
	}
	
	public void run()
	{
		if( this.console == null )
			;
		
		while( Thread.interrupted() == false )
		{
			String command = console.readLine( "forwarder> " );
			process( command );
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Command Processing   //////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void process( String command )
	{
		if( command == null || command.trim().equals("") )
			return;

		command = command.trim().toLowerCase();
		if( command.equals("quit") || command.equals("exit") )
		{
			forwarder.shutdown();
			Thread.currentThread().interrupt();
		}
		else
		{
			this.console.printf( "command not known: %s\n", command );
		}
	}

	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
