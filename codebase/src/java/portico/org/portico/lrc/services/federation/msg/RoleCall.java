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
package org.portico.lrc.services.federation.msg;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;

import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.time.data.TimeStatus;
import org.portico.utils.messaging.PorticoMessage;

/**
 * For proper distribution, each federate needs to know about each other federate that exists in a
 * federation. When joining, each federate sends out a join notification so that other federates
 * know its there. However, this will only tell existing federates when a new federate joins. As
 * there is no notice, the joining federate won't know about all the other federates that joined
 * the federation before it. This message is intended to provide a solution to this. When a federate
 * receives a join notification, it should broadcast out an instance of this message containing
 * information about itself, thus informing newly joined federates of its existence. Although this
 * isn't ideal from a network traffic perspective, it is the simplest way to ge the information out
 * there, and the relative infrequency of federates joining a simultion means the excess traffic
 * problem is only a minor (at most) concern.
 */
public class RoleCall extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federateName;
	private TimeStatus timeStatus;
	private OCInstance[] controlledObjects;
	private HashMap<String,byte[]> syncPointTags;
	private HashMap<String,Boolean> syncPointStatus; // label/whether federate has acheived it or not

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/** <b>DO NOT USE</b> This is only provided because the deserialization of Externalizable
	    objects requires that the class have a 0-arg constructor */
	public RoleCall()
	{
		super();
	}

	public RoleCall( int federateHandle,
	                 String federateName, 
	                 TimeStatus status,
	                 OCInstance[] controlledObjects )
	{
		this();
		this.federateName = federateName;
		this.sourceFederate = federateHandle;
		this.timeStatus = status;
		this.controlledObjects = controlledObjects;
		this.syncPointStatus = new HashMap<String,Boolean>();
		this.syncPointTags = new HashMap<String,byte[]>();
		setImmediateProcessingFlag( true );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public String getFederateName()
	{
		return this.federateName;
	}
	
	public void setFederateName( String federateName )
	{
		this.federateName = federateName;
	}
	
	public TimeStatus getTimeStatus()
	{
		return this.timeStatus;
	}
	
	public OCInstance[] getControlledObjects()
	{
		return this.controlledObjects;
	}
	
	/**
	 * This will NEVER return null. If there are no points, it will return an empty map.
	 */
	public HashMap<String,Boolean> getSyncPointStatus()
	{
		if( syncPointStatus == null )
			return new HashMap<String,Boolean>();
		else
			return syncPointStatus;
	}
	
	/**
	 * This will NEVER return null. If there are no points, it will return an empty map.
	 */
	public HashMap<String,byte[]> getSyncPointTags()
	{
		if( syncPointTags == null )
			return new HashMap<String,byte[]>();
		else
			return syncPointTags;	
	}

	@Override
	public boolean isImmediateProcessingRequired()
	{
		return true;
	}
	
	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public void readExternal( ObjectInput input ) throws IOException, ClassNotFoundException
	{
		super.readExternal( input );
		this.federateName = input.readUTF();
		this.timeStatus = (TimeStatus)input.readObject();
		this.controlledObjects = (OCInstance[])input.readObject();
		
		// read unsynchronized point data
		boolean exists = input.readBoolean();
		if( exists )
			this.syncPointStatus = (HashMap<String,Boolean>)input.readObject();

		exists = input.readBoolean();
		if( exists )
			this.syncPointTags = (HashMap<String,byte[]>)input.readObject();

	}
	
	public void writeExternal( ObjectOutput output ) throws IOException
	{
		super.writeExternal( output );
		output.writeUTF( this.federateName );
		output.writeObject( this.timeStatus );
		output.writeObject( this.controlledObjects );
		
		// write unsynchronized point data, if there is none, write false as the flag
		// to signal this (to avoid sending an empty hashmap)
		if( this.syncPointStatus == null || this.syncPointStatus.isEmpty() )
		{
			output.writeBoolean( false );
		}
		else
		{
			output.writeBoolean( true );
			output.writeObject( this.syncPointStatus );
		}

		if( this.syncPointTags == null || this.syncPointTags.isEmpty() )
		{
			output.writeBoolean( false );
		}
		else
		{
			output.writeBoolean( true );
			output.writeObject( this.syncPointTags );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
