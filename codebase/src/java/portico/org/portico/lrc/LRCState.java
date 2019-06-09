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
package org.portico.lrc;

import org.portico.lrc.compat.JConcurrentAccessAttempted;
import org.portico.lrc.compat.JEnableTimeConstrainedPending;
import org.portico.lrc.compat.JEnableTimeRegulationPending;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JFederationTimeAlreadyPassed;
import org.portico.lrc.compat.JInvalidFederationTime;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JRestoreInProgress;
import org.portico.lrc.compat.JSaveInProgress;
import org.portico.lrc.compat.JSynchronizationLabelNotAnnounced;
import org.portico.lrc.compat.JTimeAdvanceAlreadyInProgress;
import org.portico.lrc.management.Federate;
import org.portico.lrc.management.Federation;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.notifications.INotificationListener;
import org.portico.lrc.notifications.NullNotificationListener;
import org.portico.lrc.services.mom.data.MomManager;
import org.portico.lrc.services.saverestore.data.Manifest;
import org.portico.lrc.services.saverestore.data.RestoreManager;
import org.portico.lrc.services.saverestore.data.SaveManager;
import org.portico.lrc.services.saverestore.data.SaveRestoreTarget;
import org.portico.lrc.services.saverestore.data.Serializer;
import org.portico.lrc.services.sync.data.SyncPointManager;
import org.portico2.common.services.ddm.data.RegionStore;
import org.portico2.common.services.federation.msg.RoleCall;
import org.portico2.common.services.ownership.data.OwnershipManager;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.rti.services.object.data.Repository2;
import org.portico2.rti.services.time.data.TimeManager;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Properties;

/**
 * This class contains all the state information that relates to a specific {@link LRC} instance.
 * Rather than pollute the {@link LRC} code with all this state-data, it was factored into this
 * separate class. The data here represents the state of the LRC (and the federate that is using
 * it) at any given time. Further entities are stored and accessible through here, including the
 * {@link Repository2} that contains a cache of all the relevant object and attribute
 * information that the LRC knows about (such as handles, owners, etc...). Also contained is the
 * {@link InterestManager}, which holds all the publication and subscription interests for the
 * federate.
 * <p/>
 * The {@link LRCState} class also contains a number of helper methods that can perform common
 * validation checks and can throw the appropriate exception types if the LRC is currently not
 * in a valid state for a particular action to occur.
 * <p/>
 * The {@link LRCState} also provides a facility that allows arbitrary properties to be stored
 * within it. This is intended to provided a baseline facility that plugins can use to store and
 * fetch information (rather than having to come up with something of their own). See the
 * getProperty and setProperty method families.
 */
