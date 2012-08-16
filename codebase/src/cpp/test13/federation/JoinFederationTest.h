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
#ifndef JOINFEDERATIONTEST_H_
#define JOINFEDERATIONTEST_H_

#include "../common/Common.h"

class JoinFederationTest : public CppUnit::TestFixture
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
		JoinFederationTest();
		virtual ~JoinFederationTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testJoinFederation();
		void testJoinFederationThatDoesNotExist();
		void testJoinFederationWithNullFederationName();
		void testJoinFederationWithNullFederateName();
		void testJoinFederationWithEmptyFederateName();
		void testJoinFederationWithNullFederateAmbassador();
		void testJoinFederationWhenAlreadyJoined();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( JoinFederationTest );
		CPPUNIT_TEST( testJoinFederation );
		CPPUNIT_TEST( testJoinFederationThatDoesNotExist );
		CPPUNIT_TEST( testJoinFederationWithNullFederationName );
		CPPUNIT_TEST( testJoinFederationWithNullFederateName );
		CPPUNIT_TEST( testJoinFederationWithEmptyFederateName );
		CPPUNIT_TEST( testJoinFederationWithNullFederateAmbassador );
		CPPUNIT_TEST( testJoinFederationWhenAlreadyJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*JOINFEDERATIONTEST_H_*/
