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
package org.portico.lrc.model;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.Serializable;

import org.portico.lrc.PorticoConstants;

/**
 * This class contains metadata information about a FOM object class 
 */
public class ICMetadata implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String                  name;
	private int                     handle;
	private Order                   order;
	private Transport               transport;
	private Sharing 			    sharing;
	private Space                   space;
	private ICMetadata              parent;
	private Set<ICMetadata>         children;
	private Map<Integer,PCMetadata> parameters;
	private ObjectModel             model;
	private String                  qualifiedName; // set on first access
	private String                  vsafeQualifiedName; // version-safe name, set on first access

	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * <b>NOTE:</b> This constructor should generally not be used. If you want an instance of this
	 * class you should use the creation methods of {@link ObjectModel ObjectModel}.
	 */
	public ICMetadata( String name, int handle )
	{
		this.name        = name;
		this.handle      = handle;
		this.order       = Order.TIMESTAMP;
		this.transport   = Transport.RELIABLE;
		this.sharing	 = Sharing.NEITHER;
		this.space       = null;
		this.parent      = null;
		this.model       = null;
		this.children    = new HashSet<ICMetadata>();
		this.parameters  = new HashMap<Integer,PCMetadata>();
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public Set<ICMetadata> getChildTypes()
	{
		return this.children;
	}

	/**
	 * Returns the contained child interaction type if its local name matches the given one.
	 * If no child with the name is found, null is returned.
	 */
	public ICMetadata getChildType( String name )
	{
		for( ICMetadata child : children )
		{
			if( child.getLocalName().equals(name) )
				return child;
		}
		
		return null;
	}
	
	/**
	 * This method will cause this interaction class to remove any links between it and the object
	 * model to which it is currently attached. This includes removing itself from the store
	 * of "child" classes in its parent and setting its object model reference to null. The
	 * overall effect of this method is to cleave the interaction class from the object model.
	 */
	protected void cleave()
	{
		// dissolve our relationship with our parent //
		if( this.parent != null )
		{
			// remove us as a child
			this.parent.children.remove( this );
			// emancipate
			this.parent = null;
		}
		
		// remove our link to the model //
		this.model = null;
	}
	
	////////////////////////////////////////////////////////////
	///////////////////// Parameter Methods ////////////////////
	////////////////////////////////////////////////////////////
	
	/**
	 * Adds the given parameter to this interaction class. If the name of the parameter is the same
	 * as one already in this class, the request will be ignored and false will be returned.
	 * Otherwise, the parameter will be added and true will be returned.
	 */
	public boolean addParameter( PCMetadata parameter )
	{
		String name = parameter.getName();
		
		// check to see if we already have an parameter with the same name
		for( PCMetadata temp : parameters.values() )
		{
			if( temp.getName().equals(name) )
			{
				return false;
			}
		}
		
		// no existing parameter
		this.parameters.put( parameter.getHandle(), parameter );
		// assign the container property
		parameter.setContainer( this );
		return true;
	}
	
	/**
	 * Remove and return the local parameter of the given handle (not inherited). If there is no
	 * parameter by that handle, null will be returned. 
	 */
	public PCMetadata removeParameter( int handle )
	{
		PCMetadata parameter = this.parameters.remove( handle );
		if( parameter == null )
		{
			return null;
		}
		else
		{
			parameter.setContainer( null );
			return parameter;
		}
	}
	
	/**
	 * Return a set of all the parameters contained directly within this class (no inherited ones) 
	 */
	public Set<PCMetadata> getDeclaredParameters()
	{
		return new HashSet<PCMetadata>( this.parameters.values() );
	}
	
	/**
	 * Return a set of all available parameters for this class (inherited included) 
	 */
	public Set<PCMetadata> getAllParameters()
	{
		// if we don't have parent, all our parameters are just our local ones
		if( this.parent == null )
		{
			return this.getDeclaredParameters();
		}
		else
		{
			// we have a parent, must combine our parameter with theirs
			Set<PCMetadata> inherited = this.parent.getAllParameters();
			// add our local parameters
			inherited.addAll( this.parameters.values() );
			// return the complete set
			return inherited;
		}
	}
	
	/**
	 * Get the locally declared parameter (not inherited) of the given handle and return it. If
	 * there is no such parameter with that handle, return null. 
	 */
	public PCMetadata getDeclaredParameter( int handle )
	{
		return this.parameters.get( handle );
	}

	/**
	 * Return the contained, declared parameter with the given name, or null if there is none.
	 * Inherited and child parameters are not considered.
	 */
	public PCMetadata getDeclaredParameter( String name )
	{
		for( PCMetadata parameter : parameters.values() )
		{
			if( parameter.getName().equals(name) )
				return parameter;
		}
		
		return null;
	}

	/**
	 * Return the number of parameters that are declared in this interaction alone (excluding
	 * any that are inherited).
	 */
	public int getDeclaredParameterCount()
	{
		return parameters.size();
	}
	
	/**
	 * Get the available parameter (inherited included) of this class for the given handle. If
	 * there is none for that handle (either local or inherited), null will be returned. 
	 */
	public PCMetadata getParameter( int handle )
	{
		// check for the parameter locally first
		if( this.parameters.containsKey(handle) )
		{
			// we have it
			return this.parameters.get( handle );
		}
		else
		{
			// we don't have it locally, check up the tree - if we can!
			if( this.parent == null )
			{
				return null;
			}
			else
			{
				return this.parent.getParameter( handle );
			}
		}
	}
	
	/**
	 * Return the handle of the contained parameter of the given name. If there is no parameter
	 * of that name in this class, ObjectModel.INVALID_HANDLE
	 * will be returned. <b>Note:</b> The search will include inherited parameters. 
	 */
	public int getParameterHandle( String name )
	{
		// check locally first
		for( PCMetadata temp : this.parameters.values() )
		{
			if( temp.getName().equals(name) )
			{
				return temp.getHandle();
			}
		}
		
		// didn't find it, check up the tree
		if( this.parent == null )
		{
			// no tree to check up
			return ObjectModel.INVALID_HANDLE;
		}
		else
		{
			return this.parent.getParameterHandle( name );
		}
		
	}
	
	/**
	 * Return the name of the contained parameter of the given handle. If there is no parameter
	 * of that handle in this class, null will be returned. <b>Note:</b> The search will include
	 * inherited parameters. 
	 */
	public String getParameterName( int handle )
	{
		if( this.parameters.containsKey(handle) )
		{
			return this.parameters.get(handle).getName();
		}
		
		// check inherited parameters
		if( this.parent == null )
		{
			return null;
		}
		else
		{
			return this.parent.getParameterName( handle );
		}
	}

	////////////////////////////////////////////////////////////
	//////////////////// Get and Set Methods ///////////////////
	////////////////////////////////////////////////////////////
	public int getHandle()
	{
		return handle;
	}

	/**
	 * Changes the handle of this class. To prevent external tampering, this
	 * is marked as protected and should not be called by anything except the
	 * model merger.
	 */
	protected void setHandle( int handle )
	{
		this.handle = handle;
	}

	/**
	 * Get the local portion of the interaction class name. For example, if the qualified name was
	 * "InteractionRoot.Surface.Start", the local name would be "Start". Note that the name this
	 * class should be given on creation IS its local name only (not its fully qualified version)
	 */
	public String getLocalName()
	{
		return name;
	}
	
	/**
	 * Return the fully-qualified name of this interaction class. This will recurse through all
	 * the parents and generate the full name.
	 */
	public String getQualifiedName()
	{
		// only calculate it if we don't already have it
		if( qualifiedName != null )
			return qualifiedName;
		
		if( parent == null )
		{
			// no parent, our full name is our name
			this.qualifiedName = name;
			return name;
		}
		else
		{
			// we have parents, get their name and append ours to the end
			this.qualifiedName = parent.getQualifiedName() + "." + name;
			return this.qualifiedName;
			//return parent.getQualifiedName() + "." + name;
		}
	}
	
	/**
	 * Gets the qualified name WITHOUT InteractionRoot or HLAinteractionRoot prefixed at the front.
	 * Thus, this name (while not 100% qualified) doesn't contain HLA version specific information. 
	 */
	public String getVersionSafeQualifiedName()
	{
		if( this.vsafeQualifiedName != null )
			return this.vsafeQualifiedName;
		
		if( parent == null )
		{
			// no parent, we must be the object root, don't return our name, it will contain
			// version-specific information
			this.vsafeQualifiedName = "";
			return "";
		}
		else
		{
			// we have a parent, get its name
			String parentQualified = parent.getVersionSafeQualifiedName();
			if( parentQualified.equals("") )
			{
				// our parent is object root, don't prefix it with "."
				this.vsafeQualifiedName = name;
				return name;
			}
			else
			{
				this.vsafeQualifiedName = parentQualified + "." + name;
				return parentQualified + "." + name;
			}
		}
	}

	
	public Order getOrder()
	{
		return order;
	}

	public void setOrder( Order order )
	{
		this.order = order;
	}

	public boolean isRO()
	{
		return this.order == Order.RECEIVE;
	}
	
	public boolean isTSO()
	{
		return this.order == Order.TIMESTAMP;
	}
	
	public ICMetadata getParent()
	{
		return parent;
	}

	public void setParent( ICMetadata parent )
	{
		// remove us from our old parent //
		if( this.parent != null )
			this.parent.children.remove( this );
		
		// clear the qualified name caches //
		this.qualifiedName = null;
		this.vsafeQualifiedName = null;
		
		// register us in the new parent //
		this.parent = parent;
		if( parent != null )
		{
			parent.children.add( this );
		}
	}

	public Transport getTransport()
	{
		return transport;
	}

	public void setTransport( Transport transport )
	{
		this.transport = transport;
	}

	public Sharing getSharing()
	{
		return sharing;
	}
	
	public void setSharing( Sharing sharing )
	{
		this.sharing = sharing;
	}
	
	public Space getSpace()
	{
		return this.space;
	}
	
	public void setSpace( Space space )
	{
		this.space = space;
	}

	public ObjectModel getModel()
	{
		return this.model;
	}
	
	public void setModel( ObjectModel model )
	{
		this.model = model;
	}

	public String toString()
	{
		if( PorticoConstants.USE_Q_NAMES )
			return getQualifiedName();
		else
			return "" + this.handle;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
