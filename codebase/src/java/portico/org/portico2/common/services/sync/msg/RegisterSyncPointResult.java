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
package org.portico2.common.services.sync.msg;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This represents a callback message to inform a federate whether its synchronization point
 * registration was a success or a failure. It is generally queued up by the federates themselves
 * when they are able to figure out if they can register a point or not. Each message contains a
 * <code>failureReason</code> variable, if this variable is <code>null</code>, then the registration
 * request was a success. If it is not null, then the registration was a failure and the string
 * holds information about the reason for this.
 */
public class RegisterSyncPointResult extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String label;
	private String failureReason;
	private byte[] tag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public RegisterSyncPointResult( boolean status, String label )
	{
		this.label = label;
		if( status == false )
			this.failureReason = "unknown failure";
	}
	
	/**
	 * New "success" result for the identified sync point (passing the tag used to register the
	 * sync point so it can be delievered during an announcement for the local federate, which
	 * ignores the announcement method it sends out itself).
	 */
	public RegisterSyncPointResult( String label, byte[] tag )
	{
		this.label = label;
		this.tag = tag;
	}

	/**
	 * New "failure" result for the identified sync point with the given label and failure reason
	 */
	public RegisterSyncPointResult( String label, String failureReason )
	{
		this.label = label;
		this.failureReason = failureReason;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.RegisterSyncPointResult;
	}

	public String getLabel()
	{
		return this.label;
	}
	
	public void setLabel( String label )
	{
		this.label = label;
	}
	
	public byte[] getTag()
	{
		return this.tag;
	}
	
	public void setTag( byte[] tag )
	{
		this.tag = tag;
	}

	public String getFailureReason()
	{
		return this.failureReason;
	}
	
	public void setFailureReason( String failureReason )
	{
		this.failureReason = failureReason;
	}
	
	public boolean wasSuccess()
	{
		return this.failureReason == null;
	}
	
	public boolean wasFailure()
	{
		return this.failureReason != null;
	}

	public String toString()
	{
		if( failureReason == null )
			return "Registration SUCCESS: label=" + label;
		else
			return "Registration FAILURE: label=" + label + ", reason=" + failureReason;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
