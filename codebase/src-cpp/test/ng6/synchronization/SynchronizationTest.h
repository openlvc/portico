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
#ifndef SYNCHRONIZATIONTEST_H_
#define SYNCHRONIZATIONTEST_H_

#include "../common/Common.h"

class SynchronizationTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        TestNG6Federate *federateOne;
        TestNG6Federate *federateTwo;
        char *pointOne;
        char *tag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		SynchronizationTest();
		virtual ~SynchronizationTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
    	void testAchievedSyncPoint();
    	void testAcheivedSyncPointFromUnjoinedFederate();
    	void testAchievedSyncPointWithNullLabel();
    	void testAchievedSyncPointWithEmptyLabel();
    	void testAchievedSyncPointWithWhitespaceLabel();
    	void testAchievedSyncPointWithNonAnnouncedLabel();
    	void testAchievedSyncPointWithPreviouslySynchronizedLabel();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( SynchronizationTest );
		CPPUNIT_TEST( testAchievedSyncPoint );
		CPPUNIT_TEST( testAcheivedSyncPointFromUnjoinedFederate );
		CPPUNIT_TEST( testAchievedSyncPointWithNullLabel );
		CPPUNIT_TEST( testAchievedSyncPointWithEmptyLabel );
		CPPUNIT_TEST( testAchievedSyncPointWithWhitespaceLabel );
		CPPUNIT_TEST( testAchievedSyncPointWithNonAnnouncedLabel );
		CPPUNIT_TEST( testAchievedSyncPointWithPreviouslySynchronizedLabel );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*SYNCHRONIZATIONTEST_H_*/
