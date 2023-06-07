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

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------
/*
 * Provides a factory for the standard logical time types HLAfloat64Time
 * and HLAinteger64Time. The RTI reference time library's LogicalTimeFactoryFactory
 * should just forward requests to here.
 */
std::auto_ptr<LogicalTimeFactory>
HLAlogicalTimeFactoryFactory::makeLogicalTimeFactory( const std::wstring& implementationName )
{
	if( implementationName.compare(L"HLAfloat64TimeFactory") == 0 )
	{
		return auto_ptr<LogicalTimeFactory>( new HLAfloat64TimeFactory() );
	}
	else if( implementationName.compare(L"HLAinteger64TimeFactory") == 0 )
	{
		return auto_ptr<LogicalTimeFactory>( new HLAinteger64TimeFactory() );
	}
	else if( implementationName.compare(L"HLAfloat64Time") == 0 )
	{
		return auto_ptr<LogicalTimeFactory>( new HLAfloat64TimeFactory() );
	}
	else if( implementationName.compare(L"HLAinteger64Time") == 0 )
	{
		return auto_ptr<LogicalTimeFactory>( new HLAinteger64TimeFactory() );
	}
	else
	{
		wstringstream wss;
		wss << "Unknown time implementation type [" << implementationName <<
			"]: Must be HLAfloat64TimeFactory or HLAinteger64TimeFactory";

		throw InternalError( wss.str() );
	}
}

IEEE1516E_NS_END
