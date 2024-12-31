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
#include "utils/StringUtils.h"
#include <string>

PORTICO1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------
std::wstring StringUtils::toWideString( std::string shortString )
{
#ifdef OS_WINDOWS
	if( shortString.empty() )
		return L"";

	const auto newSize = MultiByteToWideChar( CP_UTF8, 0, shortString.data(), (int)shortString.size(), nullptr, 0 );
	if( newSize <= 0 )
		throw std::runtime_error( "MultiByteToWideChar() failed: " + std::to_string(newSize) );

	std::wstring wideString( newSize, 0 );
	MultiByteToWideChar( CP_UTF8, 0, shortString.data(), (int)shortString.size(), &wideString.at(0), newSize );
	return wideString;

#else
	// Original, failed on Windows
	std::wstring wideString;
	return wideString.assign( shortString.begin(), shortString.end() );

#endif
}

std::string StringUtils::toShortString( std::wstring wideString )
{
#ifdef OS_WINDOWS
	if( wideString.empty() )
		return "";

	const auto newSize = WideCharToMultiByte( CP_UTF8, 0, wideString.data(), (int)wideString.size(), nullptr, 0, nullptr, nullptr );
	if( newSize <= 0 )
		throw std::runtime_error( "WideCharToMultiByte() failed: " + std::to_string(newSize) );

	std::string shortString( newSize, 0 );
	WideCharToMultiByte( CP_UTF8, 0, wideString.data(), (int)wideString.size(), &shortString.at(0), newSize, nullptr, nullptr);
	return shortString;

#else
	// Original, failed on Windows
	std::string shortString;
	return shortString.assign( wideString.begin(), wideString.end() );
#endif
}

PORTICO1516E_NS_END
