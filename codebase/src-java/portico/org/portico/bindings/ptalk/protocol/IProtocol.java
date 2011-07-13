/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.protocol;

import java.util.Map;

import org.portico.bindings.ptalk.channel.Channel;
import org.portico.bindings.ptalk.channel.Packet;
import org.portico.lrc.compat.JConfigurationException;

/**
 * Classes implementing this interface should declare the annotation {@link Protocol} to allow
 * the Portico framework to automatically locate and load them.
 * <p/>
 * {@link IProtocol} defines the interface components that wish to handle incoming or outgoing
 * {@link Packet}s must implement. A Protocol is defined as any component that wishes to manipulate
 * the content or structure of a ptalk message as it is being sent or received.
 * <p/>
 * Any number of protocols many be plugged into a {@link Pipeline}. When a message is received for
 * sending, it is passed to the pipeline, which in turn passes it on to each member Protocol in the
 * order they are arranged. When a message is received from some source, it is passed through the
 * pipeline to each member protocol in the reverse order.
 * 
 * <p/>
 * <b>Configuring Protocols</b>
 * <p/>
 * While a protocol can make use of any option it wants to fetch configuration data, within Portico
 * the recommended approach is as follows.
 * <p/>
 * Each {@link IProtocol} implementation should have a unique name. This name is provided in the
 * {@link Protocol} annotation declaration. All configuration options should be obtained via
 * system properties. When Portico starts up, it will load all properties found in the RID file
 * into the system properties, thus allowing them to be accessed from anywhere.
 * <p/>
 * It is expected that PTalk Protocol implementations will use properties of the name:
 * <code>portico.ptalk.protocol.PROTOCOL_NAME.option=value</code>. If your {@link IProtocol}
 * implementation inherits from {@link AbstractProtocol}, it will have access to methods that
 * will scan the entire system property set and return all provided properties that conform to
 * this format.
 */
public interface IProtocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	/**
	 * This method will be called after the protocol has been constructed and should contain all
	 * configuration logic. The class-level comments define how protocol configuration options
	 * can be obtained. If there is a problem configuring the protocol, a
	 * {@link JConfigurationException} should be thrown.
	 * 
	 * @param The channel containing the pipeline this protocol is deployed into.
	 */
	public void configure( Channel channel, Map<String,Object> properties )
		throws JConfigurationException;
	
	/**
	 * Handle a message that is being prepared to be sent to the network. Headers can be added, 
	 * content transformations made, etc... If the method returns false, then processing should
	 * stop and the packet won't be passed on to the next protocol.
	 * 
	 * @return true if the packet should continue to be processed by next protocol, false otherwise
	 */
	public boolean outgoing( Packet packet ) throws RuntimeException;
	
	/**
	 * Handle a message that has been received from the network. Headers can be read so that the
	 * appropriate actions are taken. Transformations can be made on the content, etc... If the
	 * method returns false, then processing should stop and the packet won't be passed on to the
	 * next protocol.
	 * 
	 * @return true if the packet should continue to be processed by next protocol, false otherwise
	 */
	public boolean incoming( Packet packet ) throws RuntimeException;
	
	/**
	 * Returns the name of the protocol
	 */
	public String getName();
}
