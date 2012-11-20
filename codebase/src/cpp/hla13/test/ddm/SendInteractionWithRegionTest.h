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
#ifndef SENDINTERACTIONWITHREGIONTEST_H_
#define SENDINTERACTIONWITHREGIONTEST_H_

#include "../common/Common.h"

class SendInteractionWithRegionTest : public CppUnit::TestFixture
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
		RTI::ParameterHandle xaHandle, xbHandle, xcHandle;
		
		RTI::Region *senderRegion;
		RTI::Region *senderRegionOOB;
		RTI::Region *listenerRegion;
		RTI::Region *otherRegion;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		SendInteractionWithRegionTest();
		virtual ~SendInteractionWithRegionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		///////////////////////////
		// receive order methods //
		///////////////////////////
		void testSendROInteractionWithRegion();
		void testSendROInteractionWithRegionAndNoParameters();
		void testSendROInteractionWithRegionAndInvalidClass();
		void testSendROInteractionWithRegionAndInvalidParameter();
		void testSendROInteractionWithRegionThatIsNotKnown();
		void testSendROInteractionWithRegionThatIsNotValid();
		void testSendROInteractionWithRegionWhenNotPublished();
		void testSendROInteractionWithRegionWhenNotJoined();

		/////////////////////////////
		// timestamp order methods //
		/////////////////////////////
		void testSendTSOInteractionWithRegion();
		void testSendTSOInteractionWithRegionAndNoParameters();
		void testSendTSOInteractionWithRegionAndInvalidClass();
		void testSendTSOInteractionWithRegionAndInvalidParameter();
		void testSendTSOInteractionWithRegionThatIsNotKnown();
		void testSendTSOInteractionWithRegionThatIsNotValid();
		void testSendTSOInteractionWithRegionWhenNotPublished();
		void testSendTSOInteractionWithRegionWithInvalidTime();
		void testSendTSOInteractionWithRegionWithTimeBelowLookahead();
		void testSendTSOInteractionWithRegionWhenNotJoined();

	private:
		void validateReceivedRO();
		void validateReceivedTSO();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( SendInteractionWithRegionTest );
		CPPUNIT_TEST( testSendROInteractionWithRegion );
		CPPUNIT_TEST( testSendROInteractionWithRegionAndNoParameters );
		CPPUNIT_TEST( testSendROInteractionWithRegionAndInvalidClass );
		CPPUNIT_TEST( testSendROInteractionWithRegionAndInvalidParameter );
		CPPUNIT_TEST( testSendROInteractionWithRegionThatIsNotKnown );
		CPPUNIT_TEST( testSendROInteractionWithRegionThatIsNotValid );
		CPPUNIT_TEST( testSendROInteractionWithRegionWhenNotPublished );
		CPPUNIT_TEST( testSendROInteractionWithRegionWhenNotJoined );
		
		CPPUNIT_TEST( testSendTSOInteractionWithRegion );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionAndNoParameters );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionAndInvalidClass );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionAndInvalidParameter );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionThatIsNotKnown );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionThatIsNotValid );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionWhenNotPublished );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionWithInvalidTime );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionWithTimeBelowLookahead );
		CPPUNIT_TEST( testSendTSOInteractionWithRegionWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*SENDINTERACTIONWITHREGIONTEST_H_*/
