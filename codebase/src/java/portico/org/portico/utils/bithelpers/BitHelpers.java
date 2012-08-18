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
        BitHelpers.putInt( beforeInt, buffer, 0 );
        int afterInt = BitHelpers.readInt( buffer, 0 );

        BitHelpers.putLong( beforeLong, buffer, 0 );
        long afterLong = BitHelpers.readLong( buffer, 0 );
        
        BitHelpers.putFloat( beforeFloat, buffer, 0 );
        float afterFloat = BitHelpers.readFloat( buffer, 0 );
        
        BitHelpers.putDouble( beforeDouble, buffer, 0 );
        double afterDouble = BitHelpers.readDouble( buffer, 0 );
        
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
	////////////////////////////////// Floating Point Methods //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Write the given float value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putFloat( float value, byte[] buffer, int offset )
	{
		checkOverflow( 4, buffer, offset );

		int rawbits = Float.floatToIntBits( value );
		buffer[offset] = (byte)(rawbits >>> 24);
		buffer[offset+1] = (byte)(rawbits >>> 16);
		buffer[offset+2] = (byte)(rawbits >>>  8);
		buffer[offset+3] = (byte)(rawbits >>>  0);
	}
	
	/**
	 * Read and return a float value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static float readFloat( byte[] buffer, int offset )
	{
		checkUnderflow( 4, buffer, offset );

		int temp = ((buffer[0] << 24) +
		           ((buffer[1] & 255) << 16) +
		           ((buffer[2] & 255) << 8) +
		           ((buffer[3] & 255) << 0));

		return Float.intBitsToFloat( temp );
	}
	
	/**
	 * Write the given double value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putDouble( double value, byte[] buffer, int offset )
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
	 * Read and return a double value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static double readDouble( byte[] buffer, int offset )
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

	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Integer Methods //////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Write the given int value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putInt( int value, byte[] buffer, int offset )
	{
		checkOverflow( 4, buffer, offset );

		buffer[offset]   = (byte)(value >> 24);
		buffer[offset+1] = (byte)(value >> 16);
		buffer[offset+2] = (byte)(value >> 8);
		buffer[offset+3] = (byte)(value /*>> 0*/);
	}

	/**
	 * Read and return an int value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 4 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static int readInt( byte[] buffer, int offset )
	{
		checkUnderflow( 4, buffer, offset );
		
		return ((buffer[offset]   & 255) << 24) +
		       ((buffer[offset+1] & 255) << 16) +
		       ((buffer[offset+2] & 255) << 8)  +
		       ((buffer[offset+3] & 255) /*<< 0*/);
	}
	
	/**
	 * Write the given long value into the buffer, starting at the offset value.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferOverflowException} will be thrown for the buffer overflow.
	 */
	public static void putLong( long value, byte[] buffer, int offset )
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
	 * Read and return a long value from the given buffer, starting at the given offset.
	 * <p/>
	 * If there are less than 8 bytes from the offset to the length of the array, a
	 * {@link BufferUnderflowException} will be thrown for the buffer underflow.
	 */
	public static long readLong( byte[] buffer, int offset )
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
}
