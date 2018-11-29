/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: RTIambassador.java

package hla.rti1516e;

import hla.rti1516e.exceptions.*;

import java.net.URL;
import java.util.Set;

/**
 Memory Management Conventions for Parameters

 All Java parameters, including object references, are passed by value.
 Therefore there is no need to specify further conventions for primitive types.

 Unless otherwise noted, reference parameters adhere to the following convention:
 The referenced object is created (or acquired) by the caller. The callee must
 copy during the call anything it wishes to save beyond the completion of the
 call.

 Unless otherwise noted, a reference returned from a method represents a new
 object created by the callee. The caller is free to modify the object whose
 reference is returned.


 */

/**
 * The RTI presents this interface to the federate.
 * RTI implementer must implement this.
 */

public interface RTIambassador {

////////////////////////////////////
// Federation Management Services //
////////////////////////////////////

   // 4.2
   void connect(FederateAmbassador federateReference,
                CallbackModel callbackModel,
                String localSettingsDesignator)
      throws
      ConnectionFailed,
      InvalidLocalSettingsDesignator,
      UnsupportedCallbackModel,
      AlreadyConnected,
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   // 4.2
   void connect(FederateAmbassador federateReference,
                CallbackModel callbackModel)
      throws
      ConnectionFailed,
      InvalidLocalSettingsDesignator,
      UnsupportedCallbackModel,
      AlreadyConnected,
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   // 4.3
   void disconnect()
      throws
      FederateIsExecutionMember,
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   //4.5
   void createFederationExecution(String federationExecutionName,
                                  URL[] fomModules,
                                  URL mimModule,
                                  String logicalTimeImplementationName)
      throws
      CouldNotCreateLogicalTimeFactory,
      InconsistentFDD,
      ErrorReadingFDD,
      CouldNotOpenFDD,
      ErrorReadingMIM,
      CouldNotOpenMIM,
      DesignatorIsHLAstandardMIM,
      FederationExecutionAlreadyExists,
      NotConnected,
      RTIinternalError;

   //4.5
   void createFederationExecution(String federationExecutionName,
                                  URL[] fomModules,
                                  String logicalTimeImplementationName)
      throws
      CouldNotCreateLogicalTimeFactory,
      InconsistentFDD,
      ErrorReadingFDD,
      CouldNotOpenFDD,
      FederationExecutionAlreadyExists,
      NotConnected,
      RTIinternalError;

   //4.5
   void createFederationExecution(String federationExecutionName,
                                  URL[] fomModules,
                                  URL mimModule)
      throws
      InconsistentFDD,
      ErrorReadingFDD,
      CouldNotOpenFDD,
      ErrorReadingMIM,
      CouldNotOpenMIM,
      DesignatorIsHLAstandardMIM,
      FederationExecutionAlreadyExists,
      NotConnected,
      RTIinternalError;

   //4.5
   void createFederationExecution(String federationExecutionName,
                                  URL[] fomModules)
      throws
      InconsistentFDD,
      ErrorReadingFDD,
      CouldNotOpenFDD,
      FederationExecutionAlreadyExists,
      NotConnected,
      RTIinternalError;

   //4.5
   void createFederationExecution(String federationExecutionName,
                                  URL fomModule)
      throws
      InconsistentFDD,
      ErrorReadingFDD,
      CouldNotOpenFDD,
      FederationExecutionAlreadyExists,
      NotConnected,
      RTIinternalError;

   //4.6
   void destroyFederationExecution(String federationExecutionName)
      throws
      FederatesCurrentlyJoined,
      FederationExecutionDoesNotExist,
      NotConnected,
      RTIinternalError;

   // 4.7
   void listFederationExecutions()
      throws
      NotConnected,
      RTIinternalError;

   //4.9
   FederateHandle joinFederationExecution(String federateName,
                                          String federateType,
                                          String federationExecutionName,
                                          URL[] additionalFomModules)
      throws
      CouldNotCreateLogicalTimeFactory,
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
      RTIinternalError;

