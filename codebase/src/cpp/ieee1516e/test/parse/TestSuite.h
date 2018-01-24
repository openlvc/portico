#pragma once

#include "../common/common.h"
#include "../common/Test1516eFederate.h"
class TestSuite : public CppUnit::TestFixture
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
	TestSuite();
	virtual ~TestSuite();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
public:
	void setUp();
	void tearDown();

protected:
	void testTestSuite();
	 

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE(TestSuite);
	CPPUNIT_TEST(testTestSuite);
	CPPUNIT_TEST_SUITE_END();
};

