/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.channel;

/**
 * This is an enumeration listing all the possible headers for a PTalk packet. While adding
 * additional headers is possible, it isn't entirely straightforward and there are a few pitfalls
 * to watch out for. For this reason, pay careful attention to how the Header's are stored and
 * used in the {@link Headers} class before trying to add your own.
 */
public enum Header
{
	//----------------------------------------------------------
	//                   ENUMERATION VALUES
	//----------------------------------------------------------
	SERIAL              ( (byte)0 ),
	GM                  ( (byte)1 ),
	FederationManagement( (byte)2 ),
	SentByBridgeID      ( (byte)3 );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private final byte fieldIndex;
	private final int flag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Header( byte fieldIndex )
	{
		this.fieldIndex = fieldIndex;
		this.flag = (int)Math.pow( 2, fieldIndex );
		//this.flag = 2 ^ fieldIndex;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public byte index()
	{
		return this.fieldIndex;
	}
	
	public int flag()
	{
		return this.flag;
	}
	
	/**
	 * Returns the total number of headers that there are. This is used by the {@link Headers}
	 * class to figure out how big the bit-field it uses to identify which headers are present
	 * should be.
	 */
	public static int count()
	{
		return Header.values().length;
	}
}
