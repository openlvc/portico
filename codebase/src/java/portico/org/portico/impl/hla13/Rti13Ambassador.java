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

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import hla.rti.*;
import hla.rti.jlc.RTIambassadorEx;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.hla13.types.DoubleTime;
import org.portico.impl.hla13.types.DoubleTimeInterval;
import org.portico.impl.hla13.types.HLA13AttributeHandleSet;
import org.portico.impl.hla13.types.HLA13ByteArrayMap;
import org.portico.impl.hla13.types.HLA13Region;
import org.portico.impl.hla13.types.HLA13Set;
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
import org.portico.utils.messaging.ErrorResponse;
import org.portico.utils.messaging.ExtendedSuccessMessage;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.PorticoMessage;
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

public class Rti13Ambassador implements RTIambassadorEx
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl13Helper helper;
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Rti13Ambassador() throws RTIinternalError
	{
		this.helper = new Impl13Helper();
		this.logger = this.helper.getLrcLogger();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public Impl13Helper getHelper()
	{
		return this.helper;
	}

	//////////////////////////////////////////////////////////////////////////////////////////// 
	////////////////////////////// Federation Management Services ////////////////////////////// 
	//////////////////////////////////////////////////////////////////////////////////////////// 
	// 4.2 
	public void createFederationExecution( String executionName, URL fed )
		throws FederationExecutionAlreadyExists,
		       CouldNotOpenFED,
		       ErrorReadingFED,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		CreateFederation request = new CreateFederation( executionName, fed );
		request.setHlaVersion( HLAVersion.HLA13 );
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
			else if( theException instanceof JErrorReadingFED )
			{
				throw new ErrorReadingFED( theException );
			}
			else if( theException instanceof JCouldNotOpenFED )
			{
				throw new CouldNotOpenFED( theException );
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

	// 4.3 
	public void destroyFederationExecution( String executionName )
		throws FederatesCurrentlyJoined,
		       FederationExecutionDoesNotExist,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 4.4 
	public int joinFederationExecution( String federateName,
	                                    String federationName,
	                                    FederateAmbassador federateReference )
		throws FederateAlreadyExecutionMember,
		       FederationExecutionDoesNotExist,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		// 0. check the federate ambassador //
		// Has to be done here as part of fix for PORT-132 //
		if( federateReference == null )
			throw new RTIinternalError( "FederateAmbassador was null" );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// set the federate ambassador now, weill need it for callbacks as a result of the join,
		// these will occur when we get RoleCall from other federates (processed before the join
		// handler returns, so setting it after processMessage() won't help)
		this.helper.setFederateAmbassador( federateReference );
		
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

	// 4.4 
	public int joinFederationExecution( String federateType,
	                                    String federationExecutionName,
	                                    FederateAmbassador federateReference,
	                                    MobileFederateServices serviceReferences )
		throws FederateAlreadyExecutionMember,
		       FederationExecutionDoesNotExist,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		return joinFederationExecution( federateType, federationExecutionName, federateReference );
	}

	// 4.5 
	public void resignFederationExecution( int resignAction )
		throws FederateOwnsAttributes,
		       FederateNotExecutionMember,
		       InvalidResignAction,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		ResignFederation request;
		try
		{
			request = new ResignFederation( JResignAction.for13Value(resignAction) );
		}
		catch( JInvalidResignAction e )
		{
			throw new InvalidResignAction( e.getMessage(), e );
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

	// 4.6 
	public void registerFederationSynchronizationPoint( String label, byte[] tag )
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterSyncPoint request = new RegisterSyncPoint( label, tag );
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

	// 4.6 
	public void registerFederationSynchronizationPoint( String label,
	                                                    byte[] tag,
	                                                    FederateHandleSet syncset )
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterSyncPoint request = new RegisterSyncPoint(label, tag, convertSet(syncset));
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

	// 4.9 
	public void synchronizationPointAchieved( String label )
		throws SynchronizationLabelNotAnnounced,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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
			else if( theException instanceof JSynchronizationLabelNotAnnounced )
			{
				throw new SynchronizationLabelNotAnnounced( theException );
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
				logException( "synchronizationPointAchieved", theException );
			}
		}
	}

	// 4.11 
	public void requestFederationSave( String label, LogicalTime theTime )
		throws FederationTimeAlreadyPassed,
		       InvalidFederationTime,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		double time = convertTime( theTime );
		if( time == PorticoConstants.NULL_TIME )
			throw new InvalidFederationTime( "LogicalTime was null" );

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

	// 4.11 
	public void requestFederationSave( String label )
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 4.13 
	public void federateSaveBegun()
		throws SaveNotInitiated,
		       FederateNotExecutionMember,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 4.14 
	public void federateSaveComplete()
		throws SaveNotInitiated,
		       FederateNotExecutionMember,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 4.14 
	public void federateSaveNotComplete()
		throws SaveNotInitiated,
		       FederateNotExecutionMember,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 4.16 
	public void requestFederationRestore( String label )
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 4.20 
	public void federateRestoreComplete()
		throws RestoreNotRequested,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 4.20 
	public void federateRestoreNotComplete()
		throws RestoreNotRequested,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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
	///////////////////////////// Declaration Management Services ////////////////////////////// 
	//////////////////////////////////////////////////////////////////////////////////////////// 
	// 5.2 
	public void publishObjectClass( int theClass, AttributeHandleSet attributeList )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       OwnershipAcquisitionPending,
		       FederateNotExecutionMember, 
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		PublishObjectClass request = new PublishObjectClass( theClass, convertSet(attributeList) );
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
				logException( "publishObjectClass", theException );
			}
		}
	}

	// 5.3 
	public void unpublishObjectClass( int theClass )
		throws ObjectClassNotDefined,
		       ObjectClassNotPublished,
		       OwnershipAcquisitionPending,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 5.4 
	public void publishInteractionClass( int theInteraction )
		throws InteractionClassNotDefined,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 5.5 
	public void unpublishInteractionClass( int theInteraction )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 5.6 
	public void subscribeObjectClassAttributes( int theClass, AttributeHandleSet attributes )
		throws ObjectClassNotDefined,
		       AttributeNotDefined, 
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SubscribeObjectClass request = new SubscribeObjectClass( theClass, convertSet(attributes) );
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

	// 5.6 
	public void subscribeObjectClassAttributesPassively( int theClass,
					                                     AttributeHandleSet attributeList )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SubscribeObjectClass request = new SubscribeObjectClass( theClass,
		                                                         convertSet(attributeList),
		                                                         true );
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
				logException( "subscribeObjectClassAttributesPassively", theException );
			}
		}
	}
	
	// 5.7 
	public void unsubscribeObjectClass( int theClass )
		throws ObjectClassNotDefined,
		       ObjectClassNotSubscribed,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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
				logException( "unsubscribeObjectClass", theException );
			}
		}
	}
	
	// 5.8 
	public void subscribeInteractionClass( int theClass )
		throws InteractionClassNotDefined,
		       FederateNotExecutionMember,
		       FederateLoggingServiceCalls,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 5.8 
	public void subscribeInteractionClassPassively( int theClass )
		throws InteractionClassNotDefined,
		       FederateNotExecutionMember,
		       FederateLoggingServiceCalls,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SubscribeInteractionClass request = new SubscribeInteractionClass( theClass, true );
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
				logException( "subscribeInteractionClassPassively", theException );
			}
		}
	}

	// 5.9 
	public void unsubscribeInteractionClass( int theClass )
		throws InteractionClassNotDefined,
		       InteractionClassNotSubscribed,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError, 
		       ConcurrentAccessAttempted
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
	// 6.2 
	public int registerObjectInstance( int theClass )
		throws ObjectClassNotDefined,
		       ObjectClassNotPublished,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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
				return PorticoConstants.NULL_HANDLE;
			}
		}
	}

	// 6.2 
	public int registerObjectInstance( int theClass, String theObject )
		throws ObjectClassNotDefined,
		       ObjectClassNotPublished,
		       ObjectAlreadyRegistered,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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
				return PorticoConstants.NULL_HANDLE;
			}
		}
	}
	
	// 6.4 
	public void updateAttributeValues( int theObject,
	                                   SuppliedAttributes theAttributes,
	                                   byte[] tag )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = convertAttributes( theAttributes );
		UpdateAttributes request = new UpdateAttributes( theObject, tag, map.toJavaMap() );
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
	
	// 6.4 
	public EventRetractionHandle updateAttributeValues( int theObject,
	                                                    SuppliedAttributes theAttributes,
	                                                    byte[] tag,
	                                                    LogicalTime theTime )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       InvalidFederationTime,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = convertAttributes( theAttributes );
		UpdateAttributes request = new UpdateAttributes( theObject,
		                                                 tag,
		                                                 map.toJavaMap(),
		                                                 convertTime(theTime) );
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
				return null;
			}
		}
	}
	
	// 6.6 
	public void sendInteraction( int interaction, SuppliedParameters theParameters, byte[] tag )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       InteractionParameterNotDefined,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError, 
		       ConcurrentAccessAttempted
	{
   		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = convertParameters( theParameters );
		SendInteraction request = new SendInteraction( interaction, tag, map.toJavaMap() );
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
	
	// 6.6 
	public EventRetractionHandle sendInteraction( int interaction,
	                                              SuppliedParameters theParameters,
	                                              byte[] tag,
	                                              LogicalTime theTime )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       InteractionParameterNotDefined,
		       InvalidFederationTime,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
  		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = convertParameters( theParameters );
		SendInteraction request = new SendInteraction( interaction,
		                                               tag,
		                                               map.toJavaMap(),
		                                               convertTime(theTime) );
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
				logException( "sendInteraction(LogicalTime)", theException );
				return null;
			}
		}
	}

	// 6.8 
	public void deleteObjectInstance( int ObjectHandle, byte[] userSuppliedTag )
		throws ObjectNotKnown,
		       DeletePrivilegeNotHeld, 
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DeleteObject request = new DeleteObject( ObjectHandle, userSuppliedTag );
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

	// 6.8 
	public EventRetractionHandle deleteObjectInstance( int objectHandle,
	                                                   byte[] tag,
	                                                   LogicalTime theTime )
		throws ObjectNotKnown,
		       DeletePrivilegeNotHeld,
		       InvalidFederationTime,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DeleteObject request = new DeleteObject( objectHandle, tag, convertTime(theTime) );
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
				logException( "deleteObjectInstance(LogicalTime)", theException );
				return null;
			}
		}
	}

	// 6.10 
	public void localDeleteObjectInstance( int objectHandle )
		throws ObjectNotKnown,
		       FederateOwnsAttributes,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		LocalDelete request = new LocalDelete( objectHandle );
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

	// 6.11 
	public void changeAttributeTransportationType( int theObject,
	                                               AttributeHandleSet theAttributes,
	                                               int theType )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       InvalidTransportationHandle,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		featureNotSupported( "changeAttributeTransportationType()" );
	}

	// 6.12 
	public void changeInteractionTransportationType( int theClass, int theType )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished,
		       InvalidTransportationHandle,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		featureNotSupported( "changeInteractionTransportationType()" );
	}

	// 6.15 
	public void requestObjectAttributeValueUpdate( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RequestObjectUpdate request = new RequestObjectUpdate( theObject, convertSet(theAttributes) );
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

	// 6.15 
	public void requestClassAttributeValueUpdate( int theClass, AttributeHandleSet theAttributes )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RequestClassUpdate request = new RequestClassUpdate( theClass,
		                                                     convertSet(theAttributes),
		                                                     null );
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
	////////////////////////////// Ownership Management Services ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////// 
	// 7.2 
	public void unconditionalAttributeOwnershipDivestiture( int theObject,
	                                                        AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 7.3 
	public void negotiatedAttributeOwnershipDivestiture( int theObject,
	                                                     AttributeHandleSet attributes,
	                                                     byte[] tag )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       AttributeAlreadyBeingDivested,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		AttributeDivest request = new AttributeDivest( theObject, convertSet(attributes), tag );
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

	// 7.7 
	public void attributeOwnershipAcquisition( int object, AttributeHandleSet atts, byte[] tag )
		throws ObjectNotKnown,
		       ObjectClassNotPublished,
		       AttributeNotDefined,
		       AttributeNotPublished,
		       FederateOwnsAttributes,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		AttributeAcquire request = new AttributeAcquire( object, convertSet(atts), tag );
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

	// 7.8 
	public void attributeOwnershipAcquisitionIfAvailable( int theObject,
	                                                      AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       ObjectClassNotPublished,
		       AttributeNotDefined,
		       AttributeNotPublished,
		       FederateOwnsAttributes,
		       AttributeAlreadyBeingAcquired,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 7.11 
	@SuppressWarnings("unchecked")
	public AttributeHandleSet attributeOwnershipReleaseResponse( int theObject,
	                                                             AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       FederateWasNotAskedToReleaseAttribute,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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
			return new HLA13AttributeHandleSet( (Set<Integer>)response.getResult() );
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
				return null; // above throws exception, this is just to keep compiler happy
			}
		}
	}

	// 7.12 
	public void cancelNegotiatedAttributeOwnershipDivestiture( int theObject,
	                                                           AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       AttributeDivestitureWasNotRequested,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 7.13 
	public void cancelAttributeOwnershipAcquisition( int theObject,
	                                                 AttributeHandleSet attributes )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeAlreadyOwned,
		       AttributeAcquisitionWasNotRequested,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 7.15 
	public void queryAttributeOwnership( int theObject, int theAttribute )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 7.17 
	public boolean isAttributeOwnedByFederate( int theObject, int theAttribute )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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
	// 8.2 
	public void enableTimeRegulation( LogicalTime theFederateTime,
	                                  LogicalTimeInterval theLookahead )
		throws TimeRegulationAlreadyEnabled,
		       EnableTimeRegulationPending,
		       TimeAdvanceAlreadyInProgress,
		       InvalidFederationTime,
		       InvalidLookahead,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		// check the lookahead //
		double doubleLA = convertLookahead( theLookahead );
		double fedTime = convertTime( theFederateTime );
		// check the federate time as null is not allowed (but is allowed by the conversion method
		if( fedTime == PorticoConstants.NULL_TIME )
			throw new InvalidFederationTime( "Federate time was null" );
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		EnableTimeRegulation request = new EnableTimeRegulation( fedTime, doubleLA );
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

	// 8.4 
	public void disableTimeRegulation()
		throws TimeRegulationWasNotEnabled,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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
	
	// 8.5 
	public void enableTimeConstrained()
		throws TimeConstrainedAlreadyEnabled,
		       EnableTimeConstrainedPending,
		       TimeAdvanceAlreadyInProgress,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 8.7 
	public void disableTimeConstrained()
		throws TimeConstrainedWasNotEnabled,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
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

	// 8.8 
	public void timeAdvanceRequest( LogicalTime theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = convertTime( theTime );
		if( time == PorticoConstants.NULL_TIME )
			throw new InvalidFederationTime( "LogicalTime was null" );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
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

	// 8.9 
	public void timeAdvanceRequestAvailable( LogicalTime theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = convertTime( theTime );
		if( time == PorticoConstants.NULL_TIME )
			throw new InvalidFederationTime( "LogicalTime was null" );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
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

	// 8.10 
	public void nextEventRequest( LogicalTime theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = convertTime( theTime );
		if( time == PorticoConstants.NULL_TIME )
			throw new InvalidFederationTime( "LogicalTime was null" );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
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

	// 8.11 
	public void nextEventRequestAvailable( LogicalTime theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = convertTime( theTime );
		if( time == PorticoConstants.NULL_TIME )
			throw new InvalidFederationTime( "LogicalTime was null" );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
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

	// 8.12 
	public void flushQueueRequest( LogicalTime theTime )
		throws InvalidFederationTime,
		       FederationTimeAlreadyPassed,
		       TimeAdvanceAlreadyInProgress,
		       EnableTimeRegulationPending,
		       EnableTimeConstrainedPending,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = convertTime( theTime );
		if( time == PorticoConstants.NULL_TIME )
			throw new InvalidFederationTime( "LogicalTime was null" );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
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

	// 8.14 
	public void enableAsynchronousDelivery()
		throws AsynchronousDeliveryAlreadyEnabled,
		       FederateNotExecutionMember,
		       SaveInProgress, 
		       RestoreInProgress, 
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		ResponseMessage response = processMessage( new EnableAsynchronousDelivery() );

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

	// 8.15 
	public void disableAsynchronousDelivery()
		throws AsynchronousDeliveryAlreadyDisabled,
		       FederateNotExecutionMember, 
		       SaveInProgress, 
		       RestoreInProgress, 
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		ResponseMessage response = processMessage( new DisableAsynchronousDelivery() );

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
	
	// 8.16 
	public LogicalTime queryLBTS()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		// return the time as dictated in the state //
		return new DoubleTime( helper.getState().getFederateLbts() );
	}

	// 8.17 
	public LogicalTime queryFederateTime()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		// return the time as dictated in the state //
		return new DoubleTime( helper.getState().getCurrentTime() );
	}
	
	// 8.18 
	public LogicalTime queryMinNextEventTime()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		// return the next min event time = current requested time + lookahead //
		return new DoubleTime( helper.getState().getRequestedTime() + 
		                       helper.getState().getLookahead() );
	}
	
	// 8.19 
	public void modifyLookahead( LogicalTimeInterval theLookahead )
		throws InvalidLookahead,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double interval = convertLookahead( theLookahead );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		ModifyLookahead request = new ModifyLookahead( interval );
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

	// 8.20 
	public LogicalTimeInterval queryLookahead()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		return new DoubleTimeInterval( this.helper.getState().getLookahead() );
	}

	// 8.21
	public void retract( EventRetractionHandle theHandle )
		throws InvalidRetractionHandle,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		featureNotSupported( "retract()" );
	}
	
	// 8.23 
	public void changeAttributeOrderType( int theObject,
	                                      AttributeHandleSet theAttributes,
	                                      int theType )
		throws ObjectNotKnown,
		       AttributeNotDefined,
		       AttributeNotOwned,
		       InvalidOrderingHandle,
		       FederateNotExecutionMember, 
		       SaveInProgress, 
		       RestoreInProgress,
		       RTIinternalError, 
		       ConcurrentAccessAttempted
	{
		featureNotSupported( "changeAttributeOrderType()" );
	}

	// 8.24 
	public void changeInteractionOrderType( int theClass, int theType )
		throws InteractionClassNotDefined,
		       InteractionClassNotPublished, 
		       InvalidOrderingHandle,
		       FederateNotExecutionMember, 
		       SaveInProgress, 
		       RestoreInProgress, 
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		featureNotSupported( "changeInteractionOrderType()" );
	}


	//////////////////////////////////////////////////////////////////////////////////////////// 
	/////////////////////////////// Data Distribution Management ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////// 
	// 9.2 
	public Region createRegion( int spaceHandle, int numberOfExtents )
		throws SpaceNotDefined,
		       InvalidExtents,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		// create and process the message
		CreateRegion request = new CreateRegion( spaceHandle, numberOfExtents );
		ResponseMessage response = processMessage( request );

		// check the response
		if( response.isError() == false )
		{
			// we have a successful response, extract the region and return it
			return new HLA13Region( (RegionInstance)response.getResult() );
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

	// 9.3 
	public void notifyOfRegionModification( Region modified )
		throws RegionNotKnown,
	           InvalidExtents,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		// construct the message and process it
		ModifyRegion request = new ModifyRegion( convertRegion(modified) );
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

	// 9.4 
	public void deleteRegion( Region theRegion )
		throws RegionNotKnown,
		       RegionInUse,
		       FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError,
		       ConcurrentAccessAttempted
	{
		// construct the message, we'll need the region token for this
		int regionToken = convertRegion(theRegion).getToken();
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

	// 9.5 
	public int registerObjectInstanceWithRegion( int theClass,
	                                             int[] attributes,
	                                             Region[] regions )
	    throws ObjectClassNotDefined,
	           ObjectClassNotPublished,
	           AttributeNotDefined,
	           AttributeNotPublished,
	           RegionNotKnown,
	           InvalidRegionContext,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		if( attributes == null )
			throw new RTIinternalError( "null passed for attribute array" );

		RegisterObject request = new RegisterObject( theClass, attributes, convertRegions(regions) );
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

	// 9.5 
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
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		if( theAttributes == null )
			throw new RTIinternalError( "null passed for attribute array" );

		RegisterObject request =
			new RegisterObject(theClass, theObject, theAttributes, convertRegions(theRegions) );
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

	// 9.6 
	public void associateRegionForUpdates( Region theRegion,
	                                       int theObject,
	                                       AttributeHandleSet theAttributes )
	    throws ObjectNotKnown,
	           AttributeNotDefined,
	           InvalidRegionContext,
	           RegionNotKnown,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		// construct the message, we'll need the region token for this
		AssociateRegion request = new AssociateRegion( convertRegion(theRegion).getToken(),
		                                               theObject,
		                                               convertSet(theAttributes) );
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

	// 9.7 
	public void unassociateRegionForUpdates( Region theRegion, int theObject )
	    throws ObjectNotKnown,
	           InvalidRegionContext,
	           RegionNotKnown,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError, 
	           ConcurrentAccessAttempted
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

	// 9.8 
	public void subscribeObjectClassAttributesWithRegion( int theClass,
	                                                      Region theRegion,
	                                                      AttributeHandleSet attributes )
	    throws ObjectClassNotDefined,
	           AttributeNotDefined,
	           RegionNotKnown,
	           InvalidRegionContext,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int regionToken = convertRegion(theRegion).getToken();
		SubscribeObjectClass request = new SubscribeObjectClass( theClass,
		                                                         convertSet(attributes),
		                                                         true,
		                                                         regionToken );
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

	// 9.8 
	public void subscribeObjectClassAttributesPassivelyWithRegion( int theClass,
	                                                               Region theRegion,
	                                                               AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined,
	           AttributeNotDefined,
	           RegionNotKnown,
	           InvalidRegionContext,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		featureNotSupported( "subscribeObjectClassAttributesPassivelyWithRegion()" );
	}

	// 9.9 
	public void unsubscribeObjectClassWithRegion( int theClass, Region theRegion )
	    throws ObjectClassNotDefined,
	           RegionNotKnown,
	           FederateNotSubscribed,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
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
				throw new FederateNotSubscribed( theException );
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
				logException( "unsubscribeObjectClassWithRegion", theException );
			}
		}
	}

	// 9.10 
	public void subscribeInteractionClassWithRegion( int theClass, Region theRegion )
	    throws InteractionClassNotDefined,
	           RegionNotKnown,
	           InvalidRegionContext,
	           FederateLoggingServiceCalls,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
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

	// 9.10 
	public void subscribeInteractionClassPassivelyWithRegion( int theClass, Region theRegion )
	    throws InteractionClassNotDefined,
	           RegionNotKnown,
	           InvalidRegionContext,
	           FederateLoggingServiceCalls,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		featureNotSupported( "subscribeInteractionClassPassivelyWithRegion()" );
	}

	// 9.11 
	public void unsubscribeInteractionClassWithRegion( int theClass, Region theRegion )
	    throws InteractionClassNotDefined,
	           InteractionClassNotSubscribed,
	           RegionNotKnown,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
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

	// 9.12 
	public void sendInteractionWithRegion( int theInteraction,
	                                       SuppliedParameters theParameters,
	                                       byte[] tag,
	                                       Region theRegion )
	    throws InteractionClassNotDefined,
	           InteractionClassNotPublished,
	           InteractionParameterNotDefined,
	           RegionNotKnown,
	           InvalidRegionContext,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = convertParameters( theParameters );
		int regionToken = convertRegion(theRegion).getToken();
		SendInteraction request =
			new SendInteraction( theInteraction, tag, map.toJavaMap(), regionToken );
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

	// 9.12 
	public EventRetractionHandle sendInteractionWithRegion( int theInteraction,
	                                                        SuppliedParameters theParameters,
	                                                        byte[] tag,
	                                                        Region theRegion,
	                                                        LogicalTime theTime )
	    throws InteractionClassNotDefined,
	           InteractionClassNotPublished,
	           InteractionParameterNotDefined,
	           InvalidFederationTime,
	           RegionNotKnown,
	           InvalidRegionContext,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HLA13ByteArrayMap map = convertParameters( theParameters );
		int regionToken = convertRegion(theRegion).getToken();
		SendInteraction request = new SendInteraction( theInteraction,
		                                               tag,
		                                               map.toJavaMap(),
		                                               regionToken,
		                                               convertTime(theTime) );
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
			else if( theException instanceof JRegionNotKnown )
			{
				throw new RegionNotKnown( theException );
			}
			else if( theException instanceof JInvalidRegionContext )
			{
				throw new InvalidRegionContext( theException );
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
				logException( "sendInteractionWithRegion(timestamped)", theException );
				return null;
			}
		}
	}

	// 9.13 
	public void requestClassAttributeValueUpdateWithRegion( int theClass,
	                                                        AttributeHandleSet theAttributes,
	                                                        Region theRegion )
	    throws ObjectClassNotDefined,
	           AttributeNotDefined,
	           RegionNotKnown,
	           FederateNotExecutionMember,
	           SaveInProgress,
	           RestoreInProgress,
	           RTIinternalError,
	           ConcurrentAccessAttempted
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int regionToken = convertRegion(theRegion).getToken();
		RequestClassUpdate request = new RequestClassUpdate( theClass,
		                                                     convertSet(theAttributes),
		                                                     null,
		                                                     regionToken );
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
	// 10.2 
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
				throw new NameNotFound( "name: " + theName );
			else
				return handle;
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
				throw new InteractionParameterNotDefined( "handle: " + theHandle );
			else
				return name;
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

	// 10.12 
	public int getRoutingSpaceHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();
		
		Space space = helper.getFOM().getSpace( theName );
		if( space == null )
			throw new NameNotFound( "space: " + theName );
		else
			return space.getHandle();
	}
	
	// 10.13 
	public String getRoutingSpaceName( int theHandle )
		throws SpaceNotDefined,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();
		
		Space space = helper.getFOM().getSpace( theHandle );
		if( space == null )
			throw new SpaceNotDefined( "space: " + theHandle );
		else
			return space.getName();
	}

	// 10.14 
	public int getDimensionHandle( String theName, int whichSpace )
		throws SpaceNotDefined,
		       NameNotFound,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();
		
		// get the space
		Space space = helper.getFOM().getSpace( whichSpace );
		if( space == null )
			throw new SpaceNotDefined( "space: " + whichSpace );
		
		// find the dimension in the space
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

	// 10.15 
	public String getDimensionName( int theHandle, int whichSpace )
		throws SpaceNotDefined,
		       DimensionNotDefined,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		helper.checkJoined();
		
		// get the space
		Space space = helper.getFOM().getSpace( whichSpace );
		if( space == null )
			throw new SpaceNotDefined( "space: " + whichSpace );
		
		// find the dimension in the space
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

	// 10.16 
	public int getAttributeRoutingSpaceHandle( int theHandle, int whichClass )
		throws ObjectClassNotDefined,
		       AttributeNotDefined,
		       FederateNotExecutionMember,
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
		
		// get the object
		OCInstance instance = helper.getState().getRepository().getInstance( theObject );
		if( instance == null )
			throw new ObjectNotKnown( "handle: " + theObject );
		
		return instance.getDiscoveredClassHandle();
	}

	// 10.18 
	public int getInteractionRoutingSpaceHandle( int theHandle )
		throws InteractionClassNotDefined,
		       FederateNotExecutionMember,
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

	// 10.19 
	public int getTransportationHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		featureNotSupported( "getTransportationHandle()" );
		return -1; // keep the compiler happy
	}

	// 10.20
	public String getTransportationName( int theHandle )
		throws InvalidTransportationHandle,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		featureNotSupported( "getTransportationName()" );
		return ""; // keep the compiler happy
	}

	// 10.21 
	public int getOrderingHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		featureNotSupported( "getOrderingHandle()" );
		return -1; // keep the compiler happy
	}

	// 10.22 
	public String getOrderingName( int theHandle )
		throws InvalidOrderingHandle,
		       FederateNotExecutionMember,
		       RTIinternalError
	{
		featureNotSupported( "getOrderingName()" );
		return ""; // keep the compiler happy
	}
	
	// 10.23 
	public void enableClassRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "enableClassRelevanceAdvisorySwitch()" );
	}
	
	// 10.24 
	public void disableClassRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "disableClassRelevanceAdvisorySwitch()" );
	}

	// 10.25 
	public void enableAttributeRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "enableAttributeRelevanceAdvisorySwitch()" );
	}

	// 10.26 
	public void disableAttributeRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "disableAttributeRelevanceAdvisorySwitch()" );
	}

	// 10.27 
	public void enableAttributeScopeAdvisorySwitch()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "enableAttributeScopeAdvisorySwitch()" );
	}
	
	// 10.28 
	public void disableAttributeScopeAdvisorySwitch()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "disableAttributeScopeAdvisorySwitch()" );
	}

	// 10.29 
	public void enableInteractionRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
		       SaveInProgress,
		       RestoreInProgress,
		       RTIinternalError
	{
		featureNotSupported( "enableInteractionRelevanceAdvisorySwitch()" );
	}

	// 10.30 
	public void disableInteractionRelevanceAdvisorySwitch()
		throws FederateNotExecutionMember,
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
			return new HLA13Region( region );
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
			HLA13Region hla13Region = (HLA13Region)region;
			// we could just get the region handle directly from the given instance, but
			// we only want to return the handle if this is a region WE (the current LRC)
			// knows about. Thus, we have to consult the LRCRegionStore
			if( helper.getState().getRegionStore().containsRegion(hla13Region) )
				return hla13Region.getRegionHandle();
			else
				throw new RegionNotKnown( "Region is unknown to this federate" );
		}
		catch( ClassCastException e )
		{
			throw new RTIinternalError( "Non-Portico Region Implementation" );
		}
	}

	/**
	 * Keeps processing the callback queue until it is empty (regardless of how long that takes)
	 */
	public void tick() throws RTIinternalError, ConcurrentAccessAttempted
	{
		this.helper.tick();
	}
	
	///// RTIambassadorEx method added by DLC /////
	public boolean tick( final double min, final double max ) 
		throws RTIinternalError, ConcurrentAccessAttempted
	{
		return this.helper.tick( min, max );
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
			this.helper.getLrcLogger().error( e.getMessage(), e );
			
			// there was an exception, pacakge a response
			return new ErrorResponse( e );
		}
	}
	
	/**
	 * This method prints the stack trace for the exception and then throws an RTIinternalError 
	 */
	private void logException( String method, Throwable e ) throws RTIinternalError
	{
		if( e instanceof RTIinternalError )
		{
			throw (RTIinternalError)e;
		}
		else
		{
			throw new RTIinternalError( "Unknown exception received from RTI ("+e.getClass()+
			                            ") for "+method+"(): "+ e.getMessage(), e );
		}
	}
	
	/**
	 * Logs that the user tried to call a method that isn't supported yet and then throws an
	 * RTIinternalError.
	 */
	private void featureNotSupported( String methodName ) throws RTIinternalError
	{
		logger.warn( "Rti13Ambassador doesn't yet support " + methodName );
		if( PorticoConstants.shouldThrowExceptionForUnsupportedCall() )
			throw new RTIinternalError( "Rti13Ambassador doesn't yet support " + methodName );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Type Conversion Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Converts the given FederateHandleSet to a pure java HashSet<Integer>(). The given FHS must
	 * be a Portico implementation ({@link HLA13Set}) or else an exception will be thrown. If the
	 * given set is null, null will be returned.
	 */
	private HashSet<Integer> convertSet( FederateHandleSet fhs ) throws RTIinternalError
	{
		// check the set and put the appropriate value in the request
		// if the set is empty/null, it is the same as a request for a federation wide sync point
		if( fhs != null )
		{
			// we have a set, process it
			if( fhs instanceof HLA13Set )
			{
				// it is one of our sets, pull the required info out of it
				return ((HLA13Set)fhs).toJavaSet();
			}
			else
			{
				// the set isn't one of our types
				throw new RTIinternalError( "Portico only supports its own set implementations" );
			}
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Converts the given AttributeHandleSet to a pure java HashSet<Integer>(). The given AHS must
	 * be a Portico implementation ({@link HLA13Set}) or an exceptoin will be thrown. If the given
	 * set is null, null will be returned.
	 */
	private HashSet<Integer> convertSet( AttributeHandleSet ahs ) throws RTIinternalError
	{
		// check the set and put the appropriate value in the request
		if( ahs != null )
		{
			// we have a set, process it
			if( ahs instanceof HLA13Set )
			{
				// it is one of our sets, pull the required info out of it
				return ((HLA13Set)ahs).toJavaSet();
			}
			else
			{
				// the set isn't one of our types
				throw new RTIinternalError( "Portico only supports its own set implementations" );
			}
		}
		else
		{
			return null;
		}
	}

	/**
	 * Converts the SuppliedAttributes to a Portico type. If there is a problem in the conversion,
	 * or the given parameter is null, an exception is thrown.
	 */
	private HLA13ByteArrayMap convertAttributes( SuppliedAttributes attributes )
		throws RTIinternalError
	{
		if( attributes == null )
			throw new RTIinternalError( "SuppliedAttributes were null" );
		
		try
		{
			return (HLA13ByteArrayMap)attributes;
		}
		catch( ClassCastException cce )
		{
			throw new RTIinternalError( "SuppliedAttributes was not a Portico implementation: " +
			                            attributes.getClass(), cce );
		}
	}

	/**
	 * Converts the SuppliedAttributes to a Portico type. If there is a problem in the conversion,
	 * or the given parameter is null, an exception is thrown.
	 */
	private HLA13ByteArrayMap convertParameters( SuppliedParameters parameters )
		throws RTIinternalError
	{
		if( parameters == null )
			throw new RTIinternalError( "SuppliedParameters were null" );
		
		try
		{
			return (HLA13ByteArrayMap)parameters;
		}
		catch( ClassCastException cce )
		{
			throw new RTIinternalError( "SuppliedParameters was not a Portico implementation: " +
			                            parameters.getClass(), cce );
		}
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
			return ((HLA13Region)region).getWrappedRegion();
		}
		catch( ClassCastException cce )
		{
			throw new RegionNotKnown( "Supplied region was not a Portico implementation: " +
			                          region.getClass(), cce );
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
				tokenArray[i] = ((HLA13Region)current).getWrappedRegion().getToken();
			}
			catch( ClassCastException cce )
			{
				throw new RegionNotKnown( "Supplied region was not a Portico implementation: " +
				                          current.getClass(), cce );
			}
		}
		
		return tokenArray;
	}
	
	/**
	 * Converts a LogicalTime to a PorticoTime and extracts the double value of the time from
	 * it, throwing an exception if there is a problem. If the given parameter is null (which is
	 * valid in many situations), {@link PorticoConstants#NULL_TIME} is returned.
	 */
	private double convertTime( LogicalTime time ) throws InvalidFederationTime
	{
		if( time == null )
			return PorticoConstants.NULL_TIME;
		
		try
		{
			return ((DoubleTime)time).getTime();
		}
		catch( ClassCastException cce )
		{
			throw new InvalidFederationTime( "Supplied LogicalTime was not a Portico version: " +
			                                 time.getClass(), cce );
		}
	}

	/**
	 * Converts the interval into the double value held by it. It will first convert the interval
	 * to the Portico type before extracting the double value. If the value is null, an exception
	 * will be thrown (this is unliked {@link #convertTime(LogicalTime)}). If the value isn't a
	 * Portico implementation, an exception will be thrown.
	 */
	private double convertLookahead( LogicalTimeInterval interval ) throws InvalidLookahead
	{
		if( interval == null )
			throw new InvalidLookahead( "Given Lookahead was null" );
		
		try
		{
			return ((DoubleTimeInterval)interval).getInterval();
		}
		catch( ClassCastException cce )
		{
			throw new InvalidLookahead( "Supplied LogicalTimeInterval was not a Portico version: " +
			                            interval.getClass(), cce );
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
