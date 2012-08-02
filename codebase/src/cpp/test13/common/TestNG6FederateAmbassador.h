/*
 *   Copyright 2007 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 *
 */
#ifndef NG6TESTFEDERATEAMBASSADOR_H_
#define NG6TESTFEDERATEAMBASSADOR_H_

#include "Common.h"
#include "NullFederateAmbassador.hh"
#include "ActiveSR.h"
#include <map>
#include <set>
#include <time.h>

using namespace std;

// forward declaration for circular dependency
class TestNG6Federate;
class TestNG6Object;
class TestNG6Interaction;
class ActiveSR;

//////////////////////////////////////////
// struct with map comparision function //
//////////////////////////////////////////
struct ltstr
{
	bool operator() ( char* s1, char* s2 ) const
	{
		return strcmp(s1, s2) < 0;
	}
};

///////////////////////////////////////////
// enum to reprepresent sync reg results //
///////////////////////////////////////////
enum SyncRegResult{ WAITING, FAILURE, SUCCESS };

/*
 * This class represents the FederateAmbassador for the test federate. In addition to keeping
 * track of a bunch of important state information, it also provides a number of useful utility
 * methods that can be used by the TestFederate to make its life easier. These are commonly
 * "waitForXxx" methods that will wait for a certain amount of time for an event to happen before
 * it goes ahead and fails the test.
 */
