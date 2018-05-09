#pragma once

#include "../common/common.h"
#include "../common/Test1516eFederate.h"

class AttributeDatatypeTest : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		Test1516eFederate *defaultFederate;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		AttributeDatatypeTest();
		virtual ~AttributeDatatypeTest();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();

	protected:
		void testGetSimpleType();
		void testGetEnumeratedType();
		void testGetArrayTypeDynamic();
		void testGetArrayTypeFixed();
		void testGetFixedRecordType();
		void testGetVariantRecordType();
		void testGetNAType();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE(AttributeDatatypeTest);
		CPPUNIT_TEST(testGetSimpleType);
		CPPUNIT_TEST(testGetEnumeratedType);
		CPPUNIT_TEST(testGetArrayTypeDynamic);
		CPPUNIT_TEST(testGetArrayTypeFixed);
		CPPUNIT_TEST(testGetFixedRecordType); 
		CPPUNIT_TEST(testGetVariantRecordType); 
		CPPUNIT_TEST(testGetNAType);
	CPPUNIT_TEST_SUITE_END();
};

