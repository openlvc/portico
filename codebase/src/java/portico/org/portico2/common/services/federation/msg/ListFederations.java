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
package org.portico2.common.services.federation.msg;

import org.portico.bindings.IConnection;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message is used to solicit the currently registered federations available. It is generally
 * only used internally by {@link IConnection} implementations, but it can be used explicity by
 * federates if they wish. It is also used by some connection implementations on startup to sense
 * for the presence (or lack of) an RTI.
 * <p/>
 * This message is used as both the request and the response. If the message is a request, then
 * <code>null</code> is passed for the {@link #getFederations() federations} array. If it is a
 * response, then the list of federtations is passed (if there are none, an empty list is passed).
 */
public class ListFederations extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String[] federations;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ListFederations()
	{
		super();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.ListFederations;
	}

	public void setFederations( String[] federations )
	{
		this.federations = federations;
	}
	
	public String[] getFederations()
	{
		return this.federations;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
