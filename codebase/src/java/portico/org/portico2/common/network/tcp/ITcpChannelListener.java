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
package org.portico2.common.network.tcp;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;

public interface ITcpChannelListener
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
	 * When one is ready the listener is expected to call {@link TcpChannel#sendControlResponse(int, byte[])}
	 * providing the given request ID.
	 * 
	 * @param channel     The channel the request was received on
	 * @param requestId   The ID of the request we should use for any response
	 * @param payload     The raw byte payload that was received
	 * @throws JException If there is a problem processing the request
	 */
	public void receiveControlRequest( TcpChannel channel, int requestId, byte[] payload ) throws JException;

	/**
	 * A data message has been received on the given channel for processing.
	 * 
	 * @param channel The channel it was received on
	 * @param payload The raw byte payload that was received
	 * @throws JRTIinternalError Throw this if there is an error and the channel will log it
	 */
	public void receiveDataMessage( TcpChannel channel, byte[] payload ) throws JRTIinternalError;

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
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
