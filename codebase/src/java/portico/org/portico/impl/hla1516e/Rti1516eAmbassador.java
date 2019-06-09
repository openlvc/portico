/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.hla1516e;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;

import org.apache.logging.log4j.Logger;
import org.portico.impl.HLAVersion;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSetFactory;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleValueMap;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleValueMapFactory;
import org.portico.impl.hla1516e.types.HLA1516eAttributeSetRegionSetPairListFactory;
import org.portico.impl.hla1516e.types.HLA1516eDimensionHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eDimensionHandleSetFactory;
import org.portico.impl.hla1516e.types.HLA1516eFederateHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eFederateHandleSet;
import org.portico.impl.hla1516e.types.HLA1516eFederateHandleSetFactory;
import org.portico.impl.hla1516e.types.HLA1516eHandle;
import org.portico.impl.hla1516e.types.HLA1516eInteractionClassHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eObjectClassHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eObjectInstanceHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMap;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMapFactory;
import org.portico.impl.hla1516e.types.HLA1516eRegionHandleSetFactory;
import org.portico.impl.hla1516e.types.HLA1516eResignAction;
import org.portico.impl.hla1516e.types.HLA1516eTransportationTypeHandleFactory;
import org.portico.impl.hla1516e.types.time.DoubleTime;
import org.portico.impl.hla1516e.types.time.DoubleTimeFactory;
import org.portico.impl.hla1516e.types.time.DoubleTimeInterval;
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
import org.portico.lrc.compat.JIllegalName;
import org.portico.lrc.compat.JInconsistentFDD;
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
import org.portico.lrc.compat.JRestoreInProgress;
import org.portico.lrc.compat.JSaveInProgress;
import org.portico.lrc.compat.JSynchronizationLabelNotAnnounced;
import org.portico.lrc.compat.JTimeAdvanceAlreadyInProgress;
import org.portico.lrc.compat.JTimeConstrainedAlreadyEnabled;
import org.portico.lrc.compat.JTimeConstrainedWasNotEnabled;
import org.portico.lrc.compat.JTimeRegulationAlreadyEnabled;
import org.portico.lrc.compat.JTimeRegulationWasNotEnabled;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.OCMetadata;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.services.ownership.msg.AttributeDivest;
import org.portico.lrc.services.ownership.msg.AttributeRelease;
import org.portico.lrc.services.ownership.msg.CancelAcquire;
import org.portico.lrc.services.ownership.msg.CancelDivest;
import org.portico.lrc.services.ownership.msg.QueryOwnership;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.messaging.ErrorResponse;
import org.portico2.common.messaging.ExtendedSuccessResponse;
import org.portico2.common.messaging.MessageContext;
import org.portico2.common.messaging.ResponseMessage;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.ResignFederation;
import org.portico2.common.services.object.msg.DeleteObject;
import org.portico2.common.services.object.msg.LocalDelete;
import org.portico2.common.services.object.msg.RegisterObject;
import org.portico2.common.services.object.msg.RequestClassUpdate;
import org.portico2.common.services.object.msg.RequestObjectUpdate;
import org.portico2.common.services.object.msg.ReserveObjectName;
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
import org.portico2.lrc.services.object.data.LOCInstance;

/**
 * The Portico implementation of the IEEE 1516-2010 (HLA Evolved) RTIambassador class.
 */
