#pragma once

#include "../common/common.h"
#include "../common/Test1516eFederate.h"
class QuickCreateTest : public CppUnit::TestFixture
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
		QuickCreateTest();
		virtual ~QuickCreateTest();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
	public:
		void setUp();
		void tearDown();

	protected:
		void testQuickCreate();
	 

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
		CPPUNIT_TEST_SUITE(QuickCreateTest);
		CPPUNIT_TEST(testQuickCreate);
		CPPUNIT_TEST_SUITE_END();
};

