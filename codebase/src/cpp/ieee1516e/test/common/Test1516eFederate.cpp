 #include "Test1516eFederate.h"
#include <iostream>
#include "RTI/time/HLAfloat64Interval.h"
#include "RTI/time/HLAfloat64Time.h"
 
//Test1516eFederate::SIMPLE_NAME = L"Test1516eFederate";
 

Test1516eFederate::Test1516eFederate(const wstring& name)
{
	// store the federate name (make sure it is a copy so that we can delete it when we want)
	this->name = name; 

	// create the RTIambassador
	RTIambassadorFactory factory = RTIambassadorFactory();
	this->rtiamb = factory.createRTIambassador().release();

	federateHandle = FederateHandle(); 


	// give some default values to the existing vars
	this->fedamb = NULL;
}

Test1516eFederate::~Test1516eFederate()
{
	delete this->rtiamb;
	if (this->fedamb != NULL)
	{
		delete this->fedamb;
	}		
}


/////////////////////////////////////////
///// federation management helpers /////
/////////////////////////////////////////
const wstring& Test1516eFederate::getFederateName()
{
	return this->name;
}
 

/////////////////////////////////////////
///// federation management helpers /////
/////////////////////////////////////////
void Test1516eFederate::quickCreate()
{
	this->quickCreate( SIMPLE_NAME);
}

void Test1516eFederate::quickCreate(const wstring& federationName)
{
	try
	{
		rtiamb->createFederationExecution(federationName, L"complete/etc/testfom.xml");
	}
	catch (FederationExecutionAlreadyExists& exists)
	{
		wcout << L"Didn't create federation, it already existed" << endl;
	} 
	catch (Exception &e)
	{
		killTest(e, "quickCreate()");
	}
}

void Test1516eFederate::quickConnect()
{
	// Create the FederateAmbassador implementation, removing the current one if it exists
	if (this->fedamb != NULL)
	{
		delete this->fedamb;
	}

	// Creat the federate ambassador.
	this->fedamb = new Test1516eFedAmb();

	// Try and connect
	try
	{
		rtiamb->connect(*this->fedamb, HLA_EVOKED);
	}
	catch (ConnectionFailed& connectionFailed)
	{
		wcout << L"Connection failed: " << connectionFailed.what() << endl;
	}
	catch (InvalidLocalSettingsDesignator& settings)
	{
		wcout << L"Connection failed, InvalidLocalSettingsDesignator: " << settings.what() << endl;
	}
	catch (UnsupportedCallbackModel& callbackModel)
	{
		wcout << L"Connection failed, UnsupportedCallbackModel: " << callbackModel.what() << endl;
	}
	catch (AlreadyConnected& connected)
	{
		wcout << L"Connection failed, AlreadyConnected: " << connected.what() << endl;
	}
	catch (RTIinternalError& error)
	{
		wcout << L"Connection failed, Generic Error: " << error.what() << endl;
	}
}

FederateHandle Test1516eFederate::quickJoin()
{
	// just call into quickJoin( const char* )
	return this->quickJoin(SIMPLE_NAME);
}

FederateHandle Test1516eFederate::quickJoin(const wstring& federationName)
{
	
	// try and join the federation
	try
	{
		this->federateHandle =
			rtiamb->joinFederationExecution(this->name, federationName, federationName);
		return this->federateHandle;
	}
	catch (Exception &e)
	{
		killTest(e, "quickJoin()");
		return FederateHandle();
	}
}

void Test1516eFederate::quickResign()
{
	this->quickResign(DELETE_OBJECTS_THEN_DIVEST);
}

void Test1516eFederate::quickResign(ResignAction action)
{
	try
	{
		rtiamb->resignFederationExecution(action);
	}
	catch (FederateNotExecutionMember &fnem)
	{
		// ignore this, we'll let it slide as there are situations where we wan to call this
		// but don't know if the federate has cleaned up after itself properly and resigned
		// its federates or not. In this case, if they HAVE done the right thing, we'll get
		// an exception, however, we don't trust ourselves to do the right thing ;)
	}
	catch (Exception &e)
	{
		killTest(e, "quickResign()");
	}
}

void Test1516eFederate::quickDestroy()
{
	this->quickDestroy(SIMPLE_NAME);
}

