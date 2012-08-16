//File federateAmbServices13.h
//Included in RTI13.h

////////////////////////////////////
// Federation Management Services //
////////////////////////////////////
// 4.7
virtual void synchronizationPointRegistrationSucceeded (const char *label)
	throw (FederateInternalError) = 0;

virtual void synchronizationPointRegistrationFailed (const char *label)
	throw (FederateInternalError) = 0;

// 4.8
virtual void announceSynchronizationPoint (const char *label, const char *tag)
	throw (FederateInternalError) = 0;

// 4.10
virtual void federationSynchronized (const char *label)
	throw (FederateInternalError) = 0;

// 4.12
virtual void initiateFederateSave (const char *label)
	throw (UnableToPerformSave, FederateInternalError) = 0;

// 4.15
virtual void federationSaved ()
	throw (FederateInternalError) = 0;

virtual void federationNotSaved () 
	throw (FederateInternalError) = 0;

// 4.17
virtual void requestFederationRestoreSucceeded (const char *label)
	throw (FederateInternalError) = 0;

virtual void requestFederationRestoreFailed (const char *label, const char *reason)
	throw (FederateInternalError) = 0;

// 4.18
virtual void federationRestoreBegun () 
	throw (FederateInternalError) = 0;

// 4.19
virtual void initiateFederateRestore (const char *label, FederateHandle handle)
	throw (SpecifiedSaveLabelDoesNotExist, CouldNotRestore, FederateInternalError) = 0;
	
// 4.21
virtual void federationRestored ()
	throw (FederateInternalError) = 0;

virtual void federationNotRestored ()
	throw (FederateInternalError) = 0;

/////////////////////////////////////
// Declaration Management Services //
/////////////////////////////////////
// 5.10
virtual void startRegistrationForObjectClass (ObjectClassHandle theClass)
	throw (ObjectClassNotPublished, FederateInternalError) = 0;

// 5.11
virtual void stopRegistrationForObjectClass (ObjectClassHandle theClass)
	throw (ObjectClassNotPublished, FederateInternalError) = 0;
	
// 5.12
virtual void turnInteractionsOn (InteractionClassHandle theHandle)
	throw (InteractionClassNotPublished, FederateInternalError) = 0;

// 5.13
virtual void turnInteractionsOff (InteractionClassHandle theHandle)
	throw (InteractionClassNotPublished, FederateInternalError) = 0;
	
////////////////////////////////
// Object Management Services //
////////////////////////////////
// 6.3
virtual void discoverObjectInstance (ObjectHandle theObject, ObjectClassHandle theObjectClass, const char * theObjectName)
	throw (CouldNotDiscover, ObjectClassNotKnown, FederateInternalError) = 0;
	
// 6.5
virtual void reflectAttributeValues (ObjectHandle theObject, const AttributeHandleValuePairSet& theAttributes, 
	const FedTime& theTime, const char *theTag, EventRetractionHandle theHandle)
	throw (ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, InvalidFederationTime, FederateInternalError) = 0;

virtual void reflectAttributeValues (ObjectHandle theObject, const AttributeHandleValuePairSet& theAttributes, const char *theTag)
	throw (ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, FederateInternalError) = 0;

// 6.7
virtual void receiveInteraction (InteractionClassHandle theInteraction, const ParameterHandleValuePairSet& theParameters, 
	const FedTime& theTime, const char *theTag, EventRetractionHandle theHandle)
	throw (InteractionClassNotKnown, InteractionParameterNotKnown, InvalidFederationTime, FederateInternalError) = 0;
	
virtual void receiveInteraction (InteractionClassHandle theInteraction, const ParameterHandleValuePairSet& theParameters,
	const char *theTag)
	throw (InteractionClassNotKnown, InteractionParameterNotKnown, FederateInternalError) = 0;
	
// 6.9
virtual void removeObjectInstance (ObjectHandle theObject, const FedTime& theTime, const char *theTag, 
	EventRetractionHandle theHandle)
	throw (ObjectNotKnown, InvalidFederationTime, FederateInternalError) = 0;

virtual void removeObjectInstance (ObjectHandle theObject, const char *theTag)
	throw (ObjectNotKnown, FederateInternalError) = 0;
	
// 6.13
virtual void attributesInScope (ObjectHandle theObject, const AttributeHandleSet& theAttributes) 
	throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) = 0;

// 6.14
virtual void attributesOutOfScope (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) = 0;
	
// 6.16
virtual void provideAttributeValueUpdate (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError) = 0;

// 6.17
virtual void turnUpdatesOnForObjectInstance (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotOwned, FederateInternalError) = 0;

// 6.18
virtual void turnUpdatesOffForObjectInstance (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotOwned, FederateInternalError) = 0;
	
///////////////////////////////////
// Ownership Management Services //
///////////////////////////////////
// 7.4
virtual void requestAttributeOwnershipAssumption (ObjectHandle theObject, const AttributeHandleSet& offeredAttributes, const char *theTag)
	throw (ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError) = 0;
	
// 7.5
virtual void attributeOwnershipDivestitureNotification (ObjectHandle theObject, const AttributeHandleSet& releasedAttributes)
	throw (ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, AttributeDivestitureWasNotRequested, FederateInternalError) = 0;
	
// 7.6
virtual void attributeOwnershipAcquisitionNotification (ObjectHandle theObject, const AttributeHandleSet& securedAttributes)
	throw (ObjectNotKnown, AttributeNotKnown, AttributeAcquisitionWasNotRequested, 
		AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError) = 0;
// 7.9
virtual void attributeOwnershipUnavailable (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotKnown, AttributeNotDefined, AttributeAlreadyOwned, 
		AttributeAcquisitionWasNotRequested, FederateInternalError) = 0;
		
// 7.10
virtual void requestAttributeOwnershipRelease (ObjectHandle theObject, const AttributeHandleSet& candidateAttributes, const char *theTag)
	throw (ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError) = 0;
	
// 7.14
virtual void confirmAttributeOwnershipAcquisitionCancellation (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotKnown, AttributeNotDefined, AttributeAlreadyOwned, AttributeAcquisitionWasNotCanceled, FederateInternalError) = 0;
	
// 7.16
virtual void informAttributeOwnership (ObjectHandle theObject, AttributeHandle theAttribute, FederateHandle theOwner)
	throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) = 0;
	
virtual void attributeIsNotOwned (ObjectHandle theObject, AttributeHandle theAttribute)
	throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) = 0;
	
virtual void attributeOwnedByRTI (ObjectHandle theObject, AttributeHandle theAttribute)
	throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) = 0;
	
//////////////////////////////
// Time Management Services //
//////////////////////////////
// 8.3
virtual void timeRegulationEnabled (const FedTime& theFederateTime)
	throw (InvalidFederationTime, EnableTimeRegulationWasNotPending, FederateInternalError) = 0;
	
// 8.6
virtual void timeConstrainedEnabled (const FedTime& theFederateTime)
	throw (InvalidFederationTime, EnableTimeConstrainedWasNotPending, FederateInternalError) = 0;
	
// 8.13
virtual void timeAdvanceGrant (const FedTime& theTime)
	throw (InvalidFederationTime, TimeAdvanceWasNotInProgress, FederationTimeAlreadyPassed, 
		FederateInternalError) = 0;

// 8.22
virtual void requestRetraction (EventRetractionHandle theHandle)
	throw (EventNotKnown, FederateInternalError) = 0;

virtual ~FederateAmbassador()
	throw (FederateInternalError) { ; }
