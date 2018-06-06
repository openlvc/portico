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
import org.portico2.common.network.Connection;
import org.portico2.common.network.Header;
import org.portico2.common.network.Message;
import org.portico2.common.network.Protocol;
import org.portico2.common.network.configuration.CryptoConfiguration;

public class EncryptionProtocol extends Protocol
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
	protected void doConfigure( Connection hostConnection ) throws JConfigurationException
	{
		this.configuration = hostConnection.getConfiguration().getCryptoConfiguration();

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
	}

	@Override
	public void open()
	{
		if( isEnabled == false )
			return;
		
		// Generate the session key FIXME - Shared via config for now
		String configuredKey = configuration.getSharedKey();
		String keylength = "%"+configuration.getKeyLength()/8;
		configuredKey = String.format( keylength+"s", configuredKey );
		setSessionKey( configuredKey.getBytes() );
	}
	
	@Override
	public void close()
	{
		// no-op
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void down( Message message )
	{
		if( isEnabled )
		{
			try
			{
				encrypt( message );
			}
			catch( Exception e )
			{
				logger.warn( e.getMessage(), e );
			}
			
			// pass down the stack
			passDown( message );
		}
	}

	@Override
	public void up( Message message )
	{
		if( isEnabled )
		{
			try
			{
				decrypt( message );
			}
			catch( Exception e )
			{
				logger.warn( e.getMessage(), e );
			}
			
			// keep passing up the stack
			passUp( message );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Session Key Management Methods   //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void setSessionKey( byte[] sessionKey )
	{
		// Store the session key
		if( sessionKey.length != 16 && sessionKey.length != 24 && sessionKey.length != 32 )
			throw new IllegalArgumentException( "Key bit-length incorrect for AES (128, 192, 256): Found="+sessionKey.length );
 
		this.sessionKey = new SecretKeySpec( sessionKey, "AES" );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Encryption/Decryption Methods   ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private synchronized void encrypt( Message message ) throws JRTIinternalError
	{
		// Incoming Message Structure:
		//    Header  [16 Bytes]
		//    Payload [xx Bytes]
		//
		// Encrypted Message Structure:
		//    Header      [16 Bytes]
		//    IV/Nonce    [16 Bytes]  -- CipherMode.getIvSize()
		//    Cipher Text [xx Bytes]  -- CipherMode.getCipherTextSize()
		//

		// Step 1. Get the original message buffer.
		//         We want to retain the header, but encrypt the payload.
		byte[] original = message.getBuffer();
		int payloadLength = message.getHeader().getPayloadLength();
		
		// Step 2. Create a new buffer large enough to hold the encrypted output
		int ivSize = cipherMode.getIvSize();
		byte[] target = new byte[original.length + ivSize];
		
		// Step 3. Encryption
		//         Do the encryption, writing the CT into the new array.
		//         Write the IV into the start of the payload section.
		try
		{
			// Initialize the encrypter. This will generate a new IV.
			encryptCipher.init( Cipher.ENCRYPT_MODE, sessionKey );
			
			// Write the IV first
			System.arraycopy( encryptCipher.getIV(),                  // Source
			                  0,                                      // Source Offset
			                  target,                                 // Destination
			                  Header.HEADER_LENGTH,                   // IV Offset
			                  ivSize );                               // Num bytes to copy
			
			// Write the CT next
			encryptCipher.doFinal( original,                          // Source
			                       Header.HEADER_LENGTH,              // Source Offset
			                       payloadLength,                     // Length to read
			                       target,                            // Destination
			                       Header.HEADER_LENGTH+ivSize );     // Destination Offset
		}
		catch( GeneralSecurityException gse )
		{
			throw new JRTIinternalError( "Error encrypting message: "+gse.getMessage(), gse );
		}

		// Step 4. Write the original header into the new target
		System.arraycopy( original, 0, target, 0, Header.HEADER_LENGTH );
		
		// Step 5. Store the updated payload back in the message and update the header
		message.replaceBuffer( target );
		message.getHeader().writeIsEncrypted( true );
		message.getHeader().writeCipherMode( cipherMode );
		message.getHeader().writePayloadLength( payloadLength + ivSize );
	}
	
	private synchronized void decrypt( Message message ) throws JRTIinternalError
	{
		if( message.getHeader().isEncrypted() == false )
			return;

		// Step 1. Get the original message buffer.
		//         We want to retain the header, but decrypt the payload.
		byte[] original = message.getBuffer();
		int payloadLength = message.getHeader().getPayloadLength();
		
		// Step 2. Create a new buffer of reduced size to hold the plain text
		int ivSize = cipherMode.getIvSize();
		byte[] target = new byte[original.length-ivSize];

		// Step 3. Decryption
		//         Extract the IV from the front of payload and then decrypt the contents
		//         in place, replacing the original buffer with the plain text version.
		try
		{
			// Read the IV in from the payload                  [ <--- Offset ---> ]
			IvParameterSpec iv = new IvParameterSpec( original, Header.HEADER_LENGTH, ivSize );
			
			// Initialize the decrypter
			decryptCipher.init( Cipher.DECRYPT_MODE, sessionKey, iv );

			// Decrypt the contents
			decryptCipher.doFinal( original,
			                       Header.HEADER_LENGTH+ivSize, // Offset to CT payload
			                       payloadLength-ivSize,
			                       target,
			                       Header.HEADER_LENGTH );
		}
		catch( GeneralSecurityException gse )
		{
			throw new JRTIinternalError( "Error decrypting message: "+gse.getMessage(), gse );
		}

		// Step 4. Write the original header into the new target
		System.arraycopy( original, 0, target, 0, Header.HEADER_LENGTH );
		
		// Step 5. Store the updated payload back in the message and update the header
		message.replaceBuffer( target );
		message.getHeader().writeIsEncrypted( false );
		message.getHeader().writePayloadLength( payloadLength-ivSize );
	}

	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName()
	{
		return "Encrypter";
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
