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
import java.util.Set;

import hla.rti1516e.*;
import hla.rti1516e.exceptions.*;

import org.apache.log4j.Logger;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleSetFactory;
import org.portico.impl.hla1516e.types.HLA1516eAttributeHandleValueMapFactory;
import org.portico.impl.hla1516e.types.HLA1516eAttributeSetRegionSetPairListFactory;
import org.portico.impl.hla1516e.types.HLA1516eDimensionHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eDimensionHandleSetFactory;
import org.portico.impl.hla1516e.types.HLA1516eFederateHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eFederateHandleSetFactory;
import org.portico.impl.hla1516e.types.HLA1516eInteractionClassHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eObjectClassHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eObjectInstanceHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleFactory;
import org.portico.impl.hla1516e.types.HLA1516eParameterHandleValueMapFactory;
import org.portico.impl.hla1516e.types.HLA1516eRegionHandleSetFactory;
import org.portico.impl.hla1516e.types.HLA1516eTransportationTypeHandleFactory;
import org.portico.impl.hla1516e.types.time.DoubleTimeFactory;
import org.portico.lrc.PorticoConstants;
import org.portico.utils.messaging.ErrorResponse;
import org.portico.utils.messaging.MessageContext;
import org.portico.utils.messaging.PorticoMessage;
import org.portico.utils.messaging.ResponseMessage;

/**
 * The Portico implementation of the IEEE 1516-2010 (HLA Evolved) RTIambassador class.
 */
