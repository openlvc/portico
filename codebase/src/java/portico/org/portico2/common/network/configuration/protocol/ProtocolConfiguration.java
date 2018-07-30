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
package org.portico2.common.network.configuration.protocol;

import org.portico.lrc.compat.JConfigurationException;
import org.portico2.common.configuration.RID;
import org.portico2.common.network.protocol.ProtocolType;
import org.w3c.dom.Element;

/**
 * This class is the parent of all protcol configuration objects.
 */
public abstract class ProtocolConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected boolean enabled;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected ProtocolConfiguration()
	{
		this.enabled = true;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public abstract ProtocolType getProtocolType();

	/**
	 * Parse the XML element within the protocol stack as extracted from the RID file.
	 * 
	 * @param rid The RID object we are being inserted into
	 * @param element The element from the RID file
	 * @throws JConfigurationException If there is a problem with the incoming configuration
	 *         information or some of it is missing.
	 */
	public abstract void parseConfiguration( RID rid, Element element ) throws JConfigurationException;
	

	public boolean isEnabled()
	{
		return this.enabled;
	}
	
	public void setEnabled( boolean enabled )
	{
		this.enabled = enabled;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
