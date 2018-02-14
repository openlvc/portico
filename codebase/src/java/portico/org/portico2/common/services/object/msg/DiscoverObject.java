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
import java.util.Map;
import java.util.Set;

import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.RegionInstance;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class DiscoverObject extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int classHandle;
	private String objectName;
	private int objectHandle;
	private int[] ownedAttributes;
	private int[][] regionTokens;
	private boolean rediscoveryCheck;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/** <b>DO NOT USE</b> This is only provided because the deserialization of Externalizable
	    objects requires that the class have a 0-arg constructor */
	public DiscoverObject(){}
	
	/**
	 * Create a discover object message for the given object instance. This will find all the
	 * attributes owned by the owner of the instance. The owner is defined as the federate that
	 * owns the privilegeToDelete attribute, so for this to function properly, this message
	 * should not be constructed after the object has been created and before any ownership
	 * transfer for that attribute has taken place.
	 */
	public DiscoverObject( OCInstance source )
	{
		// set the basics up
		this.classHandle = source.getRegisteredType().getHandle();
		this.objectHandle = source.getHandle();
		this.objectName = source.getName();
		
		// initialize the "owned attributes" array
		Set<ACInstance> owned = source.getAllOwnedAttributes( source.getOwner() );
		this.ownedAttributes = new int[owned.size()];
		int index = 0;
		for( ACInstance attribute : owned )
		{
			this.ownedAttributes[index++] = attribute.getHandle();
		}
		
		// initialize the region token array
		Map<ACInstance,RegionInstance> associated = source.getAllRegionAssociatedAttributes();
		this.regionTokens = new int[associated.size()][2];
		index = 0;
		for( ACInstance attribute : associated.keySet() )
		{
			regionTokens[index][0] = attribute.getHandle();
			regionTokens[index][1] = associated.get(attribute).getToken();
			++index;
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.DiscoverObject;
	}
	
	public int getClassHandle()
    {
    	return classHandle;
    }

	public void setClassHandle( int classHandle )
    {
    	this.classHandle = classHandle;
    }

	public String getObjectName()
    {
    	return objectName;
    }

	public void setObjectName( String objectName )
    {
    	this.objectName = objectName;
    }

	public int getObjectHandle()
    {
    	return objectHandle;
    }

	public void setObjectHandle( int objectHandle )
    {
    	this.objectHandle = objectHandle;
    }

	public int[] getOwnedAttributes()
    {
    	return ownedAttributes;
    }

	public void setOwnedAttributes( int[] ownedAttributes )
    {
    	this.ownedAttributes = ownedAttributes;
    }

	public int[][] getRegionTokens()
	{
		return this.regionTokens;
	}
	
	public void setRegionTokens( int[][] regionTokens )
	{
		this.regionTokens = regionTokens;
	}
	
	public boolean isRediscoveryCheck()
	{
		return this.rediscoveryCheck;
	}
	
	public void setRediscoveryCheck( boolean check )
	{
		this.rediscoveryCheck = check;
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.classHandle = input.readInt();
		this.objectHandle = input.readInt();
		this.objectName = input.readUTF();
		this.ownedAttributes = (int[])input.readObject();
		this.regionTokens = (int[][])input.readObject();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		output.writeInt( this.classHandle );
		output.writeInt( this.objectHandle );
		output.writeUTF( this.objectName );
		output.writeObject( this.ownedAttributes );
		output.writeObject( this.regionTokens );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
