/*
 *   Copyright 2009 The Portico Project
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
package org.portico.utils.messaging;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * For each {@link PorticoMessage Request} that is sent, a response is needed. This class is the
 * parent of all response messages. This class can represent either a basic "success" or "error"
 * response via its {@link #isError()} flag. Users can design their own custom response types
 * (the same way they design their own requests) by extending this class. For your convenience,
 * the messaging framework provides a couple of helper extensions:
 * 
 * <ul>
 *   <li><b>{@link ExtendedSuccessMessage}</b>: A "success" message that allows values to be
 *       returned with it (the {@link ResponseMessage} class only allows you to mark as response
 *       as a success or failure).</li>
 *   <li><b>{@link ErrorResponse}</b>: An "error" message that allows additional error information
 *       to be provided with the response. This includes things like an Exception or an error
 *       message.</li>
 * </ul>
 */
public class ResponseMessage implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected boolean isError;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ResponseMessage()
	{
		this.isError = false;
	}

	public ResponseMessage( boolean isError )
	{
		this.isError = isError;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Returns <code>true</code> if this message represents an error response, <code>false</code>
	 * otherwise.
	 */
	public boolean isError()
	{
		return this.isError;
	}
	
	/**
	 * Returns <code>true</code> if this message represents an success response, <code>false</code>
	 * otherwise.
	 */
	public boolean isSuccess()
	{
		return this.isError == false;
	}
	
	/**
	 * If this message is an {@link ExtendedSuccessMessage}, this method will fetch the "result"
	 * object from it and return it (see the javadoc for that class). If this class is not an
	 * instance of {@link ExtendedSuccessMessage}, <code>null</code> will be returned. This is the
	 * case even if this message is a "success" ({@link #isError()} == <code>false</code>) because
	 * by default, a success message doesn't need to have an associated result.
	 */
	public Object getResult()
	{
		if( this instanceof ExtendedSuccessMessage )
		{
			return ((ExtendedSuccessMessage)this).getResult();
		}
		
		// not an ExtendedSuccessMessage, there is no result
		return null;
	}

	/**
	 * If this message is an {@link ExtendedSuccessMessage}, this method will fetch the HashMap
	 * contained inside it and return the value it has at the given key. If this instance isn't
	 * an ExtendedSuccessMessage, or the key doesn't exist in the map, null is returned. 
	 */
	public Object getResult( String key )
	{
		if( this instanceof ExtendedSuccessMessage )
			return ((ExtendedSuccessMessage)this).getReturnValues().get( key );
		
		// not an ExtendedSuccessMessage, there is no result map
		return null;
	}

	/**
	 * If this message is an {@link ExtendedSuccessMessage}, this method will call the
	 * {@link ExtendedSuccessMessage#setResult(Object)} method on it, passing the given value.
	 * If it isn't an ExtendedSuccessMessage, an exception will be thrown.
	 */
	public void setResult( Object result ) throws MessagingException
	{
		if( this instanceof ExtendedSuccessMessage )
			((ExtendedSuccessMessage)this).setResult( result );
		else
			throw new MessagingException( "Can't set result on non-successful response message" );
	}

	/**
	 * If this message is an {@link ExtendedSuccessMessage}, this method will call the
	 * {@link ExtendedSuccessMessage#setResult(String, Object)} method on it, passing the
	 * given key and value. This will associate the value with the key in the map of results
	 * that are contained in the response. If this instance isn't an ExtendedSuccessMessage,
	 * an exception will be thrown.
	 */
	public void setResult( String key, Object value ) throws MessagingException
	{
		if( this instanceof ExtendedSuccessMessage )
			((ExtendedSuccessMessage)this).setResult( key, value );
		else
			throw new MessagingException( "Can't set value on non-successful response message" );
	}
	
	/**
	 * If this response is an {@link ErrorResponse}, this method will extract it and fetch
	 * the cause of the error, casting it to an Exception before returning it (it is stored as a
	 * Throwable). If this isn't an instance of ErrorResponse, null is returned.
	 */
	public Exception getError()
	{
		if( this instanceof ErrorResponse )
		{
			return (Exception)((ErrorResponse)this).getCause();
		}
		else
		{
			return null;
		}
	}

	/**
	 * Returns the simple name of the message class. Override this if you want something else
	 */
	public String toString()
	{
		return getClass().getSimpleName();
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	private void writeObject( ObjectOutputStream oos ) throws IOException
	{
		writeExternal( oos );
	}

	private void readObject( ObjectInputStream ois)  throws IOException, ClassNotFoundException
	{
		readExternal( ois );
	}
	
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		this.isError = input.readBoolean();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		output.writeBoolean( this.isError );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
