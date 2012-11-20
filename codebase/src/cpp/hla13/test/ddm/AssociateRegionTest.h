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
#ifndef ASSOCIATEREGIONTEST_H_
#define ASSOCIATEREGIONTEST_H_

#include "../common/Common.h"

class AssociateRegionTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Test13Federate *defaultFederate; // associate with region
		Test13Federate *federateA;       // subscription region overlaps
		Test13Federate *federateB;       // subscription region doesn't overlap
		Test13Federate *federateC;       // not using ddm
		
		RTI::Region     *senderRegion;
		RTI::Region     *federateARegion;
		RTI::Region     *federateBRegion;

		// handles
		RTI::ObjectClassHandle aHandle;
		RTI::AttributeHandle   aaHandle;
		RTI::AttributeHandle   abHandle;
		RTI::AttributeHandle   acHandle;
		RTI::ObjectClassHandle bHandle;
		RTI::AttributeHandle   baHandle;
		RTI::AttributeHandle   bbHandle;
		RTI::AttributeHandle   bcHandle;
		
		RTI::AttributeHandleSet *allHandles;
		RTI::ObjectHandle theObject;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		AssociateRegionTest();
		virtual ~AssociateRegionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		///////////////////////
		// association tests //
		///////////////////////
		void testAssociateForUpdates();
		//void testAssociateForUpdatesTwice();
		void testAssociateForUpdatesWithEmptySet();

		void testAssociateForUpdatesWithUnknownObject();
		void testAssociateForUpdatedWithInvalidAttribute();
		void testAssociateForUpdatesWithUnknownRegion();
		void testAssociateForUpdatesWithInvalidRegionForAttribute();
		void testAssociateForUpdatesWithNullRegion();
		void testAssociateForUpdatesWhenNotJoined();
		
		/////////////////////////
		// unassociation tests //
		/////////////////////////
		void testUnassociateForUpdates();
		void testUnassociateWhenNotAssociated();

		void testUnassociateForUpdatesWithUnknownObject();
		void testUnassociateForUpdatesWithInvalidRegionForAnyAttributes();
		void testUnassociateForUpdatesWithUnknownRegion();
		void testUnassociateForUpdatesWithNullRegion();
		void testUnassociateForUpdatesWhenNotJoined();

	private:
		void validateAssociated( RTI::ObjectHandle theObject );
		void validateNotAssociated( RTI::ObjectHandle theObject );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( AssociateRegionTest );
		// associate
		CPPUNIT_TEST( testAssociateForUpdates );
		CPPUNIT_TEST( testAssociateForUpdatesWithEmptySet );	
		CPPUNIT_TEST( testAssociateForUpdatesWithUnknownObject );
		CPPUNIT_TEST( testAssociateForUpdatedWithInvalidAttribute );
		CPPUNIT_TEST( testAssociateForUpdatesWithUnknownRegion );
		CPPUNIT_TEST( testAssociateForUpdatesWithInvalidRegionForAttribute );
		CPPUNIT_TEST( testAssociateForUpdatesWhenNotJoined );
		// unassociate
		CPPUNIT_TEST( testUnassociateForUpdates );
		CPPUNIT_TEST( testUnassociateWhenNotAssociated );
		CPPUNIT_TEST( testUnassociateForUpdatesWithInvalidRegionForAnyAttributes );
		CPPUNIT_TEST( testUnassociateForUpdatesWithUnknownObject );
		CPPUNIT_TEST( testUnassociateForUpdatesWithUnknownRegion );
		CPPUNIT_TEST( testUnassociateForUpdatesWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*ASSOCIATEREGIONTEST_H_*/