void Test1516eFederate::quickDestroy(const wstring& federationName)
{
	try
	{
		rtiamb->destroyFederationExecution(federationName);
	}
	catch (Exception &e)
	{
		killTest(e, "quickDestroy()");
	}
}

void Test1516eFederate::quickDestroyNoFail()
{
	try
	{
		rtiamb->destroyFederationExecution(SIMPLE_NAME);
	}
	catch (Exception &e)
	{
		// do nothing
	}
}

void Test1516eFederate::quickDisconnect()
{
	try
	{
		rtiamb->disconnect();
	}
	catch (Exception &e)
	{
		killTest(e, "quickDisconnect()");
	}
}



/////////////////////////////////////////
///// synchronization point helpers /////
/////////////////////////////////////////
void Test1516eFederate::quickAnnounce(const wstring& label, VariableLengthData tag)
{

	// check the tag
	if (tag.data() == NULL)
	{
		tag = VariableLengthData((void*)"NA", 2);
	}
 
	try
	{
		rtiamb->registerFederationSynchronizationPoint(label, tag);
	}
	catch (Exception &e)
	{
		killTest(e, "quickAnnounce()");
	}
}

void Test1516eFederate::quickAnnounce(const wstring& label, std::vector<FederateHandle> federates)
{
	// convert the handle set
	FederateHandleSet handleSet = FederateHandleSet();
	std::vector<FederateHandle>::iterator itr = federates.begin();
	va_list args;
	 
	for (itr; itr != federates.end(); itr++)
	{
		handleSet.insert((*itr));
	}
	 
	try
	{
		VariableLengthData tag((void*)"", 1);
		rtiamb->registerFederationSynchronizationPoint(label, tag, handleSet);		
	}
	catch (Exception &e)
	{
		killTest(e, "quickAnnounce(int[])");
	}
}

void Test1516eFederate::quickAchieved(const wstring& label)
{
	try
	{
		rtiamb->synchronizationPointAchieved(label);
	}
	catch (Exception &e)
	{
		killTest(e, "quickAchieved()");
	}
}


/////////////////////////////////////////
///// publish and subscribe helpers /////
/////////////////////////////////////////
void Test1516eFederate::quickPublish(ObjectClassHandle objectClass, std::vector<AttributeHandle> attributes)
{
	// convert the handle set
	AttributeHandleSet handleSet = AttributeHandleSet();
	std::vector<AttributeHandle>::iterator itr = attributes.begin();

	for (itr; itr != attributes.end(); itr++)
	{
		handleSet.insert((*itr));
	}

	try
	{
		rtiamb->publishObjectClassAttributes(objectClass, handleSet);
	}
	catch (Exception &e)
	{
		killTest(e, "quickAnnounce(int[])");
	}
}

void Test1516eFederate::quickPublish(const wstring& objectClass, std::vector<std::wstring> attributeNames)
{
	// resolve the handle for the class 
	ObjectClassHandle classHandle = rtiamb->getObjectClassHandle(objectClass);
	AttributeHandleSet attributeHandles = AttributeHandleSet();

	std::vector<std::wstring>::iterator itr = attributeNames.begin();

	for (itr; itr != attributeNames.end(); itr++)
	{
		AttributeHandle handle = rtiamb->getAttributeHandle(classHandle, (*itr));
		attributeHandles.insert(handle);
	}

	// attempt the do the publish
	try
	{
		rtiamb->publishObjectClassAttributes(classHandle, attributeHandles);
	}
	catch (Exception &e)
	{
		killTest(e, "quickPublish(attributes,name-based)");
	}
}

void Test1516eFederate::quickSubscribe(ObjectClassHandle objectClass, std::vector<AttributeHandle> attributes)
{
	// convert the handle set
	AttributeHandleSet handleSet = AttributeHandleSet();
	std::vector<AttributeHandle>::iterator itr = attributes.begin();

	for (itr; itr != attributes.end(); itr++)
	{
		handleSet.insert((*itr));
	}

	try
	{
		// attempt the publish
		rtiamb->subscribeObjectClassAttributes(objectClass, handleSet);
	}
	catch (Exception &e)
	{
		killTest(e, "quickSubscribe(attributes,handle-based)");
	}
}

