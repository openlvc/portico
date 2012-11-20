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
#ifndef DDMSUPPORTSERVICESTEST_H_
#define DDMSUPPORTSERVICESTEST_H_

#include "../common/Common.h"

class DDMSupportServicesTest : public CppUnit::TestFixture
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
		DDMSupportServicesTest();
		virtual ~DDMSupportServicesTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testGetRegion();
		void testGetRegionToken();
		void testGetAttributeRoutingSpaceHandle();
		void testGetDimensionHandle();
		void testGetDimensionName();
		void testGetInteractionRoutingSpaceHandle();
		void testGetRoutingSpaceHandle();
		void testGetRoutingSpaceName();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( DDMSupportServicesTest );
		CPPUNIT_TEST( testGetRegion );
		CPPUNIT_TEST( testGetRegionToken );
		CPPUNIT_TEST( testGetAttributeRoutingSpaceHandle );
		CPPUNIT_TEST( testGetDimensionHandle );
		CPPUNIT_TEST( testGetDimensionName );
		CPPUNIT_TEST( testGetInteractionRoutingSpaceHandle );
		CPPUNIT_TEST( testGetRoutingSpaceHandle );
		CPPUNIT_TEST( testGetRoutingSpaceName );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*DDMSUPPORTSERVICESTEST_H_*/
