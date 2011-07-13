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

import java.util.HashMap;

import org.portico.bindings.ptalk.Common;
import org.portico.bindings.ptalk.protocol.FederationManagement;
import org.portico.bindings.ptalk.protocol.GroupManagement;

/**
 * This class is a container for a number of headers that are sent along with the contents of any
 * particular packet when it is broadcast to the network. Various {@link IProtocol} implementations
 * can add their own headers to help them complete their work. An instance of this class sits inside
 * each {@link Packet} instance.
 * <p/>
 * Each header can be as large or as small as you wish. Any number of headers can be added to a
 * {@link Headers}. Note however, that the underlying implementation uses arrays for the sake of
 * efficiency. This means that at some point, if you're adding more headers, the group is
 * going to need to grow (which could be a costly operation). The initial capacity is 4 headers,
 * with it doubling each time it grows.
 */
public class Headers
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	public int headers; // used as bitfield
	private HashMap<Header,byte[]> values;
	private int marshaledSize;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Headers()
	{
		// initialize the header set with an initial capacity of 4
		clearHeaders();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------	
	/**
	 * Remove all headers and associated values.
	 */
	public void clearHeaders()
	{
		this.headers = 0;
		this.values = new HashMap<Header,byte[]>();
		this.marshaledSize = 4 /* total size of headers */;
	}

	/**
	 * Add the given value for the given header. Duplicate values can exist in the header set,
	 * so if you don't want them, be sure to avoid them by only adding a value once. If the given
	 * value is null, the addition won't take place.
	 * 
	 * @param header The header to associate the value with. This should be one of the statics that
	 *               are pre-defined in this class
	 * @param value The value to associate with the header. If null, no addition will be made.
	 */
	public final void setHeader( Header header, byte[] value )
	{
		if( value == null )
			return;
		
		if( hasHeader(header) )
		{
			// we already have a value for this header, we need to replace it
			// and adjust the marshaled size of the headers
			marshaledSize -= (2 + values.get(header).length);
		}
		
		// mark that we have this header and store the value
		headers |= header.flag();
		values.put( header, value );
		
		// update the marshaled size - but only if we didn't alread
		marshaledSize += (1/*header index*/ + 1/*value size*/ + value.length);
		
		//assert hasHeader( header );
	}
	
	/**
	 * Special version of addHeader when the values are just expressed as a single byte.
	 */
	public final void setHeader( Header header, byte value )
	{
		setHeader( header, new byte[]{value} );
	}
	
	/**
	 * Convert the given value to a byte[] and add it to the header set using
	 * {@link #setHeader(byte, byte[])}
	 */
	public final void setHeader( Header header, int value )
	{
		// convert the int to a byte[] - big endian format
		setHeader( header, Common.integerToByteArray(value) );
	}
	
	/**
	 * Convert the given value to a byte[] and add it to the header set using
	 * {@link #setHeader(byte, byte[])}. If the provided value is null, an empty string will be
	 * used.
	 * 
	 * @param header The header to associate the value with
	 * @param value The value to associate with the header. If it is null, an empty string is used
	 */
	public final void setHeader( Header header, String value )
	{
		if( value == null )
			value = "";

		setHeader( header, value.getBytes() );
	}
	
	/**
	 * Get the value for the given header. If there is no value associated with the header, null
	 * will be returned.
	 * 
	 * @param header The header to find the value for
	 * @return The byte[] representing the raw data of the value, or null if there is no value
	 *         associated with the header
	 */
	public final byte[] getHeader( Header header )
	{
		return values.get( header );
	}
	
	/**
	 * Get the vlaue for the given header as a byte. If there is no value associated with that
	 * header, or the value is not 1-byte in length (indicating it isn't just a byte), an exception
	 * is thrown. Otherwise, the byte value is returned.
	 * 
	 * @param header The header to fetch the value for
	 * @return The value associated with the header, as a byte
	 * @throws RuntimeException If there is no value associated with the header, or the value
	 *                          is not a byte
	 */
	public final byte getHeaderAsByte( Header header ) throws RuntimeException
	{
		byte[] blob = getHeader( header );
		if( blob == null || blob.length != 1 )
			throw new RuntimeException( "Expected value for header ("+header+") to be an byte" );

		return blob[0];
	}
	
	/**
	 * Get the value for the given header as an int. If there is no value associated with that
	 * header, or the value is not 4-bytes in length (indicating it isn't an int), an exception
	 * is thrown. Otherwise, the int value is returned.
	 * 
	 * @param header The header to fetch the value for
	 * @return The value associated with the header as an int
	 * @throws RuntimeException If there is no value associated with the header, or the value
	 *                          is not an int
	 */
	public final int getHeaderAsInt( Header header ) throws RuntimeException
	{
		byte[] blob = getHeader( header );
		if( blob == null || blob.length != 4 )
		{
			if( blob == null )
			{
				throw new RuntimeException( "Expected value for header ("+header+
				                            ") to be an int, but was null" );
			}
			else
			{
				throw new RuntimeException( "Expected value for header ("+header+
				                            ") to be an int. Needed 4-bytes, found "+blob.length );
			}
		}
		
		return Common.byteArrayToInteger( blob );
	}
	
	/**
	 * Get the value for the given header as a String. If there is no value associated with that
	 * header, null is returned.
	 * 
	 * @param header The header to fetch the value for
	 * @return The value associated with the header as a String, or null if there is no value
	 */	
	public final String getHeaderAsString( Header header )
	{
		byte[] blob = getHeader( header );
		if( blob == null )
			return null;
		else
			return new String( blob );
	}
	
	/**
	 * Returns true if this instance contains a value for the identified header. Note that this
	 * could involve a search through all headers in the group.
	 */
	public final boolean hasHeader( Header header )
	{
		return (headers & header.flag()) == header.flag();
	}

	/**
	 * Copies the provided header from this instance to the given instance. This set of headers
	 * remains unchanged. If the header doesn't exist, a RuntimeException is thrown.
	 */
	public final void copyTo( Header header, Headers other ) throws RuntimeException
	{
		if( hasHeader(header) )
		{
			other.setHeader( header, values.get(header) );
		}
		else
		{
			throw new RuntimeException( "Can't copy header ["+header+
			                            "] between packets. Missing from source packet" );
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Marshaling Code /////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the current size that would be required to store the headers in marshaled form.
	 */
	public final int getMarshaledSize()
	{
		return this.marshaledSize;
	}
	
	/**
	 * Marshals the contents of the header set into a new byte[] and returns it. The layout of the
	 * array is as follows:
	 * <p/>
	 * <pre>
	 * {
	 *   [0..3] = the size of the headers (int), so that we know how far to go when unmarshaling
	 *   [4..7] = the headers bitfield
	 *   [8]    = the header we're writing the first value for
	 *   [9]    = size of the first value (as byte - less than MAX_VALUE_SIZE)
	 *   [10..x] = value for first header
	 *   [x+1]  = second header identifier
	 *   ...
	 *   ...
	 *   ...
	 * }
	 * </pre>
	 * 
	 * The total marshaled size of the headers is: <b>1 + (2 x number of headers) + (sum of value sizes)</b>
	 * <p/>
	 * 
	 */
	public final byte[] marshal()
	{
		byte[] buffer = new byte[this.marshaledSize];
		marshal( buffer, 0 );
		return buffer;
	}
	
	/**
	 * Marshals the contents of the header set into the provided buffer. If there isn't enough
	 * room in the buffer, a RuntimeException is thrown. See {@link #marshal()} for full details
	 * of the marhsaling process.
	 * <p/>
	 * 
	 * @return Returns the number of bytes that were written
	 */
	public final int marshal( byte[] buffer, int offset ) throws RuntimeException
	{
		// check that there is enough space
		if( (buffer.length-offset) < this.marshaledSize )
		{
			throw new RuntimeException( "Provided buffer is too small to marshal headers into. "+
			                            "Remaining capacity is "+(buffer.length-offset)+" bytes, "+
			                            this.marshaledSize + " bytes required" );
		}
		
		// marshal the total size of the headers
		int bytesWritten = 0;
		bytesWritten += Common.integerToByteArray( this.marshaledSize, buffer, offset );
		
		// marshal each of the headers and their values
		for( Header header : values.keySet() )
		{
			byte[] value = values.get( header );
			// write the header this is for
			buffer[offset+bytesWritten] = header.index();
			bytesWritten++;
			
			// write the value size
			final byte valueLength = (byte)value.length;
			buffer[offset+bytesWritten] = valueLength;
			bytesWritten++;
			
			// write the value itself
			System.arraycopy( value, 0, buffer, offset+bytesWritten, valueLength );
			bytesWritten += valueLength;
		}
		
		return bytesWritten;
	}
	
	/**
	 * Populate this instance from the given byte[]. Note that this will clear out any previously
	 * saved headers first. The number of bytes unmarshaled is returned.
	 */
	public final int unmarshal( byte[] buffer, int offset )
	{
		clearHeaders();
		int bytesRead = 0;

		// figure out the size of the incoming headers so we know how much to read from the buffer
		// don't update the offset because we're going to add "bytesRead" to it for future
		// calculations and we'll just start at 4 there, rather than 0
		int headersSize = Common.byteArrayToInteger( buffer, offset );
		bytesRead += 4;

		// read the header contents
		while( bytesRead < headersSize )
		{
			// read the header
			byte header = buffer[offset+bytesRead];
			bytesRead++;
			// read the value size
			byte valueSize = buffer[offset+bytesRead];
			bytesRead++;
			// read the value
			byte[] value = new byte[valueSize];
			System.arraycopy( buffer, offset+bytesRead, value, 0, valueSize );
			bytesRead += valueSize;
			
			setHeader( Header.values()[header], value );
		}
		
		return bytesRead;
	}
	
	public String toString()
	{
		if( values.isEmpty() )
			return "{empty}";
		
		StringBuilder builder = new StringBuilder();
		builder.append( "{" );
		int count = 0;
		for( Header header : values.keySet() )
		{
			builder.append( "["+header+"="+headerToString(header,values.get(header))+"]" );
			if( ++count != values.size() )
				builder.append( ", " );
		}
		
		builder.append( "}" );
		return builder.toString();
	}

	/**
	 * Used to provide more context to some header values.
	 */
	private String headerToString( Header header, byte[] value )
	{
		switch( header )
		{
			case SERIAL:
				return ""+Common.byteArrayToInteger(value);
			case GM:
				return ""+GroupManagement.MessageType.valueOf( value[0] );
			case FederationManagement:
				return ""+FederationManagement.MessageType.valueOf( value[0] );
			default:
				return value.length+"b";
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
