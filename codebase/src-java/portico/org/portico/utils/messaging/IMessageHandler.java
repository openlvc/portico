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

import java.util.Map;

import org.portico.lrc.LRCProperties;
import org.portico.lrc.compat.JConfigurationException;

/**
 * A message handler is designed to consume and process {@link PorticoMessage} subclass objects,
 * performing any appropraite actions (whether that be doing nothing, tranforming the message
 * somehow, taking some action with the information contained within the message, filling out a
 * {@link ResponseMessage}, etc...).
 * <p/>
 * To be considered valid, a message handler must both implement this interface and declare the
 * {@link MessageHandler} annotation (providing all the necessary configuration information).
 * <p/>
 * <b>RECOMMENDED:</b> Unless you have an excellent reason, it is *STRONGLY* recommended that all
 * your message handlers extend the {@link org.portico.lrc.LRCMessageHandler} class. It provides
 * a number of useful helper methods and during initialization it will cache locally a number of
 * useful LRC resources for easy access.
 * <p/>
 * <b>NOTE:</b> All {@link IMessageHandler} implementations should have either <b>NO CONSTRUCTOR</b>
 * or a <b>PUBLIC, NO-ARG CONSTRUCTOR</b>. This is due to the fact that handler instances will
 * generally be created through reflection (with the name of the implementation being obtained from
 * a configuration file).
 */
public interface IMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * The handler should execute all its initialization code in this method. It can be passed
	 * a {@link Map} instance containing whatever necessary configuration data the 
	 * application using the messaging framework thinks it needs. Included in the properties
	 * at all times will be a link to the {@link MessageSink} the handler is being added to under
	 * the key {@link LRCProperties#KEY_LRC}.
	 * <p/>
	 * If a fatal error occurs during configuration, the handler should throw a
	 * {@link JConfigurationException} and this will cause the messaging framework to stop
	 * processing.
	 */
	public void initialize( Map<String,Object> properties ) throws JConfigurationException;
	
	/**
	 * Process the message information found in the given {@link MessageContext} and take an
	 * appropriate course of action. If an error occurs, an exception should be thrown. You can
	 * declare your handler to throw application specific checked-exceptions (or to throw no
	 * exceptions at all).
	 * 
	 * @param context The {@link MessageContext} that contains the information to be processed.
	 */
	public void process( MessageContext context ) throws Exception;

	/**
	 * Each handler must have a name. Return the name. 
	 */
	public String getName();
	
	/**
	 * Set the name for this handler. If the name is provided in the {@link MessageHandler}
	 * annotation, this method will be automatically called by the framework. If not, the
	 * class should provide a default name. 
	 */
	public void setName( String name );
	
	/**
	 * Returns true if this handler can be extended into a {@link MessageChain} if another
	 * handler wishes to handle the same message type. False if not. If it can be augmented,
	 * the relative position of the handlers is decided by their priority.
	 */
	public boolean isAugmentable();
	
	/**
	 * Set the augmentable status for this handler. (see {@link #isAugmentable()})
	 */
	public void setAugmentable( boolean augmentable );
	
	/**
	 * If the handler {@link #isAugmentable()} and is extended into a chain, the relative position
	 * of the handlers depends on their priority. The higher the priority, the earlier in the
	 * chain they will appear. This value should be between 1 and 10 (anything over 10 is assumed
	 * to be 10 and anything under 1 is assumed to be 1). This value should be provided in the
	 * {@link MessageHandler} annotation (defaults to 5) and this method will be called by the
	 * framework.
	 */
	public int getPriority();
	
	/**
	 * Sets the priority for the handler, see {@link #getPriority()} for more information.
	 */
	public void setPriority( int priority );
}
