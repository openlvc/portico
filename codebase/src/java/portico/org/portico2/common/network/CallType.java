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
package org.portico2.common.network;

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
 * A notification is effectively an asynchronous control message (no response required). It will
 * typically be addressed to a subset of the federation, most often only one federate. It is also
 * typically an RTI->Federate message, representing things like discovery notifications and so on. 
 * 
 * This enumeration identifies the type of message that is being sent:
 * <ul>
 *   <li><b>DataMessage</b>: A message that should be broadcast to all participants.</li>
 *   <li><b>Notification</b>: A one-way message that does not require a response (typically rti->fed)</li>
 *   <li><b>ControlRequest</b>: A control request message that requires a response (typically fed->rti)</li>
 *   <li><b>ControlResponseOK</b>: A success response to a control request.</li>
 *   <li><b>ControlResponseErr</b>:An error response to a control request.</li>
 * </ul>
 * 
 */
public enum CallType
{
	//----------------------------------------------------------
	//                        VALUES
	//----------------------------------------------------------
	DataMessage (0),
	Notification(1),
	ControlRequest(2),
	ControlResponseOK(3),
	ControlResponseErr(4);
	
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
			case 1: return Notification;
			case 2: return ControlRequest;
			case 3: return ControlResponseOK;
			case 4: return ControlResponseErr;
			default: throw new IllegalArgumentException( "CallType id not known: "+id );
		}
	}
}
