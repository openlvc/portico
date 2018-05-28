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
package org.portico2.common.network2.configuration;

import java.util.Properties;

public class JvmConfiguration extends ConnectionConfiguration
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
	public JvmConfiguration( String name )
	{
		super( name );
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

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Configuration Loading   ////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void parseConfiguration( String prefix, Properties properties )
	{
		// no-op
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
