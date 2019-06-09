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
 * This class represents an ownership acquisition or divestiture cancellation confirmation.
 */
public class CancelConfirmation extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int objectHandle;
	private Set<Integer> attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public CancelConfirmation( int objectHandle, Set<Integer> attributes )
	{
		super();
		this.objectHandle = objectHandle;
		this.attributes = attributes;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.CancelConfirmation;
	}

	public int getObjectHandle()
	{
		return objectHandle;
	}

	public void setObjectHandle( int objectHandle )
	{
		this.objectHandle = objectHandle;
	}

	public Set<Integer> getAttributes()
	{
		return attributes;
	}

	public void setAttributes( Set<Integer> attributes )
	{
		this.attributes = attributes;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