void Test1516eFederate::quickSubscribe(const wstring&  objectClass, std::vector<std::wstring> attributeNames)
{
	// resolve the handle for the class 
	ObjectClassHandle classHandle = rtiamb->getObjectClassHandle(objectClass);
	AttributeHandleSet attributeHandles = AttributeHandleSet();

	std::vector<std::wstring>::iterator itr = attributeNames.begin();

	for (itr; itr != attributeNames.end(); itr++)
	{
		AttributeHandle handle = rtiamb->getAttributeHandle(classHandle, (*itr));
		attributeHandles.insert(handle);
	}

	// attempt the subscribe
	try
	{
		rtiamb->subscribeObjectClassAttributes(classHandle, attributeHandles);
	}
	catch (Exception &e)
	{
		killTest(e, "quickSubscribe(attributes,name-based)");
	}
}


void Test1516eFederate::quickPublish(InteractionClassHandle interactionClass)
{
	// attempt the publication
	try
	{
		rtiamb->publishInteractionClass(interactionClass);
	}
	catch (Exception &e)
	{
		killTest(e, "quickPublish(interaction,handle-based)");
	}
}

void Test1516eFederate::quickPublish(const wstring& interactionClass)
{
	InteractionClassHandle classHandle = rtiamb->getInteractionClassHandle(interactionClass);
	// attempt the publication
	try
	{
		rtiamb->publishInteractionClass(classHandle);
	}
	catch (Exception &e)
	{
		killTest(e, "quickPublish(interaction,name-based)");
	}
}

void Test1516eFederate::quickSubscribe(InteractionClassHandle interactionClass)
{
	// attempt the subscription
	try
	{
		rtiamb->subscribeInteractionClass(interactionClass);
	}
	catch (Exception &e)
	{
		killTest(e, "quickSubscribe(interaction,handle-based)");
	}
}

void Test1516eFederate::quickSubscribe(const wstring& interactionClass)
{
	// resolve the handle
	InteractionClassHandle classHandle = rtiamb->getInteractionClassHandle(interactionClass);

	// attempt the subscription
	try
	{
		rtiamb->subscribeInteractionClass(classHandle);
	}
	catch (Exception &e)
	{
		killTest(e, "quickSubscribe(interaction,name-based)");
	}
}


void Test1516eFederate::quickUnpublishOC(const wstring&  objectClass)
{
	// resolve the handle for the class
	ObjectClassHandle classHandle = rtiamb->getObjectClassHandle(objectClass);

	try
	{
		rtiamb->unpublishObjectClass(classHandle);
	}
	catch (Exception &e)
	{
		killTest(e, "quickUnpublishOC()");
	}
}


///////////////////////////////////////////////
///// object registration/removal helpers /////
///////////////////////////////////////////////
ObjectInstanceHandle Test1516eFederate::quickRegister(ObjectClassHandle classHandle)
{
	try
	{
		return rtiamb->registerObjectInstance(classHandle);
	}
	catch (Exception &e)
	{
		killTest(e, "quickRegister(int)");
		return ObjectInstanceHandle();
	}
}

ObjectInstanceHandle Test1516eFederate::quickRegister(ObjectClassHandle classHandle, const wstring& objectName)
{
	try
	{
		return rtiamb->registerObjectInstance(classHandle, objectName);
	}
	catch (Exception &e)
	{
		killTest(e, "quickRegister(int,char*)");
		return ObjectInstanceHandle();
	}
}

ObjectInstanceHandle Test1516eFederate::quickRegister(const wstring& className)
{
	// resolve the handle before the request
	ObjectClassHandle classHandle = rtiamb->getObjectClassHandle(className);

	try
	{
		return rtiamb->registerObjectInstance(classHandle);
	}
	catch (Exception &e)
	{
		killTest(e, "quickRegister(char*)");
		return ObjectInstanceHandle();
	}
}

ObjectInstanceHandle Test1516eFederate::quickRegister(const wstring& className, const const wstring& objectName)
{
	// resolve the handle before the request
	ObjectClassHandle classHandle = rtiamb->getObjectClassHandle(className);

	try
	{
		return rtiamb->registerObjectInstance(classHandle, objectName);
	}
	catch (Exception &e)
	{
		killTest(e, "quickRegister(char*)");
		return ObjectInstanceHandle();
	}
}

void  Test1516eFederate::quickRegisterFail(ObjectClassHandle classHandle)
{
	try
	{
		rtiamb->registerObjectInstance(classHandle);
		// FAIL THE TEST, shouldn't get here if there is an exception as expected
		killTest("Was expecting registration of class [%s] would fail", classHandle);
	}
	catch (Exception &e)
	{
		// success!
	}
}

