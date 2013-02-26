/*
 *   Copyright 2007 The Portico Project
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
#ifndef RESIGNFEDERATIONTEST_H_
#define RESIGNFEDERATIONTEST_H_

#include "../common/Common.h"

class ResignFederationTest : public CppUnit::TestFixture
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
		ResignFederationTest();
		virtual ~ResignFederationTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testResignWithNoAction();
		void testResignWithDeleteObjects();
		void testResignWithReleaseAttributes();
		void testResignWithDeleteObjectsAndReleaseAttributes();
		void testResignWithNoActionWhileAttributesAreOwned();
		void testResignFromFederationNotJoinedTo();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( ResignFederationTest );
		CPPUNIT_TEST( testResignWithNoAction );
		CPPUNIT_TEST( testResignWithDeleteObjects );
		CPPUNIT_TEST( testResignWithReleaseAttributes );
		CPPUNIT_TEST( testResignWithDeleteObjectsAndReleaseAttributes );
		CPPUNIT_TEST( testResignWithNoActionWhileAttributesAreOwned );
		CPPUNIT_TEST( testResignFromFederationNotJoinedTo );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*RESIGNFEDERATIONTEST_H_*/
