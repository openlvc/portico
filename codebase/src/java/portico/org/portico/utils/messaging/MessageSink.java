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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.portico.utils.messaging.Module.className;

/**
 * A {@link MessageSink} provides a super-structure for message processing. It is the main logical
 * unit that drives message processing in the messaging framework.
 * <p/>
 * <b>Global Handlers</b>
 * <p/>
 * A sink may contain any number of <i>global {@link IMessageHandler}s</i>. These handlers are
 * passed <b>every</b> message given to the sink, regardless of message type. Global handlers are
 * arranged as {@link MessageChain}s. A global handler can be registered in one of two chains:
 * the <b><code>PREPROCESS</code></b> chain or the <b><code>POSTPROCESS</code></b> chain. Handlers
 * in the <code>PREPROCESS</code> chain are passed a message before it is given to any registered
 * handler. Handlers in the <code>POSTPROCESS</code> chain are passed a message after it has been
 * processed by its registered handler (unless the registered handler threw an exception).
 * <p/>
 * Other options for global handlers are <code>BOTH</code> which will see a handler added to both
 * chains, and <code>NONE</code> which denotes that a handler ISN'T a global handler. This is the
 * default stance for handlers and is needed so that the {@link MessageHandler#global()} attribute
 * of the annotation can have a default value that says "not global".
 * <p/>
 * <b>Registered Handlers</b>
 * <p/>
 * Registered handlers are where the meat of the processing occurs. A handler can be registered
 * with the sink as the handler responsible for a particular class of {@link PorticoMessage}. When
 * requests of that class arrive, that handler is given it to process. 
 * <p/>
 * <b>The Default Handler</b>
 * <p/>
 * If there is no registered handler for a message, it is given to the default handler. By default,
 * the default handler is an instance of {@link DefaultHandler}. You can replace this with the
 * {@link #setDefaultHandler(IMessageHandler)} method.
 * <p/>
 * <b>Handler Augmentation</b>
 * <p/>
 * When a handler is registered, there may already be a registered handler for that same message
 * type. In this case, the handler can be "augmented". In this case, a new {@link MessageChain}
 * is created and the existing handler and the new handler are added to it. The chain then replaces
 * the existing handler as the registered handler for the message type.
 * <p/>
 * If the existing message type is already a chain, the new handler is just added to the chain.
 * <b>NOTE:</b> The position of handlers within a chain depends on each of their
 * {@link IMessageHandler#getPriority() priorities}. See the previous link for more information.
 * <p/>
 * If either of the handlers is marked as {@link IMessageHandler#isAugmentable() not augmentable},
 * then the attempt to augment the handlers will fail and nothing will change.
 * <p/>
 * <b>Veto Exceptions</b>
 * <p/>
 * When a {@link VetoException} is found during processing, the sink will stop any further
 * processing with one exception. If the exception happens in a regular handler, the global
 * post-processing handlers will still be given the opportunity to run. If a veto happens in
 * the global pre-processing chain, no regular or post-processing handlers will run. Before
 * returning processing after a veto, the sink will check to see if a response message has been
 * filled out. If one hasn't, it will <b><i>automatically mark the call as a success</i></b>.
 */
