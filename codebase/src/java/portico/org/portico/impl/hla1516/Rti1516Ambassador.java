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
package org.portico.impl.hla1516;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.hla1516.types.*;
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
import org.portico.lrc.compat.JFederatesCurrentlyJoined;
import org.portico.lrc.compat.JFederationExecutionAlreadyExists;
import org.portico.lrc.compat.JFederationExecutionDoesNotExist;
import org.portico.lrc.compat.JFederationTimeAlreadyPassed;
import org.portico.lrc.compat.JInteractionClassNotDefined;
import org.portico.lrc.compat.JInteractionClassNotPublished;
import org.portico.lrc.compat.JInteractionClassNotSubscribed;
import org.portico.lrc.compat.JInteractionParameterNotDefined;
import org.portico.lrc.compat.JInvalidFederationTime;
import org.portico.lrc.compat.JInvalidLookahead;
import org.portico.lrc.compat.JInvalidResignAction;
import org.portico.lrc.compat.JObjectAlreadyRegistered;
import org.portico.lrc.compat.JObjectClassNotDefined;
import org.portico.lrc.compat.JObjectClassNotPublished;
import org.portico.lrc.compat.JObjectClassNotSubscribed;
import org.portico.lrc.compat.JObjectNotKnown;
import org.portico.lrc.compat.JOwnershipAcquisitionPending;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.compat.JResignAction;
import org.portico.lrc.compat.JRestoreInProgress;
import org.portico.lrc.compat.JSaveInProgress;
import org.portico.lrc.compat.JSynchronizationLabelNotAnnounced;
import org.portico.lrc.compat.JTimeAdvanceAlreadyInProgress;
import org.portico.lrc.compat.JTimeConstrainedAlreadyEnabled;
import org.portico.lrc.compat.JTimeConstrainedWasNotEnabled;
import org.portico.lrc.compat.JTimeRegulationAlreadyEnabled;
import org.portico.lrc.compat.JTimeRegulationWasNotEnabled;
import org.portico.lrc.model.ACInstance;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCInstance;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.lrc.services.ownership.msg.AttributeRelease;
import org.portico.lrc.services.ownership.msg.CancelAcquire;
import org.portico.lrc.services.ownership.msg.CancelDivest;
import org.portico.lrc.services.ownership.msg.QueryOwnership;
import org.portico.utils.messaging.PorticoMessage;

import org.portico.utils.messaging.ErrorResponse;
import org.portico.utils.messaging.ExtendedSuccessMessage;
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
import org.portico2.common.services.time.msg.QueryGalt;
import org.portico2.common.services.time.msg.TimeAdvanceRequest;
import org.portico.utils.messaging.MessageContext;

import hla.rti1516.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is the Portico implementation of the HLA 1516 RTIambassador class.
 */
