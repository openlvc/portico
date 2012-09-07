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
#include "types/time/HLAfloat64TimeImpl.h"
#include "types/time/HLAfloat64IntervalImpl.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAfloat64Interval::HLAfloat64Interval()
{
	this->_impl = new HLAfloat64IntervalImpl( 0.0 );
}

HLAfloat64Interval::HLAfloat64Interval( Float64 value )
{
	this->_impl = new HLAfloat64IntervalImpl( value );
}

HLAfloat64Interval::HLAfloat64Interval( const LogicalTimeInterval& rhs )
{
	this->_impl = new HLAfloat64IntervalImpl( ((HLAfloat64Interval)rhs)._impl->getValue() );
}

HLAfloat64Interval::HLAfloat64Interval( const HLAfloat64Interval& rhs )
{
	this->_impl = new HLAfloat64IntervalImpl( rhs._impl->getValue() );
}

// Destructor
HLAfloat64Interval::~HLAfloat64Interval() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
double HLAfloat64Interval::getInterval() const
{
	return this->_impl->getValue();
}

void HLAfloat64Interval::setInterval( Float64 value )
{
	this->_impl->setValue( value );
}

HLAfloat64Interval& HLAfloat64Interval::operator=( const HLAfloat64Interval& value )
	throw( InvalidLogicalTimeInterval )
{
	this->_impl->setValue( value._impl->getValue() );
	return *this;
}

HLAfloat64Interval::operator Float64() const
{
	return this->_impl->getValue();
}

////////////////////////////////////////////////////////////
//////////////// Inherited from LogicalTime //////////////// 
////////////////////////////////////////////////////////////
// Basic accessors/mutators
void HLAfloat64Interval::setZero()
{
	this->setInterval( 0.0 );
}

bool HLAfloat64Interval::isZero() const
{
	return this->getInterval() == 0.0;
}

void HLAfloat64Interval::setEpsilon()
{
	this->setInterval( 0.000000000000001 );
}

bool HLAfloat64Interval::isEpsilon() const
{
	return this->getInterval() == 0.000000000000001;
}

// Generates an encoded value that can be used to send
// LogicalTimeIntervals to other federates in updates or interactions
VariableLengthData HLAfloat64Interval::encode() const
{
	// TODO fill me out
	return VariableLengthData();
}

// Alternate encode for directly filling a buffer
// Return the length of encoded data
size_t HLAfloat64Interval::encode( void* buffer, size_t bufferSize ) const
	throw( CouldNotEncode )
{
	// TODO fill me out
	return 0;
}

// The length of the encoded data
size_t HLAfloat64Interval::encodedLength() const
{
	return 8;
}

// Decode encodedValue into self
void HLAfloat64Interval::decode( const VariableLengthData& encodedValue )
	throw( InternalError, CouldNotDecode )
{
	// TODO fill me out
}

// Decode encodedValue into self
// Alternate decode that reads directly from a buffer
void HLAfloat64Interval::decode( void* buffer, size_t bufferSize )
	throw( InternalError, CouldNotDecode )
{
	// TODO fill me out
}

// Diagnostic string representation of time
std::wstring HLAfloat64Interval::toString() const
{
	std::wstringstream wss;
	wss << this->getInterval();
	return wss.str();
}

// Return the name of the Implementation, as needed by
// createFederationExecution.
std::wstring HLAfloat64Interval::implementationName() const
{
	return L"HLAfloat64Interval";
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Operators
LogicalTimeInterval& HLAfloat64Interval::operator= ( const LogicalTimeInterval& value )
	throw( InvalidLogicalTimeInterval )
{
	this->_impl->setValue( ((HLAfloat64Interval)value)._impl->getValue() );
	return *this;
}

LogicalTimeInterval& HLAfloat64Interval::operator+= ( const LogicalTimeInterval& addend )
	throw( IllegalTimeArithmetic, InvalidLogicalTimeInterval )
{
	Float64 other = ((HLAfloat64Interval)addend).getInterval();
	this->_impl->setValue( this->_impl->getValue() + other );
	return *this;
}

LogicalTimeInterval& HLAfloat64Interval::operator-= ( const LogicalTimeInterval& subtrahend )
	throw( IllegalTimeArithmetic, InvalidLogicalTimeInterval )
{
	Float64 other = ((HLAfloat64Interval)subtrahend).getInterval();
	this->_impl->setValue( this->_impl->getValue() - other );
	return *this;
}

bool HLAfloat64Interval::operator> ( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() > ((HLAfloat64Interval)value)._impl->getValue();
}

bool HLAfloat64Interval::operator< ( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() < ((HLAfloat64Interval)value)._impl->getValue();
}

bool HLAfloat64Interval::operator== ( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() == ((HLAfloat64Interval)value)._impl->getValue();
}

bool HLAfloat64Interval::operator>= ( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() >= ((HLAfloat64Interval)value)._impl->getValue();
}

bool HLAfloat64Interval::operator<= ( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() <= ((HLAfloat64Interval)value)._impl->getValue();
}

// Set self to the difference between two LogicalTimes
void HLAfloat64Interval::setToDifference( const LogicalTime& minuend,
                                          const LogicalTime& subtrahend )
	throw( IllegalTimeArithmetic, InvalidLogicalTime )
{
	Float64 one = ((HLAfloat64Time)minuend)._impl->getValue();
	Float64 two = ((HLAfloat64Time)subtrahend)._impl->getValue();
	this->_impl->setValue( one - two );
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