   //4.9
   FederateHandle joinFederationExecution(String federateType,
                                          String federationExecutionName,
                                          URL[] additionalFomModules)
      throws
      CouldNotCreateLogicalTimeFactory,
      FederationExecutionDoesNotExist,
      InconsistentFDD,
      ErrorReadingFDD,
      CouldNotOpenFDD,
      SaveInProgress,
      RestoreInProgress,
      FederateAlreadyExecutionMember,
      NotConnected,
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   //4.9
   FederateHandle joinFederationExecution(String federateName,
                                          String federateType,
                                          String federationExecutionName)
      throws
      CouldNotCreateLogicalTimeFactory,
      FederateNameAlreadyInUse,
      FederationExecutionDoesNotExist,
      SaveInProgress,
      RestoreInProgress,
      FederateAlreadyExecutionMember,
      NotConnected,
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   //4.9
   FederateHandle joinFederationExecution(String federateType,
                                          String federationExecutionName)
      throws
      CouldNotCreateLogicalTimeFactory,
      FederationExecutionDoesNotExist,
      SaveInProgress,
      RestoreInProgress,
      FederateAlreadyExecutionMember,
      NotConnected,
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   //4.10
   void resignFederationExecution(ResignAction resignAction)
      throws
      InvalidResignAction,
      OwnershipAcquisitionPending,
      FederateOwnsAttributes,
      FederateNotExecutionMember,
      NotConnected,
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   //4.11
   void registerFederationSynchronizationPoint(String synchronizationPointLabel,
                                               byte[] userSuppliedTag)
      throws
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   //4.11
   void registerFederationSynchronizationPoint(String synchronizationPointLabel,
                                               byte[] userSuppliedTag,
                                               FederateHandleSet synchronizationSet)
      throws
      InvalidFederateHandle,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   //4.14
   void synchronizationPointAchieved(String synchronizationPointLabel)
      throws
      SynchronizationPointLabelNotAnnounced,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   //4.14
   void synchronizationPointAchieved(String synchronizationPointLabel,
                                     boolean successIndicator)
      throws
      SynchronizationPointLabelNotAnnounced,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.16
   void requestFederationSave(String label)
      throws
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.16
   void requestFederationSave(String label,
                              LogicalTime theTime)
      throws
      LogicalTimeAlreadyPassed,
      InvalidLogicalTime,
      FederateUnableToUseTime,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.18
   void federateSaveBegun()
      throws
      SaveNotInitiated,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.19
   void federateSaveComplete()
      throws
      FederateHasNotBegunSave,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.19
   void federateSaveNotComplete()
      throws
      FederateHasNotBegunSave,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.21
   void abortFederationSave()
      throws
      SaveNotInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.22
   void queryFederationSaveStatus()
      throws
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.24
   void requestFederationRestore(String label)
      throws
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.28
   void federateRestoreComplete()
      throws
      RestoreNotRequested,
      SaveInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.28
   void federateRestoreNotComplete()
      throws
      RestoreNotRequested,
      SaveInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.30
   void abortFederationRestore()
      throws
      RestoreNotInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 4.31
   void queryFederationRestoreStatus()
      throws
      SaveInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;


/////////////////////////////////////
// Declaration Management Services //
/////////////////////////////////////

   // 5.2
   void publishObjectClassAttributes(ObjectClassHandle theClass,
                                     AttributeHandleSet attributeList)
      throws
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.3
   void unpublishObjectClass(ObjectClassHandle theClass)
      throws
      OwnershipAcquisitionPending,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.3
   void unpublishObjectClassAttributes(ObjectClassHandle theClass,
                                       AttributeHandleSet attributeList)
      throws
      OwnershipAcquisitionPending,
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.4
   void publishInteractionClass(InteractionClassHandle theInteraction)
      throws
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.5
   void unpublishInteractionClass(InteractionClassHandle theInteraction)
      throws
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.6
   void subscribeObjectClassAttributes(ObjectClassHandle theClass,
                                       AttributeHandleSet attributeList)
      throws
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.6
   void subscribeObjectClassAttributes(ObjectClassHandle theClass,
                                       AttributeHandleSet attributeList,
                                       String updateRateDesignator)
      throws
      AttributeNotDefined,
      ObjectClassNotDefined,
      InvalidUpdateRateDesignator,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.6
   void subscribeObjectClassAttributesPassively(ObjectClassHandle theClass,
                                                AttributeHandleSet attributeList)
      throws
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.6
   void subscribeObjectClassAttributesPassively(ObjectClassHandle theClass,
                                                AttributeHandleSet attributeList,
                                                String updateRateDesignator)
      throws
      AttributeNotDefined,
      ObjectClassNotDefined,
      InvalidUpdateRateDesignator,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.7
   void unsubscribeObjectClass(ObjectClassHandle theClass)
      throws
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.7
   void unsubscribeObjectClassAttributes(ObjectClassHandle theClass,
                                         AttributeHandleSet attributeList)
      throws
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.8
   void subscribeInteractionClass(InteractionClassHandle theClass)
      throws
      FederateServiceInvocationsAreBeingReportedViaMOM,
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.8
   void subscribeInteractionClassPassively(InteractionClassHandle theClass)
      throws
      FederateServiceInvocationsAreBeingReportedViaMOM,
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 5.9
   void unsubscribeInteractionClass(InteractionClassHandle theClass)
      throws
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

////////////////////////////////
// Object Management Services //
////////////////////////////////