public abstract class Rti1516eAmbassador implements RTIambassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl1516eHelper helper;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Rti1516eAmbassador() throws RTIinternalError
	{
		this.helper = new Impl1516eHelper();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public Impl1516eHelper getHelper()
	{
		return this.helper;
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Federation Management Services///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 4.2
	public void connect( FederateAmbassador federateReference,
	                     CallbackModel callbackModel,
	                     String localSettingsDesignator )
	    throws ConnectionFailed,
	           InvalidLocalSettingsDesignator,
	           UnsupportedCallbackModel,
	           AlreadyConnected,
	           CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		this.helper.connect( federateReference, callbackModel, localSettingsDesignator );
	}

	// 4.2
	public void connect( FederateAmbassador federateReference, CallbackModel callbackModel )
	    throws ConnectionFailed,
	           InvalidLocalSettingsDesignator,
	           UnsupportedCallbackModel,
	           AlreadyConnected,
	           CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		this.helper.connect( federateReference, callbackModel );
	}

	// 4.3
	public void disconnect()
		throws FederateIsExecutionMember,
		       CallNotAllowedFromWithinCallback,
		       RTIinternalError
	{
		this.helper.disconnect();
	}

	// 4.5
	public void createFederationExecution( String executionName, URL fomModule )
		throws InconsistentFDD,
		       ErrorReadingFDD,
		       CouldNotOpenFDD,
		       FederationExecutionAlreadyExists,
		       NotConnected,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		CreateFederation request = new CreateFederation( executionName, fomModule );
		request.setHlaVersion( HLAVersion.IEEE1516e );
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
			else if( theException instanceof JFederationExecutionAlreadyExists )
			{
				throw new FederationExecutionAlreadyExists( theException );
			}
			else if( theException instanceof JInconsistentFDD )
			{
				throw new InconsistentFDD( theException );
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

	// 4.5
	public void createFederationExecution( String federationName, URL[] fomModules )
		throws InconsistentFDD,
		       ErrorReadingFDD,
		       CouldNotOpenFDD,
		       FederationExecutionAlreadyExists,
		       NotConnected,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		CreateFederation request = new CreateFederation( federationName, fomModules );
		request.setHlaVersion( HLAVersion.IEEE1516e );
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
			else if( theException instanceof JFederationExecutionAlreadyExists )
			{
				// FIXME - old type of exception initialization
				throw new FederationExecutionAlreadyExists( theException.getMessage() );
			}
			else if( theException instanceof JInconsistentFDD )
			{
				throw new InconsistentFDD( theException );
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

	// 4.5
	public void createFederationExecution( String federationName, URL[] fomModules, URL mimModule )
	    throws InconsistentFDD,
	           ErrorReadingFDD,
	           CouldNotOpenFDD,
	           ErrorReadingMIM,
	           CouldNotOpenMIM,
	           DesignatorIsHLAstandardMIM,
	           FederationExecutionAlreadyExists,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// jam the MIM in at the front of the list of modules
		ArrayList<URL> moduleList = new ArrayList<URL>();
		moduleList.add( mimModule );
		if( fomModules != null )
			for( URL module : fomModules )
				moduleList.add( module );

		CreateFederation request = new CreateFederation( federationName, moduleList );
		request.setHlaVersion( HLAVersion.IEEE1516e );
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
			else if( theException instanceof JFederationExecutionAlreadyExists )
			{
				throw new FederationExecutionAlreadyExists( theException );
			}
			else if( theException instanceof JInconsistentFDD )
			{
				throw new InconsistentFDD( theException );
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

	// 4.5
	public void createFederationExecution( String federationName, URL[] fomModules, String timeName )
	    throws CouldNotCreateLogicalTimeFactory,
	           InconsistentFDD,
	           ErrorReadingFDD,
	           CouldNotOpenFDD,
	           FederationExecutionAlreadyExists,
	           NotConnected,
	           RTIinternalError
	{
		// validate the time type, ensuring it is one of the standard ones
		if( timeName != null && (timeName.trim().equals("") == false) )
		{
			timeName = timeName.trim();
			if( timeName.equals("HLAfloat64Time") == false &&
				timeName.equals("HLAinteger64Time") == false )
			{
				throw new CouldNotCreateLogicalTimeFactory( "Invalid time implementation: Must be "+
				                                            "\"HLAfloat64Time\" or \"HLAinteger64Time\"" );
			}
		}

		// validate the time type and hand off to the (String,URL[]) overload
		createFederationExecution( federationName, fomModules );
	}

	// 4.5
	public void createFederationExecution( String federationName,
	                                       URL[] fomModules,
	                                       URL mimModule,
	                                       String timeName )
	    throws CouldNotCreateLogicalTimeFactory,
	           InconsistentFDD,
	           ErrorReadingFDD,
	           CouldNotOpenFDD,
	           ErrorReadingMIM,
	           CouldNotOpenMIM,
	           DesignatorIsHLAstandardMIM,
	           FederationExecutionAlreadyExists,
	           NotConnected,
	           RTIinternalError
	{
		// validate the time type, ensuring it is one of the standard ones
		if( timeName != null )
		{
			timeName = timeName.trim();
			if( timeName.equals("HLAfloat64Time") == false &&
				timeName.equals("HLAinteger64Time") == false )
			{
				throw new CouldNotCreateLogicalTimeFactory( "Invalid time implementation: Must be "+
				                                            "\"HLAfloat64Time\" or \"HLAinteger64Time\"" );
			}
		}

		// validate the time parameter and hand off to the (String,URL[],URL) overload
		createFederationExecution( federationName, fomModules, mimModule );
	}

	// 4.6
	public void destroyFederationExecution( String executionName )
		throws FederatesCurrentlyJoined,
		       FederationExecutionDoesNotExist,
		       NotConnected,
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
			else
			{
				logException( "destroyFederationExecution", theException );
			}
		}
	}

	// 4.7
	public void listFederationExecutions() throws NotConnected, RTIinternalError
	{
		featureNotSupported( "listFederationExecutions()" );
	}

	// 4.9
	public FederateHandle joinFederationExecution( String federateName, String federationName )
	    throws CouldNotCreateLogicalTimeFactory,
	           FederationExecutionDoesNotExist,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateAlreadyExecutionMember,
	           NotConnected,
	           CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		// this method just passes off the request to the (String,String,String) overload
		// using the federate name as the federate type
		try
		{
			return joinFederationExecution( federateName, federateName, federationName );
		}
		catch( FederateNameAlreadyInUse niu )
		{
			throw new RTIinternalError( niu.getMessage(), niu );
		}
	}

	// 4.9
	public FederateHandle joinFederationExecution( String federateName,
	                                               String federateType,
	                                               String federationName )
	    throws CouldNotCreateLogicalTimeFactory,
	           FederateNameAlreadyInUse,
	           FederationExecutionDoesNotExist,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateAlreadyExecutionMember,
	           NotConnected,
	           CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		try
		{
			return joinFederationExecution( federateName, federateType, federationName, null );
		}
		catch( CouldNotOpenFDD cnof )
		{
			// should not happen
			logException( "joinFederationExecution", cnof );
			return null;
		}
		catch( InconsistentFDD ifdd )
		{
			// should not happen
			logException( "joinFederationExecution", ifdd );
			return null;
		}
		catch( ErrorReadingFDD erf )
		{
			// should not happen
			logException( "joinFederationExecution", erf );
			return null;
		}
	}

	// 4.9
	public FederateHandle joinFederationExecution( String federateType,
	                                               String federationName,
	                                               URL[] fomModules )
	    throws CouldNotCreateLogicalTimeFactory,
	           FederationExecutionDoesNotExist,
	           InconsistentFDD,
	           ErrorReadingFDD,
	           CouldNotOpenFDD,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateAlreadyExecutionMember,
	           NotConnected,
	           CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		try
		{
			return joinFederationExecution( federateType,
			                                federateType,
			                                federationName,
			                                fomModules );
		}
		catch( FederateNameAlreadyInUse fniu )
		{
			// should not happen
			logException( "joinFederationExecution", fniu );
			return null;
		}
	}

	// 4.9
	public FederateHandle joinFederationExecution( String federateName,
	                                               String federateType,
	                                               String federationName,
	                                               URL[] fomModules )
	    throws CouldNotCreateLogicalTimeFactory,
	           FederateNameAlreadyInUse,
	           FederationExecutionDoesNotExist,
	           InconsistentFDD,
	           ErrorReadingFDD,
	           CouldNotOpenFDD,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateAlreadyExecutionMember,
	           NotConnected,
	           CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		// 0. check the federate ambassador //
		// If we don't have a federate ambassador, we haven't connected yet
		helper.checkConnected();
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		JoinFederation request = new JoinFederation( federationName, federateName, federateType, fomModules );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			ExtendedSuccessResponse success = (ExtendedSuccessResponse)response;
			
			// return the "handle"
			return new HLA1516eHandle( (Integer)success.getResult() );
		}
		else
		{
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

	// 4.10
	public void resignFederationExecution( ResignAction resignAction )
		throws InvalidResignAction,
		       OwnershipAcquisitionPending,
		       FederateOwnsAttributes,
		       FederateNotExecutionMember,
		       NotConnected,
		       CallNotAllowedFromWithinCallback,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// the constructor below will throw InvalidResignAction for a dodgy value
		if( resignAction == null )
			throw new RTIinternalError( "Null resign action" );

		ResignFederation request =
			new ResignFederation( HLA1516eResignAction.fromResignAction(resignAction) );

		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			// reinitialize the federate to get rid of any built-up state
			helper.reinitializeLrc();
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

	// 4.11
	public void registerFederationSynchronizationPoint( String label, byte[] userSuppliedTag )
	    throws SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterSyncPoint request = new RegisterSyncPoint( label, userSuppliedTag );
		ResponseMessage response = processMessage( request );
		
		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "registerFederationSynchronizationPoint", 
		                                response, 
		                                label,
		                                userSuppliedTag );
		
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

	// 4.11
	public void registerFederationSynchronizationPoint( String label,
	                                                    byte[] tag,
	                                                    FederateHandleSet synchronizationSet )
	    throws InvalidFederateHandle,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// convert set into appropriate form
		HashSet<Integer> set = null;
		if( synchronizationSet != null )
			set = HLA1516eFederateHandleSet.toJavaSet( synchronizationSet );
		RegisterSyncPoint request = new RegisterSyncPoint( label, tag, set );
		ResponseMessage response = processMessage( request );
		
		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "registerFederationSynchronizationPoint", 
		                                response, 
		                                label,
		                                tag,
		                                synchronizationSet );
		
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

	// 4.14
	public void synchronizationPointAchieved( String synchronizationPointLabel )
	    throws SynchronizationPointLabelNotAnnounced,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SyncPointAchieved request = new SyncPointAchieved( synchronizationPointLabel );
		ResponseMessage response = processMessage( request );
		
		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "synchronizationPointAchieved", 
		                                response, 
		                                synchronizationPointLabel );
		
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

	// 4.14
	public void synchronizationPointAchieved( String synchronizationPointLabel,
	                                          boolean successIndicator )
	    throws SynchronizationPointLabelNotAnnounced,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SyncPointAchieved request = new SyncPointAchieved( synchronizationPointLabel );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "synchronizationPointAchieved", 
		                                response, 
		                                synchronizationPointLabel,
		                                successIndicator );
		
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

	// 4.16
	public void requestFederationSave( String label )
		throws SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "requestFederationSave()" );
	}

	// 4.16
	public void requestFederationSave( String label, LogicalTime theTime )
		throws LogicalTimeAlreadyPassed,
		       InvalidLogicalTime,
		       FederateUnableToUseTime,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "requestFederationSave()" );
	}

	// 4.18
	public void federateSaveBegun()
		throws SaveNotInitiated,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "federateSaveBegun()" );
	}

	// 4.19
	public void federateSaveComplete()
		throws FederateHasNotBegunSave,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "federateSaveComplete()" );
	}

	// 4.19
	public void federateSaveNotComplete()
		throws FederateHasNotBegunSave,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "federateSaveNotComplete()" );
	}

	// 4.21
	public void abortFederationSave()
		throws SaveNotInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "abortFederationSave()" );
	}

	// 4.22
	public void queryFederationSaveStatus()
		throws RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 4.24
	public void requestFederationRestore( String label )
		throws SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "requestFederationRestore()" );
	}

	// 4.28
	public void federateRestoreComplete()
		throws RestoreNotRequested,
		       SaveInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "federateRestoreComplete()" );
	}

	// 4.28
	public void federateRestoreNotComplete()
		throws RestoreNotRequested,
		       SaveInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "federateRestoreNotComplete()" );
	}

	// 4.30
	public void abortFederationRestore()
		throws RestoreNotInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "abortFederationRestore()" );
	}

	// 4.31
	public void queryFederationRestoreStatus()
		throws SaveInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationRestoreStatus()" );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////// Declaration Management Services /////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////

	// 5.2
	public void publishObjectClassAttributes( ObjectClassHandle theClass,
	                                          AttributeHandleSet attributeList )
	    throws AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		// check the handle set //
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( attributeList );
		int handle = HLA1516eHandle.fromHandle( theClass );
		PublishObjectClass request = new PublishObjectClass( handle, set );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "publishObjectClassAttributes", 
		                                response, 
		                                theClass,
		                                attributeList );
		
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
				logException( "publishObjectClassAttributes", theException );
			}
		}
	}

	// 5.3
	public void unpublishObjectClass( ObjectClassHandle theClass )
		throws OwnershipAcquisitionPending,
		       ObjectClassNotDefined,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnpublishObjectClass request = new UnpublishObjectClass(HLA1516eHandle.fromHandle(theClass));
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "unpublishObjectClass", 
		                                response, 
		                                theClass );
		
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

	// 5.3
	public void unpublishObjectClassAttributes( ObjectClassHandle theClass,
	                                            AttributeHandleSet attributeList )
	    throws OwnershipAcquisitionPending,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int cHandle = HLA1516eHandle.fromHandle( theClass );
		HashSet<Integer> attributes = HLA1516eAttributeHandleSet.toJavaSet( attributeList );
		UnpublishObjectClass request = new UnpublishObjectClass( cHandle, attributes );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "unpublishObjectClassAttributes", 
		                                response, 
		                                theClass,
		                                attributeList );
		
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
				logException( "unpublishObjectClassAttributes", theException );
			}
		}
	}

	// 5.4
	public void publishInteractionClass( InteractionClassHandle theInteraction )
	    throws InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		PublishInteractionClass request =
			new PublishInteractionClass( HLA1516eHandle.fromHandle(theInteraction) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "publishInteractionClass", 
		                                response, 
		                                theInteraction );
		
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
	    throws InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnpublishInteractionClass request =
			new UnpublishInteractionClass( HLA1516eHandle.fromHandle(theInteraction) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "unpublishInteractionClass", 
		                                response, 
		                                theInteraction );
		
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
	    throws AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( attributeList );
		int handle = HLA1516eHandle.fromHandle( theClass );
		SubscribeObjectClass request = new SubscribeObjectClass( handle, set, true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "subscribeObjectClassAttributes", 
		                                response, 
		                                theClass,
		                                attributeList );
		
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

	// 5.6
	public void subscribeObjectClassAttributes( ObjectClassHandle theClass,
	                                            AttributeHandleSet attributeList,
	                                            String updateRateDesignator )
	    throws AttributeNotDefined,
	           ObjectClassNotDefined,
	           InvalidUpdateRateDesignator,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributes(updateRateDesignator)" );
	}

	// 5.6
	public void subscribeObjectClassAttributesPassively( ObjectClassHandle theClass,
	                                                     AttributeHandleSet attributeList )
	    throws AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesPassively()" );
	}

	// 5.6
	public void subscribeObjectClassAttributesPassively( ObjectClassHandle theClass,
	                                                     AttributeHandleSet attributeList,
	                                                     String updateRateDesignator )
	    throws AttributeNotDefined,
	           ObjectClassNotDefined,
	           InvalidUpdateRateDesignator,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesPassively()" );
	}

	// 5.7
	public void unsubscribeObjectClass( ObjectClassHandle theClass )
		throws ObjectClassNotDefined,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnsubscribeObjectClass request =
			new UnsubscribeObjectClass( HLA1516eHandle.fromHandle(theClass) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "unsubscribeObjectClass", 
		                                response, 
		                                theClass );
		
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

	// 5.7
	public void unsubscribeObjectClassAttributes( ObjectClassHandle theClass,
	                                              AttributeHandleSet attributeList )
	    throws AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int cHandle = HLA1516eHandle.fromHandle( theClass );
		HashSet<Integer> attributes = HLA1516eAttributeHandleSet.toJavaSet( attributeList );
		UnsubscribeObjectClass request = new UnsubscribeObjectClass( cHandle, attributes );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "unsubscribeObjectClassAttributes", 
		                                response, 
		                                theClass,
		                                attributeList );
		
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
				logException( "unsucbscirbeObjectClassAttributes", theException );
			}
		}
	}

	// 5.8
	public void subscribeInteractionClass( InteractionClassHandle theClass )
	    throws FederateServiceInvocationsAreBeingReportedViaMOM,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SubscribeInteractionClass request =
			new SubscribeInteractionClass( HLA1516eHandle.fromHandle(theClass) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "subscribeInteractionClass", 
		                                response, 
		                                theClass );
		
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

	// 5.8
	public void subscribeInteractionClassPassively( InteractionClassHandle theClass )
	    throws FederateServiceInvocationsAreBeingReportedViaMOM,
	    InteractionClassNotDefined,
	    SaveInProgress,
	    RestoreInProgress,
	    FederateNotExecutionMember,
	    NotConnected,
	    RTIinternalError
	{
		featureNotSupported( "subscribeInteractionClassPassively()" );
	}

	// 5.9
	public void unsubscribeInteractionClass( InteractionClassHandle theClass )
	    throws InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UnsubscribeInteractionClass request =
			new UnsubscribeInteractionClass( HLA1516eHandle.fromHandle(theClass) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "unsubscribeInteractionClass", 
		                                response, 
		                                theClass );
		
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

	///////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Object Management Services ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 6.2
	public void reserveObjectInstanceName( String theObjectName )
		throws IllegalName,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		ReserveObjectName request = new ReserveObjectName( theObjectName );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "reserveObjectInstanceName", 
		                                response, 
		                                theObjectName );
		
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
			else if( theException instanceof JIllegalName )
			{
				throw new IllegalName( theException );
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
				logException( "reserveObjectInstanceName", theException );
			}
		}
	}

	// 6.4
	public void releaseObjectInstanceName( String theObjectInstanceName )
	    throws ObjectInstanceNameNotReserved,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "releaseObjectInstanceName()" );
	}

	// 6.5
	public void reserveMultipleObjectInstanceName( Set<String> theObjectNames )
		throws IllegalName,
		       NameSetWasEmpty,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "reserveMultipleObjectInstanceName()" );
	}

	// 6.7
	public void releaseMultipleObjectInstanceName( Set<String> theObjectNames )
	    throws ObjectInstanceNameNotReserved,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "releaseMultipleObjectInstanceName()" );
	}

	// 6.8
	public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass )
	    throws ObjectClassNotPublished,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterObject request = new RegisterObject( HLA1516eHandle.fromHandle(theClass) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "registerObjectInstance", 
		                                response, 
		                                theClass );
		
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			ExtendedSuccessResponse success = (ExtendedSuccessResponse)response;
			LOCInstance instance = (LOCInstance)success.getResult();
			return new HLA1516eHandle( instance.getHandle() );
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

	// 6.8
	public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass,
	                                                    String theObjectName )
	    throws ObjectInstanceNameInUse,
	           ObjectInstanceNameNotReserved,
	           ObjectClassNotPublished,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		RegisterObject request =
			new RegisterObject( HLA1516eHandle.fromHandle(theClass), theObjectName );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "registerObjectInstance", 
		                                response, 
		                                theClass,
		                                theObjectName );
		
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			ExtendedSuccessResponse success = (ExtendedSuccessResponse)response;
			LOCInstance instance = (LOCInstance)success.getResult();
			return new HLA1516eHandle( instance.getHandle() );
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

	// 6.10
	public void updateAttributeValues( ObjectInstanceHandle theObject,
	                                   AttributeHandleValueMap theAttributes,
	                                   byte[] tag )
	    throws AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashMap<Integer,byte[]> map = HLA1516eAttributeHandleValueMap.toJavaMap( theAttributes );
		int objectId = HLA1516eHandle.fromHandle( theObject );
		UpdateAttributes request = new UpdateAttributes( objectId, tag, map );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "updateAttributeValues", 
		                                response, 
		                                theObject,
		                                theAttributes,
		                                tag );
		
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

	// 6.10
	public MessageRetractionReturn updateAttributeValues( ObjectInstanceHandle theObject,
	                                                      AttributeHandleValueMap theAttributes,
	                                                      byte[] tag,
	                                                      LogicalTime theTime )
	    throws InvalidLogicalTime,
	           AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double doubleTime = PorticoConstants.NULL_TIME;
		if( theTime != null )
			doubleTime = DoubleTime.fromTime( theTime );

		HashMap<Integer,byte[]> map = HLA1516eAttributeHandleValueMap.toJavaMap( theAttributes );
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		UpdateAttributes request = new UpdateAttributes( oHandle, tag, map, doubleTime );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "updateAttributeValues", 
		                                response, 
		                                theObject,
		                                theAttributes,
		                                tag,
		                                theTime );
		
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return new MessageRetractionReturn( true, new HLA1516eHandle(0) );
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

	// 6.12
	public void sendInteraction( InteractionClassHandle theInteraction,
	                             ParameterHandleValueMap theParameters,
	                             byte[] tag )
	    throws InteractionClassNotPublished,
	           InteractionParameterNotDefined,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashMap<Integer,byte[]> map = HLA1516eParameterHandleValueMap.toJavaMap( theParameters ); 
		int interactionId = HLA1516eHandle.fromHandle( theInteraction );
		SendInteraction request = new SendInteraction( interactionId, tag, map );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "sendInteraction", 
		                                response, 
		                                theInteraction,
		                                theParameters,
		                                tag );
		
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

	// 6.12
	public MessageRetractionReturn sendInteraction( InteractionClassHandle theInteraction,
	                                                ParameterHandleValueMap theParameters,
	                                                byte[] tag,
	                                                LogicalTime theTime )
	    throws InvalidLogicalTime,
	           InteractionClassNotPublished,
	           InteractionParameterNotDefined,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double doubleTime = PorticoConstants.NULL_TIME;
		if( theTime != null )
			doubleTime = DoubleTime.fromTime( theTime );
		HashMap<Integer,byte[]> map = HLA1516eParameterHandleValueMap.toJavaMap( theParameters );
		int iHandle = HLA1516eHandle.fromHandle( theInteraction );

		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		SendInteraction request = new SendInteraction( iHandle, tag, map, doubleTime );
		ResponseMessage response = processMessage( request );
		
		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "sendInteraction", 
		                                response, 
		                                theInteraction,
		                                theParameters,
		                                tag,
		                                theTime );
		
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return new MessageRetractionReturn( true, new HLA1516eHandle(0) );
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

	// 6.14
	public void deleteObjectInstance( ObjectInstanceHandle objectHandle, byte[] userSuppliedTag )
	    throws DeletePrivilegeNotHeld,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DeleteObject request =
			new DeleteObject( HLA1516eHandle.fromHandle(objectHandle), userSuppliedTag );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "deleteObjectInstance", 
		                                response, 
		                                objectHandle,
		                                userSuppliedTag );
		
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

	// 6.14
	public MessageRetractionReturn deleteObjectInstance( ObjectInstanceHandle objectHandle,
	                                                     byte[] userSuppliedTag,
	                                                     LogicalTime theTime )
	    throws InvalidLogicalTime,
	           DeletePrivilegeNotHeld,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		////////////////////////////////////////////////////////
		// 0. check that we have the right logical time class //
		////////////////////////////////////////////////////////
		double time = PorticoConstants.NULL_TIME;
		if( theTime != null )
			time = DoubleTime.fromTime( theTime );
		int oHandle = HLA1516eHandle.fromHandle( objectHandle );
		
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		DeleteObject request = new DeleteObject( oHandle, userSuppliedTag, time );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "deleteObjectInstance", 
		                                response, 
		                                objectHandle,
		                                userSuppliedTag,
		                                theTime );
		
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return new MessageRetractionReturn( true, new HLA1516eHandle(0) );
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

	// 6.16
	public void localDeleteObjectInstance( ObjectInstanceHandle objectHandle )
	    throws OwnershipAcquisitionPending,
	           FederateOwnsAttributes,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		LocalDelete request = new LocalDelete( HLA1516eHandle.fromHandle(objectHandle) );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "localDeleteObjectInstance", 
		                                response, 
		                                objectHandle );
		
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

	// 6.19
	public void requestAttributeValueUpdate( ObjectInstanceHandle theObject,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] tag )
	    throws AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( theAttributes );
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		RequestObjectUpdate request = new RequestObjectUpdate( oHandle, set, tag );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "requestAttributeValueUpdate", 
		                                response, 
		                                theObject,
		                                theAttributes,
		                                tag );
		
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

	// 6.19
	public void requestAttributeValueUpdate( ObjectClassHandle theClass,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] tag )
	    throws AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( theAttributes );
		int cHandle = HLA1516eHandle.fromHandle( theClass );
		RequestClassUpdate request = new RequestClassUpdate( cHandle, set, tag );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "requestAttributeValueUpdate", 
		                                response, 
		                                theClass,
		                                theAttributes,
		                                tag );
		
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

	// 6.23
	public void requestAttributeTransportationTypeChange( ObjectInstanceHandle theObject,
	                                                      AttributeHandleSet theAttributes,
	                                                      TransportationTypeHandle theType )
	    throws AttributeAlreadyBeingChanged,
	           AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           InvalidTransportationType,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "requestAttributeTransportationTypeChange()" );
	}

	// 6.25
	public void queryAttributeTransportationType( ObjectInstanceHandle theObject,
	                                              AttributeHandle theAttribute )
	    throws AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryAttributeTransportationType()" );
	}

	// 6.27
	public void requestInteractionTransportationTypeChange( InteractionClassHandle theClass,
	                                                        TransportationTypeHandle theType )
	    throws InteractionClassAlreadyBeingChanged,
	           InteractionClassNotPublished,
	           InteractionClassNotDefined,
	           InvalidTransportationType,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "requestInteractionTransportationTypeChange()" );
	}

	// 6.29
	public void queryInteractionTransportationType( FederateHandle theFederate,
	                                                InteractionClassHandle theInteraction )
	    throws InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryInteractionTransportationType()" );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// Ownership Management Services //////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 7.2
	public void unconditionalAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
	                                                        AttributeHandleSet theAttributes )
	    throws AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( theAttributes );

		AttributeDivest request = new AttributeDivest( oHandle, set, true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "unconditionalAttributeOwnershipDivestiture", 
		                                response, 
		                                theObject,
		                                theAttributes );
		
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
	    throws AttributeAlreadyBeingDivested,
	           AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( theAttributes );

		AttributeDivest request = new AttributeDivest( oHandle, set, userSuppliedTag );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "negotiatedAttributeOwnershipDivestiture", 
		                                response, 
		                                theObject,
		                                theAttributes,
		                                userSuppliedTag );
		
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
	    throws NoAcquisitionPending,
	           AttributeDivestitureWasNotRequested,
	           AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "confirmDivestiture()" );
	}

	// 7.8
	public void attributeOwnershipAcquisition( ObjectInstanceHandle theObject,
	                                           AttributeHandleSet desiredAttributes,
	                                           byte[] userSuppliedTag )
	    throws AttributeNotPublished,
	           ObjectClassNotPublished,
	           FederateOwnsAttributes,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( desiredAttributes );

		AttributeAcquire request = new AttributeAcquire( oHandle, set, userSuppliedTag );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "attributeOwnershipAcquisition", 
		                                response, 
		                                theObject,
		                                desiredAttributes,
		                                userSuppliedTag );
		
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
	    throws AttributeAlreadyBeingAcquired,
	           AttributeNotPublished,
	           ObjectClassNotPublished,
	           FederateOwnsAttributes,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( desiredAttributes );

		AttributeAcquire request = new AttributeAcquire( oHandle, set, true );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "attributeOwnershipAcquisitionIfAvailable", 
		                                response, 
		                                theObject,
		                                desiredAttributes );
		
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
	public void attributeOwnershipReleaseDenied( ObjectInstanceHandle theObject,
	                                             AttributeHandleSet theAttributes )
	    throws AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "attributeOwnershipReleaseDenied()" );
	}

	// 7.13
	@SuppressWarnings("unchecked")
	public AttributeHandleSet attributeOwnershipDivestitureIfWanted( ObjectInstanceHandle theObject,
	                                                                 AttributeHandleSet theAttributes )
	    throws AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		// TODO NOTE This is just used the same as a release response in 1.3 at the moment.
		//           However, in 1516/e this doesn't have to be used in response to a release
		//           request, it can be used any time any federate wants
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( theAttributes );

		AttributeRelease request = new AttributeRelease( oHandle, set );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "attributeOwnershipDivestitureIfWanted", 
		                                response, 
		                                theObject,
		                                theAttributes );
		
		// check to see if we got an error or a success
		if( response.isError() == false )
		{
			// everything went fine!
			return new HLA1516eAttributeHandleSet( (Set<Integer>)response.getResult() );
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

	// 7.14
	public void cancelNegotiatedAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
	                                                           AttributeHandleSet theAttributes )
	    throws AttributeDivestitureWasNotRequested,
	           AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( theAttributes );

		CancelDivest request = new CancelDivest( oHandle, set );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "cancelNegotiatedAttributeOwnershipDivestiture", 
		                                response, 
		                                theObject,
		                                theAttributes );
		
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

	// 7.15
	public void cancelAttributeOwnershipAcquisition( ObjectInstanceHandle theObject,
	                                                 AttributeHandleSet theAttributes )
	    throws AttributeAcquisitionWasNotRequested,
	           AttributeAlreadyOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		HashSet<Integer> set = HLA1516eAttributeHandleSet.toJavaSet( theAttributes );

		CancelAcquire request = new CancelAcquire( oHandle, set );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "cancelAttributeOwnershipAcquisition", 
		                                response, 
		                                theObject,
		                                theAttributes );
		
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

	// 7.17
	public void queryAttributeOwnership( ObjectInstanceHandle theObject,
	                                     AttributeHandle theAttribute )
	    throws AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		int oHandle = HLA1516eHandle.fromHandle( theObject );
		int aHandle = HLA1516eHandle.fromHandle( theAttribute );

		QueryOwnership request = new QueryOwnership( oHandle, aHandle );
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "queryAttributeOwnership", 
		                                response, 
		                                theObject,
		                                theAttribute );
		
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

	// 7.19
	public boolean isAttributeOwnedByFederate( ObjectInstanceHandle theObject,
	                                           AttributeHandle theAttribute )
	    throws AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		return true;
		
