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
#ifndef SUBSCRIBEINTERACTIONTEST_H_
#define SUBSCRIBEINTERACTIONTEST_H_

#include "../common/Common.h"

class SubscribeInteractionTest : public CppUnit::TestFixture
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
		RTI::InteractionClassHandle xHandle;
		RTI::InteractionClassHandle yHandle;
		RTI::ParameterHandle xaHandle, xbHandle, xcHandle;
		RTI::ParameterHandle yaHandle, ybHandle, ycHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		SubscribeInteractionTest();
		virtual ~SubscribeInteractionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testICSubscribe();
		void testICSubscribeWithInvalidHandle();
		void testICSubscribeWhenNotJoined();
		
		void testICUnsubscribe();
		void testICUnsubscribeWithInvalidHandle();
		void testICUnsubscribeWhenNotSubscribed();
		void testICUnsubscribeWhenNotJoined();

	private:
		void validateSubscribed();
		void validateNotSubscribed();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( SubscribeInteractionTest );
		CPPUNIT_TEST( testICSubscribe );
		CPPUNIT_TEST( testICSubscribeWithInvalidHandle );
		CPPUNIT_TEST( testICSubscribeWhenNotJoined );

		CPPUNIT_TEST( testICUnsubscribe );
		CPPUNIT_TEST( testICUnsubscribeWithInvalidHandle );
		CPPUNIT_TEST( testICUnsubscribeWhenNotSubscribed );
		CPPUNIT_TEST( testICUnsubscribeWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*SUBSCRIBEINTERACTIONTEST_H_*/
