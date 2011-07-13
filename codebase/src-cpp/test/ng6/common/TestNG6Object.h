/*
 *   Copyright 2008 The Portico Project
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
#ifndef TESTNG6OBJECT_H_
#define TESTNG6OBJECT_H_

#include "Common.h"

using namespace std;

class TestNG6Object
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
    	RTI::ObjectHandle objectHandle;
    	RTI::ObjectClassHandle classHandle;
    	char *objectName;
    	map<RTI::AttributeHandle,char*> *attributes;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		TestNG6Object( RTI::ObjectHandle objectHandle,
		               RTI::ObjectClassHandle classHandle,
		               const char *objectName );
		virtual ~TestNG6Object();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		RTI::ObjectHandle getHandle();
		RTI::ObjectClassHandle getClassHandle();
		char* getName();
		
		char* getAttribute( RTI::AttributeHandle attribute );
		bool  containsAttribute( RTI::AttributeHandle attribute );
		void  setAttribute( RTI::AttributeHandle attribute, char *newValue );
		void  updateAttributes( const RTI::AttributeHandleValuePairSet& theAttributes );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

#endif /*TESTNG6OBJECT_H_*/