   // 6.2
   void reserveObjectInstanceName(String theObjectName)
      throws
      IllegalName,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.4
   void releaseObjectInstanceName(String theObjectInstanceName)
      throws
      ObjectInstanceNameNotReserved,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.5
   void reserveMultipleObjectInstanceName(Set<String> theObjectNames)
      throws
      IllegalName,
      NameSetWasEmpty,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.7
   void releaseMultipleObjectInstanceName(Set<String> theObjectNames)
      throws
      ObjectInstanceNameNotReserved,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.8
   ObjectInstanceHandle registerObjectInstance(ObjectClassHandle theClass)
      throws
      ObjectClassNotPublished,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.8
   ObjectInstanceHandle registerObjectInstance(ObjectClassHandle theClass,
                                               String theObjectName)
      throws
      ObjectInstanceNameInUse,
      ObjectInstanceNameNotReserved,
      ObjectClassNotPublished,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.10
   void updateAttributeValues(ObjectInstanceHandle theObject,
                              AttributeHandleValueMap theAttributes,
                              byte[] userSuppliedTag)
      throws
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.10
   MessageRetractionReturn updateAttributeValues(ObjectInstanceHandle theObject,
                                                 AttributeHandleValueMap theAttributes,
                                                 byte[] userSuppliedTag,
                                                 LogicalTime theTime)
      throws
      InvalidLogicalTime,
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.12
   void sendInteraction(InteractionClassHandle theInteraction,
                        ParameterHandleValueMap theParameters,
                        byte[] userSuppliedTag)
      throws
      InteractionClassNotPublished,
      InteractionParameterNotDefined,
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.12
   MessageRetractionReturn sendInteraction(InteractionClassHandle theInteraction,
                                           ParameterHandleValueMap theParameters,
                                           byte[] userSuppliedTag,
                                           LogicalTime theTime)
      throws
      InvalidLogicalTime,
      InteractionClassNotPublished,
      InteractionParameterNotDefined,
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.14
   void deleteObjectInstance(ObjectInstanceHandle objectHandle,
                             byte[] userSuppliedTag)
      throws
      DeletePrivilegeNotHeld,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.14
   MessageRetractionReturn deleteObjectInstance(ObjectInstanceHandle objectHandle,
                                                byte[] userSuppliedTag,
                                                LogicalTime theTime)
      throws
      InvalidLogicalTime,
      DeletePrivilegeNotHeld,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.16
   void localDeleteObjectInstance(ObjectInstanceHandle objectHandle)
      throws
      OwnershipAcquisitionPending,
      FederateOwnsAttributes,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.19
   void requestAttributeValueUpdate(ObjectInstanceHandle theObject,
                                    AttributeHandleSet theAttributes,
                                    byte[] userSuppliedTag)
      throws
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.19
   void requestAttributeValueUpdate(ObjectClassHandle theClass,
                                    AttributeHandleSet theAttributes,
                                    byte[] userSuppliedTag)
      throws
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.23
   void requestAttributeTransportationTypeChange(ObjectInstanceHandle theObject,
                                                 AttributeHandleSet theAttributes,
                                                 TransportationTypeHandle theType)
      throws
      AttributeAlreadyBeingChanged,
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      InvalidTransportationType,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.25
   void queryAttributeTransportationType(ObjectInstanceHandle theObject,
                                         AttributeHandle theAttribute)
      throws
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.27
   void requestInteractionTransportationTypeChange(InteractionClassHandle theClass,
                                                   TransportationTypeHandle theType)
      throws
      InteractionClassAlreadyBeingChanged,
      InteractionClassNotPublished,
      InteractionClassNotDefined,
      InvalidTransportationType,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 6.29
   void queryInteractionTransportationType(FederateHandle theFederate,
                                           InteractionClassHandle theInteraction)
      throws
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

///////////////////////////////////
// Ownership Management Services //
///////////////////////////////////

