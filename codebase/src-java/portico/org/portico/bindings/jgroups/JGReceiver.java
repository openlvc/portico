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
package org.portico.bindings.jgroups;

import org.jgroups.Message;

/**
 * This interface must be implemented by any class that wants to be plugged into a
 * {@link ChannelWrapper} to receive messages.
 */
public interface JGReceiver
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public void setWrapper( ChannelWrapper wrapper );

	/**
	 * Process the provided async-message, no response is required.
	 */
	public void receiveAsynchronous( Message message );

	/**
	 * The following message has been sent and has requested responses, process it and
	 * return a response.
	 */
	public Object receiveSynchronous( Message message );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
