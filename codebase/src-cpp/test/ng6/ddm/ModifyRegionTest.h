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
#ifndef MODIFYREGIONTEST_H_
#define MODIFYREGIONTEST_H_

#include "../common/Common.h"

class ModifyRegionTest : public CppUnit::TestFixture
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
		
		RTI::SpaceHandle testSpace;
		RTI::DimensionHandle testDimension;
		RTI::ObjectHandle testObject;

		RTI::Region *listenerRegion;
		RTI::Region *testRegion;
		RTI::RegionToken testToken;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		ModifyRegionTest();
		virtual ~ModifyRegionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testModifyRegion();
		void testModifyRegionWithUnknownRegion();
		void testModifyRegionWithInvalidExtents();
		void testModifyRegionWhenNotJoined();
	
	private:
		void validateRegionOverlap();
		void validateRegionNoOverlap();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( ModifyRegionTest );
		CPPUNIT_TEST( testModifyRegion );
		CPPUNIT_TEST( testModifyRegionWithUnknownRegion );
		CPPUNIT_TEST( testModifyRegionWithInvalidExtents );
		CPPUNIT_TEST( testModifyRegionWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*MODIFYREGIONTEST_H_*/