//		int oHandle = HLA1516eHandle.fromHandle( theObject );		
//		OCInstance instance = helper.getState().getRepository().getInstance( oHandle );
//		if( instance == null )
//		{
//			throw new ObjectInstanceNotKnown( "handle: " + oHandle );
//		}
//		else
//		{
//			// convert the handle into an ACInstance
//			int aHandle = HLA1516eHandle.fromHandle( theAttribute );
//			ACInstance attribute = instance.getAttribute( aHandle );
//			if( attribute == null )
//				throw new AttributeNotDefined( "handle: " + aHandle );
//
//			// check to see if we are the owner
//			return attribute.getOwner() == helper.getState().getFederateHandle();
//		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Time Management Services /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 8.2
	public void enableTimeRegulation( LogicalTimeInterval theLookahead )
		throws InvalidLookahead,
		       InTimeAdvancingState,
		       RequestForTimeRegulationPending,
		       TimeRegulationAlreadyEnabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
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
		helper.reportServiceInvocation( "enableTimeRegulation", 
		                                response, 
		                                theLookahead );
		
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

	// 8.4
	public void disableTimeRegulation()
		throws TimeRegulationIsNotEnabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
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
		helper.reportServiceInvocation( "disableTimeRegulation", 
		                                response );
		
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

	// 8.5
	public void enableTimeConstrained()
		throws InTimeAdvancingState,
		       RequestForTimeConstrainedPending,
		       TimeConstrainedAlreadyEnabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
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
		helper.reportServiceInvocation( "enableTimeConstrained", 
		                                response );
		
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

	// 8.7
	public void disableTimeConstrained()
		throws TimeConstrainedIsNotEnabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
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
		helper.reportServiceInvocation( "disableTimeConstrained", 
		                                response );
		
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

	// 8.8
	public void timeAdvanceRequest( LogicalTime theTime )
		throws LogicalTimeAlreadyPassed,
		       InvalidLogicalTime,
		       InTimeAdvancingState,
		       RequestForTimeRegulationPending,
		       RequestForTimeConstrainedPending,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
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
		helper.reportServiceInvocation( "timeAdvanceRequest", 
		                                response,
		                                theTime );
		
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

	// 8.9
	public void timeAdvanceRequestAvailable( LogicalTime theTime )
		throws LogicalTimeAlreadyPassed,
		       InvalidLogicalTime,
		       InTimeAdvancingState,
		       RequestForTimeRegulationPending,
		       RequestForTimeConstrainedPending,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
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
		helper.reportServiceInvocation( "timeAdvanceRequestAvailable", 
		                                response,
		                                theTime );
		
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

	// 8.10
	public void nextMessageRequest( LogicalTime theTime )
		throws LogicalTimeAlreadyPassed,
		       InvalidLogicalTime,
		       InTimeAdvancingState,
		       RequestForTimeRegulationPending,
		       RequestForTimeConstrainedPending,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
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
		helper.reportServiceInvocation( "nextMessageRequest", 
		                                response,
		                                theTime );
		
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

	// 8.11
	public void nextMessageRequestAvailable( LogicalTime theTime )
		throws LogicalTimeAlreadyPassed,
		       InvalidLogicalTime,
		       InTimeAdvancingState,
		       RequestForTimeRegulationPending,
		       RequestForTimeConstrainedPending,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
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
		helper.reportServiceInvocation( "nextMessageRequestAvailable", 
		                                response,
		                                theTime );
		
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

	// 8.12
	public void flushQueueRequest( LogicalTime theTime )
		throws LogicalTimeAlreadyPassed,
		       InvalidLogicalTime,
		       InTimeAdvancingState,
		       RequestForTimeRegulationPending,
		       RequestForTimeConstrainedPending,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
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
		helper.reportServiceInvocation( "flushQueueRequest", 
		                                response,
		                                theTime );
		
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

	// 8.14
	public void enableAsynchronousDelivery()
		throws AsynchronousDeliveryAlreadyEnabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		ResponseMessage response = processMessage( new EnableAsynchronousDelivery() );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "enableAsynchronousDelivery", 
		                                response );
		
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

	// 8.15
	public void disableAsynchronousDelivery()
		throws AsynchronousDeliveryAlreadyDisabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		ResponseMessage response = processMessage( new DisableAsynchronousDelivery() );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "disableAsynchronousDelivery", 
		                                response );
		
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

	// 8.16
	public TimeQueryReturn queryGALT()
		throws SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		///////////////////////////////////////////////////////
		// 1. create the message and pass it to the LRC sink //
		///////////////////////////////////////////////////////
		QueryGalt request = new QueryGalt();
		ResponseMessage response = processMessage( request );

		////////////////////////////
		// 2. process the results //
		////////////////////////////
		helper.reportServiceInvocation( "queryGALT", 
		                                response );
		
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

	// 8.17
	public LogicalTime queryLogicalTime()
		throws SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		DoubleTime result = new DoubleTime( helper.getState().getCurrentTime() );
		
		helper.reportServiceInvocation( "queryLogicalTime", true, result );
		
		return result;
	}

	// 8.18
	public TimeQueryReturn queryLITS()
		throws SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		DoubleTime time = new DoubleTime( helper.getState().getCurrentTime() );
		TimeQueryReturn result = new TimeQueryReturn( true, time );
		
		helper.reportServiceInvocation( "queryLITS", true, result );
		
		return result;
	}

	// 8.19
	public void modifyLookahead( LogicalTimeInterval theLookahead )
		throws InvalidLookahead,
		       InTimeAdvancingState,
		       TimeRegulationIsNotEnabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
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
		helper.reportServiceInvocation( "modifyLookahead", 
		                                response,
		                                theLookahead );
		
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

	// 8.20
	public LogicalTimeInterval queryLookahead()
		throws TimeRegulationIsNotEnabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		helper.checkJoined();
		helper.checkSave();
		helper.checkRestore();
		
		DoubleTimeInterval result = new DoubleTimeInterval( helper.getState().getLookahead() );
		
		helper.reportServiceInvocation( "queryLookahead", true, result );
		
		return result;
	}

	// 8.21
	public void retract( MessageRetractionHandle theHandle )
		throws MessageCanNoLongerBeRetracted,
		       InvalidMessageRetractionHandle,
		       TimeRegulationIsNotEnabled,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "retract()" );
	}

	// 8.23
	public void changeAttributeOrderType( ObjectInstanceHandle theObject,
	                                      AttributeHandleSet theAttributes,
	                                      OrderType theType )
	    throws AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "changeAttributeOrderType()" );
	}

	// 8.24
	public void changeInteractionOrderType( InteractionClassHandle theClass, OrderType theType )
	    throws InteractionClassNotPublished,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "changeInteractionOrderType()" );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////// Data Distribution Management ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 9.2
	public RegionHandle createRegion( DimensionHandleSet dimensions )
		throws InvalidDimensionHandle,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "createRegion()" );
		return null;
	}

	// 9.3
	public void commitRegionModifications( RegionHandleSet regions )
	    throws RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "commitRegionModifications()" );
	}

	// 9.4
	public void deleteRegion( RegionHandle theRegion )
		throws RegionInUseForUpdateOrSubscription,
		       RegionNotCreatedByThisFederate,
		       InvalidRegion,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "deleteRegion()" );
	}

	// 9.5
	public ObjectInstanceHandle 
	       registerObjectInstanceWithRegions( ObjectClassHandle theClass,
	                                          AttributeSetRegionSetPairList attributesAndRegions )
	    throws InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotPublished,
	           ObjectClassNotPublished,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "registerObjectInstanceWithRegions()" );
		return null;
	}

	// 9.5
	public ObjectInstanceHandle
	       registerObjectInstanceWithRegions( ObjectClassHandle theClass,
	                                          AttributeSetRegionSetPairList attributesAndRegions,
	                                          String theObject )
	    throws ObjectInstanceNameInUse,
	           ObjectInstanceNameNotReserved,
	           InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotPublished,
	           ObjectClassNotPublished,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "registerObjectInstanceWithRegions()" );
		return null;
	}

	// 9.6
	public void associateRegionsForUpdates( ObjectInstanceHandle theObject,
	                                        AttributeSetRegionSetPairList attributesAndRegions )
	    throws InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "associateRegionsForUpdates()" );
	}

	// 9.7
	public void unassociateRegionsForUpdates( ObjectInstanceHandle theObject,
	                                          AttributeSetRegionSetPairList attributesAndRegions )
	    throws RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "unassociateRegionsForUpdates()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesWithRegions( ObjectClassHandle theClass,
	                                                      AttributeSetRegionSetPairList attributesAndRegions )
	    throws InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesWithRegions()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesWithRegions( ObjectClassHandle theClass,
	                                                       AttributeSetRegionSetPairList attributesAndRegions,
	                                                       String updateRateDesignator )
	    throws InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           InvalidUpdateRateDesignator,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesWithRegions()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesPassivelyWithRegions( ObjectClassHandle theClass,
	                                                                AttributeSetRegionSetPairList attributesAndRegions )
	    throws InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesPassivelyWithRegions()" );
	}

	// 9.8
	public void subscribeObjectClassAttributesPassivelyWithRegions( ObjectClassHandle theClass,
	                                                                AttributeSetRegionSetPairList attributesAndRegions,
	                                                                String updateRateDesignator )
	    throws InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           InvalidUpdateRateDesignator,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeObjectClassAttributesPassivelyWithRegions()" );
	}

	// 9.9
	public void unsubscribeObjectClassAttributesWithRegions( ObjectClassHandle theClass,
	                                                         AttributeSetRegionSetPairList attributesAndRegions )
	    throws RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "unsubscribeObjectClassAttributesWithRegions()" );
	}

	// 9.10
	public void subscribeInteractionClassWithRegions( InteractionClassHandle theClass,
	                                                  RegionHandleSet regions )
	    throws FederateServiceInvocationsAreBeingReportedViaMOM,
	           InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeInteractionClassWithRegions()" );
	}

	// 9.10
	public void subscribeInteractionClassPassivelyWithRegions( InteractionClassHandle theClass,
	                                                           RegionHandleSet regions )
	    throws FederateServiceInvocationsAreBeingReportedViaMOM,
	           InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "subscribeInteractionClassPassivelyWithRegions()" );
	}

	// 9.11
	public void unsubscribeInteractionClassWithRegions( InteractionClassHandle theClass,
	                                                    RegionHandleSet regions )
	    throws RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "unsubscribeInteractionClassWithRegions()" );
	}

	// 9.12
	public void sendInteractionWithRegions( InteractionClassHandle theInteraction,
	                                        ParameterHandleValueMap theParameters,
	                                        RegionHandleSet regions,
	                                        byte[] userSuppliedTag )
	    throws InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           InteractionClassNotPublished,
	           InteractionParameterNotDefined,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "sendInteractionWithRegions()" );
	}

	// 9.12
	public MessageRetractionReturn
	       sendInteractionWithRegions( InteractionClassHandle theInteraction,
	                                   ParameterHandleValueMap theParameters,
	                                   RegionHandleSet regions,
	                                   byte[] userSuppliedTag,
	                                   LogicalTime theTime )
	    throws InvalidLogicalTime,
	           InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           InteractionClassNotPublished,
	           InteractionParameterNotDefined,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "sendInteractionWithRegions()" );
		return null;
	}

	// 9.13
	public void requestAttributeValueUpdateWithRegions( ObjectClassHandle theClass,
	                                                    AttributeSetRegionSetPairList attributesAndRegions,
	                                                    byte[] userSuppliedTag )
	    throws InvalidRegionContext,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "requestAttributeValueUpdateWithRegions()" );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// RTI Support Services ///////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	// 10.2
	public ResignAction getAutomaticResignDirective()
		throws FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "getAutomaticResignDirective()" );
		return null;
	}

	// 10.3
	public void setAutomaticResignDirective( ResignAction resignAction )
		throws InvalidResignAction,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "setAutomaticResignDirective()" );
	}

	// 10.4
	public FederateHandle getFederateHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "getFederateHandle()" );
		return null;
	}

	// 10.5
	public String getFederateName( FederateHandle theHandle )
		throws InvalidFederateHandle,
	           FederateHandleNotKnown,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();

