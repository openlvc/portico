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

import java.io.File;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.RID;
import org.portico2.common.network.protocol.ProtocolType;
import org.portico2.common.utils.XmlUtils;
import org.w3c.dom.Element;

/**
 * Configuration information for the Authentication protocol.
 */
public class AuthenticationConfiguration extends ProtocolConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	
	// PKI Values
	private boolean isPki;
	private File privateKey;
	private char[] privateKeyPassword;
	private File rtiPublicKey;
	private boolean isEnforced;
	
	// Password-Based Authroziation
	// TODO Add support for simple login/password based authentication

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public ProtocolType getProtocolType()
	{
		return ProtocolType.Authentication;
	}

	public void setType( String type )
	{
		if( type != null )
		{
			if( type.trim().equalsIgnoreCase("pki") )
			{
				this.isPki = true;
				return;
			}
			else if( type.trim().equalsIgnoreCase("password") )
			{
				this.isPki = false;
				return;
			}
		}
		
		throw new JConfigurationException( "Type [] is not valid; must be \"pki\" or \"password\"" ); 
	}
	
	// PKI Settings
	public boolean    isPki() { return this.isPki; }
	public boolean    isEnforced() { return this.isEnforced; }
	public File       getPrivateKey() { return this.privateKey; }
	public char[]     getPrivateKeyPassword() { return this.privateKeyPassword; }
	public File       getRtiPublicKey() { return this.rtiPublicKey; }
	
	public void setEnforced( boolean enforced )          { this.isEnforced = enforced; }
	public void setPrivateKey( File privateKey )         { this.privateKey = privateKey; }
	public void setPrivateKeyPassword( char[] password ) { this.privateKeyPassword = password; }
	public void setRtiPublicKey( File rtiPublic )        { this.rtiPublicKey = rtiPublic; }
	
	// Password Settings
	// TODO Add support

	////////////////////////////////////////////////////////////////////////////////////////
	///  Configuration Parsing   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void parseConfiguration( RID rid, Element element ) throws JConfigurationException
	{
		// Make sure this is the right type of element
		String tagname = element.getTagName();
		if( tagname.equalsIgnoreCase("authentication") == false )
			throw new JConfigurationException( "Authentication procotol expected element <authentication>; received <"+tagname+">");

		// Are we enabled?
		if( element.hasAttribute("enabled") )
			this.enabled = Boolean.valueOf( element.getAttribute("enabled") );
		
		// What type of authentication are we using?
		String type = element.getAttribute( "type" );
		if( type == null || type.trim().equals("") )
			throw new JConfigurationException( "Authentication protocol missing required attribute \"type\"" );

		// Load up our values 
		Element subelement = XmlUtils.getChild(element,type,true); // required

		if( type.equalsIgnoreCase("pki") )
		{
			if( subelement.hasAttribute("rtipublic") )
			{
				String path = subelement.getAttribute( "rtipublic" );
				path = rid.substituteSymbols( path );
				this.rtiPublicKey = new File( path );
			}
			
			if( subelement.hasAttribute("privatekey") )
			{
				String path = subelement.getAttribute( "privatekey" );
				path = rid.substituteSymbols( path );
				this.privateKey = new File( path );
			}

			if( subelement.hasAttribute("privatepass") )
			{
				String pass = subelement.getAttribute( "privatepass" );
				if( pass.equalsIgnoreCase("{none}") )
					this.privateKeyPassword = null;
				else
					this.privateKeyPassword = pass.toCharArray();
			}
			
			if( subelement.hasAttribute("enforced") )
				this.isEnforced = Boolean.valueOf( subelement.getAttribute("enforced") );
		}
		else if( type.equalsIgnoreCase("password") )
		{
			throw new JConfigurationException( "Authentication type \"password\" not yet supported" );
		}
		else
		{
			throw new JConfigurationException( "Authentication type not known: "+type );
		}
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
