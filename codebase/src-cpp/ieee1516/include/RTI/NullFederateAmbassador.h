/***********************************************************************
  IEEE 1516.1 High Level Architecture Interface Specification C++ API
  File: RTI/NullFederateAmbassador.h
***********************************************************************/

#ifndef RTI_NullFederateAmbassador_h
#define RTI_NullFederateAmbassador_h

#include <RTI/Exception.h>
#include <RTI/FederateAmbassador.h>

namespace rti1516
{
  class RTI_EXPORT NullFederateAmbassador : public FederateAmbassador
  {
  public:
    NullFederateAmbassador()
       throw (FederateInternalError) {}

    virtual
    ~NullFederateAmbassador()
    throw () {}

    // 4.7
    virtual
    void
    synchronizationPointRegistrationSucceeded(std::wstring const & label)
      throw (FederateInternalError) {}

    virtual
    void
    synchronizationPointRegistrationFailed(std::wstring const & label,
                                           SynchronizationFailureReason reason)
      throw (FederateInternalError) {}

    // 4.8
    virtual
    void
    announceSynchronizationPoint(std::wstring  const & label,
                                 VariableLengthData const & theUserSuppliedTag)
      throw (FederateInternalError) {}

    // 4.10
    virtual
    void
    federationSynchronized(std::wstring const & label)
      throw (FederateInternalError) {}

    // 4.12
    virtual
    void
    initiateFederateSave(std::wstring const & label)
      throw (UnableToPerformSave,
             FederateInternalError) {}

    virtual
    void
    initiateFederateSave(std::wstring const & label,
                         LogicalTime const & theTime)
      throw (UnableToPerformSave,
             InvalidLogicalTime,
             FederateInternalError) {}

    // 4.15
    virtual
    void
    federationSaved()
      throw (FederateInternalError) {}

    virtual
    void
    federationNotSaved(SaveFailureReason theSaveFailureReason)
      throw (FederateInternalError) {}


    // 4.17
    virtual
    void
    federationSaveStatusResponse(
      FederateHandleSaveStatusPairVector const & 
      theFederateStatusVector)
      throw (FederateInternalError) {}

    // 4.19
    virtual
    void
    requestFederationRestoreSucceeded(std::wstring const & label)
      throw (FederateInternalError) {}

    virtual
    void
    requestFederationRestoreFailed(std::wstring const & label)
      throw (FederateInternalError) {}

    // 4.20
    virtual
    void
    federationRestoreBegun()
      throw (FederateInternalError) {}

    // 4.21
    virtual
    void
    initiateFederateRestore(std::wstring         const & label,
                            FederateHandle handle)
      throw (SpecifiedSaveLabelDoesNotExist,
             CouldNotInitiateRestore,
             FederateInternalError) {}

    // 4.23
    virtual
    void
    federationRestored()
      throw (FederateInternalError) {}

    virtual
    void
    federationNotRestored(RestoreFailureReason theRestoreFailureReason)
      throw (FederateInternalError) {}

    // 4.25
    virtual
    void
    federationRestoreStatusResponse(
      FederateHandleRestoreStatusPairVector  const & 
      theFederateStatusVector)
      throw (FederateInternalError) {}

    /////////////////////////////////////
    // Declaration Management Services //
    /////////////////////////////////////
  
    // 5.10
    virtual
    void
    startRegistrationForObjectClass(ObjectClassHandle theClass)
      throw (ObjectClassNotPublished,
             FederateInternalError) {}

    // 5.11
    virtual
    void
    stopRegistrationForObjectClass(ObjectClassHandle theClass)
      throw (ObjectClassNotPublished,
             FederateInternalError) {}

    // 5.12
    virtual
    void
    turnInteractionsOn(InteractionClassHandle theHandle)
      throw (InteractionClassNotPublished,
             FederateInternalError) {}

    // 5.13
    virtual
    void
    turnInteractionsOff(InteractionClassHandle theHandle)
      throw (InteractionClassNotPublished,
             FederateInternalError) {}

    ////////////////////////////////
    // Object Management Services //
    ////////////////////////////////
  
    // 6.3
    virtual
    void
    objectInstanceNameReservationSucceeded(std::wstring const &
                                           theObjectInstanceName)
      throw (UnknownName,
             FederateInternalError) {}

    virtual
    void
    objectInstanceNameReservationFailed(std::wstring const &
                                        theObjectInstanceName)
      throw (UnknownName,
             FederateInternalError) {}

  
    // 6.5
    virtual
    void
    discoverObjectInstance(ObjectInstanceHandle theObject,
                           ObjectClassHandle theObjectClass,
                           std::wstring const & theObjectInstanceName)
      throw (CouldNotDiscover,
             ObjectClassNotKnown,
             FederateInternalError) {}

    // 6.7
    virtual
    void
    reflectAttributeValues
    (ObjectInstanceHandle theObject,
     AttributeHandleValueMap const & theAttributeValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotSubscribed,
             FederateInternalError) {}

