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
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message represents a request to send an interaction with the given set of parameters.
 * It contains an optional region token with with the interaction is being sent. If it is a
 * vanilla interaction, without region data, the regionToken is {@link PorticoConstants#NULL_HANDLE}
 */
public class SendInteraction extends PorticoMessage implements Externalizable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int interactionId;
	private byte[] tag;
	private HashMap<Integer,byte[]> parameters;
	private int regionToken;
	
	// these parameters are filled out on the receiver side as required, do not transmit!
	// TODO is this still true in portico2?
	private transient int receivingRegionToken;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public SendInteraction()
	{
		super();
		this.regionToken = PorticoConstants.NULL_HANDLE;
		this.receivingRegionToken = PorticoConstants.NULL_HANDLE;
	}
	
	public SendInteraction( int interacitonId, byte[] tag, HashMap<Integer,byte[]> parameters )
	{
		this();
		this.interactionId = interacitonId;
		this.tag = tag;
		this.parameters = parameters;
	}

	public SendInteraction( int interactionId,
	                        byte[] tag,
	                        HashMap<Integer,byte[]> parameters,
	                        int regionToken )
	{
		this( interactionId, tag, parameters );
		this.regionToken = regionToken;
	}

	public SendInteraction( int interactionId,
	                        byte[] tag,
	                        HashMap<Integer,byte[]> parameters,
	                        double time )
	{
		this( interactionId, tag, parameters );
		this.timestamp = time;
	}
	
	public SendInteraction( int interactionId,
	                        byte[] tag,
	                        HashMap<Integer,byte[]> parameters,
	                        int regionToken,
	                        double time )
	{
		this( interactionId, tag, parameters, regionToken );
		this.timestamp = time;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.SendInteraction;
	}

	public int getInteractionId()
	{
		return interactionId;
	}

	public void setInteractionId( int interactionId )
	{
		this.interactionId = interactionId;
	}

	public HashMap<Integer,byte[]> getParameters()
	{
		return parameters;
	}

	public void setParameters( HashMap<Integer,byte[]> parameters )
	{
		this.parameters = parameters;
	}

	public byte[] getTag()
	{
		return tag;
	}

	public void setTag( byte[] tag )
	{
		this.tag = tag;
	}

	public int getRegionToken()
	{
		return this.regionToken;
	}
	
	public void setRegionToken( int regionToken )
	{
		this.regionToken = regionToken;
	}
	
	/**
	 * {@link #getRegionToken()} will get the token of the region the interaction was sent with.
	 * This method will get the token of the region the local federate used in its subscription
	 * that is responsible for it receiving the interaction. If the local federate or the sending
	 * federate is not using DDM, {@link PorticoConstants#NULL_HANDLE} is used.
	 */
	public int getReceivingRegionToken()
	{
		return this.receivingRegionToken;
	}
	
	public void setReceivingRegionToken( int regionToken )
	{
		this.receivingRegionToken = regionToken;
	}

	/**
	 * Returns <code>true</code> is DDM information has been supplied with this message,
	 * <code>false</code> if it is just a vanilla interaciton sending request.
	 */
	public boolean usesDDM()
	{
		return this.regionToken != PorticoConstants.NULL_HANDLE;
	}

	/**
	 * Returns <code>true</code>
	 */
	public boolean isSpecDefinedMessage()
	{
		return true;
	}

	/////////////////////////////////////////////////////////////
	/////////////////// Serialization Methods ///////////////////
	/////////////////////////////////////////////////////////////
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
		buffer.writeInt( this.interactionId );

		// marshal the relevant super-class information first
		buffer.writeInt( sourceFederate );
		//buffer.writeInt( targetFederate ); -- not needed, always -1 (all federates)
		buffer.writeInt( targetFederation );
		//buffer.writeInt( serial );         -- not needed, always -1 (async)
		buffer.writeDouble( timestamp );
		//buffer.writeBoolean( immediate );  -- not needed, always false
		
		// now marshal up the rest of our stuff
		buffer.writeUTF( bytesToString(this.tag) ); // conver it for a string for simple read/write
		buffer.writeInt( this.regionToken );

		// write the number of parameters we are pushing out
		buffer.writeInt( parameters.size() );
		// write each of the attribute values
		for( Integer parameterHandle : parameters.keySet() )
		{
			buffer.writeInt( parameterHandle );
			byte[] parameterValue = parameters.get( parameterHandle );
			buffer.writeInt( parameterValue.length );
			buffer.write( parameterValue );
		}
		
		buffer.flush();
	}

	@Override
	public void unmarshal( ObjectInput buffer ) throws IOException, ClassNotFoundException
	{
		// read the interaction handle first (we put this first so we can snoop it easily in MessageHelpers)
		//this.interactionId = buffer.readInt();

		// read the super-class information first
		super.sourceFederate = buffer.readInt();
		//super.targetFederate = buffer.readInt(); -- not needed, always -1 (all federates)
		super.targetFederation = buffer.readInt();
		//super.serial = buffer.readInt();         -- not needed, always -1 (async)
		super.timestamp = buffer.readDouble();
		//super.immediate = buffer.readBoolean();  -- not needed, always false
		
		// now read the rest of our stuff
		this.tag = buffer.readUTF().getBytes();
		this.regionToken = buffer.readInt();

		// read the parameter data in, formatted as int,[int,byte[]]...
		// first int is number of parameters, next are pairs of parameterHandle/values
		if( this.parameters == null )
			this.parameters = new HashMap<Integer,byte[]>();

		int parameterCount = buffer.readInt();
		for( int i = 0; i < parameterCount; i++ )
		{
			int parmaterHandle = buffer.readInt();
			int valueSize = buffer.readInt();
			byte[] parameterValue = new byte[valueSize];
			buffer.readFully( parameterValue, 0, valueSize );
			this.parameters.put( parmaterHandle, parameterValue );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

}
