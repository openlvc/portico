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
#include "common.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAfloat64TimeFactory::HLAfloat64TimeFactory()
{
	
}

HLAfloat64TimeFactory::~HLAfloat64TimeFactory() throw()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a LogicalTime with a value of "initial"
std::auto_ptr<LogicalTime> HLAfloat64TimeFactory::makeInitial()
	throw( InternalError )
{
	return auto_ptr<LogicalTime>( new HLAfloat64Time() );
}

// Return a LogicalTime with a value of "final"
std::auto_ptr<LogicalTime> HLAfloat64TimeFactory::makeFinal()
	throw( InternalError )
{
	HLAfloat64Time *time = new HLAfloat64Time();
	time->setFinal();
	return auto_ptr<LogicalTime>( time );
}

// Return a LogicalTimeInterval with a value of "zero"
std::auto_ptr<LogicalTimeInterval> HLAfloat64TimeFactory::makeZero()
	throw( InternalError )
{
	HLAfloat64Interval *time = new HLAfloat64Interval();
	time->setInterval( 0.0 );
	return auto_ptr<LogicalTimeInterval>( time );
}

// Return a LogicalTimeInterval with a value of "epsilon"
std::auto_ptr<LogicalTimeInterval> HLAfloat64TimeFactory::makeEpsilon()
	throw( InternalError )
{
	HLAfloat64Interval *time = new HLAfloat64Interval();
	time->setEpsilon();
	return auto_ptr<LogicalTimeInterval>( time );
}

std::auto_ptr<HLAfloat64Time> HLAfloat64TimeFactory::makeLogicalTime( Float64 value )
	throw( InternalError )
{
	return auto_ptr<HLAfloat64Time>( new HLAfloat64Time(value) );
}

std::auto_ptr<HLAfloat64Interval>
HLAfloat64TimeFactory::makeLogicalTimeInterval( Float64 value )
	throw( InternalError )
{
	return auto_ptr<HLAfloat64Interval>( new HLAfloat64Interval(value) );
}

// LogicalTime decode from an encoded LogicalTime
std::auto_ptr<LogicalTime>
HLAfloat64TimeFactory::decodeLogicalTime( const VariableLengthData& encodedLogicalTime )
	throw( InternalError, CouldNotDecode )
{
	HLAfloat64Time *time = new HLAfloat64Time();
	time->decode( encodedLogicalTime );
	return auto_ptr<LogicalTime>( time );
}

// Alternate LogicalTime decode that reads directly from a buffer
std::auto_ptr<LogicalTime>
HLAfloat64TimeFactory::decodeLogicalTime( void* buffer, size_t bufferSize )
	throw( InternalError, CouldNotDecode )
{
	HLAfloat64Time *time = new HLAfloat64Time();
	time->decode( buffer, bufferSize );
	return auto_ptr<LogicalTime>( time );
}

// LogicalTimeInterval decode from an encoded LogicalTimeInterval
std::auto_ptr<LogicalTimeInterval>
HLAfloat64TimeFactory::decodeLogicalTimeInterval( const VariableLengthData& encodedValue )
	throw( InternalError, CouldNotDecode )
{
	HLAfloat64Interval *time = new HLAfloat64Interval();
	time->decode( encodedValue );
	return auto_ptr<LogicalTimeInterval>( time );
}

// Alternate LogicalTimeInterval decode that reads directly from a buffer
std::auto_ptr<LogicalTimeInterval>
HLAfloat64TimeFactory::decodeLogicalTimeInterval( void* buffer, size_t bufferSize )
	throw( InternalError, CouldNotDecode )
{
	HLAfloat64Interval *time = new HLAfloat64Interval();
	time->decode( buffer, bufferSize );
	return auto_ptr<LogicalTimeInterval>( time );
}

std::wstring HLAfloat64TimeFactory::getName() const
{
	return L"HLAfloat64TimeFactory";
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
