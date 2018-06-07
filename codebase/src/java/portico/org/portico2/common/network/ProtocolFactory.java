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
package org.portico2.common.network;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.network.protocols.auth.LRCAuthenticationProtocol;
import org.portico2.common.network.protocols.auth.RTIAuthenticationProtocol;
import org.portico2.common.network.protocols.crypto.EncryptionProtocol;

/**
 * The {@link ProtocolFactory} stores references to all the various protocols that
 * Portico supports, and the {@link Connection.Host} components that they support.
 * You can create new instances of protocols through <code>createProtocol</code>.
 */
public class ProtocolFactory
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final ProtocolFactory INSTANCE = new ProtocolFactory();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String,Class<? extends Protocol>> rti;
	private Map<String,Class<? extends Protocol>> lrc;
	private Map<String,Class<? extends Protocol>> fwd;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private ProtocolFactory()
	{
		this.rti = new HashMap<>();
		this.lrc = new HashMap<>();
		this.fwd = new HashMap<>();
		
		// Register all the supported protocols
		// RTI
		rti.put( "encryption", EncryptionProtocol.class );
		rti.put( "authentication", RTIAuthenticationProtocol.class );
		
		// LRC
		lrc.put( "encryption", EncryptionProtocol.class );
		lrc.put( "authentication", LRCAuthenticationProtocol.class );
		
		// Forwarder
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Create and return a new {@link Protocol} subclass based on the given name and
	 * the host component that the protocol is being deployed to. An exception will
	 * be thrown if there is no type registered for the name (or it isn't supported
	 * by that particular host component), or if there is a problem during construction.
	 * 
	 * @param name The name to use for a lookup
	 * @param hostComponent The host component (RTI, LRC, Forwarder) that it will be wrapped up in
	 * @return A newly created instance of the protocol
	 * @throws JConfigurationException The type doesn't support the host component or there was
	 *                                 some other exception when we were creating it
	 */
	public Protocol createProtocol( String name, Connection.Host hostComponent )
		throws JConfigurationException
	{
		Map<String,Class<? extends Protocol>> map = null;
		switch( hostComponent )
		{
			case RTI: map = rti; break;
			case LRC: map = lrc; break;
			case Forwarder: map = fwd; break;
		}
		
		Class<? extends Protocol> clazz = map.get(name);
		if( clazz == null )
		{
			throw new JConfigurationException( "Host %s does not support Protocol %s",
			                                   hostComponent, name );
		}
		
		try
		{
			return clazz.newInstance();
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Could not create Protocol: "+clazz.getSimpleName() );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static ProtocolFactory instance()
	{
		return INSTANCE;
	}

}
