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
 * Federate must implement this interface.
 */
public interface FederateAmbassador
{
	// //////////////////////////////////
	// Federation Management Services //
	// //////////////////////////////////
	// 4.7
	public void synchronizationPointRegistrationSucceeded( String synchronizationPointLabel )
	    throws FederateInternalError;

	public void synchronizationPointRegistrationFailed( String synchronizationPointLabel,
	                                                    SynchronizationPointFailureReason reason )
	    throws FederateInternalError;

	// 4.8
	public void announceSynchronizationPoint( String synchronizationPointLabel,
	                                          byte[] userSuppliedTag ) throws FederateInternalError;

	// 4.10
	public void federationSynchronized( String synchronizationPointLabel )
	    throws FederateInternalError;

	// 4.12
	public void initiateFederateSave( String label ) throws UnableToPerformSave,
	    FederateInternalError;

	public void initiateFederateSave( String label, LogicalTime time ) throws InvalidLogicalTime,
	    UnableToPerformSave, FederateInternalError;

	// 4.15
	public void federationSaved() throws FederateInternalError;

	public void federationNotSaved( SaveFailureReason reason ) throws FederateInternalError;

	// 4.17
	public void federationSaveStatusResponse( FederateHandleSaveStatusPair[] response )
	    throws FederateInternalError;

	// 4.19
	public void requestFederationRestoreSucceeded( String label ) throws FederateInternalError;

	public void requestFederationRestoreFailed( String label ) throws FederateInternalError;

	// 4.20
	public void federationRestoreBegun() throws FederateInternalError;

	// 4.21
	public void initiateFederateRestore( String label, FederateHandle federateHandle )
	    throws SpecifiedSaveLabelDoesNotExist, CouldNotInitiateRestore, FederateInternalError;

	// 4.23
	public void federationRestored() throws FederateInternalError;

	public void federationNotRestored( RestoreFailureReason reason ) throws FederateInternalError;

	// 4.25
	public void federationRestoreStatusResponse( FederateHandleRestoreStatusPair[] response )
	    throws FederateInternalError;

	// ///////////////////////////////////
	// Declaration Management Services //
	// ///////////////////////////////////
	// 5.10
	public void startRegistrationForObjectClass( ObjectClassHandle theClass )
	    throws ObjectClassNotPublished, FederateInternalError;

	// 5.11
	public void stopRegistrationForObjectClass( ObjectClassHandle theClass )
	    throws ObjectClassNotPublished, FederateInternalError;

	// 5.12
	public void turnInteractionsOn( InteractionClassHandle theHandle )
	    throws InteractionClassNotPublished, FederateInternalError;

	// 5.13
	public void turnInteractionsOff( InteractionClassHandle theHandle )
	    throws InteractionClassNotPublished, FederateInternalError;

	// //////////////////////////////
	// Object Management Services //
	// //////////////////////////////
	// 6.3
	public void objectInstanceNameReservationSucceeded( String objectName ) throws UnknownName,
	    FederateInternalError;

	public void objectInstanceNameReservationFailed( String objectName ) throws UnknownName,
	    FederateInternalError;

	// 6.5
	public void discoverObjectInstance( ObjectInstanceHandle theObject,
	                                    ObjectClassHandle theObjectClass, String objectName )
	    throws CouldNotDiscover, ObjectClassNotRecognized, FederateInternalError;

