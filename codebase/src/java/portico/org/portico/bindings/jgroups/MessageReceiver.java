/*
 *   Copyright 2012 The Portico Project
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
 * This class is designed route incoming messages to the approrpiate location. If the
 * connection represents a joined federate, this class will have a reference to the
 * LRC and will route all incoming messages there. If this associated connection has
 * not joined the federation yet, all messages will be dropped.
 */
public class MessageReceiver
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRC lrc;
	private Logger logger;
	private Auditor auditor;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public MessageReceiver( Auditor auditor )
	{
		this.lrc = null;
		this.logger = Logger.getLogger( "portico.lrc.jgroups" );
		this.auditor = auditor;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public synchronized void linkToLRC( LRC lrc )
	{
		this.lrc = lrc;
	}
	
	public synchronized void unlink()
	{
		this.lrc = null;
	}

	public synchronized void receiveAsynchronous( Message message )
	{
		if( this.lrc == null )
			return; // ignore
		
		try
		{
    		// fetch the payload from the message
    		PorticoMessage payload = MessageHelpers.inflate( message.getBuffer(),
    		                                                 PorticoMessage.class,
    		                                                 lrc );
    		
    		// if we get null back, it means we should stop processing now
    		if( payload == null )
   				return;
    		
    		// log an audit entry for the reception
    		if( auditor.isRecording() )
    			auditor.received( payload, message.getLength() );
    		
    		// shove into our queue for later processing
    		lrc.getState().getQueue().offer( payload );
		}
		catch( Exception e )
		{
			logger.error( "Error processing received message: " + e.getMessage(), e );
		}
	}
	
	/**
	 * This method will drop the message directly into the incoming message sink rather than into
	 * the message queue. It packages the contained {@link PorticoMessage} up in
	 * a {@link MessageContext} and will return the response message from that context.
	 */
	public synchronized Object receiveSynchronous( Message message )
	{
		if( this.lrc == null )
			return null; // ignore

		try
		{
			// fetch the payload from the message
			PorticoMessage payload = MessageHelpers.inflate( message.getBuffer(),
			                                                 PorticoMessage.class );
			
    		// log an audit entry for the reception
    		if( auditor.isRecording() )
    			auditor.received( payload, message.getLength() );

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
