/*
 *   This file is provided as a copy from the SISO (http://www.sisostds.org) DLC standard for
 *   HLA 1516 (SISO-STD-004.1-2004).
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
package hla.rti1516;

/**
 * Memory Management Conventions for Parameters All Java parameters, including object references,
 * are passed by value. Therefore there is no need to specify further conventions for primitive
 * types. Unless otherwise noted, reference parameters adhere to the following convention: The
 * referenced object is created (or acquired) by the caller. The callee must copy during the call
 * anything it wishes to save beyond the completion of the call. Unless otherwise noted, a
 * reference returned from a method represents a new object created by the callee. The caller is
 * free to modify the object whose reference is returned.
 */ 
/**
 * The RTI presents this interface to the federate. RTI implementer must implement this.
 */ 

public interface RTIambassador
{
	////////////////////////////////////
	// Federation Management Services //
	////////////////////////////////////
	// 4.2
	public void createFederationExecution( String federationExecutionName, java.net.URL fdd )
	    throws FederationExecutionAlreadyExists, CouldNotOpenFDD, ErrorReadingFDD, RTIinternalError;

	// 4.3
	public void destroyFederationExecution( String federationExecutionName )
	    throws FederatesCurrentlyJoined, FederationExecutionDoesNotExist, RTIinternalError;

	// 4.4
	public FederateHandle joinFederationExecution( String federateType,
	                                               String federationExecutionName,
	                                               FederateAmbassador federateReference,
	                                               MobileFederateServices serviceReferences )
	    throws FederateAlreadyExecutionMember, FederationExecutionDoesNotExist, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 4.5
	public void resignFederationExecution( ResignAction resignAction )
	    throws OwnershipAcquisitionPending, FederateOwnsAttributes, FederateNotExecutionMember,
	    RTIinternalError;

	// 4.6
	public void registerFederationSynchronizationPoint( String synchronizationPointLabel,
	                                                    byte[] userSuppliedTag )
	    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	public void registerFederationSynchronizationPoint( String synchronizationPointLabel,
	                                                    byte[] userSuppliedTag,
	                                                    FederateHandleSet synchronizationSet )
	    throws FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 4.9
	public void synchronizationPointAchieved( String synchronizationPointLabel )
	    throws SynchronizationPointLabelNotAnnounced, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 4.11
	public void requestFederationSave( String label ) throws FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	public void requestFederationSave( String label, LogicalTime theTime )
	    throws LogicalTimeAlreadyPassed, InvalidLogicalTime, FederateUnableToUseTime,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 4.13
	public void federateSaveBegun() throws SaveNotInitiated, FederateNotExecutionMember,
	    RestoreInProgress, RTIinternalError;

	// 4.14
	public void federateSaveComplete() throws FederateHasNotBegunSave, FederateNotExecutionMember,
	    RestoreInProgress, RTIinternalError;

	public void federateSaveNotComplete() throws FederateHasNotBegunSave,
	    FederateNotExecutionMember, RestoreInProgress, RTIinternalError;

	// 4.16
	public void queryFederationSaveStatus() throws FederateNotExecutionMember, RestoreInProgress,
	    RTIinternalError;

	// 4.18
	public void requestFederationRestore( String label ) throws FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 4.22
	public void federateRestoreComplete() throws RestoreNotRequested, FederateNotExecutionMember,
	    SaveInProgress, RTIinternalError;

	public void federateRestoreNotComplete() throws RestoreNotRequested,
	    FederateNotExecutionMember, SaveInProgress, RTIinternalError;

	//4.24 
	public void queryFederationRestoreStatus() throws FederateNotExecutionMember, SaveInProgress,
	    RTIinternalError;

