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
#include "utils/StringUtils.h"

IEEE1516E_NS_START

////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////// Global RTI Information Methods //////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
std::wstring rtiName()
{
	return L"Portico";
}

std::wstring rtiVersion()
{
	stringstream ss;
	ss << STRING_FROM_MACRO(PORTICO_VERSION);
	ss << " (build ";
	ss << STRING_FROM_MACRO(PORTICO_BUILD_NUMBER);
	ss << ")";
	return portico1516e::StringUtils::toWideString( ss.str() );
}

IEEE1516E_NS_END
