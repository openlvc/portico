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
#ifndef STRINGHELPERS_H_
#define STRINGHELPERS_H_

// define a couple of quick helper methdos for string conversion
std::wstring toWideString( std::string shortString )
{
	std::wstring wideString;
	return wideString.assign( shortString.begin(), shortString.end() );
}

std::string toShortString( std::wstring wideString )
{
	std::string shortString;
	return shortString.assign( wideString.begin(), wideString.end() );
}

#endif /* STRINGHELPERS_H_ */
