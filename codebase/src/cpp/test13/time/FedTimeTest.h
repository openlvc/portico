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
#ifndef FEDTIMETEST_H_
#define FEDTIMETEST_H_

#include "../common/Common.h"

class FedTimeTest : public CppUnit::TestFixture
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
		FedTimeTest();
		virtual ~FedTimeTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testEquals();
		void testDoesNotEqual();
		void testLessThan();
		void testGreaterThan();
		void testPlusEquals();
		void testMinusEquals();
		void testMultiplyEquals();
		void testDivideEquals();
		
		// Commenting these out, these functions aren't defined as FED_TIME_EXPORT in fedtime.hh,
		// and therefore don't seem to be publicly accessable under Windows
		// void testPlus();
		// void testMinus();
		// void testMultiply();
		// void testDivide();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( FedTimeTest );
		CPPUNIT_TEST( testEquals );
		CPPUNIT_TEST( testDoesNotEqual );
		CPPUNIT_TEST( testLessThan );
		CPPUNIT_TEST( testGreaterThan );
		CPPUNIT_TEST( testPlusEquals );
		CPPUNIT_TEST( testMinusEquals );
		CPPUNIT_TEST( testMultiplyEquals );
		CPPUNIT_TEST( testDivideEquals );
		//CPPUNIT_TEST( testPlus );
		//CPPUNIT_TEST( testMinus );
		//CPPUNIT_TEST( testMultiply );
		//CPPUNIT_TEST( testDivide );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*FEDTIMETEST_H_*/
