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
package org.portico2.common.network.protocol.pki;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.network.Header;
import org.portico2.common.network.Message;
import org.portico2.common.network.protocol.encryption.CipherMode;

/**
 * This is a set of functions used by the Authentication protocol to do encryption/decryption
 * things. They do not exist as general crypto tools because they're all not very efficient.
 * However, for the authentication use case (only a couple of messages ever sent for each
 * federate during its lifecycle as it does setup), this is not a big deal. Don't use these
 * for any high-performance/fast-path things! <p/>
 * 
 * Generating Keys with ssh-keygen
 * <ul>
 *  <li><code>ssh-keygen -t rsa -b 4096 -C "example@example.com"   // produces id_rsa/id_rsa.pub</code></li>
 *  <li><code>ssh-keygen -f id_rsa.pub -e -m pem > id_rsa.pem      // turn the .pub file into PEM formatted</code></li>
 * </ul>
 * 
 * Generating Keys with OpenSSL
 * <ul>
 *  <li><code>openssl genrsa -aes128 -out private.pem 2048                       // Generate private key</code></li>
 *  <li><code>openssl rsa -in private.pem -outform PEM -pubout -out public.pem   // Generate public key</code></li>
 * </ul>
 */
public class AuthUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static{ Security.addProvider(new BouncyCastleFipsProvider()); }

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////////////////////
    ///  Symmetric Key Methods   ///////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 * @param bits
	 * @return
	 * @throws JRTIinternalError
	 */
	public static SecretKey generateSymmetricKey( int bits ) throws JRTIinternalError
	{
		try
		{
    		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "BCFIPS");
    		keyGenerator.init(bits);
    		return keyGenerator.generateKey();
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( e.getMessage(), e );
		}
	}

	/**
	 * 
	 * @param key
	 * @param cipherMode
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 */
	public static byte[] encryptWithSymmetricKey( SecretKey key,
	                                              CipherMode cipherMode,
	                                              byte[] buffer,
	                                              int offset,
	                                              int length )
	{
		try
		{
			// Create Cipher Object
			Cipher encrypter = Cipher.getInstance( cipherMode.getConfigString(), "BCFIPS" );

			// Initialize Cipher
			encrypter.init( Cipher.ENCRYPT_MODE, key );
			byte[] iv = encrypter.getIV();

			// Encrypt, putting the IV in the front
			byte[] target = new byte[length+iv.length];
			System.arraycopy( iv, 0, target, 0, iv.length );
			encrypter.doFinal( buffer, offset, length, target, iv.length );
			return target;
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Could not encrypt: "+e.getMessage(), e );
		}
	}
	
	public static void encryptWithSymmetricKey( SecretKey key, Cipher cipher, Message message )
	{	
		try
		{
			// Initialize Cipher
			cipher.init( Cipher.ENCRYPT_MODE, key );
			byte[] iv = cipher.getIV();
			
			// Create the target buffer
			int payloadLength = message.getHeader().getPayloadLength();
			byte[] encrypted = new byte[Header.HEADER_LENGTH+iv.length+payloadLength];

			// Do the payload encryption
			cipher.doFinal( message.getBuffer(), Header.HEADER_LENGTH, payloadLength, encrypted, Header.HEADER_LENGTH+iv.length );
			
			// Copy in the header and IV
			System.arraycopy( message.getBuffer(), 0, encrypted, 0, Header.HEADER_LENGTH );
			System.arraycopy( iv, 0, encrypted, Header.HEADER_LENGTH, iv.length );
			
			// Replace the buffer in the message
			message.replaceBuffer( encrypted );
			message.getHeader().writeIsEncrypted( true );
			message.getHeader().writePayloadLength( payloadLength+iv.length );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Could not encrypt: "+e.getMessage(), e );
		}
	}
	
	/**
	 * 
	 * @param key
	 * @param cipherMode
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 */
	public static byte[] decryptWithSymmetricKey( SecretKey key,
	                                              CipherMode cipherMode,
	                                              byte[] buffer,
	                                              int offset,
	                                              int length )
	{
		try
		{
			// Read off the IV
			int ivLength = cipherMode.getIvSize();
			IvParameterSpec iv = new IvParameterSpec( buffer, offset, ivLength );
			
    		// Create Cipher Object
    		Cipher decrypter = Cipher.getInstance( cipherMode.getConfigString(), "BCFIPS" );
    
    		// Initialize Cipher
    		decrypter.init( Cipher.DECRYPT_MODE, key, iv );
    
    		// Encrypt
    		return decrypter.doFinal( buffer, offset+ivLength, length-ivLength );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Could not decrypt: "+e.getMessage(), e );
		}
	}

	public static void decryptWithSymmetricKey( SecretKey key, Cipher cipher, Message message )
	{
		byte[] buffer = message.getBuffer();
		
		try
		{
			// Read off the IV
			int ivLength = cipher.getBlockSize();
			IvParameterSpec iv = new IvParameterSpec( buffer, Header.HEADER_LENGTH, ivLength );
			
    		// Initialize Cipher
    		cipher.init( Cipher.DECRYPT_MODE, key, iv );
    
    		// Do decryption
    		final int payloadLength = message.getHeader().getPayloadLength()-ivLength;
    		final int payloadOffset = Header.HEADER_LENGTH+ivLength;
    		byte[] decrypted = new byte[Header.HEADER_LENGTH+payloadLength];
    		cipher.doFinal( buffer, payloadOffset, payloadLength, decrypted, Header.HEADER_LENGTH );
    		
    		// Copy the header over
    		System.arraycopy( buffer, 0, decrypted, 0, Header.HEADER_LENGTH );
    		
    		// Replace the buffer in the message
    		message.replaceBuffer( decrypted );
    		message.getHeader().writeIsEncrypted( false );
    		message.getHeader().writePayloadLength( payloadLength );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Could not decrypt: "+e.getMessage(), e );
		}
	}
	
	/**
	 * 
	 * @param buffer
	 * @param offset
	 * @param length
	 * @param cipherMode
	 * @return
	 */
	public static SecretKey decodeSymmetricKey( byte[] buffer,
	                                            int offset,
	                                            int length )
	{
		int keylength = length-offset;
		if( keylength != 16 && keylength != 24 && keylength != 32 )
			throw new IllegalArgumentException( "Key bit-length incorrect for AES (128, 192, 256): Found="+keylength );
 
		return new SecretKeySpec( buffer, offset, length, "AES" );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Public Key Methods   //////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 * @param key
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 */
	public static byte[] encryptWithRsaKey( Key key, byte[] buffer, int offset, int length )
	{
		try
		{
			Cipher cipher = Cipher.getInstance( "RSA" );
			cipher.init( Cipher.ENCRYPT_MODE, key );
			return cipher.doFinal( buffer, offset, length );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Could not encrypt: "+e.getMessage(), e );
		}
	}

	/**
	 * 
	 * @param key
	 * @param buffer
	 * @param offset
	 * @param length
	 * @return
	 */
	public static byte[] decryptWithRsaKey( Key key, byte[] buffer, int offset, int length )
	{
		try
		{
			Cipher cipher = Cipher.getInstance( "RSA" );
			cipher.init( Cipher.DECRYPT_MODE, key );
			return cipher.doFinal( buffer, offset, length );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Could not decrypt: "+e.getMessage(), e );
		}
	}

	
	public static PublicKey decodePublicKey( byte[] encoded )
	{
		try
		{
			KeyFactory factory = KeyFactory.getInstance( "RSA", "BCFIPS" );
			return factory.generatePublic( new X509EncodedKeySpec(encoded) );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Error decoding public key from bytes: "+e.getMessage(), e );
		}
	}
	
	 /**
	 * Read in a private key from a PEM formatted file. If the file is encrypted, you can
	 * provide the optional password. If the password is null, or 0-length, it is assumed
	 * that no password is present.
	 * 
	 * @param file The PEM format file that contains the private key
	 * @param password The password needed for the file (null or 0-length means no password needed)
	 * @return A key pair containing the private key and the public key derived from it
	 * @throws JConfigurationException If there is a problem reading or decoding the file
	 */
	public static KeyPair readPrivateKeyPemFile( File file, char[] password/*optional*/ )
		throws JConfigurationException
	{
		try
		{
			// Extract the PemObject (metadata) and Key Pair (hopefully) from the file
			PEMParser parser = new PEMParser( new FileReader(file) );
			Object temp = parser.readObject();
			parser.close();

			if( temp instanceof PEMKeyPair )
			{
				// Unencrypted
				PEMKeyPair pair = (PEMKeyPair)temp;
				return new JcaPEMKeyConverter().getKeyPair( pair );
			}
			else if( temp instanceof PEMEncryptedKeyPair )
			{
				// Encrypted
				if( password == null || password.length == 0 )
					throw new JConfigurationException( "Private Key load error: Encrypted, but no password provided. "+file );

				PEMEncryptedKeyPair pair = (PEMEncryptedKeyPair)temp;
				PEMDecryptorProvider pkcs8 = new JcePEMDecryptorProviderBuilder().setProvider( "BCFIPS" ).build( password );
				JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider( "BCFIPS" );
				return converter.getKeyPair( pair.decryptKeyPair( pkcs8 ) );
			}

			// We don't know what we read...
			throw new JConfigurationException( "Private Key load error: Unsupported (" +
			                                   temp.getClass() + ")" );
		}
		catch( IOException ioex )
		{
			throw new JConfigurationException( "Error reading PEM format private key file: " +
			                                   ioex.getMessage(), ioex );
		}
	}
	

	public static PublicKey readPublicKeyPemFile( File file ) throws JConfigurationException
	{
		try
		{
    		// Extract the PemObject (metadata) and Key Pair (hopefully) from the file
    		PEMParser parser = new PEMParser( new FileReader(file) );
    		Object temp = parser.readObject();
    		parser.close();
    
    		if( temp instanceof SubjectPublicKeyInfo )
    		{
    			SubjectPublicKeyInfo publicKeyInfo = (SubjectPublicKeyInfo)temp;
    			return new JcaPEMKeyConverter().getPublicKey( publicKeyInfo );
    		}
    		
    		// We don't know what we read...
    		throw new JConfigurationException( "Public Key load error: Unsupported ("+temp.getClass()+")" );
		}
		catch( IOException ioex )
		{
			throw new JConfigurationException( "Error reading PEM format public key file: "+ioex.getMessage(), ioex );
		}
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Horrible Hbrid Methods   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Messages encrypted with an RSA key can only have a max size of 501-512 bytes (assuming
	 * a key size of 4096). We have messages larger than that we need to encrypt. To get around
	 * this we generate a symmetric key, use that to encrypt the payload, and then encrypt the
	 * symmetric key with the public/private key and send it with the message. POW! <p/>
	 * 
	 * This method does just that. Repacking the result into the message.
	 * 
	 * @param key The RSA key we should encrypt the symmetric key we generate with
	 * @param message The message we should encrypt the contents of
	 */
	public static void encryptLongRsaMessage( Key key, Message message )
	{
		// Why is this so convoluted?
		// -----------------------------
		//     RSA can only encrypt messages up to a size of 501 bytes. Booo.
		//     To deal with large messages, we need a different approach.
		//     We will generate a symmetric key, then encrypt the data with that.
		//     We'll then encrypt the symmetric key with the public key.
		//      We'll then send everything together.
		//     RTI can decrypt the symmetric key, and then decrypt the payload using it.

		final int headerLength = Header.HEADER_LENGTH; // easier to read when using lots 'o times
		byte[] original = message.getBuffer();

		try
		{
			// Step 1. Generate a Symmetric Key
			SecretKey symmetricKey = AuthUtils.generateSymmetricKey(128);
			
			// Step 2. Encrypt the payload with the symmetric key
			byte[] encrypted = AuthUtils.encryptWithSymmetricKey( symmetricKey,
			                                                     CipherMode.defaultMode(),
			                                                     original,
			                                                     headerLength,
			                                                     message.getHeader().getPayloadLength() );
			
			// Step 3. Encrypt the key with the RTI public key
			byte[] encodedKey = symmetricKey.getEncoded();
			byte[] encryptedKey = AuthUtils.encryptWithRsaKey( key,
			                                                  encodedKey,
			                                                  0,
			                                                  encodedKey.length );
			
			// Step 4. Smash everything together
			byte[] finalVersion = new byte[headerLength+encryptedKey.length+encrypted.length];
			
			
			// Step 5. Update the message buffer
			System.arraycopy( original, 0, finalVersion, 0, headerLength );
			System.arraycopy( encryptedKey, 0, finalVersion, headerLength, encryptedKey.length );
			System.arraycopy( encrypted, 0, finalVersion, headerLength+encryptedKey.length, encrypted.length );
			message.replaceBuffer( finalVersion );
		}
		catch( JRTIinternalError jrtie )
		{
			throw jrtie;
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Error encrypting message with RTI public key: "+e.getMessage(), e );
		}
	}
	
	
	
	/**
	 * The structure of this message is expected to be:
	 * <ul>
	 *    <li>Portico Message Header</li>
	 *    <li>Symmetric Key (128-bit), encrypted with the opposite of the given RSA key</li>
	 *    <li>Payload, encrypted with the symmetric key from above</li>
	 * </ul>
	 * 
	 * This method will unpack all that and update the provided {@link Message} object with
	 * the decrypted contents.
	 * 
	 * @param key The key to decrypt the symmetric key (that we use to decrypt the message) with.
	 * @param message The message that has the encrypted contents we need to decrypt
	 */
	public static void decryptLongRsaMessage( Key key, Message message )
	{
		byte[] original = message.getBuffer();
		
		// Step 1. Get some information for later use
		int headerLength  = Header.HEADER_LENGTH;
		int symkeyLength  = 512;
		int payloadOffset = headerLength+symkeyLength;
		int payloadLength = original.length - payloadOffset;
		
		// Step 2. Decrypt the Symmetric Key
		//         The actual body is encrypted with a symmetric key.
		//         That key is encrypted with the opposite half of the given key.
		//         SymKey length is hard-coded as 128-bit for now.
		byte[] symmetricBytes = AuthUtils.decryptWithRsaKey( key, original, headerLength, 512/*RSA Enc*/ );
		SecretKey symmetricKey = AuthUtils.decodeSymmetricKey( symmetricBytes, 0, 16/*AES128*/ );
		
		// Step 3. Decrypt the payload content using the symmetric key
		byte[] decrypted = AuthUtils.decryptWithSymmetricKey( symmetricKey,
		                                                     CipherMode.defaultMode(),
		                                                     original,
		                                                     payloadOffset,
		                                                     payloadLength );
		
		// Step 4. Create a new buffer to hold the decrypted contents
		byte[] finalVersion = new byte[Header.HEADER_LENGTH+decrypted.length];
		System.arraycopy( original, 0, finalVersion, 0, headerLength );
		System.arraycopy( decrypted, 0, finalVersion, headerLength, decrypted.length );
		
		// Step 5. Put the decrypted version into the message
		message.replaceBuffer( finalVersion );
	}
}
