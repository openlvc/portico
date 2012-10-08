/*
 *   Copyright 2012 The Portico Project
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.portico.lrc.compat.JInconsistentFDD;

/**
 * This class provides the logic for merging multiple {@link ObjectModel}s together into a
 * single, unified object model as required by IEEE-1516e. This behaviour will just arbitrarily
 * merge together modules as the 1516e specification demands, even if the models are for earlier
 * HLA versions.
 */
public class ModelMerger
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public ModelMerger()
	{
		this.logger = Logger.getLogger( "portico.lrc" );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Model merging is not supported at the moment. For now we just return the first module
	 * in the list and ignore the others.
	 */
	private ObjectModel mergeModels( List<ObjectModel> models ) throws JInconsistentFDD
	{
		// if we only got one model, there's nothing to merge!
		if( models.size() == 1 )
			return validate( models.get(0) );

		logger.debug( "Merging "+models.size()+" different FOM models" );

		// if we have more than two, loop until we're done
		ObjectModel base = models.get(0);
		for( int i = 1; i < models.size(); i++ )
		{
			ObjectModel current = models.get(i);
			logger.debug( "Merging ["+current.getFileName()+"]" );
			base = merge( base, current );
		}
		
		// validate the model to ensure it has everything we need
		return validate( base );
	}

	private ObjectModel merge( ObjectModel base, ObjectModel extension ) throws JInconsistentFDD
	{
		// merge objects, starting at the object root
		if( extension.getObjectRoot() != null )
			mergeObjectClass( base.getObjectRoot(), extension.getObjectRoot() );
		
		// merge interactions, starting at the root
		if( extension.getInteractionRoot() != null )
			mergeInteractionClass( base.getInteractionRoot(), extension.getInteractionRoot() );
		
		// return the merges model
		return base;
	}

	/**
	 * Merge the provided object classes together, extension the parent type with the extension
	 * type.
	 * <p/>
	 * The specification only provides for inserting whole new classes, not merging attribute
	 * sets from existing classes. As such, this method will verify that both classes are either
	 * {@link OCMetadata#shallowEquals(OCMetadata) equivalent} or that the extension class is just
	 * a scaffolding type. If not, an exception will be thrown.
	 * <p/>
	 * If they are equal or the extension is just scaffolding, child classes of the extension type
	 * will be considered. Where there is no equivalent child class beneth the base, the child
	 * class from the extension will be linked into the base hierarchy. Where there is a child
	 * from both types with the same name, we recurse back in and repeat the process.
	 *   
	 * @param base The class from the base FOM that the module is being merged into
	 * @param extension The class from the module that is being merged into the base FOM
	 * @throws JInconsistentFDD If the extension type is not scaffolding AND not equal to the
	 *                          base class.
	 */
	private void mergeObjectClass( OCMetadata base, OCMetadata extension ) throws JInconsistentFDD
	{
		// According to the spec, we can't actually *merge* classes. An extension module
		// can insert new classes into the hierarchy, but can't insert attributes into existing
		// classes. However, to ensure that extension classes have the correct hierarchy,
		// classes with the same name must be present in both modules. This can be done validly
		// in one of two ways: redeclare the entire class with exactly the same settings
		// (attributes, order, transport, pub/sub settings and all) or redeclare the class
		// entirely empty (known as providing it for scaffolding). We will throw an exception
		// and not continue to process children if neither of these is the case.

		// no attribute merging possible, just check for equivalence or scaffolding
		if( extension.getDeclaredAttributeCount() != 0  &&  /* scaffolding */
			base.shallowEquals(extension) == false )        /* equivalence */
		{
			// the extension type is not scaffolding and isn't equal to the base type :(
			throw new JInconsistentFDD( "Class "+base.getQualifiedName()+
			                            " redeclared but not equal of scaffolding" );
		}

		// check to see if there are any types in the extension that can be inserted into the base
		// take copy of the set to avoid concurrent modificiation exceptions if we extend the model
		Set<OCMetadata> children = new HashSet<OCMetadata>( extension.getChildTypes() );
		for( OCMetadata extensionChild : children )
		{
			// if the child exists in the base model, try and merge, otherwise it needs to
			// be added into the base as it represents a new type
			OCMetadata baseChild = base.getChildType( extensionChild.getLocalName() );
			if( baseChild == null )
			{
				logger.trace( "Merging class ["+extensionChild.getQualifiedName()+"]" );
				//copyIntoModel( base.getModel(), base, extensionChild );
				extensionChild.setParent( base );
				insertObjectClass( base.getModel(), extensionChild );
			}
			else
			{
				// it exists in the base and extension, recurse for checks at the next level down.
				mergeObjectClass( baseChild, extensionChild );
			}
		}
	}

//	private void copyIntoModel( ObjectModel model, OCMetadata parent, OCMetadata child )
//	{
//		logger.trace( "   -> Inserting class ["+child.getQualifiedName()+"]" );		
//	}
	
	private void insertObjectClass( ObjectModel model, OCMetadata child )
	{
		logger.trace( "   -> Inserting class ["+child.getQualifiedName()+"]" );
		model.addObjectClass( child );
		
		// look through all childen and link them in with their new model as well
		for( OCMetadata grandchild : child.getChildTypes() )
			insertObjectClass( model, grandchild );
	}

	/**
	 * Merge the provided interaction classes together, extending the parent type with the
	 * extension type.
	 * <p/>
	 * The specification only provides for inserting whole new classes, not merging parameter
	 * sets from existing classes. As such, this method will verify that both classes are either
	 * {@link ICMetadata#shallowEquals(ICMetadata) equivalent} or that the extension class is just
	 * a scaffolding type. If not, an exception will be thrown.
	 * <p/>
	 * If they are equal or the extension is just scaffolding, child classes of the extension type
	 * will be considered. Where there is no equivalent child class beneth the base, the child
	 * class from the extension will be linked into the base hierarchy. Where there is a child
	 * from both types with the same name, we recurse back in and repeat the process.
	 *   
	 * @param base The class from the base FOM that the module is being merged into
	 * @param extension The class from the module that is being merged into the base FOM
	 * @throws JInconsistentFDD If the extension type is not scaffolding AND not equal to the
	 *                          base class.
	 */
	private void mergeInteractionClass( ICMetadata base, ICMetadata extension )	
		throws JInconsistentFDD
	{
		// As with object classes, we cannot merge two interaction classes. Extension modules
		// can only append new interactions onto the existing hierarchy in the base module.
		// If the extension class is not scaffolding and not equal to the base, an inconsistent
		// FDD exception will be thrown. Otherwise, we check children to see if there are any
		// we can insert across into the base model
		if( extension.getDeclaredParameters().size() != 0 &&  /* scaffolding */
			base.shallowEquals(extension) == false )          /* equivalence */
		{
			// the extension type is not scaffolding and isn't equal to the base type :(
			throw new JInconsistentFDD( "Class "+base.getQualifiedName()+
			                            " redeclared but not equal of scaffolding" );
		}
		
		// check to see if there are any types in the extension that can be inserted into the base
		for( ICMetadata extensionChild : extension.getChildTypes() )
		{
			// if the child exists in the base model, try and merge, otherwise it needs to
			// be added into the base as it represents a new type
			ICMetadata baseChild = base.getChildType( extensionChild.getLocalName() );
			if( baseChild == null )
			{
				logger.trace( "Merging in class ["+extensionChild.getQualifiedName()+"]" );
				extensionChild.setParent( base );
				insertInteractionClass( base.getModel(), extensionChild );
			}
			else
			{
				// it exists in the base and extension, recurse for checks at the next level down.
				mergeInteractionClass( baseChild, extensionChild );
			}
		}
	}

	private void insertInteractionClass( ObjectModel model, ICMetadata child )
	{
		logger.trace( "   -> Inserting class ["+child.getQualifiedName()+"]" );
		model.addInteractionClass( child );
		
		// look through all childen and link them in with their new model as well
		for( ICMetadata grandchild : child.getChildTypes() )
			insertInteractionClass( model, grandchild );
	}

	
	private ObjectModel validate( ObjectModel model )
	{
		// ensure that HLAobjectRoot is present
		// ensure that HLAinteractionRoot is present
		return model;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Model merging is not supported at the moment. For now we just return the first module
	 * in the list and ignore the others.
	 */
	public static ObjectModel merge( List<ObjectModel> models ) throws JInconsistentFDD
	{
		return new ModelMerger().mergeModels( models );
	}
}
