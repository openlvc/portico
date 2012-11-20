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
#ifndef REGISTERSYNCPOINTTEST_H_
#define REGISTERSYNCPOINTTEST_H_

#include "../common/Common.h"

class RegisterSyncPointTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Test13Federate *federateOne;
		Test13Federate *federateTwo;
		char *label, *tag;
        

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		RegisterSyncPointTest();
		virtual ~RegisterSyncPointTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testRegisterSyncPoint();
		void testRegisterSyncPointWithFHS();
		void testRegisterSyncPointWithNullTag();
		void testRegisterSyncPointWithEmptyTag();
		void testRegisterSyncPointWithWhitespaceTag();
		void testRegisterSyncPointFromUnjoinedFederate();
		void testRegisterSyncPointWithNullLabel();
		void testRegisterSyncPointWithEmptyLabel();
		void testRegisterSyncPointWithWhitespaceLabel();
		void testRegisterSyncPointWithExistingLabel();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( RegisterSyncPointTest );
		CPPUNIT_TEST( testRegisterSyncPoint );
		CPPUNIT_TEST( testRegisterSyncPointWithFHS );
		CPPUNIT_TEST( testRegisterSyncPointWithNullTag );
		CPPUNIT_TEST( testRegisterSyncPointWithEmptyTag );
		CPPUNIT_TEST( testRegisterSyncPointWithWhitespaceTag );
		CPPUNIT_TEST( testRegisterSyncPointFromUnjoinedFederate );
		CPPUNIT_TEST( testRegisterSyncPointWithNullLabel );
		CPPUNIT_TEST( testRegisterSyncPointWithEmptyLabel );
		CPPUNIT_TEST( testRegisterSyncPointWithWhitespaceLabel );
		CPPUNIT_TEST( testRegisterSyncPointWithExistingLabel );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*REGISTERSYNCPOINTTEST_H_*/
