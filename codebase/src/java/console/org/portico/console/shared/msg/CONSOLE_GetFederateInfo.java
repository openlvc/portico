/*
 *   Copyright 2006 The Portico Project
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
package org.portico.console.shared.msg;

/**
 * This message is a request to the console RTI binding to provide information about a particular
 * federate
 */
public class CONSOLE_GetFederateInfo extends CONSOLE_RequestMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	public static final String KEY_ADVANCING = "advancing";
	public static final String KEY_CONSTRAINED = "constrained";
	public static final String KEY_CURRENT_TIME = "current_time";
	public static final String KEY_HANDLE = "handle";
	public static final String KEY_LOOK_AHEAD = "look_ahead";
	public static final String KEY_NAME = "name";
	public static final String KEY_REGULATING = "regulating";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationName;
	private String federateName;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public CONSOLE_GetFederateInfo()
	{
		super();
	}
	
	public CONSOLE_GetFederateInfo(String federationName, String federateName)
	{
		this();
		this.federationName = federationName;
		this.federateName = federateName;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * @return Returns the value of federationName.
	 */
	public String getFederationName()
	{
		return federationName;
	}

	/**
	 * @param federationName Sets the value of federationName.
	 */
	public void setFederationName( String federationName)
	{
		this.federationName = federationName;
	}

	/**
	 * @return Returns the value of federateName.
	 */
	public String getFederateName()
	{
		return federateName;
	}

	/**
	 * @param federateName Sets the value of federateName.
	 */
	public void setFederateName( String federateName )
	{
		this.federateName = federateName;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}


