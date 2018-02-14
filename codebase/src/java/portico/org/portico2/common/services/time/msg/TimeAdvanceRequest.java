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
package org.portico2.common.services.time.msg;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.portico.lrc.PorticoConstants;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

public class TimeAdvanceRequest extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private double time;
	private boolean tara;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public TimeAdvanceRequest()
	{
		makeDummyRequest();
	}

	public TimeAdvanceRequest( double time )
	{
		setTime( time );
	}

	public TimeAdvanceRequest( double time, boolean tara )
	{
		setTime( time );
		this.tara = tara;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.TimeAdvanceRequest;
	}

	public double getTime()
	{
		return this.time;
	}
	
	public void setTime( double time )
	{
		// DO NOT SET TIMESTAMP!! THIS IS NOT A TSO MESSAGE!!
		this.time = time;
	}

	public boolean isTara()
	{
		return this.tara;
	}
	
	public void setTara( boolean tara )
	{
		this.tara = tara;
	}

	public void makeDummyRequest()
	{
		this.setTime( PorticoConstants.NULL_TIME );
	}
	
	public boolean isDummyRequest()
	{
		return this.time == PorticoConstants.NULL_TIME;
	}

	public boolean isTimeAdvance()
	{
		// we are not!!! only time advance grants are
		return false;
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.time = input.readDouble();
		this.tara = input.readBoolean();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
	
		output.writeDouble( this.time );
		output.writeBoolean( this.tara );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
