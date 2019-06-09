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

import java.util.HashMap;
import java.util.Map;

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
	private Map<Integer,String> objectToClassQName;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected Federation( String name, ObjectModel model )
	{
		this.name = name;
		this.fom = model;
		this.objectToClassQName = new HashMap<>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  State Management Methods   ////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
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

	/**
	 * Store information about an object that was registered. We pre-cache the qualified
	 * name of its registering class so we can quickly look it up later via the method
	 * {@link #resolveObjectHandleToClassName(int)}.
	 * 
	 * @param objectHandle The handle of the object that was added
	 * @param classHandle  The class handle for the object that was added
	 * @return True if the name was registered, false otherwise. This can fail if we cannot
	 *         resolve the class handle to a name
	 */
	protected boolean addObject( int objectHandle, int classHandle )
	{
		// convert the class handle into a class name
		String qname = resolveClassHandleToName( classHandle );
		if( qname == null )
			return false;

		// register the name against the object handle
		objectToClassQName.put( objectHandle, qname );
		return true;
	}
	
	protected void removeObject( int objectHandle )
	{
		objectToClassQName.remove( objectHandle );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Query Support Methods   ///////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	protected final String resolveObjectHandleToClassName( int objectHandle )
	{
		return objectToClassQName.get( objectHandle );
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

	
	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getName()
	{
		return this.name;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
