#include "GetAttributeDatatype.h"


// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION(GetAttributeDatatype);
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION(GetAttributeDatatype, "testGetBasicType");

 

GetAttributeDatatype::GetAttributeDatatype()
{
	this->defaultFederate = new Test1516eFederate(L"defaultFederate"); 
	this->tag = VariableLengthData((void*)"", 1); 
}
GetAttributeDatatype::~GetAttributeDatatype()
{
	delete this->defaultFederate; 
}
 
void GetAttributeDatatype::setUp()
{ 
	this->defaultFederate->quickConnect();
	this->defaultFederate->quickCreate();	
	this->defaultFederate->quickJoin(); 

	// publish and subscribe
	this->sodeObject = this->defaultFederate->rtiamb->getObjectClassHandle(L"HLAobjectRoot.B");
	this->flavor = this->defaultFederate->rtiamb->getAttributeHandle(this->sodeObject, L"bb");

	std::vector<AttributeHandle> attributes;
	attributes.push_back(this->flavor);
	defaultFederate->quickPublish(this->sodeObject, attributes);

	// set time up
	defaultFederate->quickEnableRegulating(5.0);  

	// register and discover the object
	theObject = defaultFederate->quickRegister(this->sodeObject);
}

void GetAttributeDatatype::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy();
	this->defaultFederate->quickDisconnect();
}

void GetAttributeDatatype::testGetBasicType()
{
	try
	{
		this->defaultFederate->rtiamb->registerObjectInstance(this->sodeObject);
	}
	catch (Exception& e)
	{
		failTest("Unexpected exception publishing object class: %s", e.what());
	}

	// validate that we are publishing ObjectRoot.A, but not ObjectRoot.A.B
	//validatePublished();
}

