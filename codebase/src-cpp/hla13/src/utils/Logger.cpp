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
#include "Logger.h"

PORTICO13_NS_START

int Logger::globalLevel = LOG_UNSET;

//----------------------------------------------------------
//                      CONSTRUCTORS
//----------------------------------------------------------
Logger::Logger( string name )
{
	this->name = string(name);
	this->level = LOG_ERROR;
}

Logger::~Logger()
{
}

//----------------------------------------------------------
//                    INSTANCE METHODS
//----------------------------------------------------------

void Logger::setName( string name )
{
	this->name = std::string(name);
}

string Logger::getName()
{
	return this->name;
}

void Logger::setLevel( int level )
{
	if( level >= LOG_OFF && level <= LOG_NOISY )
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
void Logger::log( const char *level, const char *message )
{
	// print the message
	//printf( "%s [%s] %s\n", level, this->name, message );
	cout << level << " [" << this->name << "] " << message << endl;
}

void Logger::log( const char *level, const char *format, va_list args )
{
	// turn the args into a single string
	// http://www.cplusplus.com/reference/clibrary/cstdio/vsprintf.html
	char buffer[MAX_MSG_LENGTH];
	vsprintf( buffer, format, args );

	// print the message
	//printf( "%s [%s] %s\n", level, this->name, buffer );
	cout << level << " [" << this->name << "] " << buffer << endl;
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
	if( Logger::globalLevel == LOG_UNSET )
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
void Logger::fatal( const char *format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(LOG_FATAL) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	log( "FATAL", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::error( const char *format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(LOG_ERROR) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "ERROR", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::warn ( const char *format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(LOG_WARN) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "WARN ", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::info ( const char *format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(LOG_INFO) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "INFO ", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::debug( const char *format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(LOG_DEBUG) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "DEBUG", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::trace( const char *format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(LOG_TRACE) )
		return;

	// start the var-arg stuff
	va_list args;
	va_start( args, format );
	// print the message
	log( "TRACE", format, args );
	// do the varargs cleanup
	va_end( args );
}

void Logger::noisy( const char *format, ... )
{
	// if the requested level is GREATER than the
	// logger level (the threshold), don't print it
	if( !checkLevel(LOG_NOISY) )
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
	return checkLevel( LOG_FATAL );
}

bool Logger::isErrorEnabled()
{
	return checkLevel( LOG_ERROR );
}

bool Logger::isWarnEnabled()
{
	return checkLevel( LOG_WARN );
}

bool Logger::isInfoEnabled()
{
	return checkLevel( LOG_INFO );
}

bool Logger::isDebugEnabled()
{
	return checkLevel( LOG_DEBUG );
}

bool Logger::isTraceEnabled()
{
	return checkLevel( LOG_TRACE );
}

bool Logger::isNoisyEnabled()
{
	return checkLevel( LOG_NOISY );
}

//----------------------------------------------------------
//                     STATIC METHODS
//----------------------------------------------------------
void Logger::setGlobalLevel( int level )
{
	if( level == LOG_UNSET || (level >= LOG_OFF && level <= LOG_NOISY) )
	{
		Logger::globalLevel = level;
	}
}

/*
 * valid values for the string are "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL" and "OFF"
 */
void Logger::setGlobalLevel( const char *level )
{
	if( strcmp(level,"NOISY") == 0 )
		Logger::setGlobalLevel( LOG_NOISY );
	else if( strcmp(level,"TRACE") == 0 )
		Logger::setGlobalLevel( LOG_TRACE );
	else if( strcmp(level,"DEBUG") == 0 )
		Logger::setGlobalLevel( LOG_DEBUG );
	else if( strcmp(level,"INFO") == 0 )
		Logger::setGlobalLevel( LOG_INFO );
	else if( strcmp(level,"WARN") == 0 )
		Logger::setGlobalLevel( LOG_WARN );
	else if( strcmp(level,"ERROR") == 0 )
		Logger::setGlobalLevel( LOG_ERROR );
	else if( strcmp(level,"FATAL") == 0 )
		Logger::setGlobalLevel( LOG_FATAL );
	else if( strcmp(level,"OFF") == 0 )
		Logger::setGlobalLevel( LOG_OFF );
}


int Logger::getGlobalLevel()
{
	return Logger::globalLevel;
}

PORTICO13_NS_END

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////// Helper Methods //////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
/*
 * Convert a FederateHandleSet to a string for printing.
 * 
 * MEMORY MANAGEMENT: You will need to delete the returned memory when finished with it.
 */
char* setToString( const HLA::FederateHandleSet& theSet )
{
	// create the buffer to store the handle set information in
	int offset = 0;
    char* buffer = new char[1024];
	offset += sprintf( (buffer+offset), "{" );

	// put the handle data in there
	for( HLA::ULong i = 0; i < theSet.size(); i++ )
	{
		if( (i+1) == theSet.size() )
		{
			// this is the last element
			offset += sprintf( (buffer+offset), "%u", (unsigned int)theSet.getHandle(i) );
		}
		else
		{
			// not the last element, add the ','
			offset += sprintf( (buffer+offset), "%u, ", (unsigned int)theSet.getHandle(i) );
		}
	}
	
	// finish off the string and return it
	sprintf( (buffer+offset), "}" );
	return buffer;
}

/*
 * Convert an AttributeHandleSet to a string for printing.
 * 
 * MEMORY MANAGEMENT: You will need to delete the returned memory when finished with it.
 */
char* setToString( const HLA::AttributeHandleSet& theSet )
{
	// create the buffer to store the handle set information in
	int offset = 0;
    char* buffer = new char[1024];
	offset += sprintf( (buffer+offset), "{" );

	// put the handle data in there
	for( HLA::ULong i = 0; i < theSet.size(); i++ )
	{
		if( (i+1) == theSet.size() )
		{
			// this is the last element
			offset += sprintf( (buffer+offset), "%u", (unsigned int)theSet.getHandle(i) );
		}
		else
		{
			// not the last element, add the ','
			offset += sprintf( (buffer+offset), "%u, ", (unsigned int)theSet.getHandle(i) );
		}
	}
	
	// finish off the string and return it
	sprintf( (buffer+offset), "}" );
	return buffer;
}

/*
 * Convert an AttributeHandle[] to a string for printing.
 * 
 * MEMORY MANAGEMENT: You will need to delete the returned memory when finished with it.
 */
char* arrayToString( HLA::AttributeHandle array[], HLA::ULong size )
{
	// create the buffer to store the handle set information in
	int offset = 0;
    char* buffer = new char[1024];
	offset += sprintf( (buffer+offset), "{" );

	// put the handle data in there
	for( HLA::ULong i = 0; i < size; i++ )
	{
		if( (i+1) == size )
		{
			// this is the last element
			offset += sprintf( (buffer+offset), "%lo", array[i] );
		}
		else
		{
			// not the last element, add the ','
			offset += sprintf( (buffer+offset), "%lo, ", array[i] );
		}
	}
	
	// finish off the string and return it
	sprintf( (buffer+offset), "}" );
	return buffer;
}

/*
 * Convert an RTI::Region*[] to a string for printing.
 * 
 * MEMORY MANAGEMENT: You will need to delete the returned memory when finished with it.
 */
char* arrayToString( HLA::Region *array[], HLA::ULong size )
{
	// create the buffer to store the region information in
	int offset = 0;
    char* buffer = new char[1024];
	offset += sprintf( (buffer+offset), "{" );

	// put the handle data in there
	for( HLA::ULong i = 0; i < size; i++ )
	{
		HLA::Region *tempRegion = array[i];
		if( (i+1) == size )
		{
			// this is the last element
			offset += sprintf( (buffer+offset), "%p", tempRegion );
		}
		else
		{
			// not the last element, add the ','
			offset += sprintf( (buffer+offset), "%p, ", tempRegion );
		}
	}
	
	// finish off the string and return it
	sprintf( (buffer+offset), "}" );
	return buffer;
}

/*
 * Convert an AttributeHandleValuePairSet to a string for printing.
 * 
 * MEMORY MANAGEMENT: You will need to delete the returned memory when finished with it.
 */
char* mapToString( const HLA::AttributeHandleValuePairSet& theMap )
{
	// create the buffer to store the handle set information in
	int offset = 0;
    char* buffer = new char[1024];
	offset += sprintf( (buffer+offset), "{" );

	// put the handle data in there
	for( HLA::ULong i = 0; i < theMap.size(); i++ )
	{
		if( (i+1) == theMap.size() )
		{
			// this is the last element
			offset += sprintf( (buffer+offset), "%u(%ub)",
			                   (unsigned int)theMap.getHandle(i),
			                   (unsigned int)theMap.getValueLength(i) );
		}
		else
		{
			// not the last element, add the ','
			offset += sprintf( (buffer+offset), "%u(%ub), ",
			                   (unsigned int)theMap.getHandle(i),
			                   (unsigned int)theMap.getValueLength(i) );
		}
	}
	
	// finish off the string and return it
	sprintf( (buffer+offset), "}" );
	return buffer;
}

/*
 * Convert an ParameterHandleValuePairSet to a string for printing.
 * 
 * MEMORY MANAGEMENT: You will need to delete the returned memory when finished with it.
 */
char* mapToString( const HLA::ParameterHandleValuePairSet& theMap )
{
	// create the buffer to store the handle set information in
	int offset = 0;
    char* buffer = new char[1024];
	offset += sprintf( (buffer+offset), "{" );

	// put the handle data in there
	for( HLA::ULong i = 0; i < theMap.size(); i++ )
	{
		if( (i+1) == theMap.size() )
		{
			// this is the last element
			offset += sprintf( (buffer+offset), "%u(%ub)",
			                   (unsigned int)theMap.getHandle(i),
			                   (unsigned int)theMap.getValueLength(i) );
		}
		else
		{
			// not the last element, add the ','
			offset += sprintf( (buffer+offset), "%u(%ub), ",
			                   (unsigned int)theMap.getHandle(i),
			                   (unsigned int)theMap.getValueLength(i) );
		}
	}
	
	// finish off the string and return it
	sprintf( (buffer+offset), "}" );
	return buffer;
}


