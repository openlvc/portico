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
package org.portico.lrc.services.sync.msg;

import java.util.HashSet;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message represents a request to secure a synchronization point label for registeration
 * by the sending federate. This signals the intent of the federate to the other federates and
 * allows them to respond, potentially informing the federate that a point by that name already
 * exists. This is NOT the actual announcement. The announcement comes once the federate is
 * satisfied that it has secured the right to the label (and the announcement includes other
 * information that is relevant to the callback, such as the tag and federate handle set associated
 * with the point).
 */
public class SyncRegistrationRequest extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String label;
	private HashSet<Integer> federates;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public SyncRegistrationRequest( String label )
	{
		this.label = label;
	}
	
	public SyncRegistrationRequest( String label, HashSet<Integer> federates )
	{
		this( label );
		this.federates = federates;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.RegisterSyncPoint;
	}

	public String getLabel()
	{
		return this.label;
	}
	
	public void setLabel( String label )
	{
		this.label = label;
	}
	
	public HashSet<Integer> getFederates()
	{
		return this.federates;
	}
	
	public void setFederates( HashSet<Integer> federates )
	{
		this.federates = federates;
	}
	
	@Override
	public boolean isImmediateProcessingRequired()
	{
		return true;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
