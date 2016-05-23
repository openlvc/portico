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
	public static void main( String[] args ) throws Exception
    {
        int beforeInt = Integer.MAX_VALUE;
        long beforeLong = Long.MAX_VALUE;
        float beforeFloat = Float.MAX_VALUE;
        double beforeDouble = Double.MAX_VALUE;

        byte[] buffer = new byte[8];
        BitHelpers.putIntBE( beforeInt, buffer, 0 );
        int afterInt = BitHelpers.readIntBE( buffer, 0 );

        BitHelpers.putLongBE( beforeLong, buffer, 0 );
        long afterLong = BitHelpers.readLongBE( buffer, 0 );
        
        BitHelpers.putFloatBE( beforeFloat, buffer, 0 );
        float afterFloat = BitHelpers.readFloatBE( buffer, 0 );
        
        BitHelpers.putDoubleBE( beforeDouble, buffer, 0 );
        double afterDouble = BitHelpers.readDoubleBE( buffer, 0 );
        
        System.out.println( "   (int) before: "+beforeInt+", after: "+afterInt );
        System.out.println( "  (long) before: "+beforeLong+", after: "+afterLong );
        System.out.println( " (float) before: "+beforeFloat+", after: "+afterFloat );
        System.out.println( "(double) before: "+beforeDouble+", after: "+afterDouble );
        
        assert beforeInt == afterInt;
        assert beforeLong == afterLong;
        assert beforeFloat == afterFloat;
        assert beforeDouble == afterDouble;
    }

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

		int temp = ((buffer[0] << 24) +
		           ((buffer[1] & 255) << 16) +
		           ((buffer[2] & 255) << 8) +
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

		int temp = ((buffer[3] << 24) +
		           ((buffer[2] & 255) << 16) +
		           ((buffer[1] & 255) << 8) +
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

		long temp = (((long)buffer[0] << 56) +
		             ((long)(buffer[1] & 255) << 48) +
		             ((long)(buffer[2] & 255) << 40) +
		             ((long)(buffer[3] & 255) << 32) +
		             ((long)(buffer[4] & 255) << 24) +
		             ((buffer[5] & 255) << 16) +
		             ((buffer[6] & 255) <<  8) +
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

		long temp = (((long)buffer[7] << 56) +
		             ((long)(buffer[6] & 255) << 48) +
		             ((long)(buffer[5] & 255) << 40) +
		             ((long)(buffer[4] & 255) << 32) +
		             ((long)(buffer[3] & 255) << 24) +
		             ((buffer[2] & 255) << 16) +
		             ((buffer[1] & 255) <<  8) +
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
		
		return (short)(((buffer[offset]   & 255) << 8) +
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
		
		return (short)(((buffer[offset+1] & 255) << 8)  +
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
		
		return ((buffer[offset]   & 255) << 24) +
		       ((buffer[offset+1] & 255) << 16) +
		       ((buffer[offset+2] & 255) << 8)  +
		       ((buffer[offset+3] & 255) /*<< 0*/);
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////// Long Methods ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Read and return an int value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static int readIntLE( byte[] buffer, int offset )
	{
		checkUnderflow( 4, buffer, offset );
		
		return ((buffer[offset+3] & 255) << 24) +
		       ((buffer[offset+2] & 255) << 16) +
		       ((buffer[offset+1] & 255) << 8)  +
		       ((buffer[offset]   & 255) /*<< 0*/);
	}

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

		return (((long)buffer[0] << 56) +
		        ((long)(buffer[1] & 255) << 48) +
		        ((long)(buffer[2] & 255) << 40) +
		        ((long)(buffer[3] & 255) << 32) +
		        ((long)(buffer[4] & 255) << 24) +
		        ((buffer[5] & 255) << 16) +
		        ((buffer[6] & 255) <<  8) +
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

		return (((long)buffer[7] << 56) +
		        ((long)(buffer[6] & 255) << 48) +
		        ((long)(buffer[5] & 255) << 40) +
		        ((long)(buffer[4] & 255) << 32) +
		        ((long)(buffer[3] & 255) << 24) +
		        ((buffer[2] & 255) << 16) +
		        ((buffer[1] & 255) <<  8) +
		        ((buffer[0] & 255) <<  0));
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
