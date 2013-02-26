#ifndef EXAMPLEFEDAMB_H_
#define EXAMPLEFEDAMB_H_

#include <RTI.hh>
#include <NullFederateAmbassador.hh>

class ExampleFedAmb : public NullFederateAmbassador
{
	public:
		// variables //
		double federateTime;
		double federateLookahead;
	
		bool isRegulating;
		bool isConstrained;
		bool isAdvancing;
		bool isAnnounced;
		bool isReadyToRun;

		// methods //
		ExampleFedAmb();
		virtual ~ExampleFedAmb() throw( RTI::FederateInternalError );
		
		///////////////////////////////////
		// synchronization point methods //
		///////////////////////////////////
		virtual void synchronizationPointRegistrationSucceeded( const char *label )
			throw( RTI::FederateInternalError );

		virtual void synchronizationPointRegistrationFailed( const char *label )
			throw( RTI::FederateInternalError );

		virtual void announceSynchronizationPoint( const char *label, const char *tag )
			throw( RTI::FederateInternalError );

		virtual void federationSynchronized( const char *label )
			throw( RTI::FederateInternalError );
		
		//////////////////////////
		// time related methods //
		//////////////////////////
		virtual void timeRegulationEnabled( const RTI::FedTime& theFederateTime )
			throw( RTI::InvalidFederationTime,
			       RTI::EnableTimeRegulationWasNotPending,
			       RTI::FederateInternalError );

		virtual void timeConstrainedEnabled( const RTI::FedTime& theFederateTime )
			throw( RTI::InvalidFederationTime,
			       RTI::EnableTimeConstrainedWasNotPending,
			       RTI::FederateInternalError );

		virtual void timeAdvanceGrant( const RTI::FedTime& theTime )
			throw( RTI::InvalidFederationTime,
			       RTI::TimeAdvanceWasNotInProgress,
			       RTI::FederateInternalError );
		
		///////////////////////////////
		// object management methods //
		///////////////////////////////
		virtual void discoverObjectInstance( RTI::ObjectHandle theObject,
		                                     RTI::ObjectClassHandle theObjectClass,
		                                     const char* theObjectName )  
			throw( RTI::CouldNotDiscover,
			       RTI::ObjectClassNotKnown,
			       RTI::FederateInternalError );

		virtual void reflectAttributeValues( RTI::ObjectHandle theObject,
		                                     const RTI::AttributeHandleValuePairSet& theAttributes,
		                                     const RTI::FedTime& theTime,
		                                     const char *theTag,
		                                     RTI::EventRetractionHandle theHandle)
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
		                                   RTI::EventRetractionHandle theHandle)
			throw( RTI::ObjectNotKnown,
			       RTI::InvalidFederationTime,
			       RTI::FederateInternalError );

		virtual void removeObjectInstance( RTI::ObjectHandle theObject, const char *theTag )
			throw( RTI::ObjectNotKnown, RTI::FederateInternalError );
	
		
	/////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// Private Section ////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	private:
		double convertTime( const RTI::FedTime& theTime );

};

#endif /*EXAMPLEFEDAMB_H_*/
