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
package org.portico2.common.network.transport.jvm;

import java.util.HashSet;
import java.util.Set;

import org.portico.lrc.compat.JRTIinternalError;

/**
 * The {@link JvmExchange} is an aggregation point for {@link JvmTransport}s. Each connection will
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
	private Set<JvmTransport> transports;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private JvmExchange()
	{
		this.transports = new HashSet<>();
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
	protected void attachLrc( JvmTransport lrcConnection ) throws JRTIinternalError
	{
		if( lrcConnection == null )
			return;
		
		this.transports.add( lrcConnection );
	}

	/**
	 * Detach the given LRC from the exchange. This will remove it from the set of active 
	 * LRCs that are running within the exchange and stop it receiving messages from those
	 * who are.
	 * 
	 * @param lrc The LRC to detach
	 * @throws JRTIinternalError TBA
	 */
	protected void detachLrc( JvmTransport lrc ) throws JRTIinternalError
	{
		this.transports.remove( lrc );
	}

	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Exchange Methods   ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected void sendMessage( JvmTransport sender, byte[] message ) throws JRTIinternalError
	{
		// reflect to all other connected JVM transports, skipping ourselves
		for( JvmTransport temp : transports )
			if( temp != sender )
				temp.receive( message );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	protected static final JvmExchange instance()
	{
		return JvmExchange.INSTANCE;
	}
}
