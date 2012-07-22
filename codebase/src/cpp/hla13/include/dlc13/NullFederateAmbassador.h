//File NullFederateAmbassador13.h
#ifndef NullFederateAmbassador13_h
#define NullFederateAmbassador13_h
#include <RTI13.h>

#ifndef NULL_AMBASSADOR_EXPORT
	#define NULL_AMBASSADOR_EXPORT
#endif
namespace rti13
{
		class NULL_AMBASSADOR_EXPORT NullFederateAmbassador : public FederateAmbassador
		{
			public:
				NullFederateAmbassador() {}
				virtual ~NullFederateAmbassador() throw (FederateInternalError) {}
			
				////////////////////////////////////
				// Federation Management Services //
				////////////////////////////////////
				virtual void synchronizationPointRegistrationSucceeded (const char *label)
					throw (FederateInternalError) {}
					
				virtual void synchronizationPointRegistrationFailed (const char *label)
					throw (FederateInternalError) {}
			
				virtual void announceSynchronizationPoint (const char *label, const char *tag)
					throw (FederateInternalError) {}
					
				virtual void federationSynchronized (const char *label)
					throw (FederateInternalError) {}
			
				virtual void initiateFederateSave (const char *label)
					throw (UnableToPerformSave, FederateInternalError) {}
			
				virtual void federationSaved ()
					throw (FederateInternalError) {}
			
				virtual void federationNotSaved ()
					throw (FederateInternalError) {}
					
				virtual void requestFederationRestoreSucceeded (const char *label)
					throw (FederateInternalError) {}
					
				virtual void requestFederationRestoreFailed (const char *label, const char *reason)
					throw (FederateInternalError) {}
			
				virtual void federationRestoreBegun ()
					throw (FederateInternalError) {}
					
				virtual void initiateFederateRestore (const char *label, FederateHandle handle)
					throw (SpecifiedSaveLabelDoesNotExist, CouldNotRestore, FederateInternalError) {}
			
				virtual void federationRestored ()
					throw (FederateInternalError) {}
			
				virtual void federationNotRestored ()
					throw (FederateInternalError) {}
			
				/////////////////////////////////////
				// Declaration Management Services //
				/////////////////////////////////////
				virtual void startRegistrationForObjectClass (ObjectClassHandle theClass)
					throw (ObjectClassNotPublished, FederateInternalError) {}
			
				virtual void stopRegistrationForObjectClass (ObjectClassHandle theClass)
					throw (ObjectClassNotPublished, FederateInternalError) {}
			
				virtual void turnInteractionsOn (InteractionClassHandle theHandle)
					throw (InteractionClassNotPublished, FederateInternalError) {}
			
				virtual void turnInteractionsOff (InteractionClassHandle theHandle)
					throw (InteractionClassNotPublished, FederateInternalError) {}
			
				////////////////////////////////
				// Object Management Services //
				////////////////////////////////
				virtual void discoverObjectInstance (ObjectHandle theObject, ObjectClassHandle theObjectClass, 
					const char* theObjectName)
					throw (CouldNotDiscover, ObjectClassNotKnown, FederateInternalError) {}
			
				virtual void reflectAttributeValues (ObjectHandle theObject, 
					const AttributeHandleValuePairSet& theAttributes, const FedTime& theTime, 
					const char *theTag, EventRetractionHandle theHandle)
					throw (ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, InvalidFederationTime,
						FederateInternalError) {}
			
				virtual void reflectAttributeValues (ObjectHandle theObject, 
					const AttributeHandleValuePairSet& theAttributes, const char *theTag)
					throw (ObjectNotKnown, AttributeNotKnown, FederateOwnsAttributes, FederateInternalError) {}
					
				// 4.6
				virtual void receiveInteraction (InteractionClassHandle theInteraction, 
					const ParameterHandleValuePairSet& theParameters,  const FedTime& theTime, 
					const char *theTag, EventRetractionHandle theHandle)
					throw (InteractionClassNotKnown, InteractionParameterNotKnown, InvalidFederationTime, 
						FederateInternalError) {}
						
				virtual void receiveInteraction (InteractionClassHandle theInteraction, 
					const ParameterHandleValuePairSet& theParameters, const char *theTag)
					throw (InteractionClassNotKnown, InteractionParameterNotKnown, FederateInternalError) {}
			
