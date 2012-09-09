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
HLAfloat64Time::HLAfloat64Time()
{
	this->_impl = new HLAfloat64TimeImpl();
	this->_impl->time = 0.0;
}

HLAfloat64Time::HLAfloat64Time( const Float64& value )
{
	this->_impl = new HLAfloat64TimeImpl();
	this->_impl->time = value;
}

HLAfloat64Time::HLAfloat64Time( const LogicalTime& value )
{
	this->_impl = new HLAfloat64TimeImpl();
	this->_impl->time = ((HLAfloat64Time)value)._impl->time;
}

HLAfloat64Time::HLAfloat64Time( const HLAfloat64Time& value )
{
	this->_impl = new HLAfloat64TimeImpl();
	this->_impl->time = value._impl->time;
}

HLAfloat64Time::~HLAfloat64Time() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
Float64 HLAfloat64Time::getTime() const
{
	return this->_impl->time;
}

void HLAfloat64Time::setTime( Float64 value )
{
	this->_impl->time = value;
}

HLAfloat64Time& HLAfloat64Time::operator= ( const HLAfloat64Time& rhs )
	throw( InvalidLogicalTime )
{
	this->_impl->time = rhs._impl->time;
	return *this;
}

HLAfloat64Time::operator Float64() const
{
	return this->_impl->time;
}

////////////////////////////////////////////////////////////
//////////////// Inherited from LogicalTime //////////////// 
////////////////////////////////////////////////////////////
// Basic accessors/mutators
void HLAfloat64Time::setInitial()
{
	setTime( 0.0 );
}

bool HLAfloat64Time::isInitial() const
{
	return getTime() == 0.0;
}

void HLAfloat64Time::setFinal()
{
	setTime( HLA_TIME_FLOAT_MAX );
}

bool HLAfloat64Time::isFinal() const
{
	return getTime() == HLA_TIME_FLOAT_MAX;
}

// Generates an encoded value that can be used to send
// LogicalTimes to other federates in updates or interactions
VariableLengthData HLAfloat64Time::encode() const
{
	// TODO fill me out!
	return VariableLengthData();
}

// Alternate encode for directly filling a buffer
// Return the length of the encoded data
size_t HLAfloat64Time::encode( void* buffer, size_t bufferSize ) const
	throw( CouldNotEncode )
{
	// TODO fill me out!
	return 0;
}

// The length of the encoded data
size_t HLAfloat64Time::encodedLength() const
{
	return 8;
}

// Decode VariableLengthData into self
void HLAfloat64Time::decode( const VariableLengthData& VariableLengthData )
	throw( InternalError, CouldNotDecode )
{
	// TODO fill me out!
}

// Alternate decode that reads directly from a buffer
void HLAfloat64Time::decode( void* buffer, size_t bufferSize )
	throw( InternalError, CouldNotDecode )
{
	// TODO fill me out!
}

// Diagnostic string representation of time
std::wstring HLAfloat64Time::toString() const
{
	wstringstream wss;
	wss << getTime();
	return wss.str();
}

// Return the name of the implementation, as needed by
// createFederationExecution.
std::wstring HLAfloat64Time::implementationName() const
{
	return L"HLAfloat64Time";
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Assignment
LogicalTime& HLAfloat64Time::operator= ( const LogicalTime& value )
	throw( InvalidLogicalTime )
{
	this->_impl->time = ((HLAfloat64Time)value)._impl->time;
	return *this;
}

// Operators
LogicalTime& HLAfloat64Time::operator+= ( const LogicalTimeInterval& addend )
	throw( IllegalTimeArithmetic, InvalidLogicalTimeInterval )
{
	this->_impl->time += ((HLAfloat64Interval)addend).getInterval();
	return *this;
}

LogicalTime& HLAfloat64Time::operator-= ( const LogicalTimeInterval& subtrahend )
	throw( IllegalTimeArithmetic, InvalidLogicalTimeInterval )
{
	this->_impl->time -= ((HLAfloat64Interval)subtrahend).getInterval();
	return *this;
}

bool HLAfloat64Time::operator> ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time > ((HLAfloat64Time)value)._impl->time;
}

bool HLAfloat64Time::operator< ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time < ((HLAfloat64Time)value)._impl->time;
}

bool HLAfloat64Time::operator== ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time == ((HLAfloat64Time)value)._impl->time;
}

bool HLAfloat64Time::operator>= ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time >= ((HLAfloat64Time)value)._impl->time;
}

bool HLAfloat64Time::operator<= ( const LogicalTime& value ) const
	throw( InvalidLogicalTime )
{
	return _impl->time <= ((HLAfloat64Time)value)._impl->time;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
