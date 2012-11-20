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

class FederationSaveTest : public CppUnit::TestFixture
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
		char *saveLabel;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		FederationSaveTest();
		virtual ~FederationSaveTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		// request save tests
		void testRequestFederationSave();
		void testRequestFederationSaveWhenNotJoined();
		void testRequestFederationSaveWhenSaveInProgress();
		void testRequestFederationSaveWhenRestoreInProgress();
		
		// request save with timestamp tests
		void testTimestampedRequestFederationSave();
		void testTimestampedRequestFederationSaveWithTimeInPast();
		void testTimestampedRequestFederationSaveWhenNotJoined();
		void testTimestampedRequestFederationSaveWhenSaveInProgress();
		void testTimestampedRequestFederationSaveWhenRestoreInProgress();
		
		// federate save begun tests
		void testFederateSaveBegun();
		void testFederateSaveBegunWithoutActiveSave();
		void testFederateSaveBegunWhenNotJoined();
		void testFederateSaveBegunWhenRestoreInProgress();
		
		// federate save complete tests
		void testFederateSaveComplete();
		void testFederateSaveCompleteWithoutActiveSave();
		void testFederateSaveCompleteWhenNotJoined();
		void testFederateSaveCompleteWhenRestoreInProgress();
		
		// federate save not complete tests
		void testFederateSaveNotCompleted();
		void testFederateSaveNotCompletedWithoutActiveSave();
		void testFederateSaveNotCompletedWhenNotJoined();
		void testFederateSaveNotCompletedWhenRestoreInProgress();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( FederationSaveTest );
		// request save tests
		CPPUNIT_TEST( testRequestFederationSave );
		CPPUNIT_TEST( testRequestFederationSaveWhenNotJoined );
		CPPUNIT_TEST( testRequestFederationSaveWhenSaveInProgress );
		CPPUNIT_TEST( testRequestFederationSaveWhenRestoreInProgress );
		
		// request save with timestamp tests
		CPPUNIT_TEST( testTimestampedRequestFederationSave );
		CPPUNIT_TEST( testTimestampedRequestFederationSaveWithTimeInPast );
		CPPUNIT_TEST( testTimestampedRequestFederationSaveWhenNotJoined );
		CPPUNIT_TEST( testTimestampedRequestFederationSaveWhenSaveInProgress );
		CPPUNIT_TEST( testTimestampedRequestFederationSaveWhenRestoreInProgress );
		
		// federate save begun tests
		CPPUNIT_TEST( testFederateSaveBegun );
		CPPUNIT_TEST( testFederateSaveBegunWithoutActiveSave );
		CPPUNIT_TEST( testFederateSaveBegunWhenNotJoined );
		CPPUNIT_TEST( testFederateSaveBegunWhenRestoreInProgress );
		
		// federate save complete tests
		CPPUNIT_TEST( testFederateSaveComplete );
		CPPUNIT_TEST( testFederateSaveCompleteWithoutActiveSave );
		CPPUNIT_TEST( testFederateSaveCompleteWhenNotJoined );
		CPPUNIT_TEST( testFederateSaveCompleteWhenRestoreInProgress );
		
		// federate save not complete tests
		CPPUNIT_TEST( testFederateSaveNotCompleted );
		CPPUNIT_TEST( testFederateSaveNotCompletedWithoutActiveSave );
		CPPUNIT_TEST( testFederateSaveNotCompletedWhenNotJoined );
		CPPUNIT_TEST( testFederateSaveNotCompletedWhenRestoreInProgress );
	CPPUNIT_TEST_SUITE_END();

};
