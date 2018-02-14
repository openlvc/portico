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

import org.portico.lrc.model.RegionInstance;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class ModifyRegion extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private RegionInstance region;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ModifyRegion()
	{
		super();
	}
	
	public ModifyRegion( RegionInstance region )
	{
		this();
		this.region = region;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.ModifyRegion;
	}

	public RegionInstance getRegion()
    {
    	return this.region;
    }

	public void setRegion( RegionInstance region )
    {
		this.region = region;
    }

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.region = (RegionInstance)input.readObject();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		output.writeObject( this.region );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
