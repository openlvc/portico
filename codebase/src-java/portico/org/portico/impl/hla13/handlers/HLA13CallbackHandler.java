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
package org.portico.impl.hla13.handlers;

import java.util.Map;

import org.portico.impl.HLAVersion;
import org.portico.impl.hla13.Impl13Helper;
import org.portico.impl.hla13.ImplJava1Helper;
import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JConfigurationException;

/**
 * This class provides some basic helper methods for dealing with the differences between the
 * standard HLA 1.3 and Java1 interfaces. Callback handlers should extend this class and be
 * sure to call {@link #initialize(Map)} before they process it for themselves.
 */
public abstract class HLA13CallbackHandler extends LRCMessageHandler
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl13Helper impl13Helper;
	private ImplJava1Helper implJava1Helper;
	private boolean standard = true;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
		if( lrc.getSpecHelper().getHlaVersion() == HLAVersion.HLA13 )
		{
			this.impl13Helper = (Impl13Helper)lrc.getSpecHelper();
			this.standard = true;
		}
		else if( lrc.getSpecHelper().getHlaVersion() == HLAVersion.JAVA1 )
		{
			this.implJava1Helper = (ImplJava1Helper)lrc.getSpecHelper();
			this.standard = false;
		}
		else
		{
			throw new JConfigurationException( "Incompatible HLA version (need HLA13 or JAVA1): " +
			                                   lrc.getSpecHelper().getHlaVersion() );
		}
	}

	protected boolean isStandard()
	{
		return this.standard;
	}

	/**
	 * Get a reference to the Java1 FederateAmbassador
	 */
	protected hla.rti13.java1.FederateAmbassador java1()
	{
		return implJava1Helper.getFederateAmbassador();
	}
	
	/**
	 * Get a reference to the HLA 1.3 FederateAmbassador
	 */
	protected hla.rti.FederateAmbassador hla13()
	{
		return impl13Helper.getFederateAmbassador();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
