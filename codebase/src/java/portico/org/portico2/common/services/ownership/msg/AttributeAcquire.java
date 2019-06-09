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
package org.portico2.common.services.ownership.msg;

import java.util.Set;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * Message representing an attribute aquisition request. The request represents two particular
 * messages that come from the RTI interface, where the reqeust can be only if the attributes
 * are available (unowned) or where they are owned by another federate and the user wants to
 * kick off a pull/negotiated aquisition.
 * <p/>
 * If this is an "isAvailable" request, then then <code>isImmediateProcessingRequired()</code>
 * flag is set to true.
 */
public class AttributeAcquire extends PorticoMessage
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
	private boolean ifAvailable;
	private byte[] tag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * If <code>ifAvailable</code> is true, this represents a request to only to aquire the
	 * attributes if they are available.
	 */
	public AttributeAcquire( int objectHandle, Set<Integer> attributes, boolean ifAvailable )
	{
		super();
		this.objectHandle = objectHandle;
		this.attributes = attributes;
		this.ifAvailable = ifAvailable;
		this.setImmediateProcessingFlag( ifAvailable );
	}

	/**
	 * Create a new message that represents a request to aquire the attributes whether they are
	 * available or not (a pull/negotiated aquisition).
	 */
	public AttributeAcquire( int objectHandle, Set<Integer> attributes, byte[] tag )
	{
		this( objectHandle, attributes, false );
		this.setTag( tag );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.AttributeAcquire;
	}

	/**
	 * If the ifAvailable flag is set, then immediate processing is required
	 */
	@Override
	public boolean isImmediateProcessingRequired()
	{
		return ifAvailable;
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

	public boolean isIfAvailable()
	{
		return ifAvailable;
	}
	
	public void setIfAvailable( boolean ifAvailable )
	{
		this.ifAvailable = ifAvailable;
		this.setImmediateProcessingFlag( ifAvailable );
	}
	
	public byte[] getTag()
	{
		return this.tag;
	}
	
	public void setTag( byte[] tag )
	{
		if( tag == null )
			tag = new byte[0];
		this.tag = tag;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