void Test1516eFederate::quickDelete(ObjectInstanceHandle objectHandle, VariableLengthData tag)
{
	// check the tag
	if (tag.data() == NULL)
	{
		tag = VariableLengthData((void*)"NA", 2);
	}
		

	try
	{
		rtiamb->deleteObjectInstance(objectHandle, tag);
	}
	catch (Exception &e)
	{
		killTest(e, "quickDelete()");
	}
}


//////////////////////////////////////////////
///// reflection and interaction helpers /////
//////////////////////////////////////////////
void Test1516eFederate::quickReflect(ObjectInstanceHandle objectHandle,
	AttributeHandleValueMap *ahvps,
	VariableLengthData tag)
{
	// check the tag
	if (tag.data() == NULL)
	{
		tag = VariableLengthData((void*)"NA", 2);
	}

	try
	{
		rtiamb->updateAttributeValues(objectHandle, *ahvps, tag);
	}
	catch (Exception &e)
	{
		killTest(e, "quickReflect()");
	}
}


void Test1516eFederate::quickReflect(ObjectInstanceHandle objectHandle,
	std::vector<std::wstring> attributeNames)
{

	ObjectClassHandle classHandle = rtiamb->getKnownObjectClassHandle(objectHandle); 
	AttributeHandleValueMap valueMap = AttributeHandleValueMap();

	std::vector<std::wstring>::iterator itr = attributeNames.begin();

	for (itr; itr != attributeNames.end(); itr++)
	{
		// Get the attribute handle
		AttributeHandle handle = rtiamb->getAttributeHandle(classHandle, (*itr));

		// Create a dummy value based off the names of the attributes
		char demoValue[32];
		sprintf(demoValue, "%s", (*itr).c_str());
		VariableLengthData demoData((void*)demoValue, strlen(demoValue)+1);

		// Add the attribute value in the the attribute value map.
		valueMap[handle] = demoData;
	}

	try
	{
		VariableLengthData tag((void*)"Hi!", 4);
		rtiamb->updateAttributeValues(objectHandle, valueMap, tag);
	}
	catch (Exception &e)
	{
		killTest(e, "quickReflect()");
	}
}


void Test1516eFederate::quickReflectFail(ObjectInstanceHandle handle,
	AttributeHandleValueMap* ahvps,
	VariableLengthData tag)
{
	VariableLengthData safeTag;

	// check the tag
	if (tag.data() == NULL)
	{
		safeTag = VariableLengthData((void*)"NA", 2);
	}
	else
	{
		safeTag = tag;
	}

	// attempt the update
	try
	{
		rtiamb->updateAttributeValues(handle, *ahvps, safeTag);
		killTest("Was expecting an exception during the updateAttributeValues call");
	}
	catch (Exception &e)
	{
		// success!
	}
}


void Test1516eFederate::quickSend(InteractionClassHandle classHandle,
	ParameterHandleValueMap *phvps,
	VariableLengthData tag)
{

	// check the tag
	if (tag.data() == NULL)
	{
		tag = VariableLengthData((void*)"NA", 2);
	}

	try
	{
		rtiamb->sendInteraction(classHandle, *phvps, tag);
	}
	catch (Exception &e)
	{
		killTest(e, "quickSend()");
	}
}


void Test1516eFederate::quickSend(InteractionClassHandle classHandle,
	std::vector<std::wstring> parameterNames)
{
	//////////////////////
	// create the PHVPS //
	//////////////////////
	// get the interaction class name for the class handle (so.

	std::vector<std::wstring>::iterator itr = parameterNames.begin();
	ParameterHandleValueMap valueMap = ParameterHandleValueMap();

	for (itr; itr != parameterNames.end(); itr++)
	{
		// Get the attribute handle
		ParameterHandle handle = rtiamb->getParameterHandle(classHandle, (*itr));

		// Create a dummy value based off the names of the attributes
		char demoValue[32];
		sprintf(demoValue, "%s", (*itr).c_str());
		VariableLengthData demoData((void*)demoValue, strlen(demoValue) + 1);

		// Add the attribute value in the the attribute value map.
		valueMap[handle] = demoData;
	}

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		rtiamb->sendInteraction(classHandle, valueMap, VariableLengthData((void*)"NA", 2));
	}
	catch (Exception &e)
	{
		killTest(e, "quickSend()");
	}


}


