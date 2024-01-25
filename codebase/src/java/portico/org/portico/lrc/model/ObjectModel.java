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
import java.io.StringWriter;
import java.util.Collection;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import org.portico.impl.HLAVersion;
import org.portico.lrc.compat.JInconsistentFDD;
import org.portico.lrc.model.datatype.DatatypeHelpers;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.lrc.model.datatype.linker.DatatypePlaceholder;
import org.portico.lrc.model.datatype.linker.Linker;
import org.portico.lrc.model.datatype.linker.LinkerException;
import org.w3c.dom.Document;

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
	private Map<String,IDatatype> datatypes;
	private Map<Integer,OCMetadata> oclasses;
	private Map<Integer,ICMetadata> iclasses;
	private Map<Integer,Space> spaces;
	private OCMetadata ocroot;
	private ICMetadata icroot;
	
	private int privilegeToDelete; // set when object root is set
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Creates a new instance with the default of Version.V1_3
	 */
	public ObjectModel()
	{
		this.datatypes = new HashMap<String,IDatatype>();
		this.oclasses = new HashMap<Integer,OCMetadata>();
		this.iclasses = new HashMap<Integer,ICMetadata>();
		this.spaces   = new HashMap<Integer,Space>();
		this.locked   = false;
		this.ocroot   = null;
		this.icroot   = null;
		this.version  = HLAVersion.HLA13;
		
		this.privilegeToDelete = INVALID_HANDLE;
		DatatypeHelpers.injectStandardDatatypes( this );
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
	////////////////////// Datatype Methods /////////////////////
	/////////////////////////////////////////////////////////////
	/**
	 * Add the given object class to this model. <i>If the model has been locked, or the datatype 
	 * already exists, this request will be ignored.</i>
	 */
	public void addDatatype( IDatatype datatype )
	{
		// Placeholder types are not allowed to be inserted directly into the ObjectModel
		if( datatype instanceof DatatypePlaceholder )
			throw new IllegalArgumentException( "datatype is a placeholder" );
		
		String name = datatype.getName().toLowerCase();
		
		// make sure we're not locked and that the datatype doesn't already exist
		if( datatype == null || this.locked || this.datatypes.containsKey(name) )
			return;
		
		// add it
		this.datatypes.put( name, datatype );
	}
	
	/**
	 * Returns whether this model contains a definition for a data type with the specified name.
	 */
	public boolean containsDatatype( String name )
	{
		String nameLower = name.toLowerCase();
		return this.datatypes.containsKey( nameLower );
	}
	
	/**
	 * Fetch the datatype contained in this model that has the given name and return it. If the
	 * datatype doesn't exist, return null.
	 */
	public IDatatype getDatatype( String name )
	{
		String nameLower = name.toLowerCase();
		return this.datatypes.get( nameLower );
	}
	
	/**
	 * Returns the set of all datatypes that are contained in this model.
	 */
	public Set<IDatatype> getDatatypes()
	{
		return new HashSet<IDatatype>( this.datatypes.values() );
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
		return this.getObjectClass( Mom.getMomObjectClassHandle(version,name) );
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
				attributeName.equals("HLAprivilegeToDeleteObject") )
			{
				return this.ocroot.getDeclaredAttribute( this.privilegeToDelete );
			}
			
			// it sure is, do a special lookup because of the requirement to map names
			// depending on the HLA version involved
			int aHandle = Mom.getMomAttributeHandle( version, classHandle, attributeName );
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
		
		// If the new ocroot has no privilegeToDelete, then make sure it is added
		this.addPrivilegeToDeleteIfNotPresent();
	}

	/**
	 * For 1516e FOMs we have a problem. The spec mandates that classes can only extend other
	 * classes, they cannot add attributed to existing classes. This is fine for all classes
	 * EXCEPT `ObjectRoot`. On create we are given a bunch of FOM modules. For us to get a valid
	 * `privToDelete`, it must be explicitly declared inside the first module. Subsequent ones
	 * cannot declare it because that would be adding to `ObjectRoot`.
	 * 
	 * What actually happens in reality is that no module declares the parameter (who knows which
	 * one may be first in any given situation?). Net result: no `privToDelete`. As such, we have
	 * to patch it in after the fact. That is what this method does. It also ensures that our
	 * cached copy of the handle is updated to reflect the new handle.
	 */
	public void addPrivilegeToDeleteIfNotPresent()
	{
		String name = this.version == HLAVersion.HLA13 ? "privilegeToDelete" : 
		                                                 "HLAprivilegeToDeleteObject";
		ACMetadata temp = ocroot.getDeclaredAttribute( name );
		if( temp == null )
		{
			// TODO: This HLAprivilegeToDelete is NA in 1516 but HLAtoken in 1516e. This method
			// looks to only ever called for 1516e foms, so I've hardcoded it for HLAtoken 
			temp = this.newAttribute( name, getDatatype("HLAtoken") );
			ocroot.addAttribute( temp );
			this.privilegeToDelete = temp.getHandle();
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
	
	/**
	 * This method will get the ACMetadata for the privilege to delete attribute. As the standards
	 * board changed the name of the attribute from 1.3 to 1516 (for no other good reason than
	 * to cause everyone pain), this method will take version into account. A user should
	 * <b>NEVER</b> try and get privilegeToDelete or HLAprivilegeToDeleteObject by name from
	 * wihin a handler (or any part of the RTI).
	 *
	 * @return The metadata type for the privilege to delete attribute or null if there is
	 *         currently no object root set.
	 */
	public ACMetadata getPrivileteToDeleteMetaClass()
	{
		return ocroot.getDeclaredAttribute( this.privilegeToDelete );
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

		// the only other thing that could have gotten missed is a MOM class
		// below will return null if it isn't a MOM class
		return this.getInteractionClass( Mom.getMomInteractionHandle(version,name) );
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
	 * Creates a new {@link OCMetadata} instance with the given name and a generated
	 * handle.
	 * <p/>
	 * <b>Note:</b> This method will automatically associate the created {@link OCMetadata} with 
	 * this Object Model
	 */
	public OCMetadata newObject( String name )
	{
		OCMetadata object = new OCMetadata( name, generateHandle() );
		object.setModel( this );
		
		return object;
	}
	
	/**
	 * Creates a new {@link ACMetadata ACMetadata} instance with the given name, type and a 
	 * generated handle.
	 */
	public ACMetadata newAttribute( String name, IDatatype datatype )
	{
		return new ACMetadata( name, datatype, generateHandle() );
	}
	
	/**
	 * Creates a new {@link ICMetadata ICMetadata} instance with the given name and a generaetd
	 * handle.
	 * <p/>
	 * <b>Note:</b> This method will automatically associate the created {@link ICMetadata} with 
	 * this Object Model
	 */
	public ICMetadata newInteraction( String name )
	{
		ICMetadata interaction = new ICMetadata( name, generateHandle() );
		interaction.setModel( this );
		
		return interaction;
	}
	
	/**
	 * Creates a new {@link PCMetadata PCMetadata} instance with the given name and a generated
	 * handle.
	 */
	public PCMetadata newParameter( String name, IDatatype datatype )
	{
		return new PCMetadata( name, datatype, generateHandle() );
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
		return new StringRenderer().renderFOM( this );
	}
	
	public String toXmlDocument()
	{
		Document xml = new XmlRenderer().renderFOM( this ); 
		StringWriter writer = new StringWriter();
		
		try
		{
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute("indent-number", Integer.valueOf(4));			
			Transformer transformer = tf.newTransformer();			
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");	
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");			
			transformer.transform(new DOMSource(xml), new StreamResult(writer));
		}
		catch(Exception e)
		{
			throw new IllegalStateException(e);
		}						 
		
		return writer.toString();
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
		String objectManagerName = "ObjectRoot.Manager";
		String interactionManagerName = "InteractionRoot.Manager";
		if( model.version == HLAVersion.IEEE1516 || model.version == HLAVersion.IEEE1516e )
		{
			objectManagerName = "HLAobjectRoot.HLAmanager";
			interactionManagerName = "HLAinteractionRoot.HLAmanager";
		}
		
		//
		// Objects
		//
		OCMetadata ocManager = model.getObjectClass( objectManagerName );
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
		
		//
		// Interactions
		//
		ICMetadata icManager = model.getInteractionClass( interactionManagerName );
		if( icManager != null )
		{
			// we have MOM stuff, nurse, pass my cleaver
			icManager.cleave();
			model.iclasses.remove( ocManager.getHandle() );
			
			// remove all its children from the model //
			// have to create a separate set to avoid a ConcurrentModificationException
			Set<ICMetadata> children = new HashSet<ICMetadata>( icManager.getChildTypes() );
			for( ICMetadata clazz : children )
			{
				clazz.cleave();
				model.iclasses.remove( clazz.getHandle() );
			}
		}

		//////////////////////////////////
		// add the predefined MOM stuff //
		//////////////////////////////////
		Mom.insertMomHierarchy( model );
		
		// lock the model again //
		if( wasLocked )
			model.lock();
	}
	
	/**
	 * Resolves dependencies between datatypes, attributes and parameters on a fully merged 
	 * {@link ObjectModel}.
	 * <p/>
	 * At parse time, there is no guarantee that attribute and parameter datatypes exist in the 
	 * {@link ObjectModel} as the dependent datatype may have been declared later in the FOM, or in 
	 * another FOM module altogether.
	 * <p/>
	 * As such a {@link DatatypePlaceholder} is inserted instead to indicate that the datatype needs
	 * to be resolve once all FOM modules have been imported.
	 * <p/>
	 * This method iterates over the {@link ObjectModel} and resolves all 
	 * {@link DatatypePlaceholder} instances to their concrete representation.
	 * <p/>
	 * <b>Note:</b> This method should only be called once all FOM modules have been merged and
	 * the Standard MIM has been inserted.
	 * 
	 * @param model the ObjectModel whose datatype dependencies require resolving
	 * @throws JInconsistentFDD if a concrete datatype representation can not be resolved for a
	 *                          placeholder symbol.
	 */
	public static void resolveSymbols( ObjectModel model ) throws JInconsistentFDD
	{
		boolean wasLocked = model.locked;
		if( wasLocked )
			model.unlock();
		
		try
		{
			Linker linker = new Linker();
			Set<IDatatype> datatypes = model.getDatatypes();
			linker.addCandidates( datatypes );
			
			// Link datatypes
			for( IDatatype type : datatypes )
				linker.linkType( type );
			
			// Link attribute datatypes
			for( OCMetadata objectClass : model.getAllObjectClasses() )
			{
				for( ACMetadata attributeClass : objectClass.getDeclaredAttributes() )
					linker.linkAttribute( attributeClass );
			}
			
			// Link parameter datatypes
			for( ICMetadata interactionClass : model.getAllInteractionClasses() )
			{
				for( PCMetadata parameterClass : interactionClass.getDeclaredParameters() )
					linker.linkParameter( parameterClass );
			}
		}
		catch( LinkerException le )
		{
			throw new JInconsistentFDD( le );
		}
		finally
		{
			// lock the model again //
			if( wasLocked )
				model.lock();
		}
		
	}
	
}
