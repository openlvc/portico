/*
 *   Copyright 2008 The Portico Project
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
package org.portico.lrc.services.ddm.msg;

import java.util.HashSet;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class AssociateRegion extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int regionToken;
	private int objectHandle;
	private HashSet<Integer> attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public AssociateRegion()
	{
		super();
	}
	
	public AssociateRegion( int regionToken, int objectHandle, HashSet<Integer> attributes )
	{
		this();
		this.regionToken = regionToken;
		this.objectHandle = objectHandle;
		this.attributes = attributes;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.AssociateRegion;
	}

	public int getRegionToken()
    {
    	return this.regionToken;
    }

	public void setRegionToken( int regionToken )
    {
		this.regionToken = regionToken;
    }
	
	public int getObjectHandle()
	{
		return this.objectHandle;
	}
	
	public void setObjectHandle( int objectHandle )
	{
		this.objectHandle = objectHandle;
	}

	public HashSet<Integer> getAttributes()
	{
		return this.attributes;
	}
	
	public void setAttributes( HashSet<Integer> attributes )
	{
		this.attributes = attributes;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
