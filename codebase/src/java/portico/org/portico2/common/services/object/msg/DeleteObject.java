/*
 *   Copyright 2006 The Portico Project
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
package org.portico2.common.services.object.msg;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message is a notification to the RTI that the object of the contained handle should be
 * deleted. If the sending federate is regulating, this message can be sent with a time at which
 * the object should be deleted. If the federate is not regulating, but invokes the delete method
 * with a time, the LRC should remove this value, thus sending the request without a time.
 */
public class DeleteObject extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int objectHandle;
	private byte[] tag;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/** <b>DO NOT USE</b> This is only provided because the deserialization of Externalizable
        objects requires that the class have a 0-arg constructor */
	public DeleteObject()
	{
		super();
	}
	
	public DeleteObject( int objectHandle, byte[] tag )
	{
		this();
		this.objectHandle = objectHandle;
		this.tag = tag;
	}
	
	public DeleteObject( int objectHandle, byte[] tag, double time )
	{
		this( objectHandle, tag );
		this.timestamp = time;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.DeleteObject;
	}
	
	public int getObjectHandle()
	{
		return this.objectHandle;
	}
	
	public void setObjectHandle( int objectHandle )
	{
		this.objectHandle = objectHandle;
	}
	
	public byte[] getTag()
	{
		return this.tag;
	}
	
	public void setTag( byte[] tag )
	{
		this.tag = tag;
	}
	
	/**
	 * Returns <code>true</code>
	 */
	public boolean isSpecDefinedMessage()
	{
		return true;
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.objectHandle = input.readInt();
		this.tag = input.readUTF().getBytes();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		
		output.writeInt( this.objectHandle );
		output.writeUTF( bytesToString(this.tag) ); // conver it for a string for simple read/write
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
