#include "GetAttributeDatatype.h"


// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION(GetAttributeDatatype);
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION(GetAttributeDatatype, "testGetBasicType");

 

GetAttributeDatatype::GetAttributeDatatype()
{
	this->defaultFederate = new Test1516eFederate(L"defaultFederate");
	this->listenerFederate = new Test1516eFederate(L"listenerFederate");
	this->tag = VariableLengthData((void*)"", 1); 
}
GetAttributeDatatype::~GetAttributeDatatype()
{
	delete this->defaultFederate;
	delete this->listenerFederate;
}
 
void GetAttributeDatatype::setUp()
{
	this->defaultFederate->quickCreate();
	this->defaultFederate->quickJoin();
	this->listenerFederate->quickJoin();

	// publish and subscribe
	std::vector<wstring> attributes;
	attributes.push_back(L"aa");
	attributes.push_back(L"aa");
	defaultFederate->quickPublish(L"ObjectRoot.A.B", attributes);
	listenerFederate->quickSubscribe(L"ObjectRoot.A", attributes);

	// set time up
	defaultFederate->quickEnableRegulating(5.0);
	listenerFederate->quickEnableAsync();
	listenerFederate->quickEnableConstrained();

	// register and discover the object
	theObject = defaultFederate->quickRegister(L"ObjectRoot.A.B");
	//listenerFederate->fedamb->waitForDiscovery(theObject);


}

void GetAttributeDatatype::tearDown()
{
	this->listenerFederate->quickResign();
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
}

void GetAttributeDatatype::testGetBasicType()
{
	try
	{
		defaultFederate->rtiamb->deleteObjectInstance(theObject, tag);
	}
	catch (Exception& e)
	{
		failTest("Unexpected exception while deleting object: %s", e.what());
	}

	// make sure the listener gets the remove message
	//listenerFederate->fedamb->waitForRORemoval(theObject);
	//defaultFederate->fedamb->waitForRORemovalTimeout(theObject); // shouldn't get its own delete
}

