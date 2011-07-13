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
import org.portico.lrc.compat.JSaveInProgress;
import org.portico.lrc.compat.JSaveNotInitiated;

/**
 * Manages the process of a federation save, recording the particulars of the save request and 
 * lists who has/has not yet completed it. Note that this manager does *NOT* implement the actual
 * saving the local LRC data, it only manages the flow of calls that dictate when federate move
 * in/out of the various saving states.
 */
public class SaveManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String activeLabel;
	private int registeringFederate;
	private Map<Integer,SRStatus> saveStatus; // key: federateHandle
	private boolean inProgress; // for the local federate

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public SaveManager()
	{
		this.activeLabel = null;
		this.registeringFederate = PorticoConstants.NULL_HANDLE;
		this.saveStatus = new HashMap<Integer,SRStatus>();
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
		this.saveStatus.put( federateHandle, SRStatus.NONE );
	}

	public void resignedFederation( int federateHandle )
	{
		this.saveStatus.remove( federateHandle );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Save Management Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method requests that a new save be initiated. The label and requesting federate is
	 * stored and after this, the statuses for all known federates are set to
	 * {@link SRStatus#INITIATED}. If there is already an active save, an exception is thrown.
	 */
	public void requestSave( int requestingFederate, String label ) throws JSaveInProgress
	{
		if( this.activeLabel != null )
			throw new JSaveInProgress( "SaveInProgress: label="+activeLabel );

		// flick the switch on all the statuses
		updateStatusForAll( SRStatus.INITIATED );
		
		// store the save information
		this.activeLabel = label;
		this.registeringFederate = requestingFederate;
		this.inProgress = true;
	}

	/**
	 * Tells the manager that the federate has begun the process of saving locally.
	 * Once all federates have completed the entire save process, the {@link #isSaveComplete()}
	 * method will return <code>true</code> and the {@link #isSaveCompleteSuccessful()} method
	 * will return true or false depending on whether or not all federates completed successfully.
	 */
	public void federateSaveBegun( int federate ) throws JSaveNotInitiated
	{
		if( activeLabel == null )
			throw new JSaveNotInitiated( "federateSaveBegun(): no active save" );
		
		saveStatus.put( federate, SRStatus.BEGUN );
	}

	/**
	 * Tells the manager that the federate has successfully completed the current active save.
	 * Once all federates have completed, the {@link #isSaveComplete()} method will return
	 * <code>true</code> and the {@link #isSaveCompleteSuccessful()} method will return true or
	 * false depending on whether or not all federates completed successfully.
	 */
	public void federateSaveComplete( int federate ) throws JSaveNotInitiated
	{
		if( activeLabel == null )
			throw new JSaveNotInitiated( "federateSaveComplete(): no active save" );
		
		saveStatus.put( federate, SRStatus.COMPLETE );
	}
	
	/**
	 * Tells the manager that the federate has unsuccessfully completed the current active save.
	 * Once all federates have completed, the {@link #isSaveComplete()} method will return
	 * <code>true</code> and the {@link #isSaveCompleteSuccessful()} method will return true or
	 * false depending on whether or not all federates completed successfully.
	 */
	public void federateSaveNotComplete( int federate ) throws JSaveNotInitiated
	{
		if( activeLabel == null )
			throw new JSaveNotInitiated( "federateSaveNotComplete(): no active save" );
		
		saveStatus.put( federate, SRStatus.NOT_COMPLETE );
	}

	/**
	 * Returns <code>true</code> is a federation save is currently in progress, <code>false</code>
	 * otherwise. This should only return true after the callback has been delivered notifying the
	 * federate that a save is in progress.
	 */
	public boolean isInProgress()
	{
		return inProgress;
	}

	/**
	 * Set the current "save in progress flag". This should be done as close to delivering the
	 * callback as possible. Otherwise federates will get "SaveInProgres" exceptions before they
	 * know a save is actually happening.
	 */
	public void setInProgress( boolean state )
	{
		this.inProgress = state;
	}

	public boolean isSaveComplete()
	{
		for( SRStatus status : saveStatus.values() )
		{
			if( status != SRStatus.COMPLETE && status != SRStatus.NOT_COMPLETE )
				return false;
		}
		
		return true;
	}
	
	/**
	 * This should only be called after verifying that the entire save is actually complete (that
	 * all federates have responded as such) through the {@link #isSaveComplete()} method. If any
	 * have not finished, this method will return <code>false</code> which might signal that the
	 * save failed, when really it hasn't finished yet. If all have completed and all have done so
	 * successfully, this method will return <code>true</code>.
	 */
	public boolean isSaveCompleteSuccessful()
	{
		for( SRStatus status : saveStatus.values() )
		{
			if( status != SRStatus.COMPLETE )
				return false;
		}
		
		return true;
	}
	
	public String getActiveLabel()
	{
		return this.activeLabel;
	}

	public int getRegisteringFederate()
	{
		return this.registeringFederate;
	}
	
	/**
	 * This method should be used after a save has been compelted and the federate notified. It
	 * is used to wipe away all information about any existing save and prepare it for a new save
	 * to be initiated.
	 */
	public void reset()
	{
		this.activeLabel = null;
		this.registeringFederate = PorticoConstants.NULL_HANDLE;
		updateStatusForAll( SRStatus.NONE );
		this.inProgress = false;
	}
	
	private void updateStatusForAll( SRStatus status )
	{
		for( Integer federateHandle : saveStatus.keySet() )
			saveStatus.put( federateHandle, status );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
