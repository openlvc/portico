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
#ifndef SUBSCRIBEOBJECTTEST_H_
#define SUBSCRIBEOBJECTTEST_H_

#include "../common/Common.h"

class SubscribeObjectTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		TestNG6Federate *defaultFederate;
		TestNG6Federate *listener;
		
		RTI::ObjectClassHandle bHandle;
		RTI::AttributeHandle   aaHandle;
		RTI::AttributeHandle   baHandle;
		RTI::AttributeHandleSet *theSet;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		SubscribeObjectTest();
		virtual ~SubscribeObjectTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testOCSubscribe();
		void testOCSubscribeWhenAlreadySubscribed();
		void testOCSubscribeWithEmptyHandleSet();
		void testOCSubscribeWithInvalidClassHandle();
		void testOCSubscribeWithInvalidAttributeHandle();
		void testOCSubscribeWhenNotJoined();

		void testOCUnsubscribe();
    	void testOCUnsubscribeWithInvalidClassHandle();
    	void testOCUnsubscribeWhenNotPublished();
    	void testOCUnsubscribeWhenNotJoined();

	private:
		void validateSubscribed();
		void validateNotSubscribed();
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( SubscribeObjectTest );
		CPPUNIT_TEST( testOCSubscribe );
		CPPUNIT_TEST( testOCSubscribeWhenAlreadySubscribed );
		CPPUNIT_TEST( testOCSubscribeWithEmptyHandleSet );
		CPPUNIT_TEST( testOCSubscribeWithInvalidClassHandle );
		CPPUNIT_TEST( testOCSubscribeWithInvalidAttributeHandle );
		CPPUNIT_TEST( testOCSubscribeWhenNotJoined );

		CPPUNIT_TEST( testOCUnsubscribe );
		CPPUNIT_TEST( testOCUnsubscribeWithInvalidClassHandle );
		CPPUNIT_TEST( testOCUnsubscribeWhenNotPublished );
		CPPUNIT_TEST( testOCUnsubscribeWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*SUBSCRIBEOBJECTTEST_H_*/
