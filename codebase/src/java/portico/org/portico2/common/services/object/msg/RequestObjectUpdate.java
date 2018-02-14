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
package org.portico2.common.services.object.msg;

import java.util.Set;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * Request the update of the attributes for a given object instance by the federates that own them.
 */
public class RequestObjectUpdate extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int objectId;
	private Set<Integer> attributes;
	private byte[] tag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.RequestObjectUpdate;
	}

	public RequestObjectUpdate()
	{
		super();
	}

	public RequestObjectUpdate( int objectId, Set<Integer> attributes )
	{
		this();
		this.objectId = objectId;
		this.attributes = attributes;
		this.tag = "".getBytes();
	}
	
	public RequestObjectUpdate( int objectId, Set<Integer> attributes, byte[] tag )
	{
		this( objectId, attributes );
		this.tag = tag;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public Set<Integer> getAttributes()
    {
    	return attributes;
    }

	public void setAttributes( Set<Integer> attributes )
    {
    	this.attributes = attributes;
    }

	public int getObjectId()
    {
    	return objectId;
    }

	public void setObjectId( int objectId )
    {
    	this.objectId = objectId;
    }

	public byte[] getTag()
    {
    	return tag;
    }

	public void setTag( byte[] tag )
    {
    	this.tag = tag;
    }
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
