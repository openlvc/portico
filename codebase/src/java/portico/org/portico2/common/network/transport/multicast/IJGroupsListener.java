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
package org.portico2.common.network.transport.multicast;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JRTIinternalError;

/**
 * This interface is used to link a JGroups channel to the component that is managing it. When
 * messages are received on the channel, they are passed to an implementation of this interface
 * for processing.
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
	 * A message has been received on the given channel for processing.
	 * 
	 * @param channel The channel it was received on
	 * @param payload The raw payload that was received
	 * @throws JRTIinternalError Throw this if there is an error and the channel will log it
	 */
	public void receive( JGroupsChannel channel, byte[] payload ) throws JRTIinternalError;
	
	/**
	 * @return Each listener must provide a logger to the channel.
	 */
	public Logger provideLogger();
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
