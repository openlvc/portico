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
package org.portico2.common.services.time.msg;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * Message that represents a time advance grant. Note that this message has two separate time
 * values: a "timestamp" that all messages have, and a "time" that represents the time the
 * advance was granted to. Previously we just used the timestamp for both purposes, however,
 * in the case of unconstrained federates, portico currently strips the timestamp off messages
 * before placing them in the RO queue (this is done in the MessageQueue class). Using the one
 * value caused this to erase the time the grant was given to, so now we have two values.
 */
public class TimeAdvanceGrant extends PorticoMessage
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

	public TimeAdvanceGrant( double time )
	{
		setTime( time );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.TimeAdvanceGrant;
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
	
	@Override
	public boolean isTimeAdvance()
	{
		return true;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
