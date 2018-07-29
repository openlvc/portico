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

import org.portico2.common.configuration.xml.RID;
import org.portico2.common.network.configuration.protocol.ProtocolStackConfiguration;
import org.portico2.common.utils.XmlUtils;
import org.w3c.dom.Element;

public class JvmConfiguration extends ConnectionConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ProtocolStackConfiguration protocolStack;
	private SharedKeyConfiguration sharedKeyConfiguration;
	private PublicKeyConfiguration publicKeyConfiguration;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public JvmConfiguration( String name )
	{
		super( name );
		this.protocolStack = new ProtocolStackConfiguration();
		this.sharedKeyConfiguration = new SharedKeyConfiguration();
		this.publicKeyConfiguration = new PublicKeyConfiguration();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Read Only
	 */
	public TransportType getTransportType()
	{
		return TransportType.JVM;
	}
	
	@Override
	public ProtocolStackConfiguration getProtocolStack()
	{
		return this.protocolStack;
	}

	@Override
	public SharedKeyConfiguration getSharedKeyConfiguration()
	{
		return this.sharedKeyConfiguration;
	}
	
	@Override
	public PublicKeyConfiguration getPublicKeyConfiguration()
	{
		return this.publicKeyConfiguration;
	}
	
	@Override
	public String toString()
	{
		return String.format( "[JVM: name=%s, enabled=%s]", super.name, super.enabled );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void parseConfiguration( RID rid, Element element )
	{
		///////////////////////////////////
		// Parent Element Properties //////
		///////////////////////////////////
		if( element.hasAttribute("enabled") )
			super.enabled = Boolean.valueOf( element.getAttribute("enabled") ); 
		
		///////////////////////////////////
		// Protocol Stack Properties //////
		///////////////////////////////////
		Element protocolStackElement = XmlUtils.getChild( element, "protocols", false );
		if( protocolStackElement != null )
			protocolStack.parseConfiguration( rid, protocolStackElement );
	}
	
	@Override
	public void parseConfiguration( String prefix, Properties properties )
	{
		this.publicKeyConfiguration.parseConfiguration( prefix, properties );
		this.sharedKeyConfiguration.parseConfiguration( prefix, properties );
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
