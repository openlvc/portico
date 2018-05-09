#include "QuickCreateTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION(QuickCreateTest);

QuickCreateTest::QuickCreateTest()
{
	this->defaultFederate = new Test1516eFederate(L"defaultFederate"); 
	this->listenerFederate = 0;
	this->tag = VariableLengthData((void*)"", 1); 
}

QuickCreateTest::~QuickCreateTest()
{
	if( this->defaultFederate )
		delete this->defaultFederate;
	if( this->listenerFederate )
		delete this->listenerFederate;
}
 
void QuickCreateTest::setUp()
{
	this->defaultFederate->quickConnect();
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin(); 
}

void QuickCreateTest::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
	this->defaultFederate->quickDisconnect();
}

void QuickCreateTest::testQuickCreate()
{
	CPPUNIT_ASSERT_EQUAL(1, 1); 
}

 

