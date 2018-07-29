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
package org.portico2.common.network.configuration.protocol;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.xml.RID;
import org.portico2.common.network.Protocol;
import org.portico2.common.network.ProtocolStack;
import org.w3c.dom.Element;

/**
 * Within a {@link ProtocolStack} there can e a number of {@link Protocol} implementations.
 * To identify the type of each protocol in the stack, we have this enumeration.
 */
public enum ProtocolType
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	Authentication,
	Encryption;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Create a new, empty protocol configuration for the current type and return it.
	 * 
	 * @return A new, empty/default configuration for a protocol of the current type.
	 */
	public ProtocolConfiguration newConfiguration()
	{
		switch( this )
		{
			case Authentication: return new AuthenticationProtocolConfiguration();
			case Encryption: return new EncryptionProtocolConfiguration();
			default: throw new JConfigurationException( "Unknown Protocol type: "+this );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Creates a new configuration object for the protocol specified in the given element,
	 * passes the configuration on to the concrete type so it can load from it and then
	 * returns the finished configuration.
	 * 
	 * @param rid The RID object we are being parsed into
	 * @param element The element with the protocol configuration
	 * @return A complete configuration for the protocol based on the element's value
	 * @throws JConfigurationException If we don't know what type the protocol is
	 */
	public static ProtocolConfiguration newConfiguration( RID rid, Element element )
		throws JConfigurationException
	{
		// get the type so we can create the configuration
		ProtocolType type = ProtocolType.fromString( element.getTagName() );
		
		// create the configuration and parse it from the element
		ProtocolConfiguration configuration = type.newConfiguration();
		configuration.parseConfiguration( rid, element );
		return configuration;
	}


	public static ProtocolType fromString( String name )
	{
		if( name.equalsIgnoreCase("authentication") )
			return Authentication;
		else if( name.equalsIgnoreCase("encryption") )
			return Encryption;

		throw new JConfigurationException( "Unknown protocol type: "+name );
	}
}
