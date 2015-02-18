/*
 *   Copyright 2009 The Portico Project
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
#include "ActiveSR.h"

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
ActiveSR::ActiveSR()
	: label()
{
	this->status = UNINITIATED;
	this->federateHandle = -1;
}

ActiveSR::~ActiveSR()
{

}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Instance Methods //////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
void ActiveSR::reset()
{
	this->label = "";
	this->status = UNINITIATED;
}

void ActiveSR::initiate( const char *label )
{
	setLabel( label );
	this->status = INITIATED;
}

void ActiveSR::initiate( const char *label, int federateHandle )
{
	setLabel( label );
	this->status = INITIATED;
	this->federateHandle = federateHandle;
}

void ActiveSR::begun()
{
	this->status = BEGUN;
}

void ActiveSR::success()
{
	this->status = COMPLETED_SUCCESS;
}

void ActiveSR::failure()
{
	this->status = COMPLETED_FAILURE;
}

RTI::Boolean ActiveSR::isInitiated( const char* expectedLabel )
{
	if( this->label.empty() )
		return RTI::RTI_FALSE;
	
	if( this->status != INITIATED )
		return RTI::RTI_FALSE;
	
	if( this->label != expectedLabel )
		return RTI::RTI_FALSE;
	else
		return RTI::RTI_TRUE;
}

RTI::Boolean ActiveSR::isBegun()
{
	if( status == BEGUN || status == INITIATED )
		return RTI::RTI_TRUE;
	else
		return RTI::RTI_FALSE;
}

RTI::Boolean ActiveSR::isSuccess()
{
	if( status == COMPLETED_SUCCESS )
		return RTI::RTI_TRUE;
	else
		return RTI::RTI_FALSE;
}

RTI::Boolean ActiveSR::isFailure()
{
	if( status == COMPLETED_FAILURE )
		return RTI::RTI_TRUE;
	else
		return RTI::RTI_FALSE;
}

RTI::Boolean ActiveSR::isComplete()
{
	if( isSuccess() == RTI::RTI_TRUE || isFailure() == RTI::RTI_TRUE )
		return RTI::RTI_TRUE;
	else
		return RTI::RTI_FALSE;
}

void ActiveSR::setLabel( const char* newLabel )
{
	this->label = string( newLabel );
}

int ActiveSR::getFederateHandle()
{
	return this->federateHandle;
}

//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Static Methods ///////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
