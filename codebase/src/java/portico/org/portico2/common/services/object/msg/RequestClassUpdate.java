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
package org.portico2.common.services.object.msg;

import java.util.Set;
import org.portico.lrc.PorticoConstants;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * Request the update of all instances for a given class (or its subclasses) by the federates that
 * own the instances. The attributes desired for the update are provided in the set of handles.
 */
public class RequestClassUpdate extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private Set<Integer> attributeHandles;
	private byte[] tag;
	private int regionToken;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.RequestClassUpdate;
	}

	public RequestClassUpdate()
	{
		super();
		this.regionToken = PorticoConstants.NULL_HANDLE;
	}

	public RequestClassUpdate( int classHandle, Set<Integer> attributes, byte[] tag )
	{
		this();
		this.classHandle = classHandle;
		this.attributeHandles = attributes;
		this.tag = tag;
	}
	
	public RequestClassUpdate( int classHandle,
	                           Set<Integer> attributes,
	                           byte[] tag,
	                           int regionToken )
	{
		this( classHandle, attributes, tag );
		this.regionToken = regionToken;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public Set<Integer> getAttributes()
    {
    	return attributeHandles;
    }

	public void setAttributes( Set<Integer> attributes )
    {
    	this.attributeHandles = attributes;
    }

	public int getClassHandle()
    {
    	return classHandle;
    }

	public void setClassHandle( int classHandle )
    {
    	this.classHandle = classHandle;
    }

	public byte[] getTag()
    {
    	return tag;
    }

	public void setTag( byte[] tag )
    {
    	this.tag = tag;
    }
	
	public int getRegionToken()
	{
		return this.regionToken;
	}
	
	public boolean usesDDM()
	{
		return this.regionToken != PorticoConstants.NULL_HANDLE;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
