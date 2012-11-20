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
#ifndef NG6TESTFEDERATE_H_
#define NG6TESTFEDERATE_H_

#include "Common.h"

// forward declaration for circular dependency
class TestNG6FederateAmbassador;

class Test13Federate
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public:
		static char *SIMPLE_NAME;
		static int OWNER_UNOWNED;
		static int OWNER_RTI;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		char                      *federateName;
		RTI::FederateHandle       federateHandle;
	public:
		RTI::RTIambassador        *rtiamb;
		TestNG6FederateAmbassador *fedamb;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		Test13Federate( char *name );
		~Test13Federate();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		char* getFederateName();
		RTI::FederateHandle getFederateHandle();

		/////////////////////////////////////////
		///// federation management helpers /////
		/////////////////////////////////////////
		void quickCreate();
		void quickCreate( char *federationName );
		RTI::FederateHandle quickJoin();
		RTI::FederateHandle quickJoin( char *federationName );
		void quickResign();
		void quickResign( RTI::ResignAction action );
		void quickDestroy();
		void quickDestroy( char *federationName );
		void quickDestroyNoFail();

		/////////////////////////////////////////
		///// synchronization point helpers /////
		/////////////////////////////////////////
		void quickAnnounce( char *label, char *tag );
		void quickAnnounce( char *label, int federateCount, ... /* federate handles */ );
		void quickAchieved( char *label );

		/////////////////////////////////////////
		///// publish and subscribe helpers /////
		/////////////////////////////////////////
		void quickPublish( int objectClass, int attributeCount, ... /* attribute handles */ );
		void quickPublish( char *objectClass, int attributeCount, ... /* attribute names */ );
		void quickSubscribe( int objectClass, int attributeCount, ... /* attribute handles */ );
		void quickSubscribe( char *objectClass, int attributeCount, ... /* attribute names */ );

		void quickPublish( int interactionClass );
		void quickPublish( char *interactionClass );
		void quickSubscribe( int interactionClass );
		void quickSubscribe( char *interactionClass );
		
		void quickUnpublishOC( char *objectClass );

		///////////////////////////////////////////////
		///// object registration/removal helpers /////
		///////////////////////////////////////////////
		RTI::ObjectHandle quickRegister( int classHandle );
		RTI::ObjectHandle quickRegister( int classHandle, char *objectName );
		RTI::ObjectHandle quickRegister( char* className );
		RTI::ObjectHandle quickRegister( char* className, char *objectName );
		void              quickRegisterFail( int classHandle );
		void              quickDelete( int objectHandle, char *tag );

		//////////////////////////////////////////////
		///// reflection and interaction helpers /////
		//////////////////////////////////////////////
		void quickReflect( int objectHandle, RTI::AttributeHandleValuePairSet *ahvps, char *tag );
		void quickReflect( int objectHandle, int attributeCount, ... /* attribute names */ );
		void quickReflectFail( int handle, RTI::AttributeHandleValuePairSet *ahvps, char *tag );
		
		void quickSend( int classHandle, RTI::ParameterHandleValuePairSet *phvps, char *tag );
		void quickSend( int classHandle, int parameterCount, ... /* parameter names */ );
		void quickSend( int classHandle, double time, int parameterCount, ... /* parameter names */ );
		void quickSendFail( int classHandle, RTI::ParameterHandleValuePairSet *phvps, char *tag );

		////////////////////////////////////////////////
		///// data distribution management methods /////
		////////////////////////////////////////////////
		RTI::SpaceHandle     quickSpaceHandle( char* spaceName );
		RTI::DimensionHandle quickDimensionHandle( char* spaceName, char* dimensionName );
		RTI::Region*         quickCreateRegion( RTI::SpaceHandle space, RTI::ULong extents );
		RTI::Region*         quickCreateTestRegion( RTI::ULong lowerBound, RTI::ULong upperBound );
		RTI::Region*         quickCreateOtherRegion( RTI::ULong lowerBound, RTI::ULong upperBound );
		void                 quickModifyRegion( RTI::Region* theRegion );
		RTI::RegionToken     quickGetRegionToken( RTI::Region* theRegion );
		RTI::Region*         quickGetRegion( RTI::RegionToken regionToken );
		
		RTI::ObjectHandle    quickRegisterWithRegion( char* objectClass,
		                                              RTI::Region* theRegion,
		                                              int attributeCount,
		                                              ... /* attribute names */ );
		void                 quickAssociateWithRegion( RTI::ObjectHandle theObject,
		                                               RTI::Region* theRegion,
		                                               int attributeCount,
		                                               ... /* attribute names */ );
		void                 quickUnassociateWithRegion( RTI::ObjectHandle theObject,
		                                                 RTI::Region* theRegion );

		void                 quickSubscribeWithRegion( RTI::ObjectClassHandle classHandle,
		                                               RTI::Region* region,
		                                               int attributeCount,
		                                               ... /* attribute handles */ );
		void                 quickSubscribeWithRegion( char* className,
		                                               RTI::Region* region,
		                                               int attributeCount, 
		                                               ... /* attribute names */ );
		void                 quickUnsubscribeOCWithRegion( RTI::ObjectClassHandle classHandle,
		                                                   RTI::Region *region );
		void                 quickUnsubscribeOCWithRegion( char* className, RTI::Region* region );

		void                 quickSubscribeWithRegion( RTI::InteractionClassHandle classhandle,
		                                               RTI::Region* region );
		void                 quickSubscribeWithRegion( char* className, RTI::Region* region );
		void                 quickUnsubscribeICWithRegion( RTI::InteractionClassHandle classHandle,
		                                                   RTI::Region *region );
		void                 quickUnsubscribeICWithRegion( char* className, RTI::Region *region );

		void quickSendWithRegion( char *classHandle,
		                          RTI::Region *region,
		                          int parameterCount,
		                          ... /* parameter names */ );

		//////////////////////////////////////////
		///// time management helper methods /////
		//////////////////////////////////////////
		double getTime( RTI::FedTime *time );
		void   quickEnableConstrained();
		void   quickEnabledConstrainedRequest();
		void   quickDisableConstrained();
		void   quickEnableAsync();
		void   quickDisableAsync();
		void   quickEnableRegulating( double lookahead );
		void   quickEnableRegulatingRequest( double lookahead );
		void   quickDisableRegulating();
		double quickQueryLookahead();
		void   quickModifyLookahead( double newLookahead );
		void   quickAdvanceRequest( double newTime );
		void   quickAdvanceAndWait( double newTime );
		void   quickAdvanceRequestAvailable( double newTime );
		void   quickNextEventRequest( double newTime );
		void   quickFlushQueueRequest( double maxTime );

		////////////////////////////////////////
		///// ownership management methods /////
		////////////////////////////////////////
		void quickAcquireIfAvailableRequest( RTI::ObjectHandle theObject, int attributeCount, ... );
		void quickAcquireRequest( RTI::ObjectHandle theObject, int attributeCount, ... );
		void quickUnconditionalRelease( RTI::ObjectHandle theObject, int attributeCount, ... );
		void quickNegotiatedRelease( RTI::ObjectHandle theObject, int attributeCount, ... );
		void quickReleaseResponse( RTI::ObjectHandle theObject, int attributeCount, ... );
		int  quickQueryOwnership( RTI::ObjectHandle theObject, RTI::AttributeHandle theAttribute );
		
		/*
		 * This method will assert that the given federate is the owner of all the specified
		 * attributes of the given object. If one of them is not, the current test will be failed.
		 * Note that you can use TestNG6Federate::OWNER_UNOWNED and TestNG6Federate::OWNER_RTI as
		 * the federate handle if it is appropriate for what you are trying to test.
		 */
		void quickAssertOwnedBy( int federate, RTI::ObjectHandle theObject, int attCount, ... );
		
		//////////////////////////////////////////////
		////////// save and restore helpers //////////
		//////////////////////////////////////////////
		void quickSaveRequest( char *saveLabel );
		void quickSaveBegun();
		void quickSaveComplete();
		void quickSaveNotComplete();
		void quickSaveInProgress( char *saveLabel );
		void quickSaveToCompletion( char *saveLabel, int federateCount, ... );
		
		void quickRestoreRequest( char *saveLabel );
		void quickRestoreRequestSuccess( char *saveLabel );
		void quickRestoreComplete();
		void quickRestoreNotComplete();
		void quickRestoreInProgress( char *saveLabel, int federateCount, ... );
		
		////////////////////////////////////////////
		///// handle resolution helper methods /////
		////////////////////////////////////////////
		RTI::ObjectClassHandle      quickOCHandle( char *objectClass );
		RTI::AttributeHandle        quickACHandle( char *objectClass, char *attributeName );
		RTI::InteractionClassHandle quickICHandle( char *interactionClass );
		RTI::ParameterHandle        quickPCHandle( char *interactionClass, char *parameterName );
		RTI::ObjectClassHandle      quickOCHandle( int objectHandle );
		char*                       quickOCName( RTI::ObjectClassHandle classHandle );
		char*                       quickOCNameForInstance( int objectHandle );
		char*                       quickICName( int interactionHandle );

		/////////////////////////////////////////////
		///// container type management helpers /////
		/////////////////////////////////////////////
		RTI::AttributeHandleSet*          createAHS( int size );
		RTI::AttributeHandleSet*          populatedAHS( int size, ... );
		RTI::FederateHandleSet*           createFHS( int size );
		RTI::FederateHandleSet*           populatedFHS( int size, ... );
		RTI::AttributeHandleValuePairSet* createAHVPS( int size );
		RTI::ParameterHandleValuePairSet* createPHVPS( int size );

		//////////////////////////////////////////
		///// general utility helper methods /////
		//////////////////////////////////////////
		RTI::RTIambassador* getRtiAmb();
		void quickTick();
		void quickTick( double min, double max );
		void killTest( RTI::Exception &e, char *activeMethod );
		void killTest( const char *format, ... );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

#endif /*NG6TESTFEDERATE_H_*/
