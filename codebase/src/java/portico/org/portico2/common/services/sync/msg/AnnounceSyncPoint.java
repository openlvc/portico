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
package org.portico2.common.services.sync.msg;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;
import org.portico2.rti.services.sync.data.SyncPoint;

/**
 * Tell all the relevant federates that a synchronization point has been announced so that
 * they can queue a callback.
 */
public class AnnounceSyncPoint extends PorticoMessage
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
	private Set<Integer> federates;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public AnnounceSyncPoint( SyncPoint point )
	{
		this.label = point.getLabel();
		this.tag = point.getTag();
		this.federates = Collections.unmodifiableSet( point.getFederates() );
	}

	public AnnounceSyncPoint( String label )
	{
		this.label = label;
		this.tag = new byte[]{};
		this.federates = Collections.emptySet();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.AnnounceSyncPoint;
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
	
	public Set<Integer> getFederateSet()
	{
		return this.federates;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
