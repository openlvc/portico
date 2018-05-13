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
package org.portico2.common.messaging;

import org.portico.utils.bithelpers.BitHelpers;
import org.portico.utils.bithelpers.BufferUnderflowException;
import org.portico.utils.messaging.PorticoMessage;

/**
 * Messages sent over the Portico network are all sent with a particular header. This class
 * represents a header and handles the logic for encoding and decoding it to and from a byte[].
 * <p/>
 * 
 * Note that this class does not carry any information except for the byte[] representing a
 * payload with a header. It will only look at the first {@link #HEADER_LENGTH} of that payload.
 * Any time you call a <code>getXxx()</code> method, it will re-extract that information from
 * the header. It does not store or cache any information. As such, its construction is very
 * light weight. No copies, no decoding. If you only want one value you can safely wrap the
 * payload in the header class and get the information you need without triggering excess
 * processing of the header.
 * 
 * <b>Encoding</b><p/>
 * The various <code>writeXxx()</code> methods will write individual pieces of data into the
 * header at the appropriate locations. There are two static methods that take the information
 * that is needed when building messages for the most common types of situations.
 * <p/>
 * Note that the information is written directly into the payload, overwriting whatever is there.
 * No copy or cache of the data is made or held for later.
 * <p/>
 * 
 * <b>Decoding</b><p/>
 * The various <code>getXxx</code> methods in this class will read from the appropriate position
 * in a payload and return the appropriate value. Note that they do not store or cache these
 * values in any way. They read and decode them every time they are called.
 */
