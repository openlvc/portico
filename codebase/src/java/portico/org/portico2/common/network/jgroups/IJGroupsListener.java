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
package org.portico2.common.network.jgroups;

import org.apache.logging.log4j.Logger;
import org.jgroups.Message;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;

/**
 * This interface is used to link a JGroups channel to the component that is managing it. When
 * messages are received on the channel, they are passed to the varoius callback methods provided
 * by this interface. 
 */
public interface IJGroupsListener
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
	/**
	 * A Control message was received on the given channel. Control messages expect a response.
	 * When one is ready the listener is expected to call {@link JGroupsChannel#sendControlResponse(int, byte[])},
	 * providing the given request ID.
	 * 
	 * @param channel     The channel the request was received on
	 * @param requestId   The ID of the request we should use for any response
	 * @param message     The raw message that was received
	 * @throws JException If there is a problem processing the request
	 */
	public void receiveControlRequest( JGroupsChannel channel, int requestId, Message message ) throws JException;

	/**
	 * A data message has been received on the given channel for processing.
	 * 
	 * @param channel The channel it was received on
	 * @param message The raw JGroups message that was received
	 * @throws JRTIinternalError Throw this if there is an error and the channel will log it
	 */
	public void receiveDataMessage( JGroupsChannel channel, Message message ) throws JRTIinternalError;

	/**
	 * @return Each listener must provide a logger to the channel.
	 */
	public Logger provideLogger();
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
