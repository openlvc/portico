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

import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.network.CallType;
import org.portico2.common.network.Connection;
import org.portico2.common.network.Connection.Status;
import org.portico2.common.network.Message;
import org.portico2.common.network.Protocol;
import org.portico2.common.network.ResponseCorrelator;
import org.portico2.common.network.configuration.AuthConfiguration;
import org.portico2.common.services.federation.msg.Authenticate;

public class LRCAuthenticationProtocol extends Protocol
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
	private AuthStatus authStatus;
	private short authToken;
	
	// Keys
	private PrivateKey fedPrivate;
	private PublicKey  fedPublic;
	private PublicKey  rtiPublic;

	// Authentication process state
	private int authenticationRequestId;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public LRCAuthenticationProtocol()
	{
		this.configuration = null;   // set in doConfigure()
		this.isEnabled = false;      // set in doConfigure()
		this.authStatus = AuthStatus.NotAuthenticated;
		this.authToken = 0;          // set when we receive response to authenticate request
		
		// Keys
		this.fedPrivate = null;      // set in doConfigure()
		this.fedPublic = null;       // set in doConfigure()
		this.rtiPublic = null;       // set in doConfigure()
		
		// Authentication process state
		this.authenticationRequestId = -1; // set in authenticate()
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
		this.fedPrivate = pair.getPrivate();
		this.fedPublic = pair.getPublic();
		
		// load the rti public key file
		this.rtiPublic = AuthUtils.readPublicKeyPemFile( configuration.getRtiPublicKey() );
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
		// Skip if not enabled
		if( !isEnabled )
		{
			passDown(message);
			return;
		}
		
		// If we are disconnected currently, do nothing
		if( hostConnection.getStatus() == Status.Disconnected )
		{
			passDown( message );
			return;
		}
		
		// We are connected, but what we do depends on whether we are authenticated yet
		if( authStatus == AuthStatus.NotAuthenticated )
		{
			// We ARE NOT Authenticated
			// Run that process and then drop through
			authenticate();
		}
		
		if( authStatus == AuthStatus.Authenticated )
		{
			// We ARE Authenticated.
			// Insert the Auth Header
			message.getHeader().writeAuthToken( authToken );
			
			// If the message is a NOT a federaion message, then we need to encrypt
			// it with the RTI's public key.
			if( !message.getMessageType().isFederationMessage() )
				AuthUtils.encryptLongRsaMessage(rtiPublic,message);
		}
		
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
		// Status: Authenticated.
		//         We are authenticated, which means that we are encrypting all
		//         federation messages and need to reverse that. Messages passing
		//         up the stack are typically response messages, which means we
		//         have no way of telling if they area federation or non-federtion
		//         messages. To deal with this, we'll just work on anything that
		//         includes an auth-token header that is the same as ours.
		if( authStatus == AuthStatus.Authenticated )
		{
			if( message.getHeader().getAuthToken() == this.authToken )
				AuthUtils.decryptLongRsaMessage( fedPrivate, message );
		}
		//
		// Status: Authenticating...
		//         Process a response to our Authenticate request message
		//
		else if( authStatus == AuthStatus.Authenticating )
		{
			if( message.getRequestId() == this.authenticationRequestId )
			{
				synchronized( this )
				{
					// Decrypt the message first
					AuthUtils.decryptLongRsaMessage( fedPrivate, message );
					
    				// Extract the auth token and store it
    				ResponseMessage response = message.inflateAsResponse();
    				if( response.isError() )
    				{
    					logger.error( "Authentication Failed. RTI responded with error." );
    					authStatus = AuthStatus.NotAuthenticated;
    				}
    				else
    				{
    					authToken = response.getSuccessResultAsShort();
    					authStatus = AuthStatus.Authenticated;
    					authenticationRequestId = -1;
    					this.notifyAll();
    				}
    				return;
				}
			}
		}
		
		// pass it further up the chain if we haven't bailed early
		passUp( message );
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
		Message message = new Message( request, CallType.ControlSync, authenticationRequestId );
		
		// Encrypt it so only the RTI sees it
		AuthUtils.encryptLongRsaMessage( rtiPublic, message );
		
		// Send the request out
		// We're going to block for a period of time after this and wait on
		// a signal, so we need the lock first
		synchronized( this )
		{
			logger.debug( "Sending authentication request to RTI" );
			authStatus = AuthStatus.Authenticating;
			passDown( message );
			
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
