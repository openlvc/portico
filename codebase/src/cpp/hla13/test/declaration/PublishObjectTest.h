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
#ifndef PUBLISHOBJECTTEST_H_
#define PUBLISHOBJECTTEST_H_

#include "../common/Common.h"

class PublishObjectTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Test13Federate *defaultFederate;
		Test13Federate *listener;
		
		RTI::ObjectClassHandle bHandle;
		RTI::AttributeHandle   aaHandle;
		RTI::AttributeHandle   baHandle;
		RTI::AttributeHandleSet *theSet;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		PublishObjectTest();
		virtual ~PublishObjectTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();
	
	protected:
    	void testOCPublish();
    	void testOCPublishWhenAlreadyPublished();
    	void testOCPublishWithEmptyHandleSet();
    	void testOCPublishWithInvalidClassHandle();
    	void testOCPublishWithInvalidAttributeHandle();
    	void testOCPublishWhenNotJoined();
    	
    	void testOCUnpublish();
    	void testOCUnpublishWithInvalidClassHandle();
    	void testOCUnpublishWhenNotPublished();
    	void testOCUnpublishWhenNotJoined();
    
	private:
		void validatePublished();
		void validateNotPublished();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE( PublishObjectTest );
		CPPUNIT_TEST( testOCPublish );
		CPPUNIT_TEST( testOCPublishWhenAlreadyPublished );
		CPPUNIT_TEST( testOCPublishWithEmptyHandleSet );
		CPPUNIT_TEST( testOCPublishWithInvalidClassHandle );
		CPPUNIT_TEST( testOCPublishWithInvalidAttributeHandle );
		CPPUNIT_TEST( testOCPublishWhenNotJoined );

		CPPUNIT_TEST( testOCUnpublish );
		CPPUNIT_TEST( testOCUnpublishWithInvalidClassHandle );
		CPPUNIT_TEST( testOCUnpublishWhenNotPublished );
		CPPUNIT_TEST( testOCUnpublishWhenNotJoined );
	CPPUNIT_TEST_SUITE_END();

};

#endif /*PUBLISHOBJECTTEST_H_*/
