/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.management;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCState;
import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;

/**
 * This class provides a bunch of methods for obtaining information about a federation. Very little
 * information is stored in this class, it mainly just provides a facade/helper to expose the
 * information stored by the LRC on other federates.
 */
public class Federation implements SaveRestoreTarget, Iterable<Federate>
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRCState lrcState;
	private Map<Integer,Federate> federates;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Federation( LRCState lrcState )
	{
		this.lrcState = lrcState;
		this.federates = new HashMap<Integer,Federate>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getFederationName()
	{
		return lrcState.getFederationName();
	}
	
	public void addFederate( Federate federate )
	{
		this.federates.put( federate.getFederateHandle(), federate );
	}
	
	public Federate removeFederate( int federateHandle )
	{
		return this.federates.remove( federateHandle );
	}
	
	public Federate getFederate( int federateHandle )
	{
		return this.federates.get( federateHandle );
	}

	public Set<Integer> getFederateHandles()
	{
		return Collections.unmodifiableSet( federates.keySet() );
	}

	public Iterator<Federate> iterator()
	{
		return federates.values().iterator();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( federates );
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		federates = (HashMap<Integer,Federate>)input.readObject();

		// restore the references to the lrcState in the federates
		for( Federate federate : federates.values() )
			federate.lrcState = this.lrcState;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
