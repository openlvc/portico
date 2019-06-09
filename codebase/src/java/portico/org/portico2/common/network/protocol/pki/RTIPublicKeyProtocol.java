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

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.Connection;import org.portico2.common.network.Message;
import org.portico2.common.network.configuration.PublicKeyConfiguration;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.protocol.Protocol;
import org.portico2.common.services.federation.msg.Authenticate;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.rti.RTI;
import org.portico2.rti.federation.Federation;
import org.portico2.rti.federation.FederationManager;

/**
 * Does Authentication and encryption of non-federation messages. Delegates encryption of
 * federation messages to 
 */
public class RTIPublicKeyProtocol extends Protocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static { Security.addProvider( new BouncyCastleFipsProvider() ); }

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private PublicKeyConfiguration configuration;
	private AuthStore authStore;
	private boolean isEnabled;

	// Links to the RTI
	private FederationManager federationManager;
	
	// Keys
	private PrivateKey rtiPrivate;
	private PublicKey  rtiPublic;
	
	// Symmetric Keys
	private Cipher encryptCipher;
	private Cipher decryptCipher;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RTIPublicKeyProtocol()
	{
		this.configuration = null;   // set in doConfigure()
		this.authStore = new AuthStore();
		this.isEnabled = false;      // set in doConfigure()

		// Links to RTI
		this.federationManager = null; // set in doConfigure()
		
		// Keys
		this.rtiPrivate = null;      // set in doConfigure()
		this.rtiPublic = null;       // set in doConfigure()
		
		// Symmetric
		this.encryptCipher = null;   // set in doConfigure()
		this.decryptCipher = null;   // set in doConfigure()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doConfigure( ProtocolConfiguration givenConfiguration, Connection hostConnection )
	{
		//this.configuration = hostConnection.getConfiguration().getPublicKeyConfiguration();
		//this.isEnabled = configuration.isEnabled(); // FIXME TEMP ON
		this.isEnabled = true;

		// get a reference to the federation manager
		this.federationManager = hostConnection.getHostReference(RTI.class).getFederationManager();
		
		// load the private key file
		KeyPair pair = AuthUtils.readPrivateKeyPemFile( configuration.getPrivateKey(),
		                                               configuration.getPrivateKeyPassword() );
		this.rtiPrivate = pair.getPrivate();
		this.rtiPublic = pair.getPublic();
		
		// Symmetric
		try
		{
//			this.encryptCipher = Cipher.getInstance( configuration.getSessionCipher().getConfigString(), "BCFIPS" );
//			this.decryptCipher = Cipher.getInstance( configuration.getSessionCipher().getConfigString(), "BCFIPS" );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( e.getMessage(), e );
		}
	}

	@Override
	public void open()
	{}

	@Override
	public void close()
	{}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Message Passing   /////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void down( Message message )
	{
		// FIXME Temporary while committing large changes to header structure and moving to XML config
		passDown( message );

//		// Skip if not enabled
//		if( !isEnabled )
//		{
//			passDown(message);
//			return;
//		}
//		
//		// Encrypt the message with the federates public key if the following is TRUE:
//		//   - Message is a Response
//		//   - Original request was NOT a federation message
//		//   - Original request had an AUTH token
//		//
//		if( message.getCallType() == CallType.ControlResp )
//		{
//			if( message.getOriginalHeader().getMessageType() == MessageType.CreateFederation )
//				federationCreated( message );
//
//			if( message.getOriginalHeader().getMessageType().isFederationMessage() == false )
//				encryptResponseRsa( message );
//		}
//		else
//		{
//			if( message.getMessageType().isFederationMessage() )
//				; // symencrypt
//		}
//		
//		// Let this one slide down to the next sucka
//		passDown( message );
	}
	
	@Override
	public void up( Message message )
	{
		// FIXME Temporary while committing large changes to header structure and moving to XML config
		passUp( message );

//		if( !isEnabled )
//		{
//			passUp( message );
//			return;
//		}
//
//		// A Federation Message, won't be public key, only symc key
//		// Check to see if we have a key for the federation and encrypt it
//		if( message.getMessageType().isFederationMessage() )
//		{
//			if( message.getHeader().isEncrypted() )
//			{
//				decryptSymmetric(message); // symdecrypt
//			}
//
//			passUp( message );
//			return;
//		}
//		
//		//
//		// Authentication Request
//		// We have received an Auth request. We need to process this entirely
//		// locally within the protocol. We'll decrypt the message using our
//		// private key and then do an authentication process, returning a token
//		// that can be used later.
//		//
//		if( message.getMessageType() == MessageType.Authenticate )
//		{
//			logger.debug( "Received authentication request, processing" );
//			
//			// decrypt the contents
//			AuthUtils.decryptLongRsaMessage( rtiPrivate, message );
//			
//			// do the authentication, re-populating the message with a response that
//			// has been encrypted with the federate's public key
//			authenticate( message );
//
//			// pass the result back down and bug out!
//			passDown( message );
//			return;
//		}
//		
//		//
//		// Auth Token Present
//		// The auth token is present on this message _and_ it is a non-federation
//		// message which means it will be encrytped with our public key. Decrypt
//		// it and let is pass up the stack
//		//
//		if( message.getMessageType().isFederationMessage() == false )
//		{
//			short authToken = message.getHeader().getAuthToken();
//			if( authToken == PorticoConstants.NO_AUTH_TOKEN )
//			{
//				if( configuration.isEnforced() && message.getMessageType() != MessageType.RtiProbe )
//				{
//					logger.warn( "Dropped message (%s): Missing authentication information", message.getMessageType() );
//					return;
//				}
//			}
//			else
//			{
//				AuthUtils.decryptLongRsaMessage( rtiPrivate, message );
//			}
//		}
//		
//		passUp( message );
	}

	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Federation Management Methods   ///////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void federationCreated( Message message )
	{
		// What is the handle of the federation that was created
		ResponseMessage response = message.getResponse();
		if( response.isError() )
			return;

		int federationHandle = response.getSuccessResultAsInt( CreateFederation.KEY_FEDERATION_HANDLE );
		
		// Get the federation associated with that handle from within the RTI
		Federation federation = federationManager.getFederation( federationHandle );
		
		// Generate a new session key and store it in the federation
		SecretKey sessionKey = AuthUtils.generateSymmetricKey( configuration.getSessionKeyLength() );
		federation.setFedetrationKey( sessionKey );
	}
	
	private void federateDisconnected( Message message )
	{
		// revoke the auth token
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Authentication Process Methods   //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void authenticate( Message message )
	{
		// We need to extract the original request so we can get
		// the federate's public key from it. From there, we can
		// do our authentication, generate a token and return
		Authenticate request = message.inflateAsPorticoMessage( Authenticate.class );
		PublicKey fedPublic = AuthUtils.decodePublicKey( request.getKeyBytes() );
		
		// do something
		short authToken = (short)authStore.authenticate( fedPublic );
		logger.error( "Generated auth token %04x", authToken );
		
		// Step 3. Send back a response containing the auth token
		ResponseMessage response = ResponseMessage.success( authToken );
		message.deflateAndStoreResponse( response );
		
		// Step x. Encrypt the contents with the federate's public key
		AuthUtils.encryptLongRsaMessage( fedPublic, message );
	}

	/**
	 * Add the auth token from the original request to the response and encrypt
	 * the contents of the message with the federate's public key. If we can't
	 * find a key, log and error and terminate processing. The return value indicates
	 * whether we should continue processing this message or not. If false, the
	 * message should not be passed down.
	 * 
	 * @param message The message to add the auth token header to and encrypt
	 * @return False if we should stop processing this and not pass it down.
	 */
	private boolean encryptResponseRsa( Message message )
	{
//		short authToken = message.getOriginalHeader().getAuthToken();
//		
//		if( authToken == PorticoConstants.NO_AUTH_TOKEN )
//			return true; // let is pass, but not auth token to encrypt with
//
//		// we have an Auth token, find the public key to use
//		PublicKey fedPublic = authStore.getKeyForToken( authToken );
//		if( fedPublic == null )
//		{
//			logger.error( "Could not find key for Auth Token: %04x. Dropping response (%s) to (%s)",
//			              (short)authToken,
//			              message.getMessageType(),
//			              message.getOriginalHeader().getMessageType() );
//			return false;
//		}
//
//		// encrypt the message
//		AuthUtils.encryptLongRsaMessage( fedPublic, message );
//		
//		// record the auth token in the response header
//		message.getHeader().writeAuthToken( authToken );
		return true;
	}

	private void encryptSymmetric( Message message )
	{
		Federation federation = federationManager.getFederation( message.getHeader().getFederation() );
		if( federation == null )
			return;

		AuthUtils.encryptWithSymmetricKey( federation.getFederationKey(), encryptCipher, message );
	}
	
	private void decryptSymmetric( Message message )
	{
		Federation federation = federationManager.getFederation( message.getHeader().getFederation() );
		if( federation == null )
			return;

		AuthUtils.decryptWithSymmetricKey( federation.getFederationKey(), decryptCipher, message );
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName()
	{
		return "Auth";
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
