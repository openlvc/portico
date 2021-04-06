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
import java.util.TreeSet;

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
	 * Merge the provided modules together and return the resulting "super FOM".
	 * <p/>
	 * If there is one module, it is returned unmodified. If there is more than one, the first
	 * is used as the base and each of the others are subsequently merged in to it. This merge
	 * is done directly on the base model (a clone was previously made, but this meant that any
	 * internal state entities that has references to old pieces of the model - say for pub and
	 * sub - lost them). 
	 */
	private ObjectModel mergeModels( List<ObjectModel> models ) throws JInconsistentFDD,
	                                                                   JRTIinternalError
	{
		// if we only got one model, there's nothing to merge!
		if( models.size() == 1 )
			return validate( models.get(0) );

		logger.trace( "Beginning merge of "+models.size()+" FOM models" );

		// We have multiple models to merge
		// Make a clone of the first model, so that existing handle values are preserved
		// and then merge in the additional models to the clone
		ObjectModel base = models.get(0);
		for( int i = 1; i < models.size(); i++ )
		{
			ObjectModel current = models.get(i);
			logger.trace( "Merging ["+current.getFileName()+"] into combined FOM" );
			merge( base, current );
		}

		// validate the model to ensure it has everything we need
		return validate( base );
	}

	/**
	 * This method will merge the provided extension FOM into the base before returning the base.
	 * Both the models will be modified if there is something to merge.
	 */
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
	 * <b>NOTE</b>
	 * <p/>
	 * According to the HLA specification, we can't actually *merge* classes. An extension module
	 * can either insert entirely new classes, or insert attributes into a previously empty class,
	 * but two classes cannot be merged.
	 * <p/>
	 * <b>Inserting New Classes</b>
	 * <p/>
	 * When inserting new classes, the extension module needs to have the same hierarchy as the
	 * base module (as classes are compared according to their qualified name). To do this, the
	 * extension can have what's called "scaffolding" types, which are basically just empty class
	 * declarations that exist to provide the hierarchy for classes further down. When the merge
	 * process sees a scaffolding type inside the extension module, the class is skipped and we
	 * continue down into its children.
	 * <p/>
	 * Another way of maintaining the hierarchy is to have a complete replication of the class
	 * declaration. This would typically happen when you just take an existing FOM and add your
	 * own attributes/classes in (whereas in the previous you'd have empty classes until you got
	 * to the one you wanted to add). So, when a class from the extension module has attributes,
	 * we first test to see if the base module has some as well. If there are not attributes in
	 * the base, it's deemed a scaffolding type on the base side (see below). If there are
	 * attributes in both, we have to test to ensure that both modules have the same declarations.
	 * Remember that we can't merge classes, so if they exist in both, they can only exist to
	 * maintain hierarchy, which means they have to be the same. We test for this, and if they
	 * are the same, we recurse into the children.
	 * <p/>
	 * <b>Inserting Attributes</b>
	 * <p/>
	 * The only time we can insert attributes into a class is when it is a scaffolding type in
	 * the base module. This is deemed to be the case when the base module has no attributes for
	 * a class and the extension module has some. In this situation, we add all the attributes
	 * from the extension class into the base and then continue to recurse into the children.
	 *   
	 * @param base The class from the base FOM that the module is being merged into
	 * @param extension The class from the module that is being merged into the base FOM
	 * @throws JInconsistentFDD If the extension type is not scaffolding AND not equal to the
	 *                          base class.
	 */
	private void mergeObjectClass( OCMetadata base, OCMetadata extension ) throws JInconsistentFDD
	{
		/////////////////////////////////////////
		// Check to see if we can merge/extend //
		/////////////////////////////////////////
		// the appropriate merging action will depend on whether the class is a scaffodling
		// type in either the base or extension module - let's get the att count to see
		int baseAttributes = base.getDeclaredAttributeCount();
		int extensionAttributes = extension.getDeclaredAttributeCount();

		// Neither base nor Extension have attributes - It's scaffolding in both modules, recurse
		//if( baseAttributes == 0 && extensionAttributes == 0 ) {} -- here for comments

		// Extension has no attributes - It's scaffolding for something further down, recuse
		//if( baseAttributes > 0 && extensionAttributes == 0 ) {} -- here for comments

		// Both Base and Extension declare attributes.
		// Check to see if both classes are equivalent, issuing a warning if they are not.
		// This is mainly just for information purposes. They can't be merged if they're not
		// equivalent, and if they are equivalent there is nothing to merge.
		if( baseAttributes > 0 && extensionAttributes > 0 )
			validateOCMetadataEquivalent( base, extension );

		// Base has no attributes, but Extension does
		// The existing base structure is scaffolding we want to graft attributes on to. We
		// will get these attributes from the extension module
		// (how does this work when multiple extend?)
		if( baseAttributes == 0 && extensionAttributes > 0 )
		{
			// insert attributes base extension into the base
			for( ACMetadata attribute : extension.getDeclaredAttributes() )
			{
				ACMetadata newAttribute = new ACMetadata( attribute.getName(),
				                                          base.getModel().generateHandle() );
				newAttribute.setOrder( attribute.getOrder() );
				newAttribute.setTransport( attribute.getTransport() );
				newAttribute.setSpace( attribute.getSpace() );
				base.addAttribute( newAttribute );
			}
		}

		//////////////////////////////////
		// resurce down to the children //
		//////////////////////////////////
		// check to see if there are any types in the extension that can be inserted into the base
		// take copy of the set to avoid concurrent modificiation exceptions if we extend the model
		TreeSet<OCMetadata> extensionChildren = new TreeSet<OCMetadata>( extension.getChildTypes() );
		for( OCMetadata extensionChild : extensionChildren )
		{
			// if the child does not exist in the base model, insert it, otherwise we need to
			// recurse down into the hierarchy to ensure that extensions don't come further down
			OCMetadata baseChild = base.getChildType( extensionChild.getLocalName() );
			if( baseChild == null )
			{
				logger.warn("here ");
				insertObjectClass( base, extensionChild );
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
	private void insertObjectClass( OCMetadata parent, OCMetadata extension )
	{
		logger.trace( "   -> Inserting class ["+extension.getQualifiedName()+"]" );

		// create a new class to insert into the base model basing it off the extension
		ObjectModel model = parent.getModel();
		OCMetadata newClass = new OCMetadata( extension.getLocalName(), model.generateHandle() );
		newClass.setParent( parent );
		
		// create new attributes for all those in the extension
		for( ACMetadata attribute : extension.getDeclaredAttributes() )
		{
			ACMetadata newAttribute = new ACMetadata( attribute.getName(), model.generateHandle() );
			newAttribute.setOrder( attribute.getOrder() );
			newAttribute.setTransport( attribute.getTransport() );
			newAttribute.setSpace( attribute.getSpace() );
			newClass.addAttribute( newAttribute );
		}
		
		model.addObjectClass( newClass );

		// repeat this process for each of the grandchildren
		for( OCMetadata extensionGrandchild : extension.getChildTypes() )
			insertObjectClass( newClass, extensionGrandchild );
	}

	/**
	 * This method is basically the same as {@link #mergeObjectClass(OCMetadata, OCMetadata)}
	 * except that it works for Interactions/Parameters not Objects/Attributes.
	 * 
	 * @param base The class from the base FOM that the module is being merged into
	 * @param extension The class from the module that is being merged into the base FOM
	 * @throws JInconsistentFDD If the extension type is not scaffolding AND not equal to the
	 *                          base class.
	 */
	private void mergeInteractionClass( ICMetadata base, ICMetadata extension )	
		throws JInconsistentFDD
	{
		/////////////////////////////////////////
		// Check to see if we can merge/extend //
		/////////////////////////////////////////
		// the appropriate merging action will depend on whether the class is a scaffodling
		// type in either the base or extension module - let's get the att count to see
		int baseParameters = base.getDeclaredParameters().size();
		int extensionParameters = extension.getDeclaredParameters().size();
		
		// Neither base nor Extension have parameters - It's scaffolding in both modules, recurse
		//if( baseParameters == 0 && extensionParameters == 0 ) {} -- here for comments

		// Extension has no parameters - It's scaffolding for something further down, recuse
		//if( baseParameters > 0 && extensionParameters == 0 ) {} -- here for comments

		// Both Base and Extension declare parameters.
		// Check to see if both classes are equivalent, issuing a warning if they are not.
		// This is mainly just for information purposes. They can't be merged if they're not
		// equivalent, and if they are equivalent there is nothing to merge.
		if( baseParameters > 0 && extensionParameters > 0 )
			validateICMetadataEquivalent( base, extension );

		// Base has no parameters, but Extension does
		// The existing base structure is scaffolding we want to graft attributes on to. We
		// will get these attributes from the extension module
		// (how does this work when multiple extend?)
		if( baseParameters == 0 && extensionParameters > 0 )
		{
			// insert attributes base extension into the base
			for( PCMetadata parameter : extension.getDeclaredParameters() )
			{
				PCMetadata newParameter = new PCMetadata( parameter.getName(),
				                                          base.getModel().generateHandle() );
				base.addParameter( newParameter );
			}
		}

		//////////////////////////////////
		// resurce down to the children //
		//////////////////////////////////
		// check to see if there are any types in the extension that can be inserted into the base
		// take copy of the set to avoid concurrent modificiation exceptions if we extend the model
		Set<ICMetadata> extensionChildren = new HashSet<ICMetadata>( extension.getChildTypes() );
		for( ICMetadata extensionChild : extensionChildren )
		{
			// if the child does not exist in the base model, insert it, otherwise we need to
			// recurse down into the hierarchy to ensure that extensions don't come further down
			ICMetadata baseChild = base.getChildType( extensionChild.getLocalName() );
			if( baseChild == null )
			{
				insertInteractionClass( base, extensionChild );
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
	private void insertInteractionClass( ICMetadata parent, ICMetadata extension )
	{
		logger.trace( "   -> Inserting class ["+extension.getQualifiedName()+"]" );

		// create a new class to insert into the base model basing it off the extension
		ObjectModel model = parent.getModel();
		ICMetadata newClass = new ICMetadata( extension.getLocalName(), model.generateHandle() );
		newClass.setOrder( extension.getOrder() );
		newClass.setTransport( extension.getTransport() );
		newClass.setSpace( extension.getSpace() );
		newClass.setParent( parent );

		// create new parameters for all those in the extension
		for( PCMetadata parameter : extension.getDeclaredParameters() )
		{
			PCMetadata newParameter = new PCMetadata( parameter.getName(), model.generateHandle() );
			newClass.addParameter( newParameter );
		}
		
		model.addInteractionClass( newClass );

		// repeat this process for each of the grandchildren
		for( ICMetadata extensionGrandchild : extension.getChildTypes() )
			insertInteractionClass( newClass, extensionGrandchild );
	}

	/////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Utility Methods //////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method make sure that once all the modules are combined that the standard MIM
	 * types are present. If they're not, they will be merged in.
	 */
	private ObjectModel validate( ObjectModel model ) throws JInconsistentFDD
	{
		// ensure that HLAobjectRoot is present
		// ensure that HLAinteractionRoot is present
		// TBC
		return model;
	}

	/**
	 * Checks to see if both classes are equivalent, issuing a warning if they are not.
	 * This is mainly just for information purposes. They can't be merged if they're not
	 * equivalent, and if they are equivalent there is nothing to merge.
	 */
	private void validateOCMetadataEquivalent( OCMetadata base, OCMetadata extension )
		throws JInconsistentFDD
	{
		// make sure they have the same number of attributes before we loop through them
		if( extension.getDeclaredAttributeCount() != base.getDeclaredAttributeCount() )
		{
			logger.warn( "Merging FOM Module ("+extension.getModel().getFileName()+"): "+
			             "Ignoring Object Class ["+extension.getQualifiedName()+
			             "], declarations not equivalent. Attribute counts differ (base="+
			             base.getDeclaredAttributeCount()+", extension="+
			             extension.getDeclaredAttributeCount()+")" );
			return;
		}
		
		// loop through all the attributes to make sure they're the same (as far as we care)
		for( ACMetadata extensionAttribute : extension.getDeclaredAttributes() )
		{
			// find the same attribute in the base
			ACMetadata baseAttribute = base.getDeclaredAttribute( extensionAttribute.getName() );
			if( baseAttribute == null )
			{
				logger.warn( "Merging FOM Module ("+extension.getModel().getFileName()+"): "+
				             "Ignoring Object Class ["+extension.getQualifiedName()+
				             "], declarations not equivalent. Attribute ["+
				             extensionAttribute.getName()+
				             "] in extension does not exist in base module" );
				return;
			}

			// is the transport equal?
			if( extensionAttribute.getTransport() != baseAttribute.getTransport() )
			{
				logger.warn( "Merging FOM Module ("+extension.getModel().getFileName()+"): "+
				             "Ignoring Object Class ["+extension.getQualifiedName()+
				             "], declarations not equivalent. Attribute ["+
				             extensionAttribute.getName()+
				             "] in extension has different Transport (base="+
				             baseAttribute.getTransport()+", extension="+
				             extensionAttribute.getTransport()+")" );
				return;
			}
			
			// is the ordering equal?
			if( extensionAttribute.getOrder() != baseAttribute.getOrder() )
			{
				logger.warn( "Merging FOM Module ("+extension.getModel().getFileName()+"): "+
				             "Ignoring Object Class ["+extension.getQualifiedName()+
				             "], declarations not equivalent. Attribute ["+
				             extensionAttribute.getName()+
				             "] in extension has different Order (base="+
				             baseAttribute.getOrder()+", extension="+
				             extensionAttribute.getOrder()+")" );
				return;
			}
		}
	}

	/**
	 * Checks to see if both classes are equivalent, issuing a warning if they are not.
	 * This is mainly just for information purposes. They can't be merged if they're not
	 * equivalent, and if they are equivalent there is nothing to merge.
	 */
	private void validateICMetadataEquivalent( ICMetadata base, ICMetadata extension )
		throws JInconsistentFDD
	{
		// do they have the same parameter count?
		if( extension.getDeclaredParameters().size() != base.getDeclaredParameters().size() )
		{
			logger.warn( "Merging FOM Module ("+extension.getModel().getFileName()+"): "+
			             "Ignoring Interaction Class ["+extension.getQualifiedName()+
			             "], declarations not equivalent. Parameter counts differ (base="+
			             base.getDeclaredParameterCount()+", extension="+
			             extension.getDeclaredParameterCount()+")" );
			return;
		}
		
		// are all of the parameters the same?
		for( PCMetadata extensionParameter : extension.getDeclaredParameters() )
		{
			// does the base class have the parameter?
			PCMetadata baseParameter = base.getDeclaredParameter( extensionParameter.getName() );
			if( baseParameter == null )
			{
				logger.warn( "Merging FOM Module ("+extension.getModel().getFileName()+"): "+
				             "Ignoring Interaction Class ["+extension.getQualifiedName()+
				             "], declarations not equivalent. Parameter ["+
				             extensionParameter.getName()+
				             "] in extension does not exist in base module" );
				return;
			}
		}
		
		// do they have the same order?
		if( extension.getOrder() != base.getOrder() )
		{
			logger.warn( "Merging FOM Module ("+extension.getModel().getFileName()+"): "+
			             "Ignoring Interaction Class ["+extension.getQualifiedName()+
			             "], declarations not equivalent. Extension class has different Order (base="+
			             base.getOrder()+", extension="+
			             extension.getOrder()+")" );
			return;
		}
		
		// do they have the same transport?
		if( extension.getTransport() != base.getTransport() )
		{
			logger.warn( "Merging FOM Module ("+extension.getModel().getFileName()+"): "+
			             "Ignoring Interaction Class ["+extension.getQualifiedName()+
			             "], declarations not equivalent. Extension class has different Transport (base="+
			             base.getTransport()+", extension="+
			             extension.getTransport()+")" );
			return;
		}
	}
	
	/**
	 * This method makes a copy of the provided model in a very heavy-weight way. Basically it
	 * serializes it to a byte[] and then reconstitutes it back into a brand new ObjectModel.
	 * Inelegant, but simple and will do for now.
	 */
	private ObjectModel deepCopy( ObjectModel model ) throws JRTIinternalError
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

	/**
	 * Return a string in the format "(merging path/to/extension into path/to/base)" to be used
	 * for logging information about the merge in error messages.
	 */
	public String getMergeDirectionString( OCMetadata base, OCMetadata extension )
	{
		return "(merging "+extension.getModel().getFileName()+" into "+
		       base.getModel().getFileName()+")";
	}

	/**
	 * Return a string in the format "(merging path/to/extension into path/to/base)" to be used
	 * for logging information about the merge in error messages.
	 */
	public String getMergeDirectionString( ICMetadata base, ICMetadata extension )
	{
		return "(merging "+extension.getModel().getFileName()+" into "+
		       base.getModel().getFileName()+" ["+Thread.currentThread().getName()+"])";
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
		if( base != null )
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
		if( base != null )
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
		cloneList.get(0).unlock();
		ModelMerger.merge( cloneList );
	}
}
