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
import java.util.Random;
import java.util.function.Supplier;

import org.jgroups.Header;
import org.jgroups.Message;
import org.portico.lrc.compat.JRTIinternalError;

/**
 * Messages sent via Portico come in two forms: broadcast and request/response. The latter require
 * an ID be used for a message to correlate request and response. This header represents that id.
 */
public class MessageIdHeader extends Header
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Random RANDOM = new Random();

	// T(im) = 20, Message 02
	public static final short ID = 2002;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int id;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/** Generates the header with a new Random int */
	public MessageIdHeader()
	{
		this.id = MessageIdHeader.RANDOM.nextInt();
	}
	
	/**
	 * Generates the header with the specified id
	 * @param id The message id to use
	 */
	public MessageIdHeader( int id )
	{
		this.id = id;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public int getId()
	{
		return this.id;
	}

	@Override
	public int serializedSize()
	{
		return 4;
	}

	@Override
	public short getMagicId()
	{
		return ID;
	}
	
	@Override
	public Supplier<? extends Header> create()
	{
		return MessageIdHeader::new;
	}

	public void writeTo( DataOutput out ) throws IOException
	{
		out.writeInt( id );
	}

	public void readFrom( DataInput in ) throws IOException,
												IllegalAccessException,
												InstantiationException
	{
		this.id = in.readInt();
	}

	@Override
	public String toString()
	{
		return "ID="+this.id;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Checks the given message to see if it contains a {@link MessageIdHeader}, and if it does it 
	 * will return the contained ID. If it does not, an exception is thrown.
	 * 
	 * @param message The message to check for the header on
	 * @return The ID from the MessageIDHeader on the message (int)
	 * @throws JRTIinternalError If there is no MessageIDHeader on the message
	 */
	public static int getMessageId( Message message ) throws JRTIinternalError
	{
		MessageIdHeader header = message.getHeader( ID );
		if( header == null )
			throw new JRTIinternalError( "Expected message to have a MessageIDHeader but it did not" );
		else
			return header.getId();
	}
}
