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

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import org.portico.impl.HLAVersion;

/**
 * This class represents a HLA FOM. It contains a set of object and interaction classes (routing
 * spaces not yet supported) which can be fetch via handle. It also contains links to the object
 * and interaction roots of the model. In order to provide support for the notion of a dynamic-FOM,
 * instances can be modified at any time <b>unless</b> the model has been locked with a call to the
 * {@link #lock() lock()} method. After this call, the model will become static and function as
 * normally expected within the HLA (note: models can be unlocked). 
 */
public class ObjectModel implements Serializable
{
	//----------------------------------------------------------
	//                      ENUMERATIONS
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;
	
	public static final int INVALID_HANDLE = -1;
	
	/** The maximum handle value for MOM data */
	public static final int MAX_MOM_HANDLE = 500;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private int handle = MAX_MOM_HANDLE;
	
	private String filename;

	private HLAVersion version;
	private boolean locked;
	private Map<Integer,OCMetadata> oclasses;
	private Map<Integer,ICMetadata> iclasses;
	private Map<Integer,Space> spaces;
	private OCMetadata ocroot;
	private ICMetadata icroot;
	
	private int privilegeToDelete; // set with object root is set
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Creates a new instance with the default of Version.V1_3
	 */
	public ObjectModel()
	{
		this.oclasses = new HashMap<Integer,OCMetadata>();
		this.iclasses = new HashMap<Integer,ICMetadata>();
		this.spaces   = new HashMap<Integer,Space>();
		this.locked   = false;
		this.ocroot   = null;
		this.icroot   = null;
		this.version  = HLAVersion.HLA13;
		
		this.privilegeToDelete = INVALID_HANDLE;
	}
	
