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
#include "../common/Common.h"

class AcquireOwnershipTest : public CppUnit::TestFixture
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
		RTI::AttributeHandleSet* ahs;
		char *tag;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		AcquireOwnershipTest();
		virtual ~AcquireOwnershipTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		// if available
		void testAcquireOwnershipIfAvailable();
		void testAcquireOwnershipIfAvailableWhenUnavailable();
		void testAcquireOwnershipIfAvailableWithUnknownObject();
		void testAcquireOwnershipIfAvailableWithInvalidObject();
		void testAcquireOwnershipIfAvailableWithInvalidAttribute();
		void testAcquireOwnershipIfAvailableWithUnpublishedClass();
		void testAcquireOwnershipIfAvailableWithUnpublishedAttribute();
		void testAcquireOwnershipIfAvailableWithAlreadyOwnedAttribute();
		void testAcquireOwnershipIfAvailableWhenAlreadyAcquiringAttribute();
		void testAcquireOwnershipIfAvailableWhenNotJoined();

		// regular acquire tests
		void testAcquireOwnership();
		void testAcquireOwnershipWithUnknownObject();
		void testAcquireOwnershipWithInvalidObject();
		void testAcquireOwnershipWithInvalidAttribute();
		void testAcquireOwnershipWithUnpublishedClass();
		void testAcquireOwnershipWithUnpublishedAttribute();
		void testAcquireOwnershipWithAlreadyOwnedAttribute();
		void testAcquireOwnershipWhenNotJoined();
		
		// release response
		void testReleaseResponse();
		void testReleaseResponseWithUnknownObject();
		void testReleaseResponseWithInvalidObject();
		void testReleaseResponseWithInvalidAttribute();
		void testReleaseResponseWithUnownedAttribute();
		void testReleaseResponseWhenNotAskedToRelease();
		void testReleaseResponseWhenNotJoined();
		
		// cancel acquire
		void testCancelOwnershipAcquisition();
		void testCancelOwnershipAcquisitionWithInvalidObject();
		void testCancelOwnershipAcquisitionWithUnknownObject();
		void testCancelOwnershipAcquisitionWithInvalidAttribute();
		void testCancelOwnershipAcquisitionWithOwnershipWhereAcquiredFinished();
		void testCancelOwnershipAcquisitionWithUnrequestedAttributes();
		void testCancelOwnershipAcquisitionWhenNotJoined();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( AcquireOwnershipTest );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailable );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailableWhenUnavailable );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailableWithUnknownObject );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailableWithInvalidObject );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailableWithInvalidAttribute );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailableWithUnpublishedClass );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailableWithUnpublishedAttribute );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailableWithAlreadyOwnedAttribute );
		CPPUNIT_TEST( testAcquireOwnershipIfAvailableWhenNotJoined );
		
		CPPUNIT_TEST( testAcquireOwnership );
		CPPUNIT_TEST( testAcquireOwnershipWithUnknownObject );
		CPPUNIT_TEST( testAcquireOwnershipWithInvalidObject );
		CPPUNIT_TEST( testAcquireOwnershipWithInvalidAttribute );
		CPPUNIT_TEST( testAcquireOwnershipWithUnpublishedClass );
		CPPUNIT_TEST( testAcquireOwnershipWithUnpublishedAttribute );
		CPPUNIT_TEST( testAcquireOwnershipWithAlreadyOwnedAttribute );
		CPPUNIT_TEST( testAcquireOwnershipWhenNotJoined );
		
		CPPUNIT_TEST( testReleaseResponse );
		CPPUNIT_TEST( testReleaseResponseWithUnknownObject );
		CPPUNIT_TEST( testReleaseResponseWithInvalidObject );
		CPPUNIT_TEST( testReleaseResponseWithInvalidAttribute );
		CPPUNIT_TEST( testReleaseResponseWithUnownedAttribute );
		CPPUNIT_TEST( testReleaseResponseWhenNotAskedToRelease );
		CPPUNIT_TEST( testReleaseResponseWhenNotJoined );
		
		CPPUNIT_TEST( testCancelOwnershipAcquisition );
		CPPUNIT_TEST( testCancelOwnershipAcquisitionWithInvalidObject );
		CPPUNIT_TEST( testCancelOwnershipAcquisitionWithUnknownObject );
		CPPUNIT_TEST( testCancelOwnershipAcquisitionWithInvalidAttribute );
		CPPUNIT_TEST( testCancelOwnershipAcquisitionWithOwnershipWhereAcquiredFinished );
		CPPUNIT_TEST( testCancelOwnershipAcquisitionWithUnrequestedAttributes );
		CPPUNIT_TEST( testCancelOwnershipAcquisitionWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};
