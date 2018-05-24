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
package org.portico2.common.network2;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.configuration.connection.ConnectionConfiguration;

/**
 * A {@link ITransport} implementation performs two functions:
 * <ol>
 *   <li>Sends {@link Message} objects out over some underlying mechanism.</li>
 *   <li>Receives byte[] from the underlying communications mechanism and passes them up
 *       to a {@link Connection} wrapped in a {@link Message} object.</li>
 * </ol>
 * 
 * That's it. The specifics of how it achieves this are left to the implementation.
 * In use, the {@link Connection} will pass a message down through its internal protocol stack
 * and then drop it into the transport for sending. When incoming, the connection should pass
 * messages up through {@link ProtocolStack#up(Message)}, which will then hand the message off
 * to the application. The protocol stack can be accessed from inside the connection object.
 */
public interface ITransport
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Lifecycle Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Configure the transport based on the incoming configuration data. We also provide a
	 * reference to the {@link Connection} that the transport serves so that it can reference
	 * it back for incoming messages.
	 * 
	 * @param configuration Configuration data that was extracted from the RID
	 * @param connection The {@link Connection} that this transport is contained within
	 * @throws JConfigurationException Thown if there is an error in the configuration data
	 */
	public void configure( ConnectionConfiguration configuration, Connection connection )
		throws JConfigurationException;

	/**
	 * Establish a link to the underlying transport and commence processing messages.
	 * This method is called when the connection itself is starting up. Prior to calls to this
	 * method, the transport should not process any messages.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during startup
	 */
	public void open() throws JRTIinternalError;
	
	/**
	 * Called when the {@link Connection} that we are in is closing down. All active links
	 * to the transport layer should be closed.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during close out
	 */
	public void close() throws JRTIinternalError;

	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Messaging Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Take the given message and send it to the medium that the transport uses.
	 * Inside the {@link Message} instance you'll be able to find all the information
	 * you need, such as:
	 * 
	 * <ol>
	 *   <li>The {@link CallType}: {@link Message#getCallType()}</li>
	 *   <li>The Request ID: {@link Message#getRequestId()}</li>
	 *   <li>The Message Header: {@link Message#getHeader()}</li>
	 *   <li>The Byte Buffer: {@link Message#getBuffer()}</li>
	 * </ol>
	 * 
	 * @param message The message that should be sent
	 */
	public void send( Message message );
	
	
}
