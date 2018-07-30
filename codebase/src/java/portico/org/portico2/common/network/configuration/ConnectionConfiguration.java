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
package org.portico2.common.network.configuration;

import java.util.Properties;

import org.portico2.common.configuration.RID;
import org.portico2.common.network.configuration.protocol.ProtocolStackConfiguration;
import org.portico2.common.network.transport.TransportType;
import org.w3c.dom.Element;

/**
 * This class represents a generic structure for the configuration of a connection.
 * Each connection has a root transport type that it uses. This is expressed in the
 * {@link TransportType} enumeration.
 * <p/>
 * 
 * Each connection also has a set of common properties, such as a name, encryption
 * and filtering settings. The common properties are accessed through this parent
 * class, but the transport-specific properties are contained in subclasses. 
 */
public abstract class ConnectionConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected String name;
	protected boolean enabled;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected ConnectionConfiguration( String name )
	{
		this.name = name;
		this.enabled = true;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * @return Get the underlying transport type that this connection will use.
	 */
	public abstract TransportType getTransportType();
	
	/**
	 * Parse the configuration for the connection from the given set of properties. The concrete
	 * connection type should look for properties with the given prefix.
	 * 
	 * @param prefix The prefix to look for properties under
	 * @param properties The properties set to look in
	 */
	@Deprecated
	public abstract void parseConfiguration( String prefix, Properties properties );

	/**
	 * Parse the configuration for the connection from the given set XML element. The concrete
	 * connection type should pull its configuration from here
	 * 
	 * @param rid The RID we are configuring
	 * @param element The XML element containing the configuration information
	 */
	public abstract void parseConfiguration( RID rid, Element element );

	/**
	 * Each connection will have a ProtocolStack. This returns the configuration that
	 * should be used for that stack.
	 * 
	 * @return The configuration to use for the protocol stack
	 */
	public abstract ProtocolStackConfiguration getProtocolStack();
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return this.name;
	}
	
	public void setName( String name )
	{
		this.name = name;
	}
	
	public boolean isEnabled()
	{
		return this.enabled;
	}
	
	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
