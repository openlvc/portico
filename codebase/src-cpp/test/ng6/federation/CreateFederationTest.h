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
#ifndef CREATEFEDERATIONTEST_H_
#define CREATEFEDERATIONTEST_H_

#include "../common/Common.h"

class CreateFederationTest : public CppUnit::TestFixture
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
		CreateFederationTest();
		virtual ~CreateFederationTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testCreateFederation();
		void testCreateFederationThatAlreadyExists();
		void testCreateFederationWithInvalidFom();
		void testCreateFederationWithInvalidFomLocation();
		void testCreateFederationWithNullFom();
		void testCreateFederationWithNullName();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( CreateFederationTest );
		CPPUNIT_TEST( testCreateFederation );
		CPPUNIT_TEST( testCreateFederationThatAlreadyExists );
		CPPUNIT_TEST( testCreateFederationWithInvalidFom );
		CPPUNIT_TEST( testCreateFederationWithInvalidFomLocation );
		CPPUNIT_TEST( testCreateFederationWithNullFom );
		CPPUNIT_TEST( testCreateFederationWithNullName );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*CREATEFEDERATIONTEST_H_*/
