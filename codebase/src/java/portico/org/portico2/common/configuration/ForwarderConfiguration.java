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
package org.portico2.common.configuration;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.utils.XmlUtils;
import org.w3c.dom.Element;

public class ForwarderConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ConnectionConfiguration downstreamConfiguration;
	private ConnectionConfiguration upstreamConfiguration;
	
	// Firewall Configuration
	private boolean isFirewallEnabled;
	private Set<String> importObjectClasses;
	private Set<String> importInteractionClasses;
	private Set<String> exportObjectClasses;
	private Set<String> exportInteractionClasses;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected ForwarderConfiguration()
	{
		this.downstreamConfiguration = null;
		this.upstreamConfiguration = null;
		
		// Firewall Configuration
		// Sets of object and interaction class name/fragements that we use to
		// feed the firewall so it knows what is can flow between the two sides
		// of our connection. Populated in #parse(Element)
		this.isFirewallEnabled = false;
		this.importObjectClasses = new HashSet<String>();
		this.importInteractionClasses = new HashSet<String>();
		this.exportObjectClasses = new HashSet<String>();
		this.exportInteractionClasses = new HashSet<String>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public ConnectionConfiguration getDownstreamConfiguration()
	{
		return this.downstreamConfiguration;
	}
	
	public ConnectionConfiguration getUpstreamConfiguration()
	{
		return this.upstreamConfiguration;
	}

	public boolean isFirewallEnabled()
	{
		return this.isFirewallEnabled;
	}
	
	public Set<String> getAllowedImportObjects()
	{
		return Collections.unmodifiableSet( this.importObjectClasses );
	}
	
	public Set<String> getAllowedImportInteractions()
	{
		return Collections.unmodifiableSet( this.importInteractionClasses );
	}
	
	public Set<String> getAllowedExportObjects()
	{
		return Collections.unmodifiableSet( this.exportObjectClasses );
	}
	
	public Set<String> getAllowedExportInteractions()
	{
		return Collections.unmodifiableSet( this.exportInteractionClasses );
	}

//	private Set<String> stringToSet( String string )
//	{
//		HashSet<String> set = new HashSet<String>();
//		StringTokenizer tokenizer = new StringTokenizer( string, "," );
//		while( tokenizer.hasMoreTokens() )
//			set.add( tokenizer.nextToken().trim() );
//		
//		return set;
//	}

	////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Parsing Methods   ////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected void parseConfiguration( RID rid, Element element ) throws JConfigurationException
	{
		//////////////////////////////////
		// Network Configuration  ////////
		//////////////////////////////////
		Element networkElement = XmlUtils.getChild( element, "network", true );
		
		// Find the upstream and downstream elements
		List<Element> connections = XmlUtils.getChildren( networkElement, "connection" );
		Element upstream = null;
		Element downstream = null;
		for( Element temp : connections )
		{
			if( temp.getAttribute("name").equalsIgnoreCase("upstream") )
				upstream = temp;
			else if( temp.getAttribute("name").equalsIgnoreCase("downstream") )
				downstream = temp;
		}
		
		if( upstream == null )
			throw new JConfigurationException( "Forwarder is missing <connection name=\"upstream\">" );
		
		if( downstream == null )
			throw new JConfigurationException( "Forwarder is missing <connection name=\"downstream\">" );

		// Parse the connection configurations
		this.upstreamConfiguration = new ConnectionConfiguration( "upstream" );
		this.upstreamConfiguration.parseConfiguration( rid, upstream );
		this.downstreamConfiguration = new ConnectionConfiguration( "downstream" );
		this.downstreamConfiguration.parseConfiguration( rid, downstream );
		
		//////////////////////////////////
		// Firewall Configuration  ///////
		//////////////////////////////////
		Element firewallElement = XmlUtils.getChild( element, "firewall", true );
		this.isFirewallEnabled = firewallElement.getAttribute("enabled").equalsIgnoreCase( "true" );
		
		// Import Configuration
		Element importElement = XmlUtils.getChild( firewallElement, "import", true );
		populateFirewallRules( importElement, importObjectClasses, importInteractionClasses );

		// Export Configuration
		Element exportElement = XmlUtils.getChild( firewallElement, "export", true );
		populateFirewallRules( exportElement, exportObjectClasses, exportInteractionClasses );
	}

	private void populateFirewallRules( Element element,
	                                    Set<String> objectRules,
	                                    Set<String> interactionRules )
	{
		String direction = element.getTagName().toUpperCase();
		List<Element> objects = XmlUtils.getChildren( element, "object" );
		for( Element object : objects )
		{
			if( object.hasAttribute("class") == false )
				throw new JConfigurationException( "Forwarder "+direction+" Rules: <object> missing \"class\" attribute" );
			else
				objectRules.add( object.getAttribute("class") );
		}

		List<Element> interactions = XmlUtils.getChildren( element, "interaction" );
		for( Element interaction : interactions )
		{
			if( interaction.hasAttribute("class") == false )
				throw new JConfigurationException( "Forwarder "+direction+" Rules: <interaction> missing \"class\" attribute" );
			else
				interactionRules.add( interaction.getAttribute("class") );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
