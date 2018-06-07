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

import java.io.File;
import java.util.Properties;

public class AuthConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_ENABLED      = ".auth.enabled";
	public static final String KEY_PRIVATE      = ".auth.privatekey";
	public static final String KEY_PRIVATE_PASS = ".auth.privatepass";
	public static final String KEY_RTI_PUBLIC   = ".auth.rtipublic";
	//public static final String KEY_ID_TOKEN     = ".auth.idtoken";  // not used yet
	//public static final String KEY_PASSWORD     = ".auth.password"; // not used yet

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean isEnabled;
	private File privateKey;
	private char[] privateKeyPassword;
	private File rtiPublicKey;
	//private String idToken;
	//private char[] password;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public AuthConfiguration() // FIXME Make Protected
	{
		this.isEnabled  = false;
		this.privateKey = new File( "./id_rsa" );  // must be set
		this.privateKeyPassword = null; // if null, use no password
		this.rtiPublicKey = new File( "./id_rsa.pem"); // must be set
	}

	protected AuthConfiguration( String prefix, Properties properties )
	{
		this();
		parseConfiguration( prefix, properties );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	protected void parseConfiguration( String prefix, Properties properties )
	{
		if( properties.containsKey(prefix+KEY_ENABLED) )
			this.isEnabled = Boolean.valueOf( properties.getProperty(prefix+KEY_ENABLED) );
		
		if( properties.containsKey(prefix+KEY_PRIVATE) )
			this.privateKey = new File( properties.getProperty(prefix+KEY_PRIVATE) );
		
		if( properties.containsKey(prefix+KEY_PRIVATE_PASS) )
		{
			String pass = properties.getProperty( prefix+KEY_PRIVATE_PASS );
			if( pass.equalsIgnoreCase("<none>") )
				pass = null;
			else
				this.privateKeyPassword = pass.toCharArray();
		}
		
		if( properties.containsKey(prefix+KEY_RTI_PUBLIC) )
			this.rtiPublicKey = new File( properties.getProperty(prefix+KEY_RTI_PUBLIC) );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean    isEnabled() { return this.isEnabled; }
	public File       getPrivateKey() { return this.privateKey; }
	public char[]     getPrivateKeyPassword() { return this.privateKeyPassword; }
	public File       getRtiPublicKey() { return this.rtiPublicKey; }
	
	public void setEnabled( boolean enabled )            { this.isEnabled = enabled; }
	public void setPrivateKey( File privateKey )         { this.privateKey = privateKey; }
	public void setPrivateKeyPassword( char[] password ) { this.privateKeyPassword = password; }
	public void setRtiPublicKey( File rtiPublic )        { this.rtiPublicKey = rtiPublic; }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
