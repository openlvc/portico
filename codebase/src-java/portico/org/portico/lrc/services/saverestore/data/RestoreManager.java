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
package org.portico.lrc.services.saverestore.data;

import java.util.HashMap;
import java.util.Map;

import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JRestoreInProgress;
import org.portico.lrc.compat.JRestoreNotRequested;

/**
 * Manages the process of a federation restore, recording the particulars of the save request and 
 * lists who has/has not yet completed it. Note that this manager does *NOT* implement the actual
 * restoring the local LRC data, it only manages the flow of calls that dictate when federate move
 * in/out of the various restoration states.
 */
public class RestoreManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String activeLabel;
	private boolean pending;
	private int registeringFederate;
	private Map<Integer,SRStatus> restoreStatus; // key: federateHandle
	private boolean inProgress; // is the current federate in progress locally?

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public RestoreManager()
	{
		this.activeLabel = null;
		this.pending = false;
		this.registeringFederate = PorticoConstants.NULL_HANDLE;
		this.restoreStatus = new HashMap<Integer,SRStatus>();
		this.inProgress = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Federate Management Methods //////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	public void joinedFederation( int federateHandle )
	{
		this.restoreStatus.put( federateHandle, SRStatus.NONE );
	}

	public void resignedFederation( int federateHandle )
	{
		this.restoreStatus.remove( federateHandle );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Restore Management Methods ///////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method requests that a new restore be initiated. The label and requesting federate is
	 * stored and after this, the statuses for all known federates are set to
	 * {@link SRStatus#INITIATED}. If there is already an active restore, an exception
	 * is thrown.
	 * <p/>
	 * <b>NOTE:</b> This will set the <code>pending</code> status to true.
	 */
	public void requestRestore( int requestingFederate, String label ) throws JRestoreInProgress
	{
		if( this.activeLabel != null )
			throw new JRestoreInProgress( "RestoreInProgress: label="+activeLabel );

		// flick the switch on all the statuses
		updateStatusForAll( SRStatus.BEGUN );
		
		// store the save information
		this.activeLabel = label;
		this.registeringFederate = requestingFederate;
		this.pending = true;
	}

	/**
	 * Please note that this only applies to the local federate. Notifications that a restore
	 * has begun are not broadcast out into the federation, so we only know about the local
	 * federate.
	 */
	public void restoreBegun()
	{
		this.inProgress = true;
	}
	
	/**
	 * Tell the manager that the federate with the given handle has *successfully* completed
	 * the federation restore successfully. Once a notification has been received from all
	 * federates, the {@link #isRestoreComplete()} method will return <code>true</code>. At that
	 * point the {@link #isRestoreCompleteSuccessful()} will return <code>true</code> if all
	 * federates restored successfully. If any federates did not complete successfully, that
	 * method will return false, indicating that the restore was a failure.
	 */
	public void federateRestoreComplete( int federate ) throws JRestoreNotRequested
	{
		if( activeLabel == null )
			throw new JRestoreNotRequested( "federateRestoreComplete(): no active restore" );
		
		restoreStatus.put( federate, SRStatus.COMPLETE );
	}
	
	/**
	 * Tell the manager that the federate with the given handle has *unsuccessfully* completed
	 * the federation restore successfully. Once a notification has been received from all
	 * federates, the {@link #isRestoreComplete()} method will return <code>true</code>. At that
	 * point the {@link #isRestoreCompleteSuccessful()} will return <code>true</code> if all
	 * federates restored successfully. If any federates did not complete successfully, that
	 * method will return false, indicating that the restore was a failure.
	 */
	public void federateRestoreNotComplete( int federate ) throws JRestoreNotRequested
	{
		if( activeLabel == null )
			throw new JRestoreNotRequested( "federateRestoreNotComplete(): no active restore" );
		
		restoreStatus.put( federate, SRStatus.NOT_COMPLETE );
	}
	
	/**
	 * Returns <code>true</code> is a federation restore is currently in progress,
	 * <code>false</code> otherwise. Note that this state must be manually set right before a
	 * callback is delivered 
	 */
	public boolean isInProgress()
	{
		return this.inProgress;
	}
	
	/**
	 * Returns <code>true</code> if a restore request has been received but the final confirmation
	 * has not yet made it through.
	 */
	public boolean isRestorePending()
	{
		return this.pending;
	}

	public void setRestorePending( boolean pending )
	{
		this.pending = pending;
	}

	public boolean isRestoreComplete()
	{
		for( SRStatus status : restoreStatus.values() )
		{
			if( status != SRStatus.COMPLETE && status != SRStatus.NOT_COMPLETE )
				return false;
		}
		
		return true;
	}
	
	/**
	 * This should only be called after verifying that the entire restore is actually complete
	 * (that all federates have responded as such) through the {@link #isRestoreComplete()}
	 * method. If any have not finished, this method will return <code>false</code> which might
	 * signal that the restore failed, when really it hasn't finished yet. If all have completed
	 * and all have done so successfully, this method will return <code>true</code>.
	 */
	public boolean isRestoreCompleteSuccessful()
	{
		for( SRStatus status : restoreStatus.values() )
		{
			if( status != SRStatus.COMPLETE )
				return false;
		}
		
		return true;
	}

	public int getRegisteringFederate()
	{
		return this.registeringFederate;
	}
	
	public String getActiveLabel()
	{
		return this.activeLabel;
	}
	
	/**
	 * This method should be used after a restore has been compelted and the federate notified.
	 * It is used to wipe away all information about any existing save and prepare it for a new
	 * restore to be initiated.
	 */
	public void reset()
	{
		this.activeLabel = null;
		this.registeringFederate = PorticoConstants.NULL_HANDLE;
		this.pending = false;
		updateStatusForAll( SRStatus.NONE );
		this.inProgress = false;
	}
	
	private void updateStatusForAll( SRStatus status )
	{
		for( Integer federateHandle : restoreStatus.keySet() )
			restoreStatus.put( federateHandle, status );
	}
	
	
	
	
	public String printit()
	{
		return ""+restoreStatus;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
