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
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Message;
import org.portico2.common.network.ResponseCorrelator;
import org.portico2.common.network.configuration.PublicKeyConfiguration;
import org.portico2.common.network.configuration.protocol.ProtocolConfiguration;
import org.portico2.common.network.protocol.Protocol;
import org.portico2.common.services.federation.msg.Authenticate;
import org.portico2.common.services.federation.msg.WelcomePack;

public class LRCPublicKeyProtocol extends Protocol
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	static { Security.addProvider( new BouncyCastleFipsProvider() ); }

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private PublicKeyConfiguration configuration;

	private boolean isEnabled;
	private AuthStatus authStatus;
	private short authToken;
	
	// Keys
	private PrivateKey fedPrivate;
	private PublicKey  fedPublic;
	private PublicKey  rtiPublic;

	// Symmetric Encryption
	private SecretKey  sessionKey;
	private Cipher     encryptCipher;
	private Cipher     decryptCipher;

	// Authentication process state
	private int authenticationRequestId;
	private int joinRequestId;
	private int resignRequestId;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LRCPublicKeyProtocol()
	{
		this.configuration = null;   // set in doConfigure()
		this.isEnabled = false;      // set in doConfigure()
		this.authStatus = AuthStatus.NotAuthenticated;
		this.authToken = 0;          // set when we receive response to authenticate request
		
		// Keys
		this.fedPrivate = null;      // set in doConfigure()
		this.fedPublic = null;       // set in doConfigure()
		this.rtiPublic = null;       // set in doConfigure()
		
		// Symmetric Encryption
		this.sessionKey = null;      // set when federate joins
		this.encryptCipher = null;   // set in doConfigure()
		this.decryptCipher = null;   // set in doConfigure()
		
		// Authentication process state
		this.authenticationRequestId = -1; // set in authenticate()
		this.joinRequestId = -1;           // set when we see a join request go out
		this.resignRequestId = -1;         // set when we see a resign request go out
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Lifecycle Management   ////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void doConfigure( ProtocolConfiguration givenConfiguration, Connection hostConnection )
		throws JConfigurationException
	{
		//this.configuration = hostConnection.getConfiguration().getPublicKeyConfiguration();
		//this.isEnabled = configuration.isEnabled(); // FIXME TEMP ON
		this.isEnabled = true;
		
		// load the private key file
		KeyPair pair = AuthUtils.readPrivateKeyPemFile( configuration.getPrivateKey(),
		                                                configuration.getPrivateKeyPassword() );
		this.fedPrivate = pair.getPrivate();
		this.fedPublic = pair.getPublic();
		
		// load the rti public key file
		this.rtiPublic = AuthUtils.readPublicKeyPemFile( configuration.getRtiPublicKey() );
		
		// Symmetric Keys
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
	{
		this.authStatus = AuthStatus.NotAuthenticated;
	}

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
		passDown(message);
		
//		// Skip if not enabled
//		if( isEnabled == false )
//		{
//			passDown(message);
//			return;
//		}
//
//		if( message.getMessageType().isFederationMessage() )
//		{
//			// if this is a federation message and we are authenticated, we need
//			// to be encrypting it with the session key
//			encryptSymmetric( message );
//		}
//		else
//		{
//			// If we are disconnected this will be an RtiProbe, still, just pass
//			// it straight down
//			if( hostConnection.getStatus() == Status.Disconnected )
//			{
//				passDown( message );
//				return;
//			}
//
//			// We only both authenticating when non-federation messages are sent.
//			// To join a federation you need to at least send an RtiProbe (which
//			// we ignore) and then a JoinRequest (Control), so you can't do any
//			// data messages without getting past us here, and this lets us quickly
//			// keep data messages on a fast-path
//			// We are connected, but what we do depends on whether we are authenticated yet
//			if( authStatus == AuthStatus.NotAuthenticated )
//			{
//				// We ARE NOT Authenticated
//				// Run that process and then drop through
//				authenticate();
//			}
//			
//			if( authStatus == AuthStatus.Authenticated )
//			{
//				// We ARE Authenticated.
//				// Insert the Auth Header
//				message.getHeader().writeAuthToken( authToken );
//				
//				// If the message is a NOT a federation message, then we need to encrypt
//				// it with the RTI's public key.
//				AuthUtils.encryptLongRsaMessage(rtiPublic,message);
//				
//				// If this is a message of keen interest for us, store the request id
//				if( message.getMessageType() == MessageType.JoinFederation )
//					this.joinRequestId = message.getRequestId();
//			}
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
//		//
//		// Status: Authenticated.
//		//         We are authenticated, which means that we are encrypting all
//		//         federation messages and need to reverse that. Messages passing
//		//         up the stack are typically response messages, which means we
//		//         have no way of telling if they area federation or non-federtion
//		//         messages. To deal with this, we'll just work on anything that
//		//         includes an auth-token header that is the same as ours.
//		if( authStatus == AuthStatus.Authenticated )
//		{
//			if( message.getHeader().isEncrypted() )
//				; // symdecrypt
//			
//			if( message.getHeader().getAuthToken() == this.authToken )
//				AuthUtils.decryptLongRsaMessage( fedPrivate, message );
//			
//			// is this one of the special messages are are interested in?
//			if( message.getRequestId() == this.joinRequestId )
//				joinedFederation( message );
//		}
//		//
//		// Status: Authenticating...
//		//         Process a response to our Authenticate request message
//		//
//		else if( authStatus == AuthStatus.Authenticating )
//		{
//			if( message.getRequestId() == this.authenticationRequestId )
//			{
//				synchronized( this )
//				{
//					// Decrypt the message first
//					AuthUtils.decryptLongRsaMessage( fedPrivate, message );
//					
//    				// Extract the auth token and store it
//    				ResponseMessage response = message.inflateAsResponse();
//    				if( response.isError() )
//    				{
//    					logger.error( "Authentication Failed. RTI responded with error." );
//    					authStatus = AuthStatus.NotAuthenticated;
//    				}
//    				else
//    				{
//    					authToken = response.getSuccessResultAsShort();
//    					authStatus = AuthStatus.Authenticated;
//    					authenticationRequestId = -1;
//    					this.notifyAll();
//    				}
//    				return;
//				}
//			}
//		}
//		
//		// pass it further up the chain if we haven't bailed early
//		passUp( message );
	}

	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Authentication Process Methods   //////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void authenticate()
	{
		logger.debug( "Not authenticated; starting authentication process" );
		
		// Create the request
		Authenticate request = new Authenticate( fedPublic );
		
		// Turn it into a message with an ID we can look for on the way back up
		this.authenticationRequestId = ResponseCorrelator.getUnregisteredRandomID();
//		Message message = new Message( request, CallType.ControlSync, authenticationRequestId );
		
		// Encrypt it so only the RTI sees it
//		AuthUtils.encryptLongRsaMessage( rtiPublic, message );
		
		// Send the request out
		// We're going to block for a period of time after this and wait on
		// a signal, so we need the lock first
		synchronized( this )
		{
			logger.debug( "Sending authentication request to RTI" );
			authStatus = AuthStatus.Authenticating;
//			passDown( message );
			
			try
			{
				this.wait( 2000 );
			}
			catch( InterruptedException ie )
			{
				// we must be shutting down; let's bail
				return;
			}
			
			// Check the status (we'll do this while we have the lock to avoid thread order issues)
			// We've either timed out or been notified, let's have a look
			if( authStatus == AuthStatus.Authenticated )
			{
				// make sure we have a token
				if( authToken == 0 )
					throw new JRTIinternalError( "Status is Authenticated, but we have no token (token=0)" );
				else
					logger.debug( "Authentication successful. AuthToken is: %04x", authToken );
			}
			else if( authStatus == AuthStatus.Authenticating )
			{
				logger.error( "Timeout waiting for authentication response; could not authenticate" );
				throw new JRTIinternalError( "Timeout waiting for authentication response" );
			}
			else
			{
				// We got a NO
				logger.error( "Authentication status has been set back to NotAuthenticated. "+
				              "Authentication must have failed" );
				throw new JRTIinternalError( "Authentication failed" );
			}
		}
		
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Encryption Methods   //////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void encryptSymmetric( Message message )
	{
		if( sessionKey == null )
			return;

		AuthUtils.encryptWithSymmetricKey( sessionKey, encryptCipher, message );
	}
	
	private void decryptSymmetric( Message message )
	{
		if( sessionKey == null )
			return;

		AuthUtils.decryptWithSymmetricKey( sessionKey, decryptCipher, message );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Messages Requiring Special Handling   /////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	private void joinedFederation( Message message )
	{
		// get the session key
		ResponseMessage response = message.inflateAsResponse();
		WelcomePack welcome = (WelcomePack)response.getResult();
		byte[] federationKey = welcome.getFederationKey();
		
		// turn the bytes into a key
		this.sessionKey = AuthUtils.decodeSymmetricKey( federationKey, 0, federationKey.length );
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
