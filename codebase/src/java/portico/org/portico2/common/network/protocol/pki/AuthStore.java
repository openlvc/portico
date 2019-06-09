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

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.portico2.common.PorticoConstants;

/**
 * Only used within RTI.
 */
public class AuthStore
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Random RANDOM = new Random();

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<Short,PublicKey> authTokens;  // key=authToken

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected AuthStore()
	{
		this.authTokens = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////

	public short authenticate( PublicKey publicKey )
	{
		int authToken = RANDOM.nextInt( PorticoConstants.MAX_AUTH_TOKEN );
		while( authTokens.containsKey((short)authToken) )
			authToken = RANDOM.nextInt( PorticoConstants.MAX_AUTH_TOKEN );
		
		authTokens.put( (short)authToken, publicKey );
		return (short)authToken;
	}

	public PublicKey getKeyForToken( short token )
	{
		return authTokens.get( token );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
