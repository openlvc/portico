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
package org.portico2.forwarder.tracking;

import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;

public class Federation
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private ObjectModel fom;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected Federation( String name, ObjectModel model )
	{
		this.name = name;
		this.fom = model;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return this.name;
	}

	/**
	 * When a federate joins a federation, it may expand the FOM. As such, whenever we see
	 * a federation join call we need to take the new FOM and use it in place of the old.
	 * 
	 * @param fom The new FOM that was part of a successful federation join
	 */
	protected void updateFOM( ObjectModel fom )
	{
		this.fom = fom;
	}

	protected final String resolveClassHandleToName( int classHandle )
	{
		OCMetadata clazz = fom.getObjectClass( classHandle );
		if( clazz != null )
			return clazz.getQualifiedName();
		else
			return null;
	}
	
	protected final String resolveInteractionClassToName( int classHandle )
	{
		ICMetadata clazz = fom.getInteractionClass( classHandle );
		if( clazz != null )
			return clazz.getQualifiedName();
		else
			return null;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
