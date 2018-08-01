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
package org.portico2.common.network.configuration.transport;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.RID;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.protocol.ProtocolType;
import org.portico2.common.network.transport.TransportType;
import org.w3c.dom.Element;

/**
 * Parent class of all the transport configuration objects. These extend the 
 * {@link ProtocolConfiguration} class because a transport is a configuration.
 * They will implemeht the {@link #getProtocolType()} method and will always
 * return <code>null</code>.
 */
public abstract class TransportConfiguration extends ProtocolConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected String name;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected TransportConfiguration( ConnectionConfiguration connectionConfiguration )
	{
		super();
		this.name = connectionConfiguration.getName();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * This will always return <code>null<code> for a Transport.
	 */
	@Override
	public final ProtocolType getProtocolType()
	{
		return null;
	}

	/**
	 * @return The {@link TransportType} that this configuration is for
	 */
	public abstract TransportType getTransportType();
	
	/**
	 * @see ProtocolConfiguration#parseConfiguration(RID, Element)
	 */
	public abstract void parseConfiguration( RID rid, Element element ) throws JConfigurationException;
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
