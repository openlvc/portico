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
#include "rtiamb/PorticoRtiAmbassador.h" 
#include "RTI/portico/RTIambassadorEx.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
RTIambassadorFactory::RTIambassadorFactory()
{
}

RTIambassadorFactory::~RTIambassadorFactory() throw ()
{
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
std::auto_ptr<RTIambassador> RTIambassadorFactory::createRTIambassador() throw( RTIinternalError )
{
	return auto_ptr<RTIambassador>( new PORTICO1516E_NS::PorticoRtiAmbassador() );
}

std::auto_ptr<RTIambassadorEx> RTIambassadorFactory::createRTIambassadorEx() throw( RTIinternalError )
{
	return auto_ptr<RTIambassadorEx>( new PORTICO1516E_NS::PorticoRtiAmbassador() );
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------




// 10.35

IEEE1516E_NS_END
