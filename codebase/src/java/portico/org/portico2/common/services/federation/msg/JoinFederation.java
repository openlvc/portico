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
package org.portico2.common.services.federation.msg;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;
import org.portico2.rti.RtiConnection;
import org.portico2.rti.services.mom.data.FomModule;

public class JoinFederation extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federateName;
	private String federateType;
	private String federationName;
	private List<ObjectModel> parsedJoinObjectModels; // parsed version of object FOM modules below
	private List<FomModule> rawJoinObjectModels;      // raw version of FOM modules below

	private transient List<URL> fomModuleLocations;
	private transient ObjectModel fom;
	private transient RtiConnection connection; // ewww, separation-of-concerns! RTI need this sadly
	                                            // FIXME Put this in some kind of generic map or something

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public JoinFederation()
	{
		super();
		this.setImmediateProcessingFlag( true );
		this.rawJoinObjectModels = new ArrayList<FomModule>();
		this.parsedJoinObjectModels = new ArrayList<ObjectModel>();
		this.fomModuleLocations = new ArrayList<URL>();
	}

	public JoinFederation( String federationName, String federateName )
	{
		// use federateName for federateType
		this( federationName, federateName, federateName, null );
	}

	public JoinFederation( String federationName, String federateName, URL[] fomModules )
	{
		// use federate name for federate type
		this( federationName, federateName, federateName, fomModules );
	}

	public JoinFederation( String federationName, String federateName, String federateType )
	{
		this( federationName, federateName, federateType, null );
	}
	
	public JoinFederation( String federationName, String federateName, String federateType, URL[] fomModules )
	{
		this();
		this.federationName = federationName;
		this.federateName = federateName;
		this.federateType = federateType;
		if( fomModules != null )
		{
			for( URL module : fomModules )
				this.fomModuleLocations.add( module );
		}
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.JoinFederation;
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
	
	public String getFederationName()
	{
		return federationName;
	}

	public void setFederationName( String federationName )
	{
		this.federationName = federationName;
	}
	
	@Override
	public boolean isImmediateProcessingRequired()
	{
		return true;
	}

	public List<FomModule> getRawJoinModules()
	{
		return this.rawJoinObjectModels;
	}
	
	/**
	 * Returns a list of all the FOM modules that this federate is trying to join with.
	 */
	public List<ObjectModel> getParsedJoinModules()
	{
		return this.parsedJoinObjectModels;
	}
	
	public void addJoinModule( URL from, ObjectModel module )
	{
		FomModule raw = new FomModule( from );
		this.rawJoinObjectModels.add( raw );
		this.parsedJoinObjectModels.add( module );
	}
	
	//////////////////////////////////////////////////
	/// Transient Properties /////////////////////////
	//////////////////////////////////////////////////	
	public ObjectModel getFOM()
	{
		return this.fom;
	}
	
	public void setFOM( ObjectModel fom )
	{
		this.fom = fom;
	}
	
	public List<URL> getFomModuleLocations()
	{
		return this.fomModuleLocations;
	}
	
	public List<FomModule> getRawFomModules()
	{
		return this.rawJoinObjectModels;
	}

	public RtiConnection getConnection()
	{
		return this.connection;
	}
	
	public void setConnection( RtiConnection connection )
	{
		this.connection = connection;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
