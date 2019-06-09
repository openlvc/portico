/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.ownership.msg;

import java.util.Set;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message signals that the specified attributes of the specified object have been aquired.
 * The target federate of the message should point to the federate that has aquired them.
 */
public class OwnershipAcquired extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int objectHandle;
	private Set<Integer> attributeHandles;
	private boolean ifAvailable;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public OwnershipAcquired( int objectHandle, Set<Integer> attributes, boolean ifAvailable )
	{
		super();
		this.objectHandle = objectHandle;
		this.attributeHandles = attributes;
		this.ifAvailable = ifAvailable;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.OwnershipAcquired;
	}

	public int getObjectHandle()
	{
		return objectHandle;
	}

	public void setObjectHandle( int objectHandle )
	{
		this.objectHandle = objectHandle;
	}

	public Set<Integer> getAttributeHandles()
	{
		return attributeHandles;
	}

	public void setAttributeHandles( Set<Integer> attributeHandles )
	{
		this.attributeHandles = attributeHandles;
	}
	
	public boolean isIfAvailable()
	{
		return this.ifAvailable;
	}
	
	public void setIfAvailable( boolean ifAvailable )
	{
		this.ifAvailable = ifAvailable;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
