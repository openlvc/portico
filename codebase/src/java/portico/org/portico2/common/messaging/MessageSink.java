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
package org.portico2.common.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;

public class MessageSink
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private Logger logger;

	private Map<MessageType,IMessageHandler> messageHandlers;
	private Set<MessageType> exclusive;
	private IMessageHandler defaultHandler;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MessageSink( String name, Logger logger )
	{
		this.name = name;
		this.logger = logger;
		
		this.messageHandlers = new HashMap<>();
		this.exclusive = new HashSet<>();
		this.defaultHandler = new DefaultHandler();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void process( MessageContext context )
	{
		IMessageHandler handler = messageHandlers.get( context.getRequest().getType() );
		if( handler == null )
			defaultHandler.process( context );
		else
			handler.process( context );
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Handler Management Methods   /////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * When called, it will loop through all its contained {@link IMessageHandler}s and call
	 * {@link IMessageHandler#configure(Map)}, passing the given properties.
	 * 
	 * @param properties The properties to pass to the handers
	 * @throws JConfigurationException If there is a problem configuring any of the handlers
	 */
	public void configure( Map<String,Object> properties ) throws JConfigurationException
	{
		for( IMessageHandler handler : messageHandlers.values() )
			handler.configure( properties );
	}
	
	/**
	 * Register the given handler to process messages of the specified {@link MessageType}.
	 * If there is already a handler for this message, a chain will be created that will cause
	 * each message to be passed to all handlers in the order in which they were added.
	 * 
	 * @param type The type of the message the handlers wants
	 * @param handler The handler to use
	 * @param JRTIinternalError There is a handler for the type that has registered as exclusive
	 */
	public void register( MessageType type, IMessageHandler handler )
		throws JRTIinternalError
	{
		if( messageHandlers.containsKey(type) )
		{
			// Has someone got exclusive access?
			if( exclusive.contains(type) )
			{
				throw new JRTIinternalError( "Cannot add hander [%s]: [%s] has exclusive access for messages of type [%s]",
				                             handler.getName(), type, messageHandlers.get(type).getName() );
			}

			// If the existing handler is a list, just extend it
			// If it is a single handler, turn it into a list
			IMessageHandler existing = messageHandlers.get( type );
			if( existing instanceof ListHandler )
				ListHandler.class.cast(existing).handlers.add( existing );
			else
				messageHandlers.put( type, new ListHandler(existing,handler) );
		}
		else
		{
			this.messageHandlers.put( type, handler );
		}
	}

	/**
	 * Register the given handler to process messages of the specified type. It will also give
	 * the handler exclusive access to the message type. If there is already another handler
	 * registered for the type, an exception will be thrown. If later on someone tries to add
	 * a handler for a type that already has an exclusive handler, an exception will be thrown
	 * then as well.
	 * 
	 * @param type The type of the message the handlers wants
	 * @param handler The handler to use
	 * @param JRTIinternalError There is already a handler registered for that message type, so we
	 *                          can't have exclusive access
	 */
	public void registerExclusive( MessageType type, IMessageHandler handler )
		throws JRTIinternalError
	{
		if( messageHandlers.containsKey(type) )
		{
			throw new JRTIinternalError( "Cannot give exclusive access of type [%s] to handler [%s]: another handler has already registered (%s)",
			                             type, handler.getName(), messageHandlers.get(type).getName() );
		}
		else
		{
			messageHandlers.put( type, handler );
			exclusive.add( type );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Private Inner Class: DefaultHandler   ////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	private class DefaultHandler implements IMessageHandler
	{
		@Override public String getName() { return "DefaultHandler"; }
		@Override public void configure( Map<String,Object> properties ){}
		@Override public void process( MessageContext context ) throws JRTIinternalError
		{
			logger.warn( "(sink: %s) IGNORE MESSSAGE. No handler for type: %s",
			             name, context.getRequest().getType() );
			
			// TODO Do we need to throw an exception?
			//throw new JRTIinternalError( "(sink: %s) No handler for type: %s",
			//                             name, context.getRequest().getType() );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Private Inner Class: ListHandler   ///////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This class is used whenever someone tries to register more than one handler for a type
	 * of message. It holds a list of handlers which it passes each message to in order. The
	 * order is the order in which the handlers were registered.
	 * <p/>
	 * If any of the handlers throws an exception, an exception will be placed into the message
	 * context response slot and processing will stop. The only exception to this is a
	 * 
	 */
	private class ListHandler implements IMessageHandler
	{
		private List<IMessageHandler> handlers;
		
		public ListHandler( IMessageHandler... handlers )
		{
			this.handlers = new ArrayList<>();
			for( IMessageHandler handler : handlers )
				this.handlers.add( handler );
		}
		
		@Override public String getName() { return "HandlerList"; }
		@Override public void configure( Map<String,Object> properties )
		{
			handlers.forEach( handler -> handler.configure(properties) );
		}

		@Override public void process( MessageContext context ) throws JRTIinternalError
		{
			for( IMessageHandler handler : handlers )
			{
				try
				{
					handler.process( context );
				}
				catch( VetoException ve )
				{
					if( logger.isTraceEnabled() )
					{
    					logger.trace( "Message [%s] veto'd by handler [%s]: %s",
    					              context.getRequest().getType(),
    					              handler.getName(),
    					              ve.getMessage() );
					}
					
					// Processing has been veto'd. If there is no response, fill with a success. 
					// The veto is an intentional action, not an error
					if( context.hasResponse() == false )
						context.success();
					return;
				}
			}
		}
	}
	
}
