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

import org.portico.lrc.compat.JConfigurationException;

/**
 * This class is an empty implementation that provides support for automatically handling the
 * recording of a handler name, augmentable status and priority. It provides an empty
 * implementation of the {@link #initialize(Map)} method as well. By extending this class,
 * a handler only needs to worry about implementing the {@link #process(MessageContext)} method
 * and the rest will be taken care of.
 * <p/>
 * <b>RECOMMENDED:</b> When using the messaging framework, it is recommended that you provide an
 * application-specific default handler that all other handlers will extend. This way, application
 * specific items can be placed in as protected members that all handlers can have automatically
 * set up for them. For example, the Portico RTI, there is a default LRCMessageHandler that has a
 * bunch of protected members (populated through the initialize() method) that contain links to the
 * various parts of the LRC so that all LRC handler subclasses can then have direct access to them
 * without having to implement the code to fetch and store those items themselves. This is a
 * recommended practice when using the messaging framework in your application.
 */
public abstract class AbstractMessageHandler implements IMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected String name = null;
	protected boolean augmentable = true;
	protected int priority = 5;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Sets the name of the handler to the non-qualified name of the class.
	 */
	protected AbstractMessageHandler()
	{
		this( null, true, 5 );
	}
	
	/**
	 * If the name is null, the non-qualified name of the class will be used.
	 * Augmentable defaults to true. Priority defaults to 5.
	 */
	protected AbstractMessageHandler( String name )
	{
		this( name, true, 5 );
	}
	
	/**
	 * If the name is null, the non-qualified name of the class will be used.
	 * Priority defaults to 5.
	 */
	protected AbstractMessageHandler( String name, boolean augmentable )
	{
		this( name, augmentable, 5 );
	}
	
	/**
	 * If the name is null, the non-qualified name of the class will be used.
	 */
	protected AbstractMessageHandler( String name, boolean augmentable, int priority )
	{
		if( name == null )
			this.name = getClass().getSimpleName();
		else
			this.name = name;
		
		this.augmentable = augmentable;
		setPriority( priority );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Not provided. User must provide this.
	 */
	public abstract void process( MessageContext context ) throws Exception;

	/**
	 * Empty implementation.
	 */
	public void initialize( Map<String,Object> properties ) throws JConfigurationException
	{
	}
	
	public String getName()
	{
		if( this.name != null )
			return this.name;
		else
			return this.getClass().getSimpleName();
	}
	
	public void setName( String name )
	{
		if( name == null || name.equals("<unknown>") )
			return;
		else
			this.name = name;
	}
	
	public boolean isAugmentable()
	{
		return this.augmentable;
	}
	
	public void setAugmentable( boolean augmentable )
	{
		this.augmentable = augmentable;
	}
	
	public int getPriority()
	{
		return this.priority;
	}
	
	public void setPriority( int priority )
	{
		if( priority < 1 )
			this.priority = 1;
		else if( priority > 10 )
			this.priority = 10;
		else
			this.priority = priority;
	}
	

	/**
	 * Just returns the name of the handler
	 */
	public String toString()
	{
		return this.name;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
