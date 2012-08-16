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
#ifndef TESTNG6INTERACTION_H_
#define TESTNG6INTERACTION_H_

#include "Common.h"

using namespace std;

class TestNG6Interaction : public CppUnit::TestFixture
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		RTI::InteractionClassHandle classHandle;
		char *tag;
		double time;
		map<RTI::ParameterHandle,char*> *parameters;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		TestNG6Interaction( RTI::InteractionClassHandle classHandle, const char *tag );
		virtual ~TestNG6Interaction();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		RTI::InteractionClassHandle getClassHandle();
		char* getTag();
		double getTime();
		void setTime( double theTime );
		int getSize();
		
		char* getParameter( RTI::ParameterHandle parameter );
		bool  containsParameter( RTI::ParameterHandle parameter );
		void  setParameter( RTI::ParameterHandle parameter, char *newValue );
		void  updateParameters( const RTI::ParameterHandleValuePairSet& theParameters );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------

};

#endif /*TESTNG6INTERACTION_H_*/
