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
package org.portico2.common.services.object.msg;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message is a notification of a request to register an object instance. The desired name for
 * the instance is supplied. If there is no preference for the name, the objectName field is null
 * and the object name should be assigned by the RTI.
 * <p/>
 * Optional region/attribute association data can be supplied to the message. If provided, the
 * request will tell the RTI to automatically associate the provided attributes with the provided
 * regions for updates. The two collections are "parallel" lists (like those passed to the RTI).
 * That is, the attribute at position "x" in the attribute list relates to the attribute at position
 * "x" in the region list. If no region data is supplied, these collection should be null (and
 * the {@link #usesDDM()} method will return <code>false</code> if this is the case).
 */
public class RegisterObject extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	// Keys against which we register returned information if request is successful
	public static final String KEY_RETURN_HANDLE = "object-handle";
	public static final String KEY_RETURN_NAME   = "object-name";
	public static final String KEY_RETURN_CLASS  = "object-class";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private String objectName;
	private ArrayList<Integer> attributes;
	private ArrayList<Integer> regionTokens;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.RegisterObject;
	}

	public RegisterObject()
	{
		super();
	}
	
	public RegisterObject( int classHandle )
	{
		this();
		this.classHandle = classHandle;
	}
	
	public RegisterObject( int classHandle, String objectName )
	{
		this( classHandle );
		this.objectName = objectName;
	}

	public RegisterObject( int classHandle, int[] attributes, int[] regions )
	{
		this( classHandle );

		this.attributes = new ArrayList<Integer>();
		for( int handle : attributes )
			this.attributes.add( handle );

		this.regionTokens = new ArrayList<Integer>();
		for( int handle : regions )
			this.regionTokens.add( handle );
	}

	public RegisterObject( int classHandle, String objectName, int[] attributes, int[] regions )
	{
		this( classHandle, objectName );

		// make sure we don't have dodgy arrays
		if( attributes == null || regions == null )
			return;

		this.attributes = new ArrayList<Integer>();
		for( int handle : attributes )
			this.attributes.add( handle );

		this.regionTokens = new ArrayList<Integer>();
		for( int handle : regions )
			this.regionTokens.add( handle );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public int getClassHandle()
	{
		return this.classHandle;
	}
	
	public void setClassHandle( int classHandle )
	{
		this.classHandle = classHandle;
	}
	
	public String getObjectName()
	{
		return this.objectName;
	}
	
	public void setObjectName( String objectName )
	{
		this.objectName = objectName;
	}
	
	/**
	 * Gets all the attributes that are to be associated with regions after the object registration.
	 * If this message isn't the result of a DDM call, this will return <code>null</code>.
	 */
	public ArrayList<Integer> getAttributes()
	{
		return this.attributes;
	}

	public void setAttributes( ArrayList<Integer> attributes )
	{
		this.attributes = attributes;
	}
	
	/**
	 * Gets all the region tokens that are to be associated with attributes after the object
	 * registration. If this message isn't the result of a DDM camm, this will return
	 * <code>null</code>
	 */
	public ArrayList<Integer> getRegionTokens()
	{
		return this.regionTokens;
	}

	public void setRegionTokens( ArrayList<Integer> regionTokens )
	{
		this.regionTokens = regionTokens;
	}

	/**
	 * Returns <code>true</code> if this message has associated DDM region information,
	 * <code>false</code> otherwise.
	 */
	public boolean usesDDM()
	{
		return (this.attributes != null && this.regionTokens != null);
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.classHandle = input.readInt();
		this.objectName = input.readUTF();
		if( this.objectName.equals("null") )
			this.objectName = null;
		
		// read the ddm related collections
		// see the writeExternal() method for information on the encoding
		byte signal = input.readByte();
		if( signal == 1 )
		{
			this.attributes = (ArrayList<Integer>)input.readObject();
			this.regionTokens = (ArrayList<Integer>)input.readObject();
		}
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		output.writeInt( this.classHandle );
		
		if( this.objectName == null )
			output.writeUTF( "null" );
		else
			output.writeUTF( this.objectName );
		
		// write the ddm related collections
		// to take care of the fact that the collection CAN be null, we first write a byte
		// to the stream so as to indicate whether there is a collection to follow. If the
		// value is 0, there is no collection to follow. If the value is 1, there is. Also,
		// we only write the collections to the stream if BOTH exist.
		if( this.attributes == null || this.regionTokens == null )
		{
			output.writeByte( 0 );
		}
		else
		{
			output.writeByte( 1 );
			output.writeObject( this.attributes );
			output.writeObject( this.regionTokens );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
