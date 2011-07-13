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


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.lbf.commons.component.ComponentException;
import com.lbf.commons.config.ConfigurationException;
import com.lbf.commons.delegate.Delegate;
import com.lbf.commons.delegate.DelegateException;
import com.lbf.commons.messaging.ExceptionMessage;
import com.lbf.commons.messaging.MessageSink;
import com.lbf.commons.messaging.MessageContext;
import com.lbf.commons.messaging.MessagingException;
import com.lbf.commons.messaging.Module;
import com.lbf.commons.messaging.ModuleConfigurator;
import com.lbf.commons.messaging.ModuleGroup;
import com.lbf.commons.messaging.RequestMessage;
import com.lbf.commons.messaging.ResponseMessage;
import com.lbf.commons.messaging.SuccessMessage;
import com.lbf.commons.utils.Bag;
import org.portico.console.client.text.exception.InvalidCommandException;
import org.portico.console.client.text.fs.FSContext;
import org.portico.console.client.text.fs.FSContextException;
import org.portico.console.client.text.fs.FSContextFactory;
import org.portico.console.client.text.fs.FSContextVisitor;
import org.portico.console.client.text.fs.FSContext.ContextType;
import org.portico.console.shared.msg.CONSOLE_CreateFederation;
import org.portico.console.shared.msg.CONSOLE_GetFederateInfo;
import org.portico.console.shared.msg.CONSOLE_GetFederateNames;
import org.portico.console.shared.msg.CONSOLE_GetFederationInfo;
import org.portico.console.shared.msg.CONSOLE_GetFederationNames;
import org.portico.console.shared.msg.CONSOLE_GetRTIInfo;
import org.portico.console.shared.msg.CONSOLE_IsFederate;
import org.portico.console.shared.msg.CONSOLE_IsFederation;
import org.portico.console.shared.msg.CONSOLE_RequestMessage;
import org.portico.console.shared.msg.CONSOLE_TerminateFederate;
import org.portico.console.shared.msg.CONSOLE_TerminateFederation;

/**
 * This class converts plain text commands into an appropriate {@link CONSOLE_RequestMessage 
 * CONSOLE_RequestMessage} which can then be sent to the RTI Console Binding for processing
 */
