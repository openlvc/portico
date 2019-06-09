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
package org.portico2.common.network.transport;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network.CallType;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Message;
import org.portico2.common.network.ProtocolStack;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.protocol.Protocol;

/**
 * The {@link Transport} class is the parent of all implementations of a particular network
 * protocol/sending approach. Concrete implementations are expected to take a message and 
 * put it onto the network (or share it with the federation somehow), and to take messages
 * received over the transport and pass them up the protocol stack.<p/>
 * 
 * To support this, {@link Transport} extends {@link Protocol} and for each connection,
 * a single {@link Transport} implementation will <i>always</i> be the last element of
 * the {@link ProtocolStack}.
 */
public abstract class Transport extends Protocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected TransportType type;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected Transport( TransportType type )
	{
		this.type = type;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public final TransportType getType()
	{
		return this.type;
	}

	@Override // from Protocol
	public String getName()
	{
		return "Transport: "+type;
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///  Transport Lifecycle Methods   ////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Configure the tranport, linking it into the given host connection. From here you can
	 * get references to other components of the {@link Connection}, most notably its
	 * configuration object.
	 * <p/>
	 * 
	 * This method will be called by the method {@link Protocol#configure(Connection)}, which
	 * itself called by the {@link Connection}. When it is called, the components of the parent
	 * {@link Protocol} class will have already been initialized (super.hostConnection and
	 * super.logger).
	 * 
	 * @param configuration Configuration data that was extracted from the RID
	 * @param connection The {@link Connection} that this transport is contained within
	 * @throws JConfigurationException Thown if there is an error in the configuration data
	 */
	@Override
	protected abstract void doConfigure( ProtocolConfiguration configuration, Connection connection );

	/**
	 * Establish a link to the underlying transport and commence processing messages.
	 * This method is called when the connection itself is starting up. Prior to calls to this
	 * method, the transport should not process any messages.
	 * <p/>
	 * 
	 * Note: Any messages logged by open() should be done at the TRACE level unless they are
	 *       warnings or errors.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during startup
	 */
	public abstract void open() throws JRTIinternalError;
	
	/**
	 * Called when the {@link Connection} that we are in is closing down. All active links
	 * to the transport layer should be closed.
	 * <p/>
	 * 
	 * Note: Any messages logged by open() should be done at the TRACE level unless they are
	 *       warnings or errors.
	 * 
	 * @throws JRTIinternalError If there is a problem encountered during close out
	 */
	public abstract void close() throws JRTIinternalError;

	/**
	 * @return Whether the transport is open or not.
	 */
	public abstract boolean isOpen();
	
	///////////////////////////////////////////////////////////////////////////////////////
	///  Protocol Messaging Methods   /////////////////////////////////////////////////////
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
	public abstract void down( Message message );

	/**
	 * Take the given message and pass it back up the protocol stack that we are in
	 * to the next one in the chain.
	 */
	public final void up( Message message )
	{
		passUp( message );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