public class LRCState extends NullNotificationListener implements SaveRestoreTarget, INotificationListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected LRC theLRC;
	protected LRCMessageQueue messageQueue;
	
	// Properties //
	// This store can be used by plugins/handlers if they need to store data in an LRC
	private Properties properties;
	
	// Basic settings //
	private String  federateName;
	private String  federateType;
	private int     federateHandle;
	private String  federationName;
	private boolean joined;

	// Object Model //
	private ObjectModel fom;
	private MomManager momManager;
	
	// Time related settings //
	private TimeManager timeManager;
	private TimeStatus timeStatus;
	private boolean ticking;
	private boolean callbacksEnabled;
	private boolean immediateCallbacks;
	
	// Pub&Sub settings //
	private InterestManager interestManager;
	
	// Sync point settings //
	private SyncPointManager syncPointManager;
	
	// Instance Repository //
	private Repository2 repository;
	private int latestObjectHandle;
	private int maxObjectHandle;
	
	// Ownership settings //
	private OwnershipManager ownershipManager;
	
	// Save/Restore settings //
	private Serializer serializer;
	private Manifest manifest;
	private SaveManager saveManager;
	private RestoreManager restoreManager;
	
	// DDM state entities //
	private RegionStore regionStore;
	private int latestRegionToken;
	private int maxRegionToken;
	
	// Record of other federates //
	private Federation federation;

	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Create a new {@link LRCState} that is linked to the given {@link LRC}. This constructor calls
	 * the {@link #reinitialize()} method once it is finished in order to set up the initial state.
	 */
	public LRCState( LRC theLRC )
	{
		this.theLRC = theLRC;
		this.reinitialize();
	}

	/**
	 * This method clears out any previous status that was contained within the instance. It is used
	 * to given the {@link LRCState} its initial values during construction, and it also used to
	 * clear out any stale data once a federate resigns from a federation (so that when it joins
	 * again, it does so with a clean slate).
	 */
	protected void reinitialize()
	{
		// the manifest must be initialized before the other components so that they can
		// add themselves to it as appropriate
		this.manifest = new Manifest();

		this.messageQueue = new LRCMessageQueue( this );

		// set all the default values //
		this.federateName = "not-joined";
		this.federateType = "not-joined";
		setFederateHandle( PorticoConstants.NULL_HANDLE );
		this.federationName = null;
		this.joined = false;
		this.fom = null;
		this.momManager = new MomManager( this,
		                                  theLRC.getSpecHelper().getHlaVersion(),
		                                  theLRC.logger );
		
		// Time related settings //
		this.timeManager = new TimeManager();
		this.timeStatus = new TimeStatus(); // give us a dummy status with default values for now
		this.ticking = false;
		this.callbacksEnabled = true;
		//this.immediateCallbacks = false; -- don't reinitialize this one, we want it to persist
		
		// Pub&Sub settings //
//MOVED	this.interestManager = new InterestManager( this );
		
		// Sync PointSetting //
		this.syncPointManager = new SyncPointManager();
		
		// Save & Restore Settings //
		this.serializer = new Serializer( theLRC.logger );
		this.saveManager = new SaveManager();
		this.restoreManager = new RestoreManager();
		
		// Instance Repository //
//		this.repository = new Repository( this );
		this.latestObjectHandle = 0;
		this.maxObjectHandle = 0;
		
		// Ownership settings //
		this.ownershipManager = new OwnershipManager();
		
		// Region Store //
//		this.regionStore = new RegionStore( this );
		this.latestRegionToken = 0;
		this.maxRegionToken = 0;
		
		// Other Federates Records //
		this.federation = new Federation( this );
		
		//////////////
		// Manifest //
		//////////////
		// add items in the order you want them saved/restored
		manifest.addTarget( federation );
		manifest.addTarget( messageQueue );
		manifest.addTarget( momManager ); // after this.federation
		manifest.addTarget( syncPointManager );
		manifest.addTarget( timeManager );
		manifest.addTarget( regionStore );
		manifest.addTarget( interestManager );
		manifest.addTarget( repository );
		manifest.addTarget( ownershipManager );
		
		// add this in last place so that we can restore links to any stuff we cache
		// from other components by getting it directly from them after they've restored
		// for example: the time status must be restored from the time manager and this
		// can only be done after the time manager has restored
		manifest.addTarget( this );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Notification Methods ////////////////////////////
	/////////////////////////////////////////////////////////////////////////////
	/**
	 * This notification is invoked when the local federate joins a federation. In the LRCState,
	 * it will cache the given information and set up things like the local time status etc...
	 */
	@Override
	public void localFederateJoinedFederation( int federateHandle,
	                                           String federateName,
	                                           String federateType,
	                                           String federationName,
	                                           ObjectModel fom )
	{
		setFederateHandle( federateHandle );
		this.federateName = federateName;
		this.federateType = federateType;
		this.federationName = federationName;
		this.fom = fom;
		this.joined = true;
		
		// tell the time manager that we've joined and cache the local state
		timeManager.joinedFederation( federateHandle, null );
		this.timeStatus = timeManager.getTimeStatus( federateHandle );
		
		// tell the save/restore managers that the local federate is in here
		saveManager.joinedFederation( federateHandle );
		restoreManager.joinedFederation( federateHandle );
		
		// tell the mom manager that things are a go
		momManager.connectedToFederation();
		
		// add us to our "known federate" map (as we know about ourselves :P)
		Federate federate = new Federate( this, federateHandle, federateName, federateType );
		federation.addFederate( federate );
		momManager.federateJoinedFederation( federate );
	}
	
	/**
	 * This notification is invoked when a remote federate joins the federation the local federate
	 * is already joined to.
	 */
	@Override
	public void remoteFederateJoinedFederation( RoleCall federateStatus )
	{
		int remoteHandle = federateStatus.getSourceFederate();
		String remoteName = federateStatus.getFederateName();
		String remoteType = federateStatus.getFederateType();
		
		// store the management and MOM information
		Federate federate = new Federate( this, remoteHandle, remoteName, remoteType );
		federation.addFederate( federate );
		momManager.federateJoinedFederation( federate );
		
		// notify the managers that need to know about this
		timeManager.joinedFederation( remoteHandle, federateStatus.getTimeStatus() );
		saveManager.joinedFederation( remoteHandle );
		restoreManager.joinedFederation( remoteHandle );

		// merge in additional FOM modules if we have them
		if( federateStatus.hasAdditionalFomModules() )
			mergeAdditionalFomModules( federateStatus.getAdditionalFomModules(), remoteName );
	}

	/**
	 * This notificatoin is called when the local federate resigns from a federation.
	 */
	@Override
	public void localFederateResignedFromFederation()
	{
		this.theLRC.reinitialize();
	}

	/**
	 * This notification is called when a remote federate resigns from the federation. The handle
	 * of the federate that resigned it passed so that it can be identified.
	 */
	@Override
	public void remoteFederateResignedFromFederation( int federateHandle, String federateName )
	{
		Federate federate = this.federation.removeFederate( federateHandle );
		if( federate != null )
			momManager.federateResignedFederation( federate );
	}

	/**
	 * When an IEEE-1516e federate joins a federation, it can optionally provide an additional
	 * set of FOM modules. This method handles the merging of additional modules into the existing
	 * FOM. It is presumed that the merge has been validated before the RoleCall is sent out.
	 * 
	 * @param modules The new modules to merge in
	 * @param remoteFederate The federate that joined with the new modules
	 */
	private void mergeAdditionalFomModules( List<ObjectModel> modules, String remoteFederate )
	{
		theLRC.logger.debug( "Merging "+modules.size()+" additional FOM modules from ["+
		                     remoteFederate+"]" );

		try
		{
			modules.add( 0, this.fom );
			this.fom.unlock();
			this.fom = ModelMerger.merge( modules );
			this.fom.lock();
		}
		catch( Exception e )
		{
			theLRC.logger.error( "Failed to merge additional FOM modules from remote federate ["+
			                     remoteFederate+"]: "+e.getMessage(), e );
			theLRC.logger.error( "Continuing in the hope that problems are limited" );
		}
	}

	////////////////////////////////////////////////////////////////////////////
	/////////////////////////// Convenience Methods ////////////////////////////
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Check to see if we are currently ticking (and thus not able to make an RTI callback). If
	 * we are currently ticking, a {@link JConcurrentAccessAttempted}
	 */
	public void checkAccess() throws JConcurrentAccessAttempted
	{
		if( !immediateCallbacks && ticking )
			throw new JConcurrentAccessAttempted( "Currently ticking" );
	}
	
	/**
	 * Check to see if we are advancing. If we are, throw an exception. 
	 */
	public void checkAdvancing() throws JTimeAdvanceAlreadyInProgress
	{
		if( timeStatus.isAdvanceRequestOutstanding() )
			throw new JTimeAdvanceAlreadyInProgress( "Currently advancing" );
	}
	
	/**
	 * Check to see if there is a time regulation enable pending. If there is, throw an exception 
	 */
	public void checkTimeRegulation() throws JEnableTimeRegulationPending
	{
		if( timeStatus.isRegulatingPending() )
			throw new JEnableTimeRegulationPending( "" );
	}
	
	/**
	 * Check to see if there is a time constrained enable pending. If there is, throw an exception 
	 */
	public void checkTimeConstrained() throws JEnableTimeConstrainedPending
	{
		if( timeStatus.isConstrainedPending() )
			throw new JEnableTimeConstrainedPending( "" );
	}
	
	/**
	 * Validate that the given time is valid for the current state (that it is equal to or greater
	 * than the current LBTS for <b>this federate</b>).
	 */
	public void checkValidTime( double time ) throws JInvalidFederationTime
	{
		// check that the time is greater than or equal to the current LBTS of this federate
		if( time < getFederateLbts() )
		{
			throw new JInvalidFederationTime( "Time [" + time + "] has already passed (lbts:" +
			                                  getFederateLbts() + ")" );
		}
	}
	
	/**
	 * Checks to see whether or not the LRC is currently in the middle of a save process. If it is,
	 * it throws a {@link JSaveInProgress} exception.
	 */
	public void checkSave() throws JSaveInProgress
	{
		if( this.saveManager.isInProgress() )
		{
			throw new JSaveInProgress( "Active federation save: label="+
			                           saveManager.getActiveLabel() );
		}
	}
	
	/**
	 * Checks to see whether or not the LRC is currently in the middle of a restore process. If it
	 * is, it throws a {@link JRestoreInProgress} exception.
	 */
	public void checkRestore() throws JRestoreInProgress
	{
		if( this.restoreManager.isInProgress() )
		{
			throw new JRestoreInProgress( "Active federation restore: label="+
			                              restoreManager.getActiveLabel() );
		}
	}
	
	/**
	 * This method checks to see if the federate associated with this LRC is joined
	 * to a federation. If it is not, a FederateNotExecutionMember exception is thrown. 
	 */
	public void checkJoined() throws JFederateNotExecutionMember
	{
		if( joined == false )
			throw new JFederateNotExecutionMember( "" );
	}
	
	/**
	 * Checks to see if the given synchronization point label has been announced 
	 */
	public void checkSyncAnnounced( String label ) throws JSynchronizationLabelNotAnnounced
	{
		if( syncPointManager.containsPoint(label) )
			throw new JSynchronizationLabelNotAnnounced( label );
	}

	/**
	 * Checks to see if the given time is greater than the current federation time. If it is,
	 * an exception is thrown.
	 */
	public void checkTimeNotInPast( double time ) throws JFederationTimeAlreadyPassed
	{
		if( getCurrentTime() > time )
		{
			throw new JFederationTimeAlreadyPassed( "current time is "+getCurrentTime()+
			                                        ", given time was "+time );
		}
	}
	
	////////////////////////////////////////////////////////////
	////////////////////// Basic Settings //////////////////////
	////////////////////////////////////////////////////////////
	public LRCMessageQueue getQueue()
	{
		return this.messageQueue;
	}
	
	public String getFederateName()
	{
		return federateName;
	}

	public String getFederateType()
	{
		return federateType;
	}
	
	public int getFederateHandle()
	{
		return this.federateHandle;
	}

	/**
	 * This will not only set the federate handle, but it will also notify the Kernel that
	 * its HLA ID has changed.
	 */
	public void setFederateHandle( int federateHandle )
	{
		this.federateHandle = federateHandle;
	}
	
	public String getFederationName()
	{
		return federationName;
	}

	public void setFederationName( String federationName )
	{
		this.federationName = federationName;
	}

	////////////////////////////////////////////////////////////
	////////////////// Time Shortcut Settings //////////////////
	////////////////////////////////////////////////////////////
	public boolean areCallbacksEnabled()
	{
		return this.callbacksEnabled;
	}
	
	public void setCallbacksEnabled( boolean enabled )
	{
		this.callbacksEnabled = enabled;
	}

	public boolean isImmediateCallbackDeliveryEnabled()
	{
		return this.immediateCallbacks;
	}

	/**
	 * The IEEE-1516 and 1516e standards provide facilities to allow the immediate delivery
	 * of callback messages rather than the usual asynchronous/tick delivery mechanism. To
	 * provide support for this, when the mode is enabled the LVCQueue itself will have an
	 * additional thread that will be used to deliver all callbacks immediately, rather than
	 * waiting for tick to be called (although we'll extract callback via the same poll()
	 * call to ensure we only release TSO messages at the appropriate time).
	 * <p/>
	 * This call will enable that mode and kick off a separate processing thread.
	 */
	public void setImmediateCallbackDelivery( boolean enabled )
	{
		this.immediateCallbacks = enabled;
	}

	public boolean isTicking()
	{
		return this.ticking;
	}
	
	public void setTicking( boolean ticking )
	{
		if( ticking == false )
		{
			// run the queue overflow check
			if( LRCProperties.LRC_QUEUE_WARNING_COUNT != -1 &&
				messageQueue.getSize() > LRCProperties.LRC_QUEUE_WARNING_COUNT )
			{
				theLRC.logger.warn( "WARNING Federate ["+federateName+"] Not ticking enough, "+
				                    "queue is getting fat! (size="+messageQueue.getSize()+
				                    ", warningLevel="+LRCProperties.LRC_QUEUE_WARNING_COUNT+")" );
			}
		}
		
		this.ticking = ticking;
	}

	public double getCurrentTime()
	{
		return timeStatus.getCurrentTime();
	}

	public double getRequestedTime()
	{
		return timeStatus.getRequestedTime();
	}

	public double getLookahead()
	{
		return timeStatus.getLookahead();
	}

	public double getFederateLbts()
	{
		return timeStatus.getLbts();
	}
	
	public boolean isRegulating()
	{
		return timeStatus.isRegulating();
	}
	
	////////////////////////////////////////////////////////////
	////////////////////// Misc  Settings //////////////////////
	////////////////////////////////////////////////////////////
	public boolean isJoined()
	{
		return this.joined;
	}
	
	public ObjectModel getFOM()
	{
		return this.fom;
	}
	
	public MomManager getMomManager()
	{
		return this.momManager;
	}
	
	public Manifest getManifest()
	{
		return this.manifest;
	}
	
	public Serializer getSerializer()
	{
		return this.serializer;
	}
	
	public SaveManager getSaveManager()
	{
		return this.saveManager;	
	}

	public RestoreManager getRestoreManager()
	{
		return this.restoreManager;
	}

	public TimeManager getTimeManager()
	{
		return this.timeManager;
	}
	
	public SyncPointManager getSyncPointManager()
	{
		return this.syncPointManager;
	}
	
	public TimeStatus getTimeStatus()
	{
		return this.timeStatus;
	}

	public InterestManager getInterestManager()
	{
		return this.interestManager;
	}
	
	public Repository2 getRepository()
	{
		return this.repository;
	}
	
	public OwnershipManager getOwnershipManager()
	{
		return this.ownershipManager;
	}
	
	public RegionStore getRegionStore()
	{
		return this.regionStore;
	}
	
	public Federation getFederation()
	{
		return this.federation;
	}

	public Federate getKnownFederate( int federateHandle )
	{
		return this.federation.getFederate( federateHandle );
	}

	/**
	 * Returns the next object handle available for this particular LRC when creating a new object.
	 * So that there is no need for distributed handle negotiation, each federate is pre-assigned a
	 * range of valid values based on its federate handle. The min/max values are calculated like
	 * so:
	 * 
	 * <ul>
	 *   <li>
	 *   <b>Min Value</b>: <code>((federateHandle-1)*{@link PorticoConstants#MAX_OBJECTS})+1</code>
	 *   (see MOM note below)
	 *   </li>
	 *   <li>
	 *   <b>Max Value</b>: <code>(federateHandle*{@link PorticoConstants#MAX_OBJECTS})-1</code>
	 *   </li>
	 * </ul> 
	 * 
	 * When a new value is requested, an internal counter is incremented and the value returned.
	 * If the new value should cross the max value, an exception is thrown.
	 * <p/>
	 * <b>NOTE:</b> The +1 is to allow a handle for the MOM object that is registered when a
	 * federate joins a federation. As the local LRC controls this particular object, the handle
	 * for the object comes out of the local federate's stash. Also note that the handle 0 is used
	 * to specify the federation MOM object.
	 * <p/>
	 * <b>NOTE:</b> Handles cannot currently be reused, so the max number of objects that can be
	 * registered for a particular federate basically means that max number of successful object
	 * registrations that can occur, not the max number of active/alive/non-deleted objects a
	 * federate can register. This problem will be addressed in the future.
	 */
	public int nextObjectHandle() throws JRTIinternalError
	{
		if( this.maxObjectHandle == 0 )
		{
			// initialize the max and current handle values
			latestObjectHandle = ((federateHandle-1) * PorticoConstants.MAX_OBJECTS)+1;
			maxObjectHandle =  (federateHandle * PorticoConstants.MAX_OBJECTS)-1;
		}
		
		++latestObjectHandle;
		if( latestObjectHandle > maxObjectHandle )
			throw new JRTIinternalError( "Execeeded max number of objects" );
		else
			return latestObjectHandle;
	}
	
	/**
	 * Returns the handle that shouhld be used by the MOM for registering its Federate object.
	 * The first handle in the range pre-assigned to each federate is kept for use by the MOM.
	 * 
	 * @return The handle the MOM can use when registering the Federate object.
	 */
	public int getMomFederateObjectHandle( int theFederateHandle )
	{
		// if this is the federate with the first handle, return 1, because 0 is used for
		// the Federation MOM object. Otherwise, just return the first object handle in the
		// range dedicated to the federate (the same was nextObjectHandle() determines it)
		if( theFederateHandle == 1 )
			return 1;
		else
			return (theFederateHandle-1) * PorticoConstants.MAX_OBJECTS;
	}

	/**
	 * Returns the next region token available for this particular LRC when creating a new region.
	 * So that there is no need for distributed token negotiation, each federate is pre-assigned a
	 * range of valid values based on its federate handle. The min/max values are calculated like
	 * so:
	 * 
	 * <ul>
	 *   <li>
	 *   <b>Min Value</b>: <code>(federateHandle-1)*{@link PorticoConstants#MAX_OBJECTS}</code>
	 *   </li>
	 *   <li>
	 *   <b>Max Value</b>: <code>((federateHandle)*{@link PorticoConstants#MAX_OBJECTS})-1</code>
	 *   </li>
	 * </ul> 
	 * 
	 * When a new value is requested, an internal counter is incremented and the value returned.
	 * If the new value should cross the max value, an exception is thrown.
	 * <p/>
	 * <b>NOTE:</b> Tokens cannot currently be reused, so the max number of regions that can be
	 * registered for a particular federate basically means that max number of successful create
	 * regions requests that can occur, not the max number of active/alive/non-deleted regions a
	 * federate can create. This problem will be addressed in the future.
	 */
	public int nextRegionToken() throws JRTIinternalError
	{
		if( maxRegionToken == 0 )
		{
			// initialize the max and current handle values
			latestRegionToken = (federateHandle-1) * PorticoConstants.MAX_REGIONS;
			maxRegionToken =  (federateHandle * PorticoConstants.MAX_REGIONS)-1;
		}
		
		++latestRegionToken;
		if( latestRegionToken > maxRegionToken )
			throw new JRTIinternalError( "Execeeded max number of regions" );
		else
			return latestRegionToken;
	}

	////////////////////////////////////////////////////////////
	//////////////////// Properties Methods ////////////////////
	////////////////////////////////////////////////////////////
	/**
	 * Add a property to the state. This facility is meant to be used by handlers/plugins that
	 * need to a place to store information. If the key already exists, it will <b>overwrite</b>
	 * any value that existed with the given value.
	 */
	public void setProperty( String key, Object value )
	{
		this.properties.put( key, value );
	}
	
	/**
	 * Fetch the value of a previously bound property. If there is no property for that key,
	 * null will be returned.
	 */
	public Object getProperty( String key )
	{
		return this.properties.get( key );
	}
	
	/**
	 * This is the same as {@link #getProperty(String)} except that you can specify the type which
	 * the contained value should be. If there is no value for that key or the type of the value
	 * does not match up with the given value, null is returned. Otherwise, the value is cast to
	 * the given type and returned.
	 */
	public <X> X getProperty( String key, Class<X> type )
	{
		Object value = this.properties.get( key );
		if( value != null && type.isInstance(value) )
		{
			return type.cast( value );
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Return true if there is a contained property for the given key, false otherwise.
	 */
	public boolean hasProperty( String key )
	{
		return this.properties.containsKey( key );
	}
	
	/**
	 * Gets the properties map directly.
	 */
	public Properties getProperties()
	{
		return this.properties;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void saveToStream( ObjectOutput output ) throws Exception
	{
		output.writeObject( properties );
		
		// Basic settings //
		output.writeInt( federateHandle );

		// Object Model //
		output.writeObject( fom );
		
		// Time related settings //
		//output.writeObject( timeStatus );
		output.writeBoolean( ticking );
		output.writeBoolean( immediateCallbacks );
		
		// Instance Repository //
		output.writeInt( latestObjectHandle );
		output.writeInt( maxObjectHandle );
		
		// DDM state entities //
		output.writeInt( latestRegionToken );
		output.writeInt( maxRegionToken );
	}

	public void restoreFromStream( ObjectInput input ) throws Exception
	{
		this.properties = (Properties)input.readObject();
		
		// Basic settings //
		this.federateHandle = input.readInt();

		// Object Model //
		this.fom = (ObjectModel)input.readObject();
		
		// Time related settings //
		this.timeStatus = timeManager.getTimeStatus( federateHandle );
		this.ticking = input.readBoolean();
		this.immediateCallbacks = input.readBoolean();
		
		// Instance Repository //
		this.latestObjectHandle = input.readInt();
		this.maxObjectHandle = input.readInt();
		
		// DDM state entities //
		this.latestRegionToken = input.readInt();
		this.maxRegionToken = input.readInt();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