public class MessageSink extends AbstractMessageHandler
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	/** Enum outlinging the possible chains a glocal handler can be added to. The NONE option
	    is necessary so that the {@link MessageHandler} annotation can have a default (which
	    i */
	public enum Global{ PREPROCESS, POSTPROCESS, BOTH, NONE };
	
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The key under which the message sink is bound in the properties given to a handler that
	    is being initialized */
	public static final String KEY_MESSAGE_SINK = "messaging.sink";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HashMap<Class<? extends PorticoMessage>, IMessageHandler> handlers;
	private IMessageHandler defaultHandler;
	private MessageChain gPreprocess;
	private MessageChain gPostprocess;
	
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public MessageSink( String name )
	{
		super( name );
		
		this.handlers = new HashMap<Class<? extends PorticoMessage>, IMessageHandler>();
		this.defaultHandler = new DefaultHandler( this.name );
		this.gPreprocess = new MessageChain( this.name + ".preprocess" );
		this.gPostprocess = new MessageChain( this.name + ".postprocess" );
		this.logger = LogManager.getFormatterLogger( "portico.container.sink." + this.name );
	}
	
	public MessageSink( String name, Logger logger )
	{
		this( name );
		if( logger != null )
			this.logger = logger;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This method will trigger the processing of a message. The processing order is as follows:
	 * 
	 * <ul>
	 * 	<li>First, all the <b>global</b> handlers with the role {@link Global#PREPROCESS} are
	 *      invoked. The order depends on the relative {@link IMessageHandler#getPriority()
	 *      priorities} of the contained handlers.</li>
	 * 	<li>Secondly, the <b>general</b> (or "registered") handler associated with type of the
	 *      request message (its class) will be invoked. If no handler has ever been
	 *      associated with this message type, it will be given to the {@link DefaultHandler}.</li>
	 * 	<li>Finally, all the <b>global</b> handlers with the role {@link Global#POSTPROCESS} are
	 *      invoked. Again, the order depends on the relative {@link IMessageHandler#getPriority()
	 *      priorities} of the contained handlers.</li>
	 * </ul>
	 * 
	 * @param context The context containing the message to process
	 * @throws MessagingException If there is an error or one of the handlers throws an exception
	 */
	public void process( MessageContext context ) throws Exception
	{
		///////////////////////////////////////////////////
		// 1. pass to all the global preprocess handlers //
		///////////////////////////////////////////////////
		try
		{
			if( logger.isTraceEnabled() )
				logger.trace( "[process] Global.PREPROCESS: message="+
				              context.getRequest().getIdentifier() );
			
			this.gPreprocess.process( context );
		}
		catch( VetoException ve )
		{
			if( logger.isTraceEnabled() )
				logger.trace( "Veto!" );

			if( context.getResponse() == null )
				context.success();
			return;
		}
		
		////////////////////////////////////
		// 2. pass to the general handler //
		////////////////////////////////////
		IMessageHandler leHandler = this.handlers.get( context.getRequest().getClass() );
		if( leHandler != null )
		{
			try
			{
				if( logger.isTraceEnabled() )
					logger.trace( "[process]: message="+context.getRequest().getIdentifier()+
					              ", handler="+leHandler.getClass().getCanonicalName() );
				
				// we found the handler, pass the message to it
				leHandler.process( context );
			}
			catch( VetoException ve )
			{
				// swallow this one up and let the post-process global handlers run
				if( context.getResponse() == null )
					context.success();
				
				if( logger.isTraceEnabled() )
					logger.trace( "Veto!" );
			}
		}
		else
		{
			// we didn't find a matching handler, doh! it'll log for us
			this.defaultHandler.process( context );
		}
		
		////////////////////////////////////////////////////
		// 4. pass to all the global postprocess handlers //
		////////////////////////////////////////////////////
		try
		{
			if( logger.isTraceEnabled() )
				logger.trace( "[process] Global.POSTPROCESS: message="+
				              context.getRequest().getIdentifier() );

			this.gPostprocess.process( context );
		}
		catch( VetoException ve )
		{
			if( logger.isTraceEnabled() )
				logger.trace( "Veto!" );

			if( context.getResponse() == null )
				context.success();
			return;
		}
	}

	
	/////////////////////////////////////////////////////////////
	////////////////// Regular Handler Methods //////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Register the handler for the given message type. If a handler already exists, the sink
	 * will attempt to augment it (see {@link MessageSink class level documentation} for a
	 * discussion of this process). If either the handler or the message is null, an exception
	 * will be thrown.
	 * 
	 * @param handler The handler that you want to handle incoming message of the given type
	 * @param forMessage The message type you want the given handler to handle
	 */
	public void registerHandler( IMessageHandler handler,
	                             Class<? extends PorticoMessage> forMessage )
		throws MessagingException
	{
		if( handler == null || forMessage == null )
			throw new MessagingException( "Can't register null handler or message type" );
		
		// is there an existing handler for this specified message type?
		IMessageHandler existing = handlers.get( forMessage );
		if( existing == null )
		{
			// there is no existing handler, just register the message and move on with life
			handlers.put( forMessage, handler );
			if( logger.isTraceEnabled() )
			{
				logger.trace( "Registered: handler="+className(handler.getClass())+
				              ", message="+className(forMessage) );
			}
			return;
		}
		else
		{
			augment( existing, handler, forMessage );
		}
	}
	
	/**
	 * If a user attempts to register a handler for a message type that already has a handler
	 * associated with it, augmentation will need to be attempted. In this case, the incoming
	 * handler needs to be augmented with the existing handler.
	 * <p/>
	 * For augmentation to take place, both handlers must be OK with it. First, the incoming
	 * handler is checked to see if it is {@link IMessageHandler#isAugmentable() augmentable}.
	 * If it isn't, an exception is thrown. Following this, the existing handler is also checked,
	 * and again, if it is NOT augmentable, an exception is thrown.
	 * <p/>
	 * Assuming that both handlers ARE augmentable, this method will proceed. If the existing
	 * handler is already a {@link MessageChain} then the incoming handler is added to it. The
	 * location within the chain that the handler takes will depend on its
	 * {@link IMessageHandler#getPriority() priority} relative to the other handlers already in
	 * the chain.
	 * <p/>
	 * If the existing handler is NOT a chain, a new {@link MessageChain} will be created and
	 * each handler will be added (existing first, then the incoming one). This method will then
	 * replace the existing handler with the chain as the handler registered for the given message
	 * type.
	 * 
	 * @param existing The handler that is already registered for the message type
	 * @param incoming The new handler that wants to be registered for the message type
	 * @throws MessagingException If either of the handlers doesn't want to be augmented
	 */
	private void augment( IMessageHandler existing,
	                      IMessageHandler incoming,
	                      Class<? extends PorticoMessage> message )
		throws MessagingException
	{
		// check to see if the incoming handler is augmentable
		if( incoming.isAugmentable() == false )
		{
			throw new MessagingException( "Can't register handler [" +
			                              className(incoming.getClass())+"] for message ["+
			                              className(message) +
			                              "]: Handler not augmentable and another handler exists" );
		}

		// check to see if the existing handler is OK with being augmented
		if( existing.isAugmentable() == false )
		{
			throw new MessagingException( "Can't register handler [" +
			                              className(incoming.getClass())+"] for message ["+
			                              className(message) +
			                              "]: Existing handler isn't augmentable" );
		}
		
		// is the current handler is already a chain, we'll add the handler directly
		// to it. if the existing handler is not a chain, we'll have to create a new
		// one and add both handlers to it before replacing the existing registration
		// with this new chain
		if( existing instanceof MessageChain )
		{
			((MessageChain)existing).addHandler( incoming );
		}
		else
		{
			MessageChain chain = new MessageChain( existing.getName() + "-augmented" );
			chain.addHandler( existing );
			chain.addHandler( incoming );
			handlers.put( message, chain );
			// log the augmentation
			logger.trace( "Augmented: for message [" + className(message) + "], added [" +
			              incoming.getName() + "] to end of chain [" + chain.getName() + "]" ); 
		}
	}

	/**
	 * Removes and returns the registered handler for the given message type. Returns null if there
	 * is no handler for that type.
	 */
	public IMessageHandler removeHandler( Class<? extends PorticoMessage> forMessage )
	{
		return handlers.remove( forMessage );
	}
	
	/**
	 * Returns the registered handler for the given message type. Returns null if there is no
	 * registered handler for that type.
	 */
	public IMessageHandler getHandler( Class<? extends PorticoMessage> forMessage )
	{
		return handlers.get( forMessage );
	}
	
	/**
	 * Returns <code>true</code> if there is a registered handler for the given message type.
	 * Also returns <code>true</code> if there is NO registered handler for the given message
	 * type (as that means it is safe to register a handler for the type). Returns
	 * <code>false</code> if there is an existing handler, but it is NOT augmentable.
	 */
	public boolean isAugmentable( Class<? extends PorticoMessage> forMessage )
	{
		IMessageHandler handler = handlers.get( forMessage );
		if( handler == null || handler.isAugmentable() )
			return true;
		else
			return false;
	}

	/**
	 * Each {@link MessageSink} has a default message handler that is passed all messages for
	 * which there is not a registered handler. This method will set the given handler as the
	 * default handler. If the given handler is null, the request is ignored.
	 */
	public void setDefaultHandler( IMessageHandler defaultHandler )
	{
		if( defaultHandler != null )
		{
			this.defaultHandler = defaultHandler;
			logger.trace( "Replaced default handler, new handler=" +
			              defaultHandler.getClass().getCanonicalName() );
		}
	}
	
	/////////////////////////////////////////////////////////////
	////////////////// Global Handler Methods ///////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Registers the given handler with either the global pre- or post-process {@link MessageChain}.
	 * The position of the handler within the chain depends on handlers
	 * {@link IMessageHandler#getPriority() priority} compared to those of the existing handlers.
	 * If the value for <code>globalChain</code> is {@link Global#BOTH}, that handler will be added
	 * to both chains. If it is {@link Global#NONE}, an exception will be thrown. 
	 */
	public void registerGlobalHandler( IMessageHandler handler, Global globalChain )
		throws MessagingException
	{
		switch( globalChain )
		{
			case PREPROCESS:
				gPreprocess.addHandler( handler );
				break;
			case POSTPROCESS:
				gPostprocess.addHandler( handler );
				break;
			case BOTH:
				gPreprocess.addHandler( handler );
				gPostprocess.addHandler( handler );
				break;
			case NONE:
				throw new MessagingException( "Not a global handler (Global.NONE): " + handler );
			default:
				break;
		}
		
		logger.trace( "[register] Global."+globalChain+", handler=" +
		              handler.getClass().getCanonicalName() );
	}

	/**
	 * Removes and returns the first handler with the given name in the global chain identified
	 * by the given argument. If there is more than one handler of the given name, only the first
	 * is removed. If there is no handler of the given name in the chain, null is returned. If the
	 * value for <code>globalChain</code> is {@link Global#NONE}, null wil be returned.
	 */
	public IMessageHandler removeGlobalHandler( String handlerName, Global globalChain )
	{
		switch( globalChain )
		{
			case PREPROCESS:
				return gPreprocess.removeHandler( handlerName );
			case POSTPROCESS:
				return gPostprocess.removeHandler( handlerName );
			case BOTH:
				gPreprocess.removeHandler( handlerName );
				return gPostprocess.removeHandler( handlerName );
			default:
				return null;
		}
	}
	
	/**
	 * Returns the first handler with the given name in the global chain identified
	 * by the given argument. If there is more than one handler of the given name, only the first
	 * is returned. If there is no handler of the given name in the chain, null is returned. If
	 * the <code>globalChain</code> value is {@link Global#BOTH} or {@link Global#NONE}, null will
	 * be returned.
	 */
	public IMessageHandler getGlobalHandler( String handlerName, Global globalChain )
	{
		switch( globalChain )
		{
			case PREPROCESS:
				return gPreprocess.getHandler( handlerName );
			case POSTPROCESS:
				return gPostprocess.getHandler( handlerName );
			default:
				return null;
		}
	}
	
	/**
	 * Returns a list of all the {@link IMessageHandler}s in the identified global chain. If the
	 * <code>globalChain</code> value is {@link Global#BOTH} or {@link Global#NONE}, null will be
	 * returned.
	 */
	public List<IMessageHandler> getGlobalHandlers( Global globalChain )
	{
		switch( globalChain )
		{
			case PREPROCESS:
				return gPreprocess.getAllHandlers();
			case POSTPROCESS:
				return gPostprocess.getAllHandlers();
			default:
				return null;
		}
	}
	
	////////////////////////////////////////////////////////////
	///////////////////// Message Methods //////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Returns true if the given message class is one that currently has a registered handler
	 * interested in handling it within the sink. 
	 *
	 * @param clazz The class for the message
	 * @return True if this sink will accept messages of the given type, false otherwsie
	 */
	public boolean isSupported( Class<? extends PorticoMessage> clazz )
	{
		return this.handlers.containsKey( clazz );
	}
	
	/**
	 * Get a set of all the message classes currenty supported by this sink. This only includes the
	 * classes that have general handlers associated with them. The returned set is NOT modifiable.
	 */
	public Set<Class<? extends PorticoMessage>> getSupportedMessages()
	{
		return Collections.unmodifiableSet( this.handlers.keySet() );
	}

	////////////////////////////////////////////////////////////
	///////////////////// Utility Methods //////////////////////
	////////////////////////////////////////////////////////////
	public void setLogger( Logger logger )
	{
		this.logger = logger;
	}
	
	public Logger getLogger()
	{
		return this.logger;
	}
	
	/**
	 * Removes all handlers from the sink and returns it to its default state (this includes
	 * resetting the default handler).
	 */
	public void clear()
	{
		this.handlers.clear();
		this.gPreprocess.clear();
		this.gPostprocess.clear();
		this.defaultHandler = new DefaultHandler( this.name );
		logger.trace( "Cleared Message Sink" );
	}
	
	/**
	 * Returns string in the format: "[sink: name=name, handlers=10, preprocess=2, postprocess=3]"
	 */
	public String toString()
	{
		StringBuilder builder = new StringBuilder( 128 );
		builder.append( "[sink: name=" );
		builder.append( this.name );
		builder.append( ", handlers=" );
		builder.append( handlers.size() );
		builder.append( ", preprocess=" );
		builder.append( gPreprocess.size() );
		builder.append( ", postprocess=" );
		builder.append( gPostprocess.size() );
		builder.append( "]" );
		
		return builder.toString();
	}
	
	/**
	 * Returns string in the following form:
	 * <pre>
	 * Message Sink: "name"
	 * Default Handler
	 *   (DEFAULT) handlerClass=com.lbf.blah
	 * Global Handlers (6)
	 *   (PREPROCESS)  name=handlerName, priority=5
	 *   (PREPROCESS)  name=handlerName, priority=5
	 *   (PREPROCESS)  name=handlerName, priority=5
	 *   (POSTPROCESS) name=handlerName, priority=5
	 *   (POSTPROCESS) name=handlerName, priority=5
	 *   (POSTPROCESS) name=handlerName, priority=5
	 * Registered Handlers (6)
	 *   (REGISTERED)  [message=class], handler=handlerName, priority=5, augmentable=true
	 *   (REGISTERED)  [message=class], handler=handlerName, priority=5, augmentable=true
	 *   (REGISTERED)  [message=class], handler=handlerName, priority=5, augmentable=true
	 *   (REGISTERED)  [message=class], handler=handlerName, priority=5, augmentable=true
	 *   (REGISTERED)  [message=class], handler=handlerName, priority=5, augmentable=true
	 *   (REGISTERED)  [message=class], handler=handlerName, priority=5, augmentable=true
	 * </pre>
	 */
	public String status()
	{
		String newline = System.getProperty( "line.separator" );
		StringBuilder builder = new StringBuilder( 2048 );
		
		// write the header
		builder.append( "Message Sink: name=" );
		builder.append( this.name );
		builder.append( newline );
		
		// write the default handler info
		builder.append( "Default Handler" );
		builder.append( newline );
		builder.append( "  (DEFAULT) class=" );
		builder.append( defaultHandler.getClass().getCanonicalName() );
		builder.append( newline );
		
		// write the global handler information
		builder.append( "Global Handlers (" );
		builder.append( gPreprocess.size() + gPostprocess.size() );
		builder.append( ")" );
		builder.append( newline );
		
		for( IMessageHandler handler : gPreprocess.getAllHandlers() )
		{
			builder.append( "  (PREPROCESS)  name=" );
			builder.append( handler.getName() );
			builder.append( ", priority=" );
			builder.append( handler.getPriority() );
			builder.append( newline );
		}
		
		for( IMessageHandler handler : gPostprocess.getAllHandlers() )
		{
			builder.append( "  (POSTPROCESS) name=" );
			builder.append( handler.getName() );
			builder.append( ", priority=" );
			builder.append( handler.getPriority() );
			builder.append( newline );
		}
		
		// write the registered handler data
		builder.append( "Registered Handlers (" );
		builder.append( handlers.size() );
		builder.append( ")" );
		builder.append( newline );
		for( Class<? extends PorticoMessage> messageType : handlers.keySet() )
		{	
			IMessageHandler handler = handlers.get( messageType );
			if( handler instanceof MessageChain )
			{
				builder.append( "  (REGISTERED)  [message=" );
				builder.append( messageType.getSimpleName() );
				builder.append( "], chain=" );
				builder.append( handler );
				builder.append( newline );
			}
			else
			{
    			builder.append( "  (REGISTERED)  [message=" );
    			builder.append( messageType.getSimpleName() );
    			builder.append( "], handler=" );
    			builder.append( handler.getName() );
    			builder.append( ", priority=" );
    			builder.append( handler.getPriority() );
    			builder.append( ", augmentable=" );
    			builder.append( handler.isAugmentable() );
    			builder.append( newline );
			}
		}

		return builder.toString();
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/////////////////////////////////////////////////////////////////////////////
	/////////////////// PRIVATE INNER CLASS: Default Handler ////////////////////
	/////////////////////////////////////////////////////////////////////////////
	public class DefaultHandler extends AbstractMessageHandler
	{
		private DefaultHandler( String sinkName )
		{
			super();
			this.setName( sinkName + ".default" );
		}
		
		/**
		 * Just logs a message (at WARN level) and returns
		 */
		public void process( MessageContext context ) throws MessagingException
		{
			logger.warn( "No handler for message [" + context.getRequest() + "], ignoring..." );
		}
	}
}
