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
#ifndef LOGGER_H_
#define LOGGER_H_

#include "common.h"

#define MAX_MSG_LENGTH 4096

PORTICO1516E_NS_START

/**
 * Creates a logger that logs to standard output. Each logger can be given a name (which
 * is printed in all log messages) and a level. If the level that a log message is provided
 * with is less than or equal to the logger level, the message will be printed. Otherwise
 * it is discarded.
 *
 * Example:
 *   Logger logger("mylogger");
 *   logger.setLevel( Logger::INFO );
 *   logger.warn( "This will be printed" );
 *   logger.info( "This will be printed" );
 *   logger.debug( "This will not be printed" );
 * 
 * NOTE: The methods of this class are NOT thread safe. Calling them at the same time from
 *       separate threads will cause issues.
 */
class Logger
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:
		static int globalLevel;

	public:
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
		static const int LEVEL_NOISY  = 8;  // undocumented ultra-verbose level
		static const int LEVEL_TRACE  = 7;
		static const int LEVEL_DEBUG  = 6;
		static const int LEVEL_INFO   = 5;  // the default level
		static const int LEVEL_WARN   = 4;
		static const int LEVEL_ERROR  = 3;
		static const int LEVEL_FATAL  = 2;
		static const int LEVEL_OFF    = 1;
		static const int LEVEL_UNSET  = -1; // the default global level

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		string name;
		int level;
		string prefix;
		
		// used to dump format strings into before printing them in log messages
		char *stringBuffer;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		Logger( std::string name );
		virtual ~Logger();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		void setName( std::string name );
		std::string getName();
		void setPrefix( std::string prefix ); // put on the front of each log message
		std::string getPrefix();
		
		// note that this value will be ignored if the global level is set
		void setLevel( int level );
		int  getLevel();

		// logging methods
		void fatal( std::string format, ... );
		void error( std::string format, ... );
		void warn ( std::string format, ... );
		void info ( std::string format, ... );
		void debug( std::string format, ... );
		void trace( std::string format, ... );
		void noisy( std::string format, ... );
		
		// level checking mehtods
		bool isFatalEnabled();
		bool isErrorEnabled();
		bool isWarnEnabled();
		bool isInfoEnabled();
		bool isDebugEnabled();
		bool isTraceEnabled();
		bool isNoisyEnabled();

	private:
		void log( std::string level, std::string message );
		void log( std::string level, std::string format, va_list args );
		
		// this method will return true if messages for the given
		// level should be printed, false otherwise
		bool checkLevel( int messageLevel );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
		static void setGlobalLevel( int level );
		static void setGlobalLevel( std::string level );
		static int  getGlobalLevel();

		/* 
		 * For the sets, this returns "{handle, handle, handle}"
		 * For the maps, this returns "{handle(Xb), handle(Xb), handle(Xb)}"
		 */
		static std::string toString( const AttributeHandleSet& theSet );
		static std::string toString( const DimensionHandleSet& theSet );
		static std::string toString( const FederateHandleSet& theSet );
		static std::string toString( const ParameterHandleSet& theSet );
		static std::string toString( const RegionHandleSet& theSet );
		static std::string toString( const AttributeHandleValueMap& theMap );
		static std::string toString( const ParameterHandleValueMap& theMap );

		static std::wstring toWString( std::vector<std::wstring> values );
		static std::wstring toWString( std::set<std::wstring> values );
};

PORTICO1516E_NS_END

#endif /*LOGGER_H_*/
