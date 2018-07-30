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
package org.portico2.common.network.transport.tcp.channel;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JRTIinternalError;

public interface ITcpChannelListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
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
	public void receive( TcpChannel channel, byte[] payload ) throws JRTIinternalError;

	/**
	 * This method is called when the channel has disconnected for any reason
	 * 
	 * @param throwable Exception causing the disconnection (may be null)
	 */
	public void disconnected( Throwable throwable );
	
	/**
	 * @return Each listener must provide a logger to the channel.
	 */
	public Logger provideLogger();

}
