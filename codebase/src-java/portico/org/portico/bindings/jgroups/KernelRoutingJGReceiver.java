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
package org.portico.bindings.jgroups;

import org.apache.log4j.Logger;
import org.jgroups.Message;
import org.portico.lrc.LRC;
import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.messaging.ErrorResponse;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.PorticoMessage;

/**
 * Default implementation of {@link JGReceiver} that passes all incoming messages into the
 * message queue of the associated {@link LRC}.
 */
public class KernelRoutingJGReceiver implements JGReceiver
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRC lrc;
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public KernelRoutingJGReceiver( LRC lrc )
	{
		this.lrc = lrc;
		this.logger = lrc.getLrcLogger();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public void setWrapper( ChannelWrapper wrapper )
	{
	}

	public void receiveAsynchronous( Message message )
	{
		try
		{
    		// fetch the payload from the message
    		PorticoMessage payload = MessageHelpers.inflate( message.getBuffer(),
    		                                                 PorticoMessage.class,
    		                                                 lrc );
    		
    		// if we get null back, it means we should stop processing now
    		if( payload == null )
   				return;
    		
    		lrc.getState().getQueue().offer( payload );
		}
		catch( Exception e )
		{
			logger.error( "Error processing receive message: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method will drop the message directly into the incoming message sink rather than into
	 * the message queue. It packages the contained {@link PorticoMessage} up in
	 * a {@link MessageContext} and will return the response message from that context.
	 */
	public Object receiveSynchronous( Message message )
	{
		try
		{
			// fetch the payload from the message
			PorticoMessage payload = MessageHelpers.inflate( message.getBuffer(),
			                                                 PorticoMessage.class );
			
			MessageContext context = new org.portico.utils.messaging.MessageContext( payload );
			lrc.getIncomingSink().process( context );
			return context.getResponse();
		}
		catch( Exception e )
		{
			logger.error( "Error processing receive message: " + e.getMessage(), e );
			return new ErrorResponse( e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