class TestNG6FederateAmbassador : public NullFederateAmbassador
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public:
		static int    STRING_SIZE;
		static double TIMEOUT;
		static int    BUFFER_SIZE;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		TestNG6Federate *testFederate;

	public: // were "protected" in Java version as that really means "package", make them
		    // public here just to make for easier access
		RTI::Boolean               constrained;
		RTI::Boolean               regulating;
		double                     logicalTime;
		map<char*, char*, ltstr>   *announced;
		set<char*, ltstr>          *synchronized;
		SyncRegResult              syncRegResult;
		
		// object/interaction containers
		map<RTI::ObjectHandle,TestNG6Object*> *discovered;
		map<RTI::ObjectHandle,TestNG6Object*> *roRemoved;
		map<RTI::ObjectHandle,TestNG6Object*> *tsoRemoved;
		set<RTI::ObjectHandle> *roUpdated;
		set<RTI::ObjectHandle> *tsoUpdated;
		map<RTI::ObjectHandle,set<RTI::AttributeHandle>*> *requestedUpdates;
		
		// ownership management containers
		map<RTI::ObjectHandle,set<RTI::AttributeHandle>*> *attributesOffered;
		map<RTI::ObjectHandle,set<RTI::AttributeHandle>*> *attributesDivested;
		map<RTI::ObjectHandle,set<RTI::AttributeHandle>*> *attributesAcquired;
		map<RTI::ObjectHandle,set<RTI::AttributeHandle>*> *attributesUnavailable;
		map<RTI::ObjectHandle,set<RTI::AttributeHandle>*> *attributesRequested;
		map<RTI::AttributeHandle,int> *attributeOwnershipInfo;
		map<RTI::ObjectHandle,set<RTI::AttributeHandle>*> *attributesCancelled;
		
		vector<TestNG6Interaction*> *roInteractions;
		vector<TestNG6Interaction*> *tsoInteractions;
		
		// save/restore stuff
		ActiveSR *currentSave;
		ActiveSR *currentRestore;
		char *successfulRestoreRequest;
		char *failedRestoreRequest;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		TestNG6FederateAmbassador( TestNG6Federate *testFederate );
		~TestNG6FederateAmbassador() throw( RTI::FederateInternalError );

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Testing Convenience Methods //////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	private:
		void         tick();
		time_t       getCurrentTime();
		void         checkTimeout( time_t startTime, char *currentMethod );
		RTI::Boolean checkTimeoutNonFatal( time_t startTime );
	
		// helper methods
		TestNG6Interaction* fetchInteraction( RTI::InteractionClassHandle theClass,
		                                      vector<TestNG6Interaction*> *theStore );
		
		/** search the received set and return true if all the expected handles are in received */
		RTI::Boolean setContainsAll( set<RTI::AttributeHandle>* expected,
		                             set<RTI::AttributeHandle>* received );
		
		/** does a string copy from source to target, but will delete the memory at target
		    is it isn't null and will allocate new memory. Really just a "cleanAndCopy" */
		void copyString( char *target, const char *source );
		
	public:
		// synchronization point methods
		RTI::Boolean isAnnounced( char* label );
		RTI::Boolean isSynchronized( char* label );
		char*        waitForSyncAnnounce( char* label );
		void         waitForSyncAnnounceTimeout( char* label );
		RTI::Boolean waitForSyncRegResult( char* label );
		void         waitForSyncRegResult( char* label, RTI::Boolean expectedResult );
		void         waitForSynchronized( char* label );
		void         waitForSynchornizedTimeout( char* label );
		
		// logical time management methods
		void         waitForConstrainedEnabled();
		void         waitForRegulatingEnabled();
		void         waitForTimeAdvance( double newTime );
		
		// object management methods
		// discover
		TestNG6Object* waitForDiscovery( RTI::ObjectHandle objectHandle );
		TestNG6Object* waitForDiscoveryAs( RTI::ObjectHandle object, RTI::ObjectClassHandle asClass );
		TestNG6Object* waitForDiscoveryAsWithName( RTI::ObjectHandle object,
		                                           RTI::ObjectClassHandle asClass,
		                                           char *expectedName );
		void         waitForDiscoveryTimeout( RTI::ObjectHandle objectHandle );
		
		// remove
		void         waitForRORemoval( RTI::ObjectHandle objectHandle );
		void         waitForRORemovalTimeout( RTI::ObjectHandle objectHandle );
		void         waitForTSORemoval( RTI::ObjectHandle objectHandle );
		void         waitForTSORemovalTimeout( RTI::ObjectHandle objectHandle );
		
		// reflect
		TestNG6Object*      waitForROUpdate( RTI::ObjectHandle objectHandle );
		void                waitForROUpdateTimeout( RTI::ObjectHandle objectHandle );
		TestNG6Object*      waitForTSOUpdate( RTI::ObjectHandle objectHandle );
		void                waitForTSOUpdateTimeout( RTI::ObjectHandle objectHandle );
		
		// interaction
		// user is responsible for deleting all returned interactions
		TestNG6Interaction* waitForROInteraction( RTI::InteractionClassHandle expected );
		void                waitForROInteractionTimeout( RTI::InteractionClassHandle expected );
		TestNG6Interaction* waitForTSOInteraction( RTI::InteractionClassHandle expected );
		void                waitForTSOInteractionTimeout( RTI::InteractionClassHandle expected );
		
		// provide update requests
		set<RTI::AttributeHandle>* waitForProvideRequest( RTI::ObjectHandle theObject );
		void                       waitForProvideRequestTimeout( RTI::ObjectHandle theObject );
		
		// ownership
		void waitForOwnershipAcquistion( RTI::ObjectHandle theObject, int attributes, ... );
		void waitForOwnershipOffered( RTI::ObjectHandle theObject, int attributes, ... );
		void waitForOwnershipDivested( RTI::ObjectHandle theObject, int attributes, ... );
		void waitForOwnershipUnavailable( RTI::ObjectHandle theObject, int attributes, ... );
		void waitForOwnershipRequest( RTI::ObjectHandle theObject, int attributes, ... );
		int  waitForOwnershipResponse( RTI::ObjectHandle object, RTI::AttributeHandle attribute );
		void waitForOwnershipCancelConfirmation( RTI::ObjectHandle object, int attributes, ... );
		
		// save restore
		void waitForSaveInitiated( char *saveLabel );
		void waitForSaveInitiatedTimeout( char *saveLabel );
		void waitForFederationSaved();
		void waitForFederationNotSaved();
		
		void waitForFederationRestoreBegun();
		int waitForFederateRestoreInitiated( char* label );
		void waitForFederateRestoreInitiatedTimeout( char* label );
		void waitForFederationRestored();
		void waitForFederationRestoredTimeout( char* label );
		void waitForFederationNotRestored();
		void waitForRestoreRequestSuccess( char *label );
		void waitForRestoreRequestFailure( char *label );
		
	//////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// FederateAmbassador Methods ///////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////
	public:
	
		//////////////////////////////////
		///// General Helper Methods /////
		//////////////////////////////////
		double convertTime( const RTI::FedTime& time );
	
		////////////////////////////////////////////
		///// Synchornization Point Management /////
		////////////////////////////////////////////
		virtual void synchronizationPointRegistrationSucceeded( const char *label )
			throw( RTI::FederateInternalError );
	
		virtual void synchronizationPointRegistrationFailed( const char *label )
			throw( RTI::FederateInternalError );
	
		virtual void announceSynchronizationPoint( const char *label, const char *tag )
			throw( RTI::FederateInternalError );
	
		virtual void federationSynchronized( const char *label )
			throw( RTI::FederateInternalError );
	
		///////////////////////////////////////
		///// Save and Restore Management /////
		///////////////////////////////////////
		virtual void initiateFederateSave( const char *label )
			throw( RTI::UnableToPerformSave, RTI::FederateInternalError );
	
		virtual void federationSaved() throw( RTI::FederateInternalError );
	
		virtual void federationNotSaved() throw( RTI::FederateInternalError );
	
		virtual void requestFederationRestoreSucceeded( const char *label )
			throw( RTI::FederateInternalError );
	
		virtual void requestFederationRestoreFailed( const char *label, const char *reason )
			throw( RTI::FederateInternalError );
	
		virtual void federationRestoreBegun() throw( RTI::FederateInternalError );
	
		virtual void initiateFederateRestore( const char *label, RTI::FederateHandle handle )
			throw( RTI::SpecifiedSaveLabelDoesNotExist,
			       RTI::CouldNotRestore,
			       RTI::FederateInternalError );
	
		virtual void federationRestored() throw( RTI::FederateInternalError );
	
		virtual void federationNotRestored() throw( RTI::FederateInternalError );
	
		///////////////////////////////////////////
		///// Declaration Management Services /////
		///////////////////////////////////////////
	
		virtual void startRegistrationForObjectClass( RTI::ObjectClassHandle theClass )
			throw( RTI::ObjectClassNotPublished, RTI::FederateInternalError );
	
		virtual void stopRegistrationForObjectClass( RTI::ObjectClassHandle theClass )
			throw( RTI::ObjectClassNotPublished, RTI::FederateInternalError );
	
		virtual void turnInteractionsOn( RTI::InteractionClassHandle theHandle )
			throw( RTI::InteractionClassNotPublished, RTI::FederateInternalError );
	
		virtual void turnInteractionsOff( RTI::InteractionClassHandle theHandle )
			throw( RTI::InteractionClassNotPublished, RTI::FederateInternalError );
	
		//////////////////////////////////////
		///// Object Management Services /////
		//////////////////////////////////////
		virtual void discoverObjectInstance( RTI::ObjectHandle theObject,
		                                     RTI::ObjectClassHandle objectClass,
		                                     const char* theObjectName )
			throw( RTI::CouldNotDiscover, RTI::ObjectClassNotKnown, RTI::FederateInternalError );
	
		virtual void reflectAttributeValues( RTI::ObjectHandle theObject,
		                                     const RTI::AttributeHandleValuePairSet& theAttributes,
		                                     const RTI::FedTime& theTime,
		                                     const char *theTag,
		                                     RTI::EventRetractionHandle theHandle )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::FederateOwnsAttributes,
			       RTI::InvalidFederationTime,
			       RTI::FederateInternalError );
	
		virtual void reflectAttributeValues( RTI::ObjectHandle theObject,
		                                     const RTI::AttributeHandleValuePairSet& theAttributes,
		                                     const char *theTag )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::FederateOwnsAttributes,
			       RTI::FederateInternalError );
	
		virtual void receiveInteraction( RTI::InteractionClassHandle theInteraction,
		                                 const RTI::ParameterHandleValuePairSet& theParameters,
		                                 const RTI::FedTime& theTime,
		                                 const char *theTag,
		                                 RTI::EventRetractionHandle theHandle )
			throw( RTI::InteractionClassNotKnown,
			       RTI::InteractionParameterNotKnown,
			       RTI::InvalidFederationTime,
			       RTI::FederateInternalError );
	
		virtual void receiveInteraction( RTI::InteractionClassHandle theInteraction,
		                                const RTI::ParameterHandleValuePairSet& theParameters,
		                                const char *theTag )
			throw( RTI::InteractionClassNotKnown,
			       RTI::InteractionParameterNotKnown,
			       RTI::FederateInternalError );
	
		virtual void removeObjectInstance( RTI::ObjectHandle theObject,
		                                   const RTI::FedTime& theTime,
		                                   const char *theTag,
		                                   RTI::EventRetractionHandle theHandle )
			throw( RTI::ObjectNotKnown,
			       RTI::InvalidFederationTime,
			       RTI::FederateInternalError );
	
		virtual void removeObjectInstance( RTI::ObjectHandle theObject, const char *theTag )
			throw( RTI::ObjectNotKnown, RTI::FederateInternalError );
	
		virtual void attributesInScope( RTI::ObjectHandle theObject,
		                                const RTI::AttributeHandleSet& theAttributes )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::FederateInternalError );
	
		virtual void attributesOutOfScope( RTI::ObjectHandle theObject,
		                                   const RTI::AttributeHandleSet& theAttributes )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::FederateInternalError );
	
		virtual void provideAttributeValueUpdate( RTI::ObjectHandle theObject,
		                                          const RTI::AttributeHandleSet& theAttributes )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::AttributeNotOwned,
			       RTI::FederateInternalError );
	
		virtual void turnUpdatesOnForObjectInstance( RTI::ObjectHandle theObject,
		                                             const RTI::AttributeHandleSet& theAttributes )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotOwned,
			       RTI::FederateInternalError );
	
		virtual void turnUpdatesOffForObjectInstance( RTI::ObjectHandle theObject,
		                                              const RTI::AttributeHandleSet& theAttributes )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotOwned,
			       RTI::FederateInternalError );
	
		/////////////////////////////////////////
		///// Ownership Management Services /////
		/////////////////////////////////////////
	
		virtual void requestAttributeOwnershipAssumption( RTI::ObjectHandle theObject,
		                                                  const RTI::AttributeHandleSet& offered,
		                                                  const char *theTag )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::AttributeAlreadyOwned,
			       RTI::AttributeNotPublished,
			       RTI::FederateInternalError );
	
		virtual void attributeOwnershipDivestitureNotification(
		                 RTI::ObjectHandle theObject,
		                 const RTI::AttributeHandleSet& released )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::AttributeNotOwned,
			       RTI::AttributeDivestitureWasNotRequested,
			       RTI::FederateInternalError );
	
		virtual void attributeOwnershipAcquisitionNotification(
		                 RTI::ObjectHandle theObject,
		                 const RTI::AttributeHandleSet& secured )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::AttributeAcquisitionWasNotRequested,
			       RTI::AttributeAlreadyOwned,
			       RTI::AttributeNotPublished,
			       RTI::FederateInternalError );
	
		virtual void attributeOwnershipUnavailable( RTI::ObjectHandle theObject,
		                                            const RTI::AttributeHandleSet& theAttributes )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown, 
			       RTI::AttributeAlreadyOwned,
			       RTI::AttributeAcquisitionWasNotRequested,
			       RTI::FederateInternalError );
	
		virtual void requestAttributeOwnershipRelease( RTI::ObjectHandle theObject,
		                                               const RTI::AttributeHandleSet& candidates,
		                                               const char *theTag )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::AttributeNotOwned,
			       RTI::FederateInternalError );
	
		virtual void confirmAttributeOwnershipAcquisitionCancellation(
		                 RTI::ObjectHandle theObject,
		                 const RTI::AttributeHandleSet& theAttributes )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::AttributeAlreadyOwned,
			       RTI::AttributeAcquisitionWasNotCanceled,
			       RTI::FederateInternalError );
	
		virtual void informAttributeOwnership( RTI::ObjectHandle theObject,
		                                       RTI::AttributeHandle theAttribute,
		                                       RTI::FederateHandle theOwner )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::FederateInternalError );
	
		virtual void attributeIsNotOwned( RTI::ObjectHandle theObject,
		                                  RTI::AttributeHandle theAttribute )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::FederateInternalError );
	
		virtual void attributeOwnedByRTI( RTI::ObjectHandle theObject,
		                                  RTI::AttributeHandle theAttribute )
			throw( RTI::ObjectNotKnown,
			       RTI::AttributeNotKnown,
			       RTI::FederateInternalError );
	
		////////////////////////////////////
		///// Time Management Services /////
		////////////////////////////////////
		virtual void timeRegulationEnabled( const RTI::FedTime& theFederateTime )
			throw( RTI::InvalidFederationTime,
			       RTI::EnableTimeRegulationWasNotPending,
			       RTI::FederateInternalError );
	
		virtual void timeConstrainedEnabled( const RTI::FedTime& federateTime )
			throw( RTI::InvalidFederationTime,
			       RTI::EnableTimeConstrainedWasNotPending,
			       RTI::FederateInternalError );
	
		virtual void timeAdvanceGrant( const RTI::FedTime& theTime )
			throw( RTI::InvalidFederationTime,
			       RTI::TimeAdvanceWasNotInProgress,
			       RTI::FederateInternalError );
	
		virtual void requestRetraction( RTI::EventRetractionHandle theHandle )
			throw( RTI::EventNotKnown,
			       RTI::FederateInternalError );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
};

#endif /*NG6TESTFEDERATEAMBASSADOR_H_*/
