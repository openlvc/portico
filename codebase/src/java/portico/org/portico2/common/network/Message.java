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
import org.portico2.common.messaging.MessageType;
import org.portico2.common.messaging.ResponseMessage;

/**
 * NEW Structure of a message:
 *   - Header
 *   - Payload
 *   - Auth Token (Optional) // layered in by auth protocol
 *   - Nonce      (Optional) // layered in by encryption protocol
 */

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
	private MessageType messageType;
	
	// Cached versions of the inflated messages.
	// Deflated versions are only constructed when the buffer is first requested.
	private PorticoMessage request;
	private Header requestHeader; 
	private ResponseMessage response;
	
	// serialized version of the message
	private byte[] buffer;
	private Header header;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Construct a new {@link Message} object that will be passed down the protocol stack.
	 * Note that this call will <b>trigger a message deflation</b>, an expensive process.
	 * Once constructed, the buffer inside this instance will contain the serialized contents. 
	 * 
	 * @param request The request that has been made
	 * @param calltype The type of call that it is (will go into the header)
	 * @param requestId The request ID, if any, for the call (will go into the header)
	 */
	public Message( PorticoMessage request, CallType calltype, int requestId )
	{
		this.calltype = calltype;
		this.requestId = requestId;
		this.messageType = request.getType();

		this.request = request;
		this.requestHeader = null;    // set in deflateAndStoreResponse()
		this.response = null;         // set in deflateAndStoreResponse()                       

		// create a buffer big enough for the header and the message
		// deflate the message into it
		// populate the header in the buffer
		this.buffer = MessageHelpers.deflate2( request, calltype, requestId );
		this.header = new Header( buffer, 0 ); // FIXME
	}
	
	public Message( byte[] buffer )
	{
		this.buffer = buffer;
		this.header = new Header( buffer, 0 );
		this.requestId = header.getRequestId();
		this.calltype = this.header.getCallType();
		this.messageType = this.header.getMessageType();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tells the message to take the contents of the buffer and inflate it into a full
	 * {@link PorticoMessage} object, returning the result. The request object is lazy
	 * loaded, so if you call {@link #getOriginalRequest()} before this method has been called,
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
		
		this.response = response;
		this.requestHeader = new Header( buffer, 0 ); // store the old header
		this.replaceBuffer( MessageHelpers.deflate2(response,this.requestId,this.request) );
	}
	
	/**
	 * Replace the existing buffer with the given one. This will generate a new header
	 * based on the start of the new buffer and will update the buffer payload to be the
	 * appropriate size based on the new buffer.
	 * <p/>
	 * 
	 * This call is primariliy used by the encryption protocols.
	 * 
	 * @param buffer The new buffer we want to use.
	 */
	public final void replaceBuffer( byte[] buffer )
	{
		this.buffer = buffer;
		this.header = new Header( buffer, 0 );
		this.header.writePayloadLength( buffer.length-Header.HEADER_LENGTH );
	}


	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public final int getRequestId() { return this.requestId; }
	public final Header getHeader() { return this.header; }
	public final byte[] getBuffer() { return this.buffer; }
	public final CallType getCallType() { return this.header.getCallType(); }
	public final MessageType getMessageType() { return this.header.getMessageType(); }
	public final boolean hasRequest() { return this.request != null; }
	public final PorticoMessage getOriginalRequest() { return this.request; }
	
	/** @return Header for original request if this message now holds a response
	            as converted via {@link #deflateAndStoreResponse(ResponseMessage)}.
	            Returns <code>null</code> if this message is a request still. */
	public final Header getOriginalHeader() { return this.requestHeader; }

	public final boolean hasResponse() { return this.response != null; }
	public final ResponseMessage getResponse() { return this.response; }

	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