public class Rti1516Ambassador implements RTIambassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl1516Helper helper;
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	public Rti1516Ambassador() throws RTIinternalError
	{
		this.helper = new Impl1516Helper();
		this.logger = this.helper.getLrcLogger();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	public Impl1516Helper getHelper()
	{
		return this.helper;
	}

	////////////////////////////////////
	// Federation Management Services //
	////////////////////////////////////
	// 4.2
	public void createFederationExecution( String executionName, java.net.URL fdd )
	    throws FederationExecutionAlreadyExists, CouldNotOpenFDD, ErrorReadingFDD, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// attempt to parse the FOM //
		// we do this here, because we need to use a HLA1.3 specific FOM parser class
		CreateFederation request = new CreateFederation( executionName, fdd );
		request.setHlaVersion( HLAVersion.IEEE1516 );
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
				throw new CouldNotOpenFDD( theException );
			}
			else if( theException instanceof JErrorReadingFED )
			{
				throw new ErrorReadingFDD( theException );
			}
			else
			{
				logException( "createFederationExecution", theException );
			}
		}
	}

	// 4.3
	public void destroyFederationExecution( String executionName )
	    throws FederatesCurrentlyJoined, FederationExecutionDoesNotExist, RTIinternalError
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
			else
			{
				logException( "destroyFederationExecution", theException );
			}
		}
	}

	// 4.4
	public FederateHandle joinFederationExecution( String federateName,
	                                               String federationName,
	                                               FederateAmbassador federateAmbassador,
	                                               MobileFederateServices serviceReferences )
	    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist, SaveInProgress,
	    RestoreInProgress, RTIinternalError
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
			return new HLA1516FederateHandle( (Integer)success.getResult() );
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
			else
			{
				logException( "joinFederationExecution", theException );
				return null;
			}
		}
	}

	// 4.5
	public void resignFederationExecution( ResignAction resignAction )
	    throws OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember,
	    RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		ResignFederation request;
		try
		{
			// FIX: PORT-553
			if( resignAction == null )
				throw new RTIinternalError( "Null resign action" );
			
			int iValue = resignAction.hashCode();
			request = new ResignFederation( JResignAction.for1516Value(iValue) );
		}
		catch( JInvalidResignAction e )
		{
			throw new RTIinternalError( e );
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
			else if( theException instanceof JInvalidResignAction )
			{
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "resignFederationExecution", theException );
			}
		}
	}

	// 4.6
	public void registerFederationSynchronizationPoint( String label, byte[] userSuppliedTag )
	    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterSyncPoint request = new RegisterSyncPoint( label, userSuppliedTag );
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
			else
			{
				logException( "registerFederationSynchronizationPoint", theException );
			}
		}
	}

	public void registerFederationSynchronizationPoint( String label,
	                                                    byte[] tag,
	                                                    FederateHandleSet synchronizationSet )
	    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// convert set into appropriate form
		HashSet<Integer> set = null;
		if( synchronizationSet != null )
			set = HLA1516FederateHandleSet.toJavaSet( synchronizationSet );
		RegisterSyncPoint request = new RegisterSyncPoint( label, tag, set );
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
			else
			{
				logException( "registerFederationSynchronizationPoint", theException );
			}
		}
	}

	// 4.9
	public void synchronizationPointAchieved( String synchronizationPointLabel )
	    throws SynchronizationPointLabelNotAnnounced, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SyncPointAchieved request = new SyncPointAchieved( synchronizationPointLabel );
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
				throw new SynchronizationPointLabelNotAnnounced( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else
			{
				logException( "synchronizationPointAchieved", theException );
			}
		}
	}

	// 4.11
	public void requestFederationSave( String label ) throws FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "requestFederationSave()" );
	}

	public void requestFederationSave( String label, LogicalTime theTime )
	    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, FederateUnableToUseTime,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "requestFederationSave(time)" );
	}

	// 4.13
	public void federateSaveBegun() throws SaveNotInitiated, FederateNotExecutionMember,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "federateSaveBegun()" );
	}

	// 4.14
	public void federateSaveComplete() throws FederateHasNotBegunSave, FederateNotExecutionMember,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "federateSaveComplete()" );
	}

	public void federateSaveNotComplete() throws FederateHasNotBegunSave,
	    FederateNotExecutionMember, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "federateSaveNotComplete()" );
	}

	// 4.16
	public void queryFederationSaveStatus() throws FederateNotExecutionMember, RestoreInProgress,
	    RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 4.18
	public void requestFederationRestore( String label ) throws FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "requestFederationRestore()" );
	}

	// 4.22
	public void federateRestoreComplete() throws RestoreNotRequested, FederateNotExecutionMember,
	    SaveInProgress, RTIinternalError
	{
		featureNotSupported( "federateRestoreComplete()" );
	}

	public void federateRestoreNotComplete() throws RestoreNotRequested,
	    FederateNotExecutionMember, SaveInProgress, RTIinternalError
	{
		featureNotSupported( "federateRestoreNotComplete()" );
	}

	//4.24 
	public void queryFederationRestoreStatus() throws FederateNotExecutionMember, SaveInProgress,
	    RTIinternalError
	{
		featureNotSupported( "queryFederationRestoreStatus()" );
	}

	/////////////////////////////////////
	// Declaration Management Services //
	//////////////////////////////////// /
	// 5.2
	public void publishObjectClassAttributes( ObjectClassHandle theClass,
	                                          AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// check the handle set //
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( attributeList );
		int handle = HLA1516Handle.fromHandle( theClass );
		PublishObjectClass request = new PublishObjectClass( handle, set );
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
			else
			{
				logException( "publishObjectClass", theException );
			}
		}
	}

	// 5.3
	public void unpublishObjectClass( ObjectClassHandle theClass ) throws ObjectClassNotDefined,
	    OwnershipAcquisitionPending, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
	    RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnpublishObjectClass request = new UnpublishObjectClass(HLA1516Handle.fromHandle(theClass));
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
			else if( theException instanceof JObjectClassNotPublished )
			{
				// ignore! it's the 1516 spec way :(
			}
			else
			{
				logException( "unpublishObjectClass", theException );
			}
		}
	}

	public void unpublishObjectClassAttributes( ObjectClassHandle theClass,
	                                            AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, OwnershipAcquisitionPending,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int cHandle = HLA1516Handle.fromHandle( theClass );
		HashSet<Integer> attributes = HLA1516AttributeHandleSet.toJavaSet( attributeList );
		UnpublishObjectClass request = new UnpublishObjectClass( cHandle, attributes );
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
			else if( theException instanceof JOwnershipAcquisitionPending )
			{
				throw new OwnershipAcquisitionPending( theException );
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
			else if( theException instanceof JObjectClassNotPublished )
			{
				// ignore! it's the 1516 spec way :(
			}
			else
			{
				logException( "unpublishObjectClass", theException );
			}
		}
	}

	// 5.4
	public void publishInteractionClass( InteractionClassHandle theInteraction )
	    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		PublishInteractionClass request =
			new PublishInteractionClass( HLA1516Handle.fromHandle(theInteraction) );
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
			else
			{
				logException( "publishInteractionClass", theException );
			}
		}
	}

	// 5.5
	public void unpublishInteractionClass( InteractionClassHandle theInteraction )
	    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnpublishInteractionClass request =
			new UnpublishInteractionClass( HLA1516Handle.fromHandle(theInteraction) );
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
			else if( theException instanceof JInteractionClassNotPublished )
			{
				// ignore! it's the 1516 spec way :(
			}
			else
			{
				logException( "unpublishInteractionClass", theException );
			}
		}
	}

	// 5.6
	public void subscribeObjectClassAttributes( ObjectClassHandle theClass,
	                                            AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( attributeList );
		int handle = HLA1516Handle.fromHandle( theClass );
		SubscribeObjectClass request = new SubscribeObjectClass( handle, set, true );
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
			else if( theException instanceof JObjectClassNotSubscribed )
			{
				// ignore, for that is the 1516 way
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else
			{
				logException( "subscribeObjectClassAttributes", theException );
			}
		}
	}

	public void subscribeObjectClassAttributesPassively( ObjectClassHandle theClass,
	                                                     AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesPassively()" );
	}

	// 5.7
	public void unsubscribeObjectClass( ObjectClassHandle theClass ) throws ObjectClassNotDefined,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnsubscribeObjectClass request =
			new UnsubscribeObjectClass( HLA1516Handle.fromHandle(theClass) );
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
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JObjectClassNotSubscribed )
			{
				// ignore, for that is the 1516 way
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else
			{
				logException( "unsucbscirbeObjectClass", theException );
			}
		}
	}

	public void unsubscribeObjectClassAttributes( ObjectClassHandle theClass,
	                                              AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int cHandle = HLA1516Handle.fromHandle( theClass );
		HashSet<Integer> attributes = HLA1516AttributeHandleSet.toJavaSet( attributeList );
		UnsubscribeObjectClass request = new UnsubscribeObjectClass( cHandle, attributes );
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
			else if( theException instanceof JFederateNotExecutionMember )
			{
				throw new FederateNotExecutionMember( theException );
			}
			else if( theException instanceof JObjectClassNotSubscribed )
			{
				// ignore, for that is the 1516 way
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else if( theException instanceof JObjectClassNotSubscribed )
			{
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "unsucbscirbeObjectClass", theException );
			}
		}
	}

	// 5.8
	public void subscribeInteractionClass( InteractionClassHandle theClass )
	    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SubscribeInteractionClass request =
			new SubscribeInteractionClass( HLA1516Handle.fromHandle(theClass) );
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
			else if( theException instanceof JInteractionClassNotSubscribed )
			{
				// ignore, for that is the 1516 way
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else
			{
				logException( "subscribeInteractionClass", theException );
			}
		}
	}

	public void subscribeInteractionClassPassively( InteractionClassHandle theClass )
	    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "subscribeInteractionClassPassively()" );
	}

	// 5.9
	public void unsubscribeInteractionClass( InteractionClassHandle theClass )
	    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError 
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnsubscribeInteractionClass request =
			new UnsubscribeInteractionClass( HLA1516Handle.fromHandle(theClass) );
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
			else if( theException instanceof JInteractionClassNotSubscribed )
			{
				// ignore, for that is the 1516 way
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else
			{
				logException( "unsubscribeInteractionClass", theException );
			}
		}
	}

	////////////////////////////////
	// Object Management Services //
	////////////////////////////////
	// 6.2
	public void reserveObjectInstanceName( String theObjectName ) throws IllegalName,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "reserveObjectInstanceName()" );
	}

	// 6.4
	public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass )
	    throws ObjectClassNotDefined, ObjectClassNotPublished, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterObject request = new RegisterObject( HLA1516Handle.fromHandle(theClass) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			ExtendedSuccessMessage success = (ExtendedSuccessMessage)response;
			OCInstance instance = (OCInstance)success.getResult();
			return new HLA1516ObjectInstanceHandle( instance.getHandle() );
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
			else
			{
				logException( "registerObjectInstance", theException );
				return null;
			}
		}
	}

	public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass,
	                                                    String theObjectName )
	    throws ObjectClassNotDefined, ObjectClassNotPublished, ObjectInstanceNameNotReserved,
	    ObjectInstanceNameInUse, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
	    RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterObject request =
			new RegisterObject( HLA1516Handle.fromHandle(theClass), theObjectName );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			ExtendedSuccessMessage success = (ExtendedSuccessMessage)response;
			OCInstance instance = (OCInstance)success.getResult();
			return new HLA1516ObjectInstanceHandle( instance.getHandle() );
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
				throw new ObjectInstanceNameInUse( theException );
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
			else
			{
				logException( "registerObjectInstance", theException );
				return null;
			}
		}
	}

	// 6.6
	public void updateAttributeValues( ObjectInstanceHandle theObject,
	                                   AttributeHandleValueMap theAttributes,
	                                   byte[] tag )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashMap<Integer,byte[]> map = HLA1516AttributeHandleValueMap.toJavaMap( theAttributes );
		int objectId = HLA1516Handle.fromHandle( theObject );
		UpdateAttributes request = new UpdateAttributes( objectId, tag, map );
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
				throw new ObjectInstanceNotKnown( theException );
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
			else
			{
				logException( "updateAttributeValues", theException );
			}
		}
	}

	public MessageRetractionReturn updateAttributeValues( ObjectInstanceHandle theObject,
	                                                      AttributeHandleValueMap theAttributes,
	                                                      byte[] tag,
	                                                      LogicalTime theTime )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidLogicalTime,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double doubleTime = PorticoConstants.NULL_TIME;
		if( theTime != null )
			doubleTime = DoubleTime.fromTime( theTime );

		HashMap<Integer,byte[]> map = HLA1516AttributeHandleValueMap.toJavaMap( theAttributes );
		int oHandle = HLA1516Handle.fromHandle( theObject );
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UpdateAttributes request = new UpdateAttributes( oHandle, tag, map, doubleTime );
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
				throw new ObjectInstanceNotKnown( theException );
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
				throw new InvalidLogicalTime( theException );
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
			else
			{
				logException( "updateAttributeValues(LogicalTime)", theException );
				throw new RTIinternalError( theException );
			}
		}
	}

	// 6.8
	public void sendInteraction( InteractionClassHandle theInteraction,
	                             ParameterHandleValueMap theParameters, byte[] tag )
	    throws InteractionClassNotPublished, InteractionClassNotDefined,
	    InteractionParameterNotDefined, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashMap<Integer,byte[]> map = HLA1516ParameterHandleValueMap.toJavaMap( theParameters ); 
		int interactionId = HLA1516Handle.fromHandle( theInteraction );
		SendInteraction request = new SendInteraction( interactionId, tag, map );
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
			else
			{
				logException( "sendInteraction", theException );
			}
		}
	}

	public MessageRetractionReturn sendInteraction( InteractionClassHandle theInteraction,
	                                                ParameterHandleValueMap theParameters,
	                                                byte[] tag, LogicalTime theTime )
	    throws InteractionClassNotPublished, InteractionClassNotDefined,
	    InteractionParameterNotDefined, InvalidLogicalTime, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double doubleTime = PorticoConstants.NULL_TIME;
		if( theTime != null )
			doubleTime = DoubleTime.fromTime( theTime );
		HashMap<Integer,byte[]> map = HLA1516ParameterHandleValueMap.toJavaMap( theParameters );
		int iHandle = HLA1516Handle.fromHandle( theInteraction );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SendInteraction request = new SendInteraction( iHandle, tag, map, doubleTime );
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
				throw new InvalidLogicalTime( theException );
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
			else
			{
				logException( "sentInteraction(LogicalTime)", theException );
				throw new RTIinternalError( theException );
			}
		}
	}

	// 6.10
	public void deleteObjectInstance( ObjectInstanceHandle objectHandle, byte[] userSuppliedTag )
	    throws DeletePrivilegeNotHeld, ObjectInstanceNotKnown, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DeleteObject request =
			new DeleteObject( HLA1516Handle.fromHandle(objectHandle), userSuppliedTag );
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
				throw new ObjectInstanceNotKnown( theException );
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
			else
			{
				logException( "deleteObjectInstance", theException );
			}
		}
	}

	public MessageRetractionReturn deleteObjectInstance( ObjectInstanceHandle objectHandle,
	                                                     byte[] userSuppliedTag,
	                                                     LogicalTime theTime )
	    throws DeletePrivilegeNotHeld, ObjectInstanceNotKnown, InvalidLogicalTime,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = PorticoConstants.NULL_TIME;
		if( theTime != null )
			time = DoubleTime.fromTime( theTime );
		int oHandle = HLA1516Handle.fromHandle( objectHandle );
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DeleteObject request = new DeleteObject( oHandle, userSuppliedTag, time );
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
				throw new ObjectInstanceNotKnown( theException );
			}
			else if( theException instanceof JDeletePrivilegeNotHeld )
			{
				throw new DeletePrivilegeNotHeld( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new InvalidLogicalTime( theException );
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
			else
			{
				logException( "deleteObjectInstance", theException );
				return null;
			}
		}
	}

	// 6.12
	public void localDeleteObjectInstance( ObjectInstanceHandle objectHandle )
	    throws ObjectInstanceNotKnown, FederateOwnsAttributes, OwnershipAcquisitionPending,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		LocalDelete request = new LocalDelete( HLA1516Handle.fromHandle(objectHandle) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
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
				throw new ObjectInstanceNotKnown( theException );
			}
			else if( theException instanceof JFederateOwnsAttributes )
			{
				throw new FederateOwnsAttributes( theException );
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
			else
			{
				logException( "localDeleteObjectInstance", theException );
			}
		}
	}

	// 6.13
	public void changeAttributeTransportationType( ObjectInstanceHandle theObject,
	                                               AttributeHandleSet theAttributes,
	                                               TransportationType theType )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "changeAttributeTransportationType()" );
	}

	// 6.14
	public void changeInteractionTransportationType( InteractionClassHandle theClass,
	                                                 TransportationType theType )
	    throws InteractionClassNotDefined, InteractionClassNotPublished,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "changeInteractionTransportationType()" );
	}

	// 6.17 
	public void requestAttributeValueUpdate( ObjectInstanceHandle theObject,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] tag )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( theAttributes );
		int oHandle = HLA1516Handle.fromHandle( theObject );
		RequestObjectUpdate request = new RequestObjectUpdate( oHandle, set, tag );
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
				throw new ObjectInstanceNotKnown( theException );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "requestAttributeValueUpdate", theException );
			}
		}
	}

	public void requestAttributeValueUpdate( ObjectClassHandle theClass,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] tag ) throws ObjectClassNotDefined,
	    AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
	    RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( theAttributes );
		int cHandle = HLA1516Handle.fromHandle( theClass );
		RequestClassUpdate request = new RequestClassUpdate( cHandle, set, tag );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "requestAttributeValueUpdate(class)", theException );
			}
		}
	}

	///////////////////////////////////
	// Ownership Management Services //
	///////////////////////////////////
	// 7.2
	public void unconditionalAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
	                                                        AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516Handle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( theAttributes );

		AttributeDivest request = new AttributeDivest( oHandle, set, true );
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
				throw new ObjectInstanceNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotOwned )
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "unconditionalAttributeOwnershipDivestiture", theException );
			}
		}
	}

	// 7.3
	public void negotiatedAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
	                                                     AttributeHandleSet theAttributes,
	                                                     byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    AttributeAlreadyBeingDivested, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516Handle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( theAttributes );

		AttributeDivest request = new AttributeDivest( oHandle, set, userSuppliedTag );
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
				throw new ObjectInstanceNotKnown( theException );
			}
			else if( theException instanceof JAttributeNotDefined )
			{
				throw new AttributeNotDefined( theException );
			}
			else if( theException instanceof JAttributeNotOwned )
			{
				throw new AttributeNotDefined( theException );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "negotiatedAttributeOwnershipDivestiture", theException );
			}
		}
	}

	// 7.6
	public void confirmDivestiture( ObjectInstanceHandle theObject,
	                                AttributeHandleSet theAttributes,
	                                byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    AttributeDivestitureWasNotRequested, NoAcquisitionPending, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "confirmDivestiture()" );
	}

	// 7.8
	public void attributeOwnershipAcquisition( ObjectInstanceHandle theObject,
	                                           AttributeHandleSet desiredAttributes,
	                                           byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
	    AttributeNotPublished, FederateOwnsAttributes, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516Handle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( desiredAttributes );

		AttributeAcquire request = new AttributeAcquire( oHandle, set, userSuppliedTag );
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
				throw new ObjectInstanceNotKnown( theException );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "attributeOwnershipAcquisition", theException );
			}
		}
	}

	// 7.9
	public void attributeOwnershipAcquisitionIfAvailable( ObjectInstanceHandle theObject,
	                                                      AttributeHandleSet desiredAttributes )
	    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
	    AttributeNotPublished, FederateOwnsAttributes, AttributeAlreadyBeingAcquired,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516Handle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( desiredAttributes );

		AttributeAcquire request = new AttributeAcquire( oHandle, set, true );
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
				throw new ObjectInstanceNotKnown( theException );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "attributeOwnershipAcquisitionIfAvailable", theException );
			}
		}
	}

	// 7.12
	@SuppressWarnings("unchecked")
	public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
	                                                             ObjectInstanceHandle theObject,
	                                                             AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		// TODO NOTE This is just used the same as a release response in 1.3 at the moment.
		//           However, in 1516 this doesn't have to be used in response to a release
		//           request, it can be used any time any federate wants
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516Handle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( theAttributes );

		AttributeRelease request = new AttributeRelease( oHandle, set );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return new HLA1516AttributeHandleSet( (Set<Integer>)response.getResult() );
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
				throw new ObjectInstanceNotKnown( theException );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "attributeOwnershipDivestitureIfWanted", theException );
			}
		}
		
		featureNotSupported( "attributeOwnershipDivestitureIfWanted()" );
		return null; // keep the compiler happy
	}

	// 7.13
	public void cancelNegotiatedAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
	                                                           AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    AttributeDivestitureWasNotRequested, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516Handle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( theAttributes );

		CancelDivest request = new CancelDivest( oHandle, set );
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
				throw new ObjectInstanceNotKnown( theException );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "cancelNegotiatedAttributeOwnershipDivestiture", theException );
			}
		}
	}

	// 7.14
	public void cancelAttributeOwnershipAcquisition( ObjectInstanceHandle theObject,
	                                                 AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
	    AttributeAcquisitionWasNotRequested, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516Handle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516AttributeHandleSet.toJavaSet( theAttributes );

		CancelAcquire request = new CancelAcquire( oHandle, set );
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
				throw new ObjectInstanceNotKnown( theException );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "cancelAttributeOwnershipAcquisition", theException );
			}
		}
	}

	// 7.16
	public void queryAttributeOwnership( ObjectInstanceHandle theObject,
	                                     AttributeHandle theAttribute )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516Handle.fromHandle( theObject );
		int aHandle = HLA1516Handle.fromHandle( theAttribute );

		QueryOwnership request = new QueryOwnership( oHandle, aHandle );
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
				throw new ObjectInstanceNotKnown( theException );
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
				throw new RTIinternalError( theException );
			}
			else
			{
				logException( "queryAttributeOwnership", theException );
			}
		}
	}

	//	 7.18 
	public boolean isAttributeOwnedByFederate( ObjectInstanceHandle theObject,
	                                           AttributeHandle theAttribute )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		helper.checkJoined();
		
		int oHandle = HLA1516Handle.validatedHandle( theObject );		
		OCInstance instance = helper.getState().getRepository().getInstance( oHandle );
		if( instance == null )
		{
			throw new ObjectInstanceNotKnown( "handle: " + oHandle );
		}
		else
		{
			// convert the handle into an ACInstance
			ACInstance attribute = null;
			try
			{
				int aHandle = HLA1516Handle.validatedHandle( theAttribute );
				attribute = instance.getAttribute( aHandle );
				if( attribute == null )
					throw new AttributeNotDefined( "handle: " + aHandle );
			}
			catch( InvalidAttributeHandle iah )
			{
				throw new AttributeNotDefined( "handle: (is invalid) " + theAttribute );
			}

			// check to see if we are the owner
			return attribute.getOwner() == helper.getState().getFederateHandle();
		}
	}

	//////////////////////////////
	// Time Management Services // 
	//////////////////////////////	 
	//	 8.2 
	public void enableTimeRegulation( LogicalTimeInterval theLookahead )
	    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState,
	    RequestForTimeRegulationPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double la = DoubleTimeInterval.fromLookahead( theLookahead );
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		EnableTimeRegulation request = new EnableTimeRegulation( 0.0, la );
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
				throw new RequestForTimeRegulationPending( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new InTimeAdvancingState( theException );
			}
			else if( theException instanceof JInvalidFederationTime )
			{
				throw new RTIinternalError( theException );
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
			else
			{
				logException( "enableTimeRegulation", theException );
			}
		}
	}

	//	 8.4 
	public void disableTimeRegulation() throws TimeRegulationIsNotEnabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
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
				throw new TimeRegulationIsNotEnabled( theException );
			}
			else if( theException instanceof JSaveInProgress )
			{
				throw new SaveInProgress( theException );
			}
			else if( theException instanceof JRestoreInProgress )
			{
				throw new RestoreInProgress( theException );
			}
			else
			{
				logException( "disableTimeRegulation", theException );
			}
		}
	}

	//	 8.5 
	public void enableTimeConstrained() throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
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
				throw new RequestForTimeConstrainedPending( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new InTimeAdvancingState( theException );
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
			else
			{
				logException( "enbaleTimeConstrained", theException );
			}
		}
	}

	//	 8.7 
	public void disableTimeConstrained() throws TimeConstrainedIsNotEnabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
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
				throw new TimeConstrainedIsNotEnabled( theException );
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
			else
			{
				logException( "disableTimeConstrained", theException );
			}
		}
	}

	//	 8.8 
	public void timeAdvanceRequest( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = DoubleTime.fromTime( theTime ); // also checks for null
		
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
				throw new InvalidLogicalTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new LogicalTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new InTimeAdvancingState( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new RequestForTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new RequestForTimeConstrainedPending( theException );
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
			else
			{
				logException( "timeAdvanceRequest", theException );
			}
		}
	}

	//	 8.9 
	public void timeAdvanceRequestAvailable( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		//XXX: NOTE!! This is just the same as a regular time advance request at this point in time
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = DoubleTime.fromTime( theTime ); // also checks for null
		
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
				throw new InvalidLogicalTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new LogicalTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new InTimeAdvancingState( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new RequestForTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new RequestForTimeConstrainedPending( theException );
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
			else
			{
				logException( "timeAdvanceRequestAvailable", theException );
			}
		}
	}

	//	 8.10 
	public void nextMessageRequest( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = DoubleTime.fromTime( theTime );
		
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
				throw new InvalidLogicalTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new LogicalTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new InTimeAdvancingState( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new RequestForTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new RequestForTimeConstrainedPending( theException );
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
			else
			{
				logException( "nextMessageRequest", theException );
			}
		}
	}

	//	 8.11 
	public void nextMessageRequestAvailable( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = DoubleTime.fromTime( theTime );
		
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
				throw new InvalidLogicalTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new LogicalTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new InTimeAdvancingState( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new RequestForTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new RequestForTimeConstrainedPending( theException );
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
			else
			{
				logException( "nextMessageRequestAvailable", theException );
			}
		}
	}

	//	 8.12 
	public void flushQueueRequest( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = DoubleTime.fromTime( theTime );
		
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
				throw new InvalidLogicalTime( theException );
			}
			else if( theException instanceof JFederationTimeAlreadyPassed )
			{
				throw new LogicalTimeAlreadyPassed( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new InTimeAdvancingState( theException );
			}
			else if( theException instanceof JEnableTimeRegulationPending )
			{
				throw new RequestForTimeRegulationPending( theException );
			}
			else if( theException instanceof JEnableTimeConstrainedPending )
			{
				throw new RequestForTimeConstrainedPending( theException );
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
			else
			{
				logException( "flushQueueRequest", theException );
			}
		}
	}

	//	 8.14 
	public void enableAsynchronousDelivery() throws AsynchronousDeliveryAlreadyEnabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
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
			else
			{
				logException( "enableAsynchronousDelivery", theException );
			}
		}
	}

	//	 8.15 
	public void disableAsynchronousDelivery() throws AsynchronousDeliveryAlreadyDisabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
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
			else
			{
				logException( "disableAsynchronousDelivery", theException );
			}
		}
	}

	//	 8.16 
	public TimeQueryReturn queryGALT() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		QueryGalt request = new QueryGalt();
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// request was fine
			DoubleTime time = new DoubleTime( (Integer)response.getResult() );
			return new TimeQueryReturn( true, time );
		}
		else
		{
			// an exception was caused :(
			Throwable theException = ((ErrorResponse)response).getCause();

			if( theException instanceof JRTIinternalError )
			{
				throw new RTIinternalError( theException );
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
			else
			{
				logException( "queryGALT", theException );
				return null; // will never get to here
			}
		}
	}

	//	 8.17 
	public LogicalTime queryLogicalTime() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		return new DoubleTime( helper.getState().getCurrentTime() );
	}

	//	 8.18 
	public TimeQueryReturn queryLITS() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		DoubleTime time = new DoubleTime( helper.getState().getCurrentTime() );
		return new TimeQueryReturn( true, time );
	}

	//	 8.19 
	public void modifyLookahead( LogicalTimeInterval theLookahead )
	    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = 0.0;
		try
		{
			time = DoubleTimeInterval.fromInterval( theLookahead );
		}
		catch( Exception e )
		{
			throw new InvalidLookahead( "Error converting lookahead: " + e.getMessage(), e );
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
			else if( theException instanceof JTimeRegulationWasNotEnabled )
			{
				throw new TimeRegulationIsNotEnabled( theException );
			}
			else if( theException instanceof JTimeAdvanceAlreadyInProgress )
			{
				throw new InTimeAdvancingState( theException );
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
			else
			{
				logException( "modifyLookahead", theException );
			}
		}
	}

	//	 8.20 
	public LogicalTimeInterval queryLookahead() throws TimeRegulationIsNotEnabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		return new DoubleTimeInterval( helper.getState().getLookahead() );
	}

	//	 8.21 
	public void retract( MessageRetractionHandle theHandle ) throws InvalidMessageRetractionHandle,
	    TimeRegulationIsNotEnabled, MessageCanNoLongerBeRetracted, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "retract()" );
	}

	//	 8.23 
	public void changeAttributeOrderType( ObjectInstanceHandle theObject,
	                                      AttributeHandleSet theAttributes, OrderType theType )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "changeAttributeOrderType()" );
	}

	//	 8.24 
	public void changeInteractionOrderType( InteractionClassHandle theClass, OrderType theType )
	    throws InteractionClassNotDefined, InteractionClassNotPublished,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "changeInteractionOrderType()" );
	}

	//////////////////////////////////
	// Data Distribution Management //
	//////////////////////////////////
	// 9.2
	public RegionHandle createRegion( DimensionHandleSet dimensions )
	    throws InvalidDimensionHandle, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "createRegion()" );
		return null; // keep the compiler happy
	}

	// 9.3
	public void commitRegionModifications( RegionHandleSet regions ) throws InvalidRegion,
	    RegionNotCreatedByThisFederate, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "commitRegionModifications()" );
	}

	// 9.4
	public void deleteRegion( RegionHandle theRegion ) throws InvalidRegion,
	    RegionNotCreatedByThisFederate, RegionInUseForUpdateOrSubscription,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "deleteRegion()" );
	}

	// 9.5
	public ObjectInstanceHandle registerObjectInstanceWithRegions(
	                                          ObjectClassHandle theClass,
	                                          AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
	    AttributeNotPublished, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "registerObjectInstanceWithRegions()" );
		return null; // keep the compiler happy
	}

	public ObjectInstanceHandle registerObjectInstanceWithRegions(
	                                          ObjectClassHandle theClass,
	                                          AttributeSetRegionSetPairList attributesAndRegions,
	                                          String theObject )
	    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
	    AttributeNotPublished, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
	    ObjectInstanceNameNotReserved, ObjectInstanceNameInUse, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "registerObjectInstanceWithRegions(name)" );
		return null; // keep the compiler happy
	}

	// 9.6
	public void associateRegionsForUpdates( ObjectInstanceHandle theObject,
	                                        AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "associateRegionsForUpdates()" );
	}

	// 9.7
	public void unassociateRegionsForUpdates( ObjectInstanceHandle theObject,
	                                          AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "unassociateRegionsForUpdates()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesWithRegions(
	                                    ObjectClassHandle theClass,
	                                    AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesWithRegions()" );
	}

	public void subscribeObjectClassAttributesPassivelyWithRegions(
	                                    ObjectClassHandle theClass,
	                                    AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesPassivelyWithRegions()" );
	}

	// 9.9
	public void unsubscribeObjectClassAttributesWithRegions(
	                                    ObjectClassHandle theClass,
	                                    AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "unsubscribeObjectClassAttributesWithRegions()" );
	}

	// 9.10
	public void subscribeInteractionClassWithRegions( InteractionClassHandle theClass,
	                                                  RegionHandleSet regions )
	    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    InvalidRegionContext, FederateServiceInvocationsAreBeingReportedViaMOM,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "subscribeInteractionClassWithRegions()" );
	}

	public void subscribeInteractionClassPassivelyWithRegions( InteractionClassHandle theClass,
	                                                           RegionHandleSet regions )
	    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    InvalidRegionContext, FederateServiceInvocationsAreBeingReportedViaMOM,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "subscribeInteractionClassPassivelyWithRegions()" );
	}

	// 9.11
	public void unsubscribeInteractionClassWithRegions( InteractionClassHandle theClass,
	                                                    RegionHandleSet regions )
	    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "unsubscribeInteractionClassWithRegions()" );
	}

	// 9.12
	public void sendInteractionWithRegions( InteractionClassHandle theInteraction,
	                                        ParameterHandleValueMap theParameters,
	                                        RegionHandleSet regions, byte[] userSuppliedTag )
	    throws InteractionClassNotDefined, InteractionClassNotPublished,
	    InteractionParameterNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
	    RTIinternalError
	{
		featureNotSupported( "sendInteractionWithRegions()" );
	}

	public MessageRetractionReturn sendInteractionWithRegions(
	                                                        InteractionClassHandle theInteraction,
	                                                        ParameterHandleValueMap theParameters,
	                                                        RegionHandleSet regions,
	                                                        byte[] userSuppliedTag,
	                                                        LogicalTime theTime )
	    throws InteractionClassNotDefined, InteractionClassNotPublished,
	    InteractionParameterNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    InvalidRegionContext, InvalidLogicalTime, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "sendInteractionWithRegions(time)" );
		return null; // keep the compiler happy
	}

	//	 9.13 
	public void requestAttributeValueUpdateWithRegions(
	                                           ObjectClassHandle theClass,
	                                           AttributeSetRegionSetPairList attributesAndRegions,
	                                           byte[] userSuppliedTag )
	    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "requestAttributeValueUpdateWithRegions()" );
	}

	//////////////////////////
	// RTI Support Services //
	//////////////////////////
	// 10.2
	public ObjectClassHandle getObjectClassHandle( String theName ) throws NameNotFound,
	    FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		// get the class
		OCMetadata cls = helper.getFOM().getObjectClass( theName );
		if( cls == null )
		{
			throw new NameNotFound( theName );
		}
		else
		{
			return new HLA1516ObjectClassHandle( cls.getHandle() );
		}
	}

	// 10.3
	public String getObjectClassName( ObjectClassHandle theHandle )
	    throws InvalidObjectClassHandle, FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		// get the class
		int handle = HLA1516Handle.validatedHandle( theHandle );
		OCMetadata cls = helper.getFOM().getObjectClass( handle );
		if( cls == null )
		{
			throw new RTIinternalError( "unknown handle: " + theHandle );
		}
		else
		{
			return cls.getQualifiedName();
		}
	}

	// 10.4
	public AttributeHandle getAttributeHandle( ObjectClassHandle whichClass, String theName )
	    throws InvalidObjectClassHandle, NameNotFound, FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		int cHandle = HLA1516Handle.validatedHandle( whichClass );
		OCMetadata cls = helper.getFOM().getObjectClass( cHandle );
		if( cls == null )
		{
			throw new InvalidObjectClassHandle( "handle: " + whichClass );
		}
		
		ACMetadata aClass = helper.getFOM().getAttributeClass( cHandle, theName );
		if( aClass == null )
		{
			throw new NameNotFound( "name: " + theName );
		}
		else
		{
			return new HLA1516AttributeHandle( aClass.getHandle() );
		}
	}

	// 10.5
	public String getAttributeName( ObjectClassHandle whichClass, AttributeHandle theHandle )
	    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined,
	    FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		int ocHandle = HLA1516Handle.validatedHandle( whichClass );
		int acHandle = HLA1516Handle.validatedHandle( theHandle );
		OCMetadata cls = helper.getFOM().getObjectClass( ocHandle );
		if( cls == null )
		{
			throw new RTIinternalError( "handle: " + whichClass );
		}
		else
		{
			String name = cls.getAttributeName( acHandle );
			if( name == null )
			{
				throw new AttributeNotDefined( "handle: " + theHandle );
			}
			else
			{
				return name;
			}
		}
	}

	// 10.6
	public InteractionClassHandle getInteractionClassHandle( String theName ) throws NameNotFound,
	    FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		// get the class
		ICMetadata cls = helper.getFOM().getInteractionClass( theName );
		if( cls == null )
		{
			throw new NameNotFound( theName );
		}
		else
		{
			return new HLA1516InteractionClassHandle( cls.getHandle() );
		}
	}

	// 10.7
	public String getInteractionClassName( InteractionClassHandle theHandle )
	    throws InvalidInteractionClassHandle, FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		// get the class
		int handle = HLA1516Handle.validatedHandle( theHandle );
		ICMetadata cls = helper.getFOM().getInteractionClass( handle );
		if( cls == null )
		{
			throw new RTIinternalError( "handle: " + theHandle );
		}
		else
		{
			return cls.getQualifiedName();
		}
	}

	// 10.8
	public ParameterHandle getParameterHandle( InteractionClassHandle whichClass, String theName )
	    throws InvalidInteractionClassHandle, NameNotFound, FederateNotExecutionMember,
	    RTIinternalError
	{
		helper.checkJoined();
		
		int cHandle = HLA1516Handle.validatedHandle( whichClass );
		ICMetadata cls = helper.getFOM().getInteractionClass( cHandle );
		if( cls == null )
		{
			throw new RTIinternalError( "handle: " + cHandle );
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
				return new HLA1516ParameterHandle( handle );
			}
		}
	}

	// 10.9
	public String getParameterName( InteractionClassHandle whichClass, ParameterHandle theHandle )
	    throws InvalidInteractionClassHandle, InvalidParameterHandle,
	    InteractionParameterNotDefined, FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		int icHandle = HLA1516Handle.validatedHandle( whichClass );
		int pcHandle = HLA1516Handle.validatedHandle( theHandle );
		ICMetadata cls = helper.getFOM().getInteractionClass( icHandle );
		if( cls == null )
		{
			throw new RTIinternalError( "handle: " + icHandle );
		}
		else
		{
			String name = cls.getParameterName( pcHandle );
			if( name == null )
			{
				throw new InteractionParameterNotDefined( "handle: " + pcHandle );
			}
			else
			{
				return name;
			}
		}
	}

	// 10.10
	public ObjectInstanceHandle getObjectInstanceHandle( String theName )
	    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		OCInstance instance = helper.getState().getRepository().getInstance( theName );
		if( instance == null )
		{
			throw new ObjectInstanceNotKnown( "name: " + theName );
		}
		else
		{
			return new HLA1516ObjectInstanceHandle( instance.getHandle() );
		}
	}

	// 10.11
	public String getObjectInstanceName( ObjectInstanceHandle theHandle )
	    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		int handle = HLA1516Handle.validatedHandle( theHandle );
		OCInstance instance = helper.getState().getRepository().getInstance( handle );
		if( instance == null )
		{
			throw new RTIinternalError( "handle: " + handle );
		}
		else
		{
			return instance.getName();
		}
	}

	// 10.12
	public DimensionHandle getDimensionHandle( String theName ) throws NameNotFound,
	    FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getDimensionHandle()" );
		return null; // keep the compiler happy
	}

	// 10.13
	public String getDimensionName( DimensionHandle theHandle ) throws InvalidDimensionHandle,
	    FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getDimensionName()" );
		return ""; // keep the compiler happy
	}

	// 10.14
	public long getDimensionUpperBound( DimensionHandle theHandle ) throws InvalidDimensionHandle,
	    FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getDimensionUpperBound()" );
		return -1; // keep the compiler happy
	}

	// 10.15
	public DimensionHandleSet getAvailableDimensionsForClassAttribute(
	                                                                  ObjectClassHandle whichClass,
	                                                                  AttributeHandle theHandle )
	    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined,
	    FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getAvailableDimensionsForClassAttribute()" );
		return null; // keep the compiler happy
	}

	// 10.16
	public ObjectClassHandle getKnownObjectClassHandle( ObjectInstanceHandle theObject )
	    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError
	{
		helper.checkJoined();
		
		int handle = HLA1516Handle.validatedHandle( theObject );
		Integer clazz =
			helper.getState().getRepository().getInstance(handle).getDiscoveredType().getHandle();

		if( clazz == null )
			throw new ObjectInstanceNotKnown( "handle: " + theObject );
		else
			return new HLA1516ObjectClassHandle( clazz );
	}

	// 10.17
	public DimensionHandleSet getAvailableDimensionsForInteractionClass(
	                                                             InteractionClassHandle theHandle )
	    throws InvalidInteractionClassHandle, FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getAvailableDimensionsForInteractionClass()" );
		return null; // keep compiler happy
	}

	// 10.18
	public TransportationType getTransportationType( String theName )
	    throws InvalidTransportationName, FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getTransportationType()" );
		return null; // keep compiler happy
	}

	// 10.19
	public String getTransportationName( TransportationType theType )
	    throws InvalidTransportationType, FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getTransportationName()" );
		return ""; // keep compiler happy
	}

	// 10.20
	public OrderType getOrderType( String theName ) throws InvalidOrderName,
	    FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getOrderType()" );
		return null; // keep compiler happy
	}

	// 10.21
	public String getOrderName( OrderType theType ) throws InvalidOrderType,
	    FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "getOrderName()" );
		return ""; // keep compiler happy
	}

	// 10.22
	public void enableObjectClassRelevanceAdvisorySwitch() throws FederateNotExecutionMember,
	    ObjectClassRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "enableObjectClassRelevanceAdvisorySwitch()" );
	}

	// 10.23
	public void disableObjectClassRelevanceAdvisorySwitch()
	    throws ObjectClassRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "disableObjectClassRelevanceAdvisorySwitch()" );
	}

	// 10.24
	public void enableAttributeRelevanceAdvisorySwitch()
	    throws AttributeRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "enableAttributeRelevanceAdvisorySwitch()" );
	}

	// 10.25
	public void disableAttributeRelevanceAdvisorySwitch()
	    throws AttributeRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "disableAttributeRelevanceAdvisorySwitch()" );
	}

	// 10.26
	public void enableAttributeScopeAdvisorySwitch() throws AttributeScopeAdvisorySwitchIsOn,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "enableAttributeScopeAdvisorySwitch()" );
	}

	// 10.27
	public void disableAttributeScopeAdvisorySwitch() throws AttributeScopeAdvisorySwitchIsOff,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "disableAttributeScopeAdvisorySwitch()" );
	}

	// 10.28
	public void enableInteractionRelevanceAdvisorySwitch()
	    throws InteractionRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "enableInteractionRelevanceAdvisorySwitch()" );
	}

	// 10.29
	public void disableInteractionRelevanceAdvisorySwitch()
	    throws InteractionRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "disableInteractionRelevanceAdvisorySwitch()" );
	}

	// 10.30
	public DimensionHandleSet getDimensionHandleSet( RegionHandle region ) throws InvalidRegion,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "getDimensionHandleSet()" );
		return null; // keep compiler happy
	}

	// 10.31
	public RangeBounds getRangeBounds( RegionHandle region, DimensionHandle dimension )
	    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "getRangeBounds()" );
		return null; // keep compiler happy
	}

	// 10.32
	public void setRangeBounds( RegionHandle region, DimensionHandle dimension, RangeBounds bounds )
	    throws InvalidRegion, RegionNotCreatedByThisFederate,
	    RegionDoesNotContainSpecifiedDimension, InvalidRangeBound, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "setRangeBounds()" );
	}

	// 10.33
	public long normalizeFederateHandle( FederateHandle federateHandle )
	    throws InvalidFederateHandle, FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "normalizeFederateHandle()" );
		return -1; // keep compiler happy
	}

	// 10.34
	public long normalizeServiceGroup( ServiceGroup group ) throws InvalidServiceGroup,
	    FederateNotExecutionMember, RTIinternalError
	{
		featureNotSupported( "normalizeServiceGroup()" );
		return -1; // keep compiler happy
	}

	// 10.37
	public boolean evokeCallback( double seconds ) throws FederateNotExecutionMember,
	    RTIinternalError
	{
		return helper.evokeSingle( seconds );
	}

	// 10.38
	public boolean evokeMultipleCallbacks( double minimumTime, double maximumTime )
	    throws FederateNotExecutionMember, RTIinternalError
	{
		return helper.evokeMultiple( minimumTime, maximumTime );
	}

	// 10.39
	public void enableCallbacks() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "enableCallbacks()" );
	}

	// 10.40
	public void disableCallbacks() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "disableCallbacks()" );
	}

	// API-specific services
	public AttributeHandleFactory getAttributeHandleFactory() throws FederateNotExecutionMember
	{
		return HLA1516AttributeHandleFactory.INSTANCE;
	}

	public AttributeHandleSetFactory getAttributeHandleSetFactory()
	    throws FederateNotExecutionMember
	{
		return HLA1516AttributeHandleSetFactory.INSTANCE;
	}

	public AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
	    throws FederateNotExecutionMember
	{
		return HLA1516AttributeHandleValueMapFactory.INSTANCE;
	}

	public AttributeSetRegionSetPairListFactory getAttributeSetRegionSetPairListFactory()
	    throws FederateNotExecutionMember
	{
		return null;
	}

	public DimensionHandleFactory getDimensionHandleFactory() throws FederateNotExecutionMember
	{
		return null;
	}

	public DimensionHandleSetFactory getDimensionHandleSetFactory()
	    throws FederateNotExecutionMember
	{
		return null;
	}

	public FederateHandleFactory getFederateHandleFactory() throws FederateNotExecutionMember
	{
		return HLA1516FederateHandleFactory.INSTANCE;
	}

	public FederateHandleSetFactory getFederateHandleSetFactory() throws FederateNotExecutionMember
	{
		return HLA1516FederateHandleSetFactory.INSTANCE;
	}

	public InteractionClassHandleFactory getInteractionClassHandleFactory()
	    throws FederateNotExecutionMember
	{
		return HLA1516InteractionClassHandleFactory.INSTANCE;
	}

	public ObjectClassHandleFactory getObjectClassHandleFactory() throws FederateNotExecutionMember
	{
		return HLA1516ObjectClassHandleFactory.INSTANCE;
	}

	public ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
	    throws FederateNotExecutionMember
	{
		return HLA1516ObjectInstanceHandleFactory.INSTANCE;
	}

	public ParameterHandleFactory getParameterHandleFactory() throws FederateNotExecutionMember
	{
		return HLA1516ParameterHandleFactory.INSTANCE;
	}

	public ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
	    throws FederateNotExecutionMember
	{
		return HLA1516ParameterHandleValueMapFactory.INSTANCE;
	}

	public RegionHandleSetFactory getRegionHandleSetFactory() throws FederateNotExecutionMember
	{
		return null;
	}

	public String getHLAversion()
	{
		return "Portico-" + PorticoConstants.RTI_VERSION;
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Private Utility Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
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
			
			// set the source federate, if we have not joined yet (or have resigned)
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
		logger.warn( "Rti1516Ambassador doesn't yet support " + methodName );
		if( PorticoConstants.shouldThrowExceptionForUnsupportedCall() )
			throw new RTIinternalError( "Rti1516Ambassador doesn't yet support " + methodName );
	}


	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
