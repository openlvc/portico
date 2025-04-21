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
#pragma warning(disable:4244) // possible loss of data in string conversion - yup, we know

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
	std::wstring wideString;
	return wideString.assign( shortString.begin(), shortString.end() );
}

std::string StringUtils::toShortString( std::wstring wideString )
{
	std::string shortString;
	return shortString.assign( wideString.begin(), wideString.end() );
}

PORTICO1516E_NS_END
