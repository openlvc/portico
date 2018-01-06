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
package org.portico.bindings.jgroups.channel;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Supplier;

import org.jgroups.Header;
import org.jgroups.conf.ClassConfigurator;

/**
 * Portico Group Management System (GMS) messages all contain a UUID that represents the
 * process sending the request. Within a single local network, the JGroups Address's would
 * be enough to uniquely pinpoint a particular federate, but in the context of a WAN there
 * could be some overlap, so we use UUIDs for GMS messages instead.
 * 
 * This header should be attached to all GMS messages, but not to regular messages.
 */
public class UUIDHeader extends Header
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// P = 16, Message 01
	public static final short HEADER = 1601;
	static
	{
		// register the header type
		ClassConfigurator.add( HEADER, UUIDHeader.class );
	}

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private UUID uuid;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public UUIDHeader()
	{
		this.uuid = null;
	}
	
	public UUIDHeader( UUID uuid )
	{
		this.uuid = uuid;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public UUID getUUID()
	{
		return this.uuid;
	}

	@Override
	public int serializedSize()
	{
		return 16;
	}

	@Override
	public short getMagicId()
	{
		return HEADER;
	}
	
	@Override
	public Supplier<? extends Header> create()
	{
		return UUIDHeader::new;
	}

	public void writeTo( DataOutput out ) throws IOException
	{
		out.writeLong( uuid.getMostSignificantBits() );
		out.writeLong( uuid.getLeastSignificantBits() );
	}

	public void readFrom( DataInput in ) throws IOException,
												IllegalAccessException,
												InstantiationException
	{
		this.uuid = new UUID( in.readLong(), in.readLong() );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
