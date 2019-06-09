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
package org.portico.utils.bithelpers;

import java.util.UUID;

/**
 * A group of static utility methods to help with various bit manipulation tasks.
 */
public class BitHelpers
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

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Helper Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Check that the required number of bytes are present in the array, starting from
	 * the provided offset. If not, throw a {@link BufferOverflowException}.
	 * 
	 * @param required The number of bytes we expect to write
	 * @param buffer The buffer to check
	 * @param offset The offset to start from
	 */
	public static void checkOverflow( int required, byte[] buffer, int offset )
		throws BufferOverflowException
	{
		int length = buffer.length - offset;
		if( length < required )
		{
			throw new BufferOverflowException( "Buffer overflow. Tried to write "+required+
			                                   " bytes to buffer, only "+length+" bytes available" );
		}
	}

	/**
	 * Check that the required number of bytes are present in the array, starting from
	 * the provided offset. If not, throw a {@link BufferUnderflowException}.
	 * 
	 * @param required The number of bytes we expect to read
	 * @param buffer The buffer to check
	 * @param offset The offset to start from
	 */
	public static void checkUnderflow( int required, byte[] buffer, int offset )
	{
		int length = buffer.length - offset;
		if( length < required )
		{
			throw new BufferUnderflowException( "Buffer underflow. Tried to read "+required+
			                                    " bytes from buffer, found "+length );
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Byte[] Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Copies the provided byte[] value into the buffer starting from the specified offset.
	 * Checks to ensure there is enough room to write into first, throwing a
	 * {@link BufferOverflowException} if there is not.
	 * 
	 * @param value The byte[] to write into the buffer
	 * @param buffer The buffer to write into
	 * @param offset The offset to commence at
	 * @throws BufferOverflowException If there is not enough space to write the value into
	 */
	public static void putByteArray( byte[] value, byte[] buffer, int offset )
		throws BufferOverflowException
	{
		checkOverflow( value.length, buffer, offset );
		System.arraycopy( value, 0, buffer, offset, value.length );
	}
	
	/**
	 * Copies the provided byte[] value into the buffer starting from the specified offset.
	 * Will copy the given number of bytes (in length) from the source array.
	 * Checks to ensure there is enough room to write into first, throwing a
	 * {@link BufferOverflowException} if there is not.
	 * 
	 * @param value The byte[] to write into the buffer
	 * @param target The buffer to write into
	 * @param offset The offset to commence at
	 * @param length The number of bytes to write from the source array
	 * @throws BufferOverflowException If there is not enough space to write the value into
	 */
	public static void putByteArray( byte[] value, byte[] target, int offset, int length )
	{
		checkOverflow( length, target, offset );
		System.arraycopy( value, 0, target, offset, length );
	}

	/**
	 * Reads and returns a sub-byte[] from the given buffer. Starting at the offset position
	 * and extending for <code>length</code> bytes. If there is not enough information in the
	 * buffer to satisfy the request, a {@link BufferUnderflowException} is thrown.
	 * 
	 * @param buffer The buffer to read from
	 * @param offset The position to start reading from
	 * @param length The number of bytes to read
	 * @return A new byte[] representing the sub-view of the buffer 
	 */
	public static byte[] readByteArray( byte[] buffer, int offset, int length )
	{
		checkUnderflow( length, buffer, offset );
		byte[] target = new byte[length];
		System.arraycopy( buffer, offset, target, 0, length );
		return target;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Floating Point Methods //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Write the given float value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putFloatBE( float value, byte[] buffer, int offset )
	{
		checkOverflow( 4, buffer, offset );

		int rawbits = Float.floatToIntBits( value );
		buffer[offset]   = (byte)(rawbits >>> 24);
		buffer[offset+1] = (byte)(rawbits >>> 16);
		buffer[offset+2] = (byte)(rawbits >>>  8);
		buffer[offset+3] = (byte)(rawbits >>>  0);
	}

	/**
	 * Write the given float value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putFloatLE( float value, byte[] buffer, int offset )
	{
		checkOverflow( 4, buffer, offset );

		int rawbits = Float.floatToIntBits( value );
		buffer[offset+3] = (byte)(rawbits >>> 24);
		buffer[offset+2] = (byte)(rawbits >>> 16);
		buffer[offset+1] = (byte)(rawbits >>>  8);
		buffer[offset]   = (byte)(rawbits >>>  0);
	}

	
	/**
	 * Read and return a float value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static float readFloatBE( byte[] buffer, int offset )
	{
		checkUnderflow( 4, buffer, offset );

		int temp = ((buffer[0] << 24) |
		           ((buffer[1] & 255) << 16) |
		           ((buffer[2] & 255) << 8) |
		           ((buffer[3] & 255) << 0));

		return Float.intBitsToFloat( temp );
	}

	/**
	 * Read and return a float value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static float readFloatLE( byte[] buffer, int offset )
	{
		checkUnderflow( 4, buffer, offset );

		int temp = ((buffer[3] << 24) |
		           ((buffer[2] & 255) << 16) |
		           ((buffer[1] & 255) << 8) |
		           ((buffer[0] & 255) << 0));

		return Float.intBitsToFloat( temp );
	}

	/**
	 * Write the given double value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putDoubleBE( double value, byte[] buffer, int offset )
	{
		checkOverflow( 8, buffer, offset );

		long rawbits = Double.doubleToLongBits( value );
		buffer[offset]   = (byte)(rawbits >>> 56);
		buffer[offset+1] = (byte)(rawbits >>> 48);
		buffer[offset+2] = (byte)(rawbits >>> 40);
		buffer[offset+3] = (byte)(rawbits >>> 32);
		buffer[offset+4] = (byte)(rawbits >>> 24);
		buffer[offset+5] = (byte)(rawbits >>> 16);
		buffer[offset+6] = (byte)(rawbits >>>  8);
		buffer[offset+7] = (byte)(rawbits >>>  0);
	}

	/**
	 * Write the given double value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putDoubleLE( double value, byte[] buffer, int offset )
	{
		checkOverflow( 8, buffer, offset );

		long rawbits = Double.doubleToLongBits( value );
		buffer[offset+7] = (byte)(rawbits >>> 56);
		buffer[offset+6] = (byte)(rawbits >>> 48);
		buffer[offset+5] = (byte)(rawbits >>> 40);
		buffer[offset+4] = (byte)(rawbits >>> 32);
		buffer[offset+3] = (byte)(rawbits >>> 24);
		buffer[offset+2] = (byte)(rawbits >>> 16);
		buffer[offset+1] = (byte)(rawbits >>>  8);
		buffer[offset]   = (byte)(rawbits >>>  0);
	}

	/**
	 * Read and return a double value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static double readDoubleBE( byte[] buffer, int offset )
	{
		checkUnderflow( 8, buffer, offset );

		long temp = (((long)buffer[0] << 56) |
		             ((long)(buffer[1] & 255) << 48) |
		             ((long)(buffer[2] & 255) << 40) |
		             ((long)(buffer[3] & 255) << 32) |
		             ((long)(buffer[4] & 255) << 24) |
		             ((buffer[5] & 255) << 16) |
		             ((buffer[6] & 255) <<  8) |
		             ((buffer[7] & 255) <<  0));

		return Double.longBitsToDouble( temp );
	}

	/**
	 * Read and return a double value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static double readDoubleLE( byte[] buffer, int offset )
	{
		checkUnderflow( 8, buffer, offset );

		long temp = (((long)buffer[7] << 56) |
		             ((long)(buffer[6] & 255) << 48) |
		             ((long)(buffer[5] & 255) << 40) |
		             ((long)(buffer[4] & 255) << 32) |
		             ((long)(buffer[3] & 255) << 24) |
		             ((buffer[2] & 255) << 16) |
		             ((buffer[1] & 255) <<  8) |
		             ((buffer[0] & 255) <<  0));

		return Double.longBitsToDouble( temp );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Short Methods ///////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Write the given int value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 2 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putShortBE( short value, byte[] buffer, int offset )
	{
		checkOverflow( 2, buffer, offset );

		buffer[offset]   = (byte)(value >> 8);
		buffer[offset+1] = (byte)(value /*>> 0*/);
	}

	/**
	 * Write the given int value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 2 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putShortLE( short value, byte[] buffer, int offset )
	{
		checkOverflow( 2, buffer, offset );

		buffer[offset+1] = (byte)(value >> 8);
		buffer[offset]   = (byte)(value /*>> 0*/);
	}

	/**
	 * Read and return an int value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 2 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static short readShortBE( byte[] buffer, int offset )
	{
		checkUnderflow( 2, buffer, offset );
		
		return (short)(((buffer[offset]   & 255) << 8) |
			           ((buffer[offset+1] & 255) /*<< 0*/));
	}

	/**
	 * Read and return an int value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 2 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static short readShortLE( byte[] buffer, int offset )
	{
		checkUnderflow( 2, buffer, offset );
		
		return (short)(((buffer[offset+1] & 255) << 8)  |
		               ((buffer[offset]   & 255) /*<< 0*/));
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////// Int Methods ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Write the given int value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putIntBE( int value, byte[] buffer, int offset )
	{
		checkOverflow( 4, buffer, offset );

		buffer[offset]   = (byte)(value >> 24);
		buffer[offset+1] = (byte)(value >> 16);
		buffer[offset+2] = (byte)(value >> 8);
		buffer[offset+3] = (byte)(value /*>> 0*/);
	}

	/**
	 * Write the given int value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putIntLE( int value, byte[] buffer, int offset )
	{
		checkOverflow( 4, buffer, offset );

		buffer[offset+3] = (byte)(value >> 24);
		buffer[offset+2] = (byte)(value >> 16);
		buffer[offset+1] = (byte)(value >> 8);
		buffer[offset]   = (byte)(value /*>> 0*/);
	}

	/**
	 * Read and return an int value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static int readIntBE( byte[] buffer, int offset )
	{
		checkUnderflow( 4, buffer, offset );
		
		return ((buffer[offset]   & 255) << 24) |
		       ((buffer[offset+1] & 255) << 16) |
		       ((buffer[offset+2] & 255) << 8)  |
		       ((buffer[offset+3] & 255) /*<< 0*/);
	}

	/**
	 * Read and return an int value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static int readIntLE( byte[] buffer, int offset )
	{
		checkUnderflow( 4, buffer, offset );
		
		return ((buffer[offset+3] & 255) << 24) |
		       ((buffer[offset+2] & 255) << 16) |
		       ((buffer[offset+1] & 255) << 8)  |
		       ((buffer[offset]   & 255) /*<< 0*/);
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Long Methods ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Write the given long value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putLongBE( long value, byte[] buffer, int offset )
	{
		checkOverflow( 8, buffer, offset );

		buffer[offset]   = (byte)(value >>> 56);
		buffer[offset+1] = (byte)(value >>> 48);
		buffer[offset+2] = (byte)(value >>> 40);
		buffer[offset+3] = (byte)(value >>> 32);
		buffer[offset+4] = (byte)(value >>> 24);
		buffer[offset+5] = (byte)(value >>> 16);
		buffer[offset+6] = (byte)(value >>> 8);
		buffer[offset+7] = (byte)(value >>> 0);
	}

	/**
	 * Write the given long value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putLongLE( long value, byte[] buffer, int offset )
	{
		checkOverflow( 8, buffer, offset );

		buffer[offset+7] = (byte)(value >>> 56);
		buffer[offset+6] = (byte)(value >>> 48);
		buffer[offset+5] = (byte)(value >>> 40);
		buffer[offset+4] = (byte)(value >>> 32);
		buffer[offset+3] = (byte)(value >>> 24);
		buffer[offset+2] = (byte)(value >>> 16);
		buffer[offset+1] = (byte)(value >>> 8);
		buffer[offset]   = (byte)(value >>> 0);
	}

	/**
	 * Read and return a long value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static long readLongBE( byte[] buffer, int offset )
	{
		checkUnderflow( 8, buffer, offset );

		return (((long)buffer[0] << 56) |
		        ((long)(buffer[1] & 255) << 48) |
		        ((long)(buffer[2] & 255) << 40) |
		        ((long)(buffer[3] & 255) << 32) |
		        ((long)(buffer[4] & 255) << 24) |
		        ((buffer[5] & 255) << 16) |
		        ((buffer[6] & 255) <<  8) |
		        ((buffer[7] & 255) <<  0));
	}

	/**
	 * Read and return a long value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static long readLongLE( byte[] buffer, int offset )
	{
		checkUnderflow( 8, buffer, offset );

		return (((long)buffer[7] << 56) |
		        ((long)(buffer[6] & 255) << 48) |
		        ((long)(buffer[5] & 255) << 40) |
		        ((long)(buffer[4] & 255) << 32) |
		        ((long)(buffer[3] & 255) << 24) |
		        ((buffer[2] & 255) << 16) |
		        ((buffer[1] & 255) <<  8) |
		        ((buffer[0] & 255) <<  0));
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Unsigned Integer Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Put the given boolean value into the bit at <code>bitPos</code> (0-7).
	 * The value will be written into the byte in the buffer at the byte offset.
	 * 
	 * @param value      The value to write
	 * @param buffer     The buffer to write into
	 * @param byteOffset The offset within the buffer to find the byte we should write into
	 * @param bitPos     The bit to set (0-7)
	 */
	public static void putBooleanBit( boolean value, byte[] buffer, int byteOffset, int bitPos )
	{
		checkOverflow( 1, buffer, byteOffset );
		
		byte mask = (byte)(1 << bitPos);
		if( value )
			buffer[byteOffset] = (byte)(buffer[byteOffset] | mask); // turn the bit on
		else
			buffer[byteOffset] = (byte)(buffer[byteOffset] & ~mask); // turn the bit off
	}

	/**
	 * Return <code>true</code> if the value of the bit we're looking up is 1, <code>false</code>
	 * if it is 0. We look up the byte within the buffer at the identified offset. We then look
	 * up the bit at <code>bitPos</code> in that byte (0-7).
	 * 
	 * @param buffer     The buffer to find the byte in
	 * @param byteOffset The offset within the buffer to find the specific byte
	 * @param bitPos     The bit index within the byte (0-7)
	 * @return True if the bit is 1, false if it is 0.
	 */
	public static boolean readBooleanBit( byte[] buffer, int byteOffset, int bitPos )
	{
		checkUnderflow( 1, buffer, byteOffset );
		
		if( bitPos < 0 || bitPos > 7 )
			throw new IllegalArgumentException( "BitPos can only be between 0 and 7" );
		
		// 1. Generate a mask with only the bit we're measuring turned on
		byte mask  = (byte)(1 << bitPos);
		
		// 2. And against the mask. All bits except bitPos turned off. bitPos will be off
		//    if it was, and on if it was
		byte value = (byte)((buffer[byteOffset] & mask));
		if( bitPos == 7 )
		{
			// can't properly shift the 8th pos because java is using it
			// as the - (negative) symbol. shifting brings in 1's rather than
			// 0's. In this case, let's just check without trying to drain the bit
			return value == -128;
		}
		else
		{
			// Bit at bitPos is now either on or off; need to drain it so we can
			// compare it to a known value
			value = (byte)(value >>> bitPos);
			return value == 1;
		}
	}

	public static void putPadding( int bytes, byte[] buffer, int offset )
	{
		switch( bytes )
		{
			case 0: return;
			case 1: putUint8((short)0,buffer,offset); return;
			case 2: putUint16(0,buffer,offset); return;
			case 3: putUint24(0,buffer,offset); return;
			case 4: putUint32(0,buffer,offset); return;
			default: break;
		}
		
		// it's more than 4... ok
		for( int i = 0; i < bytes; i++ )
			putUint8((short)0,buffer,offset);
	}

	public static void putUint4( byte value, byte[] buffer, int byteOffset, int bitOffset )
	{
		if( bitOffset > 4 )
			throw new IllegalArgumentException( "Bit offset cannot be greater than 4" );
		
		// 1. Prep Incoming Value
		//    The bits we want are sitting in the low-order 4. There _should_ be
		//    nothing in the high-order 4, but let's zero them out just in case
		int temp = (byte)(value & 0x0f);

		// 2. Shift Incoming Into Place
		//    We are writing 4 bytes starting from a particular offset. Shift
		//    the bits into place. After this, the 4-bit value will be sandwiched
		//    by 0's in the temp value.
		temp = temp << bitOffset;
		
		// 3. Zero-Out Space in Existing Value
		//    To effectively merge the prepared bits into the ones present in the
		//    buffer currently, we need to zero-out the space they'll go into
		int mask = ~(0x0f << bitOffset);
		int orig = buffer[byteOffset] & mask;
		
		// 4. Merge New into Old
		//    Or the modified original bits (step 3) with the shifted bits that
		//    represent the value we want to inject (step 2). Save back to buffer;
		buffer[byteOffset] = (byte)(orig | temp);
	}

	public static byte readUint4( byte[] buffer, int byteOffset, int bitOffset )
	{
		int temp = buffer[byteOffset] >> bitOffset;
		return (byte)(0x0f & temp);
	}

	public static void putUint8( short value, byte[] buffer, int offset )
	{
		checkOverflow( 1, buffer, offset );
		
		buffer[offset] = (byte)value;
	}
	
	public static short readUint8( byte[] buffer, int offset )
	{
		checkUnderflow( 1, buffer, offset );
		return (short)(buffer[offset] & 0xff);
	}

	
	public static void putUint16( int value, byte[] buffer, int offset )
	{
		checkOverflow( 2, buffer, offset );

		buffer[offset]   = (byte)(value >>> 8);
		buffer[offset+1] = (byte)(value /*>> 0*/);
	}
	
	public static int readUint16( byte[] buffer, int offset )
	{
		checkUnderflow( 2, buffer, offset );
		
		return ((buffer[offset]   & 0xff) << 8) |
		       ((buffer[offset+1] & 0xff));
	}
	
	public static void putUint24( long value, byte[] buffer, int offset )
	{
		checkOverflow( 3, buffer, offset );

		buffer[offset]   = (byte)(value >>> 16);
		buffer[offset+1] = (byte)(value >>> 8);
		buffer[offset+2] = (byte)(value /*>> 0*/);
	}
	
	public static long readUint24( byte[] buffer, int offset )
	{
		checkUnderflow( 3, buffer, offset );
		
		return ((buffer[offset]   & 0xff) << 16) |
		       ((buffer[offset+1] & 0xff) << 8)  |
		       ((buffer[offset+2] & 0xff));
	}
	
	public static void putUint32( long value, byte[] buffer, int offset )
	{
		checkOverflow( 4, buffer, offset );

		buffer[offset]   = (byte)(value >> 24);
		buffer[offset+1] = (byte)(value >> 16);
		buffer[offset+2] = (byte)(value >> 8);
		buffer[offset+3] = (byte)(value /*>> 0*/);
	}
	
	public static long readUint32( byte[] buffer, int offset )
	{
		checkUnderflow( 4, buffer, offset );
		
		return ((long)(buffer[offset]   & 0xff) << 24) |
		       ((long)(buffer[offset+1] & 0xff) << 16) |
		       ((long)(buffer[offset+2] & 0xff) << 8)  |
		       ((long)(buffer[offset+3] & 0xff));
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// General Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Convert the given UUID into a byte[] form for transmission. This will return a byte[16].
	 */
	public static byte[] uuidToBytes( UUID uuid )
	{
		byte[] buffer = new byte[16];
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();

		for (int i = 0; i < 8; i++)
			buffer[i] = (byte) (msb >>> 8*(7-i) );

		for (int i = 8; i < 16; i++)
			buffer[i] = (byte) (lsb >>> 8*(7-i) );

		return buffer;
	}
	
	/**
	 * Write the given UUID into the provided buffer, starting at the offset position.
	 * If there is not enough space in the buffer, an IndexOutOfBoundsException will be thrown.
	 * 
	 * @return The buffer back to the called, now with the UUID marshalled
	 */
	public static void putUUID( UUID uuid, byte[] buffer, int offset )
	{
		if( buffer.length-offset < 16 )
			throw new IndexOutOfBoundsException( "Need at least 16 bytes in buffer" );
		
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();

		for (int i = offset; i < 8+offset; i++)
			buffer[i] = (byte) (msb >>> 8*(7-i) );

		for (int i = 8+offset; i < 16+offset; i++)
			buffer[i] = (byte) (lsb >>> 8*(7-i) );
	}
	
	/**
	 * Convert the given byte[] buffer into a UUID and return. Expects a byte[] that was generated
	 * using {@link #uuidToBytes(UUID)}. 
	 */
	public static UUID uuidFromBytes( byte[] buffer )
	{
		long msb = 0;
		long lsb = 0;

		for( int i = 0; i < 8; i++ )
			msb = (msb << 8) | (buffer[i] & 0xff);

		for( int i = 8; i < 16; i++ )
			lsb = (lsb << 8) | (buffer[i] & 0xff);

		return new UUID( msb, lsb );
	}

	/**
	 * Read from the given buffer, starting at the given offset, and return a UUID using the
	 * information found. An IndexOutOfBoundsException will be thrown if the buffer is too short.
	 */
	public static UUID readUUID( byte[] buffer, int offset )
	{
		if( buffer.length-offset < 16 )
			throw new IndexOutOfBoundsException( "Need at least 16 bytes in buffer" );

		long msb = 0;
		long lsb = 0;

		for( int i = offset; i < 8+offset; i++ )
			msb = (msb << 8) | (buffer[i] & 0xff);

		for( int i = 8+offset; i < 16+offset; i++ )
			lsb = (lsb << 8) | (buffer[i] & 0xff);

		return new UUID( msb, lsb );
	}
}
