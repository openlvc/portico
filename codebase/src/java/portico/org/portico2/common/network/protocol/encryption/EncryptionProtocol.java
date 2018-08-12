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
import org.portico2.common.network.configuration.protocol.EncryptionConfiguration;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.protocol.Protocol;

/**
 * The {@link EncryptionProtocol} is separate from but may be related to the Auth protocol.
 * We decide whether to encrypt a message or not. For that, we need an encryption key.
 * <p/>
 * 
 * <p><b>Encryption Key</b></p>
 * <p>
 * We can either get the encryption key directly from configuration, which will cause us to
 * always use that verison and always encrypt messages. If there is none in configuration, we
 * rely on the Auth protocol to hand one down to us. </p>
 * 
 * <p><b>Encryption Process</b></p>
 * <p>If we have an encryption key, we always use it.</p>
 * <p>If we don't have a key and we receive a federation-level message, we throw an exception.
 * Things should never get that far without us having our key and something is wrong. However,
 * to allow setup to happen, we let non-federation messages get through unencrypted.</p>
 */
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
		
		// Shared key is extracted in open() call, or set by a the Auth protocol above
		// us if we are getting it from the RTI
	}

	/**
	 * Get the shared key from the configuration and store it locally so that we're ready.
	 * There may NOT be a key in the configuration. We handle this differently depending on
	 * who we are:
	 * <ul>
	 *   <li>If LRC: We will get the key on federation JOIN, so watch for those messages. In
	 *               the mean time we'll throw an exception for all federate-messages until
	 *               we get our key.</li>
	 *   <li>If RTI: We'll generate a session key for the federation, from FIXME</li>
	 * </ul>
	 */
	public void open()
	{
		if( isEnabled == false )
			return;

		// If the configuration has a shared key, we need to extract it from there.
		// If the configuration does NOT have a shared key, we expect that someone
		// will pass this down to us, either through a SetSessionKey message, or by
		// directly setting it on us.
		if( configuration.hasSharedKey() )
		{
    		// Generate the session key FIXME - Shared via config for now
    		String configuredKey = configuration.getSharedKey();
    		String keylength = "%"+configuration.getKeyLength()/8;
    		configuredKey = String.format( keylength+"s", configuredKey );
    		setSessionKey( configuredKey.getBytes() );
		}
	}

	public void close()
	{
		// Nothing to do
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Encryption and auth are two separate but related tasks. We can receive out encryption
	 * key one of two ways. Firstly, it may be set in configuration. If this is the case, we
	 * just encrypt everything.
	 * <p/>
	 * 
	 * If it isn't set, we rely on the Auth protocol to get the key for us and pass it down.
	 * Any non-federation message we'll let pass through because it is still "setup" stuff,
	 * however if we don't have a key and we get a federation message, something is wrong and
	 * we'll throw an exception.
	 * <p/>
	 * 
	 * Skip all this guff if encryption isn't enabled.
	 * 
	 * @param message The message to encrypt (or maybe not!)
	 * @throws JRTIinternalError If this is a federation-level message and we haven't got a key yet
	 */
	@Override
	public void down( Message message ) throws JRTIinternalError
	{
		if( isEnabled )
		{
			// If we have a session key, just encrypt everything.
			// If we don't have a session key, we'll let any non-federation 
			// messages pass, but we'll have to push back on federation-level
			// messages.
			if( sessionKey != null )
			{
				// encrypt
				encrypt( message );
			}
			else if( message.getMessageType().isFederationMessage() )
			{
				// it's a federation message and we haven't got a key - something has gone wrong
				throw new JRTIinternalError( "No encryption key found for messsage: %s",
				                             message.getMessageType() );
			}
			else
			{
				// just let it slip through unencrypted; but log it!
				logger.warn( "Processing ("+message.getMessageType()+
				             "): No encryption key found, but letting pass; Auth should provide it." );
			}
		}

		// will only drop through to here if we've encrypted the message
		// or we don't need to encrypt it
		passDown( message );
	}

	/**
	 * Processing incoming messages is pretty simple. If its encrypted flag it set then we
	 * will try to decrypt it. Should we be missing our encryption key, we'll get an exception.
	 * 
	 * @param message The message to decrypt (or not)
	 * @throws JRTIinternalError If we try to decrypt a message with the wrong key or no key at all
	 */
	public void up( Message message ) throws JRTIinternalError
	{
		if( isEnabled && message.getHeader().isEncrypted() )
		{
			decrypt( message );
		}
		
		passUp( message );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Session Key Management Methods   //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public void setSessionKey( byte[] sessionKey )
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
		//
		// Note that if authentication is being used it will have been layered into
		// the data message before now.
		//
		
		// Unencrypted Message Structure (Before):
		//    Header  [12 Bytes]
		//    Payload [xx Bytes]
		//
		// Encrypted Message Structure (After):
		//    Header      [12 Bytes]
		//    Payload     [xx+16 Bytes]
		//       Data         [xx Bytes]
		//       IV/Nonce     [16 Bytes]   -- may be different size
		//

		// Step 1. Get the original message buffer.
		//         We want to retain the header, but encrypt the payload.
		byte[] original = message.getBuffer();
		int payloadLength = message.getHeader().getPayloadLength();
		
		// Step 2. Create a new buffer large enough to hold the encrypted output
		int ivSize = cipherMode.getIvSize();
		byte[] target = new byte[original.length + 4/*auth*/ + ivSize];
		
		// Step 3. Encryption
		//         Do the encryption, writing the CT into the new array.
		//         Write the IV into the start of the payload section.
		try
		{
			// Initialize the encrypter. This will generate a new IV.
			encryptCipher.init( Cipher.ENCRYPT_MODE, sessionKey );
			
			// Write the Cipher Text first
			encryptCipher.doFinal( original,                          // Source
			                       Header.HEADER_LENGTH,              // Source Offset
			                       payloadLength,                     // Length to read
			                       target,                            // Destination
			                       Header.HEADER_LENGTH );            // Destination Offset
			
			// Write the IV last
			System.arraycopy( encryptCipher.getIV(),                  // Source
			                  0,                                      // Source Offset
			                  target,                                 // Destination
			                  Header.HEADER_LENGTH+payloadLength,     // IV Offset
			                  ivSize );                               // Num bytes to copy
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
		
		// Step 2. Create a new buffer of reduced size to hold just the plain text
		int ivSize = cipherMode.getIvSize();
		byte[] target = new byte[original.length-ivSize];

		// Step 3. Decryption
		//         Extract the IV from the tail of the payload and then decrypt the contents
		//         in place, replacing the original buffer with the plain text version.
		try
		{
			// Read the IV from the tail of the payload         [ <---- Offset ----> ]
			IvParameterSpec iv = new IvParameterSpec( original, original.length-ivSize, ivSize );
			
			// Initialize the decrypter
			decryptCipher.init( Cipher.DECRYPT_MODE, sessionKey, iv );

			// Decrypt the contents
			decryptCipher.doFinal( original,
			                       Header.HEADER_LENGTH,   // Offset to CT payload
			                       payloadLength-ivSize,   // Size of section to decrypt
			                       target,                 // Output buffer
			                       Header.HEADER_LENGTH ); // Offset into output
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
	public String getName()
	{
		return "Encryption";
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