	// 6.7
	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] userSuppliedTag, OrderType sentOrdering,
	                                    TransportationType theTransport )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed,
	    FederateInternalError;

	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] userSuppliedTag, OrderType sentOrdering,
	                                    TransportationType theTransport, RegionHandleSet sentRegions )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed,
	    FederateInternalError;

	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] userSuppliedTag, OrderType sentOrdering,
	                                    TransportationType theTransport, LogicalTime theTime,
	                                    OrderType receivedOrdering ) throws ObjectInstanceNotKnown,
	    AttributeNotRecognized, AttributeNotSubscribed, FederateInternalError;

	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] userSuppliedTag, OrderType sentOrdering,
	                                    TransportationType theTransport, LogicalTime theTime,
	                                    OrderType receivedOrdering, RegionHandleSet sentRegions )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed,
	    FederateInternalError;

	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] userSuppliedTag, OrderType sentOrdering,
	                                    TransportationType theTransport, LogicalTime theTime,
	                                    OrderType receivedOrdering,
	                                    MessageRetractionHandle retractionHandle )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed,
	    InvalidLogicalTime, FederateInternalError;

	public void reflectAttributeValues( ObjectInstanceHandle theObject,
	                                    AttributeHandleValueMap theAttributes,
	                                    byte[] userSuppliedTag, OrderType sentOrdering,
	                                    TransportationType theTransport, LogicalTime theTime,
	                                    OrderType receivedOrdering,
	                                    MessageRetractionHandle retractionHandle,
	                                    RegionHandleSet sentRegions )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed,
	    InvalidLogicalTime, FederateInternalError;

	// 6.9
	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
	                                OrderType sentOrdering, TransportationType theTransport )
	    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
	    InteractionClassNotSubscribed, FederateInternalError;

	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
	                                OrderType sentOrdering, TransportationType theTransport,
	                                RegionHandleSet sentRegions )
	    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
	    InteractionClassNotSubscribed, FederateInternalError;

	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
	                                OrderType sentOrdering, TransportationType theTransport,
	                                LogicalTime theTime, OrderType receivedOrdering )
	    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
	    InteractionClassNotSubscribed, FederateInternalError;

	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
	                                OrderType sentOrdering, TransportationType theTransport,
	                                LogicalTime theTime, OrderType receivedOrdering,
	                                RegionHandleSet regions ) throws InteractionClassNotRecognized,
	    InteractionParameterNotRecognized, InteractionClassNotSubscribed, FederateInternalError;

	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
	                                OrderType sentOrdering, TransportationType theTransport,
	                                LogicalTime theTime, OrderType receivedOrdering,
	                                MessageRetractionHandle messageRetractionHandle )
	    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
	    InteractionClassNotSubscribed, InvalidLogicalTime, FederateInternalError;

	public void receiveInteraction( InteractionClassHandle interactionClass,
	                                ParameterHandleValueMap theParameters, byte[] userSuppliedTag,
	                                OrderType sentOrdering, TransportationType theTransport,
	                                LogicalTime theTime, OrderType receivedOrdering,
	                                MessageRetractionHandle messageRetractionHandle,
	                                RegionHandleSet sentRegions )
	    throws InteractionClassNotRecognized, InteractionParameterNotRecognized,
	    InteractionClassNotSubscribed, InvalidLogicalTime, FederateInternalError;

	// 6.11
	public void removeObjectInstance( ObjectInstanceHandle theObject, byte[] userSuppliedTag,
	                                  OrderType sentOrdering ) throws ObjectInstanceNotKnown,
	    FederateInternalError;

	public void removeObjectInstance( ObjectInstanceHandle theObject, byte[] userSuppliedTag,
	                                  OrderType sentOrdering, LogicalTime theTime,
	                                  OrderType receivedOrdering ) throws ObjectInstanceNotKnown,
	    FederateInternalError;

	public void removeObjectInstance( ObjectInstanceHandle theObject, byte[] userSuppliedTag,
	                                  OrderType sentOrdering, LogicalTime theTime,
	                                  OrderType receivedOrdering,
	                                  MessageRetractionHandle retractionHandle )
	    throws ObjectInstanceNotKnown, InvalidLogicalTime, FederateInternalError;

	// 6.15
	public void attributesInScope( ObjectInstanceHandle theObject, AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed,
	    FederateInternalError;

	// 6.16
	public void attributesOutOfScope( ObjectInstanceHandle theObject,
	                                  AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotSubscribed,
	    FederateInternalError;

	// 6.18
	public void provideAttributeValueUpdate( ObjectInstanceHandle theObject,
	                                         AttributeHandleSet theAttributes,
	                                         byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
	    FederateInternalError;

	// 6.19
	public void turnUpdatesOnForObjectInstance( ObjectInstanceHandle theObject,
	                                            AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
	    FederateInternalError;

	// 6.20
	public void turnUpdatesOffForObjectInstance( ObjectInstanceHandle theObject,
	                                             AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
	    FederateInternalError;

	// /////////////////////////////////
	// Ownership Management Services //
	// /////////////////////////////////
	// 7.4
	public void requestAttributeOwnershipAssumption( ObjectInstanceHandle theObject,
	                                                 AttributeHandleSet offeredAttributes,
	                                                 byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeAlreadyOwned,
	    AttributeNotPublished, FederateInternalError;

	// 7.5
	public void requestDivestitureConfirmation( ObjectInstanceHandle theObject,
	                                            AttributeHandleSet offeredAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
	    AttributeDivestitureWasNotRequested, FederateInternalError;

	// 7.7
	public void attributeOwnershipAcquisitionNotification( ObjectInstanceHandle theObject,
	                                                       AttributeHandleSet securedAttributes,
	                                                       byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeAcquisitionWasNotRequested,
	    AttributeAlreadyOwned, AttributeNotPublished, FederateInternalError;

	// 7.10 
	public void attributeOwnershipUnavailable( ObjectInstanceHandle theObject,
	                                           AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeAlreadyOwned,
	    AttributeAcquisitionWasNotRequested, FederateInternalError;

	// 7.11 
	public void requestAttributeOwnershipRelease( ObjectInstanceHandle theObject,
	                                              AttributeHandleSet candidateAttributes,
	                                              byte[] userSuppliedTag )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeNotOwned,
	    FederateInternalError;

	// 7.15 
	public void confirmAttributeOwnershipAcquisitionCancellation( ObjectInstanceHandle theObject,
	                                                              AttributeHandleSet theAttributes )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, AttributeAlreadyOwned,
	    AttributeAcquisitionWasNotCanceled, FederateInternalError;

	// 7.17 
	public void informAttributeOwnership( ObjectInstanceHandle theObject,
	                                      AttributeHandle theAttribute, FederateHandle theOwner )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError;

	public void attributeIsNotOwned( ObjectInstanceHandle theObject, AttributeHandle theAttribute )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError;

	public void attributeIsOwnedByRTI( ObjectInstanceHandle theObject, AttributeHandle theAttribute )
	    throws ObjectInstanceNotKnown, AttributeNotRecognized, FederateInternalError;

	////////////////////////////// 
	// Time Management Services // 
	////////////////////////////// 
	// 8.3 
	public void timeRegulationEnabled( LogicalTime time ) throws InvalidLogicalTime,
	    NoRequestToEnableTimeRegulationWasPending, FederateInternalError;

	// 8.6 
	public void timeConstrainedEnabled( LogicalTime time ) throws InvalidLogicalTime,
	    NoRequestToEnableTimeConstrainedWasPending, FederateInternalError;

	//8.13 
	public void timeAdvanceGrant( LogicalTime theTime ) throws InvalidLogicalTime,
	    JoinedFederateIsNotInTimeAdvancingState, FederateInternalError;

	// 8.22 
	public void requestRetraction( MessageRetractionHandle theHandle ) throws FederateInternalError;
}
//end FederateAmbassador 
