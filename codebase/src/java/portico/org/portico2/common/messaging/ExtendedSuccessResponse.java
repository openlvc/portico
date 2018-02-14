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

/**
 * This class represents a success response message and can contain any number of return values
 * that should be passed back to the initiator of the request. This class also contains the notion
 * of the "result" object, which is basically just a single, pre-named object that is used to
 * identify the main or only object being returned. It is basically just a shortcut to avoid
 * handlers from having to know ahead of time what the key used to identify the single result being
 * returned is (a single return object is the most common case).
 */
public class ExtendedSuccessResponse extends ResponseMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private HashMap<String,Object> returnValues;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ExtendedSuccessResponse()
	{
		super( false );
		this.returnValues = new HashMap<String,Object>();
	}
	
	public ExtendedSuccessResponse( HashMap<String,Object> returnValues )
	{
		super( false );
		this.returnValues = returnValues;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns a direct reference to the result HashMap containing the results.
	 */
	public HashMap<String,Object> getReturnValues()
	{
		return this.returnValues;
	}
	
	/**
	 * This method will <b>REPLACE</b> the existing set of return values with the given set. If the
	 * new set is <code>null</code>, the request will be <b>ignored</b>.
	 */
	public void setReturnValues( HashMap<String,Object> newValues )
	{
		if( newValues != null )
			this.returnValues = newValues;
	}
	
	/**
	 * In each {@link ExtendedSuccessResponse} there is the notion of the "result" object. This is
	 * essentially an object bound to the key "result" in the return values. This method will set
	 * the given object as the result object (even if it is null), replacing the existing object
	 * if there is one.
	 */
	public void setResult( Object resultObject )
	{
		this.returnValues.put( "result", resultObject );
	}
	
	/**
	 * Put the given value into the results map at the given key.
	 */
	public void setResult( String key, Object value )
	{
		this.returnValues.put( key, value );
	}
	
	/**
	 * In each {@link ExtendedSuccessResponse} there is the notion of the "result" object. This is
	 * essentially an object bound to the key "result" in the return values. This method will fetch
	 * that object and return it (or null if there isn't an explicit result).
	 */
	public Object getResult()
	{
		if( this.returnValues != null )
			return this.returnValues.get( "result" );
		else
			return null;
	}
	
	/**
	 * In each {@link ExtendedSuccessResponse} there is the notion of the "result" object. This is
	 * essentially an object bound to the key "result" in the return values. This method will fetch
	 * that object and return it (or null if there isn't an explicit result). This method lets you
	 * specify the expected type of the result object. If it exists, this method will cast it
	 * before returning. If the result object is of the wrong type, null will be returned.
	 */
	public <X> X getResult( Class<X> expectedClass )
	{
		Object result = getResult();
		
		// check its type, and if it is the right type, cast and return it
		if( expectedClass.isAssignableFrom(result.getClass()) )
			return expectedClass.cast( result );
		else
			return null;
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		this.isError = false;
		// no need to call super.readExternal(input), because in this case it doesn't do
		// anything as this is a "success" message
		returnValues = (HashMap<String,Object>)input.readObject();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		// no need to call super.writeExternal(output), because in this case it doesn't do
		// anything as this is a "success" message
		output.writeObject( returnValues );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
