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
package org.portico2.common.network.protocols.auth;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.util.Random;

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.portico2.common.PorticoConstants;
import org.portico2.common.messaging.MessageType;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Message;
import org.portico2.common.network.Protocol;
import org.portico2.common.network.configuration.AuthConfiguration;
import org.portico2.common.services.federation.msg.Authenticate;

public class RTIAuthenticationProtocol extends Protocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static { Security.addProvider( new BouncyCastleFipsProvider() ); }

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private AuthConfiguration configuration;

	private boolean isEnabled;
	
	// Keys
	private PrivateKey rtiPrivate;
	private PublicKey  rtiPublic;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RTIAuthenticationProtocol()
	{
		this.configuration = null;   // set in doConfigure()
		this.isEnabled = false;      // set in doConfigure()
		
		// Keys
		this.rtiPrivate = null;      // set in doConfigure()
		this.rtiPublic = null;       // set in doConfigure()
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doConfigure( Connection hostConnection )
	{
		this.configuration = hostConnection.getConfiguration().getAuthConfiguration();
		//this.isEnabled = configuration.isEnabled(); // FIXME TEMP ON
		this.isEnabled = true;
		
		// load the private key file
		KeyPair pair = AuthUtils.readPrivateKeyPemFile( configuration.getPrivateKey(),
		                                               configuration.getPrivateKeyPassword() );
		this.rtiPrivate = pair.getPrivate();
		this.rtiPublic = pair.getPublic();
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
		// Skip if not enabled
		if( !isEnabled )
		{
			passDown(message);
			return;
		}
		
		// FIXME Start Here - Identify when this is a response that requires encryption and do it
		
		// Let this one slide down to the next sucka
		passDown( message );
	}
	
	@Override
	public void up( Message message )
	{
		if( !isEnabled )
		{
			passUp( message );
			return;
		}

		//
		// Authentication Request
		// We have received an Auth request. We need to process this entirely
		// locally within the protocol. We'll decrypt the message using our
		// private key and then do an authentication process, returning a token
		// that can be used later.
		//
		if( message.getMessageType() == MessageType.Authenticate )
		{
			logger.debug( "Received authentication request, processing" );
			
			// decrypt the contents
			AuthUtils.decryptLongRsaMessage( rtiPrivate, message );
			
			// do the authentication, re-populating the message with a response that
			// has been encrypted with the federate's public key
			authenticate( message );

			// pass the result back down and bug out!
			passDown( message );
			return;
		}
		
		//
		// Auth Token Present
		// The auth token is present on this message _and_ it is a non-federation
		// message which means it will be encrytped with our public key. Decrypt
		// it and let is pass up the stack
		//
		if( message.getMessageType().isFederationMessage() == false )
		{
			short authToken = message.getHeader().getAuthToken();
			if( authToken != PorticoConstants.NO_AUTH_TOKEN )
				AuthUtils.decryptLongRsaMessage( rtiPrivate, message );
			else
				System.out.println( "RECEIVED "+message.getMessageType()+", but no auth token" );
		}
		
		passUp( message );
	}

	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Encryption and Decryption Methods   ///////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////

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
		short authToken = (short)(new Random().nextInt(65536)-32768);
		logger.error( "Generated auth token %04x", authToken );
		
		// Step 3. Send back a response containing the auth token
		ResponseMessage response = ResponseMessage.success( authToken );
		message.deflateAndStoreResponse( response );
		
		// Step x. Encrypt the contents with the federate's public key
		AuthUtils.encryptLongRsaMessage( fedPublic, message );
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
