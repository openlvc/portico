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
package org.portico.bindings.jgroups;

import java.util.HashSet;
import java.util.Set;

import org.portico.bindings.ConnectedRoster;
import org.portico.lrc.model.ObjectModel;

public class JGroupsRoster implements ConnectedRoster
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int localHandle;
	private Set<Integer> remoteHandles;
	private ObjectModel fom;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public JGroupsRoster( int localHandle, Set<Integer> remoteHandles, ObjectModel fom )
	{
		this.localHandle = localHandle;
		this.remoteHandles = new HashSet<Integer>( remoteHandles );
		this.fom = fom;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public int getLocalHandle()
    {
    	return localHandle;
    }

	public void setLocalHandle( int localHandle )
    {
    	this.localHandle = localHandle;
    }

	public Set<Integer> getRemoteHandles()
    {
    	return remoteHandles;
    }

	public void setRemoteHandles( Set<Integer> remoteHandles )
    {
    	this.remoteHandles = new HashSet<Integer>( remoteHandles );
    }

	public ObjectModel getFOM()
    {
    	return fom;
    }

	public void setFOM( ObjectModel fom )
    {
    	this.fom = fom;
    }
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
