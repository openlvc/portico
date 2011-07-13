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
#ifndef REGISTEROBJECTTEST_H_
#define REGISTEROBJECTTEST_H_

#include "../common/Common.h"

class RegisterObjectTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        TestNG6Federate *defaultFederate; // publishes ObjectRoot.A.B
        TestNG6Federate *federateA;       // subscribes to ObjectRoot.A
        TestNG6Federate *federateB;       // subscribes to ObjectRoot.A.B
        TestNG6Federate *federateNone;    // subscribes to nothing
        
        // handles
        RTI::ObjectClassHandle aHandle;
        RTI::ObjectClassHandle bHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		RegisterObjectTest();
		virtual ~RegisterObjectTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
    	void testRegisterObject();
    	void testRegisterObjectWithInvalidClassHandle();
    	void testRegisterObjectWithNonPublishedClassHandle();
    	void testRegisterNamedObject();
    	void testRegisterNamedObjectWithNullName();
		void testRegisterNamedObjectWithWhitespaceName();
		void testRegisterNamedObjectWithInvalidClassHandle();
		void testRegisterNamedObjectWithExistingName();
		void testRegisterNamedObjectWithNonPublishedClassHandle();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( RegisterObjectTest );
		CPPUNIT_TEST( testRegisterObject );
		CPPUNIT_TEST( testRegisterObjectWithInvalidClassHandle );
		CPPUNIT_TEST( testRegisterObjectWithNonPublishedClassHandle );
		CPPUNIT_TEST( testRegisterNamedObject );
		CPPUNIT_TEST( testRegisterNamedObjectWithNullName );
		CPPUNIT_TEST( testRegisterNamedObjectWithWhitespaceName );
		CPPUNIT_TEST( testRegisterNamedObjectWithInvalidClassHandle );
		// - not yet implemented in 0.9 CPPUNIT_TEST( testRegisterNamedObjectWithExistingName );
		CPPUNIT_TEST( testRegisterNamedObjectWithNonPublishedClassHandle );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*REGISTEROBJECTTEST_H_*/
