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
package org.portico.lrc.services.federation.msg;

import org.portico.lrc.compat.JResignAction;
import org.portico.utils.messaging.PorticoMessage;

public class ResignFederation extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private JResignAction resignAction;
	private String federateName;
	private String federationName;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public ResignFederation( JResignAction resignAction )
	{
		this.resignAction = resignAction;
	}

	public ResignFederation( JResignAction resignAction, String federate, String federation )
	{
		this.resignAction = resignAction;
		this.federateName = federate;
		this.federationName = federation;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public JResignAction getResignAction()
	{
		return this.resignAction;
	}

	public void setResignAction( JResignAction action )
	{
		this.resignAction = action;
	}
	
	public String getFederationName()
	{
		return this.federationName;
	}
	
	public void setFederationName( String federationName )
	{
		this.federationName = federationName;
	}
	
	public String getFederateName()
	{
		return this.federateName;
	}
	
	public void setFederateName( String federate )
	{
		this.federateName = federate;
	}

	@Override
	public boolean isImmediateProcessingRequired()
	{
		return true;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
