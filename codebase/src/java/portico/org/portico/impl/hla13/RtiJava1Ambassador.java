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
package org.portico.impl.hla13;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.portico.utils.messaging.ErrorResponse;
import org.portico.utils.messaging.ExtendedSuccessMessage;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.ResponseMessage;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.ResignFederation;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.common.services.object.msg.LocalDelete;
import org.portico2.common.services.object.msg.RegisterObject;
import org.portico2.common.services.object.msg.RequestClassUpdate;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.common.services.object.msg.SendInteraction;
import org.portico2.common.services.object.msg.UpdateAttributes;
import org.portico2.common.services.ownership.msg.AttributeAcquire;
import org.portico2.common.services.pubsub.msg.PublishInteractionClass;
import org.portico2.common.services.pubsub.msg.PublishObjectClass;
import org.portico2.common.services.pubsub.msg.SubscribeInteractionClass;
import org.portico2.common.services.pubsub.msg.SubscribeObjectClass;
import org.portico2.common.services.pubsub.msg.UnpublishInteractionClass;
import org.portico2.common.services.pubsub.msg.UnpublishObjectClass;
import org.portico2.common.services.pubsub.msg.UnsubscribeInteractionClass;
import org.portico2.common.services.pubsub.msg.UnsubscribeObjectClass;
import org.portico2.common.services.sync.msg.RegisterSyncPoint;
import org.portico2.common.services.sync.msg.SyncPointAchieved;
import org.portico2.common.services.time.msg.DisableAsynchronousDelivery;
import org.portico2.common.services.time.msg.DisableTimeConstrained;
import org.portico2.common.services.time.msg.DisableTimeRegulation;
import org.portico2.common.services.time.msg.EnableAsynchronousDelivery;
import org.portico2.common.services.time.msg.EnableTimeConstrained;
import org.portico2.common.services.time.msg.EnableTimeRegulation;
import org.portico2.common.services.time.msg.FlushQueueRequest;
import org.portico2.common.services.time.msg.ModifyLookahead;
import org.portico2.common.services.time.msg.NextEventRequest;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.apache.logging.log4j.Logger;
import org.portico.impl.hla13.types.HLA13ByteArrayMap;
import org.portico.impl.hla13.types.Java1Region;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JAsynchronousDeliveryAlreadyDisabled;
import org.portico.lrc.compat.JAsynchronousDeliveryAlreadyEnabled;
import org.portico.lrc.compat.JAttributeAcquisitionWasNotRequested;
import org.portico.lrc.compat.JAttributeAlreadyBeingAcquired;
import org.portico.lrc.compat.JAttributeAlreadyBeingDivested;
import org.portico.lrc.compat.JAttributeAlreadyOwned;
import org.portico.lrc.compat.JAttributeDivestitureWasNotRequested;
import org.portico.lrc.compat.JAttributeNotDefined;
import org.portico.lrc.compat.JAttributeNotOwned;
import org.portico.lrc.compat.JAttributeNotPublished;
import org.portico.lrc.compat.JConcurrentAccessAttempted;
import org.portico.lrc.compat.JCouldNotOpenFED;
import org.portico.lrc.compat.JDeletePrivilegeNotHeld;
import org.portico.lrc.compat.JEnableTimeConstrainedPending;
import org.portico.lrc.compat.JEnableTimeRegulationPending;
import org.portico.lrc.compat.JErrorReadingFED;
import org.portico.lrc.compat.JFederateAlreadyExecutionMember;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JFederateOwnsAttributes;
import org.portico.lrc.compat.JFederateWasNotAskedToReleaseAttribute;
import org.portico.lrc.compat.JFederatesCurrentlyJoined;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JFederationTimeAlreadyPassed;
import org.portico.lrc.compat.JInteractionClassNotDefined;
import org.portico.lrc.compat.JInteractionClassNotPublished;
import org.portico.lrc.compat.JInteractionClassNotSubscribed;
import org.portico.lrc.compat.JInteractionParameterNotDefined;
import org.portico.lrc.compat.JInvalidExtents;
import org.portico.lrc.compat.JInvalidFederationTime;
import org.portico.lrc.compat.JInvalidLookahead;
import org.portico.lrc.compat.JInvalidRegionContext;
import org.portico.lrc.compat.JInvalidResignAction;
import org.portico.lrc.compat.JObjectAlreadyRegistered;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JObjectClassNotSubscribed;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.compat.JOwnershipAcquisitionPending;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JRegionInUse;
import org.portico.lrc.compat.JRegionNotKnown;
import org.portico.lrc.compat.JResignAction;
import org.portico.lrc.compat.JRestoreInProgress;
import org.portico.lrc.compat.JRestoreNotRequested;
import org.portico.lrc.compat.JSaveInProgress;
import org.portico.lrc.compat.JSaveNotInitiated;
import org.portico.lrc.compat.JSpaceNotDefined;
import org.portico.lrc.compat.JSynchronizationLabelNotAnnounced;
import org.portico.lrc.compat.JTimeAdvanceAlreadyInProgress;
import org.portico.lrc.compat.JTimeConstrainedAlreadyEnabled;
import org.portico.lrc.compat.JTimeConstrainedWasNotEnabled;
import org.portico.lrc.compat.JTimeRegulationAlreadyEnabled;
import org.portico.lrc.compat.JTimeRegulationWasNotEnabled;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.Dimension;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.Mom;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.RegionInstance;
import org.portico.lrc.model.Space;
import org.portico.lrc.services.ddm.msg.AssociateRegion;
import org.portico.lrc.services.ddm.msg.CreateRegion;
import org.portico.lrc.services.ddm.msg.DeleteRegion;
import org.portico.lrc.services.ddm.msg.ModifyRegion;
import org.portico.lrc.services.ddm.msg.UnassociateRegion;
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.lrc.services.ownership.msg.AttributeRelease;
import org.portico.lrc.services.ownership.msg.CancelAcquire;
import org.portico.lrc.services.ownership.msg.CancelDivest;
import org.portico.lrc.services.ownership.msg.QueryOwnership;
import org.portico.lrc.services.saverestore.msg.RestoreComplete;
import org.portico.lrc.services.saverestore.msg.RestoreRequest;
import org.portico.lrc.services.saverestore.msg.SaveBegun;
import org.portico.lrc.services.saverestore.msg.SaveComplete;
import org.portico.lrc.services.saverestore.msg.SaveRequest;
import org.portico.utils.messaging.PorticoMessage;

import hla.rti13.java1.*;

/**
 * This class is the Portico implementation of the HLA 1.3 Annex A RTIambassador class.
 */
