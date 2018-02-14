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
package org.portico2.common.services.sync.msg;

import java.util.HashSet;
import java.util.Set;

import org.portico.lrc.services.sync.data.SyncPoint;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;

/**
 * This message represents the request to register a sync point with the given label and federate
 * set. If accepted by the RTI, the RTI will then generate {@link SyncPointAnnounce} messages that
 * are sent to each federate for callback processing.
 */
public class RegisterSyncPoint extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String label;
	private byte[] tag;
	private HashSet<Integer> federates;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RegisterSyncPoint( String label, byte[] tag )
	{
		this.label = label;
		this.tag = tag;
	}
	
	/**
	 * This will make a copy of the given set of federates, it won't store the original set.
	 */
	public RegisterSyncPoint( String label, byte[] tag, Set<Integer> federates )
	{
		this( label, tag );
		if( federates != null )
			this.federates = new HashSet<Integer>( federates );
	}
	
	/**
	 * Initialize the registration request from an existing sync point
	 */
	public RegisterSyncPoint( SyncPoint point )
	{
		this( point.getLabel(), point.getTag(), point.getFederates() );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.RegisterSyncPoint;
	}

	public void setLabel( String label )
	{
		this.label = label;
	}
	
	public String getLabel()
	{
		return this.label;
	}
	
	public void setTag( byte[] tag )
	{
		this.tag = tag;
	}

	public byte[] getTag()
	{
		return this.tag;
	}
	
	/**
	 * Set the federate handles that this sync point should be restircted to. If the given set is
	 * null, the request will be ignored and nothing will be changed.
	 */
	public void setFederateSet( Set<Integer> federates )
	{
		if( federates != null )
			this.federates = new HashSet<Integer>( federates );
	}
	
	/**
	 * This method will convert the internal federate handle set to be <code>null</code>, thus
	 * indicating that this is a federation-wide sync point (and not a sync point destined for
	 * 0 federates if the set is empty). The {@link #setFederateSet(Set)} method will ignore
	 * any request to set the internal set to null, so this method must be used instead.
	 */
	public void makeFederationWide()
	{
		this.federates = null;
	}
	
	/**
	 * Returns <code>true</code> if this point is meant for all federates (a federation-wide point),
	 * returns <code>false</code> if there is only meant to be a restricted number of federates
	 * involved with the point.
	 */
	public boolean isFederationWide()
	{
		if( federates == null || federates.isEmpty() )
			return true;
		else
			return false;
	}
	
	public HashSet<Integer> getFederateSet()
	{
		return this.federates;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
