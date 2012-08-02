/*
 *   Copyright 2008 The Portico Project
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
#ifndef SUBSCRIBEINTERACTIONWITHREGIONTEST_H_
#define SUBSCRIBEINTERACTIONWITHREGIONTEST_H_

#include "../common/Common.h"

class SubscribeInteractionWithRegionTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		TestNG6Federate *defaultFederate;
		TestNG6Federate *listenerFederate;
		RTI::InteractionClassHandle xHandle;
		RTI::ParameterHandle xaHandle, xbHandle, xcHandle;

		RTI::Region *senderRegion;
		RTI::Region *senderRegionOOB;
		RTI::Region *listenerRegion;
		RTI::Region *listenerRegionOOB;
		RTI::Region *otherRegion;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		SubscribeInteractionWithRegionTest();
		virtual ~SubscribeInteractionWithRegionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testICSubscribe();
		void testICSubscribeWithUnknownRegion();
		void testICSubscribeWithInvalidRegion();
		void testICSubscribeWithInvalidHandle();
		void testICSubscribeWhenNotJoined();
		
		void testICUnsubscribe();
		void testICUnsubscribeWithoutRegionRemovesAllSubscriptions();
		void testICUnsubscribeWithUnknownRegion();
		void testICUnsubscribeWithInvalidHandle();
		void testICUnsubscribeWhenNotSubscribed();
		void testICUnsubscribeWhenNotJoined();

	private:
		void validateSubscribed( RTI::Region *regionToSend );
		void validateNotSubscribed( RTI::Region *regionToSend );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( SubscribeInteractionWithRegionTest );
		CPPUNIT_TEST( testICSubscribe );
		CPPUNIT_TEST( testICSubscribeWithUnknownRegion );
		CPPUNIT_TEST( testICSubscribeWithInvalidRegion );
		CPPUNIT_TEST( testICSubscribeWithInvalidHandle );
		CPPUNIT_TEST( testICSubscribeWhenNotJoined );
		
		CPPUNIT_TEST( testICUnsubscribe );
		CPPUNIT_TEST( testICUnsubscribeWithoutRegionRemovesAllSubscriptions );
		CPPUNIT_TEST( testICUnsubscribeWithUnknownRegion );
		CPPUNIT_TEST( testICUnsubscribeWithInvalidHandle );
		CPPUNIT_TEST( testICUnsubscribeWhenNotSubscribed );
		CPPUNIT_TEST( testICUnsubscribeWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*SUBSCRIBEINTERACTIONWITHREGIONTEST_H_*/