public class Header
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final int HEADER_LENGTH = 16;
	public static final byte[] EMPTY_HEADER = new byte[HEADER_LENGTH];

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private byte[] buffer;
	private int offset;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Header( byte[] buffer, int offset )
	{
		if( buffer.length < HEADER_LENGTH )
			throw new BufferUnderflowException( "Header requires at least "+HEADER_LENGTH+" bytes; found "+buffer.length );
		
		this.buffer = buffer;
		this.offset = offset;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public final CallType getCallType()
	{
		return CallType.fromId( BitHelpers.readUint4(buffer,offset,0) );
	}
	
	public final void writeCallType( CallType calltype )
	{
		BitHelpers.putUint4( (byte)calltype.getId(), buffer, offset, 0 );
	}
	
	public final int getFederation()
	{
		return BitHelpers.readUint4( buffer, offset, 4 );
	}
	
	public final void writeFederation( int federationHandle )
	{
		BitHelpers.putUint4( (byte)federationHandle, buffer, offset, 4 );
	}
	
	public final int getSourceFederate()
	{
		return BitHelpers.readUint16( buffer, offset+4 );
	}
	
	public final void writeSourceAndTargetFederate( int sourceFederate, int targetFederate )
	{
		BitHelpers.putUint16( sourceFederate, buffer, offset+4 );
		BitHelpers.putUint16( targetFederate, buffer, offset+6 );
	}
	
	public final int getTargetFederate()
	{
		return BitHelpers.readUint16( buffer, offset+6 );
	}
	
	public final MessageType getMessageType()
	{
		return MessageType.fromId( BitHelpers.readUint8(buffer,offset+1) );
	}
	
	public final void writeMessageType( MessageType type )
	{
		BitHelpers.putUint8( type.getId(), buffer, offset+1 );
	}
	
	public final int getRequestId()
	{
		return BitHelpers.readUint16( buffer, offset+2 );
	}
	
	public final void writeRequestId( int requestId )
	{
		BitHelpers.putUint16( requestId, buffer, offset+2 );
	}

	public final boolean hasFilteringData()
	{
		return BitHelpers.readBooleanBit( buffer, offset+8, 0 );
	}
	
	public final void writeHasFilteringData( boolean hasFiltering )
	{
		BitHelpers.putBooleanBit( hasFiltering, buffer, offset+8, 0 );
	}

	public final boolean isFilteringDataObjectClass()
	{
		return BitHelpers.readBooleanBit( buffer, offset+8, 1 );
	}
	
	public final void writeFilteringDataIsObjectClass( boolean isObjectClass )
	{
		BitHelpers.putBooleanBit( isObjectClass, buffer, offset+8, 1 );
	}
	
	public final int getFilteringClassHandle()
	{
		return BitHelpers.readUint16( buffer, offset+10 );
	}
	
	public final void writeFilteringClassHandle( int classHandle )
	{
		BitHelpers.putUint16( classHandle, buffer, offset+10 );
	}
	
	public final boolean isEncrypted()
	{
		return BitHelpers.readBooleanBit( buffer, offset+12, 0 );
	}
	
	public final void writeIsEncrypted( boolean isEncrypted )
	{
		BitHelpers.putBooleanBit( false, buffer, offset+12, 0 );
	}
	
	public final boolean isManualMarshal()
	{
		return BitHelpers.readBooleanBit( buffer, offset+12, 1 );
	}
	
	public final void writeIsManualMarshal( boolean isManualMarshal )
	{
		BitHelpers.putBooleanBit( isManualMarshal, buffer, offset+12, 1 );		
	}
	
	public final int getPayloadLength()
	{
		return (int)BitHelpers.readUint24( buffer, offset+13 );
	}
	
	public final void writePayloadLength( int payloadLength )
	{
		BitHelpers.putUint24( payloadLength, buffer, offset+13 );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void writeHeader( byte[] buffer,           // buffer to write into
	                                int byteOffset,          // offset to start at within buffer
	                                PorticoMessage message,  // message with lots of info needed
	                                CallType calltype,       // the type of call this is
	                                int requestId,           // id for request correlation
	                                boolean encrypted,       // should the payload be encrypted?
	                                int payloadLength )      // length of the payload
	{
		Header header = new Header( buffer, byteOffset );
		
		// Main Header
		//    4-bit, Call Type     (enum)
		//    4-bit, Federation ID (uint4)
		//    8-bit, Message ID    (uint8)
		//   16-bit, Request ID    (uint16)
		header.writeCallType( calltype );
		header.writeFederation( message.getTargetFederation() );
		header.writeMessageType( message.getType() );
		header.writeRequestId( requestId );
		
		// Routing
		//   16-bit, Source FederateHandle (uint16)
		//   16-bit, Target FederateHandle (uint16)
		header.writeSourceAndTargetFederate( message.getSourceFederate(),
		                                     message.getTargetFederate() );

		// Optional Forwarding
		//    1-bit, Forwarding Data Present?  (boolean)
		//    1-bit, Object Class Handle?      (boolean)
		//   14-bit, Padding
		//   16-bit, Object/Interaction Handle (uint16)
		//
		// FIXME SKIP FOR NOW
		
		// Payload Information
		//    1-bit, Encrypted?                (boolean)
		//    1-bit, Manually Marshalled?      (boolean)
		//    1-bit, Padding
		//    5-bit, Tail Padding              (uint5)
		//   24-bit, Message Length            (uint24)
		header.writeIsEncrypted( false );
		header.writeIsManualMarshal( message.supportsManualMarshal() );
		// FIXME Tail padding?
		header.writePayloadLength( payloadLength );
	}
	
	public static void writeResponseHeader( byte[] buffer,
	                                        int byteOffset,
	                                        int requestId,
	                                        boolean isSuccess,
	                                        int targetFederation,
	                                        int sourceFederate,
	                                        int targetFederate,
	                                        int payloadLength )
	{
		Header header = new Header( buffer, byteOffset );
		// TODO Put something here to zero-out the header. It currently is zero'd out by
		//      virtue of the way MessageHelpers.deflate() works, but that isn't guaranteed
		//      to stay that way if we look at more efficient buffer use
		
		// Main Header
		header.writeCallType( CallType.ControlResp );
		header.writeFederation( targetFederation );
		header.writeMessageType( isSuccess ? MessageType.SuccessResponse : MessageType.ErrorResponse );
		header.writeRequestId( requestId );
		
		// Routing
		header.writeSourceAndTargetFederate( sourceFederate, targetFederate );
		
		// Optional Forwarding
		header.writeHasFilteringData( false );
		header.writeFilteringDataIsObjectClass( false );
		header.writeFilteringClassHandle( 0 );
		//   None
		
		// Payload Information
		//header.writeIsEncrypted( false );      // default (0)
		//header.writeIsManualMarshal( false );  // default (0)
		header.writePayloadLength( payloadLength );
	}
	                                
}
