/*
 *   Copyright 2006 The Portico Project
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
package org.portico.console.client.text.fs;

import java.util.HashSet;
import java.util.Set;

import org.portico.console.client.text.fs.FSContext.ContextType;

/**
 * A Factory class to build the context objects requred by the RTI psuedo file system
 */
public class FSContextFactory
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Creates a root FSContext object, filling in the Context name, type, parent, valid
	 * file set and child types accordingly
	 */
	public static FSContext createRootContext()
	{
		Set<String> validFileSet = new HashSet<String>();
		FSContext newContext = new FSContext("", ContextType.RTI,
		                                     null, validFileSet, ContextType.Federation);
		return newContext;
	}
	
	/**
	 * Creates FSContext objects other than the root object. The type of FSContext object
	 * created depends on the parent type parsed to the function
	 */
	public static FSContext createContext(String name, FSContext parent)
		throws FSContextException
	{
		ContextType parentType = parent.getType();
		
		if ( parentType == FSContext.ContextType.RTI )
		{
			return createFederationContext (name, parent);
		}
		if (parentType == FSContext.ContextType.Federation )
		{
			return createFederateContext (name, parent);
		}
		else
		{
			throw new FSContextException("Can't create a context under: " + parentType);
		}
	}
	
	/**
	 * Creates a Federation FSContext object, filling in the Context name, type, parent, valid
	 * file set and child types accordingly
	 */
	private static FSContext createFederationContext(String name, FSContext parent)
		throws FSContextException
	{
		Set<String> validFileSet = new HashSet<String>();
		
		ContextType parentType = parent.getType();
		
		if (parent.getType() != ContextType.RTI)
		{
			throw new FSContextException("Can't create a federation context under " + parentType);
		}
		
		FSContext newContext = new FSContext(name, ContextType.Federation, parent, 
		                                     validFileSet, ContextType.Federate);
		
		return newContext;
	}
	
	/**
	 * Creates a Federate FSContext object, filling in the Context name, type, parent, valid
	 * file set and child types accordingly
	 */
	private static FSContext createFederateContext(String name, FSContext parent)
		throws FSContextException
	{
		Set<String> validFileSet = new HashSet<String>();
		
		ContextType parentType = parent.getType();
		
		if (parent.getType() != ContextType.Federation)
		{
			throw new FSContextException("Can't create a federate context under " + parentType);
		}
		
		FSContext newContext = new FSContext(name, ContextType.Federate, parent, 
		                                     validFileSet, ContextType.None);
		
		return newContext;
	}
	
	
}