public class Rti1516eAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Impl1516eHelper helper;
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	private Rti1516eAmbassador() throws RTIinternalError
	{
		this.helper = new Impl1516eHelper();
		this.logger = this.helper.getLrcLogger();
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
		featureNotSupported( "connect()" );
	}

	// 4.2
	public void connect( FederateAmbassador federateReference,
	                     CallbackModel callbackModel )
	    throws ConnectionFailed,
	           InvalidLocalSettingsDesignator,
	           UnsupportedCallbackModel,
	           AlreadyConnected,
	           CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		featureNotSupported( "connect()" );
	}

	// 4.3
	public void disconnect()
		throws FederateIsExecutionMember,
		       CallNotAllowedFromWithinCallback,
		       RTIinternalError
	{
		featureNotSupported( "disconnect()" );
	}

	// 4.5
	public void createFederationExecution( String federationExecutionName,
	                                       URL[] fomModules,
	                                       URL mimModule,
	                                       String logicalTimeImplementationName )
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
		featureNotSupported( "createFederationExecution()" );
	}

	// 4.5
	public void createFederationExecution( String federationExecutionName,
	                                       URL[] fomModules,
	                                       String logicalTimeImplementationName )
	    throws CouldNotCreateLogicalTimeFactory,
	           InconsistentFDD,
	           ErrorReadingFDD,
	           CouldNotOpenFDD,
	           FederationExecutionAlreadyExists,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "createFederationExecution()" );
	}

	// 4.5
	public void createFederationExecution( String federationExecutionName,
	                                       URL[] fomModules,
	                                       URL mimModule )
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
		featureNotSupported( "createFederationExecution()" );
	}

	// 4.5
	public void createFederationExecution( String federationExecutionName, URL[] fomModules )
		throws InconsistentFDD,
		       ErrorReadingFDD,
		       CouldNotOpenFDD,
		       FederationExecutionAlreadyExists,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "createFederationExecution()" );
	}

	// 4.5
	public void createFederationExecution( String federationExecutionName, URL fomModule )
		throws InconsistentFDD,
		       ErrorReadingFDD,
		       CouldNotOpenFDD,
		       FederationExecutionAlreadyExists,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "createFederationExecution()" );
	}

	// 4.6
	public void destroyFederationExecution( String federationExecutionName )
		throws FederatesCurrentlyJoined,
		       FederationExecutionDoesNotExist,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "destroyFederationExecution()" );
	}

	// 4.7
	public void listFederationExecutions() throws NotConnected, RTIinternalError
	{
		featureNotSupported( "listFederationExecutions()" );
	}

	// 4.9
	public FederateHandle joinFederationExecution( String federateName,
	                                               String federateType,
	                                               String federationExecutionName,
	                                               URL[] additionalFomModules )
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
		featureNotSupported( "joinFederationExecution()" );
		return null;
	}

	// 4.9
	public FederateHandle joinFederationExecution( String federateType,
	                                               String federationExecutionName,
	                                               URL[] additionalFomModules )
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
		featureNotSupported( "joinFederationExecution()" );
		return null;
	}

	// 4.9
	public FederateHandle joinFederationExecution( String federateName,
	                                               String federateType,
	                                               String federationExecutionName )
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
		featureNotSupported( "joinFederationExecution()" );
		return null;
	}

	// 4.9
	public FederateHandle joinFederationExecution( String federateType,
	                                               String federationExecutionName )
	    throws CouldNotCreateLogicalTimeFactory,
	           FederationExecutionDoesNotExist,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateAlreadyExecutionMember,
	           NotConnected,
	           CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		featureNotSupported( "joinFederationExecution()" );
		return null;
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
		featureNotSupported( "resignFederationExecution()" );
	}

	// 4.11
	public void registerFederationSynchronizationPoint( String synchronizationPointLabel,
	                                                    byte[] userSuppliedTag )
	    throws SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "registerFederationSynchronizationPoint()" );
	}

	// 4.11
	public void registerFederationSynchronizationPoint( String synchronizationPointLabel,
	                                                    byte[] userSuppliedTag,
	                                                    FederateHandleSet synchronizationSet )
	    throws InvalidFederateHandle,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "registerFederationSynchronizationPoint()" );
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
		featureNotSupported( "synchronizationPointAchieved()" );
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
		featureNotSupported( "synchronizationPointAchieved()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 4.28
	public void federateRestoreComplete()
		throws RestoreNotRequested,
		       SaveInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 4.28
	public void federateRestoreNotComplete()
		throws RestoreNotRequested,
		       SaveInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 4.30
	public void abortFederationRestore()
		throws RestoreNotInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 4.31
	public void queryFederationRestoreStatus()
		throws SaveInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 6.10
	public void updateAttributeValues( ObjectInstanceHandle theObject,
	                                   AttributeHandleValueMap theAttributes,
	                                   byte[] userSuppliedTag )
	    throws AttributeNotOwned,
	           AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 6.10
	public MessageRetractionReturn updateAttributeValues( ObjectInstanceHandle theObject,
	                                                      AttributeHandleValueMap theAttributes,
	                                                      byte[] userSuppliedTag,
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 6.12
	public void sendInteraction( InteractionClassHandle theInteraction,
	                             ParameterHandleValueMap theParameters,
	                             byte[] userSuppliedTag )
	    throws InteractionClassNotPublished,
	           InteractionParameterNotDefined,
	           InteractionClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 6.12
	public MessageRetractionReturn sendInteraction( InteractionClassHandle theInteraction,
	                                                ParameterHandleValueMap theParameters,
	                                                byte[] userSuppliedTag,
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
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
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 6.19
	public void requestAttributeValueUpdate( ObjectInstanceHandle theObject,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] userSuppliedTag )
	    throws AttributeNotDefined,
	           ObjectInstanceNotKnown,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 6.19
	public void requestAttributeValueUpdate( ObjectClassHandle theClass,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] userSuppliedTag )
	    throws AttributeNotDefined,
	           ObjectClassNotDefined,
	           SaveInProgress,
	           RestoreInProgress,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 7.13
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return false;
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 8.16
	public TimeQueryReturn queryGALT()
		throws SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 8.17
	public LogicalTime queryLogicalTime()
		throws SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 8.18
	public TimeQueryReturn queryLITS()
		throws SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.3
	public void setAutomaticResignDirective( ResignAction resignAction )
		throws InvalidResignAction,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 10.4
	public FederateHandle getFederateHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.6
	public ObjectClassHandle getObjectClassHandle( String theName )
		throws NameNotFound,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.7
	public String getObjectClassName( ObjectClassHandle theHandle )
		throws InvalidObjectClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.8
	public ObjectClassHandle getKnownObjectClassHandle( ObjectInstanceHandle theObject )
	    throws ObjectInstanceNotKnown,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.9
	public ObjectInstanceHandle getObjectInstanceHandle( String theName )
		throws ObjectInstanceNotKnown,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.10
	public String getObjectInstanceName( ObjectInstanceHandle theHandle )
		throws ObjectInstanceNotKnown,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.11
	public AttributeHandle getAttributeHandle( ObjectClassHandle whichClass, String theName )
	    throws NameNotFound,
	           InvalidObjectClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.13
	public double getUpdateRateValue( String updateRateDesignator )
		throws InvalidUpdateRateDesignator,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return 0.0;
	}

	// 10.15
	public InteractionClassHandle getInteractionClassHandle( String theName )
		throws NameNotFound,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.16
	public String getInteractionClassName( InteractionClassHandle theHandle )
	    throws InvalidInteractionClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.17
	public ParameterHandle getParameterHandle( InteractionClassHandle whichClass, String theName )
	    throws NameNotFound,
	           InvalidInteractionClassHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.19
	public OrderType getOrderType( String theName )
		throws InvalidOrderName,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.20
	public String getOrderName( OrderType theType )
		throws InvalidOrderType,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.21
	public TransportationTypeHandle getTransportationTypeHandle( String theName )
	    throws InvalidTransportationName,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.22
	public String getTransportationTypeName( TransportationTypeHandle theHandle )
	    throws InvalidTransportationType,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.25
	public DimensionHandle getDimensionHandle( String theName )
		throws NameNotFound,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.26
	public String getDimensionName( DimensionHandle theHandle )
		throws InvalidDimensionHandle,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return null;
	}

	// 10.27
	public long getDimensionUpperBound( DimensionHandle theHandle ) 
		throws InvalidDimensionHandle,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 10.31
	public long normalizeFederateHandle( FederateHandle federateHandle )
		throws InvalidFederateHandle,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return 0;
	}

	// 10.32
	public long normalizeServiceGroup( ServiceGroup group )
		throws InvalidServiceGroup,
	           FederateNotExecutionMember,
	           NotConnected,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
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
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 10.41
	public boolean evokeCallback( double approximateMinimumTimeInSeconds )
	    throws CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return false;
	}

	// 10.42
	public boolean evokeMultipleCallbacks( double approximateMinimumTimeInSeconds,
	                                       double approximateMaximumTimeInSeconds )
	    throws CallNotAllowedFromWithinCallback,
	           RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
		return false;
	}

	// 10.43
	public void enableCallbacks() throws SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
	}

	// 10.44
	public void disableCallbacks() throws SaveInProgress, RestoreInProgress, RTIinternalError
	{
		featureNotSupported( "queryFederationSaveStatus()" );
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
	private ResponseMessage processMessage( PorticoMessage request )
	{
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
		logger.error( "The IEEE 1516e interface doesn't yet support " + methodName );
		if( PorticoConstants.shouldThrowExceptionForUnsupportedCall() )
			throw new RTIinternalError( "The IEEE 1516e interface doesn't yet support "+methodName );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
