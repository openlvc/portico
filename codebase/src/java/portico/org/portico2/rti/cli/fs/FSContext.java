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
package org.portico2.rti.cli.fs;

import java.util.Set;

/**
 * Represents a node within the RTI pseudo file system 
 */
public class FSContext
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static enum ContextType 
	{
		RTI, 
		Federation, 
		Federate, 
		None
	};
		
	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String name;
	private ContextType type;
	private FSContext parent;
	private Set<String> validFiles;
	private ContextType childDirType;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FSContext( String newName, 
	                  ContextType newType, 
	                  FSContext newParent, 
	                  Set<String> newValidFiles, 
	                  ContextType newChildType )
	{
		this.name = newName;
		this.type = newType;
		this.parent = newParent;
		this.validFiles = newValidFiles;
		this.childDirType = newChildType;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * @return the value of this context's local name.
	 */
	public String getName()
	{
		return name;
	}


	/**
	 * Sets the value of this context's local name.
	 */
	public void setName( String name )
	{
		this.name = name;
	}


	/**
	 * @return this context's parent.
	 */
	public FSContext getParent()
	{
		return parent;
	}


	/**
	 * Sets this context's parent.
	 */
	public void setParent( FSContext parent )
	{
		this.parent = parent;
	}


	/**
	 * @return what context type this context is.
	 */
	public ContextType getType()
	{
		return type;
	}


	/**
	 * Sets what context type this context is
	 */
	public void setType( ContextType type )
	{
		this.type = type;
	}

	/**
	 * @return  what type is allowed to reside under this context
	 */
	public ContextType getChildDirType()
	{
		return childDirType;
	}

	/**
	 * Sets what child type this context can have
	 */
	public void setChildDirType( ContextType childDirType )
	{
		this.childDirType = childDirType;
	}

	/**
	 * @return a list of psuedo files that are contained within this context.
	 */
	public Set<String> getValidFiles()
	{
		return validFiles;
	}

	/**
	 * @return the list of psuedo files that are contained within this context.
	 */
	public void setValidFiles( Set<String> validFiles )
	{
		this.validFiles = validFiles;
	}	
	
	/**
	 * @return the absoloute path to this context
	 */
	public String getHeirachicalName()
	{
		if ( this.parent != null )
		{
			return this.parent.getHeirachicalName() + "/" + this.getName();
		}
		else
		{
			return this.name;
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static String getContextPath( FSContext context )
	{
		String contextName = context.getHeirachicalName();
		if( contextName.isEmpty() )
			contextName = "/";
		
		return contextName;
	}
}

