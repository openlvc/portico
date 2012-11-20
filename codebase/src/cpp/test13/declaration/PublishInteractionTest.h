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
#ifndef PUBLISHINTERACTIONTEST_H_
#define PUBLISHINTERACTIONTEST_H_

#include "../common/Common.h"

class PublishInteractionTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Test13Federate *defaultFederate;
		Test13Federate *listenerFederate;
		RTI::InteractionClassHandle xHandle;
		RTI::InteractionClassHandle yHandle;
		RTI::ParameterHandle xaHandle, xbHandle, xcHandle;
		RTI::ParameterHandle yaHandle, ybHandle, ycHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		PublishInteractionTest();
		virtual ~PublishInteractionTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
		void testICPublish();
		void testICPublishWithInvalidHandle();
		void testICPublishWhileNotJoined();
		void testICUnpublish();
		void testICUnpublishWithInvalidHandle();
		void testICUnpublishWhenNotPublished();
		void testICUnpublishWhileNotJoined();

	private:
		void validatePublished();
		void validateNotPublished();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( PublishInteractionTest );
		CPPUNIT_TEST( testICPublish );
		CPPUNIT_TEST( testICPublishWithInvalidHandle );
		CPPUNIT_TEST( testICPublishWhileNotJoined );
		CPPUNIT_TEST( testICUnpublish );
		CPPUNIT_TEST( testICUnpublishWithInvalidHandle );
		CPPUNIT_TEST( testICUnpublishWhenNotPublished );
		CPPUNIT_TEST( testICUnpublishWhileNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*PUBLISHINTERACTIONTEST_H_*/
