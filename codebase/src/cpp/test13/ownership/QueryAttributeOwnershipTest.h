/*
 *   Copyright 2009 The Portico Project
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
#ifndef QUERYATTRIBUTEOWNERSHIPTEST_H_
#define QUERYATTRIBUTEOWNERSHIPTEST_H_

#include "../common/Common.h"

class QueryAttributeOwnershipTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Test13Federate *defaultFederate;
		Test13Federate *secondFederate;
		RTI::ObjectHandle theObject;
		RTI::AttributeHandle aa;
		RTI::AttributeHandle ab;
		RTI::AttributeHandle ac;
		RTI::AttributeHandle ba;
		RTI::AttributeHandle bb;
		RTI::AttributeHandle bc;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		QueryAttributeOwnershipTest();
		virtual ~QueryAttributeOwnershipTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testQueryAttributeOwnershipAfterObjectRegistration();
		void testQueryAttributeOwnershipAfterOwnershipTransfer();
		void testQueryAttributeOwnershipForMomAttributes();
		void testQueryAttributeOwnershipWithInvalidObject();
		void testQueryAttributeOwnershipWithUnknownObject();
		void testQueryAttributeOwnershipWithWrongAttributeForClass();
		void testQueryAttributeOwnershipWithInvalidAttribute();
		void testQueryAttributeOwnershipWhenNotJoined();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( QueryAttributeOwnershipTest );
		CPPUNIT_TEST( testQueryAttributeOwnershipAfterObjectRegistration );
		CPPUNIT_TEST( testQueryAttributeOwnershipAfterOwnershipTransfer );
		CPPUNIT_TEST( testQueryAttributeOwnershipForMomAttributes );
		CPPUNIT_TEST( testQueryAttributeOwnershipWithInvalidObject );
		CPPUNIT_TEST( testQueryAttributeOwnershipWithUnknownObject );
		CPPUNIT_TEST( testQueryAttributeOwnershipWithWrongAttributeForClass );
		CPPUNIT_TEST( testQueryAttributeOwnershipWithInvalidAttribute );
		CPPUNIT_TEST( testQueryAttributeOwnershipWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /* QUERYATTRIBUTEOWNERSHIPTEST_H_ */