return "";
// TODO FIXME
//		// get a reference to all the known federates
//		int handle = HLA1516eHandle.validatedHandle( theHandle );
//		Federate federate = helper.getLrc().getState().getKnownFederate( handle );
//		if( federate == null )
//			throw new InvalidFederateHandle( "No known federate for handle ["+handle+"]" );
//		else
//			return federate.getFederateName();
	}

	// 10.6
	public ObjectClassHandle getObjectClassHandle( String theName )
		throws NameNotFound,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		
		// get the class
		OCMetadata cls = helper.getFOM().getObjectClass( theName );
		if( cls == null )
		{
			helper.reportServiceInvocation( "getObjectClassHandle", false, "name not found", theName );
			throw new NameNotFound( theName );
		}
		else
		{
			ObjectClassHandle result = new HLA1516eHandle( cls.getHandle() );
			helper.reportServiceInvocation( "getObjectClassHandle", true, result, theName );
			return result;
		}
	}

	// 10.7
	public String getObjectClassName( ObjectClassHandle theHandle )
		throws InvalidObjectClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		
		// get the class
		int handle = HLA1516eHandle.validatedHandle( theHandle );
		OCMetadata cls = helper.getFOM().getObjectClass( handle );
		if( cls == null )
		{
			helper.reportServiceInvocation( "getObjectClassName", false, "unknown handle", theHandle );
			throw new RTIinternalError( "unknown handle: " + theHandle );
		}
		else
		{
			String result = cls.getQualifiedName();
			helper.reportServiceInvocation( "getObjectClassName", true, result, theHandle );
			return result;
		}
	}

	// 10.8
	public ObjectClassHandle getKnownObjectClassHandle( ObjectInstanceHandle theObject )
	    throws ObjectInstanceNotKnown,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		LOCInstance instance = helper.getState().getRepository().getObject( theObject.hashCode() );
		if( instance == null )
		{
			helper.reportServiceInvocation( "getKnownObjectClassHandle", 
			                                false, 
			                                "object instance not known", 
			                                theObject );
			throw new ObjectInstanceNotKnown( "handle: " + theObject );
		}
		else
		{
			ObjectClassHandle result = new HLA1516eHandle( instance.getDiscoveredClassHandle() );
			helper.reportServiceInvocation( "getKnownObjectClassHandle", 
			                                true, 
			                                result, 
			                                theObject );
			return result;
		}
	}

	// 10.9
	public ObjectInstanceHandle getObjectInstanceHandle( String theName )
		throws ObjectInstanceNotKnown,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		helper.checkJoined();
		return null;
		
