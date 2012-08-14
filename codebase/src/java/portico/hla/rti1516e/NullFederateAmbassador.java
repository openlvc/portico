/*
 * The IEEE hereby grants a general, royalty-free license to copy, distribute,
 * display and make derivative works from this material, for all purposes,
 * provided that any use of the material contains the following
 * attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
 * Should you require additional information, contact the Manager, Standards
 * Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).
 */

package hla.rti1516e;

import hla.rti1516e.exceptions.FederateInternalError;

import java.util.Set;

public class NullFederateAmbassador implements FederateAmbassador {
   public void connectionLost(String faultDescription)
      throws FederateInternalError
   {
   }

   //4.7
   public void synchronizationPointRegistrationSucceeded(String synchronizationPointLabel)
      throws FederateInternalError
   {
   }

   public void synchronizationPointRegistrationFailed(String synchronizationPointLabel,
                                                      SynchronizationPointFailureReason reason)
      throws FederateInternalError
   {
   }

   //4.8
   public void announceSynchronizationPoint(String synchronizationPointLabel, byte[] userSuppliedTag)
      throws FederateInternalError
   {
   }

   //4.10
   public void federationSynchronized(String synchronizationPointLabel, FederateHandleSet failedToSyncSet)
      throws FederateInternalError
   {
   }

   //4.12
   public void initiateFederateSave(String label)
      throws FederateInternalError
   {
   }

   public void initiateFederateSave(String label, LogicalTime time)
      throws FederateInternalError
   {
   }

   // 4.15
   public void federationSaved()
      throws FederateInternalError
   {
   }

   public void federationNotSaved(SaveFailureReason reason)
      throws FederateInternalError
   {
   }

   // 4.17
   public void federationSaveStatusResponse(FederateHandleSaveStatusPair[] response)
      throws FederateInternalError
   {
   }

   // 4.19
   public void requestFederationRestoreSucceeded(String label)
      throws FederateInternalError
   {
   }

   public void requestFederationRestoreFailed(String label)
      throws FederateInternalError
   {
   }

   // 4.20
   public void federationRestoreBegun()
      throws FederateInternalError
   {
   }

   // 4.21
   public void initiateFederateRestore(String label, 
                                       String federateName, 
                                       FederateHandle federateHandle)
      throws FederateInternalError
   {
   }

   // 4.23
   public void federationRestored()
      throws FederateInternalError
   {
   }

   public void federationNotRestored(RestoreFailureReason reason)
      throws FederateInternalError
   {
   }

   // 4.25
   public void federationRestoreStatusResponse(FederateRestoreStatus[] response)
      throws FederateInternalError
   {
   }

   public void reportFederationExecutions(FederationExecutionInformationSet theFederationExecutionInformationSet)
      throws FederateInternalError
   {
   }

   // 5.10
   public void startRegistrationForObjectClass(ObjectClassHandle theClass)
      throws FederateInternalError
   {
   }

   // 5.11
   public void stopRegistrationForObjectClass(ObjectClassHandle theClass)
      throws FederateInternalError
   {
   }

   // 5.12
   public void turnInteractionsOn(InteractionClassHandle theHandle)
      throws FederateInternalError
   {
   }

   // 5.13
   public void turnInteractionsOff(InteractionClassHandle theHandle)
      throws FederateInternalError
   {
   }

   // 6.3
   public void objectInstanceNameReservationSucceeded(String objectName)
      throws FederateInternalError
   {
   }

   public void multipleObjectInstanceNameReservationSucceeded(Set<String> objectNames)
      throws FederateInternalError
   {
   }

   public void objectInstanceNameReservationFailed(String objectName)
      throws FederateInternalError
   {
   }

   public void multipleObjectInstanceNameReservationFailed(Set<String> objectNames)
      throws FederateInternalError
   {
   }

   // 6.5
   public void discoverObjectInstance(ObjectInstanceHandle theObject,
                                      ObjectClassHandle theObjectClass,
                                      String objectName)
      throws FederateInternalError
   {
   }

