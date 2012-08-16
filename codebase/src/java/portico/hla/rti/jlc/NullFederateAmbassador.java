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
package hla.rti.jlc;

import hla.rti.*; 

/** 
 * Provides empty implementations for all methods in FederateAmbassador. 
 */ 

public class NullFederateAmbassador implements FederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	// 4.7
	public void synchronizationPointRegistrationFailed( String synchronizationPointLabel )
		throws FederateInternalError
	{
	}

	// 4.7
	public void synchronizationPointRegistrationSucceeded( String synchronizationPointLabel )
		throws FederateInternalError
	{
	}

	// 4.8
	public void announceSynchronizationPoint( String synchronizationPointLabel,
					byte[] userSuppliedTag ) throws FederateInternalError
	{
	}

	// 4.10
	public void federationSynchronized( String synchronizationPointLabel )
		throws FederateInternalError
	{
	}

	// 4.12
	public void initiateFederateSave( String label ) throws UnableToPerformSave,
		FederateInternalError
	{
	}

	// 4.15
	public void federationSaved() throws FederateInternalError
	{
	}

	// 4.15
	public void federationNotSaved() throws FederateInternalError
	{
	}

	// 4.17
	public void requestFederationRestoreSucceeded( String label ) throws FederateInternalError
	{
	}

	// 4.17
	public void requestFederationRestoreFailed( String label, String reason )
		throws FederateInternalError
	{
	}

	// 4.18
	public void federationRestoreBegun() throws FederateInternalError
	{
	}

	// 4.19
	public void initiateFederateRestore( String label, int federateHandle )
		throws SpecifiedSaveLabelDoesNotExist, CouldNotRestore, FederateInternalError
	{
	}

	// 4.21
	public void federationRestored() throws FederateInternalError
	{
	}

	// 4.21
	public void federationNotRestored() throws FederateInternalError
	{
	}

	// 5.10
	public void startRegistrationForObjectClass( int theClass ) throws ObjectClassNotPublished,
		FederateInternalError
	{
	}

	// 5.11
	public void stopRegistrationForObjectClass( int theClass ) throws ObjectClassNotPublished,
		FederateInternalError
	{
	}

	// 5.12
	public void turnInteractionsOn( int theHandle ) throws InteractionClassNotPublished,
		FederateInternalError
	{
	}

	// 5.13
	public void turnInteractionsOff( int theHandle ) throws InteractionClassNotPublished,
		FederateInternalError
	{
	}

	// 6.3
	public void discoverObjectInstance( int theObject, int theObjectClass, String objectName )
		throws CouldNotDiscover, ObjectClassNotKnown, FederateInternalError
	{
	}

	// 6.5
	public void reflectAttributeValues( int theObject, ReflectedAttributes theAttributes,
					byte[] userSuppliedTag ) throws ObjectNotKnown, AttributeNotKnown,
		FederateOwnsAttributes, FederateInternalError
	{
	}

	// 6.5
	public void reflectAttributeValues( int theObject, ReflectedAttributes theAttributes,
					byte[] userSuppliedTag, LogicalTime theTime,
					EventRetractionHandle retractionHandle ) throws ObjectNotKnown,
		AttributeNotKnown, FederateOwnsAttributes, InvalidFederationTime, FederateInternalError
	{
	}

	// 6.7
	public void receiveInteraction( int interactionClass, ReceivedInteraction theInteraction,
					byte[] userSuppliedTag ) throws InteractionClassNotKnown,
		InteractionParameterNotKnown, FederateInternalError
	{
	}

	// 6.7
	public void receiveInteraction( int interactionClass, ReceivedInteraction theInteraction,
					byte[] userSuppliedTag, LogicalTime theTime,
					EventRetractionHandle eventRetractionHandle ) throws InteractionClassNotKnown,
		InteractionParameterNotKnown, InvalidFederationTime, FederateInternalError
	{
	}

	// 6.9
	public void removeObjectInstance( int theObject, byte[] userSuppliedTag )
		throws ObjectNotKnown, FederateInternalError
	{
	}

	// 6.9
	public void removeObjectInstance( int theObject, byte[] userSuppliedTag, LogicalTime theTime,
					EventRetractionHandle retractionHandle ) throws ObjectNotKnown,
		InvalidFederationTime, FederateInternalError
	{
	}

	// 6.13
	public void attributesInScope( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
	{
	}

	// 6.14
	public void attributesOutOfScope( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
	{
	}

	// 6.16
	public void provideAttributeValueUpdate( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError
	{
	}

	// 6.17
	public void turnUpdatesOnForObjectInstance( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown, AttributeNotOwned, FederateInternalError
	{
	}

	// 6.18
	public void turnUpdatesOffForObjectInstance( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown, AttributeNotOwned, FederateInternalError
	{
	}

	// 7.4
	public void requestAttributeOwnershipAssumption( int theObject,
					AttributeHandleSet offeredAttributes, byte[] userSuppliedTag )
		throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned, AttributeNotPublished,
		FederateInternalError
	{
	}

	// 7.5
	public void attributeOwnershipDivestitureNotification( int theObject,
					AttributeHandleSet releasedAttributes ) throws ObjectNotKnown,
		AttributeNotKnown, AttributeNotOwned, AttributeDivestitureWasNotRequested,
		FederateInternalError
	{
	}

	// 7.6
	public void attributeOwnershipAcquisitionNotification( int theObject,
					AttributeHandleSet securedAttributes ) throws ObjectNotKnown,
		AttributeNotKnown, AttributeAcquisitionWasNotRequested, AttributeAlreadyOwned,
		AttributeNotPublished, FederateInternalError
	{
	}

	// 7.9
	public void attributeOwnershipUnavailable( int theObject, AttributeHandleSet theAttributes )
		throws ObjectNotKnown, AttributeNotKnown, AttributeAlreadyOwned,
		AttributeAcquisitionWasNotRequested, FederateInternalError
	{
	}

	// 7.10
	public void requestAttributeOwnershipRelease( int theObject,
					AttributeHandleSet candidateAttributes, byte[] userSuppliedTag )
		throws ObjectNotKnown, AttributeNotKnown, AttributeNotOwned, FederateInternalError
	{
	}

	// 7.14
	public void confirmAttributeOwnershipAcquisitionCancellation( int theObject,
					AttributeHandleSet theAttributes ) throws ObjectNotKnown, AttributeNotKnown,
		AttributeAlreadyOwned, AttributeAcquisitionWasNotCanceled, FederateInternalError
	{
	}

	// 7.16
	public void informAttributeOwnership( int theObject, int theAttribute, int theOwner )
		throws ObjectNotKnown, AttributeNotKnown, FederateInternalError
	{
	}

	// 7.16
	public void attributeIsNotOwned( int theObject, int theAttribute ) throws ObjectNotKnown,
		AttributeNotKnown, FederateInternalError
	{
	}

	// 7.16
	public void attributeOwnedByRTI( int theObject, int theAttribute ) throws ObjectNotKnown,
		AttributeNotKnown, FederateInternalError
	{
	}

	// 8.3
	public void timeRegulationEnabled( LogicalTime theFederateTime ) throws InvalidFederationTime,
		EnableTimeRegulationWasNotPending, FederateInternalError
	{
	}

	// 8.6
	public void timeConstrainedEnabled( LogicalTime theFederateTime ) throws InvalidFederationTime,
		EnableTimeConstrainedWasNotPending, FederateInternalError
	{
	}

	// 8.13
	public void timeAdvanceGrant( LogicalTime theTime ) throws InvalidFederationTime,
		TimeAdvanceWasNotInProgress, FederateInternalError
	{
	}

	// 8.22
	public void requestRetraction( EventRetractionHandle theHandle ) throws EventNotKnown,
		FederateInternalError
	{
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
