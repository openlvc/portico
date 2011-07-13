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
#ifndef ATTRIBUTEHANDLEVALUEPAIRSETTEST_H_
#define ATTRIBUTEHANDLEVALUEPAIRSETTEST_H_

#include "../common/Common.h"

class AttributeHandleValuePairSetTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        TestNG6Federate *defaultFederate;
        RTI::AttributeHandleValuePairSet *theSet;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		AttributeHandleValuePairSetTest();
		virtual ~AttributeHandleValuePairSetTest();

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
		void testGetValueLength();
		void testGetValueLengthWithInvalidIndex();
		void testGetValue();
		void testGetValueWithInvalidIndex();
		void testGetValuePointer();
		void testGetValuePointerWithInvalidIndex();
		void testRemove();
		void testEmpty();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( AttributeHandleValuePairSetTest );
		CPPUNIT_TEST( testSize );
		CPPUNIT_TEST( testGetHandle );
		CPPUNIT_TEST( testGetHandleWithInvalidIndex );
		CPPUNIT_TEST( testAdd );
		CPPUNIT_TEST( testGetValueLength );
		CPPUNIT_TEST( testGetValueLengthWithInvalidIndex );
		CPPUNIT_TEST( testGetValue );
		CPPUNIT_TEST( testGetValueWithInvalidIndex );
		CPPUNIT_TEST( testGetValuePointer );
		CPPUNIT_TEST( testGetValuePointerWithInvalidIndex );
		CPPUNIT_TEST( testRemove );
		CPPUNIT_TEST( testEmpty );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*ATTRIBUTEHANDLEVALUEPAIRSETTEST_H_*/
