/*
 *   Copyright 2008 The Portico Project
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
package org.portico.lrc.services.object.data;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCState;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JInteractionClassNotDefined;
import org.portico.lrc.compat.JInteractionClassNotPublished;
import org.portico.lrc.compat.JInteractionClassNotSubscribed;
import org.portico.lrc.compat.JInvalidRegionContext;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JObjectClassNotSubscribed;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;

/**
 * The interest manager contains federate publication and subscription data. This manager will
 * store data for any federate, where the federate handle can be used to get the desired
 * information. It works by storing groups of {@link OCInterest} and {@link ICInterest} instances
 * for each object/interaction class. These classes hold data about which federates have an
 * interest in them, and what the interest is. That is, rather than storing a separate set of
 * information for each federate, we store sets of information about a particular object/interaction
 * class (inside each is information about any federate).
 */
public class InterestManager implements SaveRestoreTarget
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private LRCState state;
	private Map<OCMetadata,OCInterest> pObjects;
	private Map<OCMetadata,OCInterest> sObjects;
	private Map<ICMetadata,ICInterest> pInteractions;
	private Map<ICMetadata,ICInterest> sInteractions;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public InterestManager( LRCState lrcState )
	{
		this.state = lrcState;
		this.pObjects = new HashMap<OCMetadata,OCInterest>();
		this.sObjects = new HashMap<OCMetadata,OCInterest>();
		this.pInteractions = new HashMap<ICMetadata,ICInterest>();
		this.sInteractions = new HashMap<ICMetadata,ICInterest>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////// Generic Object Publication/Subscription Methods ////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The same as {@link #register(Map, String, int, int, Set, int)} except that it passes
	 * {@link PorticoConstants#NULL_HANDLE} for the region token and catches/ignores region
	 * related exceptions. Use this for non-ddm registrations.
	 * 
	 * @param map        The map of interest data to update (publication or subscription)
	 * @param action     A tag used in exceptions that describes what the action is attempting 
	 * @param federate   The handle of the federate this registration is in reference to
	 * @param clazz      The handle of the class this registration is for
	 * @param attributes The set of attributes that are having an interest registered in
	 */
	private void register( Map<OCMetadata,OCInterest> map,
	                       String action,
	                       int federate,
	                       int clazz,
	                       Set<Integer> attributes )
		throws JObjectClassNotDefined,
		       JAttributeNotDefined,
		       JRTIinternalError
	{
		try
		{
			register( map, action, federate, clazz, attributes, PorticoConstants.NULL_HANDLE );
		}
		catch( JRegionNotKnown rnk )
		{
			// won't happen because we're passing PorticoConstants.NULL_HANDLE
		}
		catch( JInvalidRegionContext irc )
		{
			// won't happen because we're passing PorticoConstants.NULL_HANDLE
		}
	}
	                       	
	/**
	 * This method will register an interest (publication or subscription) of an object class for
	 * the federate identified by the given federate handle. It will run all the appriorate checks
	 * on the given information to validate that the handles exist in the model and are appropriate
	 * (make sure the attributes are of the right object class etc...). If there is a problem with
	 * the given information, an exception will be thrown. This version of the method accepts a
	 * region token to associate with the unregister. Use it for DDM-related calls.
	 * 
	 * @param map            The map of interest data to update (publication or subscription)
	 * @param action         A tag used in exceptions that describes what the action is attempting 
	 * @param federateHandle The handle of the federate this registration is in reference to
	 * @param classHandle    The handle of the class this registration is for
	 * @param attributes     The set of attributes that are having an interest registered in
	 * @param regionToken    Token for the region to associate with the registration. If you don't
	 *                       want to both with DDM stuff, pass {@link PorticoConstants#NULL_HANDLE}
	 */
	private void register( Map<OCMetadata,OCInterest> map,
	                       String action,
	                       int federateHandle,
	                       int classHandle,
	                       Set<Integer> attributes,
	                       int regionToken )
		throws JObjectClassNotDefined,
		       JAttributeNotDefined,
		       JRegionNotKnown,
		       JInvalidRegionContext,
		       JRTIinternalError
	{
		// find the object class
		OCMetadata objectClass = fom().getObjectClass( classHandle );
		if( objectClass == null )
			throw new JObjectClassNotDefined( action + ": class not defined: " + classHandle );
		
		// find each attribute handle in the class
		for( int attributeHandle : attributes )
		{
			if( objectClass.hasAttribute(attributeHandle) == false )
			{
				throw new JAttributeNotDefined( action +": attribute ["+ attributeHandle +
				                                "] not defined in object class [" +
				                                objectClass.getQualifiedName() + "]" );
			}
		}
		
		////////////////////////////////////////////////////
		// do all the data distribution management checks //
		////////////////////////////////////////////////////
		RegionInstance region = null;
		if( regionToken != PorticoConstants.NULL_HANDLE )
		{
			region = state.getRegionStore().getRegion( regionToken );
			if( region == null )
				throw new JRegionNotKnown( "token: " + regionToken );
			
			for( int attributeHandle : attributes )
			{
				ACMetadata attribute = objectClass.getAttribute(attributeHandle);
				if( attribute.getSpace() == null ||
					attribute.getSpace().getHandle() != region.getSpaceHandle() )
				{
					throw new JInvalidRegionContext( "attribute [" + attributeHandle +
					    "] can't be associated with region [token:" + region.getToken() +
					    "]: Routing space not associated with attribute in FOM" );
				}
			}
		}
		
		///////////////////////////////////////////////////
		// record the registration, augment if it exists //
		///////////////////////////////////////////////////
		OCInterest interest = map.get( objectClass );
		if( interest == null )
		{
			interest = new OCInterest( objectClass );
			map.put( objectClass, interest );
		}

		interest.registerInterest( federateHandle, attributes, region /*null ok*/ );
	}
	
	/**
	 * This method will unregister any interest the specified federate has in the identified
	 * object class. If the class doesn't exist in the FOM, or the particular federate didn't have
	 * an interest in it, an exception will be thrown. This method will throw a
	 * {@link NoRegistration} if there is no registration. Wrapper methods can extract the message
	 * and throw an appropriate exception type. 
	 * 
	 * @param map            The map of interest data to update (publication or subscription)
	 * @param action         A tag used in exceptions that describes what the action is attempting 
	 * @param federateHandle The handle of the federate this unregistration is in reference to
	 * @param classHandle    The handle of the class this unregistration is for
	 */
	private void unregister( Map<OCMetadata,OCInterest> map,
	                         String action,
	                         int federateHandle,
	                         int classHandle )
		throws JObjectClassNotDefined,
		       NoRegistration
	{
		try
		{
			unregister( map, action, federateHandle, classHandle, PorticoConstants.NULL_HANDLE );
		}
		catch( JRegionNotKnown rnk )
		{
			// won't happen because we're passing PorticoConstants.NULL_HANDLE
		}
	}

	/**
	 * This method will unregister any interest the specified federate has in the identified
	 * object class. If the class doesn't exist in the FOM, or the particular federate didn't have
	 * an interest in it, an exception will be thrown. This method will throw a
	 * {@link NoRegistration} if there is no registration. Wrapper methods can extract the message
	 * and throw an appropriate exception type. This version of the method accepts a region token
	 * to associate with the unregister. Use it for DDM-related calls.
	 * 
	 * @param map            The map of interest data to update (publication or subscription)
	 * @param action         A tag used in exceptions that describes what the action is attempting 
	 * @param federateHandle The handle of the federate this unregistration is in reference to
	 * @param classHandle    The handle of the class this unregistration is for
	 * @param regionToken    Token for the region to associate with the registration. If you don't
	 *                       want to both with DDM stuff, pass {@link PorticoConstants#NULL_HANDLE}
	 */
	private void unregister( Map<OCMetadata,OCInterest> map,
	                         String action,
	                         int federateHandle,
	                         int classHandle,
	                         int regionToken )
		throws JObjectClassNotDefined,
		       JRegionNotKnown,
		       NoRegistration
	{
		// validate the region if applicable
		RegionInstance region = null;
		if( regionToken != PorticoConstants.NULL_HANDLE )
		{
			region = state.getRegionStore().getRegion( regionToken );
			if( region == null )
				throw new JRegionNotKnown( "token: " + regionToken );
		}

		// find the metadata for the object class so that we can locate the appropriate
		// OCInterest and remove the federate as a recorded interest
		OCMetadata objectClass = fom().getObjectClass( classHandle );
		if( objectClass == null )
			throw new JObjectClassNotDefined( action + ": class not defined: " + classHandle );
		
		OCInterest interest = map.get( objectClass );
		if( interest == null )
			throw new NoRegistration( action+": federate has no pub/sub interest in "+classHandle );
		
		interest.removeInterest( federateHandle, region );
	}

	/**
	 * This method will unregister any interest the associated fedeate has in the specified
	 * attributes of the given class. If other attributes in the class were also registered,
	 * that registration will still remain after this caall. If the given set of attribute
	 * handles is either <code>null</code> or empty, the entire interest (*ALL* attributes)
	 * will be removed. If the object class cannot be found, or the record doesn't exist, an
	 * exception will be thrown.
	 * 
	 * @param map         The map of interest data to update (publication or subscription)
	 * @param action      A tag used in exceptions that describes what the action is attempting 
	 * @param federate    The handle of the federate this unregistration is in reference to
	 * @param classHandle The handle of the class this unregistration is for
	 * @param attributes  The set of attributes that are having an interest registered in
	 */
	private void unregister( Map<OCMetadata,OCInterest> map,
	                         String action,
	                         int federate,
	                         int classHandle,
	                         Set<Integer> attributes )
		throws JObjectClassNotDefined,
		       NoRegistration
	{
		try
		{
			unregister( map, action, federate, classHandle, attributes, PorticoConstants.NULL_HANDLE );
		}
		catch( JRegionNotKnown rnk )
		{
			// won't happen because we're passing PorticoConstants.NULL_HANDLE
		}
	}
	

	/**
	 * This method will unregister any interest the associated fedeate has in the specified
	 * attributes of the given class. If other attributes in the class were also registered,
	 * that registration will still remain after this caall. If the given set of attribute
	 * handles is either <code>null</code> or empty, the entire interest (*ALL* attributes)
	 * will be removed. If the object class cannot be found, or the record doesn't exist, an
	 * exception will be thrown. This version of the method accepts a region token to associate
	 * with the unregister. Use it for DDM-related calls.
	 * 
	 * @param map            The map of interest data to update (publication or subscription)
	 * @param action         A tag used in exceptions that describes what the action is attempting 
	 * @param federateHandle The handle of the federate this unregistration is in reference to
	 * @param classHandle    The handle of the class this unregistration is for
	 * @param attributes     The set of attributes that are having an interest registered in
	 * @param regionToken    Token for the region to associate with the registration. If you don't
	 *                       want to both with DDM stuff, pass {@link PorticoConstants#NULL_HANDLE}
	 */
	private void unregister( Map<OCMetadata,OCInterest> map,
	                         String action,
	                         int federateHandle,
	                         int classHandle,
	                         Set<Integer> attributes,
	                         int regionToken )
		throws JObjectClassNotDefined,
		       JRegionNotKnown,
		       NoRegistration
	{
		// validate the region if applicable
		RegionInstance region = null;
		if( regionToken != PorticoConstants.NULL_HANDLE )
		{
			region = state.getRegionStore().getRegion( regionToken );
			if( region == null )
				throw new JRegionNotKnown( "token: " + regionToken );
			
			// FIXME Add support for region handling and attribute sets
			throw new RuntimeException( "not yet supported" );
		}

		// find the metadata for the object class so that we can locate the appropriate
		// OCInterest and remove the federate as a recorded interest
		OCMetadata objectClass = fom().getObjectClass( classHandle );
		if( objectClass == null )
			throw new JObjectClassNotDefined( action+": class not defined: "+classHandle );
		
		OCInterest interest = map.get( objectClass );
		if( interest == null )
			throw new NoRegistration( action+": federate has no pub/sub interest in "+classHandle );
		
		interest.removeInterest( federateHandle, attributes );
	}
	
	/**
	 * Get all the attributes that are currently registered by the identified federate handle. If
	 * the class isn't registered by the federate, a {@link NoRegistration} is thrown.
	 */
	private Set<Integer> getRegisteredAttributes( Map<OCMetadata,OCInterest> map,
	                                              String action,
	                                              int federateHandle,
	                                              int classHandle )
		throws JObjectClassNotDefined, NoRegistration
	{
		// find the metadata for the object class so that we can locate the appropriate
		// OCInterest and get all the registered attributes
		OCMetadata objectClass = fom().getObjectClass( classHandle );
		if( objectClass == null )
			throw new JObjectClassNotDefined( action+": object class not defined: "+classHandle );
		
		OCInterest interest = map.get( objectClass );
		if( interest == null )
			throw new NoRegistration( action+": federate has no pub/sub interest in "+classHandle );
		
		Set<Integer> registered = interest.getInterest( federateHandle );
		if( registered == null )
			throw new NoRegistration( action+": federate has no pub/sub interest in "+classHandle );
		else
			return interest.getInterest( federateHandle );
	}

	/**
	 * This method will check to see if the identified federate has registered an interest in the
	 * given object class.
	 */
	private boolean isObjectClassRegistered( Map<OCMetadata,OCInterest> map,
	                                         int federateHandle,
	                                         int classHandle )
	{
		// get the metadata for the object class so we can locate the interest
		OCInterest interest = map.get( fom().getObjectClass(classHandle) );
		if( interest == null )
			return false;
		else
			return interest.hasInterest( federateHandle );
	}

	/**
	 * Returns <code>true</code> if the federate has a registered interest in the identified
	 * attribute class. If the object or attribute classes can't be found, or the attribute
	 * isn't registered, <code>false</code> is returned.
	 */
	private boolean isAttributeClassRegistered( Map<OCMetadata,OCInterest> map,
	                                            int federateHandle,
	                                            int classHandle,
	                                            int attributeHandle )
	{
		// get the metadata for the object class so we can locate the interest
		OCInterest interest = map.get( fom().getObjectClass(classHandle) );
		if( interest == null )
			return false;
		else
			return interest.hasAttributeInterest( federateHandle, attributeHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////// Generic Interaction Publication/Subscription Methods //////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The same as {@link #register(Map, String, int, int, int)} except that it passes
	 * {@link PorticoConstants#NULL_HANDLE} for the region token and catches/ignores region
	 * related exceptions. Use this for non-ddm registrations.
	 * 
	 * @param map            The map of interest data to update (publication or subscription)
	 * @param action         A tag used in exceptions that describes what the action is attempting 
	 * @param federateHandle The handle of the federate this registration is in reference to
	 * @param classHandle    The handle of the class this registration is for
	 */
	private void register( Map<ICMetadata,ICInterest> map,
	                       String action,
	                       int federateHandle,
	                       int classHandle )
		throws JInteractionClassNotDefined,
		       JRTIinternalError
	{
		try
		{
			register( map, action, federateHandle, classHandle, PorticoConstants.NULL_HANDLE );
		}
		catch( JRegionNotKnown rnk )
		{
			// won't happen because we're passing PorticoConstants.NULL_HANDLE
		}
		catch( JInvalidRegionContext irc )
		{
			// won't happen because we're passing PorticoConstants.NULL_HANDLE
		}
	}
	
	/**
	 * Registers a publication or subscription interest in a particular interaction class for the
	 * identified federate. Whether the interest is a pub or sub one depends on the map that is
	 * passed to the method (e.g. for publications, pass the interaction-publications instance var).
	 * If the interaction class isn't defined in the FOM, an exception will be thrown and the given
	 * string will be used in the error message (so you can specify what you were trying to do).
	 * This method takes into account region data when making the registration. If you don't want
	 * DDM-related methods, you can pass {@link PorticoConstants#NULL_HANDLE} for the regionToken.
	 * 
	 * @param map            The map of interest data to update (publication or subscription)
	 * @param action         A tag used in exceptions that describes what the action is attempting 
	 * @param federateHandle The handle of the federate this registration is in reference to
	 * @param classHandle    The handle of the class this registration is for
	 * @param regionToken    Token for the region to associate with the registration. If you don't
	 *                       want to both with DDM stuff, pass {@link PorticoConstants#NULL_HANDLE}
	 */
	private void register( Map<ICMetadata,ICInterest> map,
	                       String action,
	                       int federateHandle,
	                       int classHandle,
	                       int regionToken )
		throws JInteractionClassNotDefined,
		       JRegionNotKnown,
		       JInvalidRegionContext,
		       JRTIinternalError
	{
		// find the interaction class
		ICMetadata interactionClass = fom().getInteractionClass( classHandle );
		if( interactionClass == null )
			throw new JInteractionClassNotDefined( action + ": class not defined: " + classHandle );

		// do all the data distribution management checks if applicable
		RegionInstance region = null;
		if( regionToken != PorticoConstants.NULL_HANDLE )
		{
			region = state.getRegionStore().getRegionCreatedBy( regionToken, federateHandle );
			if( region == null )
				throw new JRegionNotKnown( "token: " + regionToken );
			
			// check that the FOM allows regions of this space to link with this interaction class
			if( interactionClass.getSpace() == null ||
				interactionClass.getSpace().getHandle() != region.getSpaceHandle() )
			{
				throw new JInvalidRegionContext( "The routing space for the region is different" +
				    " from the routing space associated with the interaction class in the FOM" );
			}
		}

		// record the registration. if the interest already exists, augment it, if not, create it
		ICInterest interest = map.get( interactionClass );
		if( interest == null )
		{
			interest = new ICInterest( interactionClass );
			map.put( interactionClass, interest );
		}
		
		interest.registerInterest( federateHandle, region );
	}
	
	/**
	 * The reverse of {@link #register(Map, String, int, int)}. Removes the publication or
	 * subscription interest in the class for the identified federate. If the class doesn't exist
	 * in the FOM, or the federate wasn't registered as having the interest, an exception will be
	 * thrown.
	 */
	private void unregisterInteraction( Map<ICMetadata,ICInterest> map,
	                                    String action,
	                                    int federateHandle,
	                                    int classHandle )
		throws JInteractionClassNotDefined,
		       NoRegistration
	{
		try
		{
			unregisterInteraction( map,
			                       action,
			                       federateHandle,
			                       classHandle,
			                       PorticoConstants.NULL_HANDLE );
		}
		catch( JRegionNotKnown rnk )
		{
			// won't happen because we're passing PorticoConstants.NULL_HANDLE
		}
	}

	/**
	 * The reverse of {@link #register(Map, String, int, int)}. Removes the publication or
	 * subscription interest in the class for the identified federate. If the class doesn't exist
	 * in the FOM, or the federate wasn't registered as having the interest, an exception will be
	 * thrown.
	 * This method takes into account region data when making the registration. If you don't want
	 * DDM-related methods, you can pass {@link PorticoConstants#NULL_HANDLE} for the regionToken.
	 * 
	 * @param map            The map of interest data to update (publication or subscription)
	 * @param action         A tag used in exceptions that describes what the action is attempting 
	 * @param federateHandle The handle of the federate this registration is in reference to
	 * @param classHandle    The handle of the class this registration is for
	 * @param regionToken    Token for the region to associate with the registration. If you don't
	 *                       want to both with DDM stuff, pass {@link PorticoConstants#NULL_HANDLE}.
	 */
	private void unregisterInteraction( Map<ICMetadata,ICInterest> map,
	                                    String action,
	                                    int federateHandle,
	                                    int classHandle,
	                                    int regionToken )
		throws JInteractionClassNotDefined,
		       JRegionNotKnown,
		       NoRegistration
	{
		// validate region information if applicable
		RegionInstance region = null;
		if( regionToken != PorticoConstants.NULL_HANDLE )
		{
			region = state.getRegionStore().getRegionCreatedBy( regionToken, federateHandle );
			if( region == null )
				throw new JRegionNotKnown( "token: " + regionToken );
		}
		
		// find the metadata for the interaction class so that we can locate the appropriate
		// ICInterest and remove the federate as a recorded interest
		ICMetadata interactionClass = fom().getInteractionClass( classHandle );
		if( interactionClass == null )
			throw new JInteractionClassNotDefined( action + ": class not defined: " + classHandle );
		
		ICInterest interest = map.get( interactionClass );
		if( interest == null )
			throw new NoRegistration( action+": federate has no pub/sub interest in "+classHandle );
		
		// null will be passed for the region if no region data was provided, this is equivalent
		// to passing the default region causing region considerations to be ignored
		interest.removeInterest( federateHandle, region );
	}
	
	/**
	 * This method will check to see if the identified federate has registered an interest in the
	 * given interaction class.
	 */
	private boolean isInteractionClassRegistered( Map<ICMetadata,ICInterest> map,
	                                              int federateHandle,
	                                              int classHandle )
	{
		// get the metadata for the object class so we can locate the interest
		ICInterest interest = map.get( fom().getInteractionClass(classHandle) );
		if( interest == null )
			return false;
		else
			return interest.hasInterest( federateHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Object Publication Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will record the publication of an object class for the federate identified
	 * by the given federate handle. It will run all the appriorate checks on the given information
	 * to validate that the handles exist in the model and are appropriate (make sure the attributes
	 * are of the right object class etc...)
	 */
	public void publishObjectClass( int federateHandle, int classHandle, Set<Integer> attributes )
		throws JObjectClassNotDefined,
		       JAttributeNotDefined,
		       JRTIinternalError
	{
		register( pObjects, "PUBLISH-OBJECT", federateHandle, classHandle, attributes );
	}

	/**
	 * This method will remove any object publication record the specified federate had in the
	 * object class identified by the given handle. If the class doesn't exist in the FOM, or
	 * the particular federate didn't have a publication interest in it, an exception will be
	 * thrown.
	 */
	public void unpublishObjectClass( int federateHandle, int classHandle )
		throws JObjectClassNotDefined,
		       JObjectClassNotPublished
	{
		try
		{
			unregister( pObjects, "PUBLISH-OBJECT", federateHandle, classHandle );
		}
		catch( NoRegistration nr )
		{
			throw new JObjectClassNotPublished( nr.getMessage() );
		}
	}
	
	/**
	 * This method will remove the publication record associated with the identified federate in
	 * the given attributes of the given class. If other attributes in the class were also
	 * published, their record will remain after this call. If the given set is either empty or
	 * <code>null</code>, then the entire publication (for *ALL* attributes of the class) will be
	 * removed. If the object class cannot be found, or a publication record didn't exist, an
	 * exception will be thrown.
	 */
	public void unpublishObjectClass( int federateHandle, int classHandle, Set<Integer> attributes )
		throws JObjectClassNotDefined,
		       JObjectClassNotPublished
	{
		try
		{
			unregister( pObjects, "PUBLISH-OBJECT", federateHandle, classHandle, attributes );
		}
		catch( NoRegistration nr )
		{
			throw new JObjectClassNotPublished( nr.getMessage() );
		}
	}
	
	/**
	 * Get all the attributes that are currently published by the identified federate handle. If
	 * the class isn't published by the federate, an exception is thrown.
	 */
	public Set<Integer> getPublishedAttributes( int federateHandle, int classHandle )
		throws JObjectClassNotDefined, JObjectClassNotPublished
	{
		try
		{
			return getRegisteredAttributes( pObjects, "PUBLISH-OBJECT", federateHandle, classHandle );
		}
		catch( NoRegistration nr )
		{
			throw new JObjectClassNotPublished( nr.getMessage() );
		}
	}

	/**
	 * This method will check to see if the identified federate publishes the given object class.
	 */
	public boolean isObjectClassPublished( int federateHandle, int classHandle )
	{
		return isObjectClassRegistered( pObjects, federateHandle, classHandle );
	}

	/**
	 * Returns <code>true</code> if the federate publishes the identified attribute class. If the
	 * object or attribute classes can't be found, or the attribute isn't published,
	 * <code>false</code> is returned.
	 */
	public boolean isAttributeClassPublished( int federateHandle,
	                                          int classHandle,
	                                          int attributeHandle )
	{
		return isAttributeClassRegistered( pObjects, federateHandle, classHandle, attributeHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Object Subscription Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will record the subscription to a particular set of attribute for an identified
	 * federate. Checks to validate that each of the attribute exists in the object class exist
	 * will be run, with exceptions being thrown for transgressions.
	 */
	public void subscribeObjectClass( int federateHandle, int classHandle, Set<Integer> attributes )
		throws JObjectClassNotDefined, JAttributeNotDefined, JRTIinternalError
	{
		register( sObjects, "SUBSCRIBE-OBJECT", federateHandle, classHandle, attributes );
	}

	/**
	 * This method will record the subscription to a particular set of attributes for an identified
	 * federate. It will also associate the subscription of each attribute with the given region.
	 * Checks to validate that each of the attributes exists in the object class, with exceptions
	 * being thrown for transgressions. Checks will also be made to ensure that the specified region
	 * exists and is of a routing space that the FOM defines as valid for each attribute.
	 */
	public void subscribeObjectClass( int federateHandle,
	                                  int classHandle,
	                                  Set<Integer> attributes,
	                                  int regionToken )
		throws JObjectClassNotDefined,
		       JAttributeNotDefined,
		       JRegionNotKnown,
		       JInvalidRegionContext,
		       JRTIinternalError
	{
		register( sObjects,
		          "SUBSCRIBE-OBJECT-DDM",
		          federateHandle,
		          classHandle,
		          attributes,
		          regionToken );
		return;
	}
	
	/**
	 * This method will remove any object subscription record the specified federate had in the
	 * object class identified by the given handle. If the class doesn't exist in the FOM, or
	 * the particular federate didn't have a publication interest in it, an exception will be
	 * thrown.
	 */
	public void unsubscribeObjectClass( int federateHandle, int classHandle )
		throws JObjectClassNotDefined,
		       JObjectClassNotSubscribed
	{
		try
		{
			unregister( sObjects, "UNSUBSCRIBE-OBJECT", federateHandle, classHandle );
		}
		catch( NoRegistration nr )
		{
			throw new JObjectClassNotSubscribed( nr.getMessage() );
		}
	}

	/**
	 * This method will remove any object subscription record the specified federate had in the
	 * object class identified by the given handle. If the class doesn't exist in the FOM, or
	 * the particular federate didn't have a publication interest in it, an exception will be
	 * thrown.  Checks will also be made to ensure that the specified region exists and is of a
	 * routing space that the FOM defines as valid for each attribute.
	 */
	public void unsubscribeObjectClass( int federateHandle, int classHandle, int regionToken )
		throws JObjectClassNotDefined,
		       JObjectClassNotSubscribed,
		       JRegionNotKnown,
		       JInvalidRegionContext
	{
		try
		{
			unregister( sObjects, "UNSUBSCRIBE-OBJECT", federateHandle, classHandle, regionToken );
		}
		catch( NoRegistration nr )
		{
			throw new JObjectClassNotSubscribed( nr.getMessage() );
		}
	}
	
	/**
	 * This method will remove the subscription record associated with the identified federate in
	 * the given attributes of the given class. If other attributes in the class were also
	 * subscribed, their record will remain after this call. If the given set is either empty or
	 * <code>null</code>, then the entire subscription (for *ALL* attributes of the class) will be
	 * removed. If the object class cannot be found, or a subscription record didn't exist, an
	 * exception will be thrown.
	 */
	public void unsubscribeObjectClass( int federateHandle,
	                                    int classHandle,
	                                    Set<Integer> attributes )
		throws JObjectClassNotDefined,
		       JObjectClassNotSubscribed
	{
		try
		{
			unregister( sObjects, "UNSUBSCRIBE-OBJECT", federateHandle, classHandle, attributes );
		}
		catch( NoRegistration nr )
		{
			throw new JObjectClassNotSubscribed( nr.getMessage() );
		}
	}

	/**
	 * Get all the attributes that are currently subscribe by the identified federate handle for
	 * the given class. If the class isn't published by the federate, an exception is thrown.
	 */
	public Set<Integer> getSubscribedAttributes( int federateHandle, int classHandle )
		throws JObjectClassNotDefined,
		       JObjectClassNotSubscribed
	{
		try
		{
			return getRegisteredAttributes( sObjects, "SUBSCRIBE-OBJECT", federateHandle, classHandle );
		}
		catch( NoRegistration nr )
		{
			throw new JObjectClassNotSubscribed( nr.getMessage() );
		}
	}
	
	/**
	 * Get the {@link OCInterest} the given federate has in the given object class. If the federate
	 * isn't subscribed, an exception will be thrown, otherwise the interest object will be returned
	 */
	public OCInterest getSubscribedInterest( int federateHandle, OCMetadata objectClass )
		throws JObjectClassNotSubscribed
	{
		OCInterest interest = sObjects.get( objectClass );
		if( interest == null )
			throw new JObjectClassNotSubscribed( "federate has no interest in "+objectClass );
		
		return interest;
	}

	/**
	 * This method will generally be used to determine the type of class a federate should discover
	 * a newly registered object as. Just because a federate isn't subscribed directly to the type
	 * of the new object doesn't mean it shouldn't get the discovery. It could be subscribed to a
	 * parent type, and thus discover it as that type.
	 * <p/>
	 * This method will find and return the most specific {@link OCMetadata} type representing the
	 * object class that the federate subscribes to. It will start with the class identified by the
	 * given handle. If the federate subscribes to that class, the metadata for it will be returned.
	 * If it doesn't, this method will move on to the classes parent (returning the metadata for
	 * that class if the federate subscribes to it) and so on until there are no more parents to
	 * check. If this happens, null will be returned. If the initial class cannot be found, null
	 * will be returned.
	 * 
	 * @param federateHandle The handle of the federate we are searching for a subscription for
	 * @param initialClass The handle of the class to start looking for subscription data with
	 * @return The OCMetadata of the most specific object class that the federate is subscribed to
	 *         or null if discovery is not possible with the current subscription interest
	 */
	public OCMetadata getDiscoveryType( int federateHandle, int initialClass )
	{
		// get the metadata for the original class so we can return it if necessary
		OCMetadata clazz = fom().getObjectClass( initialClass );
		if( clazz == null )
			return null; // don't know what the initial class is :S
		
		// check to see if the class is subscribed, if it isn't, move on to its parent and check.
		// keep doing this until we find the first class that is subscribed (and return it) or
		// until we run out of parents to check
		while( true )
		{
			if( isObjectClassSubscribedDirectly(federateHandle,clazz.getHandle()) )
			{
				return clazz;
			}
			else
			{
				clazz = clazz.getParent();
				if( clazz == null )
					return null; // no more parents
			}
		}
	}
	
	/**
	 * This method will check to see if the identified federate subscribes to the object class. If
	 * the exact class isn't subscribed, the method will check up the inheritence hierarchy to see
	 * if a parent class is subscribed to.
	 * 
	 * @return <code>true</code> if the identified federate subscribes to the class of the given
	 *         handle, OR, to any of the parent classes
	 */
	public boolean isObjectClassSubscribed( int federateHandle, int initialClass )
	{
		OCMetadata clazz = fom().getObjectClass( initialClass );
		if( clazz == null )
			return false;
		
		while( clazz != null )
		{
			if( isObjectClassSubscribedDirectly( federateHandle, clazz.getHandle()) )
				return true;
			else
				clazz = clazz.getParent();
		}
		
		return false;
	}

	/**
	 * The same as {@link #isObjectClassSubscribed(int, int)} except that it <b>DOES NOT</b> take
	 * inheritance into account when making its decision.
	 * 
	 * @return <code>true</code> if the identified federate published the given class handle, 
	 *         <code>false</code> otherwise.
	 */
	public boolean isObjectClassSubscribedDirectly( int federateHandle, int classHandle )
	{
		return isObjectClassRegistered( sObjects, federateHandle, classHandle );
	}

	/**
	 * Returns <code>true</code> if the federate subscribes to the identified attribute class. If
	 * the object or attribute classes can't be found, or the attribute isn't subscribed,
	 * <code>false</code> is returned.
	 */
	public boolean isAttributeClassSubscribed( int federateHandle,
	                                           int classHandle,
	                                           int attributeHandle )
	{
		return isAttributeClassRegistered( sObjects, federateHandle, classHandle, attributeHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Interaction Publication Methods ////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will record the publication of the identified interaction class for the given
	 * federate. It will also validate that the interaction class exists in the FOM and will throw
	 * an exception if it isn't.
	 */
	public void publishInteractionClass( int federateHandle, int classHandle )
		throws JInteractionClassNotDefined,
		       JRTIinternalError
	{
		register( pInteractions, "PUBLISH-INTERACTION", federateHandle, classHandle );
	}
	
	/**
	 * This method will remove any interaction publication record the specified federate had in the
	 * interaction class identified by the given handle. If the class doesn't exist in the FOM, or
	 * the particular federate didn't have a publication interest in it, an exception will be
	 * thrown.
	 */
	public void unpublishInteractionClass( int federateHandle, int classHandle )
		throws JInteractionClassNotDefined,
		       JInteractionClassNotPublished
	{
		try
		{
			unregisterInteraction( pInteractions,
			                       "UNPUBLISH-INTERACTION",
			                       federateHandle,
			                       classHandle );
		}
		catch( NoRegistration nr )
		{
			throw new JInteractionClassNotPublished( nr.getMessage() );
		}
	}
	
	/**
	 * This method will check to see if the identified federate publishes the interaction class.
	 */
	public boolean isInteractionClassPublished( int federateHandle, int classHandle )
	{
		return isInteractionClassRegistered( pInteractions, federateHandle, classHandle );
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////// Interaction Subscription Methods ////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will record the subscription to the identified interaction class for the given
	 * federate. It will also validate that the interaction class exists in the FOM and will throw
	 * an exception if it isn't.
	 */
	public void subscribeInteractionClass( int federateHandle, int classHandle )
		throws JInteractionClassNotDefined,
		       JRTIinternalError
	{
		register( sInteractions, "SUBSCRIBE-INTERACTION", federateHandle, classHandle );
	}
	
	/**
	 * This method will record the subscription to the identified interaction class for the given
	 * federate. It will also validate that the interaction class exists in the FOM and will throw
	 * an exception if it isn't. If the given <code>regionToken</code> doesn't identify a known
	 * region, or the region's routing space isn't declared in the FOM for the interaction class,
	 * exceptions are thrown. 
	 */
	public void subscribeInteractionClass( int federateHandle, int classHandle, int regionToken )
		throws JInteractionClassNotDefined,
		       JRegionNotKnown,
		       JInvalidRegionContext,
		       JRTIinternalError
	{
		register( sInteractions,
		          "SUBSCRIBE-INTERACTION-DDM",
		          federateHandle,
		          classHandle,
		          regionToken );
	}

	/**
	 * This method will remove any interaction subscription record the specified federate had in the
	 * interaction class identified by the given handle. If the class doesn't exist in the FOM, or
	 * the particular federate didn't have a subscription interest in it, an exception will be
	 * thrown.
	 */
	public void unsubscribeInteractionClass( int federateHandle, int classHandle )
		throws JInteractionClassNotDefined,
		       JInteractionClassNotSubscribed
	{
		try
		{
			unregisterInteraction( sInteractions,
			                       "UNSUBSCRIBE-INTERACTION",
			                       federateHandle,
			                       classHandle );
		}
		catch( NoRegistration nr )
		{
			throw new JInteractionClassNotSubscribed( nr.getMessage() );
		}
	}
	
	/**
	 * This method will remove any interaction subscription record the specified federate had in the
	 * interaction class identified by the given handle. If the class doesn't exist in the FOM, or
	 * the particular federate didn't have a subscription interest in it, an exception will be
	 * thrown. If the given <code>regionToken</code> doesn't identify a known region, or the
	 * region's routing space isn't declared in the FOM for the interaction class, exceptions are
	 * thrown.
	 */
	public void unsubscribeInteractionClass( int federateHandle, int classHandle, int regionToken )
		throws JInteractionClassNotDefined,
		       JInteractionClassNotSubscribed,
		       JRegionNotKnown
	{
		try
		{
			unregisterInteraction( sInteractions,
			                       "UNSUBSCRIBE-INTERACTION",
			                       federateHandle,
			                       classHandle,
			                       regionToken );
		}
		catch( NoRegistration nr )
		{
			throw new JInteractionClassNotSubscribed( nr.getMessage() );
		}
	}

	/**
	 * This method will check to see if the identified federate subscribes to the interaction class.
	 */
	public boolean isInteractionClassSubscribed( int federateHandle, int initialClass )
	{
		ICMetadata clazz = fom().getInteractionClass( initialClass );
		if( clazz == null )
			return false;
		
		while( clazz != null )
		{
			if( isInteractionClassSubscribedDirectly(federateHandle, clazz.getHandle()) )
				return true;
			else
				clazz = clazz.getParent();
		}
		
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the identified federate is subscribed directly to the given
	 * interaction class. This method will *NOT* check the inheritance hierarchy, it will only
	 * check for direct subscriptions.
	 */
	public boolean isInteractionClassSubscribedDirectly( int federateHandle, int classHandle )
	{
		return isInteractionClassRegistered( sInteractions, federateHandle, classHandle );
	}
	
	/**
	 * Just because a federate isn't subscribed directly to a particular interaction class doesn't
	 * mean that it isn't interested in incoming interactions of that type. The federate could be
	 * subscribed to a parent class. This method will find the interaction class the given federate
	 * is subscribed to that is closest to the interaction class represented by the given class
	 * handle. Starting with the initial class, it will climb the inheritance hierarchy until it
	 * either finds an interaction class that the federate is subscribed to, or runs out of parents
	 * to check for. If it finds a class, the {@link ICMetadata} for that type is returned. If it
	 * doesn't find one, <code>null</code> is returned.
	 * 
	 * @param federateHandle The handle of the federate who has the potential subscription
	 * @param initialClass The handle of the object class to start looking for subscriptions at
	 */
	public ICMetadata getSubscribedInteractionType( int federateHandle, int initialClass )
	{
		ICInterest interest = getSubscribedInteractionInterest( federateHandle, initialClass );
		if( interest == null )
			return null;
		else
			return interest.getInteractionClass();
	}
	
	/**
	 * This method is the same as {@link #getSubscribedInteractionType(int, int)} except that it
	 * will return the relevant {@link ICInterest} rather than the metadata of the type (you can
	 * get the metadata from the interest).
	 * 
	 * @param federateHandle The handle of the federate who has the potential subscription
	 * @param initialClass The handle of the object class to start looking for subscriptions at
	 */
	public ICInterest getSubscribedInteractionInterest( int federateHandle, int initialClass )
	{
		// get the metadata for the original class so we can return it if necessary
		ICMetadata clazz = fom().getInteractionClass( initialClass );
		if( clazz == null )
			return null; // don't know what the initial class is :S
		
		// check to see if the class is subscribed, if it isn't, move on to its parent and check.
		// keep doing this until we find the first class that is subscribed (and return it) or
		// until we run out of parents to check
		while( true )
		{
			ICInterest interest = sInteractions.get( clazz );
			if( interest != null && interest.hasInterest(federateHandle) )
				return interest;
			
			// there is no interest registered for this type of the federate itself hasn't
			// got an interest in it, move on up to the parent if there is one
			clazz = clazz.getParent();
			if( clazz == null )
				return null; // no more parents
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Private Helper Methods /////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * When the interest manager is initialized, the federate won't be joined to a federation,
	 * thus, there will be no object model to obtain. This method is just a utility that fetches
	 * the model from the {@link LRCState} via {@link LRCState#getFOM()}.
	 */
	private ObjectModel fom()
	{
		return this.state.getFOM();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( pObjects );
		output.writeObject( sObjects );
		output.writeObject( pInteractions );
		output.writeObject( sInteractions );
	}

	@SuppressWarnings("unchecked")
	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.pObjects      = (Map<OCMetadata,OCInterest>)input.readObject();
		this.sObjects      = (Map<OCMetadata,OCInterest>)input.readObject();
		this.pInteractions = (Map<ICMetadata,ICInterest>)input.readObject();
		this.sInteractions = (Map<ICMetadata,ICInterest>)input.readObject();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
	private class NoRegistration extends Exception
	{
		private static final long serialVersionUID = 98121116105109L;
		public NoRegistration( String message )
		{
			super( message );
		}
	}
}
