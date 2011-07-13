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
#ifndef DELETEOBJECTTEST_H_
#define DELETEOBJECTTEST_H_

#include "../common/Common.h"

class DeleteObjectTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		TestNG6Federate *defaultFederate;
		TestNG6Federate *listenerFederate;
		RTI::ObjectHandle theObject;
		char *tag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		DeleteObjectTest();
		virtual ~DeleteObjectTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testRODeleteObject();
		void testRODeleteObjectWithInvalidHandle();
		void testRODeleteObjectNotOwned();
		void testRODeleteObjectWhenNotJoined();
		
		void testTSODeleteObject();
		void testTSODeleteObjectWithInvalidHandle();
		void testTSODeleteObjectWithTimeInPast();
		void testTSODeleteObjectNotOwned();
		void testTSODeleteObjectWhenNotJoined();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( DeleteObjectTest );
		CPPUNIT_TEST( testRODeleteObject );
		CPPUNIT_TEST( testRODeleteObjectWithInvalidHandle );
		CPPUNIT_TEST( testRODeleteObjectNotOwned );
		CPPUNIT_TEST( testRODeleteObjectWhenNotJoined );
		
		CPPUNIT_TEST( testTSODeleteObject );
		CPPUNIT_TEST( testTSODeleteObjectWithInvalidHandle );
		CPPUNIT_TEST( testTSODeleteObjectWithTimeInPast );
		CPPUNIT_TEST( testTSODeleteObjectNotOwned );
		CPPUNIT_TEST( testTSODeleteObjectWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*DELETEOBJECTTEST_H_*/