	/**
	 * Creates a new instance with the given version 
	 */
	public ObjectModel( HLAVersion version )
	{
		this();
		this.version  = version;
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public void setFileName( String name )
	{
		this.filename = name;
	}
	
	public String getFileName()
	{
		return this.filename;
	}
	
	public HLAVersion getHlaVersion()
	{
		return this.version;
	}

	/////////////////////////////////////////////////////////////
	/////////////////////// Space Methods ///////////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Fetch the space contained in this model that has the given handle and return it. If the
	 * space doesn't exist, return null.
	 */
	public Space getSpace( int spaceHandle )
	{
		return this.spaces.get( spaceHandle );
	}
	
	/**
	 * Fetch the space contained in this model that has the given name and return it. If the
	 * space doesn't exist, return null.
	 */
	public Space getSpace( String name )
	{
		for( Space temp : this.spaces.values() )
		{
			if( temp.getName().equalsIgnoreCase(name) )
				return temp;
		}
		
		// we didn't find it
		return null;
	}
	
	/**
	 * Returns the collection of all spaces declared in the FOM
	 */
	public Collection<Space> getAllSpaces()
	{
		return this.spaces.values();
	}

	/**
	 * Add the given {@link Space} to this model. If a space with the same handle already
	 * exists, it will be overwritten with this new space. <i>If the model has been locked, this
	 * request will be ignored.</i>
	 */
	public void addSpace( Space space )
	{
		// make sure we're not locked
		if( space == null || this.locked )
		{
			return;
		}
		
		// add it
		this.spaces.put( space.getHandle(), space );
		space.setModel( this );
	}

	
	/////////////////////////////////////////////////////////////
	//////////////////// ObjectClass Methods ////////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Fetch the object class of the given handle in this model. If there is none for the handle,
	 * null will be returned.
	 */
	public OCMetadata getObjectClass( int handle )
	{
		return this.oclasses.get( handle );
	}
	
	/**
	 * Fetch the object class with the given name and return it. If none is found, null is returned.
	 * This method will take into account version differences, thus, you can use ObjectRoot or
	 * HLAobjectRoot in the name and it will make no difference. 
	 */
	public OCMetadata getObjectClass( String name )
	{
		// names are meant to be case-insensitive
		name = name.toLowerCase();
		
		//////////////////////////////////////////////////////////////////////////////
		// Because of the differences in FOM names between HLA 1.3 and HLA 1516 we  //
		// have to make allowances. Thus, to avoid ANY possible problems, we will   //
		// make all class name assessments by taking the first portion of qualified //
		// names off and ignoring it (as this is the part with the difference).     //
		//////////////////////////////////////////////////////////////////////////////

		// cut off any HLA-version specific notations on the front
		if( name.startsWith("objectroot.") )
			name = name.substring(11);
		else if( name.startsWith("hlaobjectroot.") )
			name = name.substring(14);
		
		// check for the qualified version of the name first, but using version-safe qualified
		for( OCMetadata oc : this.oclasses.values() )
		{
			if( oc.getVersionSafeQualifiedName().equalsIgnoreCase(name) )
			{
				return oc;
			}
		}

		// we didn't find it in the version safe stuff, check the name by itself
		// FIXME: Check to see if this is actually defined in the spec, it might just be that
		//        the ObjectRoot bit is optional, not the entire prefix
		for( OCMetadata oc : this.oclasses.values() )
		{
			if( oc.getLocalName().equalsIgnoreCase(name) )
			{
				return oc;
			}
		}
		
		// make sure that we haven't got the root name itself, if we get this far without finding
		// anything, we may well have it
		if( name.equalsIgnoreCase("objectroot") || name.equalsIgnoreCase("hlaobjectroot") )
			return this.getObjectRoot();
		
		// the only other thing that could have gotten missed is a MOM class
		// below will return null if it isn't a MOM class
		return this.getObjectClass( Mom.getMomClassHandle(name) );
	}
	
	/**
	 * This method will fetch the {@link ACMetadata} for the attribute of the given name in the
	 * class of the given handle. If the name doesn't represent a valid attribute, or the handle
	 * doesn't represent a valid class, null will be returned.
	 */
	public ACMetadata getAttributeClass( int classHandle, String attributeName )
	{
		// find the class //
		OCMetadata ocMetadata = this.oclasses.get( classHandle );
		// do we have a class?
		if( ocMetadata == null )
		{
			return null;
		}
		
		// find the attribute //
		// Is this a MOM class? //
		if( classHandle < MAX_MOM_HANDLE )
		{
			// make sure we aren't talking privilegeToDelete
			if( attributeName.equals("privilegeToDelete") ||
				attributeName.equals("HLAprivilegeToDelete") )
			{
				return this.ocroot.getDeclaredAttribute( this.privilegeToDelete );
			}
			
			// it sure is, do a special lookup because of the requirement to map names
			// depending on the HLA version involved
			int aHandle = Mom.getMomAttributeHandle( classHandle, attributeName );
			return ocMetadata.getAttribute( aHandle );
		}
		else
		{
			// it isn't, we can just look directly in the class
			int aHandle = ocMetadata.getAttributeHandle( attributeName );
			return ocMetadata.getAttribute( aHandle );
		}
	}
	
	/**
	 * Get the class that represents ObjectRoot (or HLAObjectRoot in 1516). If it has not yet
	 * been set, null will be returned. 
	 */
	public OCMetadata getObjectRoot()
	{
		return this.ocroot;
	}
	
	/**
	 * Designate the given class as the object root of this FOM. This request will be
	 * <b>ignored</b> if the model has been {@link #lock() lock()}'ed. This request will also be
	 * ignored if the fully qualified name of the class is not "ObjectRoot" (for 1.3) or
	 * "HLAObjectRoot" (for 1516).
	 */
	public void setObjectRoot( OCMetadata root )
	{
		// make sure we're right to go
		if( root == null || this.locked )
		{
			return;
		}
		
		// check that the name is valid
		if( this.version == HLAVersion.HLA13 )
		{
			if( root.getQualifiedName().equals("ObjectRoot") )
			{
				this.ocroot = root;
				// set the handle for privilegeToDelete
				this.privilegeToDelete = root.getAttributeHandle( "privilegeToDelete" );
			}
		}
		else
		{
			if( root.getQualifiedName().equals("HLAobjectRoot") )
			{
				this.ocroot = root;
				// set the handle for privilegeToDelete
				this.privilegeToDelete = root.getAttributeHandle( "HLAprivilegeToDeleteObject" );
			}
		}
	}
	
	/**
	 * Get a set of all the object classes currently contained within this FOM.
	 */
	public Set<OCMetadata> getAllObjectClasses()
	{
		return new HashSet<OCMetadata>( this.oclasses.values() );
	}
	
	/**
	 * Add the given object class to this model. If a class with the same handle already exists, it
	 * will be overwritten with this new class. <i>If the model has been locked, this request will
	 * be ignored.</i>
	 */
	public void addObjectClass( OCMetadata oc )
	{
		// make sure we're not locked
		if( oc == null || this.locked )
		{
			return;
		}
		
		// add it
		this.oclasses.put( oc.getHandle(), oc );
		oc.setModel( this );
	}
	
	/**
	 * Remove and return the given object class from this model. If the class does not exist, null
	 * will be returned. <i>If the model has been locked, this request will be ignored.</i> 
	 */
	public OCMetadata removeObjectClass( int handle )
	{
		OCMetadata removed = this.oclasses.remove( handle );
		// set the model to null
		if( removed != null )
		{
			removed.setModel( null );
		}
		
		return removed;
	}
	
	/**
	 * Get the handle of the class with the given name. If no such class exists in this model,
	 * {@link #INVALID_HANDLE INVALID_HANDLE} will be returned.
	 */
	public int getObjectClassHandle( String name )
	{
		OCMetadata metadata = this.getObjectClass( name );
		if( metadata == null )
		{
			// couldn't find it, return the dud
			return INVALID_HANDLE;
		}
		else
		{
			return metadata.getHandle();
		}
	}
	
	/**
	 * Get the name of the object class represented by the given handle in this model. If the
	 * handle does not represent a class, null will be returned. 
	 */
	public String getObjectClassName( int handle )
	{
		if( this.oclasses.containsKey(handle) )
		{
			return this.oclasses.get(handle).getQualifiedName();
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * This method will scan EVERY OBJECT CLASS in the FOM until it finds the attribute with the
	 * given handle. It will then return that attributes name. Note that depending on the size of
	 * the FOM, this could be *VERY* slow. If no attribute for the handle can be found, the string
	 * "&lt;unknown&gt;" will be returned.
	 */
	public String findAttributeName( int attributeHandle )
	{
		for( OCMetadata objectClass : this.oclasses.values() )
		{
			ACMetadata attributeClass = objectClass.getDeclaredAttribute( attributeHandle );
			if( attributeClass != null )
				return attributeClass.getName();
		}
		
		return attributeHandle+" <unknown>";
	}

	/**
	 * This method will get the handle of the privilege to delete attribute. As the standards
	 * board changed the name of the attribute from 1.3 to 1516 (for no other good reason than
	 * to cause everyone pain), this method will take version into account. A user should
	 * <b>NEVER</b> try and get privilegeToDelete or HLAprivilegeToDeleteObject by name from
	 * wihin a handler (or any part of the RTI).
	 *
	 * @return The handle for the privilege to delete attribute or {@link #INVALID_HANDLE} if
	 * there is no currently set object root.
	 */
	public int getPrivilegeToDelete()
	{
		return this.privilegeToDelete;
	}
	/////////////////////////////////////////////////////////////
	////////////////// InteractionClass Methods /////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Fetch the object class of the given handle in this model. If there is none for the handle,
	 * null will be returned.
	 */
	public ICMetadata getInteractionClass( int handle )
	{
		return this.iclasses.get( handle );
	}
	
	/**
	 * Find the interaction class with the given name and return it. If there is none with that
	 * name, null will be returned. 
	 */
	public ICMetadata getInteractionClass( String name )
	{
		// names are meant to be case-insensitive
		name = name.toLowerCase();

		//////////////////////////////////////////////////////////////////////////////
		// Because of the differences in FOM names between HLA 1.3 and HLA 1516 we  //
		// have to make allowances. Thus, to avoid ANY possible problems, we will   //
		// make all class name assessments by taking the first portion of qualified //
		// names off and ignoring it (as this is the part with the difference)      //
		//////////////////////////////////////////////////////////////////////////////

		// cut off any HLA-version specific notations on the front
		if( name.startsWith("interactionroot.") )
			name = name.substring(16);
		else if( name.startsWith("hlainteractionroot.") )
			name = name.substring(19);
		
		// check for the qualified version of the name first, but using version-safe qualified
		for( ICMetadata ic : this.iclasses.values() )
		{
			if( ic.getVersionSafeQualifiedName().equalsIgnoreCase(name) )
			{
				return ic;
			}
		}

		// we didn't find it in the version safe stuff, check the name by itself
		// FIXME: Check to see if this is actually defined in the spec, it might just be that
		//        the ObjectRoot bit is optional, not the entire prefix
		for( ICMetadata ic : this.iclasses.values() )
		{
			if( ic.getLocalName().equalsIgnoreCase(name) )
			{
				return ic;
			}
		}
		
		// make sure that we haven't got the root name itself, if we get this far without finding
		// anything, we may well have it
		if( name.equalsIgnoreCase("InteractionRoot") ||
			name.equalsIgnoreCase("HLAinteractionRoot") )
			return this.getInteractionRoot();

		// we didn't find the name, return null
		return null;
	}
	
	/**
	 * Get the class that represents InteractionRoot (or HLAInteractionRoot in 1516). If it has not
	 * yet been set, null will be returned. 
	 */
	public ICMetadata getInteractionRoot()
	{
		return this.icroot;
	}
	
	/**
	 * Designate the given class as the interaction root of this FOM. This request will be
	 * <b>ignored</b> if the model has been {@link #lock() lock()}'ed. This request will also be
	 * ignored if the fully qualified name of the class is not "InteractionRoot" (for 1.3) or
	 * "HLAInteractionRoot" (for 1516).
	 */
	public void setInteractionRoot( ICMetadata root )
	{
		// make sure we're right to go
		if( root == null || this.locked )
		{
			return;
		}
		
		// check that the name is valid
		if( this.version == HLAVersion.HLA13 )
		{
			if( root.getQualifiedName().equals("InteractionRoot") )
			{
				this.icroot = root;
			}
		}
		else
		{
			if( root.getQualifiedName().equals("HLAinteractionRoot") )
			{
				this.icroot = root;
			}
		}
	}
	
	/**
	 * Get a set of all the interaction classes currently contained within this FOM.
	 */
	public Set<ICMetadata> getAllInteractionClasses()
	{
		return new HashSet<ICMetadata>( this.iclasses.values() );
	}
	
	/**
	 * Add the given interaction class to this model. If a class with the same handle already
	 * exists, it will be overwritten with this new class. <i>If the model has been locked, this
	 * request will be ignored.</i>
	 */
	public void addInteractionClass( ICMetadata ic )
	{
		// make sure we're not locked
		if( ic == null || this.locked )
		{
			return;
		}
		
		// add it
		this.iclasses.put( ic.getHandle(), ic );
		ic.setModel( this );
	}
	
	/**
	 * Remove and return the given interaction class from this model. If the class does not exist,
	 * null will be returned. <i>If the model has been locked, this request will be ignored.</i> 
	 */
	public ICMetadata removeInteractionClass( int handle )
	{
		return this.iclasses.remove( handle );
	}
	
	/**
	 * Get the handle of the class with the given name. If no such class exists in this model,
	 * {@link #INVALID_HANDLE INVALID_HANDLE} will be returned.
	 */
	public int getInteractionClassHandle( String name )
	{
		ICMetadata metadata = this.getInteractionClass( name );
		if( metadata == null )
		{
			// couldn't find it, return the dud
			return INVALID_HANDLE;
		}
		else
		{
			return metadata.getHandle();
		}
	}
	
	/**
	 * Get the name of the interaction class represented by the given handle in this model. If the
	 * handle does not represent a class, null will be returned. 
	 */
	public String getInteractionClassName( int handle )
	{
		if( this.iclasses.containsKey(handle) )
		{
			return this.iclasses.get(handle).getQualifiedName();
		}
		else
		{
			return null;
		}
	}

	/**
	 * This method will scan EVERY INTERACTION CLASS in the FOM until it finds the parameter with
	 * the given handle. It will then return that parameter's name. Note that depending on the size
	 * of the FOM, this could be *VERY* slow. If no parameter for the handle is found, the string
	 * "&lt;unknown&gt;" will be returned.
	 */
	public String findParameterName( int parameterHandle )
	{
		for( ICMetadata interactionClass : this.iclasses.values() )
		{
			PCMetadata parameterClass = interactionClass.getDeclaredParameter( parameterHandle );
			if( parameterClass != null )
				return parameterClass.getName();
		}
		
		return "<unknown>";
	}

	/////////////////////////////////////////////////////////////
	//////////////////// Dynamic FOM Methods ////////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Locks this model so that changes can no longer be made. 
	 */
	public synchronized void lock()
	{
		this.locked = true;
	}
	
	/**
	 * Unlocks this model so that changes can be made once again 
	 */
	public synchronized void unlock()
	{
		this.locked = false;
	}
	
	/////////////////////////////////////////////////////////////
	////////////////// Creation/Handle Methods //////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Creates a new {@link OCMetadata OCMetadata} instance with the given name and a generaetd
	 * handle.
	 */
	public OCMetadata newObject( String name )
	{
		return new OCMetadata( name, generateHandle() );
	}
	
	/**
	 * Creates a new {@link ACMetadata ACMetadata} instance with the given name and a generated
	 * handle.
	 */
	public ACMetadata newAttribute( String name )
	{
		return new ACMetadata( name, generateHandle() );
	}
	
	/**
	 * Creates a new {@link ICMetadata ICMetadata} instance with the given name and a generaetd
	 * handle.
	 */
	public ICMetadata newInteraction( String name )
	{
		return new ICMetadata( name, generateHandle() );
	}
	
	/**
	 * Creates a new {@link PCMetadata PCMetadata} instance with the given name and a generated
	 * handle.
	 */
	public PCMetadata newParameter( String name )
	{
		return new PCMetadata( name, generateHandle() );
	}

	/**
	 * Creates a new {@link Space} instance with the given name and generates a FOM-unique
	 * handle for it.
	 */
	public Space newSpace( String name )
	{
		return new Space( name, generateHandle() );
	}

	/**
	 * Creates a new {@link Dimension} instance with the given name and generates a
	 * FOM-unique handle for it.
	 */
	public Dimension newDimension( String name )
	{
		return new Dimension( name, generateHandle() );
	}

	protected synchronized int generateHandle()
	{
		return ++handle;
	}
	
	public String toString()
	{
		return filename;
		//return new StringRenderer().renderFOM( this );
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	/////////////////////////////////////////////////////////////
	///////////// Temporary Methods : DO NOT USE!!! /////////////
	/////////////////////////////////////////////////////////////
	/**
	 * <b>DO NOT USE!!!</b>
	 * <p/>
	 * This method will take the given model, rip out any MOM classes and then insert a bunch of
	 * MOM specific classes that conform to the structure/handles we expect. Use of this method is
	 * <i>a temporary solution for PORT-313</i>. I reserve the right to remove it at any point in
	 * time, without warning, hinting or even a cryptic blog post that might hint towards a
	 * potential impending action which may or may not pertain to the removal of said method. This
	 * won't ever be marked deprecated, rather, I'll just go all "preemptive strike" on it. One day
	 * you WILL wake up and it WILL just be gone.
	 */
	public static void mommify( ObjectModel model )
	{
		// let the abomonation begin //
		boolean wasLocked = model.locked;
		if( wasLocked )
			model.unlock();
		
		/////////////////////////////////////////////////////////////
		// remove any MOM stuff that currently exists in the model //
		/////////////////////////////////////////////////////////////
		String managerName = "ObjectRoot.Manager";
		if( model.version == HLAVersion.IEEE1516 )
		{
			managerName = "HLAobjectRoot.HLAmanager";
		}
		
		OCMetadata ocManager = model.getObjectClass( managerName );
		if( ocManager != null )
		{
			// we have MOM stuff, nurse, pass my cleaver
			ocManager.cleave();
			model.oclasses.remove( ocManager.getHandle() );
			
			// remove all its children from the model //
			// have to create a separate set to avoid a ConcurrentModificationException
			Set<OCMetadata> children = new HashSet<OCMetadata>( ocManager.getChildTypes() );
			for( OCMetadata clazz : children )
			{
				clazz.cleave();
				model.oclasses.remove( clazz.getHandle() );
			}
		}
		
		// do the same for any MOM interactions //
		// FIXME still need to add this //

		//////////////////////////////////
		// add the predefined MOM stuff //
		//////////////////////////////////
		Mom.insertMomHierarchy( model );
		
		// lock the model again //
		if( wasLocked )
			model.lock();
	}
	
}
