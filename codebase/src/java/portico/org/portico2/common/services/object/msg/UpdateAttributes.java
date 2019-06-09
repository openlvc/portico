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
import java.util.HashMap;

import org.portico.lrc.PorticoConstants;
import org.portico.lrc.model.RegionInstance;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message represents a request to update the values of a group of attributes contained in a
 * specific object instance.
 */
public class UpdateAttributes extends PorticoMessage implements Externalizable, Cloneable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int objectId;
	private byte[] tag;
	private HashMap<Integer,byte[]> attributes;
	
	// these are filled out on the receiver side as required for filtering callbacks
	// they should never be sent over the wire
	private transient HashMap<Integer,FilteredAttribute> filtered;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public UpdateAttributes()
	{
		super();
		this.objectId = PorticoConstants.NULL_HANDLE;
		this.tag = new byte[0];
		this.attributes = new HashMap<Integer,byte[]>();
		this.filtered = new HashMap<Integer,FilteredAttribute>();
	}
	
	public UpdateAttributes( int objectId, byte[] tag, HashMap<Integer,byte[]> attributes )
	{
		this();
		this.objectId = objectId;
		this.tag = tag;
		this.attributes = attributes;
	}
	
	public UpdateAttributes( int objectId,
	                         byte[] tag,
	                         HashMap<Integer,byte[]> attributes,
	                         double time )
	{
		this( objectId, tag, attributes );
		this.timestamp = time;
	}
	
	/**
	 * Copy constructor used by handlers when generating callback messages with a filtered set
	 * of attributes based on subscription data.
	 */
	public UpdateAttributes( UpdateAttributes original )
	{
		this( original.objectId, original.tag, original.attributes, original.timestamp );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.UpdateAttributes;
	}

	public HashMap<Integer,byte[]> getAttributes()
	{
		return attributes;
	}

	public void setAttributes( HashMap<Integer,byte[]> attributes )
	{
		this.attributes = attributes;
	}

	/**
	 * A filtered attribute is one that the local federate is interested in. This information is
	 * only filled out as part of reflection callback processing. It allows more information than
	 * just the handle and value to be linked in. In this case, the region that the local federate
	 * is subscribed to (and that overlaps with the publishers region) can be attached. Note that
	 * the region value can also be <code>null</code>, which in this case is equivalent to the
	 * "default region" that covers all values of all dimensions.
	 */
	public void addFilteredAttribute( int handle, byte[] value, RegionInstance region )
	{
		FilteredAttribute attribute = new FilteredAttribute( value, region );
		this.filtered.put( handle, attribute );
	}
	
	public void clearFilteredAttributes()
	{
		this.filtered.clear();
	}

	public HashMap<Integer,FilteredAttribute> getFilteredAttributes()
	{
		return this.filtered;
	}
	
	public int getObjectId()
	{
		return objectId;
	}

	public void setObjectId( int objectId )
	{
		this.objectId = objectId;
	}

	public byte[] getTag()
	{
		return tag;
	}

	public void setTag( byte[] tag )
	{
		this.tag = tag;
	}

	/**
	 * Returns <code>true</code>
	 */
	public boolean isSpecDefinedMessage()
	{
		return true;
	}

	////////////////////////////////////////////////////////////
	//////////////// Manual Marshalling Methods ////////////////
	////////////////////////////////////////////////////////////
	// DON'T COPY THIS UNLESS YOU KNOW WHAT YOU'RE DOING!!
	@Override
	public boolean supportsManualMarshal()
	{
		return true;
	}

	@Override
	public void marshal( ObjectOutput buffer ) throws IOException
	{
		// write the object ID first (we put this first so we can snoop it easily in MessageHelpers)
		buffer.writeInt( this.objectId );

		// marshal the relevant super-class information first
		buffer.writeInt( sourceFederate );
		//buffer.writeInt( targetFederate ); -- not needed, always -1 (all federates)
		buffer.writeInt( targetFederation );
		//buffer.writeInt( serial );         -- not needed, always -1 (async)
		buffer.writeDouble( timestamp );
		//buffer.writeBoolean( immediate );  -- not needed, always false
		
		// now marshal up our stuff
		buffer.writeUTF( bytesToString(this.tag) ); // conver it for a string for simple read/write

		// write the number of attributes we are pushing out
		buffer.writeInt( attributes.size() );
		// write each of the attribute values
		for( Integer attributeHandle : attributes.keySet() )
		{
			buffer.writeInt( attributeHandle );
			byte[] value = attributes.get( attributeHandle );
			buffer.writeInt( value.length );
			buffer.write( value );
		}

		buffer.flush();
	}

	@Override
	public void unmarshal( ObjectInput buffer ) throws IOException, ClassNotFoundException
	{
		// read the object ID first (we put this first so we can snoop it easily in MessageHelpers)
		//this.objectId = buffer.readInt(); -- this will be done in MessageHelpers
		
		// read the super-class information first
		super.sourceFederate = buffer.readInt();
		//super.targetFederate = buffer.readInt(); -- not needed, always -1 (all federates)
		super.targetFederation = buffer.readInt();
		//super.serial = buffer.readInt();         -- not needed, always -1 (async)
		super.timestamp = buffer.readDouble();
		//super.immediate = buffer.readBoolean();  -- not needed, always false
		
		// now read our stuff
		this.tag = buffer.readUTF().getBytes();
		
		// read the attribute data in, formatted as int,[int,byte[]]...
		// first int is number of attributes, next are pairs of attributeHandle/values
		if( this.attributes == null )
			this.attributes = new HashMap<Integer,byte[]>();

		int attributeCount = buffer.readInt();
		for( int i = 0; i < attributeCount; i++ )
		{
			int attributeHandle = buffer.readInt();
			int valueSize = buffer.readInt();
			byte[] attributeValue = new byte[valueSize];
			buffer.readFully( attributeValue, 0, valueSize );
			this.attributes.put( attributeHandle, attributeValue );
		}
	}

	/**
	 * Perform a proper clone. We need to reset the filtered attributes as they are updated
	 * on the receiver end and so multiple threads will change this field. These are currently
	 * only implemented to serve the JVM comms binding.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		UpdateAttributes clone = (UpdateAttributes)super.clone();
		//clone.attributes = new HashMap<Integer,byte[]>( this.attributes ); --read-only on incoming
		clone.filtered = new HashMap<Integer,FilteredAttribute>();
		return clone;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Inner Class: Attribute ////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public class FilteredAttribute
	{
		public byte[] value;
		public RegionInstance region;
		public FilteredAttribute( byte[] value, RegionInstance region )
		{
			this.value = value;
			this.region = region;
		}
	}

}
