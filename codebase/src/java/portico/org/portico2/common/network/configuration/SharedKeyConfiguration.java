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

import org.portico2.common.network.protocol.encryption.CipherMode;

/**
 * This class extract encryption configuration information properties from the RID file
 * and makes it available to the application.
 */
public class SharedKeyConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String KEY_ENABLED    = ".sharedkey.enabled";
	public static final String KEY_KEY_LENGTH = ".sharedkey.keylen"; // Need Java unlimited strength policies to use >128
	public static final String KEY_CIPHER     = ".sharedkey.cipher";
	public static final String KEY_SHARED_KEY = ".sharedkey.key";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean isEnabled;
	private int keyLength;
	private CipherMode cipherMode;
	private String sharedKey;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SharedKeyConfiguration() // FIXME protected
	{
		this.isEnabled  = false;
		this.keyLength  = 128;
		this.cipherMode = CipherMode.defaultMode();
		this.sharedKey  = null;
	}

	protected SharedKeyConfiguration( String prefix, Properties properties )
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
		
		if( properties.containsKey(prefix+KEY_KEY_LENGTH) )
			this.keyLength = Integer.valueOf( properties.getProperty(prefix+KEY_KEY_LENGTH) );
		
		if( properties.containsKey(prefix+KEY_CIPHER) )
			this.cipherMode = CipherMode.fromConfigString(properties.getProperty(prefix+KEY_CIPHER) );
		
		if( properties.containsKey(prefix+KEY_SHARED_KEY) )
			this.sharedKey = properties.getProperty( prefix+KEY_SHARED_KEY );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean    isEnabled() { return this.isEnabled; }
	public int        getKeyLength() { return this.keyLength; }
	public CipherMode getCipherConfig() { return this.cipherMode; }
	
	public void setEnabled( boolean enabled )    { this.isEnabled = enabled; }
	public void setKeyLength( int keylen )       { this.keyLength = keylen; }
	public void setCipherMode( CipherMode mode ) { this.cipherMode = mode; }
	public void setCipherConfig( String cipherConfig ) { this.cipherMode = CipherMode.fromConfigString(cipherConfig); }
	
	public boolean hasSharedKey() { return this.sharedKey != null; }
	public String getSharedKey() { return this.sharedKey; }
	public void setSharedKey( String sharedKey ) { this.sharedKey = sharedKey; }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
