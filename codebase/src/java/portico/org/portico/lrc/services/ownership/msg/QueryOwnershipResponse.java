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

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class QueryOwnershipResponse extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	public static final int UNOWNED = -1;
	public static final int OWNED_BY_RTI = 0;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int objectHandle;
	private int attributeHandle;
	private int owner;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public QueryOwnershipResponse( int objectHandle, int attributeHandle, int owner )
	{
		this.objectHandle = objectHandle;
		this.attributeHandle = attributeHandle;
		this.owner = owner;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.QueryOwnershipResponse;
	}

	public int getObjectHandle()
	{
		return this.objectHandle;
	}
	
	public int getAttributeHandle()
	{
		return this.attributeHandle;
	}
	
	public boolean isUnowned()
	{
		return owner == UNOWNED;
	}
	
	public boolean isOwnedByRti()
	{
		return owner == OWNED_BY_RTI;
	}
	
	public int getOwner()
	{
		return this.owner;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
