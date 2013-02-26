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
#ifndef FEDERATEHANDLESETTEST_H_
#define FEDERATEHANDLESETTEST_H_

#include "../common/Common.h"

class FederateHandleSetTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        Test13Federate *defaultFederate;
        RTI::FederateHandleSet *theSet;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		FederateHandleSetTest();
		virtual ~FederateHandleSetTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testSize();
		void testGetHandle();
		void testGetHandleWithInvalidIndex();
		void testAdd();
		void testRemove();
		void testEmpty();
		//void testIsEmpty(); stupid FederateHandleSet doesn't have this!
		void testIsMember();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( FederateHandleSetTest );
		CPPUNIT_TEST( testSize );
		CPPUNIT_TEST( testGetHandle );
		CPPUNIT_TEST( testGetHandleWithInvalidIndex );
		CPPUNIT_TEST( testAdd );
		CPPUNIT_TEST( testRemove );
		CPPUNIT_TEST( testEmpty );
		//CPPUNIT_TEST( testIsEmpty );
		CPPUNIT_TEST( testIsMember );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*FEDERATEHANDLESETTEST_H_*/
