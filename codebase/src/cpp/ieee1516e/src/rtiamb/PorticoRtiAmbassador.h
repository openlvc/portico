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
#ifndef PORTICORTIAMBASSADOR_H_
#define PORTICORTIAMBASSADOR_H_

#include "common.h"
#include "jni/Runtime.h"
#include "jni/JavaRTI.h"

PORTICO1516E_NS_START

class PorticoRtiAmbassador : public RTIambassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		JavaRTI *javarti;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		PorticoRtiAmbassador();
		~PorticoRtiAmbassador();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/////////////////////////////////////////////////////////////////////////////////
		// Federation Management Services ///////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////
		// 4.2
		void connect( FederateAmbassador & federateAmbassador,
		              CallbackModel theCallbackModel,
		              const std::wstring& localSettingsDesignator = L"" )
			throw( ConnectionFailed,
			       InvalidLocalSettingsDesignator,
			       UnsupportedCallbackModel,
			       AlreadyConnected,
			       CallNotAllowedFromWithinCallback,
			       RTIinternalError );

		// 4.3
		void disconnect() throw( FederateIsExecutionMember,
		                         CallNotAllowedFromWithinCallback,
		                         RTIinternalError );

		// 4.5
		void createFederationExecution( const std::wstring& federationExecutionName,
		                                const std::wstring& fomModule,
		                                const std::wstring& logicalTimeImplementationName = L"" )
			throw( CouldNotCreateLogicalTimeFactory,
		           InconsistentFDD,
		           ErrorReadingFDD,
		           CouldNotOpenFDD,
		           FederationExecutionAlreadyExists,
		           NotConnected,
		           RTIinternalError );

		void createFederationExecution( const std::wstring& federationExecutionName,
		                                const std::vector<std::wstring>& fomModules,
		                                const std::wstring& logicalTimeImplementationName = L"" )
			throw( CouldNotCreateLogicalTimeFactory,
			       InconsistentFDD,
			       ErrorReadingFDD,
			       CouldNotOpenFDD,
			       FederationExecutionAlreadyExists,
			       NotConnected,
			       RTIinternalError );

		void createFederationExecutionWithMIM( const std::wstring& federationExecutionName,
		                                       const std::vector<std::wstring>& fomModules,
		                                       const std::wstring& mimModule,
		                                       const std::wstring& logicalTimeImplementationName = L"" )
			throw( CouldNotCreateLogicalTimeFactory,
		           InconsistentFDD,
		           ErrorReadingFDD,
		           CouldNotOpenFDD,
		           DesignatorIsHLAstandardMIM,
		           ErrorReadingMIM,
		           CouldNotOpenMIM,
		           FederationExecutionAlreadyExists,
		           NotConnected,
		           RTIinternalError );

		// 4.6
		void destroyFederationExecution( const std::wstring& federationExecutionName )
			throw( FederatesCurrentlyJoined,
			       FederationExecutionDoesNotExist,
			       NotConnected,
			       RTIinternalError );

		// 4.7
		void listFederationExecutions() throw( NotConnected, RTIinternalError );

		// 4.9
		FederateHandle joinFederationExecution( const std::wstring& federateType,
		                                        const std::wstring& federationExecutionName,
		                                        const std::vector<std::wstring>& additionalFomModules = std::vector<std::wstring>() )
			throw( CouldNotCreateLogicalTimeFactory,
			       FederationExecutionDoesNotExist,
			       InconsistentFDD,
			       ErrorReadingFDD, 
			       CouldNotOpenFDD,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateAlreadyExecutionMember,
			       NotConnected,
			       CallNotAllowedFromWithinCallback,
			       RTIinternalError );

		FederateHandle joinFederationExecution( const std::wstring& federateName,
		                                        const std::wstring& federateType,
		                                        const std::wstring& federationExecutionName,
		                                        const std::vector<std::wstring>& additionalFomModules = std::vector<std::wstring>() )
			throw( CouldNotCreateLogicalTimeFactory,
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
			       RTIinternalError );

		// 4.10
		void resignFederationExecution( ResignAction resignAction )
			throw( InvalidResignAction,
			       OwnershipAcquisitionPending,
			       FederateOwnsAttributes,
			       FederateNotExecutionMember,
			       NotConnected,
			       CallNotAllowedFromWithinCallback,
			       RTIinternalError );

		/////////////////////////////////////////////////////////////////////////////////
		// Synchronization Services /////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////
		// 4.11
		void registerFederationSynchronizationPoint( const std::wstring& label,
		                                             const VariableLengthData& tag )
			throw( SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		void registerFederationSynchronizationPoint( const std::wstring& label,
		                                             const VariableLengthData& tag,
		                                             const FederateHandleSet& synchronizationSet )
			throw( InvalidFederateHandle,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.14
		void synchronizationPointAchieved( const std::wstring& label, bool successfully = true )
			throw( SynchronizationPointLabelNotAnnounced,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		/////////////////////////////////////////////////////////////////////////////////
		// Save and Restore Services ////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////
		// 4.16
		void requestFederationSave( const std::wstring& label )
			throw( SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		void requestFederationSave( const std::wstring& label,
		                            const LogicalTime& theTime )
			throw( LogicalTimeAlreadyPassed,
			       InvalidLogicalTime,
			       FederateUnableToUseTime,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.18
		void federateSaveBegun()
			throw( SaveNotInitiated,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.19
		void federateSaveComplete()
			throw( FederateHasNotBegunSave,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		void federateSaveNotComplete()
			throw( FederateHasNotBegunSave,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.21
		void abortFederationSave()
			throw( SaveNotInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.22
		void queryFederationSaveStatus()
			throw( RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.24
		void requestFederationRestore( const std::wstring& label )
			throw( SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.28
		void federateRestoreComplete()
			throw( RestoreNotRequested,
			       SaveInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		void federateRestoreNotComplete()
			throw( RestoreNotRequested,
			       SaveInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.30
		void abortFederationRestore()
			throw( RestoreNotInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 4.31
		void queryFederationRestoreStatus()
			throw( SaveInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		/////////////////////////////////////////////////////////////////////////////////
		// Declaration Management Services //////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////
		// 5.2
		void publishObjectClassAttributes( ObjectClassHandle theClass,
		                                   const AttributeHandleSet& attributeList )
			throw( AttributeNotDefined,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 5.3
		void unpublishObjectClass( ObjectClassHandle theClass )
			throw( OwnershipAcquisitionPending,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		void unpublishObjectClassAttributes( ObjectClassHandle theClass,
		                                     const AttributeHandleSet& attributeList )
			throw( OwnershipAcquisitionPending,
			       AttributeNotDefined,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 5.4
		void publishInteractionClass( InteractionClassHandle theInteraction )
			throw( InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 5.5
		void unpublishInteractionClass( InteractionClassHandle theInteraction )
			throw( InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 5.6
		void subscribeObjectClassAttributes( ObjectClassHandle theClass,
		                                     const AttributeHandleSet& attributeList,
		                                     bool active = true,
		                                     const std::wstring& updateRateDesignator = L"" )
			throw( AttributeNotDefined,
			       ObjectClassNotDefined,
			       InvalidUpdateRateDesignator,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 5.7
		void unsubscribeObjectClass( ObjectClassHandle theClass )
			throw( ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		void unsubscribeObjectClassAttributes( ObjectClassHandle theClass,
		                                       const AttributeHandleSet& attributeList )
			throw( AttributeNotDefined,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 5.8
		void subscribeInteractionClass( InteractionClassHandle theClass,
		                                bool active = true )
			throw( FederateServiceInvocationsAreBeingReportedViaMOM,
			       InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 5.9
		void unsubscribeInteractionClass( InteractionClassHandle theClass )
			throw( InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		/////////////////////////////////////////////////////////////////////////////////
		// Object Management Services ///////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////

		// 6.2
		void reserveObjectInstanceName( const std::wstring& instanceName ) 
			throw( IllegalName,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.4
		void releaseObjectInstanceName( const std::wstring& instanceName )
			throw( ObjectInstanceNameNotReserved,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.5
		void reserveMultipleObjectInstanceName( const std::set<std::wstring>& instanceNames )
			throw( IllegalName,
			       NameSetWasEmpty,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.7
		void releaseMultipleObjectInstanceName( const std::set<std::wstring>& instanceNames )
			throw( ObjectInstanceNameNotReserved,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.8
		ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass )
			throw( ObjectClassNotPublished,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass,
		                                             const std::wstring& theObjectInstanceName )
			throw( ObjectInstanceNameInUse,
			       ObjectInstanceNameNotReserved,
			       ObjectClassNotPublished,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.10
		void updateAttributeValues( ObjectInstanceHandle theObject,
		                            const AttributeHandleValueMap& attributes,
		                            const VariableLengthData& tag )
			throw( AttributeNotOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		MessageRetractionHandle updateAttributeValues( ObjectInstanceHandle theObject,
		                                               const AttributeHandleValueMap& theAttributes,
		                                               const VariableLengthData& tag,
		                                               const LogicalTime& theTime )
		throw( InvalidLogicalTime,
		       AttributeNotOwned,
		       AttributeNotDefined,
		       ObjectInstanceNotKnown,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError );

		// 6.12
		void sendInteraction( InteractionClassHandle theInteraction,
		                      const ParameterHandleValueMap& parameters,
		                      const VariableLengthData& tag )
			throw( InteractionClassNotPublished,
			       InteractionParameterNotDefined,
			       InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		MessageRetractionHandle sendInteraction( InteractionClassHandle theInteraction,
		                                         const ParameterHandleValueMap& parameters,
		                                         const VariableLengthData& tag,
		                                         const LogicalTime& theTime )
			throw( InvalidLogicalTime,
			       InteractionClassNotPublished,
			       InteractionParameterNotDefined,
			       InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.14
		void deleteObjectInstance( ObjectInstanceHandle theObject,
		                           const VariableLengthData& tag )
			throw( DeletePrivilegeNotHeld,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		MessageRetractionHandle deleteObjectInstance( ObjectInstanceHandle theObject,
		                                              const VariableLengthData& tag,
		                                              const LogicalTime& theTime )
			throw( InvalidLogicalTime,
			       DeletePrivilegeNotHeld,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.16
		void localDeleteObjectInstance( ObjectInstanceHandle theObject )
			throw( OwnershipAcquisitionPending,
			       FederateOwnsAttributes,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.19
		void requestAttributeValueUpdate( ObjectInstanceHandle theObject,
		                                  const AttributeHandleSet& attributes,
		                                  const VariableLengthData& tag )
			throw( AttributeNotDefined,
				   ObjectInstanceNotKnown,
				   SaveInProgress,
				   RestoreInProgress,
				   FederateNotExecutionMember,
				   NotConnected,
				   RTIinternalError );

		void requestAttributeValueUpdate( ObjectClassHandle theClass,
		                                  const AttributeHandleSet& attributes,
		                                  const VariableLengthData& tag )
			throw( AttributeNotDefined,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.23
		void requestAttributeTransportationTypeChange( ObjectInstanceHandle theObject,
		                                               const AttributeHandleSet& attributes,
		                                               TransportationType theType )
			throw( AttributeAlreadyBeingChanged,
			       AttributeNotOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       InvalidTransportationType,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.25
		void queryAttributeTransportationType( ObjectInstanceHandle theObject,
		                                       AttributeHandle theAttribute )
			throw( AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.27
		void requestInteractionTransportationTypeChange( InteractionClassHandle theClass,
		                                                 TransportationType theType )
			throw( InteractionClassAlreadyBeingChanged,
			       InteractionClassNotPublished,
			       InteractionClassNotDefined,
			       InvalidTransportationType,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 6.29
		void queryInteractionTransportationType( FederateHandle theFederate,
		                                         InteractionClassHandle theInteraction )
			throw( InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		
		///////////////////////////////////
		// Ownership Management Services //
		///////////////////////////////////
		// 7.2
		void unconditionalAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
		                                                 const AttributeHandleSet& theAttributes )
			throw( AttributeNotOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.3
		void negotiatedAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
		                                              const AttributeHandleSet& theAttributes,
		                                              const VariableLengthData& tag )
			throw( AttributeAlreadyBeingDivested,
			       AttributeNotOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.6
		void confirmDivestiture( ObjectInstanceHandle theObject,
		                         const AttributeHandleSet& confirmedAttributes,
		                         const VariableLengthData& tag )
			throw( NoAcquisitionPending,
			       AttributeDivestitureWasNotRequested,
			       AttributeNotOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.8
		void attributeOwnershipAcquisition( ObjectInstanceHandle theObject,
		                                    const AttributeHandleSet& desiredAttributes,
		                                    const VariableLengthData& tag )
			throw( AttributeNotPublished,
			       ObjectClassNotPublished,
			       FederateOwnsAttributes,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.9
		void attributeOwnershipAcquisitionIfAvailable( ObjectInstanceHandle theObject,
		                                               const AttributeHandleSet& desiredAttributes )
			throw( AttributeAlreadyBeingAcquired,
			       AttributeNotPublished,
			       ObjectClassNotPublished,
			       FederateOwnsAttributes,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.12
		void attributeOwnershipReleaseDenied( ObjectInstanceHandle theObject,
		                                      const AttributeHandleSet& theAttributes )
			throw( AttributeNotOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.13
		void attributeOwnershipDivestitureIfWanted( ObjectInstanceHandle theObject,
		                                            const AttributeHandleSet& theAttributes,
		                                            AttributeHandleSet& theDivestedAttributes ) // filled by RTI
		throw( AttributeNotOwned,
		       AttributeNotDefined,
		       ObjectInstanceNotKnown,
		       SaveInProgress,
		       RestoreInProgress,
		       FederateNotExecutionMember,
		       NotConnected,
		       RTIinternalError );

		// 7.14
		void cancelNegotiatedAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
		                                                    const AttributeHandleSet& theAttributes )
			throw( AttributeDivestitureWasNotRequested,
			       AttributeNotOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.15
		void cancelAttributeOwnershipAcquisition( ObjectInstanceHandle theObject,
		                                          const AttributeHandleSet& theAttributes )
			throw( AttributeAcquisitionWasNotRequested,
			       AttributeAlreadyOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.17
		void queryAttributeOwnership( ObjectInstanceHandle theObject,
		                              AttributeHandle theAttribute )
			throw( AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 7.19
		bool isAttributeOwnedByFederate( ObjectInstanceHandle theObject,
		                                 AttributeHandle theAttribute )
			throw( AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		
		/////////////////////////////////////////////////////////////////////////////////
		// Time Management Services /////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////
		// 8.2
		void enableTimeRegulation( const LogicalTimeInterval& theLookahead )
			throw( InvalidLookahead,
			       InTimeAdvancingState,
			       RequestForTimeRegulationPending,
			       TimeRegulationAlreadyEnabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.4
		void disableTimeRegulation()
			throw( TimeRegulationIsNotEnabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.5
		void enableTimeConstrained()
			throw( InTimeAdvancingState,
			       RequestForTimeConstrainedPending,
			       TimeConstrainedAlreadyEnabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.7
		void disableTimeConstrained()
			throw( TimeConstrainedIsNotEnabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.8
		void timeAdvanceRequest( const LogicalTime& theTime )
			throw( LogicalTimeAlreadyPassed,
			       InvalidLogicalTime,
			       InTimeAdvancingState,
			       RequestForTimeRegulationPending,
			       RequestForTimeConstrainedPending,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.9
		void timeAdvanceRequestAvailable( const LogicalTime& theTime )
			throw( LogicalTimeAlreadyPassed,
			       InvalidLogicalTime,
			       InTimeAdvancingState,
			       RequestForTimeRegulationPending,
			       RequestForTimeConstrainedPending,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.10
		void nextMessageRequest( const LogicalTime& theTime )
			throw( LogicalTimeAlreadyPassed,
			       InvalidLogicalTime,
			       InTimeAdvancingState,
			       RequestForTimeRegulationPending,
			       RequestForTimeConstrainedPending,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.11
		void nextMessageRequestAvailable( const LogicalTime& theTime )
			throw( LogicalTimeAlreadyPassed,
			       InvalidLogicalTime,
			       InTimeAdvancingState,
			       RequestForTimeRegulationPending,
			       RequestForTimeConstrainedPending,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.12
		void flushQueueRequest( const LogicalTime& theTime )
			throw( LogicalTimeAlreadyPassed,
			       InvalidLogicalTime,
			       InTimeAdvancingState,
			       RequestForTimeRegulationPending,
			       RequestForTimeConstrainedPending,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.14
		void enableAsynchronousDelivery()
			throw( AsynchronousDeliveryAlreadyEnabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.15
		void disableAsynchronousDelivery()
			throw( AsynchronousDeliveryAlreadyDisabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.16
		bool queryGALT( LogicalTime& theTime )
			throw( SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.17
		void queryLogicalTime( LogicalTime& theTime )
			throw( SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.18
		bool queryLITS( LogicalTime& theTime )
			throw( SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.19
		void modifyLookahead( const LogicalTimeInterval& theLookahead )
			throw( InvalidLookahead,
			       InTimeAdvancingState,
			       TimeRegulationIsNotEnabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.20
		void queryLookahead( LogicalTimeInterval& interval )
			throw( TimeRegulationIsNotEnabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.21
		void retract( MessageRetractionHandle theHandle )
			throw( MessageCanNoLongerBeRetracted,
			       InvalidMessageRetractionHandle,
			       TimeRegulationIsNotEnabled,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.23
		void changeAttributeOrderType( ObjectInstanceHandle theObject,
		                               const AttributeHandleSet& theAttributes,
		                               OrderType theType )
			throw( AttributeNotOwned,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 8.24
		void changeInteractionOrderType( InteractionClassHandle theClass,
		                                 OrderType theType )
			throw( InteractionClassNotPublished,
			       InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		
		//////////////////////////////////
		// Data Distribution Management //
		//////////////////////////////////
		// 9.2
		RegionHandle createRegion( const DimensionHandleSet& theDimensions )
			throw( InvalidDimensionHandle,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.3
		void commitRegionModifications( const RegionHandleSet& regionHandleSet )
			throw( RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.4
		void deleteRegion( const RegionHandle& theRegion )
			throw( RegionInUseForUpdateOrSubscription,
			       RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.5
		ObjectInstanceHandle
		registerObjectInstanceWithRegions( ObjectClassHandle theClass,
		                             const AttributeHandleSetRegionHandleSetPairVector& theVector )
			throw( InvalidRegionContext,
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
			       RTIinternalError );

		ObjectInstanceHandle
		registerObjectInstanceWithRegions( ObjectClassHandle theClass,
		                             const AttributeHandleSetRegionHandleSetPairVector& theVector,
		                             const std::wstring& objectName )
			throw( ObjectInstanceNameInUse,
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
			       RTIinternalError );

		// 9.6
		void associateRegionsForUpdates( ObjectInstanceHandle theObject,
		                           const AttributeHandleSetRegionHandleSetPairVector& theVector )
			throw( InvalidRegionContext,
			       RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.7
		void unassociateRegionsForUpdates( ObjectInstanceHandle theObject,
		                             const AttributeHandleSetRegionHandleSetPairVector& theVector )
			throw( RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       AttributeNotDefined,
			       ObjectInstanceNotKnown,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.8
		void subscribeObjectClassAttributesWithRegions(
			     ObjectClassHandle theClass,
		         const AttributeHandleSetRegionHandleSetPairVector& theVector,
		         bool active = true,
		         const std::wstring& updateRateDesignator = L"" )
			throw( InvalidRegionContext,
			       RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       AttributeNotDefined,
			       ObjectClassNotDefined,
			       InvalidUpdateRateDesignator,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.9
		void unsubscribeObjectClassAttributesWithRegions(
		         ObjectClassHandle theClass,
		         const AttributeHandleSetRegionHandleSetPairVector& theVector )
			throw( RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       AttributeNotDefined,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.10
		void subscribeInteractionClassWithRegions( InteractionClassHandle theClass,
		                                           const RegionHandleSet& regions,
		                                           bool active = true )
			throw( FederateServiceInvocationsAreBeingReportedViaMOM,
			       InvalidRegionContext,
			       RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.11
		void unsubscribeInteractionClassWithRegions( InteractionClassHandle theClass,
		                                             const RegionHandleSet& regions )
			throw( RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 9.12
		void sendInteractionWithRegions( InteractionClassHandle theClass,
		                                 const ParameterHandleValueMap& parameters,
		                                 const RegionHandleSet& regions,
		                                 const VariableLengthData& tag )
			throw( InvalidRegionContext,
			       RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       InteractionClassNotPublished,
			       InteractionParameterNotDefined,
			       InteractionClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		MessageRetractionHandle sendInteractionWithRegions( InteractionClassHandle theClass,
		                                                    const ParameterHandleValueMap& parameters,
		                                                    const RegionHandleSet& regions,
		                                                    const VariableLengthData& tag,
		                                                    const LogicalTime& theTime )
			throw( InvalidLogicalTime,
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
			       RTIinternalError );

		// 9.13
		void requestAttributeValueUpdateWithRegions( ObjectClassHandle theClass,
		                                       const AttributeHandleSetRegionHandleSetPairVector& theSet,
		                                       const VariableLengthData& tag )
			throw( InvalidRegionContext,
			       RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       AttributeNotDefined,
			       ObjectClassNotDefined,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		
		/////////////////////////////////////////////////////////////////////////////////
		// RTI Support Services /////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////
		// 10.2
		ResignAction getAutomaticResignDirective()
		    throw( FederateNotExecutionMember,
		        NotConnected,
		        RTIinternalError );

		// 10.3
		void setAutomaticResignDirective( ResignAction resignAction )
		    throw( InvalidResignAction,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.4
		FederateHandle getFederateHandle( const std::wstring& theName )
		    throw( NameNotFound,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.5
		std::wstring getFederateName( FederateHandle theHandle )
		    throw( InvalidFederateHandle,
		           FederateHandleNotKnown,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.6
		ObjectClassHandle getObjectClassHandle( const std::wstring& theName )
		    throw( NameNotFound,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.7
		std::wstring getObjectClassName( ObjectClassHandle theHandle )
		    throw( InvalidObjectClassHandle,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.8
		ObjectClassHandle getKnownObjectClassHandle( ObjectInstanceHandle theObject )
		    throw( ObjectInstanceNotKnown,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.9
		ObjectInstanceHandle getObjectInstanceHandle( const std::wstring& theName )
		    throw( ObjectInstanceNotKnown,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.10
		std::wstring getObjectInstanceName( ObjectInstanceHandle theHandle )
		    throw( ObjectInstanceNotKnown,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.11
		AttributeHandle getAttributeHandle( ObjectClassHandle whichClass,
		                                    const std::wstring& name )
			throw( NameNotFound,
			       InvalidObjectClassHandle,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 10.12
		std::wstring getAttributeName( ObjectClassHandle whichClass, AttributeHandle theHandle )
			throw( AttributeNotDefined,
			       InvalidAttributeHandle,
			       InvalidObjectClassHandle,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 10.13
		double getUpdateRateValue( const std::wstring& updateRateDesignator )
		    throw( InvalidUpdateRateDesignator,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.14
		double getUpdateRateValueForAttribute( ObjectInstanceHandle theObject,
		                                       AttributeHandle theAttribute )
			throw( ObjectInstanceNotKnown,
			       AttributeNotDefined,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 10.15
		InteractionClassHandle getInteractionClassHandle( const std::wstring& theName )
		    throw( NameNotFound,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.16
		std::wstring getInteractionClassName( InteractionClassHandle theHandle )
		    throw( InvalidInteractionClassHandle,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.17
		ParameterHandle getParameterHandle( InteractionClassHandle whichClass,
		                                    const std::wstring& theName )
			throw( NameNotFound,
			       InvalidInteractionClassHandle,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 10.18
		std::wstring getParameterName( InteractionClassHandle whichClass,
		                               ParameterHandle theHandle )
			throw( InteractionParameterNotDefined,
			       InvalidParameterHandle,
			       InvalidInteractionClassHandle,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 10.19
		OrderType getOrderType( const std::wstring& orderName )
		    throw( InvalidOrderName,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.20
		std::wstring getOrderName( OrderType orderType )
		    throw( InvalidOrderType,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.21
		TransportationType getTransportationType( const std::wstring& transportationName )
		    throw( InvalidTransportationName,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.22
		std::wstring getTransportationName( TransportationType transportationType )
		    throw( InvalidTransportationType,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.23
		DimensionHandleSet getAvailableDimensionsForClassAttribute( ObjectClassHandle theClass,
		                                                            AttributeHandle theHandle )
		throw( AttributeNotDefined,
			   InvalidAttributeHandle,
			   InvalidObjectClassHandle,
			   FederateNotExecutionMember,
			   NotConnected,
			   RTIinternalError );

		// 10.24
		DimensionHandleSet
		getAvailableDimensionsForInteractionClass( InteractionClassHandle theClass )
		    throw( InvalidInteractionClassHandle,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.25
		DimensionHandle getDimensionHandle( const std::wstring& theName )
		    throw( NameNotFound,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.26
		std::wstring getDimensionName( DimensionHandle theHandle )
		    throw( InvalidDimensionHandle,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.27
		unsigned long getDimensionUpperBound( DimensionHandle theHandle )
		    throw( InvalidDimensionHandle,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.28
		DimensionHandleSet getDimensionHandleSet( RegionHandle regionHandle )
		    throw( InvalidRegion,
		           SaveInProgress,
		           RestoreInProgress,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.29
		RangeBounds getRangeBounds( RegionHandle regionHandle,
		                            DimensionHandle dimensionHandle )
			throw( RegionDoesNotContainSpecifiedDimension,
				   InvalidRegion,
				   SaveInProgress,
				   RestoreInProgress,
				   FederateNotExecutionMember,
				   NotConnected,
				   RTIinternalError );

		// 10.30
		void setRangeBounds( RegionHandle regionHandle,
		                     DimensionHandle dimensionHandle,
		                     const RangeBounds& theRangeBounds )
			throw( InvalidRangeBound,
			       RegionDoesNotContainSpecifiedDimension,
			       RegionNotCreatedByThisFederate,
			       InvalidRegion,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 10.31
		unsigned long normalizeFederateHandle( FederateHandle theFederateHandle )
		    throw( InvalidFederateHandle,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.32
		unsigned long normalizeServiceGroup( ServiceGroup theServiceGroup )
		    throw( InvalidServiceGroup,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.33
		void enableObjectClassRelevanceAdvisorySwitch()
			throw( ObjectClassRelevanceAdvisorySwitchIsOn,
			       SaveInProgress,
			       RestoreInProgress,
			       FederateNotExecutionMember,
			       NotConnected,
			       RTIinternalError );

		// 10.34
		void disableObjectClassRelevanceAdvisorySwitch()
		    throw( ObjectClassRelevanceAdvisorySwitchIsOff,
		           SaveInProgress,
		           RestoreInProgress,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.35
		void enableAttributeRelevanceAdvisorySwitch()
		    throw( AttributeRelevanceAdvisorySwitchIsOn,
		           SaveInProgress,
		           RestoreInProgress,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.36
		void disableAttributeRelevanceAdvisorySwitch()
		    throw( AttributeRelevanceAdvisorySwitchIsOff,
		           SaveInProgress,
		           RestoreInProgress,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.37
		void enableAttributeScopeAdvisorySwitch()
		    throw( AttributeScopeAdvisorySwitchIsOn,
		           SaveInProgress,
		           RestoreInProgress,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.38
		void disableAttributeScopeAdvisorySwitch()
		    throw( AttributeScopeAdvisorySwitchIsOff,
		           SaveInProgress,
		           RestoreInProgress,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.39
		void enableInteractionRelevanceAdvisorySwitch()
		    throw( InteractionRelevanceAdvisorySwitchIsOn,
		           SaveInProgress,
		           RestoreInProgress,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.40
		void disableInteractionRelevanceAdvisorySwitch()
		    throw( InteractionRelevanceAdvisorySwitchIsOff,
		           SaveInProgress,
		           RestoreInProgress,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// 10.41
		bool evokeCallback( double minSeconds )
		    throw( CallNotAllowedFromWithinCallback, RTIinternalError );

		// 10.42
		bool evokeMultipleCallbacks( double minSeconds,
		                             double maxSeconds )
			throw( CallNotAllowedFromWithinCallback, RTIinternalError );

		// 10.43
		void enableCallbacks() throw( SaveInProgress,
		                              RestoreInProgress,
		                              RTIinternalError );

		// 10.44
		void disableCallbacks() throw( SaveInProgress,
		                               RestoreInProgress,
		                               RTIinternalError );

		/////////////////////////////////////////////////////////////////////////////////
		// API-specific services ////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////
		// Return instance of time factory being used by the federation
		std::auto_ptr<LogicalTimeFactory> getTimeFactory() const
		    throw( FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		// Decode handles
		FederateHandle decodeFederateHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		ObjectClassHandle decodeObjectClassHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		InteractionClassHandle
		decodeInteractionClassHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		ObjectInstanceHandle
		decodeObjectInstanceHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		AttributeHandle decodeAttributeHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		ParameterHandle decodeParameterHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		DimensionHandle decodeDimensionHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		MessageRetractionHandle
		decodeMessageRetractionHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

		RegionHandle decodeRegionHandle( const VariableLengthData& encodedValue ) const
		    throw( CouldNotDecode,
		           FederateNotExecutionMember,
		           NotConnected,
		           RTIinternalError );

	private:

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};

PORTICO1516E_NS_END

#endif /* PORTICORTIAMBASSADOR_H_ */
