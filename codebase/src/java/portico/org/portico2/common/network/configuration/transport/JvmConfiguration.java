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
package org.portico2.common.network.configuration.transport;

import org.portico2.common.configuration.RID;
import org.portico2.common.network.configuration.ConnectionConfiguration;
import org.portico2.common.network.transport.TransportType;
import org.w3c.dom.Element;

public class JvmConfiguration extends TransportConfiguration
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public JvmConfiguration( ConnectionConfiguration connectionConfiguration )
	{
		super( connectionConfiguration );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Read Only
	 */
	public TransportType getTransportType()
	{
		return TransportType.JVM;
	}
	
	@Override
	public String toString()
	{
		return String.format( "[JVM: name=%s, enabled=%s]", super.name, super.enabled );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void parseConfiguration( RID rid, Element element )
	{
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
