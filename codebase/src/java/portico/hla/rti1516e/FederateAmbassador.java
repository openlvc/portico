/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

//File: FederateAmbassador.java

package hla.rti1516e;

import hla.rti1516e.exceptions.FederateInternalError;

import java.util.Set;


/**
 * Federate must implement this interface.
 */

public interface FederateAmbassador {

////////////////////////////////////
// Federation Management Services //
////////////////////////////////////

   // 4.4
   void connectionLost(String faultDescription)
      throws
      FederateInternalError;

   // 4.8
   void reportFederationExecutions(FederationExecutionInformationSet theFederationExecutionInformationSet)
      throws
      FederateInternalError;

   //4.12
   void synchronizationPointRegistrationSucceeded(String synchronizationPointLabel)
      throws
      FederateInternalError;

   //4.12
   void synchronizationPointRegistrationFailed(String synchronizationPointLabel,
                                               SynchronizationPointFailureReason reason)
      throws
      FederateInternalError;

   //4.13
   void announceSynchronizationPoint(String synchronizationPointLabel,
                                     byte[] userSuppliedTag)
      throws
      FederateInternalError;

   //4.15
   void federationSynchronized(String synchronizationPointLabel,
                               FederateHandleSet failedToSyncSet)
      throws
      FederateInternalError;

   //4.17
   void initiateFederateSave(String label)
      throws
      FederateInternalError;

   //4.17
   void initiateFederateSave(String label,
                             LogicalTime time)
      throws
      FederateInternalError;

   // 4.20
   void federationSaved()
      throws
      FederateInternalError;

   // 4.20
   void federationNotSaved(SaveFailureReason reason)
      throws
      FederateInternalError;

   // 4.23
   void federationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
      throws
      FederateInternalError;

   // 4.25
   void requestFederationRestoreSucceeded(String label)
      throws
      FederateInternalError;

   // 4.25
   void requestFederationRestoreFailed(String label)
      throws
      FederateInternalError;

   // 4.26
   void federationRestoreBegun()
      throws
      FederateInternalError;

   // 4.27
   void initiateFederateRestore(String label,
                                String federateName,
                                FederateHandle federateHandle)
      throws
      FederateInternalError;

   // 4.29
   void federationRestored()
      throws
      FederateInternalError;

   // 4.29
   void federationNotRestored(RestoreFailureReason reason)
      throws
      FederateInternalError;

   // 4.32
   void federationRestoreStatusResponse(FederateRestoreStatus[] response)
      throws
      FederateInternalError;

/////////////////////////////////////
// Declaration Management Services //
/////////////////////////////////////

   // 5.10
   void startRegistrationForObjectClass(ObjectClassHandle theClass)
      throws
      FederateInternalError;

   // 5.11
   void stopRegistrationForObjectClass(ObjectClassHandle theClass)
      throws
      FederateInternalError;

   // 5.12
   void turnInteractionsOn(InteractionClassHandle theHandle)
      throws
      FederateInternalError;

   // 5.13
   void turnInteractionsOff(InteractionClassHandle theHandle)
      throws
      FederateInternalError;

////////////////////////////////
// Object Management Services //
////////////////////////////////

   // 6.3
   void objectInstanceNameReservationSucceeded(String objectName)
      throws
      FederateInternalError;

   // 6.3
   void objectInstanceNameReservationFailed(String objectName)
      throws
      FederateInternalError;

   // 6.6
   void multipleObjectInstanceNameReservationSucceeded(Set<String> objectNames)
      throws
      FederateInternalError;

   // 6.6
   void multipleObjectInstanceNameReservationFailed(Set<String> objectNames)
      throws
      FederateInternalError;

   // 6.9
   void discoverObjectInstance(ObjectInstanceHandle theObject,
                               ObjectClassHandle theObjectClass,
                               String objectName)
      throws
      FederateInternalError;

   // 6.9
   void discoverObjectInstance(ObjectInstanceHandle theObject,
                               ObjectClassHandle theObjectClass,
                               String objectName,
                               FederateHandle producingFederate)
      throws
      FederateInternalError;

   interface SupplementalReflectInfo {
      boolean hasProducingFederate();

