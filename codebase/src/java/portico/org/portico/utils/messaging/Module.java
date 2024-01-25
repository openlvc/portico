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

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.utils.annotations.AnnotationLocator;

/**
 * A {@link Module} collects together {@link IMessageHandler} classes so that they can be stored
 * as a single unit and applied to a {@link MessageSink} or group of sinks as a single unit. Each
 * {@link Module} has a name that a {@link IMessageHandler} should specify in its 
 * {@link MessageHandler} annotation to associate it with the module. You can apply a set of
 * {@link MessageSink}s to a Module and have each contained handler be added to the appropriate
 * sinks.
 */
public class Module
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private Logger logger;
	private Set<Class<? extends IMessageHandler>> handlers;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected Module( String name )
	{
		this.name = name;
		this.logger = LogManager.getFormatterLogger( "portico.container" );
		this.handlers = new HashSet<Class<? extends IMessageHandler>>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Gets the name of this {@link Module}. To associate itself with a particular module, a
	 * message handler needs to include this name in its {@link MessageHandler#modules()}
	 * annotation declaration.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Returns the number of handlers that are linked to this module.
	 */
	public int size()
	{
		return this.handlers.size();
	}
	
	/**
	 * Returns an <i>unmodifiable</i> set of the handler classes associated with this module.
	 */
	public Set<Class<? extends IMessageHandler>> getAllHandlers()
	{
		return Collections.unmodifiableSet( handlers );
	}
	
	/**
	 * If the provided module has the same name as the local module, all the handlers from it will
	 * be added to the local module. This method is used to combine handlers that are spread across
	 * multiple jarfiles/locations but are part of a single logical module.
	 */
	public void combine( Module otherModule )
	{
		if( otherModule.getName().equals(this.name) == false )
			return;
		
		this.handlers.addAll( otherModule.handlers );
	}
	
	/**
	 * Adds the given handler class to this module. If the handler class is null, or doesn't
	 * declare the {@link MessageHandler} annotation, an exception will be thrown. 
	 */
	public void addHandler( Class<? extends IMessageHandler> handler ) throws MessagingException
	{
		if( handler == null )
			throw new MessagingException( "Can't add null class to a Module" );
		
		// make sure the handler class declares the MessageHandler annotation
		// this will throw an exception if it is missing
		getHandlerAnnotation( handler );
		
		// if we get here, we're all good
		this.handlers.add( handler );
	}
	
	/**
	 * Calls {@link #addHandler(Class)} for each handler class (see the javadoc for that method
	 * for more details). 
	 */
	public void addHandlers( Collection<Class<? extends IMessageHandler>> handlers )
		throws MessagingException
	{
		for( Class<? extends IMessageHandler> clazz : handlers )
			addHandler( clazz );
	}

	/**
	 * Removes the given message handler class from the module. If this is successful, the class
	 * itself is returned, if not, null is returned.
	 */
	public Class<? extends IMessageHandler> removeHandler(Class<? extends IMessageHandler> handler)
	{
		if( this.handlers.remove(handler) )
			return handler;
		else
			return null;
	}
	
	/**
	 * Create a new instance of each of the handler classes contained in the module and adds them
	 * to the appropriate {@link MessageSink}s from the set given to this method as a parameter.
	 * <p/>
	 * <b>Which handlers are applied to which sinks?</b>
	 * <p/>
	 * For each {@link IMessageHandler} class in the {@link Module}, the declared
	 * {@link MessageHandler} annotation is located. The {@link MessageSink}s that this handler
	 * wants to be applied to are extracted from the annotation and then located in the group of
	 * sinks (by name) that were passed to this call. If the handler annotation declares a sink
	 * that doesn't exist in the given set, an exception is thrown.
	 * <p/>
	 * Each handler can be registered multiple times with a single {@link MessageSink} (once for
	 * each of the {@link PorticoMessage} types it declared. For each sink a handler wants to be
	 * registered with, a new instance of the handler class will be created and registered using
	 * all the message types it declares (in {@link MessageHandler#messages()}). To repeat, for
	 * each <b>sink</b> a new handler instance is created, but, that same instance is used more
	 * than once (for the sink) if the handler wants to handle more than one message type. Thus,
	 * the same handler can be registered multiple times with a single sink, but if the handler
	 * declared multiple sinks, there will be a separate instance for each sink. Gots it?
	 * <p/>
	 * If the {@link MessageHandler} indicates that it is a {@link MessageHandler#global() global}
	 * handler, the {@link MessageHandler#messages() messages} are ignored and a new instance is
	 * created and registered with the sink only as a global handler.
	 * <p/>
	 * <b>Restricting Handlers With Keywords</b>
	 * <p/>
	 * In the {@link MessageHandler} annotation, each handler can specify a set of keywords that
	 * must be provided before applying it to the given set of sinks. This allows components to
	 * provide some form of identification (perhaps a name or a list of capabilities) and for
	 * handlers within a module to opt-in or out of being applied. If at least one of the requested
	 * keywords provided by the handler does not match at least one of the keywords provided to the
	 * call, that handler will not be applied. If the handler provides no keywords, or there are
	 * no keywords provided to the call, then all handlers will be applied (no keywords is
	 * effectively the same as saying "all keywords").
	 * 
	 * @param sinks The group of sinks that the handlers contained in this {@link Module} can be
	 *              applied to.
	 * @param keywords The set of keywords that should match up with the
	 *                 {@link MessageHandler#keywords() Message Handler keywords} if the handler is
	 *                 to be applied to the sinks (see above).
	 * @throws MessagingException If a handler wants a {@link MessageSink} that isn't in the set
	 * that was passed. If the {@link IMessageHandler} class can't be instantiated. If there is
	 * a problem registering a handler with a sink.
	 * @return The set of handlers that were successfully applied to at least one of the sinks
	 */
	public Set<Class<?>> apply( MessageSink[] sinks, String[] keywords, Map<String,Object> properties )
		throws MessagingException
	{
		// make sure the keywords are in a form we can work with
		if( keywords == null )
			keywords = new String[]{};

		logger.debug( "Apply Module ["+name+"]: sinks="+sinksToNames(sinks)+
		              ", keywords="+Arrays.toString(keywords) );
		
		// turn the given set of message sinks into a map so we can reference them easily by name
		HashMap<String,MessageSink> sinkMap = new HashMap<String,MessageSink>();
		for( MessageSink sink : sinks )
			sinkMap.put( sink.getName(), sink );

		// for each handler class, find out what message sinks it is interested in and
		// find each of those sinks in those given to the method. Add a new instance of
		// the handler to each sink
		HashSet<Class<?>> appliedHandlers = new HashSet<Class<?>>();
		for( Class<? extends IMessageHandler> handlerClass : handlers )
		{
			MessageHandler annotation = getHandlerAnnotation( handlerClass );
			String[] targetSinks = annotation.sinks();

			// check for the appropriate keywords (if any are specified)
			if( keywords.length > 0 && annotation.keywords().length > 0 )
			{
				// if we don't have a keyword match, skip to the next handler
				if( keywordMatch(keywords,annotation.keywords()) == false )
				{
					if( logger.isTraceEnabled() )
					{
						logger.trace( "Skip: ["+className(handlerClass)+
						              "], no keyword match: provided="+
						              Arrays.toString(keywords)+", annotation="+
						              Arrays.toString(annotation.keywords()) );
					}
					continue;
				}
			}

			// we either have a keyword match, or we don't need to search for one, try and
			// find the sink, if successful, try to apply the handler class to it
			for( String targetSinkName : targetSinks )
			{
				MessageSink targetSink = sinkMap.get( targetSinkName );
				if( targetSink == null )
				{
					throw new MessagingException( "Can't locate sink [" + targetSinkName +
					                              "]: required by handler " + handlerClass );
				}
				
				applyHandlerToSink( handlerClass,
				                    targetSink,
				                    annotation.messages(),
				                    annotation.global(),
				                    properties );
				
				appliedHandlers.add( handlerClass );
			}
		}
		
		return appliedHandlers;
	}
	
	/**
	 * The same as {@link #apply(MessageSink[], String[], Map)} except that no
	 * additional information will be given to the handlers when they are created.
	 * 
	 * @return The set of handlers that were successfully applied to at least one of the sinks
	 */
	public Set<Class<?>> apply( MessageSink[] sinks, String[] keywords ) throws MessagingException
	{
		return apply( sinks, keywords, null );
	}
	
	/**
	 * The same as {@link #apply(MessageSink[], String[], Map)} except that no
	 * keywords will be specified, meaning that it will attempt to apply every handler to the group
	 * of given sinks. No properties will also be specified, meaning no additional information will
	 * be given to the handlers when they are created.
	 * 
	 * @return The set of handlers that were successfully applied to at least one of the sinks
	 */
	public Set<Class<?>> apply( MessageSink... sinks ) throws MessagingException
	{
		return apply( sinks, new String[]{}, null );
	}
	
	public String toString()
	{
		return name + " (" + handlers.size() + " handlers)";
	}
	
	/**
	 * Generates a longer, far more details string about the handlers in the Module
	 */
	public String toDetailedString()
	{
		StringBuilder builder = new StringBuilder( name );
		builder.append( " (" );
		builder.append( handlers.size() );
		builder.append( " handlers)\n" );
		
		for( Class<? extends IMessageHandler> clazz : handlers )
		{
			builder.append( "\tHandler: " );
			builder.append( clazz.getCanonicalName() );
			
			MessageHandler handler = Module.getMessageHandlerAnnotation( clazz );
			
			// keywords
			builder.append( "\n\t\t(keywords)    " );
			builder.append( listafy(handler.keywords()) );

			// sinks
			builder.append( "\n\t\t(sinks)       " );
			builder.append( listafy(handler.sinks()) );

			builder.append( "\n\t\t(messages)    " );
			builder.append( listafy(handler.messages()) );

			// modules 
			builder.append( "\n\t\t(modules)     " );
			builder.append( listafy(handler.modules()) );
			
			// global, augmentable, priority
			builder.append( "\n\t\t(global)      " );
			builder.append( handler.global() );
			builder.append( "\n\t\t(augmentable) " );
			builder.append( handler.augmentable() );
			builder.append( "\n\t\t(priority)    " );
			builder.append( handler.priority() );
			
			//builder.append( Module.getMessageHandlerAnnotation(clazz) );
			builder.append( "\n" );
		}
		
		return builder.toString();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Private Helper Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Convert the given array of MessageSinks into a String of the format [value1,value2,...]
	 * where "value" is the name of the message sink.
	 */
	private String sinksToNames( MessageSink[] sinks )
	{
		StringBuilder builder = new StringBuilder( "[" );
		for( int i = 0; i < sinks.length; i++ )
		{
			builder.append( sinks[i].getName() );
			if( sinks.length-1 == i )
				builder.append( "]" );
			else
				builder.append( "," );
		}
		return builder.toString();
	}
	
	/**
	 * Converts an array of strings to a single string separated by ","
	 */
	private String listafy( String... values )
	{
		if( values == null || values.length == 0 )
			return "none";
		
		StringBuilder builder = new StringBuilder( values[0] );
		for( int i = 1; i < values.length; i++ )
		{
			builder.append( "," );
			builder.append( values[i] );
		}
		
		return builder.toString();
	}
	
	/**
	 * Converts an array of classes to a single string of all the names separated by ","
	 */
	private String listafy( Class<?>... values )
	{
		if( values == null || values.length == 0 )
			return "none";
		
		StringBuilder builder = new StringBuilder( values[0].getCanonicalName() );
		for( int i = 1; i < values.length; i++ )
		{
			builder.append( "," );
			builder.append( values[i].getCanonicalName() );
		}
		
		return builder.toString();		
	}
	
	/**
	 * Returns true if there is at least one String that is in both arrays. The strings are
	 * compared using <code>equalsIgnoreCase()</code>.
	 */
	private boolean keywordMatch( String[] providedKeywords, String[] annotationKeywords )
	{
		for( String provided : providedKeywords )
			for( String annotation : annotationKeywords )
				if( annotation.equalsIgnoreCase(provided) )
					return true;
	
		// we didn't find a match for any of the keywords, return false
		return false;
	}
	
	/**
	 * This method will take the given handler class, create a new instance and register it with
	 * the given message sink once for each of the message types in <code>messages</code>. However,
	 * if the <code>global</code> variable says that this hander is a global handler, the messages
	 * will be ignored and the handler will be registered as a global handler as appropriate.
	 * <p/>
	 * The given properties will be passed to each and every handler that is created via its
	 * {@link IMessageHandler#initialize(Map)} method. A new properties map will be used, so the
	 * original won't be modified.
	 * <p/>
	 * An exception will be thrown if there is a problem instantiating the handler class or
	 * registering it with the {@link MessageSink}.
	 * 
	 * @param handlerClass The class of the handler to create and register
	 * @param messageSink The {@link MessageSink} to register the handler with
	 * @param message Message that the handler supports
	 * @param global The value from the {@link MessageHandler} annotation for the class. If this is
	 *               {@link MessageSink.Global#NONE} then the handler is treated as a regular
	 *               handler. If it is anything else, then the messages are ignored and the handler
	 *               is registered as a global handler.
	 */
	private void applyHandlerToSink( Class<? extends IMessageHandler> handlerClass,
	                                 MessageSink messageSink,
	                                 Class<? extends PorticoMessage> message,
	                                 MessageSink.Global global,
	                                 Map<String,Object> givenProperties )
		throws MessagingException
	{
		// instantiate the handler
		IMessageHandler handler = null;
		try
		{
			handlerClass.getDeclaredConstructor().newInstance();
		}
		catch( Exception e )
		{
			throw new MessagingException( "Can't apply class [" + handlerClass.getCanonicalName() +
			                              "]: instantiation error", e );
		}
		
		// set its default status from the information in the MessageHandler annotation
		MessageHandler annotation = getHandlerAnnotation( handlerClass );
		handler.setPriority( annotation.priority() );
		handler.setAugmentable( annotation.augmentable() );
		handler.setName( annotation.name() );
		
		// if this is a global handler, add it to the appropriate chain (and in that
		// case, all the message types should be ignored anyway as they are not valid
		// in the global handler sense).
		if( global == MessageSink.Global.NONE /*non-global handler*/ )
		{
			// register it with the sink for each message type it wants
			messageSink.registerHandler( handler, message );
		}
		else
		{
			messageSink.registerGlobalHandler( handler, global );
		}
		
		// initialize the handler before we let it go
		// create a new properties map so we don't modify the original, populate it with
		// the system properties and then add in any of the given properties. add the message
		// sink before initializing the handler
		Map<String,Object> initializationProperties = getInitializationProperties( givenProperties );
		initializationProperties.put( MessageSink.KEY_MESSAGE_SINK, messageSink );
		handler.initialize( initializationProperties );
	}
	
	/**
	 * Fetch and return the {@link MessageHandler} annotation for the provided handler class. If it
	 * is not present, an exception is thrown. 
	 */
	private MessageHandler getHandlerAnnotation( Class<?> clazz ) throws MessagingException
	{
		MessageHandler annotation = clazz.getAnnotation( MessageHandler.class );
		if( annotation == null )
			throw new MessagingException( "MessageHandler annotation missing from " + clazz );
		else
			return annotation;
	}

	/**
	 * This method really only exists so that I can put a "SuppressWarnings" annotation on it
	 * because an unchecked warning comes up when using the easiest approach for getting the
	 * system properties into a map (casting them to a Hashtable)
	 */
	private Map<String,Object> getInitializationProperties( Map<String,Object> given )
	{
		Map<String,Object> properties = PorticoConstants.getSystemPropertiesAsMap();
		
		if( given != null )
			properties.putAll( given );
		
		return properties;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * If the property "messaging.qnames" is <code>true</code>, return the qualified name of the
	 * class, otherwise return the simple name.
	 */
	public static String className( Class<?> clazz )
	{
		if( "true".equalsIgnoreCase(System.getProperty("messaging.qnames")) )
			return clazz.getCanonicalName();
		else
			return clazz.getSimpleName();
	}
	
	/**
	 * This method will scan the jar file at the given location for all classes containing the
	 * {@link MessageHandler} annotation. It will then sort each of the found handlers into
	 * separate {@link Module}s which are then returned.
	 * <p/>
	 * Should handlers for a single module be spread across multiple jar files (quite likely), you
	 * can pass in a collection of existing modules that will be returned. If the given collection
	 * is null, this is automatically converted to an empty collection, so null is a valid value.
	 * Note that the original collection will NOT be altered. A new, updated collection will be
	 * returned.
	 * 
	 * @param fromPath The jar-file to scan for any classes that declare the
	 *                 {@link MessageHandler} annotation
	 * @param existing The collection of existing modules you want to update. Null will cause
	 *                 a new collection to be automatically created. Note that the origin
	 *                 collection will not be altered, a new, updated collection will be returned
	 */
	public static Collection<Module> findModules( URL location ) throws JConfigurationException
	{
		try
		{
			// locate all the classes with the annotation
			Set<Class<?>> classes =
				AnnotationLocator.locateClassesWithAnnotation( MessageHandler.class, location );
			// sort them into modules
			return sortIntoModules( classes );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Error scanning path ["+location+
			                                   "] for MessageHandler annotations" );
		}
	}
	
	/**
	 * For each of the given classes, group them into modules. Each {@link MessageHandler}
	 * annotation will declare the name/s of the module/s they want to be associated with. This
	 * method will create each of these modules and add the handler classes to them. The set of
	 * all created modules is returned.
	 * <p/>
	 * If any of the located classes do *NOT* implement the {@link IMessageHandler} interface,
	 * an exception will be thrown.
	 * 
	 * @param handlerClasses The set of handler classes to sort into modules
	 * @throws JConfigurationException If any of the located message handlers don't declare the
	 *                                 {@link IMessageHandler} interface.
	 */
	private static Collection<Module> sortIntoModules( Set<Class<?>> handlerClasses )
		throws JConfigurationException
	{
		// convert the collection into a map for easy local module lookup
		HashMap<String,Module> modules = new HashMap<String,Module>();
		
		// for each located handler class, see what modules it wants to be a part of. Locate
		// the module inside the map (or create it if it hasn't previously been created) and
		// register the handler with that module.
		for( Class<?> clazz : handlerClasses )
		{
			// find out which modules the handler wants to be associated with
			MessageHandler annotation = Module.getMessageHandlerAnnotation( clazz );
			Class<? extends IMessageHandler> handlerClass = clazz.asSubclass(IMessageHandler.class);
			
			for( String moduleName : annotation.modules() )
			{
				// does a module of this name already exist? if so, add to it, if not
				// create the module and then add the handler class to it
				if( modules.containsKey(moduleName) )
				{
					modules.get(moduleName).addHandler( handlerClass );
				}
				else
				{
					Module newModule = new Module( moduleName );
					newModule.addHandler( handlerClass );
					modules.put( moduleName, newModule );
				}
			}
		}
		
		return modules.values();
	}
	
	/**
	 * Checks that the given class is an {@link IMessageHandler} and if it is, located and returns
	 * the {@link MessageHandler} annotation for it. If the class isn't an {@link IMessageHandler},
	 * or the annotation can't be found, an exception will be thrown.
	 */
	private static MessageHandler getMessageHandlerAnnotation( Class<?> clazz )
		throws JConfigurationException
	{
		// check that the class is an IMessageHandler
		if( IMessageHandler.class.isAssignableFrom(clazz) == false )
		{
			throw new JConfigurationException( "Class [" + clazz.getCanonicalName() +
			    "] does not implement IMessageHandler" );
		}
		
		// convert the class to a handler class
		return clazz.asSubclass(IMessageHandler.class).getAnnotation( MessageHandler.class );
	}
}
