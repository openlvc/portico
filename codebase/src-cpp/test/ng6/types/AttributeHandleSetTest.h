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
#ifndef ATTRIBUTEHANDLESETTEST_H_
#define ATTRIBUTEHANDLESETTEST_H_

#include "../common/Common.h"

class AttributeHandleSetTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        TestNG6Federate *defaultFederate;
        RTI::AttributeHandleSet *theSet;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		AttributeHandleSetTest();
		virtual ~AttributeHandleSetTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testSize();
		void testGetHandle();
		void testGetHandleWithInvalidIndex();
		void testAdd();
		void testRemove();
		void testEmpty();
		void testIsEmpty();
		void testIsMember();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( AttributeHandleSetTest );
		CPPUNIT_TEST( testSize );
		CPPUNIT_TEST( testGetHandle );
		CPPUNIT_TEST( testGetHandleWithInvalidIndex );
		CPPUNIT_TEST( testAdd );
		CPPUNIT_TEST( testRemove );
		CPPUNIT_TEST( testEmpty );
		CPPUNIT_TEST( testIsEmpty );
		CPPUNIT_TEST( testIsMember );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*ATTRIBUTEHANDLESETTEST_H_*/
