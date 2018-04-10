#include "TestQuickCreate.h"


// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION(TestQuickCreate);
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION(TestQuickCreate, "testQuickCreateCHECK");

 

TestQuickCreate::TestQuickCreate()
{
	this->defaultFederate = new Test1516eFederate(L"defaultFederate"); 
	this->tag = VariableLengthData((void*)"", 1); 
}

TestQuickCreate::~TestQuickCreate()
{
	delete this->defaultFederate;
	delete this->listenerFederate;
}
 
void TestQuickCreate::setUp()
{
	this->defaultFederate->quickConnect();
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin(); 
}

void TestQuickCreate::tearDown()
{
	 
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
	this->defaultFederate->quickDisconnect();
}

void TestQuickCreate::testTestQuickCreate()
{
	CPPUNIT_ASSERT_EQUAL(1, 1); 
}

 

