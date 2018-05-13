/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.common.messaging;

/**
 * Messages in Portico are broadly broken down into two type: Data and Control.
 * <p/>
 * 
 * A control message is something that is exchanged exclusively between a single federate and
 * the RTI (in either direction). These always use a reliable transport and can be considered
 * point-to-point in semantics. How that translates onto the network depends on the specifics
 * off the connection implementation.
 * <p/>
 * 
 * A data message is identified as a "high-volume" data carrying message. It is a message that
 * semantically is more like a broadcast. Attribte updates and reflections are the most common
 * (currently only) data messages. This type exists so that connections can efficiently route
 * and filter messages that should be delivered in high-volume, as these are the ones that will
 * affect performance the most.
 * <p/>
 * 
 * This enumeration identifies the type of message that is being sent:
 * <ul>
 *   <li><b>DataMessage</b>: A message that should use the fastest available transport. Broadcast.</li>
 *   <li><b>ControlSync</b>: A control request message that requires a response (typically fed->rti)</li>
 *   <li><b>ControlAsync</b>: A control request that does not require a response (typically rti->fed)</li>
 *   <li><b>ControlResp</b>: A response to a control message</li>
 *   <li><b>RtiProbe</b>: Someone looking for an RTI. A Control Response should be sent in return</li>
 *   <li><b>Bundle</b>: A bunch of messages bundled together in bulk</li>
 * </ul>
 * 
 */
public enum CallType
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	DataMessage (0),
	ControlSync (1),
	ControlAsync(2),
	ControlResp (3),
	RtiProbe    (4),
	Bundle      (5);
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int id; // Marshalling limit is 8!

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private CallType( int id )
	{
		this.id = id;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public final int getId()
	{
		return id;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static CallType fromId( int id )
	{
		switch( id )
		{
			case 0: return DataMessage;
			case 1: return ControlSync;
			case 2: return ControlAsync;
			case 3: return ControlResp;
			case 4: return RtiProbe;
			case 5: return Bundle;
			default: throw new IllegalArgumentException( "CallType id not known: "+id );
		}
	}
}
