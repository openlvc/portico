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
#include "../common/Common.h"

class TemplateTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
    private:
        TestNG6Federate *defaultFederate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		TemplateTest();
		virtual ~TemplateTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		//void testCreateFederation();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( TemplateTest );
		CPPUNIT_TEST( testCreateFederation );
	CPPUNIT_TEST_SUITE_END();

};

////////////////////////////////////////////////////////////////////////////////////////////////////

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
#include "TemplateTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION( TemplateTest );

/////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Constructors/Destructors /////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////
CreateDestroyTest::CreateDestoryTest()
{
	this->defaultFederate = new TestNG6Federate( "defaultFederate" );
}

CreateDestroyTest::~CreateDestroyTest()
{
	delete this->defaultFederate;
}

/////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Test Setup and Helper Methods ///////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////

void CreateDestroyTest::setUp()
{
    this->defaultFederate->quickCreate();
    this->defaultFederate->quickJoin();
}

void CreateDestroyTest::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Test Methods ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////

//////////////////////////////////////////
// TEST: (valid) testCreateFederation() //
//////////////////////////////////////////

