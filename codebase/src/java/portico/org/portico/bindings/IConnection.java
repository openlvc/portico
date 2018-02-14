/*
 *   Copyright 2008 The Portico Project
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
package org.portico.bindings;

import java.util.Map;

import org.portico.lrc.LRC;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.ResignFederation;

/**
 * The Portico communications infrastructure is abstracted from the rest of the framework so that
 * it can be swapped out/replaced with differing implementations without the core of Portico having
 * to know anything about it. The {@link IConnection} interface defines the contract between
 * connection implementations and the rest of the Portico framework. Messages are passed to and
 * from the connection, how they are sent across a network is a matter entirely for the
 * consideration of a given implementation.
 * 
 * <p/>
 * <b>Remote Messages Received by the Connection</b>
 * <p/>
 * When a connection receives an incoming message it should place it in the message queue of the
 * {@link LRC} it is associated with (something like
 * <code>lrc.getState().getQueue().offer(msg)</code>). 
 * 
 * <p/>
 * <b>No Argument Constructors</b>
 * <p/>
 * Instances of the concrete class that conforms to {@link IConnection} are created dynamically
 * from the class name using reflection. Each implementation <b>MUST</b> provide a public, no-arg
 * constructor that will be used to create new instances of it during startup.
 * 
 * <p/>
 * <b>Connection Lifecycle</b>
 * <p/>
 * Following instantiation, the infrastructure will put the connection through the following
 * lifecycle:
 * <ol>
 *   <li><b>{@link #configure(LRC,Map)}</b>: Once an instance of a conforming implementation has
 *       been created, it will be asked to configure itself. Inside the provided {@link Map} will
 *       be a number of items the connection can use to achieve this task.
 *   </li>
 *   <li><b>{@link #connect()}</b>: This is the signal to the connection that it is time to start
 *       doing its work. At this point, the connection should connect to the RTI/federation through
 *       whatever protocol-specific means is necessary (this includes RTI discovery if required).
 *   </li>
 *   <li><b>{@link #disconnect()}</b>: When the kernel is shutting down, it will notify the
 *       connection that it is time to finish up through this method. At this point, the connection
 *       should disconnect and perform any cleanup that is necessary.</li>
 * </ol>
 * 
 * <p/>
 * <b>HLA Bootstrapping Methods</b>
 * <p/>
 * Each connection also must implement a number of basic HLA bootstrapping methods. These 4 calls
 * (create federation, destroy federation, join federation, resign federation) are ones that
 * generally require some sort of speical handling on the part of the connection. For example,
 * when joining a federation the connection might need to attempt to establish a particular
 * connection to some service on behalf of the federate. It might also need to tear this down on
 * resign. As such, these requests are treated specially and separate methods are provided for them.
 * If no special processing is required by a particular connection, it can just treat these
 * particular calls the same.
 *
 * <p/>
 * For more information on the communications architecture, see the documentation on the Portico
 * <a href="http://porticoproject.org">wiki</a>.
 */
public interface IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// lifecycle methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * <i>This method is called by the Portico infrastructure during setup.</i>
	 * <p/>
	 * Called right after the connection implementation has been instantiated. This gives the
	 * connection an opportunity to perform any setup it requires. If there is a problem (either
	 * with any configuration data it has located through the properties map or some other
	 * mechanism) then it should throw a {@link JConfigurationException}. The {@link LRC} into
	 * which the connection is being deployed can be obtained via this method.
	 */
	public void configure( LRC lrc, Map<String,Object> properties )
		throws JConfigurationException;
	
	/**
	 * <i>This method is called by the Portico infrastructure during setup.</i>
	 * <p/>
	 * When it is time for the kernel to connect to the RTI/federate/etc... and to start accepting
	 * incoming messages, while being ready to send outgoing messages, this method is called. The
	 * connection implementations should use it to connect to network (or whatever communications
	 * mechanism is being used). This should include any discovery of remote components (such as
	 * the discovery of an RTI by federates).
	 */
	public void connect() throws JRTIinternalError;
	
	/**
	 * <i>This method is called by the Portico infrastructure during shutdown.</i>
	 * <p/>
	 * When the kernel is ready to shutdown, it will call this method, signalling to the connection
	 * that it should disconnect and do any shutdown and cleanup necessary.
	 */
	public void disconnect() throws JRTIinternalError;
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// message sending methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Broadcast the given message out to all participants in a federation asynchronously.
	 * As soon as the message has been received for processing or sent, this method is free
	 * to return. No response will be waited for.
	 * 
	 * @param message The message to broadcast
	 * @throws Exception If there was an error when sending the message
	 */
	public void broadcast( PorticoMessage message ) throws Exception;
	
	/**
	 * This method should be used when a message has to be sent and then time for responses to
	 * be broadcast back is needed before moving on. In this case, the connection will broadcast
	 * the message and then sleep for an amount of time appropriate based on the underlying
	 * comms protocol in use. For example, the JVM connection won't sleep for long, but a connection
	 * sending information over a network should wait longer.
	 * 
	 * @param message The message to broadcast
	 * @throws Exception If there was an error when sending the message
	 */
	public void broadcastAndSleep( PorticoMessage message ) throws Exception;

	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// bootstrapping methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Do any special processing required to create a federation and process the request. If there
	 * is any kind of connection specific problem (or a problem creating the federation in the
	 * first place), throw an exception idicating so.
	 */
	public void createFederation( CreateFederation createMessage ) throws Exception;
	
	/**
	 * Do any special processing required to destroy a federation and process the request. If there
	 * is any kind of connection specific problem (or a problem destroying the federation in the
	 * first place), throw an exception idicating so.
	 */
	public void destroyFederation( DestroyFederation destoryMessage ) throws Exception;
	
	/**
	 * Do any special processing required to join a federation and process the request. If there
	 * is any kind of connection specific problem (or a problem joining the federation in the
	 * first place), throw an exception idicating so. The return should be an instance of
	 * {@link ConnectedRoster} containing all the necessary information about the federation
	 * (local handle, remote federate handles, fom, etc...)
	 */
	public ConnectedRoster joinFederation( JoinFederation joinMessage ) throws Exception;
	
	/**
	 * Do any special processing required to resign from a federation and process the request.
	 * If there is any kind of connection specific problem (or a problem resigning from the
	 * federation in the first place), throw an exception idicating so.
	 */
	public void resignFederation( ResignFederation resignMessage ) throws Exception;
	
	/**
	 * Returns a list of all the federations currently active. The manner for fetching this will
	 * be binding-specific. If there is a problem locating this information, throw an exception.
	 */
	public String[] listActiveFederations() throws Exception;
}
