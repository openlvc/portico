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

import org.portico.utils.StringUtils;
import org.portico.utils.bithelpers.BitHelpers;
import org.portico.utils.bithelpers.BufferUnderflowException;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;
import org.portico2.common.network.protocols.crypto.CipherMode;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.common.services.object.msg.UpdateAttributes;

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

	//////////////////////////////////////////////////////////////////////////////////////
	///  Header Structure    /////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	//
	//  0                   1                   2                   3
	//  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// | CType | FedID |   MessageID   |           RequestId           |    // Main Header
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// |         Source Handle         |         Target Handle         |    // Routing
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// |F|O|M|E|Cipher.|        Filtering Handle       | Message Len.. |    // Payload Header
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// |      Message Length (cont)    |           Auth Token          |    // Payload Header (cont)
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
	// |                                                               |    // Payload Data
	// +                           Payload...                          +
	// |                                                               |
	// +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    //
    //  == Main Header ==
    //  (00-03)   | 4-bit  | Call Type: enum, {Data,ControlSync,ControlAsync,ControlResp,Bundle,Reserverd,Reserved,Reserverd}
    //  (04-07)   | 4-bit  | Federation ID: half-byte, range=1-16
    //  (08-15)   | 8-bit  | Message Type: byte, ID for the specific message type
    //  (16-31)   | 16-bit | Request ID: uint, range=64k 
    //
    //  == Routing ==
    //  (32-47)   | 16-bit | Source Handle: handle of source federate 
    //  (48-63)   | 16-bit | Target Handle: handle of target federate
    //
    // == Payload Header ==
    //  (64-64)   | 1-bit  | Forwarding Data Present? boolean, Is forwarding/filtering info present
    //  (65-65)   | 1-bit  | Type of handle: boolean, 0 if object, 1 if interaction
    //  (66-66)   | 1-bit  | Manual Marshal: boolean, Was the message manually marshalled?
    //  (67-67)   | 1-bit  | Encrypted: boolean, Is the payload encrypted?
    //  (68-71)   | 4-bit  | Cipher Mode: ID into CipherMode enumeration
    //  (72-87)   | 16-bit | Filtering Handle: uint, range=65k, Object of Interaction Class the payload is concerned with
    //  (88-103)  | 16-bit | Auth Token: Authentication Token used to identify an authenticated federate
    //  (104-127) | 24-bit | Message length: range=16,777,216 (16MB)
    //
    // == Payload ==
    //  (128-xxx) | Varies | Payload Data, padded out to nearest 32-bit boundary.
    //

	//////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators    ///////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	//
	// Main Header Section
	//
	public final CallType getCallType()
	{
		return CallType.fromId( BitHelpers.readUint4(buffer,offset,0) );
	}
	
	public final boolean isDataMessage()
	{
		return BitHelpers.readUint4(buffer,offset,0) == 0;
	}
	
	public final boolean isControlMessage()
	{
		return !isDataMessage();
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
	
	//
	// Routing Section
	//
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

	//
	// Payload Header
	//

	// Has Filtering?
	public final boolean hasFilteringData()
	{
		return BitHelpers.readBooleanBit( buffer, offset+8, 0 );
	}
	
	public final void writeHasFilteringData( boolean hasFiltering )
	{
		BitHelpers.putBooleanBit( hasFiltering, buffer, offset+8, 0 );
	}

	// Filtering handle is object class handle?
	public final boolean isFilteringHandleObject()
	{
		return BitHelpers.readBooleanBit( buffer, offset+8, 1 ) == false;
	}
	
	public final boolean isFilteringHandleInteraction()
	{
		return BitHelpers.readBooleanBit( buffer, offset+8, 1 ) == true;
	}

	public final void writeFilteringHandleIsObject( boolean isObjectHandle )
	{
		// 0 if Object Handle
		// 1 if Interaction Class Handle
		if( isObjectHandle )
			BitHelpers.putBooleanBit( false, buffer, offset+8, 1 );
		else
			BitHelpers.putBooleanBit( true, buffer, offset+8, 1 );
	}
	
	// Manual Marshal
	public final boolean isManualMarshal()
	{
		return BitHelpers.readBooleanBit( buffer, offset+8, 2 );
	}
	
	public final void writeIsManualMarshal( boolean isManualMarshal )
	{
		BitHelpers.putBooleanBit( isManualMarshal, buffer, offset+8, 2 );		
	}
	
	// Encrypted
	public final boolean isEncrypted()
	{
		return BitHelpers.readBooleanBit( buffer, offset+8, 3 );
	}
	
	public final void writeIsEncrypted( boolean isEncrypted )
	{
		BitHelpers.putBooleanBit( isEncrypted, buffer, offset+8, 3 );
	}
	
	// Cipher Mode
	public final CipherMode getCipherMode()
	{
		return CipherMode.fromId( BitHelpers.readUint4(buffer,offset+8,4) );
	}
	
	public final void writeCipherMode( CipherMode cipherMode )
	{
		BitHelpers.putUint4( (byte)cipherMode.getId(), buffer, offset+8, 4 );
	}
	
	// Filtering Handle
	public final int getFilteringHandle()
	{
		return (int)BitHelpers.readUint24( buffer, offset+9 );
	}
	
	public final void writeFilteringHandle( int handle )
	{
		BitHelpers.putUint24( handle, buffer, offset+9 );
	}
	
	// Message Size
	public final int getPayloadLength()
	{
		return (int)BitHelpers.readUint24( buffer, offset+11 );
	}
	
	public final void writePayloadLength( int payloadLength )
	{
		BitHelpers.putUint24( payloadLength, buffer, offset+11 );
	}

	public final int getFullMessageLength()
	{
		return getPayloadLength()+HEADER_LENGTH;
	}
	
	public final short getAuthToken()
	{
		return BitHelpers.readShortBE( buffer, offset+14 );
	}
	
	public final void writeAuthToken( short token )
	{
		BitHelpers.putShortBE( token, buffer, offset+14 );
	}

	@Override
	public String toString()
	{
		//  Header: call=CALLTYPE, federation=1, message=MESSAGE_TYPE, requestId=1234
		// Routing: source=<>, target=<>
		// Payload: flags=ManualMarshal(true); Filtering(Object=1234)
		//          encrypted=false, Cipher=<>
		//          payload=1234 bytes

		// Header
		String line1 = String.format( " Header: call=%s, federation=%d, message=%s, requestId=%d\n",
		                              getCallType(), getFederation(), getMessageType(), getRequestId() );

		// Routing
		String line2 = String.format( "Routing: source=%s, target=%s\n",
		                              StringUtils.sourceHandleToString(getSourceFederate()),
		                              StringUtils.targetHandleToString(getTargetFederate()) );

		// Payload Data
		String filterString = "not present";
		if( hasFilteringData() )
		{
			filterString = String.format( "%s=%d",
			                              isFilteringHandleObject() ? "Object" : "Interaction",
			                              getFilteringHandle() );
		}
		
		String line3 = String.format( "Auth Token: "+String.format("%04x",getAuthToken()) );

		String line4 = String.format( "Payload: flags=ManualMarshal(%s);Filtering(%s)\n",
		                              isManualMarshal(), filterString );

		String line5 = String.format( "         encrypted=%s, cipher=%s\n",
		                              isEncrypted(), isEncrypted() ? getCipherMode() : "None" );
		
		String line6 = String.format( "         payload=%s\n",
		                              StringUtils.humanReadableSize(getPayloadLength()) );
		
		// An utterly frivolous use of cycles
		StringBuilder builder = new StringBuilder();
		builder.append( "\n" );
		builder.append( line1 );
		builder.append( line2 );
		builder.append( line3 );
		builder.append( line4 );
		builder.append( line5 );
		builder.append( line6 );
		return builder.toString();
		
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static void writeHeader( byte[] buffer,           // buffer to write into
	                                int byteOffset,          // offset to start at within buffer
	                                PorticoMessage message,  // message with lots of info needed
	                                CallType calltype,       // the type of call this is
	                                int requestId,           // id for request correlation
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

		// Payload Header
		//    1-bit, Filtering Data Present?   (boolean)
		//    1-bit, Object Class Handle?      (boolean)
		//    1-bit, Manually Marshalled?      (boolean)
		//    1-bit, Encrypted?                (boolean)
		//    4-bit, Cipher Mode               (uint4)
		//   16-bit, Object/Interaction Handle (uint16)
		//   24-bit, Message Length            (uint24)
		//   16-bit, Auth Token                (int16)
		if( calltype == CallType.DataMessage )
		{
			header.writeHasFilteringData( true );
			switch( message.getType() )
			{
				case UpdateAttributes:
					header.writeFilteringHandleIsObject( true );
					header.writeFilteringHandle( ((UpdateAttributes)message).getObjectId() );
					break;
				case SendInteraction:
					header.writeFilteringHandleIsObject( false );
					header.writeFilteringHandle( ((SendInteraction)message).getInteractionId() );
					break;
				default:
					break;
			}
		}

		// Manual Marshalling?
		header.writeIsManualMarshal( message.supportsManualMarshal() );
		
		// Encryption Information
		// This is written in by the EncryptionProtocol if it is used.
		
		// Auth Token
		// This is written in by the AuthenticationProtocol if it is used.
		
		// Payload Information
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
		//header.writeFilteringDataIsObjectClass( false );
		//header.writeFilteringClassHandle( 0 );
		//   None
		
		// Payload Information
		// Encryption information written in encryption IProtocol
		// Auth token information written in authentication header
		// Write payload size
		header.writePayloadLength( payloadLength );
	}
	                                
}