void Test1516eFederate::quickSend(InteractionClassHandle classHandle,
	double time,
	std::vector<std::wstring> parameterNames)
{
	//////////////////////
	// create the PHVPS //
	//////////////////////
	// get the interaction class name for the class handle (so
	//std::wstring classHandle = rtiamb->getInteractionClassName(classHandle);
	std::vector<std::wstring>::iterator itr = parameterNames.begin();
	ParameterHandleValueMap valueMap = ParameterHandleValueMap();

	for (itr; itr != parameterNames.end(); itr++)
	{
		// Get the attribute handle
		ParameterHandle handle = rtiamb->getParameterHandle(classHandle, (*itr));

		// Create a dummy value based off the names of the attributes
		char demoValue[32];
		sprintf(demoValue, "%s", (*itr).c_str());
		VariableLengthData demoData((void*)demoValue, strlen(demoValue) + 1);

		// Add the attribute value in the the attribute value map.
		valueMap[handle] = demoData;
	}

	//////////////////////////
	// sent the interaction //
	//////////////////////////
	try
	{
		auto_ptr<HLAfloat64Time> time(new HLAfloat64Time(fedamb->federateTime + fedamb->federateLookahead));
		rtiamb->sendInteraction(classHandle, valueMap, VariableLengthData((void*)"NA", 2), *time);
	}
	catch (Exception &e)
	{
		killTest(e, "quickSend()");
	}
}


void Test1516eFederate::quickSendFail(InteractionClassHandle classHandle,
	ParameterHandleValueMap *phvps,
	const VariableLengthData& tag)
{
	VariableLengthData safeTag;

	// check the tag
	if (tag.data() == NULL)
	{
		safeTag = VariableLengthData((void*)"NA", 2);
	}
	else
	{
		safeTag = tag;
	}

	// attempt the interaction
	try
	{
		rtiamb->sendInteraction( classHandle, *phvps, safeTag);
		killTest("Was expecting an exception during the sendInteraction call");
	}
	catch (Exception &e)
	{
		// success!
	}
}


//////////////////////////////////////////
///// time management helper methods /////
//////////////////////////////////////////
void  Test1516eFederate::quickEnableConstrained()
{
	try
	{
		rtiamb->enableTimeConstrained();
		//fedamb->waitForConstrainedEnabled();
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickEnableConstrained()");
	}
}

void   Test1516eFederate::quickEnabledConstrainedRequest()
{
	try
	{
		rtiamb->enableTimeConstrained();
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickEnableConstrainedRequest()");
	}
}

void   Test1516eFederate::quickDisableConstrained()
{
	try
	{
		rtiamb->disableTimeConstrained();
		//fedamb->constrained = RTI::RTI_FALSE;
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickDisableConstrained()");
	}
}

void  Test1516eFederate::quickEnableAsync()
{
	try
	{
		rtiamb->enableAsynchronousDelivery();
	}
	catch (Exception& e)
	{
		killTest(e, "quickEnableAsync()");
	}
}

void   Test1516eFederate::quickDisableAsync()
{

	try
	{
		rtiamb->disableAsynchronousDelivery();
	}
	catch (Exception& e)
	{
		killTest(e, "quickDisableAsync()");
	}
}

void Test1516eFederate::quickEnableRegulating(double lookahead)
{
	auto_ptr<HLAfloat64Interval> interval(new HLAfloat64Interval(lookahead)); 

	try
	{
		rtiamb->enableTimeRegulation(*interval);
		// tick until we get the callback
		while (fedamb->isRegulating == false)
		{
			rtiamb->evokeMultipleCallbacks(0.1, 1.0);
		}
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickEnableRegulating()");
	}
}

void  Test1516eFederate::quickEnableRegulatingRequest(double lookahead)
{
	auto_ptr<HLAfloat64Interval> interval(new HLAfloat64Interval(lookahead));

	try
	{
		rtiamb->enableTimeRegulation(*interval);
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickEnableRegulatingRequest()");
	}
}

void Test1516eFederate::quickDisableRegulating()
{
	try
	{ 
		rtiamb->disableTimeRegulation();
		fedamb->isRegulating = false;
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickDisableRegulating()");
	}
}

