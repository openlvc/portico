#pragma once

#include "../common/common.h"
#include "../common/Test1516eFederate.h"
class GetAttributeDatatype : public CppUnit::TestFixture
{
public:

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
private:
	Test1516eFederate *defaultFederate; 
	ObjectInstanceHandle AObject;
	ObjectInstanceHandle BObject;
	VariableLengthData tag;

	ObjectClassHandle testAClassHandle;
	ObjectClassHandle testCClassHandle;

	AttributeHandle   testBasicAttributeHandle; 	
	AttributeHandle   testSimpleAttributeHandle;
	AttributeHandle   testEnumAttributeHandle;
	AttributeHandle   testFixedAttributeHandle;
	AttributeHandle   testNAAttributeHandle;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
public:
	GetAttributeDatatype();
	virtual ~GetAttributeDatatype();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
public:
	void setUp();
	void tearDown();

protected:
	void testGetBasicType();
	void testGetSimpleType();
	void testGetEnumeratedType();
	void testGetFixedRecordType();
	void testGetNAType();

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE(GetAttributeDatatype);
	CPPUNIT_TEST(testGetBasicType);
	CPPUNIT_TEST(testGetSimpleType);
	CPPUNIT_TEST(testGetEnumeratedType);
	CPPUNIT_TEST(testGetFixedRecordType); 
	CPPUNIT_TEST(testGetNAType);
	CPPUNIT_TEST_SUITE_END();
};

