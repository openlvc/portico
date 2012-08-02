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

class TimeAdvanceRequestTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        TestNG6Federate *defaultFederate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		TimeAdvanceRequestTest();
		virtual ~TimeAdvanceRequestTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testTar();
		void testTarWhenInProgress();
		void testTarWhenConstrainedPending();
		void testTarWhenRegulatingPending();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( TimeAdvanceRequestTest );
		CPPUNIT_TEST( testTar );
		CPPUNIT_TEST( testTarWhenInProgress );
		CPPUNIT_TEST( testTarWhenConstrainedPending );
		CPPUNIT_TEST( testTarWhenRegulatingPending );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*TIMEADVANCEREQUESTTEST_H_*/
