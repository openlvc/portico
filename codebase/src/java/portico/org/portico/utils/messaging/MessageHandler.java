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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation outlines the configuration data needed for a message handler. All valid
 * message handlers should declare this annotation and implement the {@link IMessageHandler}
 * interface.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MessageHandler
{
	/** The name of the component. If no name is provided, the string "<unknown>" will be used.
	    If the annotation is not provided, the setName() method for the handler will never
	    actually be called (so the default will be whatever the handler uses). In the provided
	    handler implementation (which can be used as a base for all other handlers), this defaults
	    to the name of the class (without the package prefix). */
	String name() default "<unknown>";
	
	/** The names of the modules that this handler belongs to. A module is just a name that
	    can be used to group handlers together so they can be managed as a single entity. */
	String[] modules() default "";
	
	/** The name of the sink (or sinks) this handler expects to be deployed into. For general
	    Portico use this will be either "incoming" or "outgiong". */
	String[] sinks();

	/** List of tags that a handler can use to specify what it requires from components. When
	    applying a module to set of handlers, a group of these tags can be provided. Only when
	    at least *one* of those values overlaps with *one* the values provided here will this
	    handler be applied. If an empty string is provided (as is the default), these values will
	    be ignored and the handler will be applied no matter what. */ 
	String[] keywords() default {};
	
	/** The classes of all the {@link PorticoMessage} children the handler wants to handle */
	Class<? extends PorticoMessage> messages();
	//Class<? extends PorticoMessage>[] messages() default {};

	/** The global message sink the handler should be added to. Only set this if you want your
	    handler to be a global handler. Defaults to "NONE" which marks it as a regular handler */
	MessageSink.Global global() default MessageSink.Global.NONE;
	
	/** Is this handler happy to be extended into a chain (defaults to true) */
	boolean augmentable() default true;
	
	/** If handler is augmented, where should it fall in the chain?
	    (numbers between 1-10). Lower priority (lower numbers) means further back in the chain.
	    Any number greater than 10 considered to be 10. (defaults to 5) */
	int priority() default 5;
	
}
