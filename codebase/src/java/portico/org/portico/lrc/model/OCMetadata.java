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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.io.Serializable;

import org.portico.lrc.PorticoConstants;

/**
 * This class contains metadata information about a FOM object class 
 */
public class OCMetadata implements Serializable
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
	private OCMetadata              parent;
	private Sharing                 sharing;
	private Map<Integer,ACMetadata> attributes;
	private Set<OCMetadata>         children;
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
	public OCMetadata( String name, int handle )
	{
		this.name        = name;
		this.handle      = handle;
		this.parent      = null;
		this.sharing	 = Sharing.NEITHER;
		this.attributes  = new HashMap<Integer,ACMetadata>();
		this.children    = new HashSet<OCMetadata>();
	}
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns a set of all the <b>direct</b> child types of this one.
	 */
	public Set<OCMetadata> getChildTypes()
	{
		return this.children;
	}

	/**
	 * Find the child object class with the given name (local part only, not
	 * qualified). If there is no class with that name, null is returned.
	 * 
	 * @param name The local name of the child class to find
	 * @return The child class of this class with the given name, or null if one doesn't exist
	 */
	public OCMetadata getChildType( String name )
	{
		for( OCMetadata child : this.children )
		{
			if( child.getLocalName().equals(name) )
				return child;
		}
		
		return null;
	}

	/**
	 * Return true if this type is a child type of the given class. If it is, it means that
	 * this type can be used as a more generic version of the given class.
	 * 
	 * @param other The class we want to know whether instances of this class could be assigned to
	 * @return True if this class is a parent of the given class, false otherwise
	 */
	public boolean isAssignableTo( OCMetadata other )
	{
		OCMetadata current = this;
		while( current != null )
		{
			if( current == other )
				return true;
			else
				current = current.getParent();
		}
		
		return false;
	}
	
	/**
	 * Creates a new instance of this type with an {@link ACInstance} for each attribute. The
	 * returned instance <i>will not have a handle or a name</i>. The owner of each and every
	 * attribute will be set to the handle of the creating federate.
	 */
	public OCInstance REMOVE_newInstance( int creatingFederate )
	{
		return REMOVE_newInstance( creatingFederate, null );
	}
	
	/**
	 * Creates a new instance of this type with an {@link ACInstance} for each attribute. The
	 * returned instance <i>will not have a handle or a name</i>. Only those attributes provided
	 * in the <code>publishedAttributes</code> parameter will have their owner set to the handle
	 * of the creating federate. The other handles will have no owner. If the given set of
	 * attributes is null or empty, *all* attributes will have their owner set to the handle of
	 * the creating federate.
	 * <p/>
	 * <b>NOTE:</b> If this method is called by a federate discovering a newly registered object,
	 *              but as a class that is a parent of that with which the object was registered,
	 *              then it is possible that some of the published attributes will be for handles
	 *              that are only relevant to the registration type and not the discovery type.
	 *              For this reason, if an attribute can't be found in the object class (when
	 *              attempting to set the ownership flag) it will be skipped, but no error will
	 *              be raised.
	 */
	public OCInstance REMOVE_newInstance( int creatingFederate, Set<Integer> publishedAttributes )
	{
		// check the given published attributes, if it is null, set it to a new empty
		// set (which will tell the later parts of the method to associate ownership of
		// all attributes to the creating federate
		if( publishedAttributes == null || publishedAttributes.isEmpty() )
			publishedAttributes = getAllAttributeHandles();
		
		// create the instance
		OCInstance instance = new OCInstance();
		instance.setRegisteredType( this );
		instance.setDiscoveredType( this );

		// add an ACInstance for ALL attributes, only set the ownership on ones that we publish
		for( ACMetadata attributeMetadata : getAllAttributes() )
		{
			ACInstance attributeInstance = attributeMetadata.newInstance();
			if( publishedAttributes.contains(attributeMetadata.getHandle()) )
				attributeInstance.setOwner( creatingFederate );
			instance.addAttribute( attributeInstance );
		}
		
		return instance;
	}
	
	/**
	 * This method will cause this object class to remove any links between it and the object
	 * model to which it is currently attached. This includes removing itself from theh store
	 * of "child" classes in its parent and setting its object model reference to null. The
	 * overall effect of this method is to cleave the object class from the object model.
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
	///////////////////// Attribute Methods ////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Adds the given attribute to this object class. If the name of the attribute is the same
	 * as one already in this class, the request will be ignored and false will be returned.
	 * Otherwise, the attribute will be added and true will be returned.
	 */
	public boolean addAttribute( ACMetadata attribute )
	{
		String name = attribute.getName();
		
		// check to see if we already have an attribute with the same name
		for( ACMetadata temp : attributes.values() )
		{
			if( temp.getName().equals(name) )
			{
				return false;
			}
		}
		
		// no existing attribute, throw this one in
		this.attributes.put( attribute.getHandle(), attribute );
		// assign the container property of the attribute to us
		attribute.setContainer( this );
		return true;
	}
	
	/**
	 * Remove and return the local attribute of the given handle (not inherited). If there is no
	 * attribute by that handle, null will be returned. 
	 */
	public ACMetadata removeAttribute( int handle )
	{
		ACMetadata attribute = this.attributes.remove( handle );
		if( attribute == null )
		{
			return null;
		}
		else
		{
			attribute.setContainer( null );
			return attribute;
		}
	}
	
	/**
	 * Return a set of all the attributes contained directly within this class (no inherited ones) 
	 */
	public Set<ACMetadata> getDeclaredAttributes()
	{
		return new HashSet<ACMetadata>( this.attributes.values() );
	}
	
	/**
	 * Return a set of all available attributes for this class (inherited included) 
	 */
	public Set<ACMetadata> getAllAttributes()
	{
		// if we don't have parent, all our attributes are just our local ones
		if( this.parent == null )
		{
			return this.getDeclaredAttributes();
		}
		else
		{
			// we do have a parent, we have to add our local attributes to its set
			Set<ACMetadata> inherited = this.parent.getAllAttributes();
			// add our local attributes
			inherited.addAll( this.attributes.values() );
			// return the complete set
			return inherited;
		}
	}

	public ACMetadata getPrivilegeToDelete()
	{
		return model.getPrivileteToDeleteMetaClass();
	}
	
	/**
	 * Return a set of attribute names for all the attributes of the given handles. If any of the
	 * handles do not correspond to attributes that exist in this class (or are inherited), that
	 * attribute will be ignored and no value for it included in the set. If the given set of
	 * handles is null or empty (or doesn't represent any valid values) an empty set of names will
	 * be returned.
	 */
	public Set<String> getAttributeNames( Collection<Integer> attributeHandles )
	{
		HashSet<String> names = new HashSet<String>();
		for( Integer attributeHandle : attributeHandles )
			names.add( getAttributeName(attributeHandle) );
		
		return names;
	}
	
	/**
	 * Return a set of attribute metadata for all the attributes of the given handles. If any of the
	 * handles do not correspond to attributes that exist in this class (or are inherited), that
	 * attribute will be ignored and no value for it included in the set. If the given set of
	 * handles is null or empty (or doesn't represent any valid values) an empty set will be
	 * returned.
	 */
	public Set<ACMetadata> getAttributeMetadata( Collection<Integer> attributeHandles )
	{
		HashSet<ACMetadata> metadata = new HashSet<ACMetadata>();
		for( Integer attributeHandle : attributeHandles )
			metadata.add( attributes.get(attributeHandle) );
		
		return metadata;
	}
	
	/**
	 * Return a set of all available attribute handles for this object class (inherited included)
	 */
	public Set<Integer> getAllAttributeHandles()
	{
		// if we don't have parent, all our attributes are just our local ones
		if( this.parent == null )
		{
			return new HashSet<Integer>( attributes.keySet() );
		}
		else
		{
			// we do have a parent, we have to add our local attributes to its set
			Set<Integer> inherited = this.parent.getAllAttributeHandles();
			// add our local attributes
			inherited.addAll( attributes.keySet() );
			// return the complete set
			return inherited;
		}
	}
	
	/**
	 * Get the locally declared attribute (not inherited) of the given handle and return it. If
	 * there is no such attribute with that handle, return null. 
	 */
	public ACMetadata getDeclaredAttribute( int handle )
	{
		return this.attributes.get( handle );
	}

	/**
	 * Get the locally declared attribute (not inherited) of the given handle and return it. If
	 * there is no such attribute with that handle, return null. 
	 */
	public ACMetadata getDeclaredAttribute( String name )
	{
		for( ACMetadata attribute : attributes.values() )
		{
			if( attribute.getName().equals(name) )
				return attribute;
		}
		
		return null;
	}
	
	/**
	 * Get the available attribute (inherited included) of this class for the given handle. If
	 * there is none for that handle (either local or inherited), null will be returned. 
	 */
	public ACMetadata getAttribute( int handle )
	{
		// check for the attribute locally first
		if( this.attributes.containsKey(handle) )
		{
			// we have it
			return this.attributes.get( handle );
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
				return this.parent.getAttribute( handle );
			}
		}
	}
	
	/**
	 * Return the handle of the contained attribute of the given name. If there is no attribute
	 * of that name in this class, ObjectModel.INVALID_HANDLE
	 * will be returned. <b>Note:</b> The search will include inherited attributes.
	 * <p/>
	 */
	public int getAttributeHandle( String name )
	{
		// check locally first
		for( ACMetadata temp : this.attributes.values() )
		{
			if( temp.getName().equals(name) )
			{
				return temp.getHandle();
			}
		}
		
		// didn't find it, check up the tree
		if( this.parent == null )
		{
			// there is nothing higher to check, ensure that we're not talking about privToDelete,
			// if we haven't found it yet it might because we've got the wrong HLA version
			if( name != null &&
				(name.equals("privilegeToDelete") || name.equals("HLAprivilegeToDeleteObject")) )
			{
				return this.model.getPrivilegeToDelete();
			}
			else
			{
				return ObjectModel.INVALID_HANDLE;
			}
		}
		else
		{
			return this.parent.getAttributeHandle( name );
		}
	}
	
	/**
	 * Return the name of the contained attribute of the given handle. If there is no attribute
	 * of that handle in this class, null will be returned. <b>Note:</b> The search will include
	 * inherited attributes. 
	 */
	public String getAttributeName( int handle )
	{
		// check locally first
		if( this.attributes.containsKey(handle) )
		{
			return this.attributes.get(handle).getName();
		}
		
		// it's not local, check up the tree
		if( this.parent == null )
		{
			// we are at the top of the tree, return null
			return null;
		}
		else
		{
			return this.parent.getAttributeName( handle );
		}
	}

	/**
	 * Returns <code>true</code> if this class contains an attribute of the given handle. This
	 * method will also search the inherited attributes.
	 */
	public boolean hasAttribute( int handle )
	{
		if( this.attributes.containsKey(handle) )
			return true;
		else if( parent != null )
			return parent.hasAttribute( handle );
		else
			return false;
	}

	/**
	 * @return The number of attributes declared in this class alone (no inherited or
	 *         child class attributes counted).
	 */
	public int getDeclaredAttributeCount()
	{
		return attributes.size();
	}

	////////////////////////////////////////////////////////////
	//////////////////// Get and Set Methods ///////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Get the handle for this class
	 */
	public int getHandle()
	{
		return this.handle;
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
	 * Get the local portion of the object class name. For example, if the qualified name was
	 * "ObjectRoot.Surface.Car", the local name would be "Car". Note that the name this class
	 * should be given on creation IS its local name only (not its fully qualified version)
	 */
	public String getLocalName()
	{
		return name;
	}
	
	/**
	 * Return the fully-qualified name of this object class. This will recurse through all
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
	 * Gets the qualified name WITHOUT ObjectRoot or HLAobjectRoot prefixed at the front. Thus,
	 * this name (while not 100% qualified) doesn't contain HLA version specific information. 
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
	
	/**
	 * Return the parent of this object class 
	 */
	public OCMetadata getParent()
	{
		return this.parent;
	}
	
	/**
	 * Set the parent of this class 
	 */
	public void setParent( OCMetadata oc )
	{
		// remove us from the old parent
		if( this.parent != null )
			this.parent.children.remove( this );
		
		// clear the qualified name caches //
		this.qualifiedName = null;
		this.vsafeQualifiedName = null;
		
		// register us with the new parent //
		this.parent = oc;
		if( oc != null )
		{
			oc.children.add( this );
		}
	}
	
	public Sharing getSharing()
	{
		return sharing;
	}
	
	public void setSharing( Sharing sharing )
	{
		this.sharing = sharing;
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
			return getQualifiedName() + " " + this.getAllAttributes();
		else
			return "" + this.handle + " " + this.getAllAttributes();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
