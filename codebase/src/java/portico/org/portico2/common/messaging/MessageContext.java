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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;

import org.portico.lrc.compat.JException;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.utils.messaging.PorticoMessage;

/**
 * A MessageContext aggregates together a {@link PorticoMessage} and a {@link ResponseMessage} into
 * a single logical unit that can be passed around the various messaging framework components.
 */
public class MessageContext implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private PorticoMessage  request;
	private ResponseMessage response;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public MessageContext( PorticoMessage request )
	{
		this.request = request;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////
	///////////////// Request Handling Methods /////////////////
	////////////////////////////////////////////////////////////
	public PorticoMessage getRequest()
	{
		return this.request;
	}
	
	public void setRequest( PorticoMessage request )
	{
		this.request = request;
	}
	
	/**
	 * This method will cast the request method to the given type and return it. If there is no
	 * request, or the request isn't assignable to the given type, an exception will be thrown.
	 * At the same time, an {@link ErrorResponse} will be created and packaged into the context.
	 */
	public <X extends PorticoMessage> X getRequest( Class<X> type ) throws JException
	{
		if( type.isInstance(this.request) == false )
		{
			// NOT the type we are after, pack an exception and throw it
			String msg = "Invalid message type [" + request.getClass().getName() +
							"], expecting [" + type.getName() + "]";
			JRTIinternalError err = new JRTIinternalError( msg );
			error( err );
			throw err;
		}

		return type.cast( this.request );
	}
	
	/**
	 * The functionality of this class is virtually the same as {@link #getRequest(Class)
	 * getRequest(Class<X>)} with the only difference being that the class of the given handler
	 * will be used in the error message.
	 */
	public <X extends PorticoMessage> X getRequest( Class<X> type, IMessageHandler handler )
		throws JRTIinternalError
	{
		if( type.isInstance(this.request) == false )
		{
			// NOT the type we want, create message and pack exception
			String msg = "Invalid message type [" + request.getClass().getName() + "] in [" +
						  handler.getClass().getName() + "], expecting (" + type.getName() + ")";
			JRTIinternalError err = new JRTIinternalError( msg );
			error( err );
			throw err;
		}

		return type.cast( this.request );
	}
	
	public MessageType getRequestType()
	{
		return this.request.getType();
	}

	////////////////////////////////////////////////////////////
	///////////////// Response Handling Methods ////////////////
	////////////////////////////////////////////////////////////
	public ResponseMessage getResponse()
	{
		return this.response;
	}
	
	/**
	 * Fetches the response as an {@link ErrorResponse}. If the response message is null, or is
	 * not an {@link ErrorResponse}, null will be returned.
	 */
	public ErrorResponse getErrorResponse()
	{
		if( response instanceof ErrorResponse )
		{
			return (ErrorResponse)response;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * If the contained response is an {@link ErrorResponse}, this method will extract it and fetch
	 * the cause of the error, casting it to an Exception before returning it (it is stored as a
	 * Throwable). If the response doesn't exist, or isn't an instance of ErrorResponse, null is
	 * returned.
	 */
	public JException getErrorResponseException()
	{
		if( response instanceof ErrorResponse )
		{
			Throwable throwable = ((ErrorResponse)response).getCause();
			if( throwable instanceof JException )
				return (JException)throwable;
			else
				return new JRTIinternalError( throwable );
		}
		else
		{
			throw new RuntimeException( "Response was null - was the message actually processed?" );
		}
	}
	
	/**
	 * If the response message is set and is an ExtendedSuccessMessage, return the "result" field
	 * of it. If there is no response, or it is a different type, return null.
	 */
	public Object getSuccessResult()
	{
		try
		{
			return ((ExtendedSuccessResponse)response).getResult();
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
			return ((ExtendedSuccessResponse)response).getReturnValues();
		}
		catch( Exception e ) // ClassCastException or NullPointerException
		{
			return null;
		}
	}
	
	public void setResponse( ResponseMessage response )
	{
		this.response = response;
	}
	
	/**
	 * Returns <code>true</code> if the response message exists and is a success message.
	 * Returns <code>false</code> if the response message is null or is an error response.
	 */
	public boolean isSuccessResponse()
	{
		if( response != null )
			return response.isSuccess();
		else
			return false;
	}
	
	/**
	 * Returns <code>true</code> if the response message exists and is an error message.
	 * Returns <code>true</code> if the response message is null.
	 * Returns <code>false</code> if the response message is a success response.
	 */
	public boolean isErrorResponse()
	{
		if( response != null )
			return response.isError();
		else
			return true;
	}

	/**
	 * Returns <code>true</code> if a response has been filled out for this context (that is, if
	 * the response is not null), <code>false</code> if a response doesn't exist.
	 */
	public boolean hasResponse()
	{
		return this.response != null;
	}

	////////////////////////////////////////////////////////////
	////////////////////// Helper Methods //////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Returns a copy of this context that uses the original request object, but not the response.
	 * If you intend to change the request message AFTER you have made a copy, be aware that it
	 * will change in the copy as well (as the reference to the request is just copied).
	 */
	public MessageContext copy()
	{
		return new MessageContext( this.request );
	}

	/**
	 * Replaces the current request with the given argument and removes any existing response.
	 */
	public void clear( PorticoMessage newRequest )
	{
		this.request = newRequest;
		this.response = null;
	}

	/**
	 * Creates a success {@link ResponseMessage} and packs it into the context. 
	 */
	public void success()
	{
		this.response = new ResponseMessage();
	}

	/**
	 * Puts a {@link ExtendedSuccessResponse} into the context and adds the given key/value to it.
	 * If there is already such a response, it just adds the key/value to that. In this way you
	 * can build up the map of values one call at a time.
	 * 
	 * @param key The key to identify the specific part of the response
	 * @param value The value to return
	 */
	public final void success( String key, Object value )
	{
		if( this.response == null ||
		   (this.response instanceof ExtendedSuccessResponse) == false )
		{
			this.response = new ExtendedSuccessResponse();
		}
		
		this.response.setResult( key, value );
	}
	
	/**
	 * Puts a {@link ExtendedSuccessResponse} into the context, including the given return values.
	 */
	public void success( HashMap<String,Object> returnValues )
	{
		this.response = new ExtendedSuccessResponse( returnValues );
	}
	
	/**
	 * Puts a {@link ExtendedSuccessResponse} into the context. The given object is set as the
	 * {@link ExtendedSuccessResponse#setResult(Object) result object}.
	 */
	public void success( Object result )
	{
		ExtendedSuccessResponse message = new ExtendedSuccessResponse();
		message.setResult( result );
		this.response = message;
	}

	/**
	 * Make the result an {@link ExtendedSuccessResponse} and put the given key/value pair
	 * into the map. You can call this successively to add more to the map.
	 * 
	 * @param key The key to store a result value against
	 * @param value The result value to store against the given key
	 * @see {@link #success(String, Object)}.
	 */
	public final void putSuccessResult( String key, Object value )
	{
		success( key, value );
	}
	
	/**
	 * Creates an {@link ErrorResponse} and sets it as the response object.
	 */
	public void error( String message )
	{
		this.response = new ErrorResponse( message );
	}
	
	/**
	 * Creates an {@link ErrorResponse} and sets it as the response object.
	 */
	public void error( Throwable cause )
	{
		this.response = new ErrorResponse( cause );
	}

	/**
	 * Returns "context: request=request.toString(), response=response.toString()"
	 */
	public String toString()
	{
		return "context: request=" + request + ", response=" + response;
	}

	public int getSuccessResultAsInt()
	{
		return (int)((ExtendedSuccessResponse)response).getResult();
	}
	
	public int getSuccessResultAsInt( String key )
	{
		return (int)getSuccessResultMap().get( key );
	}
	
	public long getSuccessResultsAsLong()
	{
		return (long)((ExtendedSuccessResponse)response).getResult();
	}

	public long getSuccessResultAsLong( String key )
	{
		return (long)getSuccessResultMap().get( key );
	}
	
	public double getSuccessResultAsDouble()
	{
		return (double)((ExtendedSuccessResponse)response).getResult();
	}

	public double getSuccessResultAsDouble( String key )
	{
		return (double)getSuccessResultMap().get( key );
	}
	
	public String getSuccessResultAsString()
	{
		return ""+((ExtendedSuccessResponse)response).getResult();
	}

	public String getSuccessResultAsString( String key )
	{
		return ""+getSuccessResultMap().get( key );
	}

	////////////////////////////////////////////////////////////
	////////////////// Serialization Methods ///////////////////
	////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		request = (PorticoMessage)input.readObject();
		response = (ResponseMessage)input.readObject();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		output.writeObject( request );
		output.writeObject( response );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
