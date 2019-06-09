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

import java.util.Arrays;

public enum CipherMode
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	/* I've ruled out the first two options due to the following:
	 *   ECB: No IV & Block mode (see CBC)
	 *   CBC: Block mode gives us variable payload sizes that are more work to encode/decode
	 */
	//ECB( 0, "AES/ECB/PKCS7Padding", true, 16 ),// Electronic Codebook            // no IV
	//CBC( 0, "AES/CBC/PKCS7Padding", true, 16 ),// Cipher Block Chaining          // variable payload
	CFB( 0, "AES/CFB/NoPadding" ),               // Cipher Feedback 
	CTR( 1, "AES/CTR/NoPadding" ),               // Counter Mode     **default**
	CTS( 2, "AES/CBC/CS3Padding" );              // CBC with Ciphertext Stealing (CTS)
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int id;
	private String configString;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private CipherMode( int id, String configString )
	{
		this.id = id;
		this.configString = configString;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getConfigString()
	{
		return this.configString;
	}

	@Override
	public String toString()
	{
		return this.configString;
	}

	public final int getId()
	{
		return this.id; // gets downcast to _HALF_ a byte (unsigned). Range 0-15.
	}

	public final int getCipherTextSize( int plainTextSize )
	{
		// currently, all supported modes generate a cipher text that is the
		// same size as the original plain text
		return plainTextSize;
	}

	public final int getIvSize()
	{
		// currently, we only support AES which has a 128-bit block size, so that's what IV is
		return 16;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static CipherMode fromId( int id )
	{
		switch( id )
		{
			case 1: return CFB;
			case 2: return CTR;
			case 3: return CTS;
			default: throw new IllegalArgumentException( "Cipher Mode id not known: "+id );
		}
	}

	public static CipherMode defaultMode()
	{
		return CipherMode.CTR;
	}

	public static CipherMode fromConfigString( String configString )
	{
		for( CipherMode mode : CipherMode.values() )
		{
			if( mode.configString.equals(configString) )
				return mode;
		}
		
		throw new IllegalArgumentException( "Unknown/Unsupported Cipher Config: "+configString+
		                                    ". Valid values: "+Arrays.toString(CipherMode.values()) );
	}

}
