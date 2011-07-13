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
#ifndef REFLECTATTRIBUTESTEST_H_
#define REFLECTATTRIBUTESTEST_H_

#include "../common/Common.h"

class ReflectAttributesTest : public CppUnit::TestFixture
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

		RTI::ObjectHandle                theObject;
		TestNG6Object                    *ng6Object;

		RTI::ObjectClassHandle           aHandle;
		RTI::ObjectClassHandle           bHandle;
		RTI::AttributeHandle             aaHandle;
		RTI::AttributeHandle             abHandle;
		RTI::AttributeHandle             acHandle;
		RTI::AttributeHandle             baHandle;
		RTI::AttributeHandle             bbHandle;
		RTI::AttributeHandle             bcHandle;
		RTI::AttributeHandleValuePairSet *ahvps;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		ReflectAttributesTest();
		virtual ~ReflectAttributesTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testReflectRO();
		void testReflectROWithUnknownObject();
		void testReflectROWithInvalidAttribute();
		void testReflectROWithUnownedAttribute();
		void testReflectROWhenNotJoined();
		
		void testReflectTSO();
		void testReflectTSOWithUnknownObject();
		void testReflectTSOWithInvalidAttribute();
		void testReflectTSOWithUnownedAttribute();
		void testReflectTSOWithInvalidTime();
		void testReflectTSOWithTimeBeforeLookahead();
		void testReflectTSOWhenNotJoined();
	
	private:
		void validateReflectedRO();
		void validateReflectedTSO();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( ReflectAttributesTest );
		CPPUNIT_TEST( testReflectRO );
		CPPUNIT_TEST( testReflectROWithUnknownObject );
		CPPUNIT_TEST( testReflectROWithInvalidAttribute );
		CPPUNIT_TEST( testReflectROWithUnownedAttribute );
		CPPUNIT_TEST( testReflectROWhenNotJoined );
		
		CPPUNIT_TEST( testReflectTSO );
		CPPUNIT_TEST( testReflectTSOWithUnknownObject );
		CPPUNIT_TEST( testReflectTSOWithInvalidAttribute );
		CPPUNIT_TEST( testReflectTSOWithUnownedAttribute );
		CPPUNIT_TEST( testReflectTSOWithInvalidTime );
		CPPUNIT_TEST( testReflectTSOWithTimeBeforeLookahead );
		CPPUNIT_TEST( testReflectTSOWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*REFLECTATTRIBUTESTEST_H_*/
