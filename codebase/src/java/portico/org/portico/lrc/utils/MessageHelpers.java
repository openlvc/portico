/*
 *   Copyright 2008 The Portico Project
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
package org.portico.lrc.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.portico.lrc.LRC;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.common.services.object.msg.UpdateAttributes;

public class MessageHelpers
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

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Array Manipulation Methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a new byte[] that is equal to the old byte[] only without the first given x bytes
	 * Currently, this is implemented using an array *copy*. Obviously this isn't ideal in terms
	 * of efficiency and should be rewritten to just mask out the header at some point in the
	 * future (if this is even possible with Java).
	 */
	public static byte[] stripHeader( byte[] data, int amountToRemove )
	{
		int size = data.length - amountToRemove;
		byte[] buffer = new byte[size];
		
		System.arraycopy( data, amountToRemove, buffer, 0, size );
		return buffer;
	}

	/**
	 * Convert the provided int into a byte[] for transmission over the network.
	 */
	public static byte[] intToByteArray( int value )
	{
		return new byte[]{ (byte)(value >>> 24),
		                   (byte)(value >>> 16),
		                   (byte)(value >>> 8),
		                   (byte)value };
	}
	
	/**
	 * Convert the provided into into a byte[] and write the values into the given buffer starting
	 * at the provided offset (this will take up 4 bytes!)
	 */
	public static void intToByteArray( int value, byte[] buffer, int offset )
	{
		buffer[offset]   = (byte)(value >>> 24);
		buffer[offset+1] = (byte)(value >>> 16);
		buffer[offset+2] = (byte)(value >>> 8 );
		buffer[offset+3] = (byte)(value);
	}

	/**
	 * Turn the given byte[] into an int after it was received from the network. If the length
	 * of the byte[] is less than 4 bytes, an exception will be thrown
	 */
	public static int byteArrayToInt( byte[] array )
	{
		return (array[0] << 24) +
		       ((array[1] & 0xFF) << 16) +
		       ((array[2] & 0xFF) << 8) +
		       (array[3] & 0xFF);
	}

	/**
	 * Read 4 bytes from the provided array (starting at the given offset) and return them as a
	 * single int. This should just be used to reverse the intToByteArray(...) methods in this class
	 */
	public static int byteArrayToInt( byte[] array, int offset )
	{
		return (array[offset] << 24) +
		       ((array[offset+1] & 0xFF) << 16) +
		       ((array[offset+2] & 0xFF) << 8) +
		       (array[offset+3] & 0xFF);
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Message Inflation/Deflation Methods ///////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will take the given object and turn it into a <code>byte[]</code>
	 * that can be sent across a network connection, stored on some medium or used in whatever way
	 * an application wants to use it. To turn this bit-blob back into something useful, the
	 * {@link #inflate(byte[],Class)} method can be used.
	 * <p/>
	 * <b>Note:</b> If the message supports manual marshaling (where the reflection-based
	 * serialization is ignored, instead providing total control to the message class), then
	 * that process will be used in preference (resulting in
	 * {@link PorticoMessage#marshal(java.io.ObjectOutput)} being called). This should not be
	 * used unless you know what you are doing and have added the message id to the hardcoded
	 * private method {@link #manuallyUnmarshal(ObjectInputStream, LRC)}).
	 */
	public static byte[] deflate( PorticoMessage message )
	{
		// create the output stream with the given size (or resizable if -1 is provided)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		// do the deflation
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			
			// write the header:
			// 
			// [message_type, federation, source, target]
			oos.writeShort( message.getType().getId() );
			oos.writeInt( message.getTargetFederation() );
			oos.writeInt( message.getSourceFederate() );
			oos.writeInt( message.getTargetFederate() );

			// write the message
			if( message.supportsManualMarshal() )
			{
				// Use the manual marshalling method. First, write the signal that we are doing
				// do, then write the id of the message we are serializing (hashcode of its name),
				// then let the message write itself
				oos.writeBoolean( true );
				oos.writeInt( message.getClass().getSimpleName().hashCode() );
				message.marshal( oos );
			}
			else
			{
				// use the simple way of doing things, just write the message itself to the stream
				oos.writeBoolean( false );
				oos.writeObject( message );
			}
			
			return baos.toByteArray();
		}
		catch( IOException ioex )
		{
			throw new RuntimeException( "couldn't convert message ["+
			                            message.getClass()+"] into byte[]", ioex );
		}
	}

	/**
	 * This method will serialize a generic object into a byte[] for sending over the network.
	 * A standard ObjectOutputStream will be use for this serialization, however we will write
	 * the structure in a format compatible with the various {@link #inflate(byte[], Class)}
	 * methods of this class.
	 * 
	 * @param message The message object to deflate
	 * @return A byte[] representation of the object
	 */
	public static byte[] deflate( Object message )
	{
		// create the output stream with the given size (or resizable if -1 is provided)
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		// do the deflation
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeBoolean( false );
			oos.writeObject( message );
			
			return baos.toByteArray();
		}
		catch( IOException ioex )
		{
			throw new RuntimeException( "couldn't convert object ["+
			                            message.getClass()+"] into byte[]", ioex );
		}
	}
	
	/**
	 * This method will take the given data and convert it into a Java object. After doing so,
	 * it will attempt to cast the object to the given type before returning it.
	 * <p/>
	 * <b>Note:</b> If the message supports manual unmarshaling (where the reflection-based
	 * deserialization is ignored, instead providing total control to the message class), then
	 * that process will be used in preference (resulting in
	 * {@link PorticoMessage#unmarshal(java.io.ObjectInput)} being called). This should not be
	 * used unless you know what you are doing and have added the message id to the hardcoded
	 * private method {@link #manuallyUnmarshal(ObjectInputStream, LRC)}).
	 */
	public static <T> T inflate( byte[] data, Class<T> expectedType )
	{
		return inflate( data, expectedType, null );
	}
	
	/**
	 * This method will take the given data and convert it into a Java object. After doing so,
	 * it will attempt to cast the object to the given type before returning it. It also accepts
	 * a filter that will be passed on to the message if it uses manual marshalling. That filter
	 * can be used by the message to short-circuit potentially expensive inflation if it isn't
	 * needed (however the filter determines that).
	 * <p/>
	 * <b>Note:</b> If the message supports manual unmarshaling (where the reflection-based
	 * deserialization is ignored, instead providing total control to the message class), then
	 * that process will be used in preference (resulting in
	 * {@link PorticoMessage#unmarshal(java.io.ObjectInput)} being called). This should not be
	 * used unless you know what you are doing and have added the message id to the hardcoded
	 * private method {@link #manuallyUnmarshal(ObjectInputStream, LRC)}).
	 */
	public static <T> T inflate( byte[] data, Class<T> expectedType, LRC lrc )
	{
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream( data );
			ObjectInputStream ois = new ObjectInputStream( bais );
			// find out whether of not manual marshaling was used
			boolean manuallyMarshaled = ois.readBoolean();
			if( manuallyMarshaled )
			{
				// create a new message from the specified id and let it unmarshal itself
				PorticoMessage message = manuallyUnmarshal( ois, lrc );
				return expectedType.cast( message ); // this is null safe
				
				//PorticoMessage message = newMessageForId( ois.readInt() );
				//message.unmarshal( ois );
				//return expectedType.cast( message );
			}
			else
			{
				// phew, a sane person wrote this! use the default unmarhal
				Object theObject = ois.readObject();
				return expectedType.cast( theObject );
			}
		}
		catch( Exception e )
		{
			throw new RuntimeException( "couldn't convert byte[] into "+expectedType.getSimpleName(), e );
		}		
	}
	
	/**
	 * This method will create the appropriate Portico message type based on data from the provided
	 * input stream. It uses the {@link LRC} to determine whether or not it should bother inflating
	 * the message at all. For example, for UpdateAttributes message, it won't bother if the object
	 * the update relates to hasn't been discovered by the local federate (if we were interested,
	 * we would have discovered it).
	 */
	private static PorticoMessage manuallyUnmarshal( ObjectInputStream ois, LRC lrc ) throws Exception
	{
		int messageType = ois.readInt();
		if( messageType == -1720820960 ) // "UpdateAttributes".hashCode()
		{
			int objectId = ois.readInt();
			
			UpdateAttributes update = new UpdateAttributes();
			update.setObjectId( objectId );
			update.unmarshal( ois );
			return update;
		}
		else if( messageType == -1918248630 ) // "SendInteraction".hashCode()
		{
			int interactionId = ois.readInt();
			
			SendInteraction interaction = new SendInteraction();
			interaction.setInteractionId( interactionId );
			interaction.unmarshal( ois );
			return interaction;
		}
		else
		{
			throw new RuntimeException( "Unknown manually marshaled message: class id="+messageType );
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////// Message Compression/Decompression Methods ////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////// Message Encryption/Decryption Methods //////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////

}
