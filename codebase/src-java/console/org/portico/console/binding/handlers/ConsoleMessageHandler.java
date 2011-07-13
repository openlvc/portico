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
package org.portico.console.binding.handlers;

import hla.rti.RTIinternalError;

import com.lbf.commons.config.ConfigurationException;
import com.lbf.commons.messaging.AbstractMessageHandler;
import com.lbf.commons.messaging.MessageContext;
import com.lbf.commons.messaging.MessageSink;
import com.lbf.commons.utils.Bag;
import org.portico.core.RTIExec;

/**
 * This class should be the parent of all console message handlers on the RTI Binding side.
 * It provides a number of helper methods to make life easier.
 */
public abstract class ConsoleMessageHandler extends AbstractMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_RTIEXEC = "rtiexec";
	public static final String KEY_SINK = "tehsink";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected RTIExec rtiExecution;
	
	protected MessageSink consoleRequestSink;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ConsoleMessageHandler( String name )
	{
		super( name );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void initialize( Bag<String,?> properties ) throws ConfigurationException
	{
		// do we have the appropriate property? //
		if( properties.containsKey(ConsoleMessageHandler.KEY_RTIEXEC) == false )
		{
			throw new ConfigurationException(
				"Missing required RTIExec instance [key: rtiexec]" );
		}
		
		// get the rti execution //
		try
		{
			this.rtiExecution = (RTIExec)properties.get( ConsoleMessageHandler.KEY_RTIEXEC );
			this.consoleRequestSink = (MessageSink)properties.get(ConsoleMessageHandler.KEY_SINK);
			if (this.consoleRequestSink == null)
			{
				throw new ConfigurationException("Console request sink is missing!");
			}
		}
		catch( ClassCastException cce )
		{
			throw new ConfigurationException(
				"Missing required RTIExec instance [key: rtiexec]: ClassCastException" );
		}
	}
	
	/**
	 * @return Returns the value of rtiExecution.
	 */
	public RTIExec getRtiExecution()
	{
		return rtiExecution;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// HELPER METHODS ////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Puts a <code>SuccessMessage</code> (see the commons) in the context. 
	 */
	protected void success( MessageContext context )
	{
		context.success();
	}
	
	/**
	 * Puts a <code>SuccessMessage</code> (see the commons) in the context and puts the given
	 * properties in it. 
	 */
	protected void success( MessageContext context, Bag<String,Object> properties )
	{
		context.success( properties );
	}
	
	/**
	 * Puts a <code>SuccessMessage</code> (see the commons) in the context and binds the given
	 * object to the <code>"result"</code> key in the properties.
	 */
	protected void success( MessageContext context, Object result )
	{
		Bag<String,Object> properties = new Bag<String,Object>();
		properties.put( "result", result );
		context.success( properties );
	}

	/**
	 * Sets an error response for the context with the given message. The message is wrapped up in
	 * an RTIinternalError.
	 */
	protected void error( MessageContext context, String message )
	{
		context.error( new RTIinternalError(message) );
	}
	
	/**
	 * Sets an error response for the context embedding the given cause
	 */
	protected void error( MessageContext context, Throwable cause )
	{
		context.error( cause );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
