/*
 *   Copyright 2009 The Portico Project
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
package org.portico.utils.messaging;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The {@link MessageChain} class provides a simple implementation of a chain-like structure,
 * linking together a number of handlers into a single, logical handler. When the chain is invoked,
 * the message is passed to each of the contained handlers in order. A chain contains an internal
 * list of handlers, the order of which depends on the relative
 * {@link IMessageHandler#getPriority() priorities} of the handlers.
 * <p/>
 * The {@link MessageChain} class implements {@link IMessageHandler}, and as such, can be inserted
 * anywhere an instance of {@link IMessageHandler} can be. This includes being embedded into other
 * chains.
 * <p/>
 * <b>Veto Exceptions</b>
 * <p/>
 * Message chains will not explicitly handle veto exceptions, it will just let them bubble up to
 * the {@link MessageSink}. If they were handled locally, in situations where a sink contains a
 * chain of handlers, and one of those is a chain, then an exception in the inner chain would not
 * end processing, it would only end processing for the handlers in the inner chain.
 */
public class MessageChain extends AbstractMessageHandler implements IMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** Just share a single logger */
	private Logger logger = LogManager.getFormatterLogger( "portico.container" );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ArrayList<IMessageHandler> handlers;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public MessageChain( String name )
	{
		super( name );
		this.handlers = new ArrayList<IMessageHandler>();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Passes the context on to each {@link IMessageHandler} in the chain. If any of them throw
	 * an exception, this stops the others receiving the message.
	 */
	public void process( MessageContext context ) throws Exception
	{
		for( IMessageHandler handler : handlers )
		{
			if( logger.isTraceEnabled() )
				logger.trace( "[chain] Process: message="+context.getRequest().getClass().getSimpleName()+
				              ", handler="+handler.getClass().getCanonicalName() );

			handler.process( context );
		}
	}

	/**
	 * Insert the given handler into the chain. Its position in the chain will be depenant on its
	 * priority. Those with a higher priority will be placed at the front of the chain. If there
	 * are other handlers with equal priority, it will be added behind all those that already exist
	 * for that level.
	 */
	public void addHandler( IMessageHandler handler )
	{
		// find where in the list the handler belongs. loop through until we find
		// a place where the priority of the incoming handler is greater than that
		// of an existing handler. if this never happens, we default to the index
		// equal to the size of this list, inserting the element at the end
		int incomingPriority = handler.getPriority();
		int locationToInsert = handlers.size();
		for( int i = 0; i < handlers.size(); i++ )
		{
			IMessageHandler currentHandler = handlers.get( i );
			if( incomingPriority > currentHandler.getPriority() )
			{
				locationToInsert = i;
				break;
			}
		}
		
		handlers.add( locationToInsert, handler );
	}
	
	/**
	 * Remove the given handler from the chain. If the handler does exist and is successfully
	 * removed from the chain, it is returned. If it exists, but the remove fails (for whatever
	 * reason), null will be returned. If the handler does not exist with in the chain, null is
	 * returned. 
	 *
	 * @param handler The handler to remove
	 * @return The handler that was removed from the chain, or null
	 */
	public IMessageHandler removeHandler( IMessageHandler handler )
	{
		if( handlers.remove(handler) )
		{
			return handler;
		}
		else
		{
			// nothing was removed, return null
			return null;
		}
	}
	
	/**
	 * Removes and returns the first found handler with the given name. If there are multiple
	 * handlers with the given name, only the first is removed. If there are no handlers with
	 * the given name, null is returned.
	 */
	public IMessageHandler removeHandler( String handlerName )
	{
		// have to separate this out so we can avoid a ConcurrentAccessException
		IMessageHandler handlerToRemove = null;
		for( IMessageHandler handler : handlers )
		{
			if( handler.getName().equals(handlerName) )
			{
				handlerToRemove = handler;
				break;
			}
		}

		return removeHandler( handlerToRemove );
	}

	/**
	 * Same as {@link #getHandler(String)} but works off index rather than name
	 */
	public IMessageHandler getHandler( int index )
	{
		return handlers.get( index );
	}
	
	/**
	 * Returns the first handler found with the given name. If there are multiple handlers with
	 * the name, the first is returned. If there are no contained handlers with the given name,
	 * null is returned.
	 */
	public IMessageHandler getHandler( String handlerName )
	{
		// have to separate this out so we can avoid a ConcurrentAccessException
		for( IMessageHandler handler : handlers )
		{
			if( handler.getName().equals(handlerName) )
			{
				return handler;
			}
		}
		
		return null;
	}
	
	/**
	 * Get a list of all the {@link IMessageHandler IMessageHandler}'s in the chain. NOTE:
	 * this list is a copy of the underlying collection, modifications made to it will not be
	 * reflected in the chain.
	 */
	public List<IMessageHandler> getAllHandlers()
	{
		return new ArrayList<IMessageHandler>( this.handlers );
	}

	/**
	 * Returns the number of handlers contained within this chain 
	 */
	public int size()
	{
		return handlers.size();
	}
	
	/**
	 * Empty this chain of ALL handlers. 
	 */
	public void clear()
	{
		this.handlers.clear();
	}

	/**
	 * Returns string in the form "[firstHandlerName(8), secondHandlerName(5), ...]". The numbers
	 * represent the {@link IMessageHandler#getPriority() handler priority}.
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder( 64 );
		builder.append( "[" );
		for( int i = 0; i < handlers.size(); i++ )
		{
			IMessageHandler current = handlers.get( i );
			builder.append( current.getName() );
			builder.append( "(" );
			builder.append( current.getPriority() );
			builder.append( ")" );
			
			if( (i+1) < handlers.size() )
				builder.append( ", " );
		}
		
		builder.append( "]" );
		return builder.toString();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
