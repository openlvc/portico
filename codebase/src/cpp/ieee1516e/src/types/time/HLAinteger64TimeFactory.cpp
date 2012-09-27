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
#include "types/time/TimeImplementations.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
HLAinteger64TimeFactory::HLAinteger64TimeFactory()
{
	
}

HLAinteger64TimeFactory::~HLAinteger64TimeFactory() throw()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return a LogicalTime with a value of "initial"
std::auto_ptr<LogicalTime> HLAinteger64TimeFactory::makeInitial()
	throw( InternalError )
{
	return auto_ptr<LogicalTime>( new HLAinteger64Time() );
}

// Return a LogicalTime with a value of "final"
std::auto_ptr<LogicalTime> HLAinteger64TimeFactory::makeFinal()
	throw( InternalError )
{
	HLAinteger64Time *time = new HLAinteger64Time();
	time->setFinal();
	return auto_ptr<LogicalTime>( time );
}

// Return a LogicalTimeInterval with a value of "zero"
std::auto_ptr<LogicalTimeInterval> HLAinteger64TimeFactory::makeZero()
	throw( InternalError )
{
	return auto_ptr<LogicalTimeInterval>( new HLAinteger64Interval() );
}

// Return a LogicalTimeInterval with a value of "epsilon"
std::auto_ptr<LogicalTimeInterval> HLAinteger64TimeFactory::makeEpsilon()
	throw( InternalError )
{
	HLAinteger64Interval *time = new HLAinteger64Interval();
	time->setEpsilon();
	return auto_ptr<LogicalTimeInterval>( time );
}

std::auto_ptr<HLAinteger64Time> HLAinteger64TimeFactory::makeLogicalTime( Integer64 value )
	throw( InternalError )
{
	return auto_ptr<HLAinteger64Time>( new HLAinteger64Time(value) );
}

std::auto_ptr<HLAinteger64Interval>
HLAinteger64TimeFactory::makeLogicalTimeInterval( Integer64 value )
	throw( InternalError )
{
	return auto_ptr<HLAinteger64Interval>( new HLAinteger64Interval(value) );
}

// LogicalTime decode from an encoded LogicalTime
std::auto_ptr<LogicalTime>
HLAinteger64TimeFactory::decodeLogicalTime( const VariableLengthData& encodedLogicalTime )
	throw( InternalError, CouldNotDecode )
{
	HLAinteger64Time *time = new HLAinteger64Time();
	time->decode( encodedLogicalTime );
	return auto_ptr<LogicalTime>( time );
}

// Alternate LogicalTime decode that reads directly from a buffer
std::auto_ptr<LogicalTime>
HLAinteger64TimeFactory::decodeLogicalTime( void* buffer, size_t bufferSize )
	throw( InternalError, CouldNotDecode )
{
	HLAinteger64Time *time = new HLAinteger64Time();
	time->decode( buffer, bufferSize );
	return auto_ptr<LogicalTime>( time );
}

// LogicalTimeInterval decode from an encoded LogicalTimeInterval
std::auto_ptr<LogicalTimeInterval>
HLAinteger64TimeFactory::decodeLogicalTimeInterval( const VariableLengthData& encodedValue )
	throw( InternalError, CouldNotDecode )
{
	HLAinteger64Interval *time = new HLAinteger64Interval();
	time->decode( encodedValue );
	return auto_ptr<LogicalTimeInterval>( time );
}

// Alternate LogicalTimeInterval decode that reads directly from a buffer
std::auto_ptr<LogicalTimeInterval>
HLAinteger64TimeFactory::decodeLogicalTimeInterval( void* buffer, size_t bufferSize )
	throw( InternalError, CouldNotDecode )
{
	HLAinteger64Interval *time = new HLAinteger64Interval();
	time->decode( buffer, bufferSize );
	return auto_ptr<LogicalTimeInterval>( time );
}

std::wstring HLAinteger64TimeFactory::getName() const
{
	return L"HLAinteger64TimeFactory";
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
