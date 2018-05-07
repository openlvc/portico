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
package org.portico.lrc.services.object.handlers.outgoing;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotPublished;
import org.portico.lrc.compat.JInvalidRegionContext;
import org.portico.lrc.compat.JObjectAlreadyRegistered;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.RegionInstance;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.object.msg.DiscoverObject;
import org.portico2.common.services.object.msg.RegisterObject;
import org.portico2.common.services.object.msg.ReserveObjectName;

@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=RegisterObject.class)
public class RegisterObjectHandler extends LRCMessageHandler
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
	public void initialize( Map<String,Object> properties )
	{
		super.initialize( properties );
	}
	
	public void process( MessageContext context ) throws Exception
	{
		RegisterObject request = context.getRequest( RegisterObject.class, this );
		int classHandle = request.getClassHandle();
		String objectName = request.getObjectName();

		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();

		if( logger.isDebugEnabled() )
		{
			String ddm = request.usesDDM() ? " (using ddm)" : "";
			logger.debug( "ATTEMPT Register instance of class ["+ocMoniker(classHandle)+"] " +
			              ddm+", name="+objectName);
		}

		// make sure the object class exists and we are publishing it
		OCMetadata objectClass = checkPublished( classHandle );

		// if a name was provided, check to make sure the name isn't already in use, and reserve
		// it for out use
		if( objectName != null )
		{
			reserveName( objectName );
		}
		
		// create a new OCInstance and store it locally
		Set<Integer> published = interests.getPublishedAttributes( lrcState.getFederateHandle(),
		                                                           classHandle );
		
		//FIXME Hangover from refactor
		OCInstance instance = null; //repository.newInstance( 666, objectClass, objectName, published );
		
		///////////////////////////////////////////
		// run the ddm checks and apply ddm data //
		///////////////////////////////////////////
		// check the ddm properties of the request
		if( request.usesDDM() )
			applyDdm( instance, request.getAttributes(), request.getRegionTokens() );
		

		repository.discoverInstance( instance, instance.getRegisteredType() );
		// notify the other federates that a new remote object has been created
		connection.broadcast( fill(new DiscoverObject(instance)) );
		context.success( instance );

		if( logger.isInfoEnabled() )
		{
			String ddm = request.usesDDM() ? " (using ddm)" : "";
			logger.info( "SUCCESS Register instance of class ["+ocMoniker(classHandle)+
			              "], object handle="+instance.getHandle() + ddm );
		}
	}

	/**
	 * This method will try to find the {@link OCMetadata} for the object class with the given
	 * handle, validate that this federate is publishing that class and then return the metadata.
	 */
	private OCMetadata checkPublished( int classHandle )
		throws JObjectClassNotDefined,
	           JObjectClassNotPublished,
	           JRTIinternalError
	{
		// validate that the class exists in the FOM
		OCMetadata oMetadata = lrcState.getFOM().getObjectClass( classHandle );
		if( oMetadata == null )
		{
			// there is no such object class, ObjectClassNotDefined
			throw new JObjectClassNotDefined( "class [" + classHandle + "] not in FOM" );
		}

		// validate that we are a publisher of this class
		if( interests.isObjectClassPublished(lrcState.getFederateHandle(),classHandle) == false )
		{
			// we are not a publisher
			throw new JObjectClassNotPublished( "class [" + classHandle + "] not published by [" +
			    lrcState.getFederateName() + "]" );
		}

		return oMetadata;
	}

	/**
	 * This method will run DDM related checks on the provided information. It will validate that
	 * each of the attributes exists, that each of the regions is known and can be associated with
	 * attributes of the type it is trying to be linked with, and will remove redundant
	 * information from the lists (e.g. if there are multiple entries for a single attribute, all
	 * but the first will be removed).
	 */
	private void applyDdm( OCInstance instance,
	                       List<Integer> attributes,
	                       List<Integer> regionTokens )
		throws JAttributeNotDefined,
		       JAttributeNotPublished,
		       JRegionNotKnown,
		       JInvalidRegionContext
	{
		// somewhere to store each of the attributes we process so we can determine if
		// an attribute has been provided more than once
		HashSet<Integer> encounteredAttributes = new HashSet<Integer>();
		
		OCMetadata objectClass = instance.getDiscoveredType();
		for( int i = 0; i < attributes.size(); i++ )
		{
			int attributeHandle = attributes.get( i );
			int regionToken = regionTokens.get( i );

			// have we encountered this attribute yet?
			if( encounteredAttributes.contains(attributeHandle) )
				continue;
			else
				encounteredAttributes.add( attributeHandle );

			// validate that the attribute exists
			ACMetadata attributeClass = objectClass.getAttribute( attributeHandle );
			if( attributeClass == null )
				throw new JAttributeNotDefined( "attribute="+attributeHandle+", class="+objectClass );

			// validate that the attribute is published
			if( !interests.isAttributeClassPublished(lrcState.getFederateHandle(),
			                                         objectClass.getHandle(),
			                                         attributeHandle) )
			{
				throw new JAttributeNotPublished( "attribute="+attributeHandle+", class="+objectClass );
			}

			// validate that we know about the region
			RegionInstance region = regions.getRegion( regionToken );
			if( region == null )
				throw new JRegionNotKnown( "token: " + regionToken );

			// validate that the region can be associated with attributes of this type
			if( attributeClass.getSpace() == null ||
			    attributeClass.getSpace().getHandle() != region.getSpaceHandle() )
			{
				// routing space for region not valid for attribute name
				throw new JInvalidRegionContext( "routing space for region [" + region.getToken() +
				    "] not valid for attribute [" + attributeClass + "]" );
			}
			
			// apply the region to the attribute
			instance.getAttribute(attributeHandle).setRegion( region );
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////// Name Registration and Reservation Methods ///////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This method will reserve an object name for this federate to use. Note that you can disable
	 * this through the RID and the method {@link PorticoConstants#isObjectNamingNegotiated()} will
	 * be used to check to see if this is so. Should this be disabled, this method will just return
	 * happily right away.
	 * <p/>
	 * <pre>
	 * Q: How the hell does distributed name reservation work?!
	 * A: The same was sync-point registration works. A little something like this:
	 *     Everyone registering an object with the same name goes through the same process:
	 *       1) Check if the name is available
	 *       2) Send out a reserve notification and wait for a response, but a timeout is OK
	 *           (note that the response never comes, this is key, it forces us to wait)
	 *       3) Check to see if the name is still available, if so, we're good
	 *
	 *   NOTE: The reservation notice message has its immediate processing flag set to true.
	 *         This is CRUCIAL because we don't want these messages sitting around in queues until
	 *         the LRC has time to get to them.
	 *
	 *   Is this 100% fool-proof? No. Does it work in 99.99% of practical sitautions? Yes.
	 *   Here is why. When a name reservation is received, all federates store it. If a reservation
	 *   for the same name already exists, they will only replace it if the handle of the requesting
	 *   federate is lower than the handle of the federate we've already recorded it for.
	 *
	 *   This is why we wait for a period of time and then check again. We give reservations for
	 *   the same name a chance to get to us and potentially gazzump our request. The other reason
	 *   we wait a period of time is for our reservation request to get to others. Once our request
	 *   has been received by another federate, that federate will be blocked from reserving that
	 *   name (even if its handle is lower) because of the first check. This way, we don't have to
	 *   worry about a lower handle federate coming in after we've waited out time and checked
	 *   again, but before the discovery has been sent out and processed, thus taking the name for
	 *   good.
	 *
	 *   So, the period of time doesn't have to be sufficient to cover this whole process, just
	 *   long enough for us to get other clashing reservations and for other LRCs to get the
	 *   notice of our intent (which, in the case of a clash, they'll ignore if the other is lower).
	 *   The only way to really trip this up (as far as I can figure while trying to think in this
	 *   noisy room) is if the message doesn't get through to a lower-handled federate before it
	 *   does the first name check and after our timeout is up. That's a pretty big delay, so I'm
	 *   comfortable that we'll be ok most of the time.
	 * </pre>
	 */
	public void reserveName( String name ) throws Exception
	{
		//////////////////////////////
		// check availability first //
		//////////////////////////////
		// check to see if the name is currently available
		int reservedBy = repository.getReserverOfName( name );
		if( reservedBy != PorticoConstants.NULL_HANDLE )
		{
			throw new JObjectAlreadyRegistered( "Can't reserve object name ["+name+"] for ["+
			                                    federateName()+"], already reserved for ["+
			                                    federateName(reservedBy)+"]" );
		}

		// not reserved, check to see if it is in use
		int objectHandle = repository.getObjectHandleForName( name );
		if( objectHandle != PorticoConstants.NULL_HANDLE )
		{
			throw new JObjectAlreadyRegistered( "Can't reserve object name ["+name+"] for ["+
			                                    federateName()+"], already used by object ["+
			                                    objectHandle+"]" );
		}

		//////////////////////
		// reserve the name //
		//////////////////////
		// skip this if name negotiation is disabled
		if( PorticoConstants.isObjectNamingNegotiated() == false )
			return;

		// send out the reservation request and wait for a period of time defined by
		// the connection (rather than us defining it arbitrarily)
		if( logger.isDebugEnabled() )
		{
			logger.debug( "Sending object name reservation request: name="+name+
			              ", federate="+moniker() );
		}

		ReserveObjectName reserve = new ReserveObjectName( name );
		connection.broadcastAndSleep( fill(reserve) );

		//////////////////////////////
		// check availability again //
		//////////////////////////////
		// we have sent the request and waited the appropriate amount of time to see if anyone
		// else wants to try and reserve the same name. If they have (and the handle of that
		// federate is lower than ours), the repository will have been updated to reflect it as
		// the owner of that, so we have to check again
		reservedBy = repository.getReserverOfName( name );
		if( reservedBy != PorticoConstants.NULL_HANDLE && reservedBy != federateHandle() )
		{
			throw new JObjectAlreadyRegistered( "Can't reserve object name ["+name+"] for ["+
			                                    federateName()+"], already reserved for ["+
			                                    federateName(reservedBy)+"]" );
		}

		// not reserved, check to see if it is in use
		objectHandle = repository.getObjectHandleForName( name );
		if( objectHandle != PorticoConstants.NULL_HANDLE )
		{
			throw new JObjectAlreadyRegistered( "Can't reserve object name ["+name+"] for ["+
			                                    federateName()+"], already used by object ["+
			                                    objectHandle+"]" );
		}
		
		// we're good to go!
		if( logger.isDebugEnabled() )
			logger.debug( "Object name ["+name+"] successfully reserved for ["+moniker()+"]" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
