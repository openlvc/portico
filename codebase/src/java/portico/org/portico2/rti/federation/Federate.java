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
package org.portico2.rti.federation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.portico2.common.PorticoConstants;
import org.portico2.rti.RtiConnection;
import org.portico2.rti.services.mom.data.FomModule;

public class Federate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private String type;
	private int federateHandle;
	private RtiConnection federateConnection;
	private FederateMetrics metrics;
	private List<FomModule> fomModules;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Federate( String name, RtiConnection federateConnection )
	{
		// use federate name for federate type
		this( name, name, federateConnection );
	}

	public Federate( String name, String type, RtiConnection federateConnection )
	{
		this.name = name;
		this.type = type;
		this.federateConnection = federateConnection;
		this.metrics = new FederateMetrics();
		this.federateHandle = PorticoConstants.NULL_HANDLE;
		this.fomModules = new ArrayList<FomModule>();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getFederateName()
	{
		return this.name;
	}
	
	public String getFederateType()
	{
		return this.type;
	}
	
	public int getFederateHandle()
	{
		return this.federateHandle;
	}

	/**
	 * Store the handle for this federate. Only called from the {@link Federation}.
	 * @param handle Our federate handle
	 */
	protected void setFederateHandle( int handle )
	{
		this.federateHandle = handle;
	}

	public RtiConnection getConnection()
	{
		return this.federateConnection;
	}
	
	public FederateMetrics getMetrics()
	{
		return this.metrics;
	}

	public void addRawFomModules( List<FomModule> modules )
	{
		// As per the 1516e spec, only modules that add something to the FOM are to be recorded. To keep
		// things simple, we'll just assume that if the designator is different then the module added
		// new content
		Set<String> existingDesignators = new HashSet<>();
		for( FomModule existingModule : this.fomModules )
			existingDesignators.add( existingModule.getDesignator() );
		
		for( FomModule newModule : modules )
		{
			String newDesignator = newModule.getDesignator();
			if( !existingDesignators.contains(newDesignator) )
				this.fomModules.add( newModule );
		}
	}
	
	public List<FomModule> getRawFomModules()
	{
		return new ArrayList<>( this.fomModules );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	/// Message Sending and Processing  //////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