   // 7.2
   void unconditionalAttributeOwnershipDivestiture(ObjectInstanceHandle theObject,
                                                   AttributeHandleSet theAttributes)
      throws
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.3
   void negotiatedAttributeOwnershipDivestiture(ObjectInstanceHandle theObject,
                                                AttributeHandleSet theAttributes,
                                                byte[] userSuppliedTag)
      throws
      AttributeAlreadyBeingDivested,
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.6
   void confirmDivestiture(ObjectInstanceHandle theObject,
                           AttributeHandleSet theAttributes,
                           byte[] userSuppliedTag)
      throws
      NoAcquisitionPending,
      AttributeDivestitureWasNotRequested,
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.8
   void attributeOwnershipAcquisition(ObjectInstanceHandle theObject,
                                      AttributeHandleSet desiredAttributes,
                                      byte[] userSuppliedTag)
      throws
      AttributeNotPublished,
      ObjectClassNotPublished,
      FederateOwnsAttributes,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.9
   void attributeOwnershipAcquisitionIfAvailable(ObjectInstanceHandle theObject,
                                                 AttributeHandleSet desiredAttributes)
      throws
      AttributeAlreadyBeingAcquired,
      AttributeNotPublished,
      ObjectClassNotPublished,
      FederateOwnsAttributes,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.12
   void attributeOwnershipReleaseDenied(ObjectInstanceHandle theObject,
                                        AttributeHandleSet theAttributes)
      throws
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.13
   AttributeHandleSet attributeOwnershipDivestitureIfWanted(ObjectInstanceHandle theObject,
                                                            AttributeHandleSet theAttributes)
      throws
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.14
   void cancelNegotiatedAttributeOwnershipDivestiture(ObjectInstanceHandle theObject,
                                                      AttributeHandleSet theAttributes)
      throws
      AttributeDivestitureWasNotRequested,
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.15
   void cancelAttributeOwnershipAcquisition(ObjectInstanceHandle theObject,
                                            AttributeHandleSet theAttributes)
      throws
      AttributeAcquisitionWasNotRequested,
      AttributeAlreadyOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.17
   void queryAttributeOwnership(ObjectInstanceHandle theObject,
                                AttributeHandle theAttribute)
      throws
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 7.19
   boolean isAttributeOwnedByFederate(ObjectInstanceHandle theObject,
                                      AttributeHandle theAttribute)
      throws
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

//////////////////////////////
// Time Management Services //
//////////////////////////////

