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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.model.ObjectModel;
import org.portico2.rti.RTI;

/**
 * The purpose of the {@link FederationManager} is to keep track and state about the various
 * active federations that are being supported by the RTI instance in which they are contained.
 */
public class FederationManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String,Federation> federationsByName;
	private Map<Integer,Federation> federationsByHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederationManager()
	{
		this.federationsByName = new HashMap<>();
		this.federationsByHandle = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public Collection<Federation> getActiveFederations()
	{
		return federationsByHandle.values();
	}

	public Federation getFederation( String name )
	{
		return federationsByName.get( name );
	}
	
	public Federation getFederation( int federationHandle )
	{
		return federationsByHandle.get( federationHandle );
	}
	
	public boolean containsFederation( String name )
	{
		return federationsByName.containsKey( name );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Federation Management   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create a new federation with the given information and store it inside the manager before
	 * returning it.
	 * 
	 * @param rti  The RTI this federation exists inside of
	 * @param name The name of the federation
	 * @param fom  The object model for the federation
	 * @param hlaVersion Version of the HLA spec the federation should load message handlers for
	 * @return A fresh {@link Federation} instance that is now active and stored inside the manager
	 * @throws JConfigurationException If there is a problem configuring the handlers
	 */
	public synchronized Federation createFederation( RTI rti,
	                                                 String name,
	                                                 ObjectModel fom,
	                                                 HLAVersion hlaVersion )
		throws JConfigurationException
	{
		Federation federation = new Federation( rti, name, fom, hlaVersion );
		this.federationsByName.put( federation.getFederationName(), federation );
		this.federationsByHandle.put( federation.getFederationHandle(), federation );
		federation.createdFederation();
		return federation;
	}
	
	public synchronized void destroyFederation( Federation federation )
	{
		federation.destroyedFederation();
		this.federationsByName.remove( federation.getFederationName() );
		this.federationsByHandle.remove( federation.getFederationHandle() );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
