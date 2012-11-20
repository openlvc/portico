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
#ifndef SENDINTERACTIONTEST_H_
#define SENDINTERACTIONTEST_H_

#include "../common/Common.h"

class SendInteractionTest : public CppUnit::TestFixture
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
		RTI::ParameterHandleValuePairSet *phvps;
		RTI::InteractionClassHandle xHandle;
		RTI::InteractionClassHandle yHandle;
		RTI::ParameterHandle xaHandle, xbHandle, xcHandle;
		RTI::ParameterHandle yaHandle, ybHandle, ycHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		SendInteractionTest();
		virtual ~SendInteractionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testSendROInteraction();
		void testSendROInteractionWithNoParameters();
		void testSendROInteractionWithInvalidClass();
		void testSendROInteractionWithInvalidParameter();
		void testSendROInteractionWhenNotPublished();
		void testSendROInteractionWhenNotJoined();
		
		void testSendTSOInteraction();
		void testSendTSOInteractionWithNoParameters();
		void testSendTSOInteractionWithInvalidClass();
		void testSendTSOInteractionWithInvalidParameter();
		void testSendTSOInteractionWhenNotPublished();
		void testSendTSOInteractionWithInvalidTime();
		void testSendTSOInteractionWithTimeBelowLookahead();
		void testSendTSOInteractionWhenNotJoined();
		
	private:
		void validateReceivedRO();
		void validateReceivedTSO();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( SendInteractionTest );
		CPPUNIT_TEST( testSendROInteraction );
		CPPUNIT_TEST( testSendROInteractionWithNoParameters );
		CPPUNIT_TEST( testSendROInteractionWithInvalidClass );
		CPPUNIT_TEST( testSendROInteractionWithInvalidParameter );
		CPPUNIT_TEST( testSendROInteractionWhenNotPublished );
		CPPUNIT_TEST( testSendROInteractionWhenNotJoined );
		
		CPPUNIT_TEST( testSendTSOInteraction );
		CPPUNIT_TEST( testSendTSOInteractionWithNoParameters );
		CPPUNIT_TEST( testSendTSOInteractionWithInvalidClass );
		CPPUNIT_TEST( testSendTSOInteractionWithInvalidParameter );
		CPPUNIT_TEST( testSendTSOInteractionWhenNotPublished );
		CPPUNIT_TEST( testSendTSOInteractionWithInvalidTime );
		CPPUNIT_TEST( testSendTSOInteractionWithTimeBelowLookahead );
		CPPUNIT_TEST( testSendTSOInteractionWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*SENDINTERACTIONTEST_H_*/
