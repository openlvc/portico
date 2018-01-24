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
	Test1516eFederate *listenerFederate;
	ObjectInstanceHandle theObject;
	VariableLengthData tag;

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
	 

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE(GetAttributeDatatype);
	CPPUNIT_TEST(testGetBasicType);
	CPPUNIT_TEST_SUITE_END();
};