	/////////////////////////////////////
	// Declaration Management Services //
	/////////////////////////////////////
	// 5.2
	public void publishObjectClassAttributes( ObjectClassHandle theClass,
	                                          AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 5.3
	public void unpublishObjectClass( ObjectClassHandle theClass ) throws ObjectClassNotDefined,
	    OwnershipAcquisitionPending, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
	    RTIinternalError;

	public void unpublishObjectClassAttributes( ObjectClassHandle theClass,
	                                            AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, OwnershipAcquisitionPending,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 5.4
	public void publishInteractionClass( InteractionClassHandle theInteraction )
	    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 5.5
	public void unpublishInteractionClass( InteractionClassHandle theInteraction )
	    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 5.6
	public void subscribeObjectClassAttributes( ObjectClassHandle theClass,
	                                            AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	public void subscribeObjectClassAttributesPassively( ObjectClassHandle theClass,
	                                                     AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 5.7
	public void unsubscribeObjectClass( ObjectClassHandle theClass ) throws ObjectClassNotDefined,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	public void unsubscribeObjectClassAttributes( ObjectClassHandle theClass,
	                                              AttributeHandleSet attributeList )
	    throws ObjectClassNotDefined, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 5.8
	public void subscribeInteractionClass( InteractionClassHandle theClass )
	    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	public void subscribeInteractionClassPassively( InteractionClassHandle theClass )
	    throws InteractionClassNotDefined, FederateServiceInvocationsAreBeingReportedViaMOM,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 5.9
	public void unsubscribeInteractionClass( InteractionClassHandle theClass )
	    throws InteractionClassNotDefined, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError; 



	////////////////////////////////
	// Object Management Services //
	////////////////////////////////
	// 6.2
	public void reserveObjectInstanceName( String theObjectName ) throws IllegalName,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 6.4
	public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass )
	    throws ObjectClassNotDefined, ObjectClassNotPublished, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	public ObjectInstanceHandle registerObjectInstance( ObjectClassHandle theClass,
	                                                    String theObjectName )
	    throws ObjectClassNotDefined, ObjectClassNotPublished, ObjectInstanceNameNotReserved,
	    ObjectInstanceNameInUse, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
	    RTIinternalError;

	// 6.6
	public void updateAttributeValues( ObjectInstanceHandle theObject,
	                                   AttributeHandleValueMap theAttributes,
	                                   byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	public MessageRetractionReturn updateAttributeValues( ObjectInstanceHandle theObject,
	                                                      AttributeHandleValueMap theAttributes,
	                                                      byte[] userSuppliedTag,
	                                                      LogicalTime theTime )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned, InvalidLogicalTime,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 6.8
	public void sendInteraction( InteractionClassHandle theInteraction,
	                             ParameterHandleValueMap theParameters, byte[] userSuppliedTag )
	    throws InteractionClassNotPublished, InteractionClassNotDefined,
	    InteractionParameterNotDefined, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	public MessageRetractionReturn sendInteraction( InteractionClassHandle theInteraction,
	                                                ParameterHandleValueMap theParameters,
	                                                byte[] userSuppliedTag, LogicalTime theTime )
	    throws InteractionClassNotPublished, InteractionClassNotDefined,
	    InteractionParameterNotDefined, InvalidLogicalTime, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 6.10
	public void deleteObjectInstance( ObjectInstanceHandle objectHandle, byte[] userSuppliedTag )
	    throws DeletePrivilegeNotHeld, ObjectInstanceNotKnown, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	public MessageRetractionReturn deleteObjectInstance( ObjectInstanceHandle objectHandle,
	                                                     byte[] userSuppliedTag,
	                                                     LogicalTime theTime )
	    throws DeletePrivilegeNotHeld, ObjectInstanceNotKnown, InvalidLogicalTime,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 6.12
	public void localDeleteObjectInstance( ObjectInstanceHandle objectHandle )
	    throws ObjectInstanceNotKnown, FederateOwnsAttributes, OwnershipAcquisitionPending,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 6.13
	public void changeAttributeTransportationType( ObjectInstanceHandle theObject,
	                                               AttributeHandleSet theAttributes,
	                                               TransportationType theType )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 6.14
	public void changeInteractionTransportationType( InteractionClassHandle theClass,
	                                                 TransportationType theType )
	    throws InteractionClassNotDefined, InteractionClassNotPublished,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 6.17 
	public void requestAttributeValueUpdate( ObjectInstanceHandle theObject,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	public void requestAttributeValueUpdate( ObjectClassHandle theClass,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] userSuppliedTag ) throws ObjectClassNotDefined,
	    AttributeNotDefined, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
	    RTIinternalError;

	///////////////////////////////////
	// Ownership Management Services //
	///////////////////////////////////
	// 7.2
	public void unconditionalAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
	                                                        AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 7.3
	public void negotiatedAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
	                                                     AttributeHandleSet theAttributes,
	                                                     byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    AttributeAlreadyBeingDivested, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 7.6
	public void confirmDivestiture( ObjectInstanceHandle theObject,
	                                AttributeHandleSet theAttributes, byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    AttributeDivestitureWasNotRequested, NoAcquisitionPending, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 7.8
	public void attributeOwnershipAcquisition( ObjectInstanceHandle theObject,
	                                           AttributeHandleSet desiredAttributes,
	                                           byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
	    AttributeNotPublished, FederateOwnsAttributes, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 7.9
	public void attributeOwnershipAcquisitionIfAvailable( ObjectInstanceHandle theObject,
	                                                      AttributeHandleSet desiredAttributes )
	    throws ObjectInstanceNotKnown, ObjectClassNotPublished, AttributeNotDefined,
	    AttributeNotPublished, FederateOwnsAttributes, AttributeAlreadyBeingAcquired,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 7.12
	public AttributeHandleSet attributeOwnershipDivestitureIfWanted(
	                                                             ObjectInstanceHandle theObject,
	                                                             AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 7.13
	public void cancelNegotiatedAttributeOwnershipDivestiture( ObjectInstanceHandle theObject,
	                                                           AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    AttributeDivestitureWasNotRequested, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 7.14
	public void cancelAttributeOwnershipAcquisition( ObjectInstanceHandle theObject,
	                                                 AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeAlreadyOwned,
	    AttributeAcquisitionWasNotRequested, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 7.16
	public void queryAttributeOwnership( ObjectInstanceHandle theObject,
	                                     AttributeHandle theAttribute )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 7.18 
	public boolean isAttributeOwnedByFederate( ObjectInstanceHandle theObject,
	                                           AttributeHandle theAttribute )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	//////////////////////////////
	// Time Management Services // 
	//////////////////////////////	 
	//	 8.2 
	public void enableTimeRegulation( LogicalTimeInterval theLookahead )
	    throws TimeRegulationAlreadyEnabled, InvalidLookahead, InTimeAdvancingState,
	    RequestForTimeRegulationPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.4 
	public void disableTimeRegulation() throws TimeRegulationIsNotEnabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 8.5 
	public void enableTimeConstrained() throws TimeConstrainedAlreadyEnabled, InTimeAdvancingState,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.7 
	public void disableTimeConstrained() throws TimeConstrainedIsNotEnabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 8.8 
	public void timeAdvanceRequest( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.9 
	public void timeAdvanceRequestAvailable( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.10 
	public void nextMessageRequest( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.11 
	public void nextMessageRequestAvailable( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.12 
	public void flushQueueRequest( LogicalTime theTime ) throws InvalidLogicalTime,
	    LogicalTimeAlreadyPassed, InTimeAdvancingState, RequestForTimeRegulationPending,
	    RequestForTimeConstrainedPending, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.14 
	public void enableAsynchronousDelivery() throws AsynchronousDeliveryAlreadyEnabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 8.15 
	public void disableAsynchronousDelivery() throws AsynchronousDeliveryAlreadyDisabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 8.16 
	public TimeQueryReturn queryGALT() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.17 
	public LogicalTime queryLogicalTime() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.18 
	public TimeQueryReturn queryLITS() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 8.19 
	public void modifyLookahead( LogicalTimeInterval theLookahead )
	    throws TimeRegulationIsNotEnabled, InvalidLookahead, InTimeAdvancingState,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 8.20 
	public LogicalTimeInterval queryLookahead() throws TimeRegulationIsNotEnabled,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 8.21 
	public void retract( MessageRetractionHandle theHandle ) throws InvalidMessageRetractionHandle,
	    TimeRegulationIsNotEnabled, MessageCanNoLongerBeRetracted, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 8.23 
	public void changeAttributeOrderType( ObjectInstanceHandle theObject,
	                                      AttributeHandleSet theAttributes, OrderType theType )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, AttributeNotOwned,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	//	 8.24 
	public void changeInteractionOrderType( InteractionClassHandle theClass, OrderType theType )
	    throws InteractionClassNotDefined, InteractionClassNotPublished,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	//////////////////////////////////
	// Data Distribution Management //
	//////////////////////////////////
	// 9.2
	public RegionHandle createRegion( DimensionHandleSet dimensions )
	    throws InvalidDimensionHandle, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 9.3
	public void commitRegionModifications( RegionHandleSet regions ) throws InvalidRegion,
	    RegionNotCreatedByThisFederate, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 9.4
	public void deleteRegion( RegionHandle theRegion ) throws InvalidRegion,
	    RegionNotCreatedByThisFederate, RegionInUseForUpdateOrSubscription,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 9.5
	public ObjectInstanceHandle registerObjectInstanceWithRegions(
	                                          ObjectClassHandle theClass,
	                                          AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
	    AttributeNotPublished, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	public ObjectInstanceHandle registerObjectInstanceWithRegions(
	                                          ObjectClassHandle theClass,
	                                          AttributeSetRegionSetPairList attributesAndRegions,
	                                          String theObject )
	    throws ObjectClassNotDefined, ObjectClassNotPublished, AttributeNotDefined,
	    AttributeNotPublished, InvalidRegion, RegionNotCreatedByThisFederate, InvalidRegionContext,
	    ObjectInstanceNameNotReserved, ObjectInstanceNameInUse, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 9.6
	public void associateRegionsForUpdates( ObjectInstanceHandle theObject,
	                                        AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 9.7
	public void unassociateRegionsForUpdates( ObjectInstanceHandle theObject,
	                                          AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectInstanceNotKnown, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 9.8
	public void subscribeObjectClassAttributesWithRegions(
	                                    ObjectClassHandle theClass,
	                                    AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	public void subscribeObjectClassAttributesPassivelyWithRegions(
	                                    ObjectClassHandle theClass,
	                                    AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 9.9
	public void unsubscribeObjectClassAttributesWithRegions(
	                                    ObjectClassHandle theClass,
	                                    AttributeSetRegionSetPairList attributesAndRegions )
	    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 9.10
	public void subscribeInteractionClassWithRegions( InteractionClassHandle theClass,
	                                                  RegionHandleSet regions )
	    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    InvalidRegionContext, FederateServiceInvocationsAreBeingReportedViaMOM,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	public void subscribeInteractionClassPassivelyWithRegions( InteractionClassHandle theClass,
	                                                           RegionHandleSet regions )
	    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    InvalidRegionContext, FederateServiceInvocationsAreBeingReportedViaMOM,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 9.11
	public void unsubscribeInteractionClassWithRegions( InteractionClassHandle theClass,
	                                                    RegionHandleSet regions )
	    throws InteractionClassNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 9.12
	public void sendInteractionWithRegions( InteractionClassHandle theInteraction,
	                                        ParameterHandleValueMap theParameters,
	                                        RegionHandleSet regions, byte[] userSuppliedTag )
	    throws InteractionClassNotDefined, InteractionClassNotPublished,
	    InteractionParameterNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    InvalidRegionContext, FederateNotExecutionMember, SaveInProgress, RestoreInProgress,
	    RTIinternalError;

	public MessageRetractionReturn sendInteractionWithRegions(
	                                                        InteractionClassHandle theInteraction,
	                                                        ParameterHandleValueMap theParameters,
	                                                        RegionHandleSet regions,
	                                                        byte[] userSuppliedTag,
	                                                        LogicalTime theTime )
	    throws InteractionClassNotDefined, InteractionClassNotPublished,
	    InteractionParameterNotDefined, InvalidRegion, RegionNotCreatedByThisFederate,
	    InvalidRegionContext, InvalidLogicalTime, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	//	 9.13 
	public void requestAttributeValueUpdateWithRegions(
	                                           ObjectClassHandle theClass,
	                                           AttributeSetRegionSetPairList attributesAndRegions,
	                                           byte[] userSuppliedTag )
	    throws ObjectClassNotDefined, AttributeNotDefined, InvalidRegion,
	    RegionNotCreatedByThisFederate, InvalidRegionContext, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	//////////////////////////
	// RTI Support Services //
	//////////////////////////
	// 10.2
	public ObjectClassHandle getObjectClassHandle( String theName ) throws NameNotFound,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.3
	public String getObjectClassName( ObjectClassHandle theHandle )
	    throws InvalidObjectClassHandle, FederateNotExecutionMember, RTIinternalError;

	// 10.4
	public AttributeHandle getAttributeHandle( ObjectClassHandle whichClass, String theName )
	    throws InvalidObjectClassHandle, NameNotFound, FederateNotExecutionMember, RTIinternalError;

	// 10.5
	public String getAttributeName( ObjectClassHandle whichClass, AttributeHandle theHandle )
	    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.6
	public InteractionClassHandle getInteractionClassHandle( String theName ) throws NameNotFound,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.7
	public String getInteractionClassName( InteractionClassHandle theHandle )
	    throws InvalidInteractionClassHandle, FederateNotExecutionMember, RTIinternalError;

	// 10.8
	public ParameterHandle getParameterHandle( InteractionClassHandle whichClass, String theName )
	    throws InvalidInteractionClassHandle, NameNotFound, FederateNotExecutionMember,
	    RTIinternalError;

	// 10.9
	public String getParameterName( InteractionClassHandle whichClass, ParameterHandle theHandle )
	    throws InvalidInteractionClassHandle, InvalidParameterHandle,
	    InteractionParameterNotDefined, FederateNotExecutionMember, RTIinternalError;

	// 10.10
	public ObjectInstanceHandle getObjectInstanceHandle( String theName )
	    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError;

	// 10.11
	public String getObjectInstanceName( ObjectInstanceHandle theHandle )
	    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError;

	// 10.12
	public DimensionHandle getDimensionHandle( String theName ) throws NameNotFound,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.13
	public String getDimensionName( DimensionHandle theHandle ) throws InvalidDimensionHandle,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.14
	public long getDimensionUpperBound( DimensionHandle theHandle ) throws InvalidDimensionHandle,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.15
	public DimensionHandleSet getAvailableDimensionsForClassAttribute(
	                                                                  ObjectClassHandle whichClass,
	                                                                  AttributeHandle theHandle )
	    throws InvalidObjectClassHandle, InvalidAttributeHandle, AttributeNotDefined,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.16
	public ObjectClassHandle getKnownObjectClassHandle( ObjectInstanceHandle theObject )
	    throws ObjectInstanceNotKnown, FederateNotExecutionMember, RTIinternalError;

	// 10.17
	public DimensionHandleSet getAvailableDimensionsForInteractionClass(
	                                                             InteractionClassHandle theHandle )
	    throws InvalidInteractionClassHandle, FederateNotExecutionMember, RTIinternalError;

	// 10.18
	public TransportationType getTransportationType( String theName )
	    throws InvalidTransportationName, FederateNotExecutionMember, RTIinternalError;

	// 10.19
	public String getTransportationName( TransportationType theType )
	    throws InvalidTransportationType, FederateNotExecutionMember, RTIinternalError;

	// 10.20
	public OrderType getOrderType( String theName ) throws InvalidOrderName,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.21
	public String getOrderName( OrderType theType ) throws InvalidOrderType,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.22
	public void enableObjectClassRelevanceAdvisorySwitch() throws FederateNotExecutionMember,
	    ObjectClassRelevanceAdvisorySwitchIsOn, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 10.23
	public void disableObjectClassRelevanceAdvisorySwitch()
	    throws ObjectClassRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 10.24
	public void enableAttributeRelevanceAdvisorySwitch()
	    throws AttributeRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 10.25
	public void disableAttributeRelevanceAdvisorySwitch()
	    throws AttributeRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 10.26
	public void enableAttributeScopeAdvisorySwitch() throws AttributeScopeAdvisorySwitchIsOn,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 10.27
	public void disableAttributeScopeAdvisorySwitch() throws AttributeScopeAdvisorySwitchIsOff,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 10.28
	public void enableInteractionRelevanceAdvisorySwitch()
	    throws InteractionRelevanceAdvisorySwitchIsOn, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 10.29
	public void disableInteractionRelevanceAdvisorySwitch()
	    throws InteractionRelevanceAdvisorySwitchIsOff, FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 10.30
	public DimensionHandleSet getDimensionHandleSet( RegionHandle region ) throws InvalidRegion,
	    FederateNotExecutionMember, SaveInProgress, RestoreInProgress, RTIinternalError;

	// 10.31
	public RangeBounds getRangeBounds( RegionHandle region, DimensionHandle dimension )
	    throws InvalidRegion, RegionDoesNotContainSpecifiedDimension, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 10.32
	public void setRangeBounds( RegionHandle region, DimensionHandle dimension, RangeBounds bounds )
	    throws InvalidRegion, RegionNotCreatedByThisFederate,
	    RegionDoesNotContainSpecifiedDimension, InvalidRangeBound, FederateNotExecutionMember,
	    SaveInProgress, RestoreInProgress, RTIinternalError;

	// 10.33
	public long normalizeFederateHandle( FederateHandle federateHandle )
	    throws InvalidFederateHandle, FederateNotExecutionMember, RTIinternalError;

	// 10.34
	public long normalizeServiceGroup( ServiceGroup group ) throws InvalidServiceGroup,
	    FederateNotExecutionMember, RTIinternalError;

	// 10.37
	public boolean evokeCallback( double seconds ) throws FederateNotExecutionMember,
	    RTIinternalError;

	// 10.38
	public boolean evokeMultipleCallbacks( double minimumTime, double maximumTime )
	    throws FederateNotExecutionMember, RTIinternalError;

	// 10.39
	public void enableCallbacks() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// 10.40
	public void disableCallbacks() throws FederateNotExecutionMember, SaveInProgress,
	    RestoreInProgress, RTIinternalError;

	// API-specific services
	public AttributeHandleFactory getAttributeHandleFactory() throws FederateNotExecutionMember;

	public AttributeHandleSetFactory getAttributeHandleSetFactory()
	    throws FederateNotExecutionMember;

	public AttributeHandleValueMapFactory getAttributeHandleValueMapFactory()
	    throws FederateNotExecutionMember;

	public AttributeSetRegionSetPairListFactory getAttributeSetRegionSetPairListFactory()
	    throws FederateNotExecutionMember;

	public DimensionHandleFactory getDimensionHandleFactory() throws FederateNotExecutionMember;

	public DimensionHandleSetFactory getDimensionHandleSetFactory()
	    throws FederateNotExecutionMember;

	public FederateHandleFactory getFederateHandleFactory() throws FederateNotExecutionMember;

	public FederateHandleSetFactory getFederateHandleSetFactory() throws FederateNotExecutionMember;

	public InteractionClassHandleFactory getInteractionClassHandleFactory()
	    throws FederateNotExecutionMember;

	public ObjectClassHandleFactory getObjectClassHandleFactory() throws FederateNotExecutionMember;

	public ObjectInstanceHandleFactory getObjectInstanceHandleFactory()
	    throws FederateNotExecutionMember;

	public ParameterHandleFactory getParameterHandleFactory() throws FederateNotExecutionMember;

	public ParameterHandleValueMapFactory getParameterHandleValueMapFactory()
	    throws FederateNotExecutionMember;

	public RegionHandleSetFactory getRegionHandleSetFactory() throws FederateNotExecutionMember;

	public String getHLAversion();
} 
