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
package org.portico.lrc.services.ddm.msg;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class CreateRegion extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int spaceHandle;
	private int extentCount;
	
	// set only when region is created as a result of this request, this is here because the
	// message is broadcast out to other federates to let them know the details of the new region
	private int regionToken;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public CreateRegion()
	{
		super();
	}
	
	public CreateRegion( int spaceHandle, int extentCount )
	{
		this();
		this.spaceHandle = spaceHandle;
		this.extentCount = extentCount;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.CreateRegion;
	}

	public int getSpaceHandle()
    {
    	return spaceHandle;
    }

	public void setSpaceHandle( int spaceHandle )
    {
    	this.spaceHandle = spaceHandle;
    }

	public int getExtentCount()
    {
    	return extentCount;
    }

	public void setExtentCount( int extentCount )
    {
    	this.extentCount = extentCount;
    }

	public int getRegionToken()
	{
		return this.regionToken;
	}
	
	public void setRegionToken( int regionToken )
	{
		this.regionToken = regionToken;
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.spaceHandle = input.readInt();
		this.extentCount = input.readInt();
		this.regionToken = input.readInt();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		output.writeInt( this.spaceHandle );
		output.writeInt( this.extentCount );
		output.writeInt( this.regionToken );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
