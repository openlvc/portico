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
#include "types/time/HLAinteger64TimeImpl.h"
#include "types/time/HLAinteger64IntervalImpl.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAinteger64Interval::HLAinteger64Interval()
{
	this->_impl = new HLAinteger64IntervalImpl( 0.0 );
}

HLAinteger64Interval::HLAinteger64Interval( Integer64 value )
{
	this->_impl = new HLAinteger64IntervalImpl( value );
}

HLAinteger64Interval::HLAinteger64Interval( const LogicalTimeInterval& rhs )
{
	this->_impl = new HLAinteger64IntervalImpl( ((HLAinteger64Interval)rhs)._impl->getValue() );
}

HLAinteger64Interval::HLAinteger64Interval( const HLAinteger64Interval& rhs )
{
	this->_impl = new HLAinteger64IntervalImpl( rhs._impl->getValue() );
}

// Destructor
HLAinteger64Interval::~HLAinteger64Interval() throw()
{
	delete this->_impl;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
Integer64 HLAinteger64Interval::getInterval() const
{
	return this->_impl->getValue();
}

void HLAinteger64Interval::setInterval( Integer64 value )
{
	this->_impl->setValue( value );
}

HLAinteger64Interval& HLAinteger64Interval::operator=( const HLAinteger64Interval& value )
	throw( InvalidLogicalTimeInterval )
{
	this->_impl->setValue( value._impl->getValue() );
	return *this;
}

HLAinteger64Interval::operator Integer64() const
{
	return this->_impl->getValue();
}

////////////////////////////////////////////////////////////
//////////////// Inherited from LogicalTime //////////////// 
////////////////////////////////////////////////////////////
// Basic accessors/mutators
void HLAinteger64Interval::setZero()
{
	this->setInterval( 0 );
}

bool HLAinteger64Interval::isZero() const
{
	return this->getInterval() == 0;
}

void HLAinteger64Interval::setEpsilon()
{
	this->setInterval( 1 );
}

bool HLAinteger64Interval::isEpsilon() const
{
	return this->getInterval() == 1;
}

// Generates an encoded value that can be used to send
// LogicalTimeIntervals to other federates in updates or interactions
VariableLengthData HLAinteger64Interval::encode() const
{
	// TODO fill me out
	return VariableLengthData();
}

// Alternate encode for directly filling a buffer
// Return the length of encoded data
size_t HLAinteger64Interval::encode( void* buffer, size_t bufferSize ) const
	throw( CouldNotEncode )
{
	// TODO fill me out
	return 0;
}

// The length of the encoded data
size_t HLAinteger64Interval::encodedLength() const
{
	return 8;
}

// Decode encodedValue into self
void HLAinteger64Interval::decode( const VariableLengthData& encodedValue )
	throw( InternalError, CouldNotDecode )
{
	// TODO fill me out	
}

// Decode encodedValue into self
// Alternate decode that reads directly from a buffer
void HLAinteger64Interval::decode( void* buffer, size_t bufferSize )
	throw( InternalError, CouldNotDecode )
{
	// TODO fill me out
}

// Diagnostic string representation of time
std::wstring HLAinteger64Interval::toString() const
{
	std::wstringstream wss;
	wss << this->getInterval();
	return wss.str();
}

// Return the name of the Implementation, as needed by
// createFederationExecution.
std::wstring HLAinteger64Interval::implementationName() const
{
	return L"HLAinteger64Interval";
}

//------------------------------------------------------------------------------------------
//                                     OPERATOR OVERLOADS
//------------------------------------------------------------------------------------------
// Operators
LogicalTimeInterval& HLAinteger64Interval::operator= ( const LogicalTimeInterval& value )
	throw( InvalidLogicalTimeInterval )
{
	this->_impl->setValue( ((HLAinteger64Interval)value)._impl->getValue() );
	return *this;
}

LogicalTimeInterval& HLAinteger64Interval::operator+= ( const LogicalTimeInterval& addend )
	throw( IllegalTimeArithmetic, InvalidLogicalTimeInterval )
{
	Integer64 other = ((HLAinteger64Interval)addend).getInterval();
	this->_impl->setValue( this->_impl->getValue() + other );
	return *this;
}

LogicalTimeInterval& HLAinteger64Interval::operator-= ( const LogicalTimeInterval& subtrahend )
	throw( IllegalTimeArithmetic, InvalidLogicalTimeInterval )
{
	Integer64 other = ((HLAinteger64Interval)subtrahend).getInterval();
	this->_impl->setValue( this->_impl->getValue() - other );
	return *this;
}

bool HLAinteger64Interval::operator> ( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() > ((HLAinteger64Interval)value)._impl->getValue();
}

bool HLAinteger64Interval::operator< ( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() < ((HLAinteger64Interval)value)._impl->getValue();
}

bool HLAinteger64Interval::operator==( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() == ((HLAinteger64Interval)value)._impl->getValue();
}

bool HLAinteger64Interval::operator>=( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() >= ((HLAinteger64Interval)value)._impl->getValue();
}

bool HLAinteger64Interval::operator<=( const LogicalTimeInterval& value ) const
	throw( InvalidLogicalTimeInterval )
{
	return _impl->getValue() >= ((HLAinteger64Interval)value)._impl->getValue();
}

// Set self to the difference between two LogicalTimes
void HLAinteger64Interval::setToDifference( const LogicalTime& minuend,
                                            const LogicalTime& subtrahend )
	throw( IllegalTimeArithmetic, InvalidLogicalTime )
{
	Integer64 one = ((HLAinteger64Time)minuend)._impl->getValue();
	Integer64 two = ((HLAinteger64Time)subtrahend)._impl->getValue();
	this->_impl->setValue( one - two );
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END


