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
#ifndef SUBSCRIBEOBJECTWITHREGIONTEST_H_
#define SUBSCRIBEOBJECTWITHREGIONTEST_H_

#include "../common/Common.h"

class SubscribeObjectWithRegionTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Test13Federate *defaultFederate;
		Test13Federate *listenerFederate;
		
		RTI::ObjectClassHandle aHandle;
		RTI::AttributeHandle   aaHandle;
		RTI::AttributeHandle   abHandle;
		RTI::AttributeHandleSet *theSet;
		
		RTI::Region *senderRegion;
		RTI::Region *senderRegionOOB;
		RTI::Region *listenerRegion;
		RTI::Region *listenerRegionOOB;
		RTI::Region *otherRegion;
		
		RTI::ObjectHandle ibObject;
		RTI::ObjectHandle oobObject;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		SubscribeObjectWithRegionTest();
		virtual ~SubscribeObjectWithRegionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testOCSubscribeWithRegion();
		void testOCSubscribeWithRegionAndEmptyHandleSet();
		void testOCSubscribeWithRegionUsingUnknownRegion();
		void testOCSubscribeWithRegionUsingInvalidRegion();
		void testOCSubscribeWithRegionWhenAlreadySubscribed();
		void testOCSubscribeWithRegionAndInvalidClassHandle();
		void testOCSubscribeWithRegionAndInvalidAttributeHandle();
		void testOCSubscribeWithRegionWhenNotJoined();

		void testOCUnsubscribeWithRegion();
		void testOCUnsubscribeWithRegionUsingUnknownRegion();
		void testOCUnsubscribeWithRegionAndInvalidClassHandle();
		void testOCUnsubscribeWithRegionWhenNotPublished();
		void testOCUnsubscribeWithRegionWhenNotJoined();

	private:
		void validateSubscribed( RTI::ObjectHandle testObject );
		void validateNotSubscribed( RTI::ObjectHandle testObject );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( SubscribeObjectWithRegionTest );
		CPPUNIT_TEST( testOCSubscribeWithRegion );
		CPPUNIT_TEST( testOCSubscribeWithRegionUsingUnknownRegion );
		CPPUNIT_TEST( testOCSubscribeWithRegionUsingInvalidRegion );
		CPPUNIT_TEST( testOCSubscribeWithRegionWhenAlreadySubscribed );
		CPPUNIT_TEST( testOCSubscribeWithRegionAndEmptyHandleSet );
		CPPUNIT_TEST( testOCSubscribeWithRegionAndInvalidClassHandle );
		CPPUNIT_TEST( testOCSubscribeWithRegionAndInvalidAttributeHandle );
		CPPUNIT_TEST( testOCSubscribeWithRegionWhenNotJoined );

		CPPUNIT_TEST( testOCUnsubscribeWithRegion );
		CPPUNIT_TEST( testOCUnsubscribeWithRegionUsingUnknownRegion );
		CPPUNIT_TEST( testOCUnsubscribeWithRegionAndInvalidClassHandle );
		CPPUNIT_TEST( testOCUnsubscribeWithRegionWhenNotPublished );
		CPPUNIT_TEST( testOCUnsubscribeWithRegionWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*SUBSCRIBEOBJECTWITHREGIONTEST_H_*/