double Test1516eFederate::quickQueryLookahead()
{

	return fedamb->federateLookahead;
 
}

void   Test1516eFederate::quickModifyLookahead(double newLookahead)
{
	auto_ptr<HLAfloat64Interval> lookahead(new HLAfloat64Interval(newLookahead));

	try
	{
		rtiamb->modifyLookahead(*lookahead);
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickAdvanceRequest()");
	}

}

void   Test1516eFederate::quickAdvanceRequest(double newTime)
{
	auto_ptr<HLAfloat64Time> lookahead(new HLAfloat64Time(newTime));

	try
	{
		rtiamb->timeAdvanceRequest(*lookahead);
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickAdvanceRequest()");
	}
}

void   Test1516eFederate::quickAdvanceAndWait(double newTime)
{
	auto_ptr<HLAfloat64Time> request(new HLAfloat64Time(newTime));

	try
	{
		rtiamb->timeAdvanceRequest(*request);
		// wait for the advance to occur
		while (fedamb->isAdvancing)
		{
			rtiamb->evokeMultipleCallbacks(0.1, 1.0);
		}
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickAdvanceRequestAndWait()");
	}
}

void   Test1516eFederate::quickAdvanceRequestAvailable(double newTime)
{
	auto_ptr<HLAfloat64Time> request(new HLAfloat64Time(newTime));

	try
	{
		rtiamb->timeAdvanceRequestAvailable(*request);
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickAdvanceRequestAvailable()");
	}
}

void   Test1516eFederate::quickNextEventRequest(double newTime)
{
	auto_ptr<HLAfloat64Time> request(new HLAfloat64Time(newTime));

	try
	{
		rtiamb->nextMessageRequest(*request);
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickAdvanceRequestAvailable()");
	}
}

void Test1516eFederate::quickFlushQueueRequest(double maxTime)
{
	auto_ptr<HLAfloat64Time> request(new HLAfloat64Time(maxTime));

	try
	{
		rtiamb->flushQueueRequest(*request);
	}
	catch (Exception &e)
	{
		this->killTest(e, "quickAdvanceRequestAvailable()");
	}
}



//////////////////////////////////////////
///// general utility helper methods /////
//////////////////////////////////////////
RTIambassador* Test1516eFederate::getRtiAmb()
{
	return this->rtiamb;
}

void Test1516eFederate::quickTick()
{
	try
	{
		// request the advance
		fedamb->isAdvancing = true;
		auto_ptr<HLAfloat64Time> newTime(new HLAfloat64Time(fedamb->federateTime + 1.0));
		rtiamb->timeAdvanceRequest(*newTime);
		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while (fedamb->isAdvancing)
		{
			rtiamb->evokeMultipleCallbacks(0.1, 1.0);
		}
	}
	catch (Exception &e)
	{
		killTest(e, "quickTick()");
	}
}

void Test1516eFederate::quickTick(double min, double max)
{
	try
	{
		// request the advance
		fedamb->isAdvancing = true;
		auto_ptr<HLAfloat64Time> newTime(new HLAfloat64Time(fedamb->federateTime + 1.0));
		rtiamb->timeAdvanceRequest(*newTime);
		// wait for the time advance to be granted. ticking will tell the
		// LRC to start delivering callbacks to the federate
		while (fedamb->isAdvancing)
		{
			rtiamb->evokeMultipleCallbacks(min, max);
		}
	}
	catch (Exception &e)
	{
		killTest(e, "quickTick()");
	}
}

void Test1516eFederate::killTest(Exception &e, const char* activeMethod)
{
	//// create a message notifying of the death
	//char message[4096];
	//sprintf(message,
	//	"(%s) Unexpected exception in %s: %s", this->name, activeMethod, e._reason);

	//// kill the test
	////CPPUNIT_FAIL(message);
}

void Test1516eFederate::killTest(const char *format, ...)
{
	//// start the var-arg stuff 
	//va_list args;
	//va_start(args, format);

	//// turn the args into a single string
	//// http://www.cplusplus.com/reference/clibrary/cstdio/vsprintf.html
	//char buffer[4096];
	//vsprintf(buffer, format, args);

	//// clean up the varargs
	//va_end(args);

	//// kill the test
	//CPPUNIT_FAIL(buffer);
}
 
 