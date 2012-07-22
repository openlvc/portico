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
#include "TestNG6Interaction.h"

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
TestNG6Interaction::TestNG6Interaction( RTI::InteractionClassHandle classHandle, const char *tag )
{
	this->classHandle = classHandle;
	this->tag = new char[strlen(tag)+1];
	strcpy( this->tag, tag );
	
	this->parameters = new map<RTI::ParameterHandle,char*>();
}

TestNG6Interaction::~TestNG6Interaction()
{
	delete [] this->tag;
	MAP_CLEANUP( RTI::ParameterHandle, char*, parameters );
	delete this->parameters;
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Instance Methods //////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
RTI::InteractionClassHandle TestNG6Interaction::getClassHandle()
{
	return this->classHandle;
}

char* TestNG6Interaction::getTag()
{
	return this->tag;
}

double TestNG6Interaction::getTime()
{
	return this->time;
}

void TestNG6Interaction::setTime( double theTime )
{
	this->time = theTime;
}

int TestNG6Interaction::getSize()
{
	return parameters->size();
}

char* TestNG6Interaction::getParameter( RTI::ParameterHandle parameter )
{
	return (*parameters)[parameter];
}

bool TestNG6Interaction::containsParameter( RTI::ParameterHandle parameter )
{
	return parameters->find(parameter) != parameters->end();
}

/*
 * Store the value for the given parameter handle. If there is already a value for that handle,
 * delete the existing value and replace it with the new value. This method will take a COPY of
 * the provided value, so it is the callers responsibility to delete the given newValue once it
 * is finished.
 */
void TestNG6Interaction::setParameter( RTI::ParameterHandle parameter, char *newValue )
{
	// attempt to erase the existing value, no effect if it doesn't exist
	parameters->erase( parameter );

	// create a copy of the string and store it
	char *copy = new char[strlen(newValue)+1];
	strcpy( copy, newValue );
	(*parameters)[parameter] = copy;
}

/*
 * This will take the given PHVPS and update the local instance with the provided data.
 */
void TestNG6Interaction::updateParameters( const RTI::ParameterHandleValuePairSet& theParameters )
{
	for( RTI::ULong i = 0; i < theParameters.size(); i++ )
	{
		RTI::ParameterHandle parameterHandle = theParameters.getHandle( i );
		RTI::ULong length;
		setParameter( parameterHandle, theParameters.getValuePointer(i,length) );
	}
}
