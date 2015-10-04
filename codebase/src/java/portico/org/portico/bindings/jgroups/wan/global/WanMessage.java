/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups.wan.global;

public class WanMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private byte header;
	private Host source;
	public byte[] buffer;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public WanMessage( Host source, byte[] buffer )
	{
		this( Header.RELAY, source, buffer );
	}

	public WanMessage( byte header, Host source, byte[] buffer )
	{
		this.header = header;
		this.source = source;
		this.buffer = buffer;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public byte getHeader()
	{
		return this.header;
	}

	public final Host getSource()
	{
		return this.source;
	}

	public byte[] getBuffer()
	{
		return this.buffer;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