   public void discoverObjectInstance(ObjectInstanceHandle theObject,
                                      ObjectClassHandle theObjectClass,
                                      String objectName,
                                      FederateHandle producingFederate)
      throws FederateInternalError
   {
   }

   // 6.7
   public void reflectAttributeValues(ObjectInstanceHandle theObject,
                                      AttributeHandleValueMap theAttributes,
                                      byte[] userSuppliedTag,
                                      OrderType sentOrdering,
                                      TransportationTypeHandle theTransport,
                                      SupplementalReflectInfo reflectInfo)
      throws FederateInternalError
   {
   }

   public void reflectAttributeValues(ObjectInstanceHandle theObject,
                                      AttributeHandleValueMap theAttributes,
                                      byte[] userSuppliedTag,
                                      OrderType sentOrdering,
                                      TransportationTypeHandle theTransport,
                                      LogicalTime theTime,
                                      OrderType receivedOrdering,
                                      SupplementalReflectInfo reflectInfo)
      throws FederateInternalError
   {
   }

   public void reflectAttributeValues(ObjectInstanceHandle theObject,
                                      AttributeHandleValueMap theAttributes,
                                      byte[] userSuppliedTag,
                                      OrderType sentOrdering,
                                      TransportationTypeHandle theTransport,
                                      LogicalTime theTime,
                                      OrderType receivedOrdering,
                                      MessageRetractionHandle retractionHandle,
                                      SupplementalReflectInfo reflectInfo)
      throws FederateInternalError
   {
   }

   // 6.9

   public void receiveInteraction(InteractionClassHandle interactionClass,
                                  ParameterHandleValueMap theParameters,
                                  byte[] userSuppliedTag,
                                  OrderType sentOrdering,
                                  TransportationTypeHandle theTransport,
                                  SupplementalReceiveInfo receiveInfo)
      throws FederateInternalError
   {
   }

   public void receiveInteraction(InteractionClassHandle interactionClass,
                                  ParameterHandleValueMap theParameters,
                                  byte[] userSuppliedTag,
                                  OrderType sentOrdering,
                                  TransportationTypeHandle theTransport,
                                  LogicalTime theTime,
                                  OrderType receivedOrdering,
                                  SupplementalReceiveInfo receiveInfo)
      throws FederateInternalError
   {
   }

   public void receiveInteraction(InteractionClassHandle interactionClass,
                                  ParameterHandleValueMap theParameters,
                                  byte[] userSuppliedTag,
                                  OrderType sentOrdering,
                                  TransportationTypeHandle theTransport,
                                  LogicalTime theTime,
                                  OrderType receivedOrdering,
                                  MessageRetractionHandle retractionHandle,
                                  SupplementalReceiveInfo receiveInfo)
      throws FederateInternalError
   {
   }

   // 6.11
   public void removeObjectInstance(ObjectInstanceHandle theObject,
                                    byte[] userSuppliedTag,
                                    OrderType sentOrdering,
                                    SupplementalRemoveInfo removeInfo)
      throws FederateInternalError
   {
   }

   public void removeObjectInstance(ObjectInstanceHandle theObject,
                                    byte[] userSuppliedTag,
                                    OrderType sentOrdering,
                                    LogicalTime theTime,
                                    OrderType receivedOrdering,
                                    SupplementalRemoveInfo removeInfo)
      throws FederateInternalError
   {
   }

   public void removeObjectInstance(ObjectInstanceHandle theObject,
                                    byte[] userSuppliedTag,
                                    OrderType sentOrdering,
                                    LogicalTime theTime,
                                    OrderType receivedOrdering,
                                    MessageRetractionHandle retractionHandle,
                                    SupplementalRemoveInfo removeInfo)
      throws FederateInternalError
   {
   }

   // 6.15
   public void attributesInScope(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes)
      throws FederateInternalError
   {
   }

