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
import java.util.function.Supplier;

import org.jgroups.Global;
import org.jgroups.Header;
import org.jgroups.conf.ClassConfigurator;

/**
 * Some messages exchanged among group members are federation control messages. Messages like
 * createFederation. This header is used to designate a message as a federation control message.
 * The specific control message that is in use depends on the value inside the header.
 */
public class ControlHeader extends Header
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// P = 16, Message 02
	public static final short HEADER                 = 1602;
	public static final short FIND_COORDINATOR       = 1;
	public static final short SET_MANIFEST           = 2; // sent in reply to FIND_COORDINATOR

	public static final short CREATE_FEDERATION      = 3;
	public static final short JOIN_FEDERATION        = 4;
	public static final short RESIGN_FEDERATION      = 5;
	public static final short DESTROY_FEDERATION     = 6;
	
	public static final short GOODBYE                = 100; // sent when disconnecting from channel

	static
	{
		// register the header type
		ClassConfigurator.add( HEADER, ControlHeader.class );
	}

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private short messageType;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ControlHeader()
	{
		this.messageType = 0;
	}

	private ControlHeader( short type )
	{
		this.messageType = type;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public short getMessageType()
	{
		return this.messageType;
	}

	@Override
	public int serializedSize()
	{
		return Global.SHORT_SIZE;
	}

	@Override
	public short getMagicId()
	{
		return HEADER;
	}

	@Override
	public Supplier<? extends Header> create()
	{
		return ControlHeader::new;
	}

	public void writeTo( DataOutput out) throws IOException
	{
		out.writeShort( messageType );
	}
	
	public void readFrom( DataInput in ) throws IOException,
												IllegalAccessException,
												InstantiationException
	{
		this.messageType = in.readShort();
	}
	
	public String toString()
	{
		switch( messageType )
		{
			case FIND_COORDINATOR:
				return "FindCoordinator";
			case SET_MANIFEST:
				return "SetManifest";
			case CREATE_FEDERATION:
				return "CreateFederation";
			case JOIN_FEDERATION:
				return "JoinFederation";
			case RESIGN_FEDERATION:
				return "ResignFederation";
			case DESTROY_FEDERATION:
				return "DestroyFederation";
			case GOODBYE:
				return "Goodbye";
			default:
				return "Unknown";
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static ControlHeader findCoordinator()
	{
		return new ControlHeader( FIND_COORDINATOR );
	}
	
	public static ControlHeader setManifest()
	{
		return new ControlHeader( SET_MANIFEST );
	}

	public static ControlHeader newCreateHeader()
	{
		return new ControlHeader( CREATE_FEDERATION );
	}
	
	public static ControlHeader newJoinHeader()
	{
		return new ControlHeader( JOIN_FEDERATION );
	}

	public static ControlHeader newResignHeader()
	{
		return new ControlHeader( RESIGN_FEDERATION );
	}
	
	public static ControlHeader newDestroyHeader()
	{
		return new ControlHeader( DESTROY_FEDERATION );
	}
	
	public static ControlHeader goodbye()
	{
		return new ControlHeader( GOODBYE );
	}
}
