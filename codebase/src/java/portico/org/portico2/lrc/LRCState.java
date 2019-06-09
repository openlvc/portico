/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.lrc;

import org.portico.lrc.compat.JConcurrentAccessAttempted;
import org.portico.lrc.compat.JEnableTimeConstrainedPending;
import org.portico.lrc.compat.JEnableTimeRegulationPending;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JFederationTimeAlreadyPassed;
import org.portico.lrc.compat.JInvalidFederationTime;
import org.portico.lrc.compat.JRestoreInProgress;
import org.portico.lrc.compat.JSaveInProgress;
import org.portico.lrc.compat.JTimeAdvanceAlreadyInProgress;
import org.portico.lrc.model.ObjectModel;
import org.portico2.common.PorticoConstants;
import org.portico2.common.services.ddm.data.RegionStore;
import org.portico2.common.services.pubsub.data.InterestManager;
import org.portico2.common.services.time.data.TimeStatus;
import org.portico2.lrc.services.object.data.Repository;

public class LRCState
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	protected LRC theLRC;
	protected LRCMessageQueue messageQueue;
	private   boolean isQueueLogging; // debugging only
	
	// Basic settings //
	private String  federateName;
	private String  federateType;
	private int     federateHandle;
	private int     federationHandle;
	private String  federationName;
	private boolean joined;

	// Object Model //
	private ObjectModel fom;
//	private MomManager momManager;
	
	// Time related settings //
	private TimeStatus timeStatus;
	private boolean ticking;
	private boolean callbacksEnabled;
	private boolean immediateCallbacks;
	
	// Pub&Sub settings //
	private InterestManager interestManager;
	
	// Instance Repository //
	private Repository repository;
	
	// Ownership settings //
//	private OwnershipManager ownershipManager;
	
	// Save/Restore settings //
