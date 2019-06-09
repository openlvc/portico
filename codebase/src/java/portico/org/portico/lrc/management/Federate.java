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

import java.io.Serializable;

import org.portico.lrc.LRCState;
import org.portico2.common.services.time.data.TimeStatus;

/**
 * This method stores information about a particular federate, such as its name, handle, time
 * status, etc... Only a small amount of information is stored internally. This class mostly
 * acts as a front to the information stored within the LRC, funnelling and filtering requests
 * through to the appropriate part of the {@link LRCState} as required.
 */
public class Federate implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected transient LRCState lrcState;
	private int federateHandle;
	private String federateName;
	private String federateType;
	private String federateHost;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Federate( LRCState lrcState, int federateHandle, String federateName, String federateType )
	{
		this.lrcState = lrcState;
		this.federateHandle = federateHandle;
		this.federateName = federateName;
		this.federateType = federateType;
		this.federateHost = "<not-implemented>";
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public int getFederateHandle()
    {
    	return federateHandle;
    }

	public void setFederateHandle( int federateHandle )
    {
    	this.federateHandle = federateHandle;
    }

	public String getFederateName()
    {
    	return federateName;
    }

	public void setFederateName( String federateName )
    {
    	this.federateName = federateName;
    }

	public String getFederateType()
	{
		return federateType;
	}
	
	public void setFederateType( String federateType )
	{
		this.federateType = federateType;
	}
	
	public String getFederateHost()
    {
    	return federateHost;
    }

	public void setFederateHost( String federateHost )
    {
    	this.federateHost = federateHost;
    }

	public TimeStatus getTimeStatus()
    {
    	return lrcState.getTimeManager().getTimeStatus( federateHandle );
    }

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
