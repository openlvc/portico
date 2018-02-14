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
package org.portico.lrc.services.saverestore.msg;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message is what is delivered to federates once the LRC has completed its local state
 * restoration. Instances of these messages should not find their way out into the network,
 * rather, they should be processed within an LRC when ready.
 */
public class RestoreInitiate extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String label;
	private int federateHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public RestoreInitiate( String label, int federateHandle )
	{
		this.label = label;
		this.federateHandle = federateHandle;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@Override
	public MessageType getType()
	{
		return MessageType.RestoreInitiate;
	}

	public String getLabel()
	{
		return this.label;
	}
	
	public void setLabel( String label )
	{
		this.label = label;
	}
	
	public int getFederateHandle()
	{
		return this.federateHandle;
	}
	
	public void setFederateHandle( int federateHandle )
	{
		this.federateHandle = federateHandle;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
