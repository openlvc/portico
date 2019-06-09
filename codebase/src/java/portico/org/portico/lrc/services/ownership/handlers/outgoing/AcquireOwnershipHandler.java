/*
 *   Copyright 2009 The Portico Project
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
package org.portico.lrc.services.ownership.handlers.outgoing;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.LRCMessageHandler;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotPublished;
import org.portico.lrc.compat.JFederateOwnsAttributes;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.services.ownership.msg.AttributesUnavailable;
import org.portico.lrc.services.ownership.msg.OwnershipAcquired;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.MessageHandler;
import org.portico2.common.services.ownership.msg.AttributeAcquire;

/**
 * This handler processes requests to acquire attributes, be it through direct solicitation (in
 * service to the attributeOwnershipAcquisition() call) or only if they're available (in service
 * to the attributeOwnershipAcquisitionIfAvailable() call).
 * <p/>
 * <b>Requesting Remotely Owned Attributes</b>
 * <p/>
 * Blah, blah, blah
 * <p/>
 * <b>Requesting Ownership If Available</b>
 * <p/>
 * When requesting ownership of unowned attributes, the decision on whether this is allowed or
 * not can mostly be made entirely locally to the LRC. However, to avoid situations where two or
 * more federates attempt to take up ownership of the same unowned attribute/s at the same time,
 * some communication with the federation is necessary.
 * <p/>
 * In this situation, the usual Portico approach to resolving potential distributed conflicts is
 * used. The handler stores information about the request locally and then broadcasts out its
 * intent to the federation. Other LRCs receive this intent and store it locally (thus preventing
 * any calls they make after they receive this message from conflicting with the same attributes).
 * The original sender then waits for a period of time defined by the connection implementation in
 * use (rather than just defining some arbitrary time). Once this time expires, the handler checks
 * to see if it still has a "lock" on these attributes.
 * <p/>
 * This is necessary because if another federate sent out a request before that federate received
 * our request, then there would be a conflict (because it hadn't yet had a chance to store our
 * intent, and thus block the local federate from trying to take ownership). This race condition
 * is resolved by giving the requesting federate with the lowest handle precedence over any others.
 * During the waiting period, if another request to lock the attributes is received, the incoming
 * handler will check the federate handle of the incoming request and the federate handle of the
 * existing request and hand the lock over to whoever has the lowest handle. This is why the handler
 * must check again after it finishes waiting to see if it still has a lock on all the attributes it
 * requested (it might have been trumped on some or all of them in the mean time). For a full
 * discussion of this approach, see the architectural documentation on the Portico website.
 */
@MessageHandler(modules="lrc-base",
                keywords={"lrc13","lrcjava1","lrc1516","lrc1516e"},
                sinks="outgoing",
                messages=AttributeAcquire.class)
