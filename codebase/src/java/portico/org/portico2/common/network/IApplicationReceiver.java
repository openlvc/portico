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

import org.apache.logging.log4j.Logger;
import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageContext;

/**
 * The component that will receive data and control messages from an {@link IConnection}.
 */
public interface IApplicationReceiver
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * The application ultimately represents the context that a connection sits within.
	 * As such, we often want the connection framework to use the same logging infrastructure.
	 * This method lets the application provide a logger to a connection.
	 * 
	 * @return The logger that the connection framework should use
	 */
	public Logger getLogger();

	///////////////////////////////////////////////////////////////////////////////////////
	///  Message RECEIVING methods   //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * For some connection types all messages are exchanged in a broadcast manner.
	 * In this situation, an RTI/LRC can receive messages that are not meant for it.
	 * We want to filter these out as early as possible. To do this, we can leave each
	 * receiver pre-approve messages so that we can knock them out early.
	 * the connection what our ID is and it can use that to assist.
	 * <p/>
	 * 
	 * Each {@link PorticoMessage} has a target that we can filter on. Common values
	 * for this are:
	 * <p/>
	 * 
	 * <ul><li>{@link PorticoConstants#RTI_HANDLE}: For the RTI </li>
	 *     <li>{@link PorticoConstants#TARGET_ALL_HANDLE}: Message is for everyone </li>
	 *     <li>{@link PorticoConstants#TARGET_MANY_HANDLE}: Message is for some but not all </li>
	 * </ul>
	 * 
	 * Note that this filtering only applies to CONTROL messages.
	 * 
	 * @param header The message header.
	 * @returns True if the message should be processed. False otherwise.
	 */
	public boolean isReceivable( Header header );

	/**
	 * A control message has been received from the other end of the connection.
	 * A response is required to this message, so please populate the provided
	 * message context and then return.
	 * 
	 * @param context The message context object containing the request
	 * @throws JRTIinternalError If there is a problem of any kind
	 */
	public void receiveControlRequest( MessageContext context ) throws JRTIinternalError;

	/**
	 * A notification is an async control request. Unlike a data message, it is not broadcast
	 * to everyone, nor should it ever come over a path that trades reliability for speed.
	 * 
	 * This type of message is typically used for important notification calls sent to a 
	 * federate (or federates) from the RTI such as object discovery callbacks, sync point
	 * notifications, time advance grants and so on.
	 * 
	 * @param message The notification message that was received.
	 * @throws JRTIinternalError If there is an error processing the message.
	 */
	public void receiveNotification( PorticoMessage message ) throws JRTIinternalError;
	
	/**
	 * A broadcast/data message has been received from the connection and should
	 * be processed.
	 * 
	 * Broadcast messages are intended to be sent to all federates within a federation.
	 * Although they may be routed through the RTI, they are not a control message. 
	 * 
	 * Their use is currently limited to attribute reflections and interactions. Although
	 * these messages are only a small subset of all those available, in any given federation
	 * they will represent the _vast_ majority of the volume of messages exchanged. 
	 * 
	 * @param message The message to send to all other federates
	 * @throws JException If there is a problem sending the message
	 */
	public void receiveDataMessage( PorticoMessage message ) throws JException;
	
}
