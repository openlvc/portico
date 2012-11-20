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
#ifndef REGISTEROBJECTDDMTEST_H_
#define REGISTEROBJECTDDMTEST_H_

#include "../common/Common.h"

class RegisterObjectDDMTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
		Test13Federate *defaultFederate; // publishes ObjectRoot.A.B
		Test13Federate *federateA;       // subscribes to ObjectRoot.A.B in regionOne
		Test13Federate *federateB;       // subscribes to ObjectRoot.A.B in regionTwo
		Test13Federate *federateC;       // subscribes to ObjectRoot.A.B in no region
		
		// handles
		RTI::ObjectClassHandle aHandle;
		RTI::AttributeHandle   aaHandle;
		RTI::AttributeHandle   abHandle;
		RTI::AttributeHandle   acHandle;
		RTI::ObjectClassHandle bHandle;
		RTI::AttributeHandle   baHandle;
		RTI::AttributeHandle   bbHandle;
		RTI::AttributeHandle   bcHandle;
		
		// regions
		RTI::Region            *regionOne;
		RTI::Region            *regionTwo;
		RTI::Region            *federateARegion;
		RTI::Region            *federateBRegion;
		
		// arrays
		RTI::AttributeHandle   allHandles[6]; // array of all the handles, sized 6
		RTI::Region*           regions[6];    // array of region* to go with allHandles

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		RegisterObjectDDMTest();
		virtual ~RegisterObjectDDMTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testRegisterObjectDDM();
		void testRegisterObjectDDMWithInvalidClassHandle();
		void testRegisterObjectDDMWithNonPublishedClassHandle();
		void testRegisterNamedObjectDDM();
		void testRegisterNamedObjectDDMWithNullName();
		void testRegisterNamedObjectDDMWithWhitespaceName();
		void testRegisterNamedObjectDDMWithInvalidClassHandle();
		void testRegisterNamedObjectDDMWithExistingName();
		void testRegisterNamedObjectDDMWithNonPublishedClassHandle();

	private:
		void validateRegistration( RTI::ObjectHandle theObject );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( RegisterObjectDDMTest );
		CPPUNIT_TEST( testRegisterObjectDDM );
		CPPUNIT_TEST( testRegisterObjectDDMWithInvalidClassHandle );
		CPPUNIT_TEST( testRegisterObjectDDMWithNonPublishedClassHandle );
		CPPUNIT_TEST( testRegisterNamedObjectDDM );
		CPPUNIT_TEST( testRegisterNamedObjectDDMWithNullName );
		CPPUNIT_TEST( testRegisterNamedObjectDDMWithWhitespaceName );
		CPPUNIT_TEST( testRegisterNamedObjectDDMWithInvalidClassHandle );
		// - not yet implemented in 0.9 CPPUNIT_TEST( testRegisterNamedObjectDDMWithExistingName );
		CPPUNIT_TEST( testRegisterNamedObjectDDMWithNonPublishedClassHandle );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*REGISTEROBJECTDDMTEST_H_*/