				virtual void removeObjectInstance (ObjectHandle theObject, const FedTime& theTime, 
					const char *theTag, EventRetractionHandle theHandle)
					throw (ObjectNotKnown, InvalidFederationTime, FederateInternalError) {}
			
				virtual void removeObjectInstance (ObjectHandle theObject, const char *theTag)
					throw (ObjectNotKnown, FederateInternalError) {}
					
				virtual void attributesInScope (ObjectHandle theObject, const AttributeHandleSet& theAttributes)
					throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) {}
					
				virtual void attributesOutOfScope (ObjectHandle theObject, 
					const AttributeHandleSet& theAttributes)
					throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) {}
			
				virtual void provideAttributeValueUpdate (ObjectHandle theObject, 
					const AttributeHandleSet& theAttributes)
					throw (ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError) {}
			
				virtual void turnUpdatesOnForObjectInstance (ObjectHandle theObject, 
					const AttributeHandleSet& theAttributes)
					throw (ObjectNotKnown, AttributeNotOwned, FederateInternalError) {}
			
				virtual void turnUpdatesOffForObjectInstance (ObjectHandle theObject, 
					const AttributeHandleSet& theAttributes)
					throw (ObjectNotKnown, AttributeNotOwned, FederateInternalError) {}
			
				///////////////////////////////////
				// Ownership Management Services //
				///////////////////////////////////
				virtual void requestAttributeOwnershipAssumption (ObjectHandle theObject, 
					const AttributeHandleSet& offeredAttributes, const char *theTag)
					throw (ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned, AttributeNotPublished,
						FederateInternalError) {}
			
				virtual void attributeOwnershipDivestitureNotification (ObjectHandle theObject, 
					const AttributeHandleSet& releasedAttributes)
					throw (ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, 
						AttributeDivestitureWasNotRequested, FederateInternalError) {}
						
				virtual void attributeOwnershipAcquisitionNotification (ObjectHandle theObject, 
					const AttributeHandleSet& securedAttributes)
					throw (ObjectNotKnown, AttributeNotKnown, AttributeAcquisitionWasNotRequested, 
						AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError) {}
						
				virtual void attributeOwnershipUnavailable (ObjectHandle theObject, 
					const AttributeHandleSet& theAttributes)
					throw (ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned, 
						AttributeAcquisitionWasNotRequested, FederateInternalError) {}
			
				virtual void requestAttributeOwnershipRelease (ObjectHandle theObject, 
					const AttributeHandleSet& candidateAttributes, const char *theTag)
					throw (ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError) {}
			
				virtual void confirmAttributeOwnershipAcquisitionCancellation (ObjectHandle theObject, 
					const AttributeHandleSet& theAttributes)
					throw (ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned, 
						AttributeAcquisitionWasNotCanceled, FederateInternalError) {}
			
				virtual void informAttributeOwnership (ObjectHandle theObject, AttributeHandle theAttribute,
					FederateHandle theOwner) 
					throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) {}
			
				virtual void attributeIsNotOwned (ObjectHandle theObject, AttributeHandle theAttribute)
					throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) {}
					
				virtual void attributeOwnedByRTI (ObjectHandle theObject, AttributeHandle theAttribute)
					throw (ObjectNotKnown, AttributeNotKnown, FederateInternalError) {}
			
				//////////////////////////////
				// Time Management Services //
				//////////////////////////////
				virtual void timeRegulationEnabled (const FedTime& theFederateTime)
					throw (InvalidFederationTime, EnableTimeRegulationWasNotPending, FederateInternalError) {}
			
				virtual void timeConstrainedEnabled (const FedTime& theFederateTime)
					throw (InvalidFederationTime, EnableTimeConstrainedWasNotPending, FederateInternalError) {}
			
				virtual void timeAdvanceGrant (const FedTime& theTime)
					throw (InvalidFederationTime, TimeAdvanceWasNotInProgress, FederateInternalError) {}
			
				virtual void requestRetraction (EventRetractionHandle theHandle)
					throw (EventNotKnown, FederateInternalError) {}
	};
} // End of namespace rti13
#endif // NullFederateAmbassador13_h
