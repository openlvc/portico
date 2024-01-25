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
package org.portico2.common.network.protocol;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.network.Connection;
import org.portico2.common.network.protocol.authentication.LrcAuthProtocol;
import org.portico2.common.network.protocol.authentication.RtiAuthProtocol;
import org.portico2.common.network.protocol.encryption.EncryptionProtocol;

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
	private Map<ProtocolType,Class<? extends Protocol>> rti;
	private Map<ProtocolType,Class<? extends Protocol>> lrc;
	private Map<ProtocolType,Class<? extends Protocol>> fwd;

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
		rti.put( ProtocolType.Authentication, RtiAuthProtocol.class );
		rti.put( ProtocolType.Encryption, EncryptionProtocol.class );
		
		// LRC
		lrc.put( ProtocolType.Authentication, LrcAuthProtocol.class );
		lrc.put( ProtocolType.Encryption, EncryptionProtocol.class );
		
		// Forwarder
		//  --None
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Create and return a new {@link Protocol} subclass based on the given protocol type
	 * and the host component that the protocol is being deployed to. An exception will be
	 * thrown if there is no type registered for the name (or it isn't supported by that
	 * particular host component), or if there is a problem during construction.
	 * 
	 * @param type The {@link ProtocolType} representing the protocol
	 * @param hostComponent The host component (RTI, LRC, Forwarder) that it will be wrapped up in
	 * @return A newly created instance of the protocol
	 * @throws JConfigurationException The type doesn't support the host component or there was
	 *                                 some other exception when we were creating it
	 */
	public Protocol createProtocol( ProtocolType type, Connection.Host hostComponent )
		throws JConfigurationException
	{
		Map<ProtocolType,Class<? extends Protocol>> map = null;
		switch( hostComponent )
		{
			case RTI: map = rti; break;
			case LRC: map = lrc; break;
			case Forwarder: map = fwd; break;
		}
		
		Class<? extends Protocol> clazz = map.get(type);
		if( clazz == null )
		{
			throw new JConfigurationException( "Host %s does not support Protocol %s",
			                                   hostComponent, type.name() );
		}
		
		try
		{
			return clazz.getDeclaredConstructor().newInstance();
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
