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
package org.portico2.common.services.object.msg;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.portico.lrc.model.OCInstance;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;
import org.portico2.rti.services.object.data.ROCInstance;

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
	private boolean rediscoveryCheck;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	@Deprecated public DiscoverObject( OCInstance instance ){}
	
	/** <b>DO NOT USE</b> This is only provided because the deserialization of Externalizable
	    objects requires that the class have a 0-arg constructor */
	public DiscoverObject(){}
	
	public DiscoverObject( ROCInstance source )
	{
		this.classHandle = source.getRegisteredClassHandle();
		this.objectHandle = source.getHandle();
		this.objectName = source.getName();
	}

	public DiscoverObject( int classHandle, int objectHandle, String objectName )
	{
		// set the basics up
		this.classHandle = classHandle;
		this.objectHandle = objectHandle;
		this.objectName = objectName;
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
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		output.writeInt( this.classHandle );
		output.writeInt( this.objectHandle );
		output.writeUTF( this.objectName );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
