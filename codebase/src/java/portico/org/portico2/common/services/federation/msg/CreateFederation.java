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
import java.util.Collection;
import java.util.List;

import org.portico.impl.HLAVersion;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.MessageType;
import org.portico2.rti.services.mom.data.FomModule;

/**
 * Contains information relating to a request to create a new federation. The component creating
 * the instance of this message should have already parsed the object model file into an
 * {@link ObjectModel} instance which it can give to this message.
 */
public class CreateFederation extends PorticoMessage
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	// Keys that will go into a successful response object
	public static final String KEY_FEDERATION_NAME = "federationName";
	public static final String KEY_FEDERATION_HANDLE = "federationHandle";
	public static final String KEY_FOM = "fom";
	
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federationName;
	private transient List<URL> fomModuleLocations;
	private ObjectModel objectModel;
	private HLAVersion hlaVersion;
	private List<FomModule> rawFomModules;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public CreateFederation()
	{
		super();
		this.fomModuleLocations = new ArrayList<URL>();
		this.rawFomModules = new ArrayList<FomModule>();
	}
	
	public CreateFederation( String federationName, ObjectModel model )
	{
		this();
		this.federationName = federationName;
		this.objectModel = model;
	}
	
	public CreateFederation( String federationName, URL fedfileLocation )
	{
		this();
		this.federationName = federationName;
		if( fedfileLocation != null )
			this.fomModuleLocations.add( fedfileLocation );
	}
	
	public CreateFederation( String federationName, URL[] fomModules )
	{
		this();
		this.federationName = federationName;
		if( fomModules != null )
			for( URL module : fomModules )
				this.fomModuleLocations.add( module );
	}
	
	public CreateFederation( String federationName, List<URL> fomModules )
	{
		this();
		this.federationName = federationName;
		if( fomModules != null )
			this.fomModuleLocations.addAll( fomModules );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	@Override
	public MessageType getType()
	{
		return MessageType.CreateFederation;
	}
	
	public String getFederationName()
	{
		return federationName;
	}

	public void setFederationName( String federationName )
	{
		this.federationName = federationName;
	}
	
	public void setModel( ObjectModel model, Collection<FomModule> rawModules )
	{
		this.objectModel = model;
		this.rawFomModules.clear();
		this.rawFomModules.addAll( rawModules );
	}
	
	public ObjectModel getModel()
	{
		return this.objectModel;
	}
	
	public List<URL> getFomModuleLocations()
	{
		return this.fomModuleLocations;
	}
	
	public List<FomModule> getRawFomModules()
	{
		return this.rawFomModules;
	}
	
	public void setHlaVersion( HLAVersion version )
	{
		this.hlaVersion = version;
	}
	
	public HLAVersion getHlaVersion()
	{
		return this.hlaVersion;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
