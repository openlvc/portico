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

import org.portico2.common.PorticoConstants;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.rti.RtiConnection;

public class Federate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private int federateHandle;
	private RtiConnection federateConnection;
	private FederateMetrics metrics;
	
	private TimeStatus timeStatus;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Federate( String name, RtiConnection federateConnection )
	{
		this.name = name;
		this.federateConnection = federateConnection;
		this.metrics = new FederateMetrics();
		this.federateHandle = PorticoConstants.NULL_HANDLE;
		
		this.timeStatus = new TimeStatus();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getFederateName()
	{
		return this.name;
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

	public TimeStatus getTimeStatus()
	{
		return this.timeStatus;
	}
	
	public FederateMetrics getMetrics()
	{
		return this.metrics;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	/// Message Sending and Processing  //////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
