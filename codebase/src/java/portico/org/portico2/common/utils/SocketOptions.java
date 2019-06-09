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
package org.portico2.common.utils;

/**
 * Class to clearly encapsulate the set of various socket options we might want to apply
 * to a socket.
 */
public class SocketOptions
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public int recvBufferSize  = (int)ByteUnit.MEGABYTES.toBytes( 4 );
	public int sendBufferSize  = (int)ByteUnit.MEGABYTES.toBytes( 4 );

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Accessor and Mutator Methods   /////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public void setSendBufferSize( int value )
	{
		this.sendBufferSize = value;
	}
	
	public void setSendBufferSize( int value, ByteUnit unit )
	{
		this.sendBufferSize = (int)unit.toBytes( value );
	}
	
	public int getSendBufferSize()
	{
		return this.sendBufferSize;
	}
	
	public void setRecvBufferSize( int value )
	{
		this.recvBufferSize = value;
	}
	
	public void setRecvBufferSize( int value, ByteUnit unit )
	{
		this.recvBufferSize = (int)unit.toBytes( value );
	}
	
	public int getRecvBufferSize()
	{
		return this.recvBufferSize;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}