/*
 *   Copyright 2012 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License(CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *  (that goes for your lawyer as well)
 *
 */
#include "common.h"
#include "types/time/TimeImplementations.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAinteger64Time::HLAinteger64Time()
	: LogicalTime()
{
	this->_impl = new HLAinteger64TimeImpl();
	this->_impl->time = 0;
}

HLAinteger64Time::HLAinteger64Time( Integer64 value )
{
	this->_impl = new HLAinteger64TimeImpl();
	this->_impl->time = value;
}

HLAinteger64Time::HLAinteger64Time( const LogicalTime& value )
	: LogicalTime(value)
{
	this->_impl = new HLAinteger64TimeImpl();
	this->_impl->time = ((HLAinteger64Time)value)._impl->time;
}

HLAinteger64Time::HLAinteger64Time( const HLAinteger64Time& value )
{
	this->_impl = new HLAinteger64TimeImpl();
	this->_impl->time = value._impl->time;
}

HLAinteger64Time::~HLAinteger64Time() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
Integer64 HLAinteger64Time::getTime() const
{
	return this->_impl->time;
}

void HLAinteger64Time::setTime( Integer64 value )
{
	this->_impl->time = value;
}

HLAinteger64Time& HLAinteger64Time::operator= ( const HLAinteger64Time& rhs )
	throw( InvalidLogicalTime )
{
	this->_impl->time = rhs._impl->time;
	return *this;
}

HLAinteger64Time::operator Integer64() const
{
	return this->_impl->time;
}


////////////////////////////////////////////////////////////
//////////////// Inherited from LogicalTime //////////////// 
////////////////////////////////////////////////////////////
// Basic accessors/mutators
void HLAinteger64Time::setInitial()
{
	setTime( 0 );
}

bool HLAinteger64Time::isInitial() const
{
	return getTime() == 0;
}

void HLAinteger64Time::setFinal()
{
	setTime( HLA_TIME_INTEGER_MAX );
}

bool HLAinteger64Time::isFinal() const
{
	return getTime() == HLA_TIME_INTEGER_MAX;
}

// Generates an encoded value that can be used to send
// LogicalTimes to other federates in updates or interactions
VariableLengthData HLAinteger64Time::encode() const
{
	// TODO fill me out!
	return VariableLengthData();
}

// Alternate encode for directly filling a buffer
// Return the length of the encoded data
size_t HLAinteger64Time::encode( void* buffer, size_t bufferSize ) const
	throw( CouldNotEncode )
{
	// TODO fill me out!
	return 0;
}

// The length of the encoded data
size_t HLAinteger64Time::encodedLength() const
{
	return 8;
}

// Decode VariableLengthData into self
void HLAinteger64Time::decode( const VariableLengthData& VariableLengthData )
	throw( InternalError, CouldNotDecode )
{
	// TODO fill me out!
}

// Alternate decode that reads directly from a buffer
void HLAinteger64Time::decode( void* buffer, size_t bufferSize )
	throw( InternalError, CouldNotDecode )
{
	// TODO fill me out!
}

// Diagnostic string representation of time
std::wstring HLAinteger64Time::toString() const
{
	wstringstream wss;
	wss << getTime();
	return wss.str();
}

// Return the name of the implementation, as needed by
// createFederationExecution.
std::wstring HLAinteger64Time::implementationName() const
{
	return L"HLAinteger64Time";
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment
LogicalTime& HLAinteger64Time::operator= ( const LogicalTime& value )
	throw( InvalidLogicalTime )
{
	this->_impl->time = ((HLAinteger64Time)value).getTime();
	return *this;
}

// Operators
LogicalTime& HLAinteger64Time::operator+= ( const LogicalTimeInterval& addend )
	throw( IllegalTimeArithmetic, InvalidLogicalTimeInterval )
{
	this->_impl->time += ((HLAinteger64Interval)addend).getInterval();
	return *this;
}

LogicalTime& HLAinteger64Time::operator-= ( const LogicalTimeInterval& subtrahend )
	throw( IllegalTimeArithmetic, InvalidLogicalTimeInterval )
{
	this->_impl->time -= ((HLAinteger64Interval)subtrahend).getInterval();
	return *this;
}

bool HLAinteger64Time::operator> ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time > ((HLAinteger64Time)value).getTime();
}

bool HLAinteger64Time::operator< ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time < ((HLAinteger64Time)value).getTime();
}

bool HLAinteger64Time::operator== ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time == ((HLAinteger64Time)value).getTime();
}

bool HLAinteger64Time::operator>= ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time >= ((HLAinteger64Time)value).getTime();
}

bool HLAinteger64Time::operator<= ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time <= ((HLAinteger64Time)value).getTime();
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
