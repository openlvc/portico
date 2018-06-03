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
package org.portico2.common.crypto;

import java.security.GeneralSecurityException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network.configuration.CryptoConfiguration;

public class CryptoManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static{ Security.addProvider(new BouncyCastleFipsProvider()); }

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private CryptoConfiguration configuration;
	private boolean isEnabled;
	private CipherMode cipherMode;
	private SecretKey sessionKey;
	private Cipher encryptCipher;
	private Cipher decryptCipher;
	
	private byte[] iv;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public CryptoManager( CryptoConfiguration configuration )
	{
		this.configuration = configuration;
		this.isEnabled = configuration.isEnabled();
		this.cipherMode = CipherMode.defaultMode();
		this.sessionKey = null;      // set in setSessionKey()
		this.encryptCipher = null;   // set in initialize()
		this.decryptCipher = null;   // set in initialize()
		
		this.iv = new byte[]{};
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * This method will re-initialize the encryption and decryption Cipher objects using
	 * the secret key. Any time the key changes, this will automatically be called. 
	 */
	public void initialize() throws JConfigurationException
	{
		try
		{
			// Encryption mode
			this.encryptCipher = Cipher.getInstance( configuration.getCipherConfig().getConfigString(), "BCFIPS" );
//			this.encryptCipher.init( Cipher.ENCRYPT_MODE, sessionKey, new IvParameterSpec(iv) );
			this.encryptCipher.init( Cipher.ENCRYPT_MODE, sessionKey );
			System.out.println( "EIV: "+encryptCipher.getIV().length );
			
			// Decryption mode
			this.decryptCipher = Cipher.getInstance( configuration.getCipherConfig().getConfigString(), "BCFIPS" );
			this.decryptCipher.init( Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(iv) );
			
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Error while setting up ciphers: "+e.getMessage(), e );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Session Key Management Methods   //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void setSessionKey( byte[] sessionKey, byte[] iv )
	{
		// Store the session key
		if( sessionKey.length != 16 && sessionKey.length != 24 && sessionKey.length != 32 )
			throw new IllegalArgumentException( "Key bit-length incorrect for AES (128, 192, 256): Found="+sessionKey.length );
 
		this.sessionKey = new SecretKeySpec( sessionKey, "AES" );
		
		// Store the initialization vector
		this.iv = iv;
		
		// Re-initialize the ciphers
		initialize();
	}
	
	
	public byte[] encrypt( byte[] buffer, int offset, int length ) throws JRTIinternalError
	{
		try
		{
			return encryptCipher.doFinal( buffer, offset, length );
		}
		catch( GeneralSecurityException gse )
		{
			throw new JRTIinternalError( "Error encrypting message: "+gse.getMessage(), gse );
		}
	}
	
	public byte[] decrypt( byte[] buffer, int offset, int length )
		throws JRTIinternalError
	{
		try
		{
			return decryptCipher.doFinal( buffer, offset, length );
		}
		catch( GeneralSecurityException gse )
		{
			throw new JRTIinternalError( "Error decrypting message: "+gse.getMessage(), gse );
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public boolean isEnabled()
	{
		return this.isEnabled;
	}
	
	public void setEnabled( boolean enabled )
	{
		this.isEnabled = enabled;
	}

	/**
	 * @return The {@link CipherMode} that the crypto manager is using for symmetric encryption,
	 *         or null if the manager is disabled
	 */
	public CipherMode getCipher()
	{
		return isEnabled ? cipherMode : null;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
