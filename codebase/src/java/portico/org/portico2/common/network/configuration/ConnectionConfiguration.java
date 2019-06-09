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

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.RID;
import org.portico2.common.network.configuration.protocol.ProtocolStackConfiguration;
import org.portico2.common.network.configuration.transport.TransportConfiguration;
import org.portico2.common.network.protocol.Protocol;
import org.portico2.common.network.transport.Transport;
import org.portico2.common.network.transport.TransportType;
import org.portico2.common.utils.XmlUtils;
import org.w3c.dom.Element;

/**
 * This class represents a generic structure for the configuration of a connection.
 * Each connection consists of a root {@link Transport}, the configuration for which
 * we contain. The connection also contains a {@link ProtocolStack}, which again we
 * contain the configuration for.
 */
public class ConnectionConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private boolean enabled;
	private TransportConfiguration transportConfiguration;
	private ProtocolStackConfiguration protocolStackConfiguration;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ConnectionConfiguration( String name )
	{
		this.name = name;
		this.enabled = true;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Parse the configuration for the connection from the given set XML element. The concrete
	 * connection type should pull its configuration from here
	 * 
	 * @param rid The RID we are configuring
	 * @param element The XML element containing the configuration information
	 */
	public void parseConfiguration( RID rid, Element element ) throws JConfigurationException
	{
		///////////////////////////////////
		// Parse Base Properties  /////////
		///////////////////////////////////
		this.name = element.getAttribute( "name" );
		if( this.name == null || this.name.equals("") )
			throw new JConfigurationException( "The \"name\" attribute was not provided on <connection>" );

		if( element.hasAttribute("enabled") )
			this.enabled = Boolean.valueOf( element.getAttribute("enabled") ); 
		
		///////////////////////////////////
		// Transport Configuration  ///////
		///////////////////////////////////
		TransportType transport = TransportType.fromString( element.getAttribute("transport") );
		this.transportConfiguration = transport.newConfiguration( this );
		Element transportElement = XmlUtils.getChild( element, transport.getConfigurationName().toLowerCase(), true );
		this.transportConfiguration.parseConfiguration( rid, transportElement );

		///////////////////////////////////
		// Protocol Stack Properties //////
		///////////////////////////////////
		Element protocolStackElement = XmlUtils.getChild( element, "protocols", false );
		this.protocolStackConfiguration = new ProtocolStackConfiguration();
		if( protocolStackElement != null )
			protocolStackConfiguration.parseConfiguration( rid, protocolStackElement );
	}

	/**
	 * Each connection has a single Transport. The {@link Transport} is a subclass
	 * of {@link Protocol} so that it can sit as the last element in the {@link ProtocolStack},
	 * but it is a special type. As such, it also has its own configuration object.
	 * 
	 * @return The configuration to use for the {@link Transport}.
	 */
	public TransportConfiguration getTransportConfiguration()
	{
		return this.transportConfiguration;
	}
	
	public void setTransportConfiguration( TransportConfiguration configuration )
	{
		this.transportConfiguration = configuration;
	}
	
	/**
	 * Each connection will have a ProtocolStack. This returns the configuration that
	 * should be used for that stack.
	 * 
	 * @return The configuration to use for the protocol stack
	 */
	public ProtocolStackConfiguration getProtocolStackConfiguration()
	{
		return this.protocolStackConfiguration;
	}
	
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
