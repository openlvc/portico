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
 * Message representing an attribute divestiture request. The request represents two particular
 * messages that come from the RTI interface, either an unconditional request, or a negotiated
 * request.
 */
public class AttributeDivest extends PorticoMessage
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
	private boolean unconditional;
	private byte[] tag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new message, specifying if it is unconditional or negotiated through the
	 * provided parameter.
	 */
	public AttributeDivest( int objectHandle, Set<Integer> attributes, boolean unconditional )
	{
		super();
		this.objectHandle = objectHandle;
		this.attributes = attributes;
		this.unconditional = unconditional;
	}

	/**
	 * Create a new message that represents a *negotiated* attribute divestiture.
	 */
	public AttributeDivest( int objectHandle, Set<Integer> attributes, byte[] tag )
	{
		this( objectHandle, attributes, false );
		this.tag = tag;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.AttributeDivest;
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

	public boolean isUnconditional()
	{
		return unconditional;
	}
	
	public boolean isNegotiated()
	{
		return !unconditional;
	}

	public void setUnconditional( boolean unconditional )
	{
		this.unconditional = unconditional;
	}
	
	public byte[] getTag()
	{
		return this.tag;
	}
	
	public void setTag( byte[] tag )
	{
		this.tag = tag;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
