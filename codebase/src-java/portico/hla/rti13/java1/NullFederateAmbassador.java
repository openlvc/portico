package hla.rti13.java1;

public class NullFederateAmbassador implements FederateAmbassador
{
	public void announceSynchronizationPoint( String label, String tag )
	    throws FederateInternalError
	{
	}

	public void attributeIsNotOwned( int theObject, int theAttribute ) throws ObjectNotKnown,
	    AttributeNotKnown, FederateInternalError
	{
	}

	public void attributeOwnedByRTI( int theObject, int theAttribute ) throws ObjectNotKnown,
	    AttributeNotKnown, FederateInternalError
	{
	}

	public void attributeOwnershipAcquisitionNotification( int theObject,
	                                                       AttributeHandleSet securedAttributes )
	    throws ObjectNotKnown, AttributeNotKnown, AttributeAcquisitionWasNotRequested,
	    AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError
	{
	}

	public void attributeOwnershipDivestitureNotification( int theObject,
	                                                       AttributeHandleSet releasedAttributes )
	    throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned,
	    AttributeDivestitureWasNotRequested, FederateInternalError
	{
	}

	public void attributeOwnershipUnavailable( int theObject, AttributeHandleSet theAttributes )
	    throws ObjectNotKnown, AttributeNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
	    AttributeAcquisitionWasNotRequested, FederateInternalError
	{
	}

	public void attributesInScope( int theObject, AttributeHandleSet theAttributes )
	    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
	{
	}

	public void attributesOutOfScope( int theObject, AttributeHandleSet theAttributes )
	    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
	{
	}

	public void confirmAttributeOwnershipAcquisitionCancellation( int theObject,
	                                                              AttributeHandleSet theAttributes )
	    throws ObjectNotKnown, AttributeNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
	    AttributeAcquisitionWasNotCanceled, FederateInternalError
	{
	}

	public void discoverObjectInstance( int theObject, int theObjectClass, String theObjectName )
	    throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError
	{
	}

	public void federationNotRestored() throws FederateInternalError
	{
	}

	public void federationNotSaved() throws FederateInternalError
	{
	}

	public void federationRestoreBegun() throws FederateInternalError
	{
	}

	public void federationRestored() throws FederateInternalError
	{
	}

	public void federationSaved() throws FederateInternalError
	{
	}

	public void federationSynchronized( String label ) throws FederateInternalError
	{
	}

	public void informAttributeOwnership( int theObject, int theAttribute, int theOwner )
	    throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
	{
	}

	public void initiateFederateRestore( String label, int handle )
	    throws SpecifiedSaveLabelDoesNotExist, CouldNotRestore, FederateInternalError
	{
	}

	public void initiateFederateSave( String label ) throws UnableToPerformSave,
	    FederateInternalError
	{
	}

	public void provideAttributeValueUpdate( int theObject, AttributeHandleSet theAttributes )
	    throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError
	{
	}

	public void receiveInteraction( int theInteraction, ReceivedInteraction theParameters,
	                                byte[] theTime, String theTag, EventRetractionHandle theHandle )
	    throws InteractionClassNotKnown, InteractionParameterNotKnown, InvalidFederationTime,
	    FederateInternalError
	{
	}

	public void receiveInteraction( int theInteraction, ReceivedInteraction theParameters,
	                                String theTag ) throws InteractionClassNotKnown,
	    InteractionParameterNotKnown, FederateInternalError
	{
	}

	public void reflectAttributeValues( int theObject, ReflectedAttributes theAttributes,
	                                    byte[] theTime, String theTag,
	                                    EventRetractionHandle theHandle ) throws ObjectNotKnown,
	    AttributeNotKnown, FederateOwnsAttributes, InvalidFederationTime, FederateInternalError
	{
	}

	public void reflectAttributeValues( int theObject, ReflectedAttributes theAttributes,
	                                    String theTag ) throws ObjectNotKnown, AttributeNotKnown,
	    FederateOwnsAttributes, FederateInternalError
	{
	}

	public void removeObjectInstance( int theObject, byte[] theTime, String theTag,
	                                  EventRetractionHandle theHandle ) throws ObjectNotKnown,
	    InvalidFederationTime, FederateInternalError
	{
	}

	public void removeObjectInstance( int theObject, String theTag ) throws ObjectNotKnown,
	    FederateInternalError
	{
	}

	public void requestAttributeOwnershipAssumption( int theObject,
	                                                 AttributeHandleSet offeredAttributes,
	                                                 String theTag ) throws ObjectNotKnown,
	    AttributeNotKnown, AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError
	{
	}

	public void requestAttributeOwnershipRelease( int theObject,
	                                              AttributeHandleSet candidateAttributes,
	                                              String theTag ) throws ObjectNotKnown,
	    AttributeNotKnown, AttributeNotOwned, FederateInternalError
	{
	}

	public void requestFederationRestoreFailed( String label, String reason )
	    throws FederateInternalError
	{
	}

	public void requestFederationRestoreSucceeded( String label ) throws FederateInternalError
	{
	}

	public void requestRetraction( EventRetractionHandle theHandle ) throws EventNotKnown,
	    FederateInternalError
	{
	}

	public void startRegistrationForObjectClass( int theClass ) throws ObjectClassNotPublished,
	    FederateInternalError
	{
	}

	public void stopRegistrationForObjectClass( int theClass ) throws ObjectClassNotPublished,
	    FederateInternalError
	{
	}

	public void synchronizationPointRegistrationFailed( String label ) throws FederateInternalError
	{
	}

	public void synchronizationPointRegistrationSucceeded( String label )
	    throws FederateInternalError
	{
	}

	public void timeAdvanceGrant( byte[] theTime ) throws InvalidFederationTime,
	    TimeAdvanceWasNotInProgress, FederationTimeAlreadyPassed, FederateInternalError
	{
	}

	public void timeConstrainedEnabled( byte[] theFederateTime ) throws InvalidFederationTime,
	    EnableTimeConstrainedWasNotPending, FederateInternalError
	{
	}

	public void timeRegulationEnabled( byte[] theFederateTime ) throws InvalidFederationTime,
	    EnableTimeRegulationWasNotPending, FederateInternalError
	{
	}

	public void turnInteractionsOff( int theHandle ) throws InteractionClassNotPublished,
	    FederateInternalError
	{
	}

	public void turnInteractionsOn( int theHandle ) throws InteractionClassNotPublished,
	    FederateInternalError
	{
	}

	public void turnUpdatesOffForObjectInstance( int theObject, AttributeHandleSet theAttributes )
	    throws ObjectNotKnown, AttributeNotOwned, FederateInternalError
	{
	}

	public void turnUpdatesOnForObjectInstance( int theObject, AttributeHandleSet theAttributes )
	    throws ObjectNotKnown, AttributeNotOwned, FederateInternalError
	{
	}
}