public class RequestCreator 
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String, Delegate> commandList; 
	private ConsoleJSOPClientConnection toRTIBinding;
	private MessageSink responseProcessor;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RequestCreator() 
	{
		super();
		this.toRTIBinding = new ConsoleJSOPClientConnection();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void connect() throws ComponentException
	{
		this.toRTIBinding.execute();
	}
	
	public void disconnect() throws ComponentException
	{
		this.toRTIBinding.shutdown();
	}
	
	/**
	 * Configures the RequestCreator by registering valid console commands into a HashMap against a
	 * {@link Delegate Delegate} method that is to be called to convert the plain text command into
	 * a {@link CONSOLE_RequestMessage CONSOLE_RequestMessage}.
	 */
	public void configure(Bag<String,Object> properties) throws ConfigurationException
	{
		// initialise the connection to the rti console binding
		this.toRTIBinding.configure(properties);
		
		// Initialise the map of command names and delegate functions
		this.commandList = new HashMap<String, Delegate>();
		
		this.responseProcessor = new MessageSink( "console-response" );
		ModuleGroup theGroup = new ModuleGroup("console-response-group");
		Module module = ModuleConfigurator.createModule( MainProperties.getModuleLocation() );
		theGroup.addModule( module );
		
		theGroup.applyGroup( this.responseProcessor );
		
		
		
		// Populate the hash map
		try
		{
			this.commandList.put( "alias",
					new Delegate(this, "processALIAS(FSContextVisitor,String[])"));
			
			// Our Commands		
			this.commandList.put( "cd",  
			         new Delegate(this, "processCD(FSContextVisitor,String[])"));
			this.commandList.put( "ls",
			         new Delegate(this, "processLS(FSContextVisitor,String[])"));
			this.commandList.put( "cat",
			         new Delegate(this, "processCAT(FSContextVisitor,String[])"));
			this.commandList.put( "pwd",
			         new Delegate(this, "processPWD(FSContextVisitor,String[])"));
			this.commandList.put("rm", 
					new Delegate(this, "processRM(FSContextVisitor,String[])"));
			this.commandList.put( "create",
			         new Delegate(this, "processCreateFed(FSContextVisitor,String[])"));
			
			// PORT-286 - Add a "help" command to the console client that prints out all the 
			// available commands
			this.commandList.put( "man", 
			        new Delegate(this, "processMAN(FSContextVisitor,String[])"));
			
			
			// DMSO RTI Console Legacy Commands		
			this.commandList.put( "federation",  
			         new Delegate(this, "processCD(FSContextVisitor,String[])"));
			this.commandList.put( "federate",  
			         new Delegate(this, "processCD(FSContextVisitor,String[])"));
			this.commandList.put( "list",
			         new Delegate(this, "processLS(FSContextVisitor,String[])"));
			this.commandList.put( "status",
			         new Delegate(this, "processCAT(FSContextVisitor,String[])"));
			this.commandList.put("kill", 
					new Delegate(this, "processRM(FSContextVisitor,String[])"));
			
			// PORT-286 - Add a "help" command to the console client that prints out all the 
			// available commands
			this.commandList.put( "help", 
			        new Delegate(this, "processMAN(FSContextVisitor,String[])"));

		}
		catch (DelegateException de)
		{
			// In the event of an error, advise the calling environment
			throw new ConfigurationException("Could not create delegate: " 
					+ de.getMessage(), de);
		}
		
		
	}
	
	/**
	 * Converts a String representing a command line into a {@link CONSOLE_RequestMessage 
	 * CONSOLE_RequestMessage}
	 * 
	 * @param commandLine An object of type String representing the command and arguments that the
	 * user has inputted on the command line.
	 * 
	 * @return An object of type {@link CONSOLE_RequestMessage CONSOLE_RequestMessage} representing
	 * the command and arguments in Message form
	 * 
	 * @throws InvalidCommandException Thrown if the parsed message is not a registered message
	 * @throws DelegateException Thrown if an exception occured while trying to convert the message
	 * into {@link CONSOLE_RequestMessage CONSOLE_RequestMessage} format.
	 * 
	 */
	public void parseCommand(FSContextVisitor contextInfo, String commandLine) 
		throws InvalidCommandException, DelegateException
	{
		// Tokenise the command line string
		StringTokenizer tok = new StringTokenizer(commandLine);
		
		// Get the first token (the actual command)
		String command = tok.nextToken();
		
		// If the command is not registered in the set of commands
		if ( !this.commandList.containsKey(command) )
		{
			// Throw an exception to the calling environmnet
			throw new InvalidCommandException("No such command '" + command  + "'");
		}
		
		// Merge the rest of the arguments into a String array
		int remainingTokens = tok.countTokens();
		String[] args = new String[remainingTokens];
		
		for ( int x = 0 ; x < remainingTokens ; ++x )
		{
			args[x] = tok.nextToken();
		}
		
		// Invoke the delegate that handles this command and return the resultant RequestMessage		
		this.commandList.get(command).invoke(new Object[] {contextInfo, args});
		
	}
	
	/**
	 * Changes the current context to the path that the user provides
	 * @param contextInfo The context information of the shell
	 * @param args The arguments parsed by the user from the command line
	 * @throws MessagingException
	 * @throws FSContextException
	 */
	public void processCD(FSContextVisitor contextInfo, String[] args) 
		throws MessagingException, FSContextException
	{
		// Make sure there is always 1 argument
		if ( args.length == 1 )
		{
			// Build up the context to where the user wants to go
			FSContext tempContext = buildContextFromPath(contextInfo, args[0]);
			
			// Check if the destination context is valid
			if ( this.validateContext( tempContext ))
			{
				// if it is, set the current context to the destination context
				contextInfo.setCurrentContext( tempContext );
			}
			else
			{
				// otherwise, throw an exception
				throw new FSContextException("Path does not exist: " + args[0]);
			}
		}
		else
		{
			// if there were an incorrect number of arguments, throw an exception
			throw new MessagingException ("Incorrect number of arguments." +
			                              " Expected usage: cd pathname");
		}
	}
	
	/**
	 * Outputs a file/directory listing of the current context
	 * @param contextInfo The context information of the shell
	 * @param args The arguments parsed by the user from the command line
	 * @throws MessagingException
	 * @throws FSContextException
	 */
	public void processLS(FSContextVisitor contextInfo, String[] args)
		throws MessagingException, FSContextException, Exception
	{
		FSContext tempContext = null;
		
		// If the user parsed no arguments
		if ( args.length == 0 )
		{
			// Set the temporary context reference to the current context
			tempContext = contextInfo.getCurrentContext();
		}
		else if ( args.length == 1 )
		{
			// otherwise if the user parsed an argument
			// Set the temporary context reference to the path that the user specified
			tempContext = this.buildContextFromPath( contextInfo, args[0] );
		}
		
		// Create a request message dependant on what the destination context is
		RequestMessage request = null;	
		
		if ( tempContext.getType() == ContextType.RTI )
		{
			request = new CONSOLE_GetFederationNames();
		}
		else if ( tempContext.getType() == ContextType.Federation )
		{
			request = new CONSOLE_GetFederateNames (tempContext.getName());
		}
		
		// If we need to get information from the RTI
		if ( request != null )
		{
			// Send a message to the RTI and process the response
			MessageContext message = new MessageContext( request );
			ResponseMessage response = this.toRTIBinding.sendMessage( message );
			message.setResponse( response );
			this.responseProcessor.processMessage( message );
		}
		else
		{
			// otherwise, no information is needed from the RTI, just print out a default
			// listing
			System.out.println("./");
			System.out.println("../");
		}
	}
	
	/**
	 * Outputs information regarding a specific object within the file system
	 * @param contextInfo The context information of the shell
	 * @param args The arguments parsed by the user from the command line
	 * @throws MessagingException
	 * @throws FSContextException
	 */
	public void processCAT(FSContextVisitor contextInfo, String[] args)
		throws MessagingException, FSContextException, Exception
	{
		FSContext tempContext = null;
		
		// If the user parsed no arguments
		if ( args.length == 0 )
		{
			// Set the temporary context reference to the current context
			tempContext = contextInfo.getCurrentContext();
		}
		else if ( args.length == 1 )
		{
			// otherwise if the user parsed an argument
			// Set the temporary context reference to the path that the user specified
			tempContext = this.buildContextFromPath( contextInfo, args[0] );
		}
		
		// Create a request message dependant on what the destination context is
		RequestMessage request = null;	
		
		if ( tempContext.getType() == ContextType.RTI )
		{
			request = new CONSOLE_GetRTIInfo();
		}
		else if ( tempContext.getType() == ContextType.Federation )
		{
			request = new CONSOLE_GetFederationInfo (tempContext.getName());
		}
		else if ( tempContext.getType() == ContextType.Federate )
		{
			request = new CONSOLE_GetFederateInfo(tempContext.getParent().getName(),
			                                      tempContext.getName());
		}
		
		// Send a message to the RTI and process the response
		MessageContext message = new MessageContext( request );
		ResponseMessage response = this.toRTIBinding.sendMessage( message );
		message.setResponse( response );
		this.responseProcessor.processMessage( message );
	}
	
	/**
	 * Aliases a new command name to an existing command
	 * @param contextInfo The context information of the shell
	 * @param args The arguments parsed by the user from the command line
	 * @throws MessagingException
	 * @throws FSContextException
	 */
	public void processALIAS(FSContextVisitor contextInfo, String[] args)
		throws MessagingException, FSContextException, Exception
	{
		// If the user has provided two arguments
		if ( args.length == 2 )
		{
			// If the existing command the user specified exists in the command list
			if ( this.commandList.containsKey(args[0]) )
			{
				// If the new command name does not exist in the command list allready
				if ( !this.commandList.containsKey(args[1]) )
				{
					// Get a reference to the existing command
					Delegate existingCommand = this.commandList.get(args[0]);
					
					// Map the new command name to the existing command
					this.commandList.put(args[1], existingCommand);
					System.out.println("Existing command " + args[0] 
					                 + " successfully aliased to " + args[1]);
				}
				else
				{
					// Otherwise let the user know that the new command name is allready taken
					throw new MessagingException ("The new command name, '" +
							args[1] + "' allready exists.");
				}
			}
			else
			{
				// Otherwise let the user know that the command name they want to alias doesn't
				// exist
				throw new MessagingException ("The command '" +
						args[0] + "' does not exist.");
			}
		}
		else
		{
			// Otherwise throw an exception with the correct usage of the command
			throw new MessagingException ("Incorrect number of arguments." +
			                              " Expected usage: alias existingCommand newName");
		}
	
	}
	
	/**
	 * Outputs the heirachical name of the current context
	 * @param contextInfo The context information of the shell
	 * @param args The arguments parsed by the user from the command line
	 * @throws MessagingException
	 * @throws FSContextException
	 */
	public void processPWD(FSContextVisitor contextInfo, String[] args)
		throws MessagingException, FSContextException, Exception
	{
		String pathName = null;
		
		// If the user is in the root context
		if ( contextInfo.getCurrentContext() == contextInfo.getRootContext())
		{
			// Just output the root context character
			pathName = "/";
		}
		else
		{
			// otherwise output the whole heirachical name
			pathName = contextInfo.getCurrentContext().getHeirachicalName();
		}
		System.out.println(pathName);
	
	}
	
	
	private FSContext buildContextFromPath(FSContextVisitor contextInfo, String path)
		throws FSContextException
	{
		// Create a temporary context to hold where we are going to go
		// initialise it to where we are currently
		FSContext tempContext = contextInfo.getCurrentContext();
		
		// If the path started with the root symbol then point the temp context to the
		// root context
		if ( path.startsWith( "/" ) )
		{
			tempContext = contextInfo.getRootContext();
		}
		
		// Tokenise the path on /
		StringTokenizer tok = new StringTokenizer(path, "/");
		
		// Iterate through the tokens
		while (tok.hasMoreTokens())
		{
			String token = tok.nextToken();
			
			// If the current entry in the path is to go up one
			if ( token.equals( ".." ) )
			{
				// Have a look at the parent of this context
				FSContext theParent = tempContext.getParent();
				
				// if the parent of this context is null
				if ( theParent == null )
				{
					// we are at the root so just stay here
				}
				else
				{
					// otherwise traverse up
					tempContext = tempContext.getParent();
				}
			}
			else if ( token.equals( "." ) )
			{
				// stay in the same context
			}
			else
			{
				// going down a directory
				tempContext = FSContextFactory.createContext( token, tempContext );
			}
		}
				
		return tempContext;	
	}

	/**
	 * Attempts to kill the specified context
	 * @param contextInfo The context information of the shell
	 * @param args The arguments parsed by the user from the command line
	 * @throws MessagingException
	 * @throws FSContextException
	 */
	public void processRM(FSContextVisitor contextInfo, String[] args)
		throws MessagingException, FSContextException, Exception
	{
		FSContext tempContext = null;
		
		// If the user parsed no arguments
		if ( args.length == 0 )
		{
			// Set the temporary context reference to the current context
			tempContext = contextInfo.getCurrentContext();
		}
		else if ( args.length == 1 )
		{
			// otherwise if the user parsed an argument
			// Set the temporary context reference to the path that the user specified
			tempContext = this.buildContextFromPath( contextInfo, args[0] );
		}
		
		// Create a request message dependant on what the destination context is
		RequestMessage request = null;	
		
		if ( tempContext.getType() == ContextType.RTI )
		{
			// TODO Should we support the killing of the rti here?
			System.out.println("ERROR: rm at the RTI level is not supported yet!");
			return;
		}
		else if ( tempContext.getType() == ContextType.Federation )
		{
			// TODO Kill all federates in the federation and then
			// kill off the fedex
			request = new CONSOLE_TerminateFederation(tempContext.getName());			
		}
		else if ( tempContext.getType() == ContextType.Federate )
		{
			request = new CONSOLE_TerminateFederate(tempContext.getParent().getName(),
			                                      tempContext.getName());
		}
		
		// Send a message to the RTI and process the response
		MessageContext message = new MessageContext( request );
		ResponseMessage response = this.toRTIBinding.sendMessage( message );
		
		// Process the result
		message.setResponse( response );
		this.responseProcessor.processMessage( message );
	}
	
	/**
	 * This method will handle requests to create a new federation on the RTI we are connected ti
	 */
	public void processCreateFed(FSContextVisitor contextInfo, String[] args) 
		throws MessagingException, FSContextException, Exception
	{
		if( args.length != 2 )
		{
	 		throw new MessagingException ( "Incorrect number of arguments. Expected usage: " +
	 		                               "createFederation federationName fedFileLocation" );
		}
		
		//////////////////////////////////
		// try and open the config file //
		//////////////////////////////////
		File fedfile = new File( args[1] );
		if( fedfile.exists() == false )
		{
			throw new MessagingException( "Can't find fed file: " + args[1] );
		}
		
		byte[] bytes = null;
		try
		{
    		// open the file and get its size
    		InputStream istream = new FileInputStream( fedfile );
    		long length = fedfile.length();
    		
    		// create the byte array to hold the data
    		bytes = new byte[(int)length];
    		
    		// read in the bytes
    		int offset = 0;
    		int read = 0;
    		while( offset < bytes.length && (read=istream.read(bytes,offset,bytes.length-offset)) >= 0 )
    		{
    			offset += read;
    		}
    		
    		// ensure all the bytes have been read in
    		if( offset < bytes.length )
    		{
    			throw new MessagingException( "Could not completely read fed file: " + fedfile );
    		}
    		
    		// close the input stream and return bytes
    		istream.close();
		}
		catch( Exception e )
		{
			throw new MessagingException( "Error reading fed: " + e.getMessage(), e );
		}
        
		//////////////////////////////////
		// fire a message to the server //
		//////////////////////////////////
		CONSOLE_CreateFederation request = new CONSOLE_CreateFederation();
		request.setFederationName( args[0] );
		request.setFomContents( bytes );
		MessageContext context = new MessageContext( request );
		
		// send the message //
		ResponseMessage response = this.toRTIBinding.sendMessage( context );
		if( response.isError() == false )
		{
			System.out.println( "Success. Created federation: " +args[0]+ " with fed " +args[1] );
		}
		else
		{
			System.out.println( "Error. Couldn't create federation: " +
			                    context.getErrorResponse().getCause().getMessage() );
		}
	}

	/**
	 * Prints usage information relevant to the specified command names given. If no commands
	 * are specified, a list of all available commands for this context are given
	 * @param contextInfo The context information of the shell
	 * @param args The arguments parsed by the user from the command line
	 */
	// PORT-286 - Add a "help" command to the console client that prints out all the 
	// available commands
	public void processMAN(FSContextVisitor contextInfo, String[] args)
	{
		ContextType theType = contextInfo.getCurrentContext().getType();
		
		StringBuilder buffer = new StringBuilder();
		
		// If no commands are given
		if ( args.length == 0 )
		{
			// Print list of available commands in this context
			buffer.append("Here is a list of commands specific to this context.\nFor more " + 
			              "information on a specfic command type: man [command name]\n" );
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("PORTICO STYLE\t\tDMSO STYLE\n" );
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("alias\t\t\t-\n");
			buffer.append("cat\t\t\tstatus\n" );
			buffer.append("cd\t\t\tfederate, federation\n");
			
			if ( theType == ContextType.RTI )
			{
				buffer.append("create\t\t\t-\n" );	
			}
			
			buffer.append("ls\t\t\tlist\n");
			buffer.append("pwd\t\t\t-\n");
			buffer.append("rm\t\t\tkill\n" );
			
			buffer.append("\n");
		}
		// otherwise collate information regarding the list of commands specified
		else
		{
			for (String command : args)
			{
				buffer.append( getCommandUsage(theType, command) );
				buffer.append( "\n" );
			}
		}
		
		// display the information
		System.out.println(buffer.toString());
		
	}
	
	private String getCommandUsage(ContextType theType, String commandName)
	{
		StringBuilder buffer = new StringBuilder();
		
		if ( commandName.equals( "alias" ) )
		{
			buffer.append("MANUAL PAGE FOR alias\n");
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("USAGE:\t\t\t alias existing_command new_name\n");
			buffer.append("DESCRIPTION:\t\t Aliases an existing command to a new command name.\n");
			buffer.append("EXAMPLE:\t\t alias cd change\n" );
			buffer.append("DMSO EQUIVALENT:\t n/a\n");
		}
		else if (commandName.equals( "cat" ) || commandName.equals( "status" ) )
		{
			buffer.append("MANUAL PAGE FOR cat\n");
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("USAGE:\t\t\t cat [context_name]\n");
			buffer.append("DESCRIPTION:\t\t Provides context-specific information about the " +
					"specified context.\n\t\t\t If no argument is provided information about the " +
					"current context is listed.\n");
			buffer.append("EXAMPLE:\t\t cat /myFederation/federate1\n" );
			buffer.append("DMSO EQUIVALENT:\t status\n");
		}
		else if (commandName.equals( "cd" ) || commandName.equals( "federate" ) 
			|| commandName.equals( "federation" ))
		{
			buffer.append("MANUAL PAGE FOR cd\n");
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("USAGE:\t\t\t cd context_name\n");
			buffer.append("DESCRIPTION:\t\t Changes the current context to the context specified\n");
			buffer.append("EXAMPLE:\t\t cd federate1\n" );
			buffer.append("DMSO EQUIVALENT:\t federate or federation\n");
		}
		else if (commandName.equals( "create" ) )
		{
			buffer.append("MANUAL PAGE FOR create\n");
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("USAGE:\t\t\t create federation_name fed_file_location\n");
			buffer.append("DESCRIPTION:\t\t Manually creates a federation with the specified name " +
					"and fed file\n");
			buffer.append("EXAMPLE:\t\t create myFederation myFOM.fed\n" );
			buffer.append("DMSO EQUIVALENT:\t n/a\n");
			buffer.append("NOTE:\t\t\t This command is only available in the RTI-level context!\n");
		}
		else if (commandName.equals( "ls" ) || commandName.equals( "list" ))
		{
			buffer.append("MANUAL PAGE FOR ls\n");
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("USAGE:\t\t\t ls [context_name]\n");
			buffer.append("DESCRIPTION:\t\t Lists the contents of the specified context.\n" +
					"\t\t\t If no argument is provided the contents of the " +
					"current context are listed.\n");
			buffer.append("EXAMPLE:\t\t ls /myFederation/federate1\n" );
			buffer.append("DMSO EQUIVALENT:\t list\n");
		}
		else if (commandName.equals( "pwd" ) )
		{
			buffer.append("MANUAL PAGE FOR pwd\n");
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("USAGE:\t\t\t pwd\n");
			buffer.append("DESCRIPTION:\t\t Returns the absoloute path of the current context\n");
			buffer.append("EXAMPLE:\t\t pwd\n" );
			buffer.append("DMSO EQUIVALENT:\t n/a\n");
		}
		else if (commandName.equals( "rm" ) || commandName.equals( "kill" ))
		{
			buffer.append("MANUAL PAGE FOR rm\n");
			buffer.append("-------------------------------------------------------------\n");
			buffer.append("USAGE:\t\t\t rm context_name\n");
			buffer.append("DESCRIPTION:\t\t Removes the specified federation or federate from the " +
					"RTI execution\n");
			buffer.append("EXAMPLE:\t\t rm /myFederation/federate1\n" );
			buffer.append("DMSO EQUIVALENT:\t kill\n");
		}
		else
		{
			buffer.append( "No manual page for command: " + commandName + "\n");
		}
		
		return buffer.toString();
	}
	
	private boolean validateContext(FSContext theContext) throws MessagingException
	{
		ContextType theType = theContext.getType();
		
		if ( theType == ContextType.RTI )
		{
			return true;
		}
		else
		{
			CONSOLE_RequestMessage requestMessage = null;
			if (theType == ContextType.Federation)
			{
				requestMessage = new CONSOLE_IsFederation(theContext.getName());
			}
			else if (theType == ContextType.Federate)
			{
				
				requestMessage = new CONSOLE_IsFederate(theContext.getParent().getName(), 
				                                        theContext.getName());
			}
			
			MessageContext message = new MessageContext(requestMessage);
			ResponseMessage response = this.toRTIBinding.sendMessage( message );
			
			if ( response.isError() )
			{
				ExceptionMessage error = (ExceptionMessage)response;
				throw new MessagingException(error.getCause());
			}
			else
			{
				SuccessMessage success = (SuccessMessage)response;
				return (Boolean)success.getResult();
			}
		}
	}
	
	
	public String getHost()
	{
		return this.toRTIBinding.getHost();
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
