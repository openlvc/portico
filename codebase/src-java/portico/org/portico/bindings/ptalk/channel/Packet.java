/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.channel;

import java.net.InetSocketAddress;
import java.util.Random;

import org.portico.bindings.ptalk.Common;
import org.portico.bindings.ptalk.protocol.IProtocol;

/**
 * A {@link Packet} represents a logical message that should be sent down a {@link Pipeline} before
 * being handed off to the network for transmission. This doesn't necessarily need to correspond to
 * actual messages sent on the network in a 1-to-1 fashion. Each Packet contains a {@link Headers}
 * instance to hold any {@link IProtocol} specific headers and it also contains a payload,
 * representing the core data to be transmitted.
 */
public class Packet
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final Random RANDOM = new Random( System.nanoTime() );

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Headers headers;
	private byte[] payload;
	
	private transient InetSocketAddress sender;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Creates a new packet with an empty payload. You will have to set it via
	 * {@link #setPayload(byte[])}
	 */
	public Packet()
	{
		this( new byte[0] );
	}

	/**
	 * Creates a new packet using the given payload.
	 * @param payload
	 */
	public Packet( byte[] payload )
	{
		this.headers = new Headers();
		this.payload = payload;
	}

	public Packet( byte[] payload, InetSocketAddress addressOfSender )
	{
		this( payload );
		this.sender = addressOfSender;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public Headers getHeaders()
	{
		return headers;
	}

	public byte[] getPayload()
	{
		return this.payload;
	}
	
	public String getPayloadAsString()
	{
		return new String( this.payload );
	}
	
	public int getPayloadAsInt()
	{
		return Common.byteArrayToInteger( this.payload );
	}
	
	public void setPayload( byte[] payload )
	{
		this.payload = payload;
	}

	public void clearPacket()
	{
		this.headers.clearHeaders();
		this.payload = new byte[0];
	}

	public void setSender( InetSocketAddress sender )
	{
		this.sender = sender;
	}

	public InetSocketAddress getSender()
	{
		return this.sender;
	}

	/**
	 * This method will generate a random serial number, add a {@link Headers#SERIAL} header to the
	 * Packet using the serial as the value and then return the generated serial.
	 */
	public int attachSerial()
	{
		int serial = RANDOM.nextInt();
		setHeader( Header.SERIAL, serial );
		return serial;
	}

	/**
	 * This method is used when constructing packets that are a response to other packets. It takes
	 * the serial header from the given packet and applies it to the current packet so as to signal
	 * that the two are linked. If there is no serial header on the packet (or the packet is null) 
	 */
	public final void attachSerial( Packet packet )
	{
		packet.headers.copyTo( Header.SERIAL, this.headers );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Marshaling Code /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Marshal the contents of the packet into a new byte[] and return it. This will store all the
	 * headers and the payload in a way that allows them to be extracted properly later. A new
	 * array will be allocated and returned.
	 */
	public byte[] marshal()
	{
		// create a byte[] big enough to hold both the headers and the core data (and an extra int
		// that stores the size of the payload)
		byte[] buffer = new byte[headers.getMarshaledSize()+4+payload.length];
		marshal( buffer, 0 );
		return buffer;
	}
	
	/**
	 * Marshal the contents of the packet (headers and payload) into the provided array, beginning
	 * at the given offset. <b>This method assumes there is enough room in the buffer</b> to
	 * complete this operation. The number of bytes that were written is returned.
	 */
	public int marshal( byte[] buffer, int offset )
	{
		// dump the headers into the buffer
		int bytesWritten = 0;
		bytesWritten = headers.marshal( buffer, offset );
		
		// copy in the payload data - payload size first, then the payload itself
		Common.integerToByteArray( this.payload.length, buffer, offset+bytesWritten );
		bytesWritten += 4;
		
		System.arraycopy( this.payload, 0, buffer, offset+bytesWritten, this.payload.length );
		bytesWritten += this.payload.length;
		
		return bytesWritten; 
	}

	/**
	 * Returns the size that would be required to store the headers and payload of this packet
	 */
	public int getMarshaledSize()
	{
		// headers size + int for size of payload + payload size
		return headers.getMarshaledSize() + 4 + this.payload.length;
	}
	
	/**
	 * This method unmarshals the contents of the given buffer into this packet for processing.
	 * 
	 * @param buffer the buffer to extract the contents from.
	 */
	public int unmarshal( byte[] buffer, int offset )
	{
		// read the headers out first
		int bytesRead = 0;
		bytesRead += headers.unmarshal( buffer, offset );
		
		// read out the payload
		int payloadSize = Common.byteArrayToInteger( buffer, offset+bytesRead );
		bytesRead += 4;
		this.payload = new byte[payloadSize];
		System.arraycopy( buffer, offset+bytesRead, this.payload, 0, payloadSize );
		bytesRead += payloadSize;
		
		return bytesRead;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Header Shortcut Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns <code>true</code> if this packet contains the provided header. This is just
	 * a shortcut to <code>getHeaders().hasHeader()</code>.
	 */
	public boolean hasHeader( Header header )
	{
		return this.headers.hasHeader( header );
	}
	
	public void setHeader( Header header, byte[] value )
	{
		this.headers.setHeader( header, value );
	}

	public void setHeader( Header header, byte value )
	{
		this.headers.setHeader( header, value );
	}
	
	public void setHeader( Header header, int value )
	{
		this.headers.setHeader( header, value );
	}
	
	public void setHeader( Header header, String value )
	{
		this.headers.setHeader( header, value );
	}
	
	public byte[] getHeader( Header header )
	{
		return this.headers.getHeader( header );
	}
	
	public byte getHeaderAsByte( Header header )
	{
		return this.headers.getHeaderAsByte( header );
	}

	public int getHeaderAsInt( Header header ) throws RuntimeException
	{
		return this.headers.getHeaderAsInt( header );
	}

	public String getHeaderAsString( Header header )
	{
		return this.headers.getHeaderAsString( header );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
