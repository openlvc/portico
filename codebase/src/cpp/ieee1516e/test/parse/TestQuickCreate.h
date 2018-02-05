#pragma once

#include "../common/common.h"
#include "../common/Test1516eFederate.h"
class TestQuickCreate : public CppUnit::TestFixture
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
	TestQuickCreate();
	virtual ~TestQuickCreate();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
public:
	void setUp();
	void tearDown();

protected:
	void testTestQuickCreate();
	 

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	CPPUNIT_TEST_SUITE(TestQuickCreate);
	CPPUNIT_TEST(testTestQuickCreate);
	CPPUNIT_TEST_SUITE_END();
};

