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
package org.portico2.rti.cli;

import java.util.StringTokenizer;

import org.portico2.common.utils.TextDevice;
import org.portico2.rti.RTI;
import org.portico2.rti.cli.command.CommandFactory;
import org.portico2.rti.cli.command.ICommand;
import org.portico2.rti.cli.fs.FSContext;
import org.portico2.rti.cli.fs.FSContextFactory;
import org.portico2.rti.cli.fs.FSContext.ContextType;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.federation.FederationManager;

/**
 * This class provides a basic command line interface to control an RTI instance.
 */
public class RtiCli implements Runnable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RTI rti;
	private EnvironmentVariables env;
	private TextDevice console;
	private FSContext rootContext;
	private FSContext currentContext;
	private volatile boolean shutdownFlag;
	
	private Thread thread;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RtiCli( RTI rti )
	{
		this.rti = rti;
		this.env = new EnvironmentVariables( this );
		this.console = TextDevice.getDefaultDevice();
		this.rootContext = FSContextFactory.createRootContext();
		this.currentContext = this.rootContext;
		this.shutdownFlag = false;
		this.thread = null; // set in start()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void start()
	{
		if( this.thread != null )
			throw new IllegalStateException( "Interpreter has already been started" );
		
		this.thread = new Thread( this, "CLI Reader" );
		this.thread.start();
	}
	
	@Override
	public void run()
	{
		while( Thread.interrupted() == false )
		{
			String commandline = console.readLine( getCommandPrompt() );
			try
			{
				process( commandline );
			}
			catch( Exception e )
			{
				console.printf( "[Error] (%s) %s\n", e.getClass().getSimpleName(), e.getMessage() );
			}
			
			if( this.shutdownFlag )
			{
				rti.shutdown();
				Thread.currentThread().interrupt();
			}
			else
			{
				// Nah. Looks better without the space! More *nix!
				//console.printf( "\n" );
			}
		}
	}
	
	private String getCommandPrompt()
	{
		String host = "rti";
		String context = currentContext == rootContext ? "/" : currentContext.getHeirachicalName();
		String prompt = ">";
		return host + " " + context + prompt + " ";
	}
	
	/**
	 * Flags the CLI for shutdown.
	 * <p/>
	 * Once flagged for shutdown, Portico will cleanly terminate at the end of the next run() cycle
	 */
	public void flagShutdown()
	{
		this.shutdownFlag = true;
	}
	
	public boolean isValidContext( FSContext candidate )
	{
		ContextType theType = candidate.getType();
		if( theType == ContextType.RTI )
			return true;

		boolean valid = false;
		FederationManager fedman = rti.getFederationManager();
		if( theType == ContextType.Federation )
		{
			valid = fedman.containsFederation( candidate.getName() );
		}
		else if( theType == ContextType.Federate )
		{
			String federationName = candidate.getParent().getName();
			Federation federation = fedman.getFederation( federationName );
			if( federation != null )
				valid = federation.containsFederate( candidate.getName() );
		}
		
		return valid;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Command Processing   //////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void process( String commandline )
	{
		if( commandline.isEmpty() )
			return;
		
		// Replace any environment variable tokens with their corresponding values
		commandline = env.replaceTokens( commandline );
		
		// Tokenise the command line string
		StringTokenizer tok = new StringTokenizer( commandline );
		
		// Get the first token (the actual command) and generate a command object from it
		String command = tok.nextToken();
		ICommand commandImpl = CommandFactory.create( command );
		
		// Merge the rest of the arguments into a String array
		int remainingTokens = tok.countTokens();
		String[] args = new String[remainingTokens];
		
		for( int x = 0 ; x < remainingTokens ; ++x )
			args[x] = tok.nextToken();
		
		// Execute the command
		commandImpl.execute( this, args );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public RTI getRti()
	{
		return this.rti;
	}
	
	public EnvironmentVariables getEnvironmentVariables()
	{
		return this.env;
	}
	
	public FSContext getRootContext()
	{
		return this.rootContext;
	}
	
	public FSContext getCurrentContext()
	{
		return this.currentContext;
	}
	
	public void setCurrentContext( FSContext context )
	{
		this.currentContext = context;
	}
	
	public TextDevice getConsole()
	{
		return this.console;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
