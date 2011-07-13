#ifndef EXAMPLECPPFEDERATE_H_
#define EXAMPLECPPFEDERATE_H_

#include "ExampleFedAmb.h"
#include "RTI.hh"

#define READY_TO_RUN "ReadyToRun"

class ExampleCPPFederate
{
	public:
		RTI::RTIambassador *rtiamb;
		ExampleFedAmb      *fedamb;

		// fom handles //
		RTI::ObjectClassHandle      aHandle;
		RTI::AttributeHandle        aaHandle;
		RTI::AttributeHandle        abHandle;
		RTI::AttributeHandle        acHandle;
		RTI::InteractionClassHandle xHandle;
		RTI::ParameterHandle        xaHandle;
		RTI::ParameterHandle        xbHandle;

		// public methods //
		ExampleCPPFederate();
		virtual ~ExampleCPPFederate();
		void runFederate( char* federateName );


	private:
		void initializeHandles();
		void waitForUser();
		void enableTimePolicy();
		void publishAndSubscribe();
		RTI::ObjectHandle registerObject();
		void updateAttributeValues( RTI::ObjectHandle objectHandle );
		void sendInteraction();
		void advanceTime( double timestep );
		void deleteObject( RTI::ObjectHandle objectHandle );
		double getLbts();
};

#endif /*EXAMPLECPPFEDERATE_H_*/
