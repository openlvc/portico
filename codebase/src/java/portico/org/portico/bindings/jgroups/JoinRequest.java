/*
 *   Copyright 2021 The Portico Project
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
package org.portico.bindings.jgroups;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.portico.lrc.model.ObjectModel;

public class JoinRequest implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String federateName;
	private List<ObjectModel> fomModules;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public JoinRequest( String federateName, List<ObjectModel> fomModules )
	{
		this.federateName = federateName;
		this.fomModules = new ArrayList<>();
		this.fomModules.addAll( fomModules );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public String getFederateName()
	{
		return this.federateName;
	}
	
	public List<ObjectModel> getFomModules()
	{
		return this.fomModules;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
