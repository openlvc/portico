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
#pragma once

#include "common.h"

PORTICO1516E_NS_START

/**
 * A few helpful string utility methods.
 */
class StringUtils
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:
		static std::wstring toWideString( const std::string& narrowString );
		static std::string toNarrowString( const std::wstring& wideString );

		static std::wstring toLower( const std::wstring& string );
};

PORTICO1516E_NS_END
