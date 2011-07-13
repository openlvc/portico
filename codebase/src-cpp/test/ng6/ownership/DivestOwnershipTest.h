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
#ifndef DIVESTOWNERSHIPTEST_H_
#define DIVESTOWNERSHIPTEST_H_

#include "../common/Common.h"

class DivestOwnershipTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		TestNG6Federate *defaultFederate;
		TestNG6Federate *secondFederate;
		RTI::ObjectHandle theObject;
		RTI::AttributeHandle aa;
		RTI::AttributeHandle ab;
		RTI::AttributeHandle ac;
		RTI::AttributeHandle ba;
		RTI::AttributeHandle bb;
		RTI::AttributeHandle bc;
		RTI::AttributeHandleSet* ahs;
		char *tag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		DivestOwnershipTest();
		virtual ~DivestOwnershipTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testUnconditionalDivest();
		void testUnconditionalDivestWithInvalidObject();
		void testUnconditionalDivestWithUnknownObject();
		void testUnconditionalDivestWithInvalidAttribute();
		void testUnconditionalDivestWithUnownedAttribute();
		void testUnconditionalDivestWhenNotJoined();

		void testNegotiatedDivest();
		void testNegotiatedDivestWithInvalidObject();
		void testNegotiatedDivestWithUnknownObject();
		void testNegotiatedDivestWithInvalidAttribute();
		void testNegotiatedDivestWithUnownedAttribute();
		void testNegotiatedDivestWithAttributeAlreadyBeingDivested();
		void testNegotiatedDivestWhenNotJoined();

		void testCancelNegotiatedDivest();
		void testCancelNegotiatedDivestWithInvalidObject();
		void testCancelNegotiatedDivestWithUnknownObject();
		void testCancelNegotiatedDivestWithInvalidAttribute();
		void testCancelNegotiatedDivestWithUnownedAttribute();
		void testCancelNegotiatedDivestWithoutOutstandingRequest();
		void testCancelNegotiatedDivestWhenNotJoined();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( DivestOwnershipTest );
		CPPUNIT_TEST( testUnconditionalDivest );
		CPPUNIT_TEST( testUnconditionalDivestWithInvalidObject );
		CPPUNIT_TEST( testUnconditionalDivestWithUnknownObject );
		CPPUNIT_TEST( testUnconditionalDivestWithInvalidAttribute );
		CPPUNIT_TEST( testUnconditionalDivestWithUnownedAttribute );
		CPPUNIT_TEST( testUnconditionalDivestWhenNotJoined );

		CPPUNIT_TEST( testNegotiatedDivest );
		CPPUNIT_TEST( testNegotiatedDivestWithInvalidObject );
		CPPUNIT_TEST( testNegotiatedDivestWithUnknownObject );
		CPPUNIT_TEST( testNegotiatedDivestWithInvalidAttribute );
		CPPUNIT_TEST( testNegotiatedDivestWithUnownedAttribute );
		CPPUNIT_TEST( testNegotiatedDivestWithAttributeAlreadyBeingDivested );
		CPPUNIT_TEST( testNegotiatedDivestWhenNotJoined );
		
		CPPUNIT_TEST( testCancelNegotiatedDivest );
		CPPUNIT_TEST( testCancelNegotiatedDivestWithInvalidObject );
		CPPUNIT_TEST( testCancelNegotiatedDivestWithUnknownObject );
		CPPUNIT_TEST( testCancelNegotiatedDivestWithInvalidAttribute );
		CPPUNIT_TEST( testCancelNegotiatedDivestWithUnownedAttribute );
		CPPUNIT_TEST( testCancelNegotiatedDivestWithoutOutstandingRequest );
		CPPUNIT_TEST( testCancelNegotiatedDivestWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();


};

#endif /* DIVESTOWNERSHIPTEST_H_ */
