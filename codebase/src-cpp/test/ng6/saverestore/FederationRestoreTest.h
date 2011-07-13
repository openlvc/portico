/*
 *   Copyright 2009 The Portico Project
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
#include "../common/Common.h"

class FederationRestoreTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		TestNG6Federate *defaultFederate;
		TestNG6Federate *secondFederate;
		char *saveLabel;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		FederationRestoreTest();
		virtual ~FederationRestoreTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		// request restore tests
		void testRequestFederationRestore();
		void testRequestFederationRestoreWhenNotJoined();
		void testRequestFederationRestoreWhenSaveInProgress();
		void testRequestFederationRestoreWhenRestoreInProgress();
		
		// federation restore complete
		void testFederationRestoreComplete();
		void testFederationRestoreCompleteWithoutActiveRestore();
		void testFederationRestoreCompleteWhenNotJoined();
		void testFederationRestoreCompleteWhenSaveInProgress();
		
		// federation restore not complete
		void testFederationRestoreNotComplete();
		void testFederationRestoreNotCompleteWithoutActiveRestore();
		void testFederationRestoreNotCompleteWhenNotJoined();
		void testFederationRestoreNotCompleteWhenSaveInProgress();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( FederationRestoreTest );
		// request restore tests
		CPPUNIT_TEST( testRequestFederationRestore );
		CPPUNIT_TEST( testRequestFederationRestoreWhenNotJoined );
		CPPUNIT_TEST( testRequestFederationRestoreWhenSaveInProgress );
		CPPUNIT_TEST( testRequestFederationRestoreWhenRestoreInProgress );
		
		// federation restore complete
		CPPUNIT_TEST( testFederationRestoreComplete );
		CPPUNIT_TEST( testFederationRestoreCompleteWithoutActiveRestore );
		CPPUNIT_TEST( testFederationRestoreCompleteWhenNotJoined );
		CPPUNIT_TEST( testFederationRestoreCompleteWhenSaveInProgress );
		
		// federation restore not complete
		CPPUNIT_TEST( testFederationRestoreNotComplete );
		CPPUNIT_TEST( testFederationRestoreNotCompleteWithoutActiveRestore );
		CPPUNIT_TEST( testFederationRestoreNotCompleteWhenNotJoined );
		CPPUNIT_TEST( testFederationRestoreNotCompleteWhenSaveInProgress );
	CPPUNIT_TEST_SUITE_END();

};
