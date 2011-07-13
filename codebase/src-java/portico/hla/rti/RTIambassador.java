/*
 *   This file is a direct copy from the SISO (http://www.sisostds.org) DLC standard for
 *   HLA 1.3 (SISO-STD-004-2004).
 * 
 *   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *   OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *   DISCLAIMED.  IN NO EVENT SHALL THE DEVELOPERS OF THIS PROJECT OR
 *   ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *   USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *   ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *   OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *   OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *   SUCH DAMAGE.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 */
package hla.rti;

/**
 *  Memory Management Conventions for Parameters. All Java parameters, including object references,
 *  are passed by value. Therefore there is no need to specify further conventions for primitive
 *  types. Unless otherwise noted, reference parameters adhere to the following convention:
 *  <p/> 
 *  The referenced object is created (or acquired) by the caller. The callee must copy during the
 *  call anything it wishes to save beyond the completion of the call. Unless otherwise noted, a
 *  reference returned from a method represents a new object created by the callee. The caller is 
 *  free to modify the object whose reference is returned.
 *  <p/> 
 *  The RTI presents this interface to the federate. RTI implementer must implement this. 
 */
public interface RTIambassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//////////////////////////////////// 
	// Federation Management Services // 
	//////////////////////////////////// 
	//4.2 
	public void createFederationExecution( String executionName, java.net.URL fed )
		throws FederationExecutionAlreadyExists, CouldNotOpenFED, ErrorReadingFED,
		RTIinternalError, ConcurrentAccessAttempted;

	//4.3 
	public void destroyFederationExecution( String executionName ) throws FederatesCurrentlyJoined,
		FederationExecutionDoesNotExist, RTIinternalError, ConcurrentAccessAttempted;

	//4.4 
	public int joinFederationExecution( String federateType, String federationExecutionName,
					FederateAmbassador federateReference ) throws FederateAlreadyExecutionMember,
		FederationExecutionDoesNotExist, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//4.4 
	public int joinFederationExecution( String federateType, String federationExecutionName,
					FederateAmbassador federateReference, MobileFederateServices serviceReferences )
		throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//4.5 
	public void resignFederationExecution( int resignAction ) throws FederateOwnsAttributes,
		FederateNotExecutionMember, InvalidResignAction, RTIinternalError,
		ConcurrentAccessAttempted;

	//4.6 
	public void registerFederationSynchronizationPoint( String synchronizationPointLabel,
					byte[] userSuppliedTag ) throws FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//4.6 
	public void registerFederationSynchronizationPoint( String synchronizationPointLabel,
					byte[] userSuppliedTag, FederateHandleSet synchronizationSet )
		throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//4.9 
	public void synchronizationPointAchieved( String synchronizationPointLabel )
		throws SynchronizationLabelNotAnnounced, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 4.11 
	public void requestFederationSave( String label, LogicalTime theTime )
		throws FederationTimeAlreadyPassed, InvalidFederationTime, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 4.11 
	public void requestFederationSave( String label ) throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 4.13 
	public void federateSaveBegun() throws SaveNotInitiated, FederateNotExecutionMember,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 4.14 
	public void federateSaveComplete() throws SaveNotInitiated, FederateNotExecutionMember,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 4.14 
	public void federateSaveNotComplete() throws SaveNotInitiated, FederateNotExecutionMember,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//4.16 
	public void requestFederationRestore( String label ) throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//4.20 
	public void federateRestoreComplete() throws RestoreNotRequested, FederateNotExecutionMember,
		SaveInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//4.20 
	public void federateRestoreNotComplete() throws RestoreNotRequested,
		FederateNotExecutionMember, SaveInProgress, RTIinternalError, ConcurrentAccessAttempted;

	///////////////////////////////////// 
	// Declaration Management Services // 
	///////////////////////////////////// 
	//5.2 
	public void publishObjectClass( int theClass, AttributeHandleSet attributeList )
		throws ObjectClassNotDefined, AttributeNotDefined, OwnershipAcquisitionPending,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//5.3 
	public void unpublishObjectClass( int theClass ) throws ObjectClassNotDefined,
		ObjectClassNotPublished, OwnershipAcquisitionPending, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 5.4 
	public void publishInteractionClass( int theInteraction ) throws InteractionClassNotDefined,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	// 5.5 
	public void unpublishInteractionClass( int theInteraction ) throws InteractionClassNotDefined,
		InteractionClassNotPublished, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 5.6 
	public void subscribeObjectClassAttributes( int theClass, AttributeHandleSet attributeList )
		throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 5.6 
	public void subscribeObjectClassAttributesPassively( int theClass,
					AttributeHandleSet attributeList ) throws ObjectClassNotDefined,
		AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	// 5.7 
	public void unsubscribeObjectClass( int theClass ) throws ObjectClassNotDefined,
		ObjectClassNotSubscribed, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	// 5.8 
	public void subscribeInteractionClass( int theClass ) throws InteractionClassNotDefined,
		FederateNotExecutionMember, FederateLoggingServiceCalls, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	// 5.8 
	public void subscribeInteractionClassPassively( int theClass )
		throws InteractionClassNotDefined, FederateNotExecutionMember, FederateLoggingServiceCalls,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 5.9 
	public void unsubscribeInteractionClass( int theClass ) throws InteractionClassNotDefined,
		InteractionClassNotSubscribed, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	////////////////////////////////
	// Object Management Services // 
	////////////////////////////////
	//6.2 
	public int registerObjectInstance( int theClass ) throws ObjectClassNotDefined,
		ObjectClassNotPublished, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//6.2 
	public int registerObjectInstance( int theClass, String theObject )
		throws ObjectClassNotDefined, ObjectClassNotPublished, ObjectAlreadyRegistered,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//6.4 
	public void updateAttributeValues( int theObject, SuppliedAttributes theAttributes,
					byte[] userSuppliedTag ) throws ObjectNotKnown, AttributeNotDefined,
		AttributeNotOwned, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//6.4 
	public EventRetractionHandle updateAttributeValues( int theObject,
					SuppliedAttributes theAttributes, byte[] userSuppliedTag, LogicalTime theTime )
		throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidFederationTime,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	// 6.6 
	public void sendInteraction( int theInteraction, SuppliedParameters theParameters,
					byte[] userSuppliedTag ) throws InteractionClassNotDefined,
		InteractionClassNotPublished, InteractionParameterNotDefined, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 6.6 
	public EventRetractionHandle sendInteraction( int theInteraction,
					SuppliedParameters theParameters, byte[] userSuppliedTag, LogicalTime theTime )
		throws InteractionClassNotDefined, InteractionClassNotPublished,
		InteractionParameterNotDefined, InvalidFederationTime, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 6.8 
	public void deleteObjectInstance( int ObjectHandle, byte[] userSuppliedTag )
		throws ObjectNotKnown, DeletePrivilegeNotHeld, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//6.8 
	public EventRetractionHandle deleteObjectInstance( int ObjectHandle, byte[] userSuppliedTag,
					LogicalTime theTime ) throws ObjectNotKnown, DeletePrivilegeNotHeld,
		InvalidFederationTime, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	// 6.10 
	public void localDeleteObjectInstance( int ObjectHandle ) throws ObjectNotKnown,
		FederateOwnsAttributes, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	// 6.11 
	public void changeAttributeTransportationType( int theObject, AttributeHandleSet theAttributes,
					int theType ) throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
		InvalidTransportationHandle, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	// 6.12 
	public void changeInteractionTransportationType( int theClass, int theType )
		throws InteractionClassNotDefined, InteractionClassNotPublished,
		InvalidTransportationHandle, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	// 6.15 
	public void requestObjectAttributeValueUpdate( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 6.15 
	public void requestClassAttributeValueUpdate( int theClass, AttributeHandleSet theAttributes )
		throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	/////////////////////////////////// 
	// Ownership Management Services // 
	/////////////////////////////////// 
	// 7.2 
	public void unconditionalAttributeOwnershipDivestiture( int theObject,
					AttributeHandleSet theAttributes ) throws ObjectNotKnown, AttributeNotDefined,
		AttributeNotOwned, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//7.3 
	public void negotiatedAttributeOwnershipDivestiture( int theObject,
					AttributeHandleSet theAttributes, byte[] userSuppliedTag )
		throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
		AttributeAlreadyBeingDivested, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 7.7 
	public void attributeOwnershipAcquisition( int theObject, AttributeHandleSet desiredAttributes,
					byte[] userSuppliedTag ) throws ObjectNotKnown, ObjectClassNotPublished,
		AttributeNotDefined, AttributeNotPublished, FederateOwnsAttributes,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	// 7.8 
	public void attributeOwnershipAcquisitionIfAvailable( int theObject,
					AttributeHandleSet desiredAttributes ) throws ObjectNotKnown,
		ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished,
		FederateOwnsAttributes, AttributeAlreadyBeingAcquired, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	// 7.11 
	public AttributeHandleSet attributeOwnershipReleaseResponse( int theObject,
					AttributeHandleSet theAttributes ) throws ObjectNotKnown, AttributeNotDefined,
		AttributeNotOwned, FederateWasNotAskedToReleaseAttribute, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 7.12 
	public void cancelNegotiatedAttributeOwnershipDivestiture( int theObject,
					AttributeHandleSet theAttributes ) throws ObjectNotKnown, AttributeNotDefined,
		AttributeNotOwned, AttributeDivestitureWasNotRequested, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 7.13 
	public void cancelAttributeOwnershipAcquisition( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
		AttributeAcquisitionWasNotRequested, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 7.15 
	public void queryAttributeOwnership( int theObject, int theAttribute ) throws ObjectNotKnown,
		AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//				 7.17 
	public boolean isAttributeOwnedByFederate( int theObject, int theAttribute )
		throws ObjectNotKnown, AttributeNotDefined, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//////////////////////////////				 
	// Time Management Services // 
	//////////////////////////////				 
	//				 8.2 
	public void enableTimeRegulation( LogicalTime theFederateTime, LogicalTimeInterval theLookahead )
		throws TimeRegulationAlreadyEnabled, EnableTimeRegulationPending,
		TimeAdvanceAlreadyInProgress, InvalidFederationTime, InvalidLookahead,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//				 8.4 
	public void disableTimeRegulation() throws TimeRegulationWasNotEnabled,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//				 8.5 
	public void enableTimeConstrained() throws TimeConstrainedAlreadyEnabled,
		EnableTimeConstrainedPending, TimeAdvanceAlreadyInProgress, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.7 
	public void disableTimeConstrained() throws TimeConstrainedWasNotEnabled,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//				 8.8 
	public void timeAdvanceRequest( LogicalTime theTime ) throws InvalidFederationTime,
		FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.9 
	public void timeAdvanceRequestAvailable( LogicalTime theTime ) throws InvalidFederationTime,
		FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.10 
	public void nextEventRequest( LogicalTime theTime ) throws InvalidFederationTime,
		FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.11 
	public void nextEventRequestAvailable( LogicalTime theTime ) throws InvalidFederationTime,
		FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.12 
	public void flushQueueRequest( LogicalTime theTime ) throws InvalidFederationTime,
		FederationTimeAlreadyPassed, TimeAdvanceAlreadyInProgress, EnableTimeRegulationPending,
		EnableTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.14 
	public void enableAsynchronousDelivery() throws AsynchronousDeliveryAlreadyEnabled,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//				 8.15 
	public void disableAsynchronousDelivery() throws AsynchronousDeliveryAlreadyDisabled,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//				 8.16 
	public LogicalTime queryLBTS() throws FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.17 
	public LogicalTime queryFederateTime() throws FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.18 
	public LogicalTime queryMinNextEventTime() throws FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.19 
	public void modifyLookahead( LogicalTimeInterval theLookahead ) throws InvalidLookahead,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//				 8.20 
	public LogicalTimeInterval queryLookahead() throws FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//				 8.21
	public void retract( EventRetractionHandle theHandle ) throws InvalidRetractionHandle,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//								 8.23 
	public void changeAttributeOrderType( int theObject, AttributeHandleSet theAttributes,
					int theType ) throws ObjectNotKnown, AttributeNotDefined, AttributeNotOwned,
		InvalidOrderingHandle, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//								 8.24 
	public void changeInteractionOrderType( int theClass, int theType )
		throws InteractionClassNotDefined, InteractionClassNotPublished, InvalidOrderingHandle,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//////////////////////////////////								 
	// Data Distribution Management // 
	//////////////////////////////////								 
	//								 9.2 
	public Region createRegion( int spaceHandle, int numberOfExtents ) throws SpaceNotDefined,
		InvalidExtents, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//								 9.3 
	public void notifyOfRegionModification( Region modifiedRegionInstance ) throws RegionNotKnown,
		InvalidExtents, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//								 9.4 
	public void deleteRegion( Region theRegion ) throws RegionNotKnown, RegionInUse,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//								9.5 
	public int registerObjectInstanceWithRegion( int theClass, int[] theAttributes,
					Region[] theRegions ) throws ObjectClassNotDefined, ObjectClassNotPublished,
		AttributeNotDefined, AttributeNotPublished, RegionNotKnown, InvalidRegionContext,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//								 9.5 
	public int registerObjectInstanceWithRegion( int theClass, String theObject,
					int[] theAttributes, Region[] theRegions ) throws ObjectClassNotDefined,
		ObjectClassNotPublished, AttributeNotDefined, AttributeNotPublished, RegionNotKnown,
		InvalidRegionContext, ObjectAlreadyRegistered, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//								 9.6 
	public void associateRegionForUpdates( Region theRegion, int theObject,
					AttributeHandleSet theAttributes ) throws ObjectNotKnown, AttributeNotDefined,
		InvalidRegionContext, RegionNotKnown, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//								 9.7 
	public void unassociateRegionForUpdates( Region theRegion, int theObject )
		throws ObjectNotKnown, InvalidRegionContext, RegionNotKnown, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//								 9.8 
	public void subscribeObjectClassAttributesWithRegion( int theClass, Region theRegion,
					AttributeHandleSet attributeList ) throws ObjectClassNotDefined,
		AttributeNotDefined, RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//								 9.8 
	public void subscribeObjectClassAttributesPassivelyWithRegion( int theClass, Region theRegion,
					AttributeHandleSet attributeList ) throws ObjectClassNotDefined,
		AttributeNotDefined, RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//								 9.9 
	public void unsubscribeObjectClassWithRegion( int theClass, Region theRegion )
		throws ObjectClassNotDefined, RegionNotKnown, FederateNotSubscribed,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//								 9.10 
	public void subscribeInteractionClassWithRegion( int theClass, Region theRegion )
		throws InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext,
		FederateLoggingServiceCalls, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//								 9.10 
	public void subscribeInteractionClassPassivelyWithRegion( int theClass, Region theRegion )
		throws InteractionClassNotDefined, RegionNotKnown, InvalidRegionContext,
		FederateLoggingServiceCalls, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//								 9.11 
	public void unsubscribeInteractionClassWithRegion( int theClass, Region theRegion )
		throws InteractionClassNotDefined, InteractionClassNotSubscribed, RegionNotKnown,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//								9.12 
	public void sendInteractionWithRegion( int theInteraction, SuppliedParameters theParameters,
					byte[] userSuppliedTag, Region theRegion ) throws InteractionClassNotDefined,
		InteractionClassNotPublished, InteractionParameterNotDefined, RegionNotKnown,
		InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
		RTIinternalError, ConcurrentAccessAttempted;

	//								 9.12 
	public EventRetractionHandle sendInteractionWithRegion( int theInteraction,
					SuppliedParameters theParameters, byte[] userSuppliedTag, Region theRegion,
					LogicalTime theTime ) throws InteractionClassNotDefined,
		InteractionClassNotPublished, InteractionParameterNotDefined, InvalidFederationTime,
		RegionNotKnown, InvalidRegionContext, FederateNotExecutionMember, SaveInProgress,
		RestoreInProgress, RTIinternalError, ConcurrentAccessAttempted;

	//								 9.13 
	public void requestClassAttributeValueUpdateWithRegion( int theClass,
					AttributeHandleSet theAttributes, Region theRegion )
		throws ObjectClassNotDefined, AttributeNotDefined, RegionNotKnown,
		FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError,
		ConcurrentAccessAttempted;

	//////////////////////////								 
	// RTI Support Services // 
	//////////////////////////								 
	//								 10.2 
	public int getObjectClassHandle( String theName ) throws NameNotFound,
		FederateNotExecutionMember, RTIinternalError;

	//								 10.3 
	public String getObjectClassName( int theHandle ) throws ObjectClassNotDefined,
		FederateNotExecutionMember, RTIinternalError;

	//								 10.4 
	public int getAttributeHandle( String theName, int whichClass ) throws ObjectClassNotDefined,
		NameNotFound, FederateNotExecutionMember, RTIinternalError;

	//								 10.5 
	public String getAttributeName( int theHandle, int whichClass ) throws ObjectClassNotDefined,
		AttributeNotDefined, FederateNotExecutionMember, RTIinternalError;

	//								 10.6 
	public int getInteractionClassHandle( String theName ) throws NameNotFound,
		FederateNotExecutionMember, RTIinternalError;

	//								 10.7 
	public String getInteractionClassName( int theHandle ) throws InteractionClassNotDefined,
		FederateNotExecutionMember, RTIinternalError;

	//								 10.8 
	public int getParameterHandle( String theName, int whichClass )
		throws InteractionClassNotDefined, NameNotFound, FederateNotExecutionMember,
		RTIinternalError;

	//								 10.9 
	public String getParameterName( int theHandle, int whichClass )
		throws InteractionClassNotDefined, InteractionParameterNotDefined,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.10 
	public int getObjectInstanceHandle( String theName ) throws ObjectNotKnown,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.11 
	public String getObjectInstanceName( int theHandle ) throws ObjectNotKnown,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.12 
	public int getRoutingSpaceHandle( String theName ) throws NameNotFound,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.13 
	public String getRoutingSpaceName( int theHandle ) throws SpaceNotDefined,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.14 
	public int getDimensionHandle( String theName, int whichSpace ) throws SpaceNotDefined,
		NameNotFound, FederateNotExecutionMember, RTIinternalError;

	//												 10.15 
	public String getDimensionName( int theHandle, int whichClass ) throws SpaceNotDefined,
		DimensionNotDefined, FederateNotExecutionMember, RTIinternalError;

	//												 10.16 
	public int getAttributeRoutingSpaceHandle( int theHandle, int whichClass )
		throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
		RTIinternalError;

	//												 10.17 
	public int getObjectClass( int theObject ) throws ObjectNotKnown, FederateNotExecutionMember,
		RTIinternalError;

	//												 10.18 
	public int getInteractionRoutingSpaceHandle( int theHandle ) throws InteractionClassNotDefined,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.19 
	public int getTransportationHandle( String theName ) throws NameNotFound,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.20
	public String getTransportationName( int theHandle ) throws InvalidTransportationHandle,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.21 
	public int getOrderingHandle( String theName ) throws NameNotFound, FederateNotExecutionMember,
		RTIinternalError;

	//												 10.22 
	public String getOrderingName( int theHandle ) throws InvalidOrderingHandle,
		FederateNotExecutionMember, RTIinternalError;

	//												 10.23 
	public void enableClassRelevanceAdvisorySwitch() throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError;

	//												 10.24 
	public void disableClassRelevanceAdvisorySwitch() throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError;

	//												 10.25 
	public void enableAttributeRelevanceAdvisorySwitch() throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError;

	//												 10.26 
	public void disableAttributeRelevanceAdvisorySwitch() throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError;

	//												 10.27 
	public void enableAttributeScopeAdvisorySwitch() throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError;

	//												 10.28 
	public void disableAttributeScopeAdvisorySwitch() throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError;

	//												 10.29 
	public void enableInteractionRelevanceAdvisorySwitch() throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError;

	//												 10.30 
	public void disableInteractionRelevanceAdvisorySwitch() throws FederateNotExecutionMember,
		SaveInProgress, RestoreInProgress, RTIinternalError;

	public Region getRegion( int regionToken ) throws FederateNotExecutionMember,
		ConcurrentAccessAttempted, RegionNotKnown, RTIinternalError;

	public int getRegionToken( Region region ) throws FederateNotExecutionMember,
		ConcurrentAccessAttempted, RegionNotKnown, RTIinternalError;

	public void tick() throws RTIinternalError, ConcurrentAccessAttempted;
	
} 
