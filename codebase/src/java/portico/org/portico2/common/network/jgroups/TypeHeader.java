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
package org.portico2.common.network.jgroups;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Supplier;

import org.jgroups.Global;
import org.jgroups.Header;

/**
 * This header provides inforamtion about the type of message that is being sent. It is included
 * in the JGroups message to allow appropriate routing and handling.
 */
public class TypeHeader extends Header
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	// Header ID: 2001
	// T for "Tim" (20), Header One (01)
	public static final short ID                     = 2001;
	public static final short DATA_MESSAGE           = 1; // message is a Portico Data message
	public static final short CONTROL_REQ_SYNC       = 2; // message is a Portico Control message (request)
	public static final short CONTROL_REQ_ASYNC      = 3; // message is a Portico Control request, sent async
	public static final short CONTROL_RESP           = 4; // message is a Portico Control message (response)

	public static final short RTI_PROBE              = 10; // see if there is an RTI running

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private short messageType;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TypeHeader()
	{
		this.messageType = 0;
	}

	private TypeHeader( short type )
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
		return ID;
	}

	@Override
	public Supplier<? extends Header> create()
	{
		return TypeHeader::new;
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
			case DATA_MESSAGE:
				return "DataMessage";
			case CONTROL_REQ_SYNC:
				return "ControlSync";
			case CONTROL_REQ_ASYNC:
				return "ControlAsync";
			case CONTROL_RESP:
				return "ControlResp";
			case RTI_PROBE:
				return "RtiProbe";
			default:
				return "Unknown";
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static TypeHeader rtiProbe()
	{
		return new TypeHeader( RTI_PROBE );
	}
	
	public static TypeHeader controlRequest()
	{
		return new TypeHeader( CONTROL_REQ_SYNC );
	}

	public static TypeHeader controlRequestAsync()
	{
		return new TypeHeader( CONTROL_REQ_ASYNC );
	}
	
	public static TypeHeader controlResponse()
	{
		return new TypeHeader( CONTROL_RESP );
	}

	public static TypeHeader dataMessage()
	{
		return new TypeHeader( DATA_MESSAGE );
	}
}