      boolean hasSentRegions();

      FederateHandle getProducingFederate();

      RegionHandleSet getSentRegions();
   }

   // 6.11
   void reflectAttributeValues(ObjectInstanceHandle theObject,
                               AttributeHandleValueMap theAttributes,
                               byte[] userSuppliedTag,
                               OrderType sentOrdering,
                               TransportationTypeHandle theTransport,
                               SupplementalReflectInfo reflectInfo)
      throws
      FederateInternalError;

   // 6.11
   void reflectAttributeValues(ObjectInstanceHandle theObject,
                               AttributeHandleValueMap theAttributes,
                               byte[] userSuppliedTag,
                               OrderType sentOrdering,
                               TransportationTypeHandle theTransport,
                               LogicalTime theTime,
                               OrderType receivedOrdering,
                               SupplementalReflectInfo reflectInfo)
      throws
      FederateInternalError;

   // 6.11
   void reflectAttributeValues(ObjectInstanceHandle theObject,
                               AttributeHandleValueMap theAttributes,
                               byte[] userSuppliedTag,
                               OrderType sentOrdering,
                               TransportationTypeHandle theTransport,
                               LogicalTime theTime,
                               OrderType receivedOrdering,
                               MessageRetractionHandle retractionHandle,
                               SupplementalReflectInfo reflectInfo)
      throws
      FederateInternalError;

   interface SupplementalReceiveInfo {
      boolean hasProducingFederate();

      boolean hasSentRegions();

      FederateHandle getProducingFederate();

      RegionHandleSet getSentRegions();
   }

   // 6.13
   void receiveInteraction(InteractionClassHandle interactionClass,
                           ParameterHandleValueMap theParameters,
                           byte[] userSuppliedTag,
                           OrderType sentOrdering,
                           TransportationTypeHandle theTransport,
                           SupplementalReceiveInfo receiveInfo)
      throws
      FederateInternalError;

   // 6.13
   void receiveInteraction(InteractionClassHandle interactionClass,
                           ParameterHandleValueMap theParameters,
                           byte[] userSuppliedTag,
                           OrderType sentOrdering,
                           TransportationTypeHandle theTransport,
                           LogicalTime theTime,
                           OrderType receivedOrdering,
                           SupplementalReceiveInfo receiveInfo)
      throws
      FederateInternalError;

   // 6.13
   void receiveInteraction(InteractionClassHandle interactionClass,
                           ParameterHandleValueMap theParameters,
                           byte[] userSuppliedTag,
                           OrderType sentOrdering,
                           TransportationTypeHandle theTransport,
                           LogicalTime theTime,
                           OrderType receivedOrdering,
                           MessageRetractionHandle retractionHandle,
                           SupplementalReceiveInfo receiveInfo)
      throws
      FederateInternalError;

   interface SupplementalRemoveInfo {
      boolean hasProducingFederate();

      FederateHandle getProducingFederate();
   }

   // 6.15
   void removeObjectInstance(ObjectInstanceHandle theObject,
                             byte[] userSuppliedTag,
                             OrderType sentOrdering,
                             SupplementalRemoveInfo removeInfo)
      throws
      FederateInternalError;

   // 6.15
   void removeObjectInstance(ObjectInstanceHandle theObject,
                             byte[] userSuppliedTag,
                             OrderType sentOrdering,
                             LogicalTime theTime,
                             OrderType receivedOrdering,
                             SupplementalRemoveInfo removeInfo)
      throws
      FederateInternalError;

   // 6.15
   void removeObjectInstance(ObjectInstanceHandle theObject,
                             byte[] userSuppliedTag,
                             OrderType sentOrdering,
                             LogicalTime theTime,
                             OrderType receivedOrdering,
                             MessageRetractionHandle retractionHandle,
                             SupplementalRemoveInfo removeInfo)
      throws
      FederateInternalError;

   // 6.17
   void attributesInScope(ObjectInstanceHandle theObject,
                          AttributeHandleSet theAttributes)
      throws
      FederateInternalError;

   // 6.18
   void attributesOutOfScope(ObjectInstanceHandle theObject,
                             AttributeHandleSet theAttributes)
      throws
      FederateInternalError;