   // 8.2
   void enableTimeRegulation(LogicalTimeInterval theLookahead)
      throws
      InvalidLookahead,
      InTimeAdvancingState,
      RequestForTimeRegulationPending,
      TimeRegulationAlreadyEnabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.4
   void disableTimeRegulation()
      throws
      TimeRegulationIsNotEnabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.5
   void enableTimeConstrained()
      throws
      InTimeAdvancingState,
      RequestForTimeConstrainedPending,
      TimeConstrainedAlreadyEnabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.7
   void disableTimeConstrained()
      throws
      TimeConstrainedIsNotEnabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.8
   void timeAdvanceRequest(LogicalTime theTime)
      throws
      LogicalTimeAlreadyPassed,
      InvalidLogicalTime,
      InTimeAdvancingState,
      RequestForTimeRegulationPending,
      RequestForTimeConstrainedPending,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.9
   void timeAdvanceRequestAvailable(LogicalTime theTime)
      throws
      LogicalTimeAlreadyPassed,
      InvalidLogicalTime,
      InTimeAdvancingState,
      RequestForTimeRegulationPending,
      RequestForTimeConstrainedPending,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.10
   void nextMessageRequest(LogicalTime theTime)
      throws
      LogicalTimeAlreadyPassed,
      InvalidLogicalTime,
      InTimeAdvancingState,
      RequestForTimeRegulationPending,
      RequestForTimeConstrainedPending,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.11
   void nextMessageRequestAvailable(LogicalTime theTime)
      throws
      LogicalTimeAlreadyPassed,
      InvalidLogicalTime,
      InTimeAdvancingState,
      RequestForTimeRegulationPending,
      RequestForTimeConstrainedPending,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.12
   void flushQueueRequest(LogicalTime theTime)
      throws
      LogicalTimeAlreadyPassed,
      InvalidLogicalTime,
      InTimeAdvancingState,
      RequestForTimeRegulationPending,
      RequestForTimeConstrainedPending,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.14
   void enableAsynchronousDelivery()
      throws
      AsynchronousDeliveryAlreadyEnabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.15
   void disableAsynchronousDelivery()
      throws
      AsynchronousDeliveryAlreadyDisabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.16
   TimeQueryReturn queryGALT()
      throws
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.17
   LogicalTime queryLogicalTime()
      throws
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.18
   TimeQueryReturn queryLITS()
      throws
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.19
   void modifyLookahead(LogicalTimeInterval theLookahead)
      throws
      InvalidLookahead,
      InTimeAdvancingState,
      TimeRegulationIsNotEnabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.20
   LogicalTimeInterval queryLookahead()
      throws
      TimeRegulationIsNotEnabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.21
   void retract(MessageRetractionHandle theHandle)
      throws
      MessageCanNoLongerBeRetracted,
      InvalidMessageRetractionHandle,
      TimeRegulationIsNotEnabled,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.23
   void changeAttributeOrderType(ObjectInstanceHandle theObject,
                                 AttributeHandleSet theAttributes,
                                 OrderType theType)
      throws
      AttributeNotOwned,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 8.24
   void changeInteractionOrderType(InteractionClassHandle theClass,
                                   OrderType theType)
      throws
      InteractionClassNotPublished,
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

//////////////////////////////////
// Data Distribution Management //
//////////////////////////////////

   // 9.2
   RegionHandle createRegion(DimensionHandleSet dimensions)
      throws
      InvalidDimensionHandle,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.3
   void commitRegionModifications(RegionHandleSet regions)
      throws
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.4
   void deleteRegion(RegionHandle theRegion)
      throws
      RegionInUseForUpdateOrSubscription,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   //9.5
   ObjectInstanceHandle registerObjectInstanceWithRegions(ObjectClassHandle theClass,
                                                          AttributeSetRegionSetPairList attributesAndRegions)
      throws
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
      RTIinternalError;

   //9.5
   ObjectInstanceHandle registerObjectInstanceWithRegions(ObjectClassHandle theClass,
                                                          AttributeSetRegionSetPairList attributesAndRegions,
                                                          String theObject)
      throws
      ObjectInstanceNameInUse,
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
      RTIinternalError;

   // 9.6
   void associateRegionsForUpdates(ObjectInstanceHandle theObject,
                                   AttributeSetRegionSetPairList attributesAndRegions)
      throws
      InvalidRegionContext,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.7
   void unassociateRegionsForUpdates(ObjectInstanceHandle theObject,
                                     AttributeSetRegionSetPairList attributesAndRegions)
      throws
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      AttributeNotDefined,
      ObjectInstanceNotKnown,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.8
   void subscribeObjectClassAttributesWithRegions(ObjectClassHandle theClass,
                                                  AttributeSetRegionSetPairList attributesAndRegions)
      throws
      InvalidRegionContext,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.8
   void subscribeObjectClassAttributesWithRegions(ObjectClassHandle theClass,
                                                  AttributeSetRegionSetPairList attributesAndRegions,
                                                  String updateRateDesignator)
      throws
      InvalidRegionContext,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      AttributeNotDefined,
      ObjectClassNotDefined,
      InvalidUpdateRateDesignator,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.8
   void subscribeObjectClassAttributesPassivelyWithRegions(ObjectClassHandle theClass,
                                                           AttributeSetRegionSetPairList attributesAndRegions)
      throws
      InvalidRegionContext,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.8
   void subscribeObjectClassAttributesPassivelyWithRegions(ObjectClassHandle theClass,
                                                           AttributeSetRegionSetPairList attributesAndRegions,
                                                           String updateRateDesignator)
      throws
      InvalidRegionContext,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      AttributeNotDefined,
      ObjectClassNotDefined,
      InvalidUpdateRateDesignator,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.9
   void unsubscribeObjectClassAttributesWithRegions(ObjectClassHandle theClass,
                                                    AttributeSetRegionSetPairList attributesAndRegions)
      throws
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.10
   void subscribeInteractionClassWithRegions(InteractionClassHandle theClass,
                                             RegionHandleSet regions)
      throws
      FederateServiceInvocationsAreBeingReportedViaMOM,
      InvalidRegionContext,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.10
   void subscribeInteractionClassPassivelyWithRegions(InteractionClassHandle theClass,
                                                      RegionHandleSet regions)
      throws
      FederateServiceInvocationsAreBeingReportedViaMOM,
      InvalidRegionContext,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 9.11
   void unsubscribeInteractionClassWithRegions(InteractionClassHandle theClass,
                                               RegionHandleSet regions)
      throws
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      InteractionClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   //9.12
   void sendInteractionWithRegions(InteractionClassHandle theInteraction,
                                   ParameterHandleValueMap theParameters,
                                   RegionHandleSet regions,
                                   byte[] userSuppliedTag)
      throws
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
      RTIinternalError;