    virtual
    void
    reflectAttributeValues
    (ObjectInstanceHandle theObject,
     AttributeHandleValueMap const & theAttributeValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     RegionHandleSet const & theSentRegionHandleSet)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotSubscribed,
             FederateInternalError) {}

    virtual
    void
    reflectAttributeValues
    (ObjectInstanceHandle theObject,
     AttributeHandleValueMap const & theAttributeValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     LogicalTime const & theTime,
     OrderType receivedOrder)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotSubscribed,
             FederateInternalError) {}
  
    virtual
    void
    reflectAttributeValues
    (ObjectInstanceHandle theObject,
     AttributeHandleValueMap const & theAttributeValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     LogicalTime const & theTime,
     OrderType receivedOrder,
     RegionHandleSet const & theSentRegionHandleSet)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotSubscribed,
             FederateInternalError) {}
  
    virtual
    void
    reflectAttributeValues
    (ObjectInstanceHandle theObject,
     AttributeHandleValueMap const & theAttributeValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     LogicalTime const & theTime,
     OrderType receivedOrder,
     MessageRetractionHandle theHandle)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotSubscribed,
             InvalidLogicalTime,
             FederateInternalError) {}

    virtual
    void
    reflectAttributeValues
    (ObjectInstanceHandle theObject,
     AttributeHandleValueMap const & theAttributeValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     LogicalTime const & theTime,
     OrderType receivedOrder,
     MessageRetractionHandle theHandle,
     RegionHandleSet const & theSentRegionHandleSet)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotSubscribed,
             InvalidLogicalTime,
             FederateInternalError) {}

    // 6.9
    virtual
    void
    receiveInteraction
    (InteractionClassHandle theInteraction,
     ParameterHandleValueMap const & theParameterValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType)
      throw (InteractionClassNotRecognized,
             InteractionParameterNotRecognized,
             InteractionClassNotSubscribed,
             FederateInternalError) {}

    virtual
    void
    receiveInteraction
    (InteractionClassHandle theInteraction,
     ParameterHandleValueMap const & theParameterValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     RegionHandleSet const & theSentRegionHandleSet)
      throw (InteractionClassNotRecognized,
             InteractionParameterNotRecognized,
             InteractionClassNotSubscribed,
             FederateInternalError) {}

    virtual
    void
    receiveInteraction
    (InteractionClassHandle theInteraction,
     ParameterHandleValueMap const & theParameterValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     LogicalTime const & theTime,
     OrderType receivedOrder)
      throw (InteractionClassNotRecognized,
             InteractionParameterNotRecognized,
             InteractionClassNotSubscribed,
             FederateInternalError) {}

    virtual
    void
    receiveInteraction
    (InteractionClassHandle theInteraction,
     ParameterHandleValueMap const & theParameterValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     LogicalTime const & theTime,
     OrderType receivedOrder,
     RegionHandleSet const & theSentRegionHandleSet)
      throw (InteractionClassNotRecognized,
             InteractionParameterNotRecognized,
             InteractionClassNotSubscribed,
             FederateInternalError) {}

    virtual
    void
    receiveInteraction
    (InteractionClassHandle theInteraction,
     ParameterHandleValueMap const & theParameterValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     LogicalTime const & theTime,
     OrderType receivedOrder,
     MessageRetractionHandle theHandle)
      throw (InteractionClassNotRecognized,
             InteractionParameterNotRecognized,
             InteractionClassNotSubscribed,
             InvalidLogicalTime,
             FederateInternalError) {}

    virtual
    void
    receiveInteraction
    (InteractionClassHandle theInteraction,
     ParameterHandleValueMap const & theParameterValues,
     VariableLengthData const & theUserSuppliedTag,
     OrderType sentOrder,
     TransportationType theType,
     LogicalTime const & theTime,
     OrderType receivedOrder,
     MessageRetractionHandle theHandle,
     RegionHandleSet const & theSentRegionHandleSet)
      throw (InteractionClassNotRecognized,
             InteractionParameterNotRecognized,
             InteractionClassNotSubscribed,
             InvalidLogicalTime,
             FederateInternalError) {}

    // 6.11
    virtual
    void
    removeObjectInstance(ObjectInstanceHandle theObject,
                         VariableLengthData const & theUserSuppliedTag,
                         OrderType sentOrder)
      throw (ObjectInstanceNotKnown,
             FederateInternalError) {}

    virtual
    void
    removeObjectInstance(ObjectInstanceHandle theObject,
                         VariableLengthData const & theUserSuppliedTag,
                         OrderType sentOrder,
                         LogicalTime const & theTime,
                         OrderType receivedOrder)
      throw (ObjectInstanceNotKnown,
             FederateInternalError) {}

    virtual
    void
    removeObjectInstance(ObjectInstanceHandle theObject,
                         VariableLengthData const & theUserSuppliedTag,
                         OrderType sentOrder,
                         LogicalTime const & theTime,
                         OrderType receivedOrder,
                         MessageRetractionHandle theHandle)
      throw (ObjectInstanceNotKnown,
             InvalidLogicalTime,
             FederateInternalError) {}

    // 6.15
    virtual
    void
    attributesInScope
    (ObjectInstanceHandle theObject,
      AttributeHandleSet const & theAttributes)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotSubscribed,
             FederateInternalError) {}

    // 6.16
    virtual
    void
    attributesOutOfScope
    (ObjectInstanceHandle theObject,
      AttributeHandleSet const & theAttributes)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotSubscribed,
             FederateInternalError) {}

    // 6.18
    virtual
    void
    provideAttributeValueUpdate
    (ObjectInstanceHandle theObject,
     AttributeHandleSet const & theAttributes,
     VariableLengthData const & theUserSuppliedTag)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotOwned,
             FederateInternalError) {}

    // 6.19
    virtual
    void
    turnUpdatesOnForObjectInstance
    (ObjectInstanceHandle theObject,
      AttributeHandleSet const & theAttributes)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotOwned,
             FederateInternalError) {}

    // 6.20
    virtual
    void
    turnUpdatesOffForObjectInstance
    (ObjectInstanceHandle theObject,
      AttributeHandleSet const & theAttributes)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotOwned,
             FederateInternalError) {}

    ///////////////////////////////////
    // Ownership Management Services //
    ///////////////////////////////////
  
    // 7.4
    virtual
    void
    requestAttributeOwnershipAssumption
    (ObjectInstanceHandle theObject,
     AttributeHandleSet const & offeredAttributes,
     VariableLengthData const & theUserSuppliedTag)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeAlreadyOwned,
             AttributeNotPublished,
             FederateInternalError) {}

    // 7.5
    virtual
    void
    requestDivestitureConfirmation
    (ObjectInstanceHandle theObject,
      AttributeHandleSet const & releasedAttributes)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotOwned,
             AttributeDivestitureWasNotRequested,
             FederateInternalError) {}

    // 7.7
    virtual
    void
    attributeOwnershipAcquisitionNotification
    (ObjectInstanceHandle theObject,
     AttributeHandleSet const & securedAttributes,
     VariableLengthData const & theUserSuppliedTag)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeAcquisitionWasNotRequested,
             AttributeAlreadyOwned,
             AttributeNotPublished,
             FederateInternalError) {}

    // 7.10
    virtual
    void
    attributeOwnershipUnavailable
    (ObjectInstanceHandle theObject,
      AttributeHandleSet const & theAttributes)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeAlreadyOwned,
             AttributeAcquisitionWasNotRequested,
             FederateInternalError) {}

    // 7.11
    virtual
    void
    requestAttributeOwnershipRelease
    (ObjectInstanceHandle theObject,
     AttributeHandleSet const & candidateAttributes,
     VariableLengthData const & theUserSuppliedTag)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeNotOwned,
             FederateInternalError) {}

    // 7.15
    virtual
    void
    confirmAttributeOwnershipAcquisitionCancellation
    (ObjectInstanceHandle theObject,
      AttributeHandleSet const & theAttributes)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             AttributeAlreadyOwned,
             AttributeAcquisitionWasNotCanceled,
             FederateInternalError) {}

    // 7.17
    virtual
    void
    informAttributeOwnership(ObjectInstanceHandle theObject,
                             AttributeHandle theAttribute,
                             FederateHandle theOwner)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             FederateInternalError) {}

    virtual
    void
    attributeIsNotOwned(ObjectInstanceHandle theObject,
                        AttributeHandle theAttribute)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             FederateInternalError) {}

    virtual
    void
    attributeIsOwnedByRTI(ObjectInstanceHandle theObject,
                          AttributeHandle theAttribute)
      throw (ObjectInstanceNotKnown,
             AttributeNotRecognized,
             FederateInternalError) {}

    //////////////////////////////
    // Time Management Services //
    //////////////////////////////
  
    // 8.3
    virtual
    void
    timeRegulationEnabled(LogicalTime const & theFederateTime)
      throw (InvalidLogicalTime,
             NoRequestToEnableTimeRegulationWasPending,
             FederateInternalError) {}

    // 8.6
    virtual
    void
    timeConstrainedEnabled(LogicalTime const & theFederateTime)
      throw (InvalidLogicalTime,
             NoRequestToEnableTimeConstrainedWasPending,
             FederateInternalError) {}

    // 8.13
    virtual
    void
    timeAdvanceGrant(LogicalTime const & theTime)
      throw (InvalidLogicalTime,
             JoinedFederateIsNotInTimeAdvancingState,
             FederateInternalError) {}

    // 8.22
    virtual
    void
    requestRetraction(MessageRetractionHandle theHandle)
      throw (FederateInternalError) {}
  };
}

#endif // RTI_NullFederateAmbassador_h
