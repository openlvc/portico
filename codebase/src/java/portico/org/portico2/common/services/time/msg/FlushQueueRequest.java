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
package org.portico2.common.services.time.msg;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class FlushQueueRequest extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private double time;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public FlushQueueRequest( double time )
	{
		setTime( time );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.FlushQueueRequest;
	}

	public double getTime()
	{
		return this.time;
	}
	
	/**
	 * Note that this will set both the time and timestamp values. If you just
	 * want to change the timestamp, call {@link #setTimestamp(double)}.
	 */
	public void setTime( double time )
	{
		this.time = time;
		this.timestamp = time;
	}

	/**
	 * Returns <code>true</code>. We want this message to be held in the queue as if it were a time
	 * advance grant. This way messages can be flushed up to this point.
	 */
	@Override
	public boolean isTimeAdvance()
	{
		return true;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
