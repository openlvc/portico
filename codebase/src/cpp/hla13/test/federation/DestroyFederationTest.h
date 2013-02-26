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
#ifndef DESTROYFEDERATIONTEST_H_
#define DESTROYFEDERATIONTEST_H_

#include "../common/Common.h"

// RTI Call:
// void RTI::RTIambassador::destroyFederationExecution( const char *executionName )
//	 throw( RTI::FederatesCurrentlyJoined,
//	        RTI::FederationExecutionDoesNotExist,
//	        RTI::ConcurrentAccessAttempted,
//	        RTI::RTIinternalError )

class DestroyFederationTest : public CppUnit::TestFixture
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
		DestroyFederationTest();
		virtual ~DestroyFederationTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testDestroyFederation();
		void testDestroyFederationWithJoinedMembers();
		void testDestroyFederationThatDoesNotExist();
		void testDestroyFederationWithNullName();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( DestroyFederationTest );
		CPPUNIT_TEST( testDestroyFederation );
		CPPUNIT_TEST( testDestroyFederationWithJoinedMembers );
		CPPUNIT_TEST( testDestroyFederationThatDoesNotExist );
		CPPUNIT_TEST( testDestroyFederationWithNullName );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*DESTROYFEDERATIONTEST_H_*/
