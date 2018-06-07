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
package org.portico2.rti.cli.fs;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.portico2.rti.cli.RtiCli;
import org.portico2.rti.cli.fs.FSContext.ContextType;

/**
 * A Factory class to build the context objects required by the RTI pseudo file system
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
		FSContext newContext = new FSContext( "", 
		                                      ContextType.RTI,
		                                      null, 
		                                      validFileSet, 
		                                      ContextType.Federation);
		return newContext;
	}
	
	public static FSContext fromPath( RtiCli container, String path )
	{
		// Create a temporary context to hold where we are going to go
		// initialise it to where we are currently
		FSContext tempContext = container.getCurrentContext();
		
		// If the path started with the root symbol then point the temp context to the
		// root context
		if ( path.startsWith( "/" ) )
			tempContext = container.getRootContext();
		
		// Tokenise the path on /
		StringTokenizer tok = new StringTokenizer( path, "/" );
		
		// Iterate through the tokens
		while( tok.hasMoreTokens() )
		{
			String token = tok.nextToken();
			
			// If the current entry in the path is to go up one
			if( token.equals( ".." ) )
			{
				// Have a look at the parent of this context
				FSContext theParent = tempContext.getParent();
				
				// if the parent of this context is null
				if( theParent == null )
				{
					// we are at the root so just stay here
				}
				else
				{
					// otherwise traverse up
					tempContext = tempContext.getParent();
				}
			}
			else if( token.equals( "." ) )
			{
				// stay in the same context
			}
			else
			{
				// going down a directory
				tempContext = FSContextFactory.createContext( token, tempContext );
			}
		}
				
		return tempContext;	
	}
	
	/**
	 * Creates FSContext objects other than the root object. The type of FSContext object
	 * created depends on the parent type parsed to the function
	 */
	private static FSContext createContext( String name, FSContext parent )
	{
		ContextType parentType = parent.getType();
		
		if( parentType == FSContext.ContextType.RTI )
			return createFederationContext( name, parent );
		else if( parentType == FSContext.ContextType.Federation )
			return createFederateContext( name, parent );
		else
			throw new IllegalArgumentException( "Can't create a context under: " + parentType );
	}
	
	/**
	 * Creates a Federation FSContext object, filling in the Context name, type, parent, valid
	 * file set and child types accordingly
	 */
	private static FSContext createFederationContext( String name, FSContext parent )
	{
		Set<String> validFileSet = new HashSet<String>();
		ContextType parentType = parent.getType();
		
		if( parent.getType() != ContextType.RTI )
			throw new IllegalArgumentException( "Can't create a federation context under " + parentType );
		
		FSContext newContext = new FSContext( name, 
		                                      ContextType.Federation, 
		                                      parent, 
		                                      validFileSet, 
		                                      ContextType.Federate );
		
		return newContext;
	}
	
	/**
	 * Creates a Federate FSContext object, filling in the Context name, type, parent, valid
	 * file set and child types accordingly
	 */
	private static FSContext createFederateContext( String name, FSContext parent )
	{
		Set<String> validFileSet = new HashSet<String>();
		ContextType parentType = parent.getType();
		
		if( parent.getType() != ContextType.Federation )
			throw new IllegalArgumentException( "Can't create a federate context under " + parentType );
		
		FSContext newContext = new FSContext( name, 
		                                      ContextType.Federate, 
		                                      parent, 
		                                      validFileSet, 
		                                      ContextType.None );
		
		return newContext;
	}
	
	
}

