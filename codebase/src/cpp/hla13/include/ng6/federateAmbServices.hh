//File federateAmbServices.hh
//Included in RTI.hh

//                RTI Parameter Passing Memory Conventions
//
// C1  In parameter by value.
// C2  Out parameter by pointer value.
// C3  Function return by value.
// C4  In parameter by const pointer value.  Caller provides memory.
//     Caller may free memory or overwrite it upon completion of
//     the call.  Callee must copy during the call anything it
//     wishes to save beyond completion of the call.  Parameter
//     type must define const accessor methods.
// C5  Out parameter by pointer value.  Caller provides reference to object.
//     Callee constructs an instance on the heap (new) and returns.
//     The caller destroys the instance (delete) at its leisure.
// C6  Function return by pointer value.  Callee constructs an instance on
//     the heap (new) and returns a reference.  The caller destroys the
//     instance (delete) at its leisure.
//

////////////////////////////////////
// Federation Management Services //
////////////////////////////////////

// 4.7
virtual void synchronizationPointRegistrationSucceeded (
  const char *label) // supplied C4)
throw (
  FederateInternalError) = 0;

virtual void synchronizationPointRegistrationFailed (
  const char *label) // supplied C4)
throw (
  FederateInternalError) = 0;

// 4.8
virtual void announceSynchronizationPoint (
  const char *label, // supplied C4
  const char *tag)   // supplied C4
throw (
  FederateInternalError) = 0;

// 4.10
virtual void federationSynchronized (
  const char *label) // supplied C4)
throw (
  FederateInternalError) = 0;

// 4.12
virtual void initiateFederateSave (
  const char *label) // supplied C4
throw (
  UnableToPerformSave,
  FederateInternalError) = 0;

// 4.15
virtual void federationSaved ()
throw (
  FederateInternalError) = 0;

virtual void federationNotSaved ()
throw (
  FederateInternalError) = 0;

// 4.17
virtual void requestFederationRestoreSucceeded (
  const char *label) // supplied C4
throw (
  FederateInternalError) = 0;

virtual void requestFederationRestoreFailed (
  const char *label,  // supplied C4
  const char *reason) // supplied C4
throw (
  FederateInternalError) = 0;

// 4.18
virtual void federationRestoreBegun ()
throw (
  FederateInternalError) = 0;

// 4.19
virtual void initiateFederateRestore (
  const char *label,   // supplied C4
        FederateHandle handle)  // supplied C1
throw (
  SpecifiedSaveLabelDoesNotExist,
  CouldNotRestore,
  FederateInternalError) = 0;

// 4.21
virtual void federationRestored ()
throw (
  FederateInternalError) = 0;

virtual void federationNotRestored ()
throw (
  FederateInternalError) = 0;

/////////////////////////////////////
// Declaration Management Services //
/////////////////////////////////////

// 5.10
virtual void startRegistrationForObjectClass (
        ObjectClassHandle   theClass)      // supplied C1
throw (
  ObjectClassNotPublished,
  FederateInternalError) = 0;

// 5.11
virtual void stopRegistrationForObjectClass (
        ObjectClassHandle   theClass)      // supplied C1
throw (
  ObjectClassNotPublished,
  FederateInternalError) = 0;

// 5.12
virtual void turnInteractionsOn (
  InteractionClassHandle theHandle) // supplied C1
throw (
  InteractionClassNotPublished,
  FederateInternalError) = 0;

// 5.13
virtual void turnInteractionsOff (
  InteractionClassHandle theHandle) // supplied C1
throw (
  InteractionClassNotPublished,
  FederateInternalError) = 0;

////////////////////////////////
// Object Management Services //
////////////////////////////////

// 6.3
virtual void discoverObjectInstance (
        ObjectHandle          theObject,      // supplied C1
        ObjectClassHandle     theObjectClass, // supplied C1
  const char *                theObjectName)  // supplied C4
throw (
  CouldNotDiscover,
  ObjectClassNotKnown,
  FederateInternalError) = 0;

// 6.5
virtual void reflectAttributeValues (
        ObjectHandle                 theObject,     // supplied C1
  const AttributeHandleValuePairSet& theAttributes, // supplied C4
  const FedTime&                     theTime,       // supplied C1
  const char                        *theTag,        // supplied C4
        EventRetractionHandle        theHandle)     // supplied C1
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  FederateOwnsAttributes,
  InvalidFederationTime,
  FederateInternalError) = 0;

virtual void reflectAttributeValues (
        ObjectHandle                 theObject,     // supplied C1
  const AttributeHandleValuePairSet& theAttributes, // supplied C4
  const char                        *theTag)        // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  FederateOwnsAttributes,
  FederateInternalError) = 0;

// 6.7
virtual void receiveInteraction (
        InteractionClassHandle       theInteraction, // supplied C1
  const ParameterHandleValuePairSet& theParameters,  // supplied C4
  const FedTime&                     theTime,        // supplied C4
  const char                        *theTag,         // supplied C4
        EventRetractionHandle        theHandle)      // supplied C1
throw (
  InteractionClassNotKnown,
  InteractionParameterNotKnown,
  InvalidFederationTime,
  FederateInternalError) = 0;

virtual void receiveInteraction (
        InteractionClassHandle       theInteraction, // supplied C1
  const ParameterHandleValuePairSet& theParameters,  // supplied C4
  const char                        *theTag)         // supplied C4
