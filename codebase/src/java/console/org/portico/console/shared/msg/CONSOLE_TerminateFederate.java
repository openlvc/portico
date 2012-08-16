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

public class CONSOLE_TerminateFederate extends CONSOLE_RequestMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationName;
	private String federateName;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public CONSOLE_TerminateFederate()
	{
		super();
	}
	
	public CONSOLE_TerminateFederate(String federationName, String federateName)
	{
		this();
		this.federationName = federationName;
		this.federateName = federateName;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * @return the federateName
	 */
	public String getFederateName()
	{
		return federateName;
	}

	/**
	 * @param federateName the federateName to set
	 */
	public void setFederateName( String federateName )
	{
		this.federateName = federateName;
	}

	/**
	 * @return the federationName
	 */
	public String getFederationName()
	{
		return federationName;
	}

	/**
	 * @param federationName the federationName to set
	 */
	public void setFederationName( String federationName )
	{
		this.federationName = federationName;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