   //9.12
   MessageRetractionReturn sendInteractionWithRegions(InteractionClassHandle theInteraction,
                                                      ParameterHandleValueMap theParameters,
                                                      RegionHandleSet regions,
                                                      byte[] userSuppliedTag,
                                                      LogicalTime theTime)
      throws
      InvalidLogicalTime,
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
      RTIinternalError;

   // 9.13
   void requestAttributeValueUpdateWithRegions(ObjectClassHandle theClass,
                                               AttributeSetRegionSetPairList attributesAndRegions,
                                               byte[] userSuppliedTag)
      throws
      InvalidRegionContext,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      AttributeNotDefined,
      ObjectClassNotDefined,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

//////////////////////////
// RTI Support Services //
//////////////////////////

   // 10.2
   ResignAction getAutomaticResignDirective()
      throws
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.3
   void setAutomaticResignDirective(ResignAction resignAction)
      throws
      InvalidResignAction,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.4
   FederateHandle getFederateHandle(String theName)
      throws
      NameNotFound,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.5
   String getFederateName(FederateHandle theHandle)
      throws
      InvalidFederateHandle,
      FederateHandleNotKnown,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.6
   ObjectClassHandle getObjectClassHandle(String theName)
      throws
      NameNotFound,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.7
   String getObjectClassName(ObjectClassHandle theHandle)
      throws
      InvalidObjectClassHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.8
   ObjectClassHandle getKnownObjectClassHandle(ObjectInstanceHandle theObject)
      throws
      ObjectInstanceNotKnown,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.9
   ObjectInstanceHandle getObjectInstanceHandle(String theName)
      throws
      ObjectInstanceNotKnown,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.10
   String getObjectInstanceName(ObjectInstanceHandle theHandle)
      throws
      ObjectInstanceNotKnown,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.11
   AttributeHandle getAttributeHandle(ObjectClassHandle whichClass,
                                      String theName)
      throws
      NameNotFound,
      InvalidObjectClassHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.12
   String getAttributeName(ObjectClassHandle whichClass,
                           AttributeHandle theHandle)
      throws
      AttributeNotDefined,
      InvalidAttributeHandle,
      InvalidObjectClassHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.13
   double getUpdateRateValue(String updateRateDesignator)
      throws
      InvalidUpdateRateDesignator,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.14
   double getUpdateRateValueForAttribute(ObjectInstanceHandle theObject,
                                         AttributeHandle theAttribute)
      throws
      ObjectInstanceNotKnown,
      AttributeNotDefined,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.15
   InteractionClassHandle getInteractionClassHandle(String theName)
      throws
      NameNotFound,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.16
   String getInteractionClassName(InteractionClassHandle theHandle)
      throws
      InvalidInteractionClassHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.17
   ParameterHandle getParameterHandle(InteractionClassHandle whichClass,
                                      String theName)
      throws
      NameNotFound,
      InvalidInteractionClassHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.18
   String getParameterName(InteractionClassHandle whichClass,
                           ParameterHandle theHandle)
      throws
      InteractionParameterNotDefined,
      InvalidParameterHandle,
      InvalidInteractionClassHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.19
   OrderType getOrderType(String theName)
      throws
      InvalidOrderName,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.20
   String getOrderName(OrderType theType)
      throws
      InvalidOrderType,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.21
   TransportationTypeHandle getTransportationTypeHandle(String theName)
      throws
      InvalidTransportationName,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.22
   String getTransportationTypeName(TransportationTypeHandle theHandle)
      throws
      InvalidTransportationType,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.23
   DimensionHandleSet getAvailableDimensionsForClassAttribute(ObjectClassHandle whichClass,
                                                              AttributeHandle theHandle)
      throws
      AttributeNotDefined,
      InvalidAttributeHandle,
      InvalidObjectClassHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.24
   DimensionHandleSet getAvailableDimensionsForInteractionClass(InteractionClassHandle theHandle)
      throws
      InvalidInteractionClassHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.25
   DimensionHandle getDimensionHandle(String theName)
      throws
      NameNotFound,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.26
   String getDimensionName(DimensionHandle theHandle)
      throws
      InvalidDimensionHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.27
   long getDimensionUpperBound(DimensionHandle theHandle)
      throws
      InvalidDimensionHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.28
   DimensionHandleSet getDimensionHandleSet(RegionHandle region)
      throws
      InvalidRegion,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.29
   RangeBounds getRangeBounds(RegionHandle region,
                              DimensionHandle dimension)
      throws
      RegionDoesNotContainSpecifiedDimension,
      InvalidRegion,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.30
   void setRangeBounds(RegionHandle region,
                       DimensionHandle dimension,
                       RangeBounds bounds)
      throws
      InvalidRangeBound,
      RegionDoesNotContainSpecifiedDimension,
      RegionNotCreatedByThisFederate,
      InvalidRegion,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.31
   long normalizeFederateHandle(FederateHandle federateHandle)
      throws
      InvalidFederateHandle,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.32
   long normalizeServiceGroup(ServiceGroup group)
      throws
      InvalidServiceGroup,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.33
   void enableObjectClassRelevanceAdvisorySwitch()
      throws
      ObjectClassRelevanceAdvisorySwitchIsOn,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.34
   void disableObjectClassRelevanceAdvisorySwitch()
      throws
      ObjectClassRelevanceAdvisorySwitchIsOff,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.35
   void enableAttributeRelevanceAdvisorySwitch()
      throws
      AttributeRelevanceAdvisorySwitchIsOn,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.36
   void disableAttributeRelevanceAdvisorySwitch()
      throws
      AttributeRelevanceAdvisorySwitchIsOff,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.37
   void enableAttributeScopeAdvisorySwitch()
      throws
      AttributeScopeAdvisorySwitchIsOn,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.38
   void disableAttributeScopeAdvisorySwitch()
      throws
      AttributeScopeAdvisorySwitchIsOff,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.39
   void enableInteractionRelevanceAdvisorySwitch()
      throws
      InteractionRelevanceAdvisorySwitchIsOn,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.40
   void disableInteractionRelevanceAdvisorySwitch()
      throws
      InteractionRelevanceAdvisorySwitchIsOff,
      SaveInProgress,
      RestoreInProgress,
      FederateNotExecutionMember,
      NotConnected,
      RTIinternalError;