//		OCInstance instance = helper.getState().getRepository().getInstance( theName );
//		if( instance == null )
//		{
//			throw new ObjectInstanceNotKnown( "name: " + theName );
//		}
//		else
//		{
//			return new HLA1516eHandle( instance.getHandle() );
//		}
	}

	// 10.10
	public String getObjectInstanceName( ObjectInstanceHandle theHandle )
		throws ObjectInstanceNotKnown,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		return null;
		
//		int handle = HLA1516eHandle.validatedHandle( theHandle );
//		OCInstance instance = helper.getState().getRepository().getInstance( handle );
//		if( instance == null )
//		{
//			throw new RTIinternalError( "handle: " + handle );
//		}
//		else
//		{
//			return instance.getName();
//		}
	}

	// 10.11
	public AttributeHandle getAttributeHandle( ObjectClassHandle whichClass, String theName )
	    throws NameNotFound,
	           InvalidObjectClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		
		int cHandle = HLA1516eHandle.validatedHandle( whichClass );
		OCMetadata cls = helper.getFOM().getObjectClass( cHandle );
		if( cls == null )
		{
			helper.reportServiceInvocation( "getAttributeHandle", 
			                                false, 
			                                "invalid object class handle", 
			                                whichClass,
			                                theName );
			throw new InvalidObjectClassHandle( "handle: " + whichClass );
		}
		
		ACMetadata aClass = helper.getFOM().getAttributeClass( cHandle, theName );
		if( aClass == null )
		{
			helper.reportServiceInvocation( "getAttributeHandle", 
			                                false, 
			                                "name not found", 
			                                whichClass,
			                                theName );
			throw new NameNotFound( "name: " + theName );
		}
		else
		{
			AttributeHandle result = new HLA1516eHandle( aClass.getHandle() );
			helper.reportServiceInvocation( "getAttributeHandle", 
			                                true, 
			                                result, 
			                                whichClass,
			                                theName );
			return result;
		}
	}

	// 10.12
	public String getAttributeName( ObjectClassHandle whichClass, AttributeHandle theHandle )
	    throws AttributeNotDefined,
	           InvalidAttributeHandle,
	           InvalidObjectClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		
		int ocHandle = HLA1516eHandle.validatedHandle( whichClass );
		int acHandle = HLA1516eHandle.validatedHandle( theHandle );
		OCMetadata cls = helper.getFOM().getObjectClass( ocHandle );
		if( cls == null )
		{
			throw new RTIinternalError( "handle: " + whichClass );
		}
		else
		{
			String name = cls.getAttributeName( acHandle );
			helper.reportServiceInvocation( "getAttributeName", 
			                                name != null, 
			                                name != null ? name : "attribute not defined", 
			                                whichClass,
			                                theHandle );
			
			if( name == null )
				throw new AttributeNotDefined( "handle: " + theHandle );
			else
				return name;
		}
	}

	// 10.13
	public double getUpdateRateValue( String updateRateDesignator )
		throws InvalidUpdateRateDesignator,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "getUpdateRateValue()" );
		return 0.0;
	}

	// 10.14
	public double getUpdateRateValueForAttribute( ObjectInstanceHandle theObject,
	                                              AttributeHandle theAttribute )
	    throws ObjectInstanceNotKnown,
	           AttributeNotDefined,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "getUpdateRateValueForAttribute()" );
		return 0.0;
	}

	// 10.15
	public InteractionClassHandle getInteractionClassHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		helper.checkJoined();
		
		// get the class
		ICMetadata cls = helper.getFOM().getInteractionClass( theName );
		helper.reportServiceInvocation( "getInteractionClassHandle", 
		                                cls != null, 
		                                cls != null ? cls.getHandle() : "name not found", 
		                                theName );
		
		if( cls == null )
		{
			throw new NameNotFound( theName );
		}
		else
		{
			return new HLA1516eHandle( cls.getHandle() );
		}
	}

	// 10.16
	public String getInteractionClassName( InteractionClassHandle theHandle )
	    throws InvalidInteractionClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		
		// get the class
		int handle = HLA1516eHandle.validatedHandle( theHandle );
		ICMetadata cls = helper.getFOM().getInteractionClass( handle );
		helper.reportServiceInvocation( "getInteractionClassName", 
		                                cls != null, 
		                                cls != null ? cls.getQualifiedName() : "invalid interaction class handle", 
		                                theHandle );
		
		if( cls == null )
		{
			throw new InvalidInteractionClassHandle( "handle: " + theHandle );
		}
		else
		{
			return cls.getQualifiedName();
		}
	}

	// 10.17
	public ParameterHandle getParameterHandle( InteractionClassHandle whichClass, String theName )
	    throws NameNotFound,
	           InvalidInteractionClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		
		int cHandle = HLA1516eHandle.validatedHandle( whichClass );
		ICMetadata cls = helper.getFOM().getInteractionClass( cHandle );
		if( cls == null )
		{
			helper.reportServiceInvocation( "getParameterHandle", 
			                                false, 
			                                "invalid interaction class handle", 
			                                whichClass,
			                                theName );
			throw new InvalidInteractionClassHandle( "handle: " + cHandle );
		}
		else
		{
			int handle = cls.getParameterHandle( theName );
			if( handle == ObjectModel.INVALID_HANDLE )
			{
				helper.reportServiceInvocation( "getParameterHandle", 
				                                false, 
				                                "parameter name not found", 
				                                whichClass,
				                                theName );
				throw new NameNotFound( "name: " + theName );
			}
			else
			{
				ParameterHandle result = new HLA1516eHandle( handle );
				helper.reportServiceInvocation( "getParameterHandle", 
				                                true, 
				                                result, 
				                                whichClass,
				                                theName );
				return result;
			}
		}
	}

	// 10.18
	public String getParameterName( InteractionClassHandle whichClass, ParameterHandle theHandle )
	    throws InteractionParameterNotDefined,
	           InvalidParameterHandle,
	           InvalidInteractionClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		helper.checkJoined();
		
		int icHandle = HLA1516eHandle.validatedHandle( whichClass );
		int pcHandle = HLA1516eHandle.validatedHandle( theHandle );
		ICMetadata cls = helper.getFOM().getInteractionClass( icHandle );
		if( cls == null )
		{
			helper.reportServiceInvocation( "getParameterHandle", 
			                                false, 
			                                "invalid interaction class handle", 
			                                whichClass,
			                                theHandle );
			throw new InvalidInteractionClassHandle( "handle: " + icHandle );
		}
		else
		{
			String name = cls.getParameterName( pcHandle );
			if( name == null )
			{
				helper.reportServiceInvocation( "getParameterHandle", 
				                                false, 
				                                "parameter not defined", 
				                                whichClass,
				                                theHandle );
				throw new InteractionParameterNotDefined( "handle: " + pcHandle );
			}
			else
			{
				helper.reportServiceInvocation( "getParameterHandle", 
				                                true, 
				                                name, 
				                                whichClass,
				                                theHandle );
				return name;
			}
		}
	}

	// 10.19
	public OrderType getOrderType( String theName )
		throws InvalidOrderName,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "getOrderType()" );
		return null;
	}

	// 10.20
	public String getOrderName( OrderType theType )
		throws InvalidOrderType,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "getOrderName()" );
		return null;
	}

	// 10.21
	public TransportationTypeHandle getTransportationTypeHandle( String theName )
	    throws InvalidTransportationName,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "getTransportationTypeHandle()" );
		return null;
	}

	// 10.22
	public String getTransportationTypeName( TransportationTypeHandle theHandle )
	    throws InvalidTransportationType,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "getTransportationTypeName()" );
		return null;
	}

	// 10.23
	public DimensionHandleSet getAvailableDimensionsForClassAttribute( ObjectClassHandle whichClass,
	                                                                   AttributeHandle theHandle )
	    throws AttributeNotDefined,
	           InvalidAttributeHandle,
	           InvalidObjectClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "getAvailableDimensionsForClassAttribute()" );
		return null;
	}

	// 10.24
	public DimensionHandleSet
	       getAvailableDimensionsForInteractionClass( InteractionClassHandle theHandle )
	    throws InvalidInteractionClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "getAvailableDimensionsForInteractionClass()" );
		return null;
	}

	// 10.25
	public DimensionHandle getDimensionHandle( String theName )
		throws NameNotFound,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "getDimensionHandle()" );
		return null;
	}

	// 10.26
	public String getDimensionName( DimensionHandle theHandle )
		throws InvalidDimensionHandle,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "getDimensionName()" );
		return null;
	}

	// 10.27
	public long getDimensionUpperBound( DimensionHandle theHandle ) 
		throws InvalidDimensionHandle,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "getDimensionUpperBound()" );
		return 0;
	}

	// 10.28
	public DimensionHandleSet getDimensionHandleSet( RegionHandle region )
		throws InvalidRegion,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "getDimensionHandleSet()" );
		return null;
	}

	// 10.29
	public RangeBounds getRangeBounds( RegionHandle region, DimensionHandle dimension )
	    throws RegionDoesNotContainSpecifiedDimension,
	           InvalidRegion,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "getRangeBounds()" );
		return null;
	}

	// 10.30
	public void setRangeBounds( RegionHandle region, DimensionHandle dimension, RangeBounds bounds )
	    throws InvalidRangeBound,
	           RegionDoesNotContainSpecifiedDimension,
	           RegionNotCreatedByThisFederate,
	           InvalidRegion,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "setRangeBounds()" );
	}

	// 10.31
	public long normalizeFederateHandle( FederateHandle federateHandle )
		throws InvalidFederateHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		return HLA1516eHandle.fromHandle( federateHandle );
	}

	// 10.32
	public long normalizeServiceGroup( ServiceGroup group )
		throws InvalidServiceGroup,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "normalizeServiceGroup()" );
		return 0;
	}

	// 10.33
	public void enableObjectClassRelevanceAdvisorySwitch()
		throws ObjectClassRelevanceAdvisorySwitchIsOn,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "enableObjectClassRelevanceAdvisorySwitch()" );
	}

	// 10.34
	public void disableObjectClassRelevanceAdvisorySwitch()
	    throws ObjectClassRelevanceAdvisorySwitchIsOff,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "disableObjectClassRelevanceAdvisorySwitch()" );
	}

	// 10.35
	public void enableAttributeRelevanceAdvisorySwitch()
		throws AttributeRelevanceAdvisorySwitchIsOn,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "enableAttributeRelevanceAdvisorySwitch()" );
	}

	// 10.36
	public void disableAttributeRelevanceAdvisorySwitch()
		throws AttributeRelevanceAdvisorySwitchIsOff,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "disableAttributeRelevanceAdvisorySwitch()" );
	}

	// 10.37
	public void enableAttributeScopeAdvisorySwitch()
		throws AttributeScopeAdvisorySwitchIsOn,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "enableAttributeScopeAdvisorySwitch()" );
	}

	// 10.38
	public void disableAttributeScopeAdvisorySwitch()
		throws AttributeScopeAdvisorySwitchIsOff,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "disableAttributeScopeAdvisorySwitch()" );
	}

	// 10.39
	public void enableInteractionRelevanceAdvisorySwitch()
		throws InteractionRelevanceAdvisorySwitchIsOn,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "enableInteractionRelevanceAdvisorySwitch()" );
	}

	// 10.40
	public void disableInteractionRelevanceAdvisorySwitch()
	    throws InteractionRelevanceAdvisorySwitchIsOff,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "disableInteractionRelevanceAdvisorySwitch()" );
	}

	// 10.41
	public boolean evokeCallback( double approximateMinimumTimeInSeconds )
	    throws CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		try
		{
			boolean result = helper.evokeSingle( approximateMinimumTimeInSeconds );
			this.helper.reportServiceInvocation( "evokeCallback", 
			                                     true, 
			                                     result, 
			                                     approximateMinimumTimeInSeconds );
			return result;
		}
		catch ( CallNotAllowedFromWithinCallback cnafwc )
		{
			this.helper.reportServiceInvocation( "evokeCallback", 
			                                     false, 
			                                     "call not allowed from within callback", 
			                                     approximateMinimumTimeInSeconds );
			throw cnafwc;
		}
	}

	// 10.42
	public boolean evokeMultipleCallbacks( double approximateMinimumTimeInSeconds,
	                                       double approximateMaximumTimeInSeconds )
	    throws CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		try
		{
			boolean result = helper.evokeMultiple( approximateMinimumTimeInSeconds,
			                                       approximateMaximumTimeInSeconds );
			this.helper.reportServiceInvocation( "evokeMultipleCallbacks", 
			                                     true, 
			                                     result, 
			                                     approximateMinimumTimeInSeconds,
			                                     approximateMaximumTimeInSeconds );
			
			return result;
		}
		catch( CallNotAllowedFromWithinCallback cnafwc )
		{
			this.helper.reportServiceInvocation( "evokeMultipleCallbacks", 
			                                     false, 
			                                     "call not allowed from within callback", 
			                                     approximateMinimumTimeInSeconds );
			throw cnafwc;
		}
	}

	// 10.43
	public void enableCallbacks() throws SaveInProgress, RestoreInProgress, RTIinternalError
	{
		helper.checkSave();
		helper.checkRestore();
		helper.getState().setCallbacksEnabled( true );
		
		this.helper.reportServiceInvocation( "enableCallbacks", true, null );
		
		logger().debug( "enableCallbacks invoked(): callbacks turned on" );
	}

	// 10.44
	public void disableCallbacks() throws SaveInProgress, RestoreInProgress, RTIinternalError
	{
		helper.checkSave();
		helper.checkRestore();
		helper.getState().setCallbacksEnabled( false );
		
		this.helper.reportServiceInvocation( "disableCallbacks", true, null );
		
		logger().debug( "disableCallbacks invoked(): callbacks turned off" );
	}

	///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// API-specific services //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////
	public AttributeHandleFactory getAttributeHandleFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eAttributeHandleFactory();
	}

	public AttributeHandleSetFactory getAttributeHandleSetFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eAttributeHandleSetFactory();
	}

	public AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
	    throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eAttributeHandleValueMapFactory();
	}

	public AttributeSetRegionSetPairListFactory getAttributeSetRegionSetPairListFactory()
	    throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eAttributeSetRegionSetPairListFactory();
	}

	public DimensionHandleFactory getDimensionHandleFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eDimensionHandleFactory();
	}

	public DimensionHandleSetFactory getDimensionHandleSetFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eDimensionHandleSetFactory();
	}

	public FederateHandleFactory getFederateHandleFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eFederateHandleFactory();
	}

	public FederateHandleSetFactory getFederateHandleSetFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eFederateHandleSetFactory();
	}

	public InteractionClassHandleFactory getInteractionClassHandleFactory()
	    throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eInteractionClassHandleFactory();
	}

	public ObjectClassHandleFactory getObjectClassHandleFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eObjectClassHandleFactory();
	}

	public ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eObjectInstanceHandleFactory();
	}

	public ParameterHandleFactory getParameterHandleFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eParameterHandleFactory();
	}

	public ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
	    throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eParameterHandleValueMapFactory();
	}

	public RegionHandleSetFactory getRegionHandleSetFactory()
		throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eRegionHandleSetFactory();
	}

	public TransportationTypeHandleFactory getTransportationTypeHandleFactory()
	    throws FederateNotExecutionMember, NotConnected
	{
		return new HLA1516eTransportationTypeHandleFactory();
	}

	public String getHLAversion()
	{
		return "Portico (ieee-1516e)";
	}

	public LogicalTimeFactory getTimeFactory() throws FederateNotExecutionMember, NotConnected
	{
		return new DoubleTimeFactory();
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Private Utility Methods ////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	private ResponseMessage processMessage( PorticoMessage request ) throws NotConnected
	{
		// make sure we're connected
		this.helper.checkConnected();

		try
		{	
			// make sure we don't have concurrent access problems
			this.helper.checkAccess();
			
			// remove the time from the message if we are not constrained
			if( this.helper.getState().isRegulating() == false )
			{
				request.setTimestamp( PorticoConstants.NULL_TIME );
			}
			
			// set the source federate, if we have not joined yet (or have resigned)
			// this will be null
			request.setSourceFederate( this.helper.getState().getFederateHandle() );
			request.setTargetFederation( this.helper.getState().getFederationHandle() );
			
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
			// log the exception, but only at the debug level. Users can catch
			// and log themselves if they want to, and not every exception is
			// indication of an error (sometimes we expect them). Rather than
			// clutter system out with this detail by default, log it at debug
			this.helper.getLrcLogger().debug( e );
			
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
		logger().warn( "The IEEE 1516e interface doesn't yet support " + methodName );
		if( PorticoConstants.shouldThrowExceptionForUnsupportedCall() )
			throw new RTIinternalError( "The IEEE 1516e interface doesn't yet support "+methodName );
	}

	private Logger logger()
	{
		return this.helper.getLrcLogger();
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
