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
#ifndef REQUESTUPDATEWITHREGIONTEST_H_
#define REQUESTUPDATEWITHREGIONTEST_H_


#include "../common/Common.h"

class RequestUpdateWithRegionTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Test13Federate *defaultFederate;
		Test13Federate *provokerFederate;
		RTI::ObjectClassHandle aHandle;
		RTI::AttributeHandle   aaHandle;
		RTI::AttributeHandle   abHandle;
		RTI::AttributeHandle   acHandle;
		RTI::ObjectClassHandle bHandle;
		
		RTI::ObjectHandle firstObject;
		RTI::ObjectHandle secondObject;
		
		RTI::Region *senderRegion;
		RTI::Region *updateRegion;
		RTI::Region *updateRegionOOB;
		
		RTI::AttributeHandleSet *ahs;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		RequestUpdateWithRegionTest();
		virtual ~RequestUpdateWithRegionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testRequestUpdateWithRegion();
		void testRequestUpdateWithRegionUsingNonOverlappingRegion();
		void testRequestUpdateWithRegionUsingInvalidObjectClass();
		void testRequestUpdateWithRegionUsingInvalidAttributeClass();
		void testRequestUpdateWithRegionUsingUnknownRegion();
		void testRequestUpdateWithRegionWhenNotJoined();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( RequestUpdateWithRegionTest );
		CPPUNIT_TEST( testRequestUpdateWithRegion );
		CPPUNIT_TEST( testRequestUpdateWithRegionUsingNonOverlappingRegion );
		CPPUNIT_TEST( testRequestUpdateWithRegionUsingInvalidObjectClass );
		CPPUNIT_TEST( testRequestUpdateWithRegionUsingInvalidAttributeClass );
		CPPUNIT_TEST( testRequestUpdateWithRegionUsingUnknownRegion );
		CPPUNIT_TEST( testRequestUpdateWithRegionWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*REQUESTUPDATEWITHREGIONTEST_H_*/