public class RtiJava1Ambassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ImplJava1Helper helper;
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public RtiJava1Ambassador() throws RTIinternalError
	{
		this.helper = new ImplJava1Helper();
		this.logger = this.helper.getLrcLogger();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------	
	
	public ImplJava1Helper getHelper()
	{
		return this.helper;
	}

	//////////////////////////////////////////////////////////////////////////////////////////// 
	////////////////////////////// Federation Management Services ////////////////////////////// 
	//////////////////////////////////////////////////////////////////////////////////////////// 
	public void createFederationExecution( String executionName, String FED )
	    throws FederationExecutionAlreadyExists,
	           CouldNotOpenFED,
	           ErrorReadingFED,
	           ConcurrentAccessAttempted,
	           RTIinternalError
	{
		// validate that the given fed location is actually a file on the local file system
		// in order to maintain compatibility with NG, then turn it into a URL and we're off
		File fedfile = new File( FED );
		if( fedfile.canRead() == false )
		{
			throw new CouldNotOpenFED( "The file [" + FED + "] does not exist or can't be read" );
		}
		
		URL fedURL = null;
		try
		{
			fedURL = fedfile.toURI().toURL();
		}
		catch( Exception e )
		{
			throw new RTIinternalError( "Problem turning ["+FED+"] into URL: "+e.getMessage(), e );
		}
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		CreateFederation request = new CreateFederation( executionName, fedURL );	
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();
			
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError(theException);
			}
			else if( theException instanceof JFederationExecutionAlreadyExists )
			{
				throw new FederationExecutionAlreadyExists( theException );
			}
			else if( theException instanceof JCouldNotOpenFED )
			{
				throw new CouldNotOpenFED( theException );
			}
			else if( theException instanceof JErrorReadingFED )
			{
				throw new ErrorReadingFED( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "createFederationExecution", theException );
				throw new RTIinternalError( theException );
			}
		}
	}

	public void destroyFederationExecution( String executionName )
		throws FederatesCurrentlyJoined,
		       FederationExecutionDoesNotExist,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DestroyFederation request = new DestroyFederation( executionName );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();
			
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederatesCurrentlyJoined )
			{
				throw new FederatesCurrentlyJoined( theException );
			}
			else if( theException instanceof JFederationExecutionDoesNotExist )
			{
				throw new FederationExecutionDoesNotExist( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "destroyFederationExecution", theException );
			}
		}
	}

	public int joinFederationExecution( String federateName,
	                                    String federationName,
	                                    FederateAmbassador federateAmbassador )
	    throws FederateAlreadyExecutionMember,
	           FederationExecutionDoesNotExist,
	           CouldNotOpenFED,
	           ErrorReadingFED,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		// 0. check the federate ambassador //
		// Has to be done here as part of fix for PORT-132 //
		if( federateAmbassador == null )
			throw new RTIinternalError( "FederateAmbassador was null" );
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// set the federate ambassador now, weill need it for callbacks as a result of the join,
		// these will occur when we get RoleCall from other federates (processed before the join
		// handler returns, so setting it after processMessage() won't help)
		this.helper.setFederateAmbassador( federateAmbassador );
		
		JoinFederation request = new JoinFederation( federationName, federateName );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			ExtendedSuccessMessage success = (ExtendedSuccessMessage)response;

			// return the "handle"
			return (Integer)success.getResult();
		}
		else
		{
			// reset the fedamb
			this.helper.setFederateAmbassador( null );

			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateAlreadyExecutionMember )
			{
				throw new FederateAlreadyExecutionMember( theException );
			}
			else if( theException instanceof JFederationExecutionDoesNotExist )
			{
				throw new FederationExecutionDoesNotExist( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "joinFederationExecution", theException );
				return -1;
			}
		}
	}

	public void resignFederationExecution( int theAction )
		throws FederateOwnsAttributes,
		       FederateNotExecutionMember,
		       InvalidResignAction,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		ResignFederation request;
		try
		{
			request = new ResignFederation( JResignAction.for13Value(theAction) );
		}
		catch( JInvalidResignAction e )
		{
			throw new InvalidResignAction( e.getMessage() );
		}
		
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateOwnsAttributes )
			{
				throw new FederateOwnsAttributes( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else if( theException instanceof JInvalidResignAction )
			{
				// even though the message creation will check for it, this could still
				// occur, perhaps in a situation where the action didn't decode properly
				// on the RTI side or the like. Think outside the box people.
				throw new InvalidResignAction( theException );
			}
			else
			{
				logException( "resignFederationExecution", theException );
			}
		}
	}	
	
	public void registerFederationSynchronizationPoint( String label, String tag )
	    throws FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterSyncPoint request = null;
		if( tag == null )
			request = new RegisterSyncPoint( label, new byte[0] );
		else
			request = new RegisterSyncPoint( label, tag.getBytes() );

		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "registerFederationSynchronizationPoint", theException );
			}
		}
	}

	public void registerFederationSynchronizationPoint( String label,
	                                                    String tag,
	                                                    FederateHandleSet syncset )
	    throws FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterSyncPoint request = null;
		if( tag == null )
			request = new RegisterSyncPoint( label, new byte[0], convertSet(syncset) );
		else
			request = new RegisterSyncPoint( label, tag.getBytes(), convertSet(syncset) );

		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "registerFederationSynchronizationPoint", theException );
			}
		}
	}
	
	public void synchronizationPointAchieved( String label )
	    throws SynchronizationPointLabelWasNotAnnounced,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SyncPointAchieved request = new SyncPointAchieved( label );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSynchronizationLabelNotAnnounced )
			{
				throw new SynchronizationPointLabelWasNotAnnounced( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "synchronizationPointAchieved", theException );
			}
		}
	}
	
	public void requestFederationSave( String label )
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SaveRequest request = new SaveRequest( label );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "requestFederationSave", theException );
			}
		}
	}

	public void requestFederationSave( String label, byte[] theTime )
	    throws FederationTimeAlreadyPassed,
	           InvalidFederationTime,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}

		SaveRequest request = new SaveRequest( label, time );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new FederationTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "requestFederationSave(time)", theException );
			}
		}
	}

	public void federateSaveBegun()
		throws SaveNotInitiated,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SaveBegun request = new SaveBegun();
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveNotInitiated )
			{
				throw new SaveNotInitiated( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "federateSaveBegun", theException );
			}
		}
	}

	public void federateSaveComplete()
		throws SaveNotInitiated,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SaveComplete request = new SaveComplete( true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveNotInitiated )
			{
				throw new SaveNotInitiated( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "federateSaveComplete", theException );
			}
		}
	}

	public void federateSaveNotComplete()
		throws SaveNotInitiated,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SaveComplete request = new SaveComplete( false );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveNotInitiated )
			{
				throw new SaveNotInitiated( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "federateSaveNotComplete", theException );
			}
		}
	}

	public void requestFederationRestore( String label )
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RestoreRequest request = new RestoreRequest( label );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "requestFederationRestore", theException );
			}
		}
	}

	public void federateRestoreComplete()
		throws RestoreNotRequested,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RestoreComplete request = new RestoreComplete( true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JRestoreNotRequested )
			{
				throw new RestoreNotRequested( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "federateRestoreComplete", theException );
			}
		}
	}

	public void federateRestoreNotComplete()
		throws RestoreNotRequested,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RestoreComplete request = new RestoreComplete( false );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JRestoreNotRequested )
			{
				throw new RestoreNotRequested( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "federateRestoreNotComplete", theException );
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////// 
	////////////////////////// Declaration Management Services // 
	//////////////////////////////////////////////////////////////////////////////////////////// 
	public void publishObjectClass( int theClass, AttributeHandleSet attributes )
	    throws ObjectClassNotDefined,
	           AttributeNotDefined,
	           OwnershipAcquisitionPending,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		PublishObjectClass request = new PublishObjectClass( theClass, convertSet(attributes) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "publishObjectClass", theException );
			}
		}
	}

	public void unpublishObjectClass( int theClass )
		throws ObjectClassNotDefined,
		       ObjectClassNotPublished,
		       OwnershipAcquisitionPending,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnpublishObjectClass request = new UnpublishObjectClass( theClass );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JObjectClassNotPublished )
			{
				throw new ObjectClassNotPublished( theException );
			}
			else if( theException instanceof JOwnershipAcquisitionPending )
			{
				throw new OwnershipAcquisitionPending( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "unpublishObjectClass", theException );
			}
		}
	}

	public void publishInteractionClass( int theInteraction )
		throws InteractionClassNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		PublishInteractionClass request = new PublishInteractionClass( theInteraction );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "publishInteractionClass", theException );
			}
		}
	}

	public void unpublishInteractionClass( int theInteraction )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnpublishInteractionClass request = new UnpublishInteractionClass( theInteraction );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JInteractionClassNotPublished )
			{
				throw new InteractionClassNotPublished( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "unpublishInteractionClass", theException );
			}
		}
	}

	public void subscribeObjectClassAttributes( int theClass, AttributeHandleSet attributes )
	    throws ObjectClassNotDefined,
	           AttributeNotDefined,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SubscribeObjectClass request =
			new SubscribeObjectClass( theClass, convertSet(attributes), true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "subscribeObjectClassAttributes", theException );
			}
		}
	}

	public void subscribeObjectClassAttributesPassively( int theClass,
	                                                     AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined,
	           AttributeNotDefined,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesPassively()" );
	}

	public void unsubscribeObjectClass( int theClass )
		throws ObjectClassNotDefined,
		       ObjectClassNotSubscribed,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnsubscribeObjectClass request = new UnsubscribeObjectClass( theClass );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JObjectClassNotSubscribed )
			{
				throw new ObjectClassNotSubscribed( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "unsubscirbeObjectClass", theException );
			}
		}
	}

	public void subscribeInteractionClass( int theClass )
		throws InteractionClassNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       FederateLoggingServiceCalls,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SubscribeInteractionClass request = new SubscribeInteractionClass( theClass );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "subscribeInteractionClass", theException );
			}
		}
	}

	public void subscribeInteractionClassPassively( int theClass )
	    throws InteractionClassNotDefined,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           FederateLoggingServiceCalls,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		featureNotSupported( "subscribeInteractionClassPassively()" );
	}

	public void unsubscribeInteractionClass( int theClass )
		throws InteractionClassNotDefined,
		       InteractionClassNotSubscribed,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnsubscribeInteractionClass request = new UnsubscribeInteractionClass( theClass );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JInteractionClassNotSubscribed )
			{
				throw new InteractionClassNotSubscribed( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "unsubscribeInteractionClass", theException );
			}
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////// 
	//////////////////////////////// Object Management Services //////////////////////////////// 
	//////////////////////////////////////////////////////////////////////////////////////////// 
	public int registerObjectInstance( int theClass )
		throws ObjectClassNotDefined,
		       ObjectClassNotPublished,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterObject request = new RegisterObject( theClass );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return ((OCInstance)response.getResult()).getHandle();
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JObjectClassNotPublished )
			{
				throw new ObjectClassNotPublished( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "registerObjectInstance", theException );
				return -1;
			}
		}
	}

	public int registerObjectInstance( int theClass, String theObject )
	    throws ObjectClassNotDefined,
	           ObjectClassNotPublished,
	           ObjectAlreadyRegistered,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterObject request = new RegisterObject( theClass, theObject );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return ((OCInstance)response.getResult()).getHandle();
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JObjectClassNotPublished )
			{
				throw new ObjectClassNotPublished( theException );
			}
			else if( theException instanceof JObjectAlreadyRegistered )
			{
				throw new ObjectAlreadyRegistered( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "registerObjectInstance", theException );
				return -1;
			}
		}
	}

	public EventRetractionHandle updateAttributeValues( int theObject,
	                                                    SuppliedAttributes theAttributes,
	                                                    byte[] theTime,
	                                                    String tag )
	    throws ObjectNotKnown,
	           AttributeNotDefined,
	           AttributeNotOwned,
	           InvalidFederationTime,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		HLA13ByteArrayMap map = theAttributes.toPorticoMap();
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}
	
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UpdateAttributes request = new UpdateAttributes( theObject,
		                                                 tag.getBytes(),
		                                                 map.toJavaMap(),
		                                                 time );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return null;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotOwned )
			{
				throw new AttributeNotOwned( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "updateAttributeValues(LogicalTime)", theException );
				throw new RTIinternalError( theException.getMessage(), theException );
			}
		}
	}

	public void updateAttributeValues( int theObject,
	                                   SuppliedAttributes theAttributes,
	                                   String tag )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = theAttributes.toPorticoMap();
		UpdateAttributes request = new UpdateAttributes( theObject,
		                                                 tag.getBytes(),
		                                                 map.toJavaMap() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotOwned )
			{
				throw new AttributeNotOwned( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "updateAttributeValues", theException );
			}
		}
	}

	public EventRetractionHandle sendInteraction( int theInteraction,
	                                              SuppliedParameters theParameters,
	                                              byte[] theTime,
	                                              String tag )
	    throws InteractionClassNotDefined,
	           InteractionClassNotPublished,
	           InteractionParameterNotDefined,
	           InvalidFederationTime,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		HLA13ByteArrayMap map = theParameters.toPorticoMap();
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SendInteraction request = new SendInteraction( theInteraction,
		                                               tag.getBytes(),
		                                               map.toJavaMap(),
		                                               time );
		ResponseMessage response = processMessage( request );
		
		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return null;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JInteractionClassNotPublished )
			{
				throw new InteractionClassNotPublished( theException );
			}
			else if( theException instanceof JInteractionParameterNotDefined )
			{
				throw new InteractionParameterNotDefined( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "sentInteraction(LogicalTime)", theException );
				throw new RTIinternalError( theException.getMessage(), theException );
			}
		}
	}

	public void sendInteraction( int iHandle, SuppliedParameters theParameters, String tag )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       InteractionParameterNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = theParameters.toPorticoMap();
		
		SendInteraction request =
			new SendInteraction( iHandle, tag.getBytes(), map.toJavaMap() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JInteractionClassNotPublished )
			{
				throw new InteractionClassNotPublished( theException );
			}
			else if( theException instanceof JInteractionParameterNotDefined )
			{
				throw new InteractionParameterNotDefined( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "sendInteraction", theException );
			}
		}
	}

	public EventRetractionHandle deleteObjectInstance( int theObject, byte[] theTime, String tag )
	    throws ObjectNotKnown,
	           DeletePrivilegeNotHeld,
	           InvalidFederationTime,
	           FederateNotExecutionMember,
	           ConcurrentAccessAttempted,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}

		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DeleteObject request = new DeleteObject(theObject, tag.getBytes(), time);
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return null;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JDeletePrivilegeNotHeld )
			{
				throw new DeletePrivilegeNotHeld( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "deleteObjectInstance", theException );
				return null;
			}
		}
	}

	public void deleteObjectInstance( int theObject, String tag )
		throws ObjectNotKnown,
		       DeletePrivilegeNotHeld,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DeleteObject request = new DeleteObject( theObject, tag.getBytes() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JDeletePrivilegeNotHeld )
			{
				throw new DeletePrivilegeNotHeld( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "deleteObjectInstance", theException );
			}
		}
	}

	public void localDeleteObjectInstance( int theObject )
		throws ObjectNotKnown,
		       FederateOwnsAttributes,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		LocalDelete request = new LocalDelete( theObject );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JFederateOwnsAttributes )
			{
				throw new FederateOwnsAttributes( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "localDeleteObjectInstance", theException );
			}
		}
	}

	public void changeAttributeTransportationType( int theObject,
	                                               AttributeHandleSet theAttributes,
	                                               int theType )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       InvalidTransportationHandle,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "changeAttributeTransportationType()" );
	}

	public void changeInteractionTransportationType( int theClass, int theType )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       InvalidTransportationHandle,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "changeInteractionTransportationType()" );
	}

	public void requestObjectAttributeValueUpdate( int theObject, AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RequestObjectUpdate request = new RequestObjectUpdate( theObject, convertSet(attributes) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "requestObjectAttributeValueUpdate", theException );
			}
		}
	}

	public void requestClassAttributeValueUpdate( int theClass, AttributeHandleSet attributes )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RequestClassUpdate request = new RequestClassUpdate(theClass, convertSet(attributes), null);
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "requestClassAttributeValueUpdate", theException );
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////// 
	/////////////////////////////// Ownership Management Services ////////////////////////////// 
	//////////////////////////////////////////////////////////////////////////////////////////// 

	public void unconditionalAttributeOwnershipDivestiture( int theObject,
	                                                        AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		AttributeDivest request = new AttributeDivest( theObject, convertSet(attributes), true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotOwned )
			{
				throw new AttributeNotOwned( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "unconditionalAttributeOwnershipDivestiture", theException );
			}
		}
	}
	
	public void negotiatedAttributeOwnershipDivestiture( int theObject,
	                                                     AttributeHandleSet attributes,
	                                                     String tag )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       AttributeAlreadyBeingDivested,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		AttributeDivest request = new AttributeDivest( theObject,
		                                               convertSet(attributes),
		                                               tag.getBytes() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotOwned )
			{
				throw new AttributeNotOwned( theException );
			}
			else if( theException instanceof JAttributeAlreadyBeingDivested )
			{
				throw new AttributeAlreadyBeingDivested( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "negotiatedAttributeOwnershipDivestiture", theException );
			}
		}
	}
	
	public void attributeOwnershipAcquisition( int theObject,
	                                           AttributeHandleSet attributes,
	                                           String tag )
		throws ObjectNotKnown,
		       ObjectClassNotPublished,
		       AttributeNotDefined,
		       AttributeNotPublished,
		       FederateOwnsAttributes,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		AttributeAcquire request = new AttributeAcquire( theObject,
		                                                 convertSet(attributes),
		                                                 tag.getBytes() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JObjectClassNotPublished )
			{
				throw new ObjectClassNotPublished( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotPublished )
			{
				throw new AttributeNotPublished( theException );
			}
			else if( theException instanceof JFederateOwnsAttributes )
			{
				throw new FederateOwnsAttributes( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "attributeOwnershipAcquisition", theException );
			}
		}
	}

	public void attributeOwnershipAcquisitionIfAvailable( int theObject,
	                                                      AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       ObjectClassNotPublished,
		       AttributeNotDefined,
		       AttributeNotPublished,
		       FederateOwnsAttributes,
		       AttributeAlreadyBeingAcquired,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		AttributeAcquire request = new AttributeAcquire( theObject, convertSet(attributes), true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JObjectClassNotPublished )
			{
				throw new ObjectClassNotPublished( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotPublished )
			{
				throw new AttributeNotPublished( theException );
			}
			else if( theException instanceof JFederateOwnsAttributes )
			{
				throw new FederateOwnsAttributes( theException );
			}
			else if( theException instanceof JAttributeAlreadyBeingAcquired )
			{
				throw new AttributeAlreadyBeingAcquired( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "attributeOwnershipAcquisitionIfAvailable", theException );
			}
		}
	}

	@SuppressWarnings("unchecked")
	public AttributeHandleSet attributeOwnershipReleaseResponse( int theObject,
	                                                             AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       FederateWasNotAskedToReleaseAttribute,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		AttributeRelease request =
			new AttributeRelease( theObject, convertSet(attributes) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return new AttributeHandleSet( (Set<Integer>)response.getResult() );
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotOwned )
			{
				throw new AttributeNotOwned( theException );
			}
			else if( theException instanceof JFederateWasNotAskedToReleaseAttribute )
			{
				throw new FederateWasNotAskedToReleaseAttribute( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "attributeOwnershipReleaseResponse", theException );
				return null; // above throws exception, this just keeps compiler happy
			}
		}
	}
	
	public void cancelNegotiatedAttributeOwnershipDivestiture( int theObject,
	                                                           AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       AttributeDivestitureWasNotRequested,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		CancelDivest request = new CancelDivest( theObject, convertSet(attributes) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotOwned )
			{
				throw new AttributeNotOwned( theException );
			}
			else if( theException instanceof JAttributeDivestitureWasNotRequested )
			{
				throw new AttributeDivestitureWasNotRequested( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "cancelNegotiatedAttributeOwnershipDivestiture", theException );
			}
		}
	}
	
	public void cancelAttributeOwnershipAcquisition( int theObject,
	                                                 AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeAlreadyOwned,
		       AttributeAcquisitionWasNotRequested,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		CancelAcquire request = new CancelAcquire( theObject, convertSet(attributes) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeAlreadyOwned )
			{
				throw new AttributeAlreadyOwned( theException );
			}
			else if( theException instanceof JAttributeAcquisitionWasNotRequested )
			{
				throw new AttributeAcquisitionWasNotRequested( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "cancelAttributeOwnershipAcquisition", theException );
			}
		}
	}
	
	public void queryAttributeOwnership( int theObject, int theAttribute )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		QueryOwnership request = new QueryOwnership( theObject, theAttribute );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "queryAttributeOwnership", theException );
			}
		}
	}
	
	public boolean isAttributeOwnedByFederate( int theObject, int theAttribute )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		helper.checkJoined();
		
		OCInstance instance = helper.getState().getRepository().getInstance( theObject );
		if( instance == null )
		{
			throw new ObjectNotKnown( "handle: " + theObject );
		}
		else
		{
			ACInstance attribute = instance.getAttribute( theAttribute );
			if( attribute == null )
				throw new AttributeNotDefined( "handle: " + theAttribute );
			else
				return attribute.getOwner() == helper.getState().getFederateHandle();
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////// 
	///////////////////////////////// Time Management Services ///////////////////////////////// 
	//////////////////////////////////////////////////////////////////////////////////////////// 
	public void enableTimeRegulation( byte[] theFederateTime, byte[] theLookahead )
		throws TimeRegulationAlreadyEnabled,
		       EnableTimeRegulationPending,
		       TimeAdvanceAlreadyInProgress,
		       InvalidFederationTime,
		       InvalidLookahead,
		       ConcurrentAccessAttempted,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double doubleLA = 0.0;
		try
		{
			doubleLA = EncodingHelpers.decodeDouble( theLookahead );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}
		
		// check that it is valid
		if( doubleLA < 0.0 )
		{
			throw new InvalidLookahead( "Negative Lookahead is invalid" );
		}
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		EnableTimeRegulation request = new EnableTimeRegulation( 0.0, doubleLA );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();
			
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JTimeRegulationAlreadyEnabled )
			{
				throw new TimeRegulationAlreadyEnabled( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new EnableTimeRegulationPending( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new TimeAdvanceAlreadyInProgress( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JInvalidLookahead )
			{
				throw new InvalidLookahead( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "enableTimeRegulation", theException );
			}
		}
	}

	public void disableTimeRegulation()
		throws TimeRegulationWasNotEnabled,
		       ConcurrentAccessAttempted,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DisableTimeRegulation request = new DisableTimeRegulation();
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();
			
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JTimeRegulationWasNotEnabled )
			{
				throw new TimeRegulationWasNotEnabled( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "disableTimeRegulation", theException );
			}
		}
	}


	public void enableTimeConstrained()
		throws TimeConstrainedAlreadyEnabled,
		       EnableTimeConstrainedPending,
		       TimeAdvanceAlreadyInProgress,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		EnableTimeConstrained request = new EnableTimeConstrained();
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();
			
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JTimeConstrainedAlreadyEnabled )
			{
				throw new TimeConstrainedAlreadyEnabled( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new EnableTimeConstrainedPending( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new TimeAdvanceAlreadyInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "enbaleTimeConstrained", theException );
			}
		}
	}

	public void disableTimeConstrained()
		throws TimeConstrainedWasNotEnabled,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DisableTimeConstrained request = new DisableTimeConstrained();
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();
			
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JTimeConstrainedWasNotEnabled )
			{
				throw new TimeConstrainedWasNotEnabled( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "disableTimeConstrained", theException );
			}
		}
	}
	
	public void timeAdvanceRequest( byte[] theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		TimeAdvanceRequest request = new TimeAdvanceRequest( time );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new FederationTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new TimeAdvanceAlreadyInProgress( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new EnableTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new EnableTimeConstrainedPending( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "timeAdvanceRequest", theException );
			}
		}
	}

	public void timeAdvanceRequestAvailable( byte[] theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		TimeAdvanceRequest request = new TimeAdvanceRequest( time, true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new FederationTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new TimeAdvanceAlreadyInProgress( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new EnableTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new EnableTimeConstrainedPending( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "timeAdvanceRequestAvailable", theException );
			}
		}
	}

	public void nextEventRequest( byte[] theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		NextEventRequest request = new NextEventRequest( time );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new FederationTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new TimeAdvanceAlreadyInProgress( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new EnableTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new EnableTimeConstrainedPending( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "nextEventRequest", theException );
			}
		}
	}

	public void nextEventRequestAvailable( byte[] theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		NextEventRequest request = new NextEventRequest( time, true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new FederationTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new TimeAdvanceAlreadyInProgress( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new EnableTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new EnableTimeConstrainedPending( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "nextEventRequestAvailable", theException );
			}
		}
	}

	public void flushQueueRequest( byte[] theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		FlushQueueRequest request = new FlushQueueRequest( time );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new FederationTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new TimeAdvanceAlreadyInProgress( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new EnableTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new EnableTimeConstrainedPending( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "flushQueueRequest", theException );
			}
		}
	}

	public void enableAsynchronousDelivery()
		throws AsynchronousDeliveryAlreadyEnabled,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		EnableAsynchronousDelivery request = new EnableAsynchronousDelivery();
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JAsynchronousDeliveryAlreadyEnabled )
			{
				throw new AsynchronousDeliveryAlreadyEnabled( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "enableAsynchronousDelivery", theException );
			}
		}
	}

	public void disableAsynchronousDelivery()
		throws AsynchronousDeliveryAlreadyDisabled,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DisableAsynchronousDelivery request = new DisableAsynchronousDelivery();
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JAsynchronousDeliveryAlreadyDisabled )
			{
				throw new AsynchronousDeliveryAlreadyDisabled( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "disableAsynchronousDelivery", theException );
			}
		}
	}

	public byte[] queryLBTS()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		return EncodingHelpers.encodeDouble( helper.getState().getFederateLbts() );
	}

	public byte[] queryFederateTime()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		return EncodingHelpers.encodeDouble( helper.getState().getCurrentTime() );
	}

	public byte[] queryMinNextEventTime()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		// return the next min event time = current requested time + lookahead //
		return EncodingHelpers.encodeDouble( helper.getState().getRequestedTime() + 
		                                     helper.getState().getLookahead() );
	}
	
	public void modifyLookahead( byte[] theLookahead )
		throws InvalidLookahead,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theLookahead );
		}
		catch( Exception e )
		{
			throw new InvalidLookahead( "Exception decoding time to double: "+e.getMessage() );
		}

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		ModifyLookahead request = new ModifyLookahead( time );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInvalidLookahead )
			{
				throw new InvalidLookahead( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "modifyLookahead", theException );
			}
		}
	}

	public byte[] queryLookahead()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		return EncodingHelpers.encodeDouble( helper.getState().getLookahead() ); 
	}

	public void retract( EventRetractionHandle theHandle )
		throws InvalidRetractionHandle,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "retract()" );
	}
	
	public void changeAttributeOrderType( int theObject,
	                                      AttributeHandleSet theAttributes,
	                                      int theType )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       InvalidOrderingHandle,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "changeAttributeOrderType()" );
	}


	public void changeInteractionOrderType( int theClass, int theType )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       InvalidOrderingHandle,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "changeInteractionOrderType()" );
	}

	//////////////////////////////////////////////////////////////////////////////////////////// 
	/////////////////////////////// Data Distribution Management /////////////////////////////// 
	//////////////////////////////////////////////////////////////////////////////////////////// 
	public Region createRegion( int theSpace, int numberOfExtents )
		throws SpaceNotDefined,
		       InvalidExtents,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		// create and process the message
		CreateRegion request = new CreateRegion( theSpace, numberOfExtents );
		ResponseMessage response = processMessage( request );

		// check the response
		if( response.isError() == false )
		{
			// we have a successful response, extract the region and return it
			return new Java1Region( (RegionInstance)response.getResult() );
		}

		// an exception was caused by the request, figure out what it is and throw it
		Throwable theException = ((ErrorResponse)response).getCause();
		if( theException instanceof JRTIinternalError )
		{
			throw new RTIinternalError( theException );
		}
		else if( theException instanceof JSpaceNotDefined )
		{
			throw new SpaceNotDefined( theException );
		}
		else if( theException instanceof JInvalidExtents )
		{
			throw new InvalidExtents( theException );
		}
		else if( theException instanceof JFederateNotExecutionMember )
		{
			throw new FederateNotExecutionMember( theException );
		}
		else if( theException instanceof JSaveInProgress )
		{
			throw new SaveInProgress( theException );
		}
		else if( theException instanceof JRestoreInProgress )
		{
			throw new RestoreInProgress( theException );
		}
		else if( theException instanceof JConcurrentAccessAttempted )
		{
			throw new ConcurrentAccessAttempted( theException );
		}
		else
		{
			logException( "createRegion", theException );
			return null; // will never get here, previous method will throw an exception always
		}
	}

	public void notifyAboutRegionModification( Region theRegion )
		throws RegionNotKnown,
		       InvalidExtents,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		// construct the message and process it
		ModifyRegion request = new ModifyRegion( Java1Region.toPorticoRegion(theRegion) );
		ResponseMessage response = processMessage( request );
		
		// if we were successful, just return
		if( response.isError() )
		{
			// an exception was caused by the request, figure out what it is and throw it
			Throwable theException = ((ErrorResponse)response).getCause();
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JInvalidExtents )
			{
				throw new InvalidExtents( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "notifyOfRegionModification", theException );
			}
		}
	}

	public void deleteRegion( Region theRegion )
		throws RegionNotKnown,
		       RegionInUse,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		// construct the message, we'll need the region token for this
		int regionToken = Java1Region.toPorticoRegion(theRegion).getToken();
		ResponseMessage response = processMessage( new DeleteRegion(regionToken) );
		
		// if we were successful, just return, otherwise, process the error
		if( response.isError() )
		{
			Throwable theException = ((ErrorResponse)response).getCause();
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JRegionInUse )
			{
				throw new RegionInUse( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "deleteRegion", theException );
			}
		}
	}

	public int registerObjectInstanceWithRegion( int theClass,
	                                             int[] theAttributes,
	                                             Region[] theRegions )
		throws ObjectClassNotDefined,
		       ObjectClassNotPublished,
		       AttributeNotDefined,
		       AttributeNotPublished,
		       RegionNotKnown,
		       InvalidRegionContext,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		if( theAttributes == null )
			throw new RTIinternalError( "null passed for attribute array" );

		RegisterObject request =
			new RegisterObject( theClass, theAttributes, convertRegions(theRegions) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return ((OCInstance)response.getResult()).getHandle();
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JObjectClassNotPublished )
			{
				throw new ObjectClassNotPublished( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotPublished )
			{
				throw new AttributeNotPublished( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "registerObjectInstanceWithRegion", theException );
				return -1;
			}
		}
	}

	public int registerObjectInstanceWithRegion( int theClass,
	                                             String theObject,
	                                             int[] theAttributes,
	                                             Region[] theRegions )
		throws ObjectClassNotDefined,
		       ObjectClassNotPublished,
		       AttributeNotDefined,
		       AttributeNotPublished,
		       RegionNotKnown,
		       InvalidRegionContext,
		       ObjectAlreadyRegistered,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		if( theAttributes == null )
			throw new RTIinternalError( "null passed for attribute array" );

		RegisterObject request =
			new RegisterObject( theClass, theObject, theAttributes, convertRegions(theRegions) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return ((OCInstance)response.getResult()).getHandle();
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JObjectClassNotPublished )
			{
				throw new ObjectClassNotPublished( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotPublished )
			{
				throw new AttributeNotPublished( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
			}
			else if( theException instanceof JObjectAlreadyRegistered )
			{
				throw new ObjectAlreadyRegistered( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "registerObjectInstanceWithRegion", theException );
				return -1;
			}
		}
	}
	
	public void associateRegionForUpdates( Region theRegion,
	                                       int theObject,
	                                       AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       InvalidRegionContext,
		       RegionNotKnown,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		// construct the message, we'll need the region token for this
		AssociateRegion request = new AssociateRegion( convertRegion(theRegion).getToken(),
		                                               theObject,
		                                               convertSet(attributes) );
		ResponseMessage response = processMessage( request );
		
		// if we were successful, just return, otherwise, process the error
		if( response.isError() )
		{
			Throwable theException = ((ErrorResponse)response).getCause();
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "associateRegionForUpdates", theException );
			}
		}
	}

	public void unassociateRegionForUpdates( Region theRegion, int theObject )
		throws ObjectNotKnown,
		       InvalidRegionContext,
		       RegionNotKnown,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		// construct the message, we'll need the region token for this
		UnassociateRegion request = new UnassociateRegion( convertRegion(theRegion).getToken(),
		                                                   theObject );
		ResponseMessage response = processMessage( request );
		
		// if we were successful, just return, otherwise, process the error
		if( response.isError() )
		{
			Throwable theException = ((ErrorResponse)response).getCause();
			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectNotKnown )
			{
				throw new ObjectNotKnown( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "unassociateRegionForUpdates", theException );
			}
		}
	}

	public void subscribeObjectClassAttributesWithRegion( int theClass,
	                                                      Region theRegion,
	                                                      AttributeHandleSet attributes )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       RegionNotKnown,
		       InvalidRegionContext,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int regionToken = convertRegion(theRegion).getToken();
		SubscribeObjectClass request =
			new SubscribeObjectClass( theClass, convertSet(attributes), true, regionToken );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "subscribeObjectClassAttributesWithRegion", theException );
			}
		}
	}

	public void subscribeObjectClassAttributesPassivelyWithRegion( int theClass,
	                                                               Region theRegion,
	                                                               AttributeHandleSet attributes )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       RegionNotKnown,
		       InvalidRegionContext,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesPassivelyWithRegion()" );
	}

	public void unsubscribeObjectClassWithRegion( int theClass, Region theRegion )
		throws ObjectClassNotDefined,
		       RegionNotKnown,
		       ObjectClassNotSubscribed,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnsubscribeObjectClass request =
			new UnsubscribeObjectClass( theClass, convertRegion(theRegion).getToken() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JObjectClassNotSubscribed )
			{
				throw new ObjectClassNotSubscribed( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "unsubscirbeObjectClassWithRegion", theException );
			}
		}
	}

	public void subscribeInteractionClassWithRegion( int theClass, Region theRegion )
		throws InteractionClassNotDefined,
		       RegionNotKnown,
		       InvalidRegionContext,
		       FederateLoggingServiceCalls,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SubscribeInteractionClass request =
			new SubscribeInteractionClass( theClass, false, convertRegion(theRegion).getToken() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "subscribeInteractionClassWithRegion", theException );
			}
		}
	}

	public void subscribeInteractionClassPassivelyWithRegion( int theClass, Region theRegion )
		throws InteractionClassNotDefined,
		       RegionNotKnown,
		       InvalidRegionContext,
		       FederateLoggingServiceCalls,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "subscribeInteractionClassPassivelyWithRegion()" );
	}

	public void unsubscribeInteractionClassWithRegion( int theClass, Region theRegion )
		throws InteractionClassNotDefined,
		       InteractionClassNotSubscribed,
		       RegionNotKnown,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnsubscribeInteractionClass request =
			new UnsubscribeInteractionClass( theClass, convertRegion(theRegion).getToken() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JInteractionClassNotSubscribed )
			{
				throw new InteractionClassNotSubscribed( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "unsubscribeInteractionClassWithRegion", theException );
			}
		}
	}

	public EventRetractionHandle sendInteractionWithRegion( int theInteraction,
	                                                        SuppliedParameters theParameters,
	                                                        byte[] theTime,
	                                                        String tag,
	                                                        Region theRegion )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       InteractionParameterNotDefined,
		       InvalidFederationTime,
		       RegionNotKnown,
		       InvalidRegionContext,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		HLA13ByteArrayMap map = theParameters.toPorticoMap();
		double time = 0.0;
		try
		{
			time = EncodingHelpers.decodeDouble( theTime );
		}
		catch( Exception e )
		{
			throw new InvalidFederationTime( "Exception decoding time to double: "+e.getMessage() );
		}

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SendInteraction request = new SendInteraction( theInteraction,
		                                               tag.getBytes(),
		                                               map.toJavaMap(),
		                                               convertRegion(theRegion).getToken(),
		                                               time );
		ResponseMessage response = processMessage( request );
		
		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return null;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JInteractionClassNotPublished )
			{
				throw new InteractionClassNotPublished( theException );
			}
			else if( theException instanceof JInteractionParameterNotDefined )
			{
				throw new InteractionParameterNotDefined( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidFederationTime( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "sentInteractionWithRegion(LogicalTime)", theException );
				throw new RTIinternalError( theException.getMessage(), theException );
			}
		}
	}

	public void sendInteractionWithRegion( int theInteraction,
	                                       SuppliedParameters theParameters,
	                                       String tag,
	                                       Region theRegion )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       InteractionParameterNotDefined,
		       RegionNotKnown,
		       InvalidRegionContext,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = theParameters.toPorticoMap();
		SendInteraction request = new SendInteraction( theInteraction,
		                                               tag.getBytes(),
		                                               map.toJavaMap(),
		                                               convertRegion(theRegion).getToken() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JInteractionClassNotDefined )
			{
				throw new InteractionClassNotDefined( theException );
			}
			else if( theException instanceof JInteractionClassNotPublished )
			{
				throw new InteractionClassNotPublished( theException );
			}
			else if( theException instanceof JInteractionParameterNotDefined )
			{
				throw new InteractionParameterNotDefined( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "sendInteractionWithRegion", theException );
			}
		}
	}
	
	public void requestClassAttributeValueUpdateWithRegion( int theClass,
	                                                        AttributeHandleSet attributes,
	                                                        Region theRegion )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       RegionNotKnown,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RequestClassUpdate request = new RequestClassUpdate( theClass,
		                                                     convertSet(attributes),
		                                                     null,
		                                                     convertRegion(theRegion).getToken() );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return;
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
			}
			else if( theException instanceof JObjectClassNotDefined )
			{
				throw new ObjectClassNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JConcurrentAccessAttempted )
			{
				throw new ConcurrentAccessAttempted( theException );
			}
			else
			{
				logException( "requestClassAttributeValueUpdateWithRegion", theException );
			}
		}
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////// 
	/////////////////////////////////// RTI Support Services /////////////////////////////////// 
	//////////////////////////////////////////////////////////////////////////////////////////// 
	public int getObjectClassHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		// get the class
		OCMetadata cls = helper.getFOM().getObjectClass( theName );
		if( cls == null )
			throw new NameNotFound( theName );
		else
			return cls.getHandle();
	}

	// 10.3
	public String getObjectClassName( int theHandle )
		throws ObjectClassNotDefined,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		// get the class
		OCMetadata cls = helper.getFOM().getObjectClass( theHandle );
		if( cls == null )
		{
			throw new ObjectClassNotDefined( "handle: " + theHandle );
		}
		else
		{
			if( theHandle < ObjectModel.MAX_MOM_HANDLE )
				return Mom.strip1516Crap( cls.getQualifiedName() );
			else
				return cls.getQualifiedName();
		}
	}

	// 10.4
	public int getAttributeHandle( String theName, int whichClass )
		throws ObjectClassNotDefined,
		       NameNotFound,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		OCMetadata cls = helper.getFOM().getObjectClass( whichClass );
		if( cls == null )
			throw new ObjectClassNotDefined( "handle: " + whichClass );
		
		ACMetadata aClass = helper.getFOM().getAttributeClass( whichClass, theName );
		if( aClass == null )
			throw new NameNotFound( "name: " + theName );
		else
			return aClass.getHandle();
	}

	// 10.5
	public String getAttributeName( int theHandle, int whichClass )
		throws ObjectClassNotDefined,
		       AttributeNotDefined, 
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		OCMetadata cls = helper.getFOM().getObjectClass( whichClass );
		if( cls == null )
		{
			throw new ObjectClassNotDefined( "handle: " + whichClass );
		}
		else
		{
			String name = cls.getAttributeName( theHandle );
			if( name == null )
			{
				throw new AttributeNotDefined( "handle: " + theHandle );
			}
			else
			{
				if( theHandle < ObjectModel.MAX_MOM_HANDLE )
					return Mom.strip1516Crap( name );
				else
					return name;
			}
		}
	}

	// 10.6
	public int getInteractionClassHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		// get the class
		ICMetadata cls = helper.getFOM().getInteractionClass( theName );
		if( cls == null )
			throw new NameNotFound( theName );
		else
			return cls.getHandle();
	}

	// 10.7
	public String getInteractionClassName( int theHandle )
		throws InteractionClassNotDefined,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		// get the class
		ICMetadata cls = helper.getFOM().getInteractionClass( theHandle );
		if( cls == null )
			throw new InteractionClassNotDefined( "handle: " + theHandle );
		else
			return cls.getQualifiedName();
	}

	// 10.8
	public int getParameterHandle( String theName, int whichClass )
		throws InteractionClassNotDefined,
		       NameNotFound,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		ICMetadata cls = helper.getFOM().getInteractionClass( whichClass );
		if( cls == null )
		{
			throw new InteractionClassNotDefined( "handle: " + whichClass );
		}
		else
		{
			int handle = cls.getParameterHandle( theName );
			if( handle == ObjectModel.INVALID_HANDLE )
			{
				throw new NameNotFound( "name: " + theName );
			}
			else
			{
				return handle;
			}
		}
	}

	// 10.9
	public String getParameterName( int theHandle, int whichClass )
		throws InteractionClassNotDefined,
		       InteractionParameterNotDefined,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		ICMetadata cls = helper.getFOM().getInteractionClass( whichClass );
		if( cls == null )
		{
			throw new InteractionClassNotDefined( "handle: " + whichClass );
		}
		else
		{
			String name = cls.getParameterName( theHandle );
			if( name == null )
			{
				throw new InteractionParameterNotDefined( "handle: " + theHandle );
			}
			else
			{
				return name;
			}
		}
	}

	// 10.10
	public int getObjectInstanceHandle( String theName )
		throws ObjectNotKnown,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		OCInstance instance = helper.getState().getRepository().getInstance( theName );
		if( instance == null )
			throw new ObjectNotKnown( "name: " + theName );
		else
			return instance.getHandle();
	}

	// 10.11
	public String getObjectInstanceName( int theHandle )
		throws ObjectNotKnown,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();

		OCInstance instance = helper.getState().getRepository().getInstance( theHandle );
		if( instance == null )
			throw new ObjectNotKnown( "handle: " + theHandle );
		else
			return instance.getName();
	}

	public int getRoutingSpaceHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkAccess();
		
		Space space = helper.getFOM().getSpace( theName );
		if( space == null )
			throw new NameNotFound( "space: " + theName );
		else
			return space.getHandle();
	}

	public String getRoutingSpaceName( int theHandle )
		throws SpaceNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkAccess();

		Space space = helper.getFOM().getSpace( theHandle );
		if( space == null )
			throw new SpaceNotDefined( "space: " + theHandle );
		else
			return space.getName();
	}
	
	public int getDimensionHandle( String theName, int whichSpace )
		throws SpaceNotDefined,
		       NameNotFound,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkAccess();

		// get the space
		Space space = helper.getFOM().getSpace( whichSpace );
		if( space == null )
			throw new SpaceNotDefined( "space: " + whichSpace );
		
		// get the dimension
		Dimension dimension = space.getDimension( theName );
		if( dimension == null )
		{
			throw new NameNotFound( "dimension: " + theName + ", space: " + whichSpace );
		}
		else
		{
			return dimension.getHandle();
		}
	}

	public String getDimensionName( int theHandle, int whichSpace )
		throws SpaceNotDefined,
		       DimensionNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkAccess();

		// get the space
		Space space = helper.getFOM().getSpace( whichSpace );
		if( space == null )
		{
			throw new SpaceNotDefined( "space: " + whichSpace );
		}
		
		// get the dimension
		Dimension dimension = space.getDimension( theHandle );
		if( dimension == null )
		{
			throw new DimensionNotDefined( "dimension: " + theHandle + ", space: " + whichSpace );
		}
		else
		{
			return dimension.getName();
		}
	}
	
	public int getAttributeRoutingSpaceHandle( int theHandle, int whichClass )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		helper.checkJoined();

		// get the class
		OCMetadata objectClass = helper.getFOM().getObjectClass( whichClass );
		if( objectClass == null )
			throw new ObjectClassNotDefined( "handle: " + whichClass );
		
		// get the attribute
		ACMetadata attributeClass = objectClass.getAttribute( theHandle );
		if( attributeClass == null )
			throw new AttributeNotDefined( "handle: " + theHandle + ", class: " + objectClass );
		
		if( attributeClass.getSpace() == null )
			return PorticoConstants.NULL_HANDLE;
		else
			return attributeClass.getSpace().getHandle();
	}

	// 10.17 
	public int getObjectClass( int theObject )
		throws ObjectNotKnown,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();
		
		OCInstance instance = helper.getState().getRepository().getInstance( theObject );
		if( instance == null )
			throw new ObjectNotKnown( "handle: " + theObject );
		else
			return instance.getDiscoveredType().getHandle();
	}

	public int getInteractionRoutingSpaceHandle( int theHandle )
		throws InteractionClassNotDefined,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		helper.checkJoined();

		// get the class
		ICMetadata interactionClass = helper.getFOM().getInteractionClass( theHandle );
		if( interactionClass == null )
			throw new InteractionClassNotDefined( "handle: " + theHandle );
		
		if( interactionClass.getSpace() == null )
			return PorticoConstants.NULL_HANDLE;
		else
			return interactionClass.getSpace().getHandle();
	}

	public int getTransportationHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		featureNotSupported( "getTransportationHandle()" );
		return -1; // keep the compiler happy
	}

	public String getTransportationName( int theHandle )
		throws InvalidTransportationHandle,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		featureNotSupported( "getTransportationName()" );
		return ""; // keep the compiler happy
	}

	public int getOrderingHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		featureNotSupported( "getOrderingHandle()" );
		return -1; // keep the compiler happy
	}

	public String getOrderingName( int theHandle )
		throws InvalidOrderingHandle,
		       FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		featureNotSupported( "getOrderingName()" );
		return ""; // keep the compiler happy
	}

	public void enableClassRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "enableClassRelevanceAdvisorySwitch()" );
	}

	public void disableClassRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "disableClassRelevanceAdvisorySwitch()" );
	}

	public void enableAttributeRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "enableAttributeRelevanceAdvisorySwitch()" );
	}

	public void disableAttributeRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "disableAttributeRelevanceAdvisorySwitch()" );
	}

	public void enableAttributeScopeAdvisorySwitch()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "enableAttributeScopeAdvisorySwitch()" );
	}

	public void disableAttributeScopeAdvisorySwitch()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "disableAttributeScopeAdvisorySwitch()" );
	}

	public void enableInteractionRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "enableInteractionRelevanceAdvisorySwitch()" );
	}

	public void disableInteractionRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "disableInteractionRelevanceAdvisorySwitch()" );
	}

	public Region getRegion( int regionToken )
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RegionNotKnown,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkAccess();
		
		// look the region up in the region store
		RegionInstance region = helper.getState().getRegionStore().getRegion( regionToken );
		if( region == null )
			throw new RegionNotKnown( "region token: " + regionToken );
		else
			return new Java1Region( region );
	}

	public int getRegionToken( Region region )
		throws FederateNotExecutionMember,
		       ConcurrentAccessAttempted,
		       RegionNotKnown,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkAccess();
		
		// make sure we have the appropriate type of region
		try
		{
			Java1Region java1Region = (Java1Region)region;
			// we could just get the region handle directly from the given instance, but
			// we only want to return the handle if this is a region WE (the current LRC)
			// knows about. Thus, we have to consult the LRCRegionStore
			if( helper.getState().getRegionStore().containsRegion(java1Region) )
				return java1Region.getRegionHandle();
			else
				throw new RegionNotKnown( "Region is unknown to this federate" );
		}
		catch( ClassCastException e )
		{
			throw new RTIinternalError( "Non-Portico Region Implementation" );
		}
	}
	
	public boolean tick()
		throws SpecifiedSaveLabelDoesNotExist,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		helper.tick();
		return true;
	}

	public boolean tick( double minimum, double maximum )
		throws SpecifiedSaveLabelDoesNotExist,
		       ConcurrentAccessAttempted,
		       RTIinternalError
	{
		return helper.tick( minimum, maximum );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Private Utility Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method will take a given {@link PorticoMessage} and pass it to the LRC for processing.
	 * If the LRC isn't regulating, it will remove any timestamp. The current handle of the LRC
	 * (returned from the joinFederation method) will be added to the message as the source
	 * federate handle. If the response is null, an RTIinternalError will be thrown, otherwise the
	 * ResponseMessage that was given back will be returned.
	 */
	private ResponseMessage processMessage( PorticoMessage request )
	{
		try
		{
			// make sure we don't have concurrent access problems
			this.helper.checkAccess();
			
			// FIX: PORT-106: remove the time from the message if we are not constrained
			if( this.helper.getState().isRegulating() == false )
			{
				request.setTimestamp( PorticoConstants.NULL_TIME );
			}
			
			// set the target federate, if we have not joined yet (or have resigned)
			// this will be null
			request.setSourceFederate( this.helper.getState().getFederateHandle() );
			
			// create the context
			MessageContext message = new MessageContext( request );
	
			// pass it to the sink
			helper.processMessage( message );

			// check for a response
			if( message.getResponse() == null )
			{
				throw new RTIinternalError( "No response from RTI (null) for message type: " +
											request.getIdentifier() );
			}

			// if the response is an error, log it
			if( message.isSuccessResponse() == false && message.getResponse() != null )
			{
				helper.getLrcLogger().error( ((ErrorResponse)message.getResponse()).getCause() );
			}
			
			// return the response
			return message.getResponse();
		}
		catch( Exception e )
		{
			// log the exception
			this.helper.getLrcLogger().error( e );
			
			// there was an exception, pacakge a response
			return new ErrorResponse( e );
		}
	}
	
	/**
	 * This method prints the stack trace for the exception and then throws an RTIinternalError 
	 */
	private void logException( String method, Throwable e ) throws RTIinternalError
	{
		throw new RTIinternalError( "Unknown exception received from RTI (" + e.getClass() +
			") for " + method + "(): "+ e.getMessage(), e );
	}

	/**
	 * Logs that the user tried to call a method that isn't supported yet and then throws an
	 * RTIinternalError.
	 */
	private void featureNotSupported( String methodName ) throws RTIinternalError
	{
		logger.error( "Rti13Java1Ambassador doesn't yet support " + methodName );
		if( PorticoConstants.shouldThrowExceptionForUnsupportedCall() )
			throw new RTIinternalError( "Rti13Java1Ambassador doesn't yet support " + methodName );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Type Conversion Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts the given FederateHandleSet to a pure java HashSet. If the given set is null,
	 * null will be returned.
	 */
	private HashSet<Integer> convertSet( FederateHandleSet fhs ) throws RTIinternalError
	{
		if( fhs == null )
			return null;
		else
			return fhs.toPorticoSet().toJavaSet();
	}
	
	/**
	 * Converts the given AttributeHandleSet to a pure java HashSet. If the given set is null,
	 * null will be returned.
	 */
	private HashSet<Integer> convertSet( AttributeHandleSet ahs ) throws RTIinternalError
	{
		if( ahs == null )
			return null;
		else
			return ahs.toPorticoSet().toJavaSet();
	}

	/**
	 * This method will take in a given region instance, check that it is of the appropriate
	 * Portico type, convert it to the type and return it. If it isn't the right type, an
	 * exception will be thrown. If the region is null, an exception will be thrown.
	 */
	private RegionInstance convertRegion( Region region ) throws RegionNotKnown
	{
		if( region == null )
			throw new RegionNotKnown( "Null region received" );

		try
		{
			return ((Java1Region)region).getWrappedRegion();
		}
		catch( ClassCastException cce )
		{
			throw new RegionNotKnown( "Supplied region is not a Portico region", cce );
		}
	}

	/**
	 * Convert the given array of regions to an array of integers that represents the region
	 * tokens of each of the regions.
	 */
	private int[] convertRegions( Region[] regions ) throws RegionNotKnown
	{
		if( regions == null )
			throw new RegionNotKnown( "Null region array received" );

		int[] tokenArray = new int[regions.length];
		
		for( int i = 0; i < regions.length; i++ )
		{
			Region current = regions[i];
			
			// make sure we have a region
			if( current == null )
				throw new RegionNotKnown( "Null region in array at index: " + i );
			
			// try to convert the region to get its token
			try
			{
				tokenArray[i] = ((Java1Region)current).getWrappedRegion().getToken();
			}
			catch( ClassCastException cce )
			{
				throw new RegionNotKnown( "Supplied region was not a Portico implementation: " +
				                          current.getClass(), cce );
			}
		}
		
		return tokenArray;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
