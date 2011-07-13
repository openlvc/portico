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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import com.lbf.commons.component.ComponentException;
import com.lbf.commons.config.ConfigurationException;
import com.lbf.commons.delegate.DelegateException;
import com.lbf.commons.utils.Bag;
import org.portico.console.client.text.exception.InvalidCommandException;
import org.portico.console.client.text.fs.FSContext;
import org.portico.console.client.text.fs.FSContextFactory;
import org.portico.console.client.text.fs.FSContextVisitor;

/**
 * A Text console that reads standard input for a user command, converts that command into an 
 * appropriate {@link CONSOLE_RequestMessage CONSOLE_RequestMessage}, sends the message to the
 * console binding in the RTI and then outputs the resonse to standard output
 */
public class TextConsole implements FSContextVisitor
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String QUIT_COMMAND = "exit";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String bindingEndpoint;
	
	private Logger logger;
	
	private RequestCreator requestCreator;
	private FSContext rootContext;
	private FSContext currentContext;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TextConsole(String newBindingEndpoint)
	{
		this.bindingEndpoint = newBindingEndpoint;

		this.logger = Logger.getLogger( "text-console" );
		this.rootContext = FSContextFactory.createRootContext();
		this.currentContext = this.rootContext;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Configures the TextConsole and it's subcomponents (Console binding connection and response
	 * processor message sink)
	 */
	public void configure() throws ConfigurationException
	{
		Bag<String,Object> additionals = new Bag<String,Object>();
		additionals.put( ConsoleJSOPClientConnection.PROP_HOST, bindingEndpoint );
		
		this.requestCreator = new RequestCreator();
		this.requestCreator.configure(additionals);
		
	}
	
	/**
	 * Continually loops until the user indicates that they want to quit. Each loop around the user
	 * is prompted to enter a command. The command is then converted into a CONSOLE_RequestMessage
	 * and fired over JSOP to the RTI Console Binding. The response from the console binding is then
	 * received and passed to the response processor, where the rules regarding it's format on screen
	 * are excersised.
	 */
	public void interpret()
	{
		try
		{
			// start the connection to the RTI Console Binding
			this.requestCreator.connect();
			
			String currentCommand = "initial";
			
			// prepare the standard in stream
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(isr);
						
			// while the user hasn't said that they want to quit
			while (!currentCommand.equals(QUIT_COMMAND))
			{
				// Display the console prompt and request input
				this.printConsolePrompt();
				currentCommand = in.readLine().trim();
				
				// If the user just entered in a blank line then continue on
				// without trying to process it
				if ( currentCommand == null || currentCommand.equals(""))
				{
					continue;
				}
				
				// if the command wasn't to quit
				if ( !currentCommand.equals(QUIT_COMMAND) )
				{
					try
					{
						// Convert the command into RequestMessage format
						this.requestCreator.parseCommand(this, currentCommand);
					}
					catch (InvalidCommandException ice)
					{
						System.out.println("ERROR: " + ice.getMessage() + "\n");
					}
					catch (DelegateException de)
					{
						System.out.println("ERROR: " + de.getCause().getMessage() + "\n");
					}
				}
				
			}
		}		
		catch (IOException ioe)
		{
			logger.fatal("An I/O occured: " + ioe.getMessage(), ioe);
			ioe.printStackTrace();
		} 
		catch (ComponentException ce) 
		{
			logger.fatal("An communication error occured " + ce.getMessage(), ce);
			ce.printStackTrace();
		}
		catch (Exception e) 
		{
			logger.fatal("An error occured " + e.getMessage(), e);
			e.printStackTrace();
		}

		// Clean up
		try
		{
			this.requestCreator.disconnect();	
		}
		catch (Exception e)
		{
			// ignore
		}
	}
	
	private void printConsolePrompt()
	{
		String rtiLocation = "";
		String hostName = this.requestCreator.getHost();
		
		if (hostName.startsWith("/"))
		{
			rtiLocation = "rtiexec@" + this.requestCreator.getHost().substring( 1 );
		}
		else
		{
			rtiLocation = "rtiexec@" + this.requestCreator.getHost();
		}
		
		
		if ( this.currentContext == this.rootContext )
		{
			System.out.print(rtiLocation + " / $ ");
		}
		else
		{
			System.out.print(rtiLocation + " " + this.getCurrentContext().getHeirachicalName() 
			                 + " $ ");
		}
	}

	public FSContext getRootContext()
	{
		return this.rootContext;
	}

	public FSContext getCurrentContext()
	{
		return this.currentContext;
	}

	public void setCurrentContext( FSContext newContext )
	{
		this.currentContext = newContext;
	}
	

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}