public class AcquireOwnershipHandler extends LRCMessageHandler
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
		// basic validity checks
		lrcState.checkJoined();
		lrcState.checkSave();
		lrcState.checkRestore();
		
		// get the request information
		AttributeAcquire request = context.getRequest( AttributeAcquire.class, this );
		int objectHandle = request.getObjectHandle();
		Set<Integer> attributes = request.getAttributes();
		
		if( logger.isDebugEnabled() )
		{
			String available = request.isIfAvailable() ? "] (if available)" : "]";
			logger.debug( "ATTEMPT Acquire ownership of attributes "+acMoniker(attributes)+
			              " in object ["+objectMoniker(objectHandle)+available );
		}
		
		// validate the information given in the request
		Set<Integer> validatedAttributes = validateRequest( objectHandle, attributes );
		
		// the request is good, process it
		if( request.isIfAvailable() )
		{
			// this is an if-available request, so we have to do a whole song and dance
			handleAcquireIfAvailable( request, objectHandle, validatedAttributes );
		}
		else
		{
			// split the requested attributes into those that are owned and those that
			// are not. use the typical "request-if-available" for those that are unowned
			// and the normal request for those that are owned
			Snapshot snapshot = getSnapshot( objectHandle, attributes );
			if( snapshot.containsUnowned() )
			{
    			AttributeAcquire ifAvailableRequest =
    				new AttributeAcquire( objectHandle, snapshot.unowned, true );
    			if( logger.isDebugEnabled() )
    			{
    				logger.debug( "Sub-requestIfAvailable for unowned attributes "+
    				              acMoniker(snapshot.unowned) );
    			}
    			handleAcquireIfAvailable( ifAvailableRequest, objectHandle, snapshot.unowned );
			}
			
			if( snapshot.containsOwned() )
			{
    			// record the request locally
    			request.setAttributes( snapshot.owned );
    			ownership.requestAcquisition( objectHandle, snapshot.owned, federateHandle() );
    			connection.broadcast( request );
    			if( logger.isDebugEnabled() )
    			{
    				logger.debug( "Sent direct ownership acquisition request for attributes "+
    				             acMoniker(snapshot.owned)+" of object ["+
    				             objectMoniker(objectHandle)+"] from federate ["+moniker()+"]" );
    			}
			}
		}
		
		context.success();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Handle isAvailable() Requests //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method handles the attribute acquisition if available process. As there is no single
	 * authority in the federation on who owns the attributes, multiple conflicting attempts to
	 * acquire ownership of unowned attributes can be made at the same time. For this reason,
	 * some cooperation among LRCs is required.
	 * <p/>
	 * Firstly, all unavailable attributes are stripped from the set being requested (such as
	 * those that are currently listed as owned attributes). If there are none left after this,
	 * no further action is taken. If there are some left, a notice claiming the attributes is
	 * broadcast to the federation, where is should be immediately processed by other LRCs causing
	 * them to block any local requests for those attributes.
	 * <p/>
	 * The sending federate then waits for a period of time defined by the connection implementation
	 * in use to allow requests that were sent at the same time to filter in. As the local notice
	 * won't reach all federates immediately, we have to guard against conflicting requests that
	 * got into the network before other LRCs saw we wanted the attributes (and thus blocked their
	 * local federates from requesting them). This problem is solved the same way other such
	 * problems like sync-point registration is solved. If, during the wait, one of these requests
	 * finally hits the local LRC, the federate with the lower handle is given precedence, replacing
	 * the local request for any attributes it wants
	 * <p/>
	 * Once the wait is over, this method checks to see which attributes its entitiled to, which
	 * may or may not be all the ones that were available when it started. Callbacks to tell the
	 * federate which attributes it has acquired and which are unavailable are then queued.
	 * <p/>
	 * Notifications of aquired attributes are not queued but rather broadcast out to all federates.
	 * This is necessary so the other federates can record the change of hands in their internal
	 * stores.
	 * <p/>
	 * This method will return the set of attributes that could not be acquired.
	 */
	private Set<Integer> handleAcquireIfAvailable( AttributeAcquire request,
	                                               int object,
	                                               Set<Integer> attributes ) throws Exception
	{
		// remove any attributes that are owned by someone else
		Snapshot results = getSnapshot( object, attributes );
		Set<Integer> available = results.unowned;
		Set<Integer> unavailable = results.owned;

		// if there are no available attributes left to request, don't keep going,
		// just return an empty set as we couldn't get any
		if( available.isEmpty() )
		{
			if( logger.isInfoEnabled() )
			{
				logger.info( "FAILURE Can't take ownership of attributes " +
				             acMoniker(attributes)+" in object ["+objectMoniker(object)+
				             "]: NONE available" );
			}
			
			// queue up the unavailable notification
			AttributesUnavailable failureNotice = new AttributesUnavailable( object, unavailable );
			fill( failureNotice );
			lrcState.getQueue().offer( failureNotice );
			return unavailable;
		}

		// record our intent locally
		ownership.requestAcquisitionIfAvailable( object, available, federateHandle() );

		if( logger.isDebugEnabled() )
		{
			String filtered = "";
			if( unavailable.isEmpty() == false )
				filtered = " (unavailable: " + acMoniker( attributes ) + ")";

			logger.debug( "Broadcasting request to take ownership of unowned attributes "+
			              acMoniker(available)+" in object ["+objectMoniker(object)+
			              "] by federate ["+moniker()+"]"+filtered );
		}

		// broadcast out our intention and wait a connection-defined period for a response
		AttributeAcquire intention = new AttributeAcquire( object, available, true );
		connection.broadcastAndSleep( fill(intention) );

		// we've waiting long enough, see if we still have a lock on any of the attributes
		// we wanted and queue up the notifications
		Set<Integer> obtained = ownership.completeAcquisitionIfAvailable( object,federateHandle() );
		if( obtained.isEmpty() == false )
		{
			// change ownership of the objects
			changeOwnershipToUs( object, obtained );
			
			// queue the aquired notification
			OwnershipAcquired acquiredNotice = new OwnershipAcquired( object, obtained, true );
			fill( acquiredNotice );
			connection.broadcast( acquiredNotice );
		}

		// queue up the unavailable notification if required
		if( unavailable.isEmpty() == false )
		{
			AttributesUnavailable failureNotice = new AttributesUnavailable( object, unavailable );
			fill( failureNotice );
			lrcState.getQueue().offer( failureNotice );
		}

		// log messages based on the outcome
		if( logger.isDebugEnabled() )
		{
			String failed = "";
			if( unavailable.isEmpty() == false )
				failed = " (unavailable: "+acMoniker(unavailable)+")";

			logger.debug( "Ownership of attributes "+acMoniker(obtained)+" in object ["+
			              objectMoniker(object)+"] assigned to ["+moniker()+"]"+failed );
		}
		
		return unavailable;
	}

	/**
	 * Changes the owner of the attributes inside the object with the given handle to be the
	 * local federate. If the object or any of the attributes cannot be found, a warning is
	 * logged but no exception is thrown.
	 */
	private void changeOwnershipToUs( int objectHandle, Set<Integer> attributes )
	{
		OCInstance objectInstance = repository.getInstance( objectHandle );
		if( objectInstance == null )
		{
			logger.warn( "Can't change owner of attributes "+acMoniker(attributes)+" to ["+
			             moniker()+"]: object unknown or undiscovered" );
		}
		
		for( Integer attributeHandle : attributes )
		{
			ACInstance attributeInstance = objectInstance.getAttribute( attributeHandle );
			if( attributeInstance != null )
			{
				attributeInstance.setOwner( federateHandle() );
			}
			else
			{
				logger.warn( "Can't change owner of attribute "+acMoniker(attributeHandle)+" to ["+
				             moniker()+"]: can't find it in ["+objectMoniker(objectHandle)+"]" );
				continue;
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////// Helper Methods /////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method looks at the current ownership transfter status of the given attributes in the
	 * given object. It returns snapshot that contains information about which of the attributes
	 * are currently owned and which are unowned (and not under an acquisition request).
	 */
	private Snapshot getSnapshot( int objectHandle, Set<Integer> attributes )
	{
		OCInstance objectInstance = repository.getInstance( objectHandle );
		Set<Integer> available = new HashSet<Integer>();
		Set<Integer> unavailable = new HashSet<Integer>();
		for( Integer attribute : attributes )
		{
			// is the attribute already owned?
			// is the attribute already under an aquisition request by another federate?
			if( objectInstance.getAttribute(attribute).isUnowned() &&
			    ownership.isAttributeUnderAcquisitionRequest(objectHandle,attribute) == false )
			{
				available.add( attribute );
			}
			else
			{
				unavailable.add( attribute );
			}
		}

		return new Snapshot( available, unavailable );
	}
	
	/**
	 * Validates the information given in the request. This method will check to see that all
	 * the attributes exist, that they are all published by the local federate and  that none
	 * are currently owned by the local federate 
	 */
	private Set<Integer> validateRequest( int objectHandle, Set<Integer> attributes )
		throws JAttributeNotDefined,
		       JFederateOwnsAttributes,
		       JAttributeNotPublished,
		       JObjectClassNotPublished,
		       JObjectClassNotDefined,
		       JObjectNotKnown
	{
		// make sure we have discovered the object, it's not enough for it to just
		// exist, we have to know about it
		OCInstance instance = repository.getInstance( objectHandle );
		if( instance == null )
		{
			throw new JObjectNotKnown( "can't aquire attributes of object "+
			                           objectMoniker(objectHandle)+": unknown (or undiscovered)" );
		}

		int classHandle = instance.getDiscoveredClassHandle();
		// are we publishing this particular attribute?
		if( !interests.isObjectClassPublished(federateHandle(), classHandle) )
		{
			throw new JObjectClassNotPublished( "can't aquire attributes of ["+
			                                    ocMoniker(classHandle)+"]: not published" );
		}
		
		/////////////////////////////////////////////////////////
		// check: valid attributes and current not owned by us //
		/////////////////////////////////////////////////////////
		// make sure all the attributes are valid and that we don't already own them
		Set<Integer> returnSet = new HashSet<Integer>();
		for( Integer expected : attributes )
		{
			ACInstance attributeInstance = instance.getAttribute( expected );
			if( attributeInstance == null )
			{
				throw new JAttributeNotDefined( "can't aquire attribute "+acMoniker(expected)+
				                                ": not valid for object type "+ocMoniker(classHandle) );
			}
			
			// do we already own them?
			if( attributeInstance.getOwner() == federateHandle() )
			{
				throw new JFederateOwnsAttributes( "can't aquire attribute "+acMoniker(expected)+
				                                   ": federate already owns them" );
			}
			
			if( !interests.isAttributeClassPublished(federateHandle(),classHandle,expected) )
			{
				throw new JAttributeNotPublished( "can't aquire attribute "+acMoniker(expected)+
				                                  ": not published" );
			}
			
			returnSet.add( expected );
		}

		return returnSet;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private class Snapshot
	{
		private Set<Integer> unowned;
		private Set<Integer> owned;
		private Snapshot( Set<Integer> unowned, Set<Integer> owned )
		{
			this.unowned = unowned;
			this.owned = owned;
		}
		
		private boolean containsOwned()
		{
			return this.owned.isEmpty() == false;
		}
		
		private boolean containsUnowned()
		{
			return this.unowned.isEmpty() == false;
		}
	}
}
