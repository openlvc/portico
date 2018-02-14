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

import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JException;
import org.portico2.lrc.LRC;
import org.portico2.rti.RTI;
import org.portico2.rti.federation.Federation;

/**
 * A message handler is designed to consume and process {@link PorticoMessage} subclass objects,
 * performing any appropraite actions (whether that be doing nothing, tranforming the message
 * somehow, taking some action with the information contained within the message, filling out a
 * {@link ResponseMessage}, etc...).
 * 
 * <p/>
 * <b>Configuration</b>
 * <p/>
 * Message handlers are used in all the major infrastructure components of Portico architecture
 * (RTI, LRC, Fordwarder). When they are created, their {@link #configure(Map)} method will be
 * called, passing in references to critical other components depending on where they are running.
 * For example, if running inside an RTI, the RTI object itself will be passed, in addition to the
 * name of the federation that the handler is servicing.
 * <p/>
 * Each piece of configuration or component reference is bound under a specific ID, all of which
 * are declared as statics on this interface. Simply request the needed piece from the map, confirm
 * it isn't null and store it.
 *   
 * <p/>
 * <b>Other Support</b>
 * <p/>
 * <b>RECOMMENDED:</b> Unless you have an excellent reason, it is *STRONGLY* recommended that all
 * your message handlers extend one of the RTI/LRC specific handler classes that will provide
 * convenience methods to extract common components and cache them locally. 
 * <p/>
 * <b>NOTE:</b> All {@link IMessageHandler} implementations should have either <b>NO CONSTRUCTOR</b>
 * or a <b>PUBLIC, NO-ARG CONSTRUCTOR</b>. This is due to the fact that handler instances will
 * generally be created through reflection.
 */
public interface IMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** Key under which the {@link RTI} object the handler lives in is bound */
	public static final String KEY_RTI                   = "portico.rti";
	/** Key under which the {@link Federation} object the handler lives in is bound */
	public static final String KEY_RTI_FEDERATION        = "portico.rti.federation";
	/** Key under which the {@link LRC} object this handler lives in is bound */
	public static final String KEY_LRC                   = "portico.lrc";

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * @return Some sort of name to represent this handler, typically for logging
	 */
	public String getName();

	/**
	 * The {@link IMessageHandler} interface is used in all critical Portico components, whether
	 * that be RTI, LRC or Forwarder. As such, we need a generic way to pass in any important
	 * references to infrastructure that a handler may require (and these will change depending
	 * on where the handler is running).
	 * <p/>
	 * The given properties are expected to have references to those infrastructure components
	 * bound to the provided properties under a key that is specified through one of the constants
	 * above. Handlers will fetch the references using these keys and then cast them to the types
	 * they require. 
	 *  
	 * @param properties The property set that hopefully has everything we are looking for
	 * @throws JConfigurationException If there is a problem with missing components or our use of them
	 */
	public void configure( Map<String,Object> properties ) throws JConfigurationException;
	
	/**
	 * A message has been received for processing. The request and/or response is contained within
	 * the given {@link MessageContext} object. Take appropriate action and throw any exception
	 * from the compatibility library you need to.
	 * 
	 * @param context The request and/or response holder
	 * @throws JException If there is a problem processing the message
	 */
	public void process( MessageContext context ) throws JException;
}
