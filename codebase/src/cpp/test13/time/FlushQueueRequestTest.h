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
#ifndef TIMEADVANCEREQUESTTEST_H_
#define TIMEADVANCEREQUESTTEST_H_

#include "../common/Common.h"

class FlushQueueRequestTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        Test13Federate *defaultFederate;
        Test13Federate *secondFederate;
        RTI::InteractionClassHandle xHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		FlushQueueRequestTest();
		virtual ~FlushQueueRequestTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testFqr();
		void testFqrWithTimeInPast();
		void testFqrWhenAdvanceInProgress();
		void testFqrWhenConstrainedPending();
		void testFqrWhenRegulatingPending();
		void testFqrWhenNotJoined();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( FlushQueueRequestTest );
		CPPUNIT_TEST( testFqr );
		CPPUNIT_TEST( testFqrWithTimeInPast );
		CPPUNIT_TEST( testFqrWhenAdvanceInProgress );
		CPPUNIT_TEST( testFqrWhenConstrainedPending );
		CPPUNIT_TEST( testFqrWhenRegulatingPending );
		CPPUNIT_TEST( testFqrWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*TIMEADVANCEREQUESTTEST_H_*/
