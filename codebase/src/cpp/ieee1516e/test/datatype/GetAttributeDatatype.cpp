#include "GetAttributeDatatype.h"
#include <portico/types/BasicType.h>
#include <portico/types/SimpleType.h>
#include <portico/types/EnumeratedType.h>
#include <portico/types/FixedRecordType.h>
#include <portico/types/NaType.h>


// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION(GetAttributeDatatype);
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION(GetAttributeDatatype, "testGetDatatypes"); 

 

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
	this->defaultFederate->quickCreate(L"HLA_datatype_tests", L"complete/etc/attributeDatatypeTestFom.xml");
	this->defaultFederate->quickJoin(L"HLA_datatype_tests");

	// Get class handles
	this->testAClassHandle = this->defaultFederate->rtiamb->getObjectClassHandle(L"HLAobjectRoot.A");
	this->testCClassHandle = this->defaultFederate->rtiamb->getObjectClassHandle(L"HLAobjectRoot.C");

	// publish and subscribe
	//Basic 
	this->testBasicAttributeHandle = this->defaultFederate->rtiamb->getAttributeHandle(this->testAClassHandle, L"ab");	
	// Simple 
	this->testSimpleAttributeHandle = this->defaultFederate->rtiamb->getAttributeHandle(this->testCClassHandle, L"cb");
	// Enumerator 
	this->testEnumAttributeHandle = this->defaultFederate->rtiamb->getAttributeHandle(this->testCClassHandle, L"ca");
	// Fixed 
	this->testFixedAttributeHandle = this->defaultFederate->rtiamb->getAttributeHandle(this->testCClassHandle, L"cc");	
	// NA 
	this->testNAAttributeHandle = this->defaultFederate->rtiamb->getAttributeHandle(this->testCClassHandle, L"cd");



	//publish all attributes
	std::vector<AttributeHandle> attributes;
	attributes.push_back(this->testBasicAttributeHandle);

	std::vector<AttributeHandle> bAttributes;
	bAttributes.push_back(this->testSimpleAttributeHandle);
	bAttributes.push_back(this->testEnumAttributeHandle);
	bAttributes.push_back(this->testFixedAttributeHandle);
	bAttributes.push_back(this->testNAAttributeHandle);


	defaultFederate->quickPublish(this->testAClassHandle, attributes);
	defaultFederate->quickPublish(this->testCClassHandle, bAttributes);

	// set time up
	defaultFederate->quickEnableRegulating(5.0);  

	// register and discover the object
	AObject = defaultFederate->quickRegister(this->testAClassHandle);
	BObject = defaultFederate->quickRegister(this->testCClassHandle);
}

void GetAttributeDatatype::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy(L"HLA_datatype_tests");
	this->defaultFederate->quickDisconnect();
}

void GetAttributeDatatype::testGetBasicType()
{
	try
	{
		IDatatype* datatype =  this->defaultFederate->rtiamb->getAttributeDatatype(this->testAClassHandle, testBasicAttributeHandle);
		BasicType* basicType = dynamic_cast<BasicType*>(datatype);

		if (basicType == nullptr)
		{
			failTest("Datatype returned null" );
			CPPUNIT_ASSERT_EQUAL(1, 0);
		}
		CPPUNIT_ASSERT_EQUAL(1, 1);
	}
	catch (Exception& e)
	{
		failTest("Unexpected exception publishing object class: %s", e.what());
		CPPUNIT_ASSERT_EQUAL(1, 0);
	}
}

void GetAttributeDatatype::testGetSimpleType()
{
	try
	{
		IDatatype* datatype = this->defaultFederate->rtiamb->getAttributeDatatype(this->testCClassHandle, testSimpleAttributeHandle);
		SimpleType* basicType = dynamic_cast<SimpleType*>(datatype);

		if (basicType == nullptr)
		{
			failTest("Datatype returned null");
			CPPUNIT_ASSERT_EQUAL(1, 0);
		}
		CPPUNIT_ASSERT_EQUAL(1, 1);
	}
	catch (Exception& e)
	{
		failTest("Unexpected exception publishing object class: %s", e.what());
		CPPUNIT_ASSERT_EQUAL(1, 0);
	}
}

void GetAttributeDatatype::testGetEnumeratedType()
{
	try
	{
		IDatatype* datatype = this->defaultFederate->rtiamb->getAttributeDatatype(this->testCClassHandle, testEnumAttributeHandle);
		EnumeratedType* basicType = dynamic_cast<EnumeratedType*>(datatype);

		if (basicType == nullptr)
		{
			failTest("Datatype returned null");
			CPPUNIT_ASSERT_EQUAL(1, 0);
		}
		CPPUNIT_ASSERT_EQUAL(1, 1);
	}
	catch (Exception& e)
	{
		failTest("Unexpected exception publishing object class: %s", e.what());
		CPPUNIT_ASSERT_EQUAL(1, 0);
	}
}

void GetAttributeDatatype::testGetFixedRecordType()
{
	try
	{
		IDatatype* datatype = this->defaultFederate->rtiamb->getAttributeDatatype(this->testCClassHandle, testFixedAttributeHandle);
		FixedRecordType* basicType = dynamic_cast<FixedRecordType*>(datatype);

		if (basicType == nullptr)
		{
			failTest("Datatype returned null");
			CPPUNIT_ASSERT_EQUAL(1, 0);
		}
		CPPUNIT_ASSERT_EQUAL(1, 1);
	}
	catch (Exception& e)
	{
		failTest("Unexpected exception publishing object class: %s", e.what());
		CPPUNIT_ASSERT_EQUAL(1, 0);
	}
}

void GetAttributeDatatype::testGetNAType()
{
	try
	{
		IDatatype* datatype = this->defaultFederate->rtiamb->getAttributeDatatype(this->testCClassHandle, testNAAttributeHandle);
		NaType* basicType = dynamic_cast<NaType*>(datatype);

		if (basicType == nullptr)
		{
			failTest("Datatype returned null");
			CPPUNIT_ASSERT_EQUAL(1, 0);
		}
		CPPUNIT_ASSERT_EQUAL(1, 1);
	}
	catch (Exception& e)
	{
		failTest("Unexpected exception publishing object class: %s", e.what());
		CPPUNIT_ASSERT_EQUAL(1, 0);
	} 
}

