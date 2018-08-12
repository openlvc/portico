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
import org.portico2.common.configuration.RID;
import org.portico2.common.network.protocol.ProtocolType;
import org.portico2.common.network.protocol.encryption.CipherMode;
import org.w3c.dom.Element;

/**
 * Class representing the configuration of the encryption protocol inside a ProtocolStack.
 */
public class EncryptionConfiguration extends ProtocolConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int keyLength;
	private CipherMode cipherMode;
	private String sharedKey;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public EncryptionConfiguration()
	{
		super();
		this.enabled    = false;
		this.keyLength  = 128;
		this.cipherMode = CipherMode.defaultMode();
		this.sharedKey  = null;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public ProtocolType getProtocolType()
	{
		return ProtocolType.Encryption;
	}

	public int        getKeyLength() { return this.keyLength; }
	public CipherMode getCipherConfig() { return this.cipherMode; }
	/** Return the shared key from the configuration file; will return null if none is set,
	    meaning we have to try and source it from the RTI (if we are the LRC), or generate
	    it ourselves randomly if we are the RTI. */
	public String     getSharedKey() { return this.sharedKey; }
	public boolean    hasSharedKey() { return this.sharedKey != null; }
	
	public void setKeyLength( int keylen )       { this.keyLength = keylen; }
	public void setCipherMode( CipherMode mode ) { this.cipherMode = mode; }
	public void setCipherConfig( String cipherConfig ) { this.cipherMode = CipherMode.fromConfigString(cipherConfig); }
	public void setSharedKey( String sharedKey ) { this.sharedKey = sharedKey; }

	////////////////////////////////////////////////////////////////////////////////////////
	///  Configuration Parsing   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void parseConfiguration( RID rid, Element element ) throws JConfigurationException
	{
		// verify that the element is named correctly
		String tagname = element.getTagName();
		if( tagname.equalsIgnoreCase("encryption") == false )
			throw new JConfigurationException( "Encryption procotol expected element <encryption>; received <"+tagname+">");
		
		if( element.hasAttribute("enabled") )
			this.enabled = Boolean.valueOf( element.getAttribute("enabled") );
		
		if( element.hasAttribute("keylength") )
			this.keyLength = Integer.valueOf( element.getAttribute("keylength") );
		
		if( element.hasAttribute("cipher") )
			this.cipherMode = CipherMode.fromConfigString( element.getAttribute("cipher") );
		
		if( element.hasAttribute("key") )
			this.sharedKey = element.getAttribute("key");
		
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
