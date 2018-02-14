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
package org.portico2.common.network.jvm;

import java.util.HashSet;
import java.util.Set;

import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;

/**
 * The {@link JvmExchange} is an aggregation point for {@link JvmConnections}. Each connection will
 * forward all its messages into the exchange where they are reflected around to all the other
 * connections.
 */
public class JvmExchange
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final JvmExchange INSTANCE = new JvmExchange();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Set<JvmConnection> connections;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private JvmExchange()
	{
		this.connections = new HashSet<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Methods   ///////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Attach the given LRC to the exchange so that it can start sending control and broadcast
	 * messages. If there is no RTI registered, this call will fail, as we are not yet in a state
	 * where we are ready for connections from federates.
	 * 
	 * @param lrcConnection The connection representing the LRC
	 * @throws JRTIinternalError If there is no active RTI yet
	 */
	protected void attachLrc( JvmConnection lrcConnection ) throws JRTIinternalError
	{
		if( lrcConnection == null )
			return;
		
		this.connections.add( lrcConnection );
	}

	/**
	 * Detach the given LRC from the exchange. This will remove it from the set of active 
	 * LRCs that are running within the exchange and stop it receiving messages from those
	 * who are.
	 * 
	 * @param lrc The LRC to detach
	 * @throws JRTIinternalError TBA
	 */
	protected void detachLrc( JvmConnection lrc ) throws JRTIinternalError
	{
		this.connections.remove( lrc );
	}

	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Exchange Methods   ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected void sendControlRequest( JvmConnection sender, MessageContext context ) throws JRTIinternalError
	{
		// FIXME Each call should have its own MessageContext
		//       Or the call should only go where it needs to go
		for( JvmConnection lrcConnection : connections )
		{
			if( lrcConnection != sender )
				lrcConnection.receiveControlRequest( sender, context );
		}
	}
	
	protected void sendDataMessage( JvmConnection sender, PorticoMessage message )
	{
		for( JvmConnection lrcConnection : connections )
		{
			if( lrcConnection != sender )
				lrcConnection.receiveDataMessage( sender, message );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static final JvmExchange instance()
	{
		return JvmExchange.INSTANCE;
	}
}