   // 10.41
   boolean evokeCallback(double approximateMinimumTimeInSeconds)
      throws
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   // 10.42
   boolean evokeMultipleCallbacks(double approximateMinimumTimeInSeconds,
                                  double approximateMaximumTimeInSeconds)
      throws
      CallNotAllowedFromWithinCallback,
      RTIinternalError;

   // 10.43
   void enableCallbacks()
      throws
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError;

   // 10.44
   void disableCallbacks()
      throws
      SaveInProgress,
      RestoreInProgress,
      RTIinternalError;

   //API-specific services
   AttributeHandleFactory getAttributeHandleFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   AttributeHandleSetFactory getAttributeHandleSetFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   AttributeSetRegionSetPairListFactory getAttributeSetRegionSetPairListFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   DimensionHandleFactory getDimensionHandleFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   DimensionHandleSetFactory getDimensionHandleSetFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   FederateHandleFactory getFederateHandleFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   FederateHandleSetFactory getFederateHandleSetFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   InteractionClassHandleFactory getInteractionClassHandleFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   ObjectClassHandleFactory getObjectClassHandleFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   ParameterHandleFactory getParameterHandleFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   RegionHandleSetFactory getRegionHandleSetFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   TransportationTypeHandleFactory getTransportationTypeHandleFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;

   String getHLAversion();

   LogicalTimeFactory getTimeFactory()
      throws
      FederateNotExecutionMember,
      NotConnected;
}

//end RTIambassador