   // 6.16
   public void attributesOutOfScope(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes)
      throws FederateInternalError
   {
   }

   // 6.18
   public void provideAttributeValueUpdate(ObjectInstanceHandle theObject,
                                           AttributeHandleSet theAttributes,
                                           byte[] userSuppliedTag)
      throws FederateInternalError
   {
   }

   // 6.19
   public void turnUpdatesOnForObjectInstance(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes)
      throws FederateInternalError
   {
   }

   public void turnUpdatesOnForObjectInstance(ObjectInstanceHandle theObject,
                                              AttributeHandleSet theAttributes,
                                              String updateRateDesignator)
      throws FederateInternalError
   {
   }

   // 6.20
   public void turnUpdatesOffForObjectInstance(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes)
      throws FederateInternalError
   {
   }

   // 6.20
   public void confirmAttributeTransportationTypeChange(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes,
                                                        TransportationTypeHandle theTransportation)
      throws
      FederateInternalError
   {
   }

   // 6.20
   public void confirmInteractionTransportationTypeChange(InteractionClassHandle theInteraction,
                                                          TransportationTypeHandle theTransportation)
      throws
      FederateInternalError
   {
   }

   // 6.20
   public void reportAttributeTransportationType(ObjectInstanceHandle theObject, AttributeHandle theAttribute,
                                                 TransportationTypeHandle theTransportation)
      throws
      FederateInternalError
   {
   }

   // 6.20
   public void reportInteractionTransportationType(FederateHandle theFederate, InteractionClassHandle theInteraction,
                                                   TransportationTypeHandle theTransportation)
      throws
      FederateInternalError
   {
   }

   // 7.4
   public void requestAttributeOwnershipAssumption(ObjectInstanceHandle theObject,
                                                   AttributeHandleSet offeredAttributes,
                                                   byte[] userSuppliedTag)
      throws FederateInternalError
   {
   }

   // 7.5
   public void requestDivestitureConfirmation(ObjectInstanceHandle theObject, AttributeHandleSet offeredAttributes)
      throws FederateInternalError
   {
   }

   // 7.7
   public void attributeOwnershipAcquisitionNotification(ObjectInstanceHandle theObject,
                                                         AttributeHandleSet securedAttributes,
                                                         byte[] userSuppliedTag)
      throws FederateInternalError
   {
   }

   // 7.10
   public void attributeOwnershipUnavailable(ObjectInstanceHandle theObject, AttributeHandleSet theAttributes)
      throws FederateInternalError
   {
   }

   // 7.11
   public void requestAttributeOwnershipRelease(ObjectInstanceHandle theObject,
                                                AttributeHandleSet candidateAttributes,
                                                byte[] userSuppliedTag)
      throws FederateInternalError
   {
   }

   // 7.15
   public void confirmAttributeOwnershipAcquisitionCancellation(ObjectInstanceHandle theObject,
                                                                AttributeHandleSet theAttributes)
      throws FederateInternalError
   {
   }

   // 7.17
   public void informAttributeOwnership(ObjectInstanceHandle theObject,
                                        AttributeHandle theAttribute,
                                        FederateHandle theOwner)
      throws FederateInternalError
   {
   }

   public void attributeIsNotOwned(ObjectInstanceHandle theObject, AttributeHandle theAttribute)
      throws FederateInternalError
   {
   }

   public void attributeIsOwnedByRTI(ObjectInstanceHandle theObject, AttributeHandle theAttribute)
      throws FederateInternalError
   {
   }

   // 8.3
   public void timeRegulationEnabled(LogicalTime time)
      throws FederateInternalError
   {
   }

   // 8.6
   public void timeConstrainedEnabled(LogicalTime time)
      throws FederateInternalError
   {
   }

   // 8.13
   public void timeAdvanceGrant(LogicalTime theTime)
      throws FederateInternalError
   {
   }

   // 8.22
   public void requestRetraction(MessageRetractionHandle theHandle)
      throws FederateInternalError
   {
   }
}
