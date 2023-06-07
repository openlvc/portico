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
 * The name is used to choose among several LogicalTimeFactories that might
 * be present in the fedtime library.  Each federation chooses its
 * implementation by passing the appropriate name to createFederationExecution.
 * If the supplied name is the empty string, a default LogicalTimeFactory is
 * returned.  If the supplied implementation name does not match any name
 * supported by the library, then a NULL pointer is returned.
 */
std::auto_ptr<LogicalTimeFactory>
LogicalTimeFactoryFactory::makeLogicalTimeFactory( const std::wstring& implementationName )
{
	return HLAlogicalTimeFactoryFactory::makeLogicalTimeFactory( implementationName );
}

IEEE1516E_NS_END