//	private Serializer serializer;
//	private Manifest manifest;
//	private SaveManager saveManager;
//	private RestoreManager restoreManager;
	
	// DDM state entities //
	private RegionStore regionStore;
	
	// MOM Settings //
	private boolean serviceReporting;
	private boolean exceptionReporting;
	private int serviceInvocationCounter;
	
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected LRCState( LRC theLRC )
	{
		this.theLRC = theLRC;
		this.isQueueLogging = false; // TODO Wire up to a configuration option
		this.reinitialize();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * This method clears out any previous status that was contained within the instance. It is used
	 * to given the {@link LRCState} its initial values during construction, and it also used to
	 * clear out any stale data once a federate resigns from a federation (so that when it joins
	 * again, it does so with a clean slate).
	 */
	protected void reinitialize()
	{
		// set all the default values //
		this.federateName = "not-joined";
		setFederateHandle( PorticoConstants.NULL_HANDLE );
		this.federationName = null;
		this.joined = false;
//		this.fom = null;
//		this.momManager = new MomManager( this,
//		                                  theLRC.getSpecHelper().getHlaVersion(),
//		                                  theLRC.logger );
//		
		// Time related settings //
		this.timeStatus = new TimeStatus(); // time status for local federate
		this.ticking = false;
		this.callbacksEnabled = true;
		//this.immediateCallbacks = false; -- don't reinitialize this one, we want it to persist

		// queue holding all incoming messages for the LRC
		this.messageQueue = new LRCMessageQueue( this );
		
		// Region Store //
		this.regionStore = new RegionStore();
		
		// Pub&Sub settings //
		this.interestManager = new InterestManager( null /*set on join*/, regionStore );
		
//		// Save & Restore Settings //
//		this.serializer = new Serializer( theLRC.logger );
//		this.saveManager = new SaveManager();
//		this.restoreManager = new RestoreManager();
		
		// Instance Repository //
		this.repository = new Repository( regionStore );
		
//		// Ownership settings //
//		this.ownershipManager = new OwnershipManager();
//		
//		//////////////
//		// Manifest //
//		//////////////
//		// add items in the order you want them saved/restored
//		manifest.addTarget( federation );
//		manifest.addTarget( messageQueue );
//		manifest.addTarget( momManager ); // after this.federation
//		manifest.addTarget( syncPointManager );
//		manifest.addTarget( timeManager );
//		manifest.addTarget( regionStore );
//		manifest.addTarget( interestManager );
//		manifest.addTarget( repository );
//		manifest.addTarget( ownershipManager );
//		
//		// add this in last place so that we can restore links to any stuff we cache
//		// from other components by getting it directly from them after they've restored
//		// for example: the time status must be restored from the time manager and this
//		// can only be done after the time manager has restored
//		manifest.addTarget( this );
	}

	/////////////////////////////////////////////////////////////////////////////
	///  Notification Methods   /////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////
	/**
	 * This notification is invoked when the local federate joins a federation. In the LRCState,
	 * it will cache the given information and set up things like the local time status etc...
	 */
	public void localFederateJoinedFederation( int federateHandle,
	                                           int federationHandle,
	                                           String federateName,
	                                           String federateType,
	                                           String federationName,
	                                           ObjectModel fom )
	{
		setFederateHandle( federateHandle );
		setFederationHandle( federationHandle );
		this.federateName = federateName;
		this.federateType = federateType;
		this.federationName = federationName;
		this.fom = fom;
		this.interestManager.setFOM( fom );
		this.joined = true;
		
		// tell the time manager that we've joined and cache the local state
		this.timeStatus.reset();
		
		// tell the save/restore managers that the local federate is in here
//		saveManager.joinedFederation( federateHandle );
//		restoreManager.joinedFederation( federateHandle );
		
		// tell the mom manager that things are a go
//		momManager.connectedToFederation();
		
		// add us to our "known federate" map (as we know about ourselves :P)
//		Federate federate = new Federate( this, federateHandle, federateName );
//		federation.addFederate( federate );
//		momManager.federateJoinedFederation( federate );
	}
	
	/**
	 * This notificatoin is called when the local federate resigns from a federation.
	 */
	public void localFederateResignedFromFederation()
	{
		this.theLRC.reinitialize();
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
//		if( this.saveManager.isInProgress() )
//		{
//			throw new JSaveInProgress( "Active federation save: label="+
//			                           saveManager.getActiveLabel() );
//		}
	}
	
	/**
	 * Checks to see whether or not the LRC is currently in the middle of a restore process. If it
	 * is, it throws a {@link JRestoreInProgress} exception.
	 */
	public void checkRestore() throws JRestoreInProgress
	{
//		if( this.restoreManager.isInProgress() )
//		{
//			throw new JRestoreInProgress( "Active federation restore: label="+
//			                              restoreManager.getActiveLabel() );
//		}
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
	
	public int getFederationHandle()
	{
		return this.federationHandle;
	}
	
	public void setFederationHandle( int federationHandle )
	{
		this.federationHandle = federationHandle;
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
	 * provide support for this, when the mode is enabled the LRCQueue itself will have an
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
//			if( LRCProperties.LRC_QUEUE_WARNING_COUNT != -1 &&
//			messageQueue.getSize() > LRCProperties.LRC_QUEUE_WARNING_COUNT )
			if( messageQueue.getSize() > theLRC.configuration.getQueueSizeWarningLimit() )
			{
				theLRC.logger.warn( "WARNING Federate [%s] is not ticking enough; queue is fat! "+
				                    "(size=%d, warningLevel=%d)",
				                    federateName,
				                    messageQueue.getSize(),
				                    theLRC.configuration.getQueueSizeWarningLimit() );
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
	
//	public MomManager getMomManager()
//	{
//		return this.momManager;
//	}
//	
//	public Manifest getManifest()
//	{
//		return this.manifest;
//	}
//	
//	public Serializer getSerializer()
//	{
//		return this.serializer;
//	}
//	
//	public SaveManager getSaveManager()
//	{
//		return this.saveManager;	
//	}
//
//	public RestoreManager getRestoreManager()
//	{
//		return this.restoreManager;
//	}
//
	public TimeStatus getTimeStatus()
	{
		return this.timeStatus;
	}

	public InterestManager getInterestManager()
	{
		return this.interestManager;
	}
	
	public Repository getRepository()
	{
		return this.repository;
	}
	
//	public OwnershipManager getOwnershipManager()
//	{
//		return this.ownershipManager;
//	}
//	
	public RegionStore getRegionStore()
	{
		return this.regionStore;
	}

	public boolean isServiceReporting()
	{
		return this.serviceReporting;
	}
	
	public void setServiceReporting( boolean reporting )
	{
		this.serviceReporting = reporting;
	}
	
	public boolean isExceptionReporting()
	{
		return this.exceptionReporting;
	}
	
	public void setExceptionReporting( boolean reporting )
	{
		this.exceptionReporting = reporting;
	}
	
	public int getNextServiceInvocationSerial()
	{
		return this.serviceInvocationCounter++;
	}
	
	protected boolean isQueueLogging()
	{
		return this.isQueueLogging;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Save/Restore Methods /////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
//	public void saveToStream( ObjectOutput output ) throws Exception
//	{
//		output.writeObject( properties );
//		
//		// Basic settings //
//		output.writeInt( federateHandle );
//
//		// Object Model //
//		output.writeObject( fom );
//		
//		// Time related settings //
//		//output.writeObject( timeStatus );
//		output.writeBoolean( ticking );
//		output.writeBoolean( immediateCallbacks );
//		
//		// Instance Repository //
//		output.writeInt( latestObjectHandle );
//		output.writeInt( maxObjectHandle );
//		
//		// DDM state entities //
//		output.writeInt( latestRegionToken );
//		output.writeInt( maxRegionToken );
//	}

//	public void restoreFromStream( ObjectInput input ) throws Exception
//	{
//		this.properties = (Properties)input.readObject();
//		
//		// Basic settings //
//		this.federateHandle = input.readInt();
//
//		// Object Model //
//		this.fom = (ObjectModel)input.readObject();
//		
//		// Time related settings //
//		this.timeStatus = timeManager.getTimeStatus( federateHandle );
//		this.ticking = input.readBoolean();
//		this.immediateCallbacks = input.readBoolean();
//		
//		// Instance Repository //
//		this.latestObjectHandle = input.readInt();
//		this.maxObjectHandle = input.readInt();
//		
//		// DDM state entities //
//		this.latestRegionToken = input.readInt();
//		this.maxRegionToken = input.readInt();
//	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
