/*
*   Copyright 2012 The Portico Project
*
*   This file is part of portico.
*
*   portico is free software; you can redistribute it and/or modify
*   it under the terms of the Common Developer and Distribution License (CDDL)
*   as published by Sun Microsystems. For more information see the LICENSE file.
*
*   Use of this software is strictly AT YOUR OWN RISK!!!
*   If something bad happens you do not have permission to come crying to me.
*   (that goes for your lawyer as well)
*
*/
#pragma once

#include "Test1516eFedAmb.h"
#include "common.h" 
#include <vector>
using namespace portico1516e;
using namespace rti1516e;
using namespace std;

#define READY_TO_RUN L"ReadyToRun"
#define OWNER_UNOWNED -1
#define OWNER_RTI 0

 
const wstring SIMPLE_NAME = L"Test1516eFederate";

class Test1516eFederate
{ 
	public:

		RTIambassadorEx *rtiamb;
		Test1516eFedAmb *fedamb;
		FederateHandle federateHandle;
		wstring name;


	public:

		Test1516eFederate(const wstring& name);
		~Test1516eFederate();

		/////////////////////////////////////////
		///// federation management helpers /////
		/////////////////////////////////////////
		const wstring& getFederateName();
		FederateHandle getFederateHandle();
	
		/////////////////////////////////////////
		///// federation management helpers /////
		/////////////////////////////////////////
		void quickCreate();
		void quickCreate(const wstring& federationName);
		void quickCreate(const wstring& federationName, const wstring& FOMPath); 
		void quickConnect();
		FederateHandle quickJoin();
		FederateHandle quickJoin(const wstring& federationName);
		void quickResign();
		void quickResign(ResignAction action);
		void quickDestroy();
		void quickDestroy(const wstring& federationName);
		void quickDestroyNoFail();
		void quickDisconnect();


		/////////////////////////////////////////
		///// synchronization point helpers /////
		/////////////////////////////////////////
		void quickAnnounce(const wstring& label, VariableLengthData tag);
		void quickAnnounce(const wstring& label, std::vector<FederateHandle> federates);
		void quickAchieved(const wstring& label);

		/////////////////////////////////////////
		///// publish and subscribe helpers /////
		/////////////////////////////////////////
		void quickPublish(ObjectClassHandle objectClass, std::vector<AttributeHandle> attributes);
		void quickPublish(const wstring& objectClass, std::vector<std::wstring> attributeNames);
		void quickSubscribe(ObjectClassHandle objectClass, std::vector<AttributeHandle> attributes);
		void quickSubscribe(const wstring&  objectClass, std::vector<std::wstring> attributeNames);

		void quickPublish(InteractionClassHandle interactionClass);
		void quickPublish(const wstring& interactionClass);
		void quickSubscribe(InteractionClassHandle interactionClass);
		void quickSubscribe(const wstring& interactionClass);

		void quickUnpublishOC(const wstring&  objectClass);

		///////////////////////////////////////////////
		///// object registration/removal helpers /////
		///////////////////////////////////////////////
		ObjectInstanceHandle quickRegister(ObjectClassHandle classHandle);
		ObjectInstanceHandle quickRegister(ObjectClassHandle classHandle, const wstring& objectName);
		ObjectInstanceHandle quickRegister(const wstring& className);
		ObjectInstanceHandle quickRegister(const wstring& className, const wstring& objectName);
		void                 quickRegisterFail(ObjectClassHandle classHandle);
		void                 quickDelete(ObjectInstanceHandle objectHandle, VariableLengthData tag);

		//////////////////////////////////////////////
		///// reflection and interaction helpers /////
		//////////////////////////////////////////////
		void quickReflect(ObjectInstanceHandle objectHandle,
						  AttributeHandleValueMap *ahvps,
						  VariableLengthData tag);

		void quickReflect(ObjectInstanceHandle objectHandle,
						  std::vector<std::wstring> attributeNames);

		void quickReflectFail(ObjectInstanceHandle handle,
							  AttributeHandleValueMap *ahvps,
							  VariableLengthData tag);

		void quickSend(InteractionClassHandle classHandle, 
					   ParameterHandleValueMap *phvps, 
					   VariableLengthData tag);

		void quickSend(InteractionClassHandle classHandle, 
					   std::vector<std::wstring> parameterNames);

		void quickSend(InteractionClassHandle classHandle, 
					   double time, 
					   std::vector<std::wstring> parameterNames);

		void quickSendFail(InteractionClassHandle classHandle,
						   ParameterHandleValueMap *phvps,
						   const VariableLengthData& tag);

		//////////////////////////////////////////
		///// time management helper methods /////
		//////////////////////////////////////////
		void   quickEnableConstrained();
		void   quickEnabledConstrainedRequest();
		void   quickDisableConstrained();
		void   quickEnableAsync();
		void   quickDisableAsync();
		void   quickEnableRegulating(double lookahead);
		void   quickEnableRegulatingRequest(double lookahead);
		void   quickDisableRegulating();
		double quickQueryLookahead();
		void   quickModifyLookahead(double newLookahead);
		void   quickAdvanceRequest(double newTime);
		void   quickAdvanceAndWait(double newTime);
		void   quickAdvanceRequestAvailable(double newTime);
		void   quickNextEventRequest(double newTime);
		void   quickFlushQueueRequest(double maxTime);


		//////////////////////////////////////////
		///// general utility helper methods /////
		//////////////////////////////////////////
		RTIambassador* getRtiAmb();
		void quickTick();
		void quickTick(double min, double max);
		void killTest(Exception &e, const char* activeMethod);
		void killTest(const char *format, ...);
};

