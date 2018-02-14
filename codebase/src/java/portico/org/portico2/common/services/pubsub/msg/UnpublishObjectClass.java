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
package org.portico2.common.services.pubsub.msg;

import java.util.HashSet;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class UnpublishObjectClass extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private HashSet<Integer> attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	@Override 
	public MessageType getType()
	{
		return MessageType.UnpublishObjectClass;
	}

	public UnpublishObjectClass( int classHandle )
	{
		this.classHandle = classHandle;
	}
	
	public UnpublishObjectClass( int classHandle, HashSet<Integer> attributes )
	{
		this( classHandle );
		this.attributes = attributes;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public int getClassHandle()
	{
		return this.classHandle;
	}
	
	public void setClassHandle( int classHandle )
	{
		this.classHandle = classHandle;
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
