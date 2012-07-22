//File RTIambServices13.h
//Included in RTI13.h

typedef FederateAmbassador* FederateAmbassadorPtr;

////////////////////////////////////
// Federation Management Services //
////////////////////////////////////
// 4.2
virtual void createFederationExecution (const char *executionName, const char *FED) 
	throw (FederationExecutionAlreadyExists, CouldNotOpenFED, ErrorReadingFED, ConcurrentAccessAttempted, RTIinternalError);

// 4.3
virtual void destroyFederationExecution (const char *executionName)
	throw (FederatesCurrentlyJoined, FederationExecutionDoesNotExist, ConcurrentAccessAttempted, RTIinternalError);
	
// 4.4
virtual FederateHandle joinFederationExecution (const char *yourName, const char *executionName, 
	FederateAmbassadorPtr federateAmbassadorReference)
	throw (FederateAlreadyExecutionMember, FederationExecutionDoesNotExist, CouldNotOpenFED, ErrorReadingFED, 
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 4.5
virtual void resignFederationExecution (ResignAction theAction) 
	throw (FederateOwnsAttributes, FederateNotExecutionMember, InvalidResignAction, ConcurrentAccessAttempted, RTIinternalError);
	
// 4.6
virtual void registerFederationSynchronizationPoint (const char *label, const char *theTag)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

virtual void registerFederationSynchronizationPoint (const char *label, const char *theTag, const FederateHandleSet& syncSet)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 4.9
virtual void synchronizationPointAchieved (const char *label)
	throw (SynchronizationPointLabelWasNotAnnounced, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, 
		RestoreInProgress, RTIinternalError);
// 4.11
virtual void requestFederationSave (const char *label, const FedTime& theTime)
	throw (FederationTimeAlreadyPassed, InvalidFederationTime, FederateNotExecutionMember, ConcurrentAccessAttempted, 
		SaveInProgress, RestoreInProgress, RTIinternalError);
virtual void requestFederationSave (const char *label)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 4.13
virtual void federateSaveBegun ()
	throw (SaveNotInitiated, FederateNotExecutionMember, ConcurrentAccessAttempted, RestoreInProgress, RTIinternalError);
	
// 4.14
virtual void federateSaveComplete () 
	throw (SaveNotInitiated, FederateNotExecutionMember, ConcurrentAccessAttempted, RestoreInProgress, RTIinternalError);

virtual void federateSaveNotComplete ()
	throw (SaveNotInitiated, FederateNotExecutionMember, ConcurrentAccessAttempted, RestoreInProgress, RTIinternalError);
	
// 4.16
virtual void requestFederationRestore (const char *label)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 4.20
virtual void federateRestoreComplete () 
	throw (RestoreNotRequested, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RTIinternalError);
	
virtual void federateRestoreNotComplete ()
	throw (RestoreNotRequested, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RTIinternalError);
	
/////////////////////////////////////
// Declaration Management Services //
/////////////////////////////////////
// 5.2
virtual void publishObjectClass (ObjectClassHandle theClass, const AttributeHandleSet& attributeList)
	throw (ObjectClassNotDefined, AttributeNotDefined, OwnershipAcquisitionPending, FederateNotExecutionMember, 
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 5.3
virtual void unpublishObjectClass (ObjectClassHandle theClass)
	throw (ObjectClassNotDefined, ObjectClassNotPublished, OwnershipAcquisitionPending, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 5.4
virtual void publishInteractionClass (InteractionClassHandle theInteraction)
	throw (InteractionClassNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
	
// 5.5
virtual void unpublishInteractionClass (InteractionClassHandle theInteraction)
	throw (InteractionClassNotDefined, InteractionClassNotPublished, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);

// 5.6
virtual void subscribeObjectClassAttributes (ObjectClassHandle theClass, const AttributeHandleSet& attributeList, 
	Boolean active = RTI_TRUE)
	throw (ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);

// 5.7
virtual void unsubscribeObjectClass (ObjectClassHandle theClass)
	throw (ObjectClassNotDefined, ObjectClassNotSubscribed, FederateNotExecutionMember, ConcurrentAccessAttempted, 
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 5.8
virtual void subscribeInteractionClass (InteractionClassHandle theClass, Boolean active = RTI_TRUE)
	throw (InteractionClassNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted,
		FederateLoggingServiceCalls, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 5.9
virtual void unsubscribeInteractionClass (InteractionClassHandle theClass)
	throw (InteractionClassNotDefined, InteractionClassNotSubscribed, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
////////////////////////////////
// Object Management Services //
////////////////////////////////
// 6.2
virtual ObjectHandle registerObjectInstance (ObjectClassHandle theClass, const char *theObject)
	throw (ObjectClassNotDefined, ObjectClassNotPublished, ObjectAlreadyRegistered, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
virtual ObjectHandle registerObjectInstance (ObjectClassHandle theClass)
	throw (ObjectClassNotDefined, ObjectClassNotPublished, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 6.4
virtual EventRetractionHandle updateAttributeValues (ObjectHandle theObject, const AttributeHandleValuePairSet& theAttributes,
	const FedTime& theTime, const char *theTag)
	throw (ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidFederationTime, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
virtual void updateAttributeValues (ObjectHandle theObject, const AttributeHandleValuePairSet& theAttributes, const char *theTag)
	throw (ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 6.6
virtual EventRetractionHandle sendInteraction (InteractionClassHandle theInteraction, 
	const ParameterHandleValuePairSet& theParameters, const FedTime& theTime, const char *theTag)
	throw (InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidFederationTime,
		FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
virtual void sendInteraction (InteractionClassHandle theInteraction, const ParameterHandleValuePairSet& theParameters,
	const char *theTag)
	throw (InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 6.8
virtual EventRetractionHandle deleteObjectInstance (ObjectHandle theObject, const FedTime& theTime, const char *theTag)
	throw (ObjectNotKnown, DeletePrivilegeNotHeld, InvalidFederationTime, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

virtual void deleteObjectInstance (ObjectHandle theObject, const char *theTag)
	throw (ObjectNotKnown, DeletePrivilegeNotHeld, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);

// 6.10
virtual void localDeleteObjectInstance (ObjectHandle theObject)
	throw (ObjectNotKnown, FederateOwnsAttributes, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 6.11
virtual void changeAttributeTransportationType (ObjectHandle theObject, const AttributeHandleSet& theAttributes, 
	TransportationHandle theType)
	throw (ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidTransportationHandle, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 6.12
virtual void changeInteractionTransportationType (InteractionClassHandle theClass, TransportationHandle theType)
	throw (InteractionClassNotDefined, InteractionClassNotPublished, InvalidTransportationHandle, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 6.15
virtual void requestObjectAttributeValueUpdate (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);

virtual void requestClassAttributeValueUpdate (ObjectClassHandle theClass, const AttributeHandleSet& theAttributes)
	throw (ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
///////////////////////////////////
// Ownership Management Services //
///////////////////////////////////
// 7.2
virtual void unconditionalAttributeOwnershipDivestiture (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 7.3
virtual void negotiatedAttributeOwnershipDivestiture (ObjectHandle theObject, const AttributeHandleSet& theAttributes, 
	const char *theTag)
	throw (ObjectNotKnown,AttributeNotDefined, AttributeNotOwned, AttributeAlreadyBeingDivested, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 7.7
virtual void attributeOwnershipAcquisition (ObjectHandle theObject, const AttributeHandleSet& desiredAttributes, 
	const char *theTag)
	throw (ObjectNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, FederateOwnsAttributes,
		FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 7.8
virtual void attributeOwnershipAcquisitionIfAvailable (ObjectHandle theObject, const AttributeHandleSet& desiredAttributes)
	throw (ObjectNotKnown, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, FederateOwnsAttributes,
		AttributeAlreadyBeingAcquired, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress,
		RTIinternalError);
		
// 7.11
virtual AttributeHandleSet* attributeOwnershipReleaseResponse (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, FederateWasNotAskedToReleaseAttribute, 
		FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 7.12
virtual void cancelNegotiatedAttributeOwnershipDivestiture (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, AttributeDivestitureWasNotRequested, 
		FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 7.13
virtual void cancelAttributeOwnershipAcquisition (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
	throw (ObjectNotKnown, AttributeNotDefined, AttributeAlreadyOwned, AttributeAcquisitionWasNotRequested, 
		FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 7.15
virtual void queryAttributeOwnership (ObjectHandle theObject, AttributeHandle theAttribute)
	throw (ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
		
// 7.17
virtual Boolean isAttributeOwnedByFederate (ObjectHandle theObject, AttributeHandle theAttribute)
	throw (ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
//////////////////////////////
// Time Management Services //
//////////////////////////////
// 8.2
virtual void enableTimeRegulation (const FedTime& theFederateTime, const FedTime& theLookahead)
	throw (TimeRegulationAlreadyEnabled, EnableTimeRegulationPending, TimeAdvanceAlreadyInProgress, InvalidFederationTime,
		InvalidLookahead, ConcurrentAccessAttempted, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError);

// 8.4
virtual void disableTimeRegulation ()
	throw (TimeRegulationWasNotEnabled, ConcurrentAccessAttempted, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError);
// 8.5
virtual void enableTimeConstrained ()
	throw (TimeConstrainedAlreadyEnabled, EnableTimeConstrainedPending, TimeAdvanceAlreadyInProgress, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 8.7
virtual void disableTimeConstrained ()
	throw (TimeConstrainedWasNotEnabled, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);

// 8.8
virtual void timeAdvanceRequest (const FedTime& theTime)
	throw (InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
		
// 8.9
virtual void timeAdvanceRequestAvailable (const FedTime& theTime)
	throw (InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
		
// 8.10
virtual void nextEventRequest (const FedTime& theTime)
	throw (InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
		
// 8.11
virtual void nextEventRequestAvailable (const FedTime& theTime)
	throw (InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
// 8.12
virtual void flushQueueRequest (const FedTime& theTime)
	throw (InvalidFederationTime, FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
		
// 8.14
virtual void enableAsynchronousDelivery()
	throw (AsynchronousDeliveryAlreadyEnabled, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
		
// 8.15
virtual void disableAsynchronousDelivery()
	throw (AsynchronousDeliveryAlreadyDisabled, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);
		
// 8.16
virtual void queryLBTS (FedTime& theTime)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 8.17
virtual void queryFederateTime (FedTime& theTime)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 8.18
virtual void queryMinNextEventTime (FedTime& theTime)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 8.19
virtual void modifyLookahead (const FedTime& theLookahead)
	throw (InvalidLookahead, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress,
		RTIinternalError);

// 8.20
virtual void queryLookahead (FedTime& theTime)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 8.21
virtual void retract (EventRetractionHandle theHandle)
	throw (InvalidRetractionHandle, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, 
		RestoreInProgress, RTIinternalError);
		
// 8.23
virtual void changeAttributeOrderType (ObjectHandle theObject, const AttributeHandleSet& theAttributes, OrderingHandle theType)
	throw (ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidOrderingHandle, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 8.24
virtual void changeInteractionOrderType (InteractionClassHandle theClass, OrderingHandle theType)
	throw (InteractionClassNotDefined, InteractionClassNotPublished, InvalidOrderingHandle, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
//////////////////////////////////
// Data Distribution Management //
//////////////////////////////////
// 9.2
virtual Region* createRegion (SpaceHandle theSpace, ULong numberOfExtents)
	throw (SpaceNotDefined, InvalidExtents, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 9.3
virtual void notifyAboutRegionModification (Region &theRegion)
	throw (RegionNotKnown, InvalidExtents, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 9.4
virtual void deleteRegion (Region *theRegion)
	throw (RegionNotKnown, RegionInUse, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 9.5
virtual ObjectHandle registerObjectInstanceWithRegion (ObjectClassHandle theClass, const char *theObject,
	AttributeHandle theAttributes[], Region *theRegions[], ULong theNumberOfHandles)
	throw (ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, RegionNotKnown,
		InvalidRegionContext, ObjectAlreadyRegistered, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);

virtual ObjectHandle registerObjectInstanceWithRegion (ObjectClassHandle theClass, AttributeHandle theAttributes[], 
	Region *theRegions[], ULong theNumberOfHandles)
	throw (ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, RegionNotKnown,
		InvalidRegionContext, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress,
		RTIinternalError);

// 9.6
virtual void associateRegionForUpdates (Region &theRegion, ObjectHandle theObject, const AttributeHandleSet &theAttributes)
	throw (ObjectNotKnown, AttributeNotDefined, InvalidRegionContext, RegionNotKnown, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 9.7
virtual void unassociateRegionForUpdates (Region &theRegion, ObjectHandle theObject)
	throw (ObjectNotKnown, InvalidRegionContext, RegionNotKnown, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 9.8
virtual void subscribeObjectClassAttributesWithRegion (ObjectClassHandle theClass, Region &theRegion, 
	const AttributeHandleSet &attributeList, Boolean active = RTI_TRUE)
	throw (ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
		
// 9.9
virtual void unsubscribeObjectClassWithRegion (ObjectClassHandle theClass, Region &theRegion)
	throw (ObjectClassNotDefined, RegionNotKnown, ObjectClassNotSubscribed, FederateNotExecutionMember, 
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 9.10
virtual void subscribeInteractionClassWithRegion (InteractionClassHandle theClass, Region &theRegion, Boolean active = RTI_TRUE)
	throw (InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext, FederateLoggingServiceCalls,
		FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 9.11
virtual void unsubscribeInteractionClassWithRegion (InteractionClassHandle theClass, Region &theRegion)
	throw (InteractionClassNotDefined, InteractionClassNotSubscribed, RegionNotKnown, FederateNotExecutionMember,
		ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 9.12
virtual EventRetractionHandle sendInteractionWithRegion (InteractionClassHandle theInteraction, 
	const ParameterHandleValuePairSet &theParameters, const FedTime& theTime, const char *theTag, const Region &theRegion)
	throw (InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, InvalidFederationTime,
		RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress,
		RestoreInProgress, RTIinternalError);

virtual void sendInteractionWithRegion (InteractionClassHandle theInteraction, const ParameterHandleValuePairSet &theParameters, 
	const char *theTag, const Region &theRegion)
	throw (InteractionClassNotDefined, InteractionClassNotPublished, InteractionParameterNotDefined, RegionNotKnown,
		InvalidRegionContext, FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress,
		RTIinternalError);
		
// 9.13
virtual void requestClassAttributeValueUpdateWithRegion (ObjectClassHandle theClass, 
	const AttributeHandleSet &theAttributes, const Region &theRegion)
	throw (ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown, FederateNotExecutionMember, ConcurrentAccessAttempted,
		SaveInProgress, RestoreInProgress, RTIinternalError);

//////////////////////////
// RTI Support Services //
//////////////////////////
// 10.2
virtual ObjectClassHandle getObjectClassHandle (const char *theName)
	throw (NameNotFound, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);

// 10.3
virtual char * getObjectClassName (ObjectClassHandle theHandle)
	throw (ObjectClassNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.4
virtual AttributeHandle getAttributeHandle (const char *theName, ObjectClassHandle whichClass)
	throw (ObjectClassNotDefined, NameNotFound, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.5
virtual char * getAttributeName (AttributeHandle theHandle, ObjectClassHandle whichClass)
	throw (ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);

// 10.6
virtual InteractionClassHandle getInteractionClassHandle (const char *theName)
	throw (NameNotFound, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);

// 10.7
virtual char * getInteractionClassName (InteractionClassHandle theHandle)
	throw (InteractionClassNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);

// 10.8
virtual ParameterHandle getParameterHandle (const char *theName, InteractionClassHandle whichClass)
	throw (InteractionClassNotDefined, NameNotFound, FederateNotExecutionMember, ConcurrentAccessAttempted,
		RTIinternalError);

// 10.9
virtual char * getParameterName (ParameterHandle theHandle, InteractionClassHandle whichClass)
	throw (InteractionClassNotDefined, InteractionParameterNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted,
		RTIinternalError);

// 10.10
virtual ObjectHandle getObjectInstanceHandle (const char *theName)
	throw (ObjectNotKnown, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.11
virtual char * getObjectInstanceName (ObjectHandle theHandle)
	throw (ObjectNotKnown, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.12
virtual SpaceHandle getRoutingSpaceHandle (const char *theName)
	throw (NameNotFound, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.13
virtual char * getRoutingSpaceName (SpaceHandle theHandle)
	throw (SpaceNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.14
virtual DimensionHandle getDimensionHandle (const char *theName, SpaceHandle whichSpace)
	throw (SpaceNotDefined, NameNotFound, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.15
virtual char * getDimensionName (DimensionHandle theHandle, SpaceHandle whichSpace)
	throw (SpaceNotDefined, DimensionNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.16
virtual SpaceHandle getAttributeRoutingSpaceHandle (AttributeHandle theHandle, ObjectClassHandle whichClass)
	throw (ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.17
virtual ObjectClassHandle getObjectClass (ObjectHandle theObject)
	throw (ObjectNotKnown, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.18
virtual SpaceHandle getInteractionRoutingSpaceHandle (InteractionClassHandle theHandle)
	throw (InteractionClassNotDefined, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.19
virtual TransportationHandle getTransportationHandle (const char *theName)
	throw (NameNotFound, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.20
virtual char* getTransportationName (TransportationHandle theHandle)
	throw (InvalidTransportationHandle, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);

// 10.21
virtual OrderingHandle getOrderingHandle (const char *theName)
	throw (NameNotFound, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.22
virtual char* getOrderingName (OrderingHandle theHandle)
	throw (InvalidOrderingHandle, FederateNotExecutionMember, ConcurrentAccessAttempted, RTIinternalError);
	
// 10.23
virtual void enableClassRelevanceAdvisorySwitch() 
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 10.24
virtual void disableClassRelevanceAdvisorySwitch()
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 10.25
virtual void enableAttributeRelevanceAdvisorySwitch()
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 10.26
virtual void disableAttributeRelevanceAdvisorySwitch()
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 10.27
virtual void enableAttributeScopeAdvisorySwitch()
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
// 10.28
virtual void disableAttributeScopeAdvisorySwitch()
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 10.29
virtual void enableInteractionRelevanceAdvisorySwitch()
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);

// 10.30
virtual void disableInteractionRelevanceAdvisorySwitch()
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, SaveInProgress, RestoreInProgress, RTIinternalError);
	
//
virtual Boolean tick ()
	throw (SpecifiedSaveLabelDoesNotExist, ConcurrentAccessAttempted, RTIinternalError);

virtual Boolean tick (TickTime minimum, TickTime maximum)
	throw (SpecifiedSaveLabelDoesNotExist, ConcurrentAccessAttempted, RTIinternalError);

RTIambassador()
	throw (MemoryExhausted, RTIinternalError);

virtual ~RTIambassador()
	throw (RTIinternalError);

virtual RegionToken getRegionToken(Region*)
	throw (FederateNotExecutionMember, ConcurrentAccessAttempted, RegionNotKnown, RTIinternalError);

virtual Region* getRegion(RegionToken)
	throw(FederateNotExecutionMember, ConcurrentAccessAttempted, RegionNotKnown, RTIinternalError);
