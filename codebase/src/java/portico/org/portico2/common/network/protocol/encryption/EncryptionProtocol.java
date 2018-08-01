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
package org.portico2.common.network.protocol.encryption;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Message;
import org.portico2.common.network.configuration.protocol.EncryptionConfiguration;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.protocol.Protocol;

public class EncryptionProtocol extends Protocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static{ Security.addProvider(new BouncyCastleFipsProvider()); }

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private EncryptionConfiguration configuration;

	private boolean isEnabled;
	private CipherMode cipherMode;
	private SecretKey sessionKey;
	private Cipher encryptCipher;
	private Cipher decryptCipher;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public EncryptionProtocol()
	{
		super();
		this.configuration = null;   // set in configure()
		
		// Runtime Properties
		this.isEnabled = false;      // set in configure()
		this.cipherMode = CipherMode.defaultMode(); // set in configure()
		this.sessionKey = null;      // set in open()
		this.encryptCipher = null;   // set in configure()
		this.decryptCipher = null;   // set in configure()		
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doConfigure( ProtocolConfiguration providedConfig, Connection hostConnection )
		throws JConfigurationException
	{
		this.configuration = (EncryptionConfiguration)providedConfig;
		this.hostConnection = hostConnection;

		// Runtime Settings
		this.isEnabled = configuration.isEnabled();
		this.cipherMode = configuration.getCipherConfig();
		
		// Create the Ciphers
		try
		{
			String configString = configuration.getCipherConfig().getConfigString();
			this.encryptCipher = Cipher.getInstance( configString, "BCFIPS" );
			this.decryptCipher = Cipher.getInstance( configString, "BCFIPS" );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Error while setting up ciphers: "+e.getMessage(), e );
		}
		
		// Shared key is extracted in open() call

	}

	public void open()
	{
		
	}

	public void close()
	{
		
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void down( Message message )
	{
		
	}

	public void up( Message message )
	{
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return "Encryption";
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
