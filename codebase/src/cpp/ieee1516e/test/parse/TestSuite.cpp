#include "TestSuite.h"


// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION(TestSuite);
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION(TestSuite, "testSuite");

 

TestSuite::TestSuite()
{
	this->defaultFederate = new Test1516eFederate(L"defaultFederate");
	this->listenerFederate = new Test1516eFederate(L"listenerFederate");
	this->tag = VariableLengthData((void*)"", 1); 
}

TestSuite::~TestSuite()
{
	delete this->defaultFederate;
	delete this->listenerFederate;
}
 
void TestSuite::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();
}

void TestSuite::tearDown()
{
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

void TestSuite::testTestSuite()
{

}

 

