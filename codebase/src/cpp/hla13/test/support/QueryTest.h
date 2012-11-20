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
#ifndef QUERYTEST_H_
#define QUERYTEST_H_

#include "../common/Common.h"

class QueryTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        Test13Federate *defaultFederate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		QueryTest();
		virtual ~QueryTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testQueryFederateTime();
		void testQueryLBTS();
		void testQueryLookahead();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( QueryTest );
		CPPUNIT_TEST( testQueryFederateTime );
		CPPUNIT_TEST( testQueryLBTS );
		CPPUNIT_TEST( testQueryLookahead );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*QUERYTEST_H_*/