throw (
  InteractionClassNotKnown,
  InteractionParameterNotKnown,
  FederateInternalError) = 0;

// 6.9
virtual void removeObjectInstance (
        ObjectHandle          theObject, // supplied C1
  const FedTime&              theTime,   // supplied C4
  const char                 *theTag,    // supplied C4
        EventRetractionHandle theHandle) // supplied C1
throw (
  ObjectNotKnown,
  InvalidFederationTime,
  FederateInternalError) = 0;

virtual void removeObjectInstance (
        ObjectHandle          theObject, // supplied C1
  const char                 *theTag)    // supplied C4
throw (
  ObjectNotKnown,
  FederateInternalError) = 0;

// 6.13
virtual void attributesInScope (
        ObjectHandle        theObject,     // supplied C1
  const AttributeHandleSet& theAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  FederateInternalError) = 0;

// 6.14
virtual void attributesOutOfScope (
        ObjectHandle        theObject,     // supplied C1
  const AttributeHandleSet& theAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  FederateInternalError) = 0;

// 6.16
virtual void provideAttributeValueUpdate (
        ObjectHandle        theObject,     // supplied C1
  const AttributeHandleSet& theAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  AttributeNotOwned,
  FederateInternalError) = 0;

// 6.17
virtual void turnUpdatesOnForObjectInstance (
        ObjectHandle        theObject,     // supplied C1
  const AttributeHandleSet& theAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotOwned,
  FederateInternalError) = 0;

// 6.18
virtual void turnUpdatesOffForObjectInstance (
        ObjectHandle        theObject,      // supplied C1
  const AttributeHandleSet& theAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotOwned,
  FederateInternalError) = 0;

///////////////////////////////////
// Ownership Management Services //
///////////////////////////////////

// 7.4
virtual void requestAttributeOwnershipAssumption (
        ObjectHandle        theObject,         // supplied C1
  const AttributeHandleSet& offeredAttributes, // supplied C4
  const char               *theTag)            // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  AttributeAlreadyOwned,
  AttributeNotPublished,
  FederateInternalError) = 0;

// 7.5
virtual void attributeOwnershipDivestitureNotification (
        ObjectHandle        theObject,          // supplied C1
  const AttributeHandleSet& releasedAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  AttributeNotOwned,
  AttributeDivestitureWasNotRequested,
  FederateInternalError) = 0;

// 7.6
virtual void attributeOwnershipAcquisitionNotification (
        ObjectHandle        theObject,         // supplied C1
  const AttributeHandleSet& securedAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  AttributeAcquisitionWasNotRequested,
  AttributeAlreadyOwned,
  AttributeNotPublished,
  FederateInternalError) = 0;

// 7.9
virtual void attributeOwnershipUnavailable (
        ObjectHandle        theObject,         // supplied C1
  const AttributeHandleSet& theAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  AttributeNotDefined,
  AttributeAlreadyOwned,
  AttributeAcquisitionWasNotRequested,
  FederateInternalError) = 0;

// 7.10
virtual void requestAttributeOwnershipRelease (
        ObjectHandle        theObject,           // supplied C1
  const AttributeHandleSet& candidateAttributes, // supplied C4
  const char               *theTag)              // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  AttributeNotOwned,
  FederateInternalError) = 0;

// 7.14
virtual void confirmAttributeOwnershipAcquisitionCancellation (
        ObjectHandle        theObject,         // supplied C1
  const AttributeHandleSet& theAttributes) // supplied C4
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  AttributeNotDefined,
  AttributeAlreadyOwned,
  AttributeAcquisitionWasNotCanceled,
  FederateInternalError) = 0;

// 7.16
virtual void informAttributeOwnership (
  ObjectHandle    theObject,    // supplied C1
  AttributeHandle theAttribute, // supplied C1
  FederateHandle  theOwner)     // supplied C1
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  FederateInternalError) = 0;

virtual void attributeIsNotOwned (
  ObjectHandle    theObject,    // supplied C1
  AttributeHandle theAttribute) // supplied C1
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  FederateInternalError) = 0;

virtual void attributeOwnedByRTI (
  ObjectHandle    theObject,    // supplied C1
  AttributeHandle theAttribute) // supplied C1
throw (
  ObjectNotKnown,
  AttributeNotKnown,
  FederateInternalError) = 0;

//////////////////////////////
// Time Management Services //
//////////////////////////////

// 8.3
virtual void timeRegulationEnabled (
 const  FedTime& theFederateTime) // supplied C4
throw (
  InvalidFederationTime,
  EnableTimeRegulationWasNotPending,
  FederateInternalError) = 0;

// 8.6
virtual void timeConstrainedEnabled (
  const FedTime& theFederateTime) // supplied C4
throw (
  InvalidFederationTime,
  EnableTimeConstrainedWasNotPending,
  FederateInternalError) = 0;

// 8.13
virtual void timeAdvanceGrant (
  const FedTime& theTime) // supplied C4
throw (
  InvalidFederationTime,
  TimeAdvanceWasNotInProgress,
  FederationTimeAlreadyPassed,
  FederateInternalError) = 0;

// 8.22
virtual void requestRetraction (
  EventRetractionHandle theHandle) // supplied C1
throw (
  EventNotKnown,
  FederateInternalError) = 0;


virtual ~FederateAmbassador()
throw (FederateInternalError) { ; }
