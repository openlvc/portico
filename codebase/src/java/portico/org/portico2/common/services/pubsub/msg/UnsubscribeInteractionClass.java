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

import org.portico.lrc.PorticoConstants;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class UnsubscribeInteractionClass extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private int regionToken;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	@Override 
	public MessageType getType()
	{
		return MessageType.UnsubscribeInteraction;
	}

	public UnsubscribeInteractionClass( int classHandle )
	{
		this.classHandle = classHandle;
		this.regionToken = PorticoConstants.NULL_HANDLE;
	}

	public UnsubscribeInteractionClass( int classHandle, int regionToken )
	{
		this( classHandle );
		this.regionToken = regionToken;
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

	public int getRegionToken()
	{
		return this.regionToken;
	}
	
	public void setRegionToken( int regionToken )
	{
		this.regionToken = regionToken;
	}
	
	public boolean usesDdm()
	{
		return this.regionToken != PorticoConstants.NULL_HANDLE;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
