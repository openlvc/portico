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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;

/**
 * For each {@link PorticoMessage} that is sent, a response is needed. This class is the
 * parent of all response messages. This class can represent either a basic "success" or "error"
 * response via its {@link #isError()} flag. Users can design their own custom response types
 * (the same way they design their own requests) by extending this class. For your convenience,
 * the messaging framework provides a couple of helper extensions:
 * 
 * <ul>
 *   <li><b>{@link ExtendedSuccessResponse}</b>: A "success" message that allows values to be
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
	 * If this message is an {@link ExtendedSuccessResponse}, this method will fetch the "result"
	 * object from it and return it (see the javadoc for that class). If this class is not an
	 * instance of {@link ExtendedSuccessResponse}, <code>null</code> will be returned. This is the
	 * case even if this message is a "success" ({@link #isError()} == <code>false</code>) because
	 * by default, a success message doesn't need to have an associated result.
	 */
	public Object getResult()
	{
		if( this instanceof ExtendedSuccessResponse )
		{
			return ((ExtendedSuccessResponse)this).getResult();
		}
		
		// not an ExtendedSuccessMessage, there is no result
		return null;
	}

	/**
	 * If this message is an {@link ExtendedSuccessResponse}, this method will fetch the HashMap
	 * contained inside it and return the value it has at the given key. If this instance isn't
	 * an ExtendedSuccessMessage, or the key doesn't exist in the map, null is returned. 
	 */
	public Object getResult( String key )
	{
		if( this instanceof ExtendedSuccessResponse )
			return ((ExtendedSuccessResponse)this).getReturnValues().get( key );
		
		// not an ExtendedSuccessMessage, there is no result map
		return null;
	}

	/**
	 * If this message is an {@link ExtendedSuccessResponse}, this method will call the
	 * {@link ExtendedSuccessResponse#setResult(Object)} method on it, passing the given value.
	 * If it isn't an ExtendedSuccessMessage, an exception will be thrown.
	 */
	public void setResult( Object result ) throws JRTIinternalError
	{
		if( this instanceof ExtendedSuccessResponse )
			((ExtendedSuccessResponse)this).setResult( result );
		else
			throw new JRTIinternalError( "Can't set result on non-successful response message" );
	}

	/**
	 * If this message is an {@link ExtendedSuccessResponse}, this method will call the
	 * {@link ExtendedSuccessResponse#setResult(String, Object)} method on it, passing the
	 * given key and value. This will associate the value with the key in the map of results
	 * that are contained in the response. If this instance isn't an ExtendedSuccessMessage,
	 * an exception will be thrown.
	 */
	public void setResult( String key, Object value ) throws JRTIinternalError
	{
		if( this instanceof ExtendedSuccessResponse )
			((ExtendedSuccessResponse)this).setResult( key, value );
		else
			throw new JRTIinternalError( "Can't set value on non-successful response message" );
	}
	
	/**
	 * If the response message is set and is an ExtendedSuccessMessage, return the "result" field
	 * of it. If there is no response, or it is a different type, return null.
	 */
	public Object getSuccessResult()
	{
		try
		{
			return ((ExtendedSuccessResponse)this).getResult();
		}
		catch( Exception e ) // ClassCastException or NullPointerException
		{
			return null;
		}
	}
	
	/**
	 * If the response message is set and is an ExtendedSuccessMessage, return its results map.
	 * If there is no response, or it is a different type, return null.
	 */
	public HashMap<String,Object> getSuccessResultMap()
	{
		try
		{
			return ((ExtendedSuccessResponse)this).getReturnValues();
		}
		catch( Exception e ) // ClassCastException or NullPointerException
		{
			return null;
		}
	}

	public short getSuccessResultAsShort()
	{
		return (short)((ExtendedSuccessResponse)this).getResult();
	}
	
	public short getSuccessResultAsShort( String key )
	{
		return (short)getSuccessResultMap().get( key );
	}
	
	public int getSuccessResultAsInt()
	{
		return (int)((ExtendedSuccessResponse)this).getResult();
	}
	
	public int getSuccessResultAsInt( String key )
	{
		return (int)getSuccessResultMap().get( key );
	}
	
	public long getSuccessResultsAsLong()
	{
		return (long)((ExtendedSuccessResponse)this).getResult();
	}

	public long getSuccessResultAsLong( String key )
	{
		return (long)getSuccessResultMap().get( key );
	}
	
	public double getSuccessResultAsDouble()
	{
		return (double)((ExtendedSuccessResponse)this).getResult();
	}

	public double getSuccessResultAsDouble( String key )
	{
		return (double)getSuccessResultMap().get( key );
	}
	
	public String getSuccessResultAsString()
	{
		return ""+((ExtendedSuccessResponse)this).getResult();
	}

	public String getSuccessResultAsString( String key )
	{
		return ""+getSuccessResultMap().get( key );
	}

	public <T> T getSuccessResultAs( Class<T> type )
	{
		return getSuccessResultAs( "result", type );
	}
	
	public <T> T getSuccessResultAs( String key, Class<T> type )
	{
		Object o = getSuccessResultMap().get( key );
		if( o == null )
			throw new IllegalArgumentException( "Unknown result key: "+key );
		else
			return type.cast( o );
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
	 * Check that we have an error response and convert it into a JException sub-type for return.
	 */
	public JException getJException()
	{
		if( this instanceof ErrorResponse )
		{
			Throwable cause = ((ErrorResponse)this).getCause();
			if( cause instanceof JException )
				return (JException)cause;
			else
				return new JRTIinternalError( cause );
		}
		else
		{
			throw new RuntimeException( "Response was null - was the message actually processed?" );
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
	public static ResponseMessage success( Object result )
	{
		ResponseMessage response = new ExtendedSuccessResponse();
		response.setResult( result );
		return response;
	}
}
