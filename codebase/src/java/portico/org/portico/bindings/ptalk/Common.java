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
package org.portico.bindings.ptalk;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class exists to provide a central location for all the common PTalk configuration and
 * runtime properties.
 */
public class Common
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The default ITransport implementation to use */
	public static final String DEFAULT_TRANSPORT =
		"org.portico.bindings.ptalk.transport.UdpTransport";
	
	/** The default protocol stack to use */
	public static final String DEFAULT_STACK = "GM";

	/** The default number of processing threads for incoming or outgoing workers */
	public static final int DEFAULT_WORKER_COUNT = 2;
	
	/** The default time in millis to wait for a response to a synchronous message */
	public static final long DEFAULT_SYNC_TIMEOUT = 1000;

	//////////////////////////////////////////////////////////////////
	// Property settings for configurable components of the binding //
	//////////////////////////////////////////////////////////////////
	/** Fully qualified name of the ITransport implementation to use */
	public static final String PROP_TRANSPORT = "portico.ptalk.transport";
	
	
	/** List of protocols that will form the stack. The order they are specified in is the order
	    they will appear in Pipeline. Each Protocol should be marked with the {@link Protocol}
	    annotation and be on the classpath so Portico can find it.
	    <p/>
	    The format of the string should be a comma separated list of the protocol names (as
	    defined in the name property of the {@link Protocol} annotation).
	 */
	public static final String PROP_STACK = "portico.ptalk.stack";
	
	/** Property to define the number of threads used for processing incoming messages */
	public static final String PROP_INCOMING_WORKERS = "portico.ptalk.incomingWorkers";
	
	/** Property to define the number of threads used for processing outgoing messages */
	public static final String PROP_OUTGOING_WORKERS = "portico.ptalk.outgoingWorkers";
	
	/** Property to define timeout period used when waiting for response to synchronous messages */
	public static final String PROP_SYNC_TIMEOUT = "portico.ptalk.syncResponseTimeout";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Common()
	{
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static Logger getLogger()
	{
		return LogManager.getLogger( "portico.lrc.ptalk" );
	}
	
	////////////////////////////////////////////////////////////
	////////////////////// Helper Methods //////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * This method takes the given serializable arguments, serializes them into a byte[] and
	 * passes it back to the caller.
	 */
	public static final byte[] serialize( Serializable... objects )
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			for( Serializable temp : objects )
				oos.writeObject( temp );
			
			oos.close();
			return baos.toByteArray();
		}
		catch( IOException ioex )
		{
			throw new RuntimeException( ioex );
		}
	}
	
	/**
	 * This method takes the given buffer and extracts <code>count</code> number of objects
	 * from it. These objects are returned in an array.
	 */
	public static final Object[] deserialize( byte[] buffer, int count )
	{
		try
		{
    		Object[] objects = new Object[count];
    		ByteArrayInputStream bais = new ByteArrayInputStream( buffer );
    		ObjectInputStream ois = new ObjectInputStream( bais );
    		
    		for( int i = 0; i < count; i++ )
    			objects[i] = ois.readObject();
    		
    		ois.close();
    		return objects;
		}
		catch( Exception ex )
		{
			throw new RuntimeException( ex );
		}
	}
	
	/**
	 * Convert the value in the provided buffer to an int and return it. If the array isn't big
	 * enough, a RuntimeException will be thrown.
	 */
	public static final int byteArrayToInteger( byte[] buffer ) throws RuntimeException
	{
		return Common.byteArrayToInteger( buffer, 0 );
	}

	/**
	 * Convert the data in the given buffer (starting at the offset) to an int and return it. If
	 * there isn't enough data to form an int (4-bytes) a RuntimeException will be thrown.
	 */
	public static final int byteArrayToInteger( byte[] buffer, int offset ) throws RuntimeException
	{
		if( (buffer.length-offset) < 4 )
		{
			throw new RuntimeException( "Not enough data in buffer to extract int. Expected at "+
			                            "least 4-bytes, found "+(buffer.length-offset) );
		}
		
		return (buffer[offset] << 24) +
              ((buffer[offset+1] & 0xFF) << 16) +
              ((buffer[offset+2] & 0xFF) << 8)  +
               (buffer[offset+3] & 0xFF);
	}
	
	/**
	 * Take the given int value and convert it into a byte array suitable for sending over the
	 * network. 
	 */
	public static final byte[] integerToByteArray( int value )
	{
		return new byte[] { (byte)(value >>> 24),
		                    (byte)(value >>> 16),
		                    (byte)(value >>> 8),
		                    (byte)(value) };
	}
	
	/**
	 * Take the given int value and convert it into a byte array suitable for sending over the
	 * network. This data is written into the provided buffer. If there is not enough room in
	 * the buffer, a RuntimeException will be thrown.
	 * 
	 * @return The number of bytes written
	 */
	public static final int integerToByteArray( int value, byte[] buffer, int offset )
		throws RuntimeException
	{
		if( (buffer.length-offset) < 4 )
		{
			throw new RuntimeException( "Not enough space in buffer to write int. Expected at "+
			                            "least 4-bytes, found "+(buffer.length-offset) );
		}
		
		buffer[offset]   = (byte)(value >>> 24);
		buffer[offset+1] = (byte)(value >>> 16);
		buffer[offset+2] = (byte)(value >>> 8);
		buffer[offset+3] = (byte)(value);
		
		return 4;
	}
	
	/**
	 * Take the given long value and convert it into a byte array suitable for sending over the
	 * network. This data is written into the provided buffer. If there is not enough room in
	 * the buffer, a RuntimeException will be thrown.
	 * 
	 * @return The number of bytes written
	 */
	public static final long longToByteArray( long value, byte[] buffer, int offset )
	{
		if( (buffer.length-offset) < 8 )
		{
			throw new RuntimeException( "Not enough space in buffer to write long. Expected at "+
			                            "least 8-bytes, found "+(buffer.length-offset) );
		}
		
		buffer[offset]   = (byte)((value >> 56) & 0xff );
		buffer[offset+1] = (byte)((value >> 48) & 0xff );
		buffer[offset+2] = (byte)((value >> 40) & 0xff );
		buffer[offset+3] = (byte)((value >> 32) & 0xff );
		buffer[offset+4] = (byte)((value >> 24) & 0xff );
		buffer[offset+5] = (byte)((value >> 16) & 0xff );
		buffer[offset+6] = (byte)((value >>  8) & 0xff );
		buffer[offset+7] = (byte)((value >>  0) & 0xff );

		return 8;
	}

	/**
	 * Convert the data in the given buffer (starting at the offset) to an long and return it. If
	 * there isn't enough data to form an long (8-bytes) a RuntimeException will be thrown.
	 */
	public static final long byteArrayToLong( byte[] buffer, int offset ) throws RuntimeException
	{
		if( (buffer.length-offset) < 8 )
		{
			throw new RuntimeException( "Not enough data in buffer to extract long. Expected at "+
			                            "least 8-bytes, found "+(buffer.length-offset) );
		}
		
		return (long)( (long)(0xff & buffer[offset]) << 56   |
		               (long)(0xff & buffer[offset+1]) << 48 |
		               (long)(0xff & buffer[offset+2]) << 40 |
		               (long)(0xff & buffer[offset+3]) << 32 |
		               (long)(0xff & buffer[offset+4]) << 24 |
		               (long)(0xff & buffer[offset+5]) << 16 |
		               (long)(0xff & buffer[offset+6]) << 8  |
		               (long)(0xff & buffer[offset+7]) << 0 );
	}
}
