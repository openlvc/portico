/*
 *   Copyright 2013 The Portico Project
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

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * Message that provides the result of an object name reservation request. Intended for callback.
 */
public class ReserveObjectNameResult extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String objectName;
	private boolean successful;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ReserveObjectNameResult( String objectName, boolean successful )
	{
		super();
		this.objectName = objectName;
		this.successful = successful;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.ReserveObjectNameResult;
	}

	public String getObjectName()
	{
		return this.objectName;
	}
	
	public void setObjectName( String objectName )
	{
		this.objectName = objectName;
	}
	
	public boolean isSuccessful()
	{
		return this.successful;
	}
	
	public void setSuccessful( boolean successful )
	{
		this.successful = successful;
	}
	
	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.objectName = input.readUTF();
		if( this.objectName.equals("null") )
			this.objectName = null;
		
		this.successful = input.readBoolean();
	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		if( this.objectName == null )
			output.writeUTF( "null" );
		else
			output.writeUTF( this.objectName );
		
		output.writeBoolean( this.successful );
	}
}
