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
#include "utils/Logger.h"
#include "types/handles/HandleFriends.h"

PORTICO1516E_NS_START

int Logger::globalLevel = Logger::LEVEL_UNSET;
std::ofstream Logger::globalRedirect;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS
//------------------------------------------------------------------------------------------

Logger::Logger( std::string name )
{
	this->name = std::string(name);
	this->level = Logger::LEVEL_ERROR;
	this->prefix = std::string("");
	
	this->stringBuffer = new char[MAX_MSG_LENGTH];
}

Logger::~Logger()
{
	delete this->stringBuffer;
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------

void Logger::setName( std::string name )
{
	this->name = std::string(name);
}

std::string Logger::getName()
{
	return this->name;
}

void Logger::setPrefix( std::string prefix )
{
	this->prefix = std::string(prefix);
}

std::string Logger::getPrefix()
{
	return this->prefix;
}

void Logger::setLevel( int level )
{
	if( level >= Logger::LEVEL_OFF && level <= Logger::LEVEL_NOISY )
	{
		this->level = level;
	}
}

int Logger::getLevel()
{
	return this->level;
}

/////////////////////////////
// private logging methods //
/////////////////////////////
void Logger::log( std::string level, std::string message )
{
	std::cout << level << "[" << this->name << "] " << prefix << message << std::endl;
}

void Logger::log( std::string level, std::string format, va_list args )
{
	// turn the args into a single string
	// http://www.cplusplus.com/reference/clibrary/cstdio/vsprintf.html
	// vsNprintf will check to make sure it doesn't write more than a certain amount
	vsnprintf( stringBuffer, MAX_MSG_LENGTH, format.c_str(), args );
	// on windows: _vsnprintf_s( buffer, MAX_MSG_LENGTH, format.c_str(), args );

	// print the message
	std::cout << level << " [" << this->name << "] " << prefix << stringBuffer << std::endl;
}

//
// This method will check to see if a message of the given level should
// be logged (given the current level of the logger) or not. If it should,
// true is returned, otherwise, false is returned.
//
// If the global level is set, the local level of the logger will not be
// consulted.
//
bool Logger::checkLevel( int messageLevel )
{
	if( Logger::globalLevel == Logger::LEVEL_UNSET )
	{
		if( messageLevel <= this->level )
			return true;
		else
			return false;
	}
	else
	{
		if( messageLevel <= Logger::globalLevel )
			return true;
		else
			return false;
	}
}

////////////////////////////
// public logging methods //
////////////////////////////
void Logger::fatal( std::string format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(Logger::LEVEL_FATAL) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	log( "FATAL", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::error( std::string format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(Logger::LEVEL_ERROR) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "ERROR", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::warn ( std::string format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(Logger::LEVEL_WARN) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "WARN ", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::info ( std::string format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(Logger::LEVEL_INFO) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "INFO ", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::debug( std::string format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(Logger::LEVEL_DEBUG) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "DEBUG", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::trace( std::string format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(Logger::LEVEL_TRACE) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "TRACE", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::noisy( std::string format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(Logger::LEVEL_NOISY) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "NOISY", format, args );
	// do the varargs cleanup
	va_end( args );
}

////////////////////////////////
// log level checking methods //
////////////////////////////////

bool Logger::isFatalEnabled()
{
	return checkLevel( Logger::LEVEL_FATAL );
}

bool Logger::isErrorEnabled()
{
	return checkLevel( Logger::LEVEL_ERROR );
}

bool Logger::isWarnEnabled()
{
	return checkLevel( Logger::LEVEL_WARN );
}

bool Logger::isInfoEnabled()
{
	return checkLevel( Logger::LEVEL_INFO );
}

bool Logger::isDebugEnabled()
{
	return checkLevel( Logger::LEVEL_DEBUG );
}

bool Logger::isTraceEnabled()
{
	return checkLevel( Logger::LEVEL_TRACE );
}

bool Logger::isNoisyEnabled()
{
	return checkLevel( Logger::LEVEL_NOISY );
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------
void Logger::setGlobalLevel( int level )
{
	if( level == Logger::LEVEL_UNSET || (level >= Logger::LEVEL_OFF && level <= Logger::LEVEL_NOISY) )
	{
		Logger::globalLevel = level;
	}
}

/*
 * valid values for the string are "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL" and "OFF"
 */
void Logger::setGlobalLevel( std::string level )
{
	if( level.compare("NOISY") == 0 )
		Logger::setGlobalLevel( Logger::LEVEL_NOISY );
	else if( level.compare("TRACE") == 0 )
		Logger::setGlobalLevel( Logger::LEVEL_TRACE );
	else if( level.compare("DEBUG") == 0 )
		Logger::setGlobalLevel( Logger::LEVEL_DEBUG );
	else if( level.compare("INFO") == 0 )
		Logger::setGlobalLevel( Logger::LEVEL_INFO );
	else if( level.compare("WARN") == 0 )
		Logger::setGlobalLevel( Logger::LEVEL_WARN );
	else if( level.compare("ERROR") == 0 )
		Logger::setGlobalLevel( Logger::LEVEL_ERROR );
	else if( level.compare("FATAL") == 0 )
		Logger::setGlobalLevel( Logger::LEVEL_FATAL );
	else if( level.compare("OFF") == 0 )
		Logger::setGlobalLevel( Logger::LEVEL_OFF );
}

int Logger::getGlobalLevel()
{
	return Logger::globalLevel;
}

void Logger::setRedirect( std::string file )
{
	if(globalRedirect.is_open())
	{
		globalRedirect.close();
	}
	globalRedirect.open(file.c_str());
	std::cout.rdbuf(globalRedirect.rdbuf());
}

////////////////////////////////////////////////////////////////////////////
//////////////////////// Set/Map Conversion Methods ////////////////////////
////////////////////////////////////////////////////////////////////////////
std::string Logger::toString( const AttributeHandleSet& theSet )
{
	// want to return "{1, 2, 3, 4, 5}"
	std::stringstream ss;
	ss << "{";

	// loop through all the handles, pumping them into the stream
	AttributeHandleSet::const_iterator iterator;
	for( iterator = theSet.begin(); iterator != theSet.end(); /*iterator++ we do below*/ )
	{
		ss << AttributeHandleFriend::toString( *iterator );
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != theSet.end() )
			ss << ", ";
	}
	
	ss << "}";
	return ss.str();
}

std::string Logger::toString( const DimensionHandleSet& theSet )
{
	// want to return "{1, 2, 3, 4, 5}"
	std::stringstream ss;
	ss << "{";

	// loop through all the handles, pumping them into the stream
	DimensionHandleSet::const_iterator iterator;
	for( iterator = theSet.begin(); iterator != theSet.end(); /*iterator++ we do below*/ )
	{
		ss << DimensionHandleFriend::toString( *iterator );
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != theSet.end() )
			ss << ", ";
	}
	
	ss << "}";
	return ss.str();
}

std::string Logger::toString( const FederateHandleSet& theSet )
{
	// want to return "{1, 2, 3, 4, 5}"
	std::stringstream ss;
	ss << "{";

	// loop through all the handles, pumping them into the stream
	FederateHandleSet::const_iterator iterator;
	for( iterator = theSet.begin(); iterator != theSet.end(); /*iterator++ we do below*/ )
	{
		ss << FederateHandleFriend::toString( *iterator );
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != theSet.end() )
			ss << ", ";
	}
	
	ss << "}";
	return ss.str();
}

std::string Logger::toString( const ParameterHandleSet& theSet )
{
	// want to return "{1, 2, 3, 4, 5}"
	std::stringstream ss;
	ss << "{";

	// loop through all the handles, pumping them into the stream
	ParameterHandleSet::const_iterator iterator;
	for( iterator = theSet.begin(); iterator != theSet.end(); /*iterator++ we do below*/ )
	{
		ss << ParameterHandleFriend::toString( *iterator );
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != theSet.end() )
			ss << ", ";
	}
	
	ss << "}";
	return ss.str();
}

std::string Logger::toString( const RegionHandleSet& theSet )
{
	// want to return "{1, 2, 3, 4, 5}"
	std::stringstream ss;
	ss << "{";

	// loop through all the handles, pumping them into the stream
	RegionHandleSet::const_iterator iterator;
	for( iterator = theSet.begin(); iterator != theSet.end(); /*iterator++ we do below*/ )
	{
		ss << RegionHandleFriend::toString( *iterator );
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != theSet.end() )
			ss << ", ";
	}
	
	ss << "}";
	return ss.str();
}

std::string Logger::toString( const AttributeHandleValueMap& theMap )
{
	// want to return "{1(4b), 2(8b), 3(1b)}"
	std::stringstream ss;
	ss << "{";

	// loop through all the handles, pumping them into the stream
	AttributeHandleValueMap::const_iterator iterator;
	for( iterator = theMap.begin(); iterator != theMap.end(); /*iterator++ we do below*/ )
	{
		ss << AttributeHandleFriend::toString( (*iterator).first );
		ss << "(";
		ss << (*iterator).second.size();
		ss << ")";
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != theMap.end() )
			ss << ", ";
	}
	
	ss << "}";
	return ss.str();
}

std::string Logger::toString( const ParameterHandleValueMap& theMap )
{
	// want to return "{1(4b), 2(8b), 3(1b)}"
	std::stringstream ss;
	ss << "{";

	// loop through all the handles, pumping them into the stream
	ParameterHandleValueMap::const_iterator iterator;
	for( iterator = theMap.begin(); iterator != theMap.end(); /*iterator++ we do below*/ )
	{
		ss << ParameterHandleFriend::toString( (*iterator).first );
		ss << "(";
		ss << (*iterator).second.size();
		ss << ")";
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != theMap.end() )
			ss << ", ";
	}
	
	ss << "}";
	return ss.str();
}

std::wstring Logger::toWString( std::vector<std::wstring> values )
{
	wstringstream wss;
	wss << L"{";
	vector<wstring>::const_iterator iterator;
	for( iterator = values.begin(); iterator != values.end(); /*iterator++ we do below*/ )
	{
		wss << (*iterator);
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != values.end() )
			wss << L", ";
	}
	
	wss << L"}";
	return wss.str();
}

std::wstring Logger::toWString( std::set<std::wstring> values )
{
	wstringstream wss;
	wss << L"{";
	set<wstring>::const_iterator iterator;
	for( iterator = values.begin(); iterator != values.end(); /*iterator++ we do below*/ )
	{
		wss << (*iterator);
		// check to see if the next is the last, don't print the
		// separator if it is
		if( (++iterator) != values.end() )
			wss << L", ";
	}
	
	wss << L"}";
	return wss.str();
}

PORTICO1516E_NS_END
