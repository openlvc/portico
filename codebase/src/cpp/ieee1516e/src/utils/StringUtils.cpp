/*
 *   Copyright 2018 The Portico Project
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

#include <algorithm>
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
std::wstring StringUtils::toWideString( const std::string& narrowString )
{
	std::wstring wideString;
	return wideString.assign( narrowString.begin(), narrowString.end() );
}

std::string StringUtils::toNarrowString( const std::wstring& wideString )
{
	std::string narrowString;
	return narrowString.assign( wideString.begin(), wideString.end() );
}

std::wstring StringUtils::toLower( const std::wstring& string )
{
	std::wstring copy = string;
	std::transform( copy.begin(), copy.end(), copy.begin(), ::tolower );

	return copy;
}

PORTICO1516E_NS_END
