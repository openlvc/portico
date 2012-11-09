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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.portico.lrc.compat.JInconsistentFDD;
import org.portico.lrc.compat.JRTIinternalError;

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
		this.logger = Logger.getLogger( "portico.lrc.merger" );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Model merging is not supported at the moment. For now we just return the first module
	 * in the list and ignore the others.
	 */
	private ObjectModel mergeModels( List<ObjectModel> models )
		throws JInconsistentFDD,
		       JRTIinternalError
	{
		// if we only got one model, there's nothing to merge!
		if( models.size() == 1 )
			return validate( models.get(0) );

		logger.trace( "Beginning merge of "+models.size()+" FOM models" );

		// if we have more than two, loop until we're done
		ObjectModel base = models.get(0);
		for( int i = 1; i < models.size(); i++ )
		{
			ObjectModel current = heavyCopy( models.get(i) );
			logger.trace( "Merging ["+current.getFileName()+"] into ["+base.getFileName()+"]" );
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
			                            " redeclared but not equivalent and not scaffolding" );
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
				logger.trace( "  Merging class ["+extensionChild.getQualifiedName()+"]" );
				mergeIntoModel( base.getModel(), base, extensionChild );
			}
			else
			{
				// it exists in the base and extension, recurse for checks at the next level down.
				mergeObjectClass( baseChild, extensionChild );
			}
		}
	}

	/**
	 * Copies the child object class into the provided model, linking it up with the given parent.
	 * This method reinitializes all the handles associated with the class so as to avoid handle
	 * clash problems when merging from two already parsed FOMs (handles sadly assigned at parse
	 * time, meaning we will most likely get clashes).
	 * <p/>
	 * This method will also recuse through all children doing the same thing.
	 * 
	 * @param model The model to insert the child into
	 * @param parent The parent the child will be linked to
	 * @param child The child that should be inserted into the model
	 */
	private void mergeIntoModel( ObjectModel model, OCMetadata parent, OCMetadata child )
	{
		logger.trace( "   -> Inserting class ["+child.getQualifiedName()+"]" );

		// link us in with the new parent
		child.setParent( parent );

		// reset the handles for the child and all its attributes
		child.setHandle( model.generateHandle() );
		for( ACMetadata attribute : child.getDeclaredAttributes() )
		{
			// remove the child under the old handle
			child.removeAttribute( attribute.getHandle() );
			// set the new handle on the attribute and re-add the child
			attribute.setHandle( model.generateHandle() );
			child.addAttribute( attribute );
		}
		
		// add the class to the model
		model.addObjectClass( child );
		
		// repeat this process for all the children's children
		// make copy of grandchildren to avoid ConcurrentModificationException during processing
		Set<OCMetadata> grandchildren = new HashSet<OCMetadata>( child.getChildTypes() ); 
		for( OCMetadata grandchild : grandchildren )
			mergeIntoModel( model, child, grandchild );
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
			                            " redeclared but not equivalent and not scaffolding" );
		}
		
		// check to see if there are any types in the extension that can be inserted into the base
		// make sure we work on a copy of the children, as moving the node across may change its
		// underlying child structure and cause a ConcurrentModificationException
		Set<ICMetadata> children = new HashSet<ICMetadata>( extension.getChildTypes() );
		for( ICMetadata extensionChild : children )
		{
			// if the child exists in the base model, try and merge, otherwise it needs to
			// be added into the base as it represents a new type
			ICMetadata baseChild = base.getChildType( extensionChild.getLocalName() );
			if( baseChild == null )
			{
				logger.trace( "  Merging class ["+extensionChild.getQualifiedName()+"]" );
				mergeIntoModel( base.getModel(), base, extensionChild );
			}
			else
			{
				// it exists in the base and extension, recurse for checks at the next level down.
				mergeInteractionClass( baseChild, extensionChild );
			}
		}
	}

	/**
	 * Copies the child object class into the provided model, linking it up with the given parent.
	 * This method reinitializes all the handles associated with the class so as to avoid handle
	 * clash problems when merging from two already parsed FOMs (handles sadly assigned at parse
	 * time, meaning we will most likely get clashes).
	 * <p/>
	 * This method will also recuse through all children doing the same thing.
	 * 
	 * @param model The model to insert the child into
	 * @param parent The parent the child will be linked to
	 * @param child The child that should be inserted into the model
	 */
	private void mergeIntoModel( ObjectModel model, ICMetadata parent, ICMetadata child )
	{
		logger.trace( "   -> Inserting class ["+child.getQualifiedName()+"]" );

		// link us in with the new parent
		child.setParent( parent );

		// reset the handles for the child and all its parameters
		child.setHandle( model.generateHandle() );
		for( PCMetadata parameter : child.getDeclaredParameters() )
		{
			// remove the child under the old handle
			child.removeParameter( parameter.getHandle() );
			// set the new handle on the parameter and re-add the child
			parameter.setHandle( model.generateHandle() );
			child.addParameter( parameter );
		}

		// add the class to the model
		model.addInteractionClass( child );
		
		// repeat this process for all the children's children
		// make copy of grandchildren to avoid ConcurrentModificationException during processing
		Set<ICMetadata> grandchildren = new HashSet<ICMetadata>( child.getChildTypes() ); 
		for( ICMetadata grandchild : grandchildren )
			mergeIntoModel( model, child, grandchild );
	}

	/**
	 * This method make sure that once all the modules are combined that the standard MIM
	 * types are present. If they're not, they will be merged in.
	 */
	private ObjectModel validate( ObjectModel model ) throws JInconsistentFDD
	{
		// ensure that HLAobjectRoot is present
		// ensure that HLAinteractionRoot is present
		return model;
	}

	/**
	 * This method makes a copy of the provided model in a very heavy-weight way. Basically it
	 * serializes it to a byte[] and then reconstitutes it back into a brand new ObjectModel.
	 * Inelegant, but simple and will do for now.
	 */
	private ObjectModel heavyCopy( ObjectModel model ) throws JRTIinternalError
	{
		try
		{
			// deflate
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			oos.writeObject( model );
			
			byte[] bytes = baos.toByteArray();
			
			// inflate
			ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
			ObjectInputStream ois = new ObjectInputStream( bais );
			return (ObjectModel)ois.readObject();
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Error while cloning object models to ensure they merge:"+
			                             e.getMessage(), e );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * From the given list, merge all subsequent object models into the first and return it. 
	 * If any of the modules cannot be merged for any reason, throw a {@link JInconsistentFDD}
	 * exception.
	 * <p/>
	 * Not that this method will ALTER the first model (merging everything else into it) but
	 * not alter any of the subsequent models (copies of them will be made).
	 */
	public static ObjectModel merge( List<ObjectModel> models )
		throws JInconsistentFDD,
		       JRTIinternalError
	{
		return new ModelMerger().mergeModels( models );
	}

	/**
	 * Merges the provided extensions into the base. This will irrevocably alter the base but
	 * will not change any of the extensions.
	 */
	public static ObjectModel merge( ObjectModel base, List<ObjectModel> extensions )
		throws JInconsistentFDD,
		       JRTIinternalError
	{
		ArrayList<ObjectModel> completeList = new ArrayList<ObjectModel>();
		completeList.add( base );
		completeList.addAll( extensions );
		return ModelMerger.merge( completeList );
	}

	/**
	 * This is the same as {@link #merge(List)} except that it doesn't make changes to ANY of
	 * the provided models. Use this when you just want to validate that two models will infact
	 * merge.
	 * <p/>
	 * This will merge the given extension modules into the provided base model
	 */
	public static void mergeDryRun( ObjectModel base, List<ObjectModel> extensions )
		throws JInconsistentFDD,
		       JRTIinternalError
	{
		ArrayList<ObjectModel> completeList = new ArrayList<ObjectModel>();
		completeList.add( base );
		completeList.addAll( extensions );

		// first we create a copy of each object model
		// we do this very crudely, by just pumping it down an object output stream and
		// then back in an input stream. Yuk. I know.
		// create the output stream with the given size (or resizable if -1 is provided)
		List<ObjectModel> cloneList = new ArrayList<ObjectModel>();
		try
		{
			// deflate
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream( baos );
			for( ObjectModel temp : completeList )
				oos.writeObject( temp );
			
			byte[] bytes = baos.toByteArray();
			
			// inflate
			ByteArrayInputStream bais = new ByteArrayInputStream( bytes );
			ObjectInputStream ois = new ObjectInputStream( bais );
			for( int i = 0; i < completeList.size(); i++ )
				cloneList.add( (ObjectModel)ois.readObject() );
		}
		catch( Exception e )
		{
			throw new JRTIinternalError( "Error while cloning object models to ensure they merge:"+
			                             e.getMessage(), e );
		}

		// run a merge on the clones to ensure it passes happily
		ModelMerger.merge( cloneList );
	}
}
