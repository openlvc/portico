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

import org.portico.lrc.utils.MessageHelpers;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.ResponseMessage;

public class Message
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private CallType calltype;
	private int requestId;
	
	// serialized version of the message
	private byte[] buffer;
	private Header header;
	
	// cached version of inflated messages
	private PorticoMessage request;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Message( PorticoMessage request, CallType calltype, int requestId )
	{
		this.request = request;
		this.calltype = calltype;
		this.requestId = requestId;
		
		this.buffer = MessageHelpers.deflate2( request, calltype, requestId );
		this.header = new Header( buffer, 0 );
	}
	
	public Message( byte[] buffer )
	{
		this.buffer = buffer;
		this.header = new Header( buffer, 0 );
		this.requestId = header.getRequestId();
		this.calltype = this.header.getCallType();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	private final void deflateRequest()
	{
		this.buffer = MessageHelpers.deflate2( request, calltype, requestId );
		this.header = new Header( buffer, 0 );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public final int getRequestId() { return this.requestId; }
	public final Header getHeader() { return this.header; }
	public final byte[] getBuffer() { return this.buffer; }
	public final CallType getCallType() { return this.calltype; }
	public final boolean hasRequest() { return this.request != null; }
	public final PorticoMessage getRequest() { return this.request; }

	/**
	 * Tells the message to take the contents of the buffer and inflate it into a full
	 * {@link PorticoMessage} object, returning the result. The request object is lazy
	 * loaded, so if you call {@link #getRequest()} before this method has been called,
	 * the message class will implicitly call it on your behalf.
	 * 
	 * @return The {@link PorticoMessage} that represents the contained byte[] buffer (if present).
	 */
	public final PorticoMessage inflateAsPorticoMessage()
	{
		this.request = MessageHelpers.inflate2( buffer, PorticoMessage.class ); 
		return request;
	}
	
	public final <T extends PorticoMessage> T inflateAsPorticoMessage( Class<T> clazz )
	{
		return clazz.cast( inflateAsPorticoMessage() );
	}
	
	public final ResponseMessage inflateAsResponse()
	{
		return MessageHelpers.inflate2( buffer, ResponseMessage.class );
	}
	
	public final void deflateAndStoreResponse( ResponseMessage response )
	{
		if( this.request == null )
			throw new IllegalArgumentException( "You cannot deflate a ResponseMessage without a request" );
		
		this.buffer = MessageHelpers.deflate2( response, this.requestId, this.request );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
