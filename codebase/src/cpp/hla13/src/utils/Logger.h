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
#ifndef LOGGER_H_
#define LOGGER_H_

#include "../common.h"

#define MAX_MSG_LENGTH 4096

//////////////////////////////////////////////////////////////////////////////////////////////
// firstly, some basic helper methods to ensuring converting sets to strings is easy enough //
// MEMORY MANAGEMENT NOTES:                                                                 //
//   For each of these methods, you are expected to clean up the char* memory               //
//   returned by the methods                                                                //
char*    setToString( const HLA::FederateHandleSet& theSet );                               //
char*    setToString( const HLA::AttributeHandleSet& theSet );                              //
char*    arrayToString( HLA::AttributeHandle array[], HLA::ULong size );                    //
char*    arrayToString( HLA::Region *theRegions[], HLA::ULong size );                       //
char*    mapToString( const HLA::AttributeHandleValuePairSet& theMap );                     //
char*    mapToString( const HLA::ParameterHandleValuePairSet& theMap );                     //
//////////////////////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////
// The various logger levels. When the logger is set to a      //
// specific level, any incoming log request must be associated //
// with a level that is equal to or lower than the value of    //
// that level.                                                 //
//                                                             //
// For example, if the level of the logger is ERROR(3), and an //
// incoming message is logged at the INFO(5) level, it will be //
// discarded as 5 is not <= 3                                  //
/////////////////////////////////////////////////////////////////
#define LOG_NOISY  8 // undocumented ultra-verbose level
#define LOG_TRACE  7
#define LOG_DEBUG  6
#define LOG_INFO   5 // the default level
#define LOG_WARN   4
#define LOG_ERROR  3
#define LOG_FATAL  2
#define LOG_OFF    1
#define LOG_UNSET -1 // the default global level

PORTICO13_NS_START

class Logger
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:
		static int globalLevel;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		string name;
		int level;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		Logger( string name );
		virtual ~Logger();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setName( string name );
		string getName();
		
		// note that this value will be ignored if the global level is set
		void setLevel( int level );
		int  getLevel();

		// logging methods
		void fatal( const char *format, ... );
		void error( const char *format, ... );
		void warn ( const char *format, ... );
		void info ( const char *format, ... );
		void debug( const char *format, ... );
		void trace( const char *format, ... );
		void noisy( const char *format, ... );
		
		// level checking mehtods
		bool isFatalEnabled();
		bool isErrorEnabled();
		bool isWarnEnabled();
		bool isInfoEnabled();
		bool isDebugEnabled();
		bool isTraceEnabled();
		bool isNoisyEnabled();

	private:
		void log( const char *level, const char *message );
		void log( const char *level, const char* format, va_list args );
		
		// this method will return true if messages for the given
		// level should be printed, false otherwise
		bool checkLevel( int messageLevel );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
		static void setGlobalLevel( int level );
		static void setGlobalLevel( const char *level );
		static int  getGlobalLevel();

};

PORTICO13_NS_END

#endif /*LOGGER_H_*/
