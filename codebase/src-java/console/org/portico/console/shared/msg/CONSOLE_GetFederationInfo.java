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

public class CONSOLE_GetFederationInfo extends CONSOLE_RequestMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	public static final String KEY_FEDERATION_NAME = "name";
	public static final String KEY_FEDERATION_LBTS = "lbts";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationName;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public CONSOLE_GetFederationInfo()
	{
		super();
	}
	
	public CONSOLE_GetFederationInfo(String newFederationName)
	{
		this();
		this.federationName = newFederationName;
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
	public void setFederationName( String federationName )
	{
		this.federationName = federationName;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}