   // 6.20
   void provideAttributeValueUpdate(ObjectInstanceHandle theObject,
                                    AttributeHandleSet theAttributes,
                                    byte[] userSuppliedTag)
      throws
      FederateInternalError;

   // 6.21
   void turnUpdatesOnForObjectInstance(ObjectInstanceHandle theObject,
                                       AttributeHandleSet theAttributes)
      throws
      FederateInternalError;

   // 6.21
   void turnUpdatesOnForObjectInstance(ObjectInstanceHandle theObject,
                                       AttributeHandleSet theAttributes,
                                       String updateRateDesignator)
      throws
      FederateInternalError;

   // 6.22
   void turnUpdatesOffForObjectInstance(ObjectInstanceHandle theObject,
                                        AttributeHandleSet theAttributes)
      throws
      FederateInternalError;

   // 6.24
   void confirmAttributeTransportationTypeChange(ObjectInstanceHandle theObject,
                                                 AttributeHandleSet theAttributes,
                                                 TransportationTypeHandle theTransportation)
      throws
      FederateInternalError;

   // 6.26
   void reportAttributeTransportationType(ObjectInstanceHandle theObject,
                                          AttributeHandle theAttribute,
                                          TransportationTypeHandle theTransportation)
      throws
      FederateInternalError;

   // 6.28
   void confirmInteractionTransportationTypeChange(InteractionClassHandle theInteraction,
                                                   TransportationTypeHandle theTransportation)
      throws
      FederateInternalError;

   // 6.30
   void reportInteractionTransportationType(FederateHandle theFederate,
                                            InteractionClassHandle theInteraction,
                                            TransportationTypeHandle theTransportation)
      throws
      FederateInternalError;

///////////////////////////////////
// Ownership Management Services //
///////////////////////////////////

   // 7.4
   void requestAttributeOwnershipAssumption(ObjectInstanceHandle theObject,
                                            AttributeHandleSet offeredAttributes,
                                            byte[] userSuppliedTag)
      throws
      FederateInternalError;

   // 7.5
   void requestDivestitureConfirmation(ObjectInstanceHandle theObject,
                                       AttributeHandleSet offeredAttributes)
      throws
      FederateInternalError;

   // 7.7
   void attributeOwnershipAcquisitionNotification(ObjectInstanceHandle theObject,
                                                  AttributeHandleSet securedAttributes,
                                                  byte[] userSuppliedTag)
      throws
      FederateInternalError;

   // 7.10
   void attributeOwnershipUnavailable(ObjectInstanceHandle theObject,
                                      AttributeHandleSet theAttributes)
      throws
      FederateInternalError;

   // 7.11
   void requestAttributeOwnershipRelease(ObjectInstanceHandle theObject,
                                         AttributeHandleSet candidateAttributes,
                                         byte[] userSuppliedTag)
      throws
      FederateInternalError;

   // 7.16
   void confirmAttributeOwnershipAcquisitionCancellation(ObjectInstanceHandle theObject,
                                                         AttributeHandleSet theAttributes)
      throws
      FederateInternalError;

   // 7.18
   void informAttributeOwnership(ObjectInstanceHandle theObject,
                                 AttributeHandle theAttribute,
                                 FederateHandle theOwner)
      throws
      FederateInternalError;

   // 7.18
   void attributeIsNotOwned(ObjectInstanceHandle theObject,
                            AttributeHandle theAttribute)
      throws
      FederateInternalError;

   // 7.18
   void attributeIsOwnedByRTI(ObjectInstanceHandle theObject,
                              AttributeHandle theAttribute)
      throws
      FederateInternalError;

//////////////////////////////
// Time Management Services //
//////////////////////////////

   // 8.3
   void timeRegulationEnabled(LogicalTime time)
      throws
      FederateInternalError;

   // 8.6
   void timeConstrainedEnabled(LogicalTime time)
      throws
      FederateInternalError;

   // 8.13
   void timeAdvanceGrant(LogicalTime theTime)
      throws
      FederateInternalError;

   // 8.22
   void requestRetraction(MessageRetractionHandle theHandle)
      throws
      FederateInternalError;
}

//end FederateAmbassador

