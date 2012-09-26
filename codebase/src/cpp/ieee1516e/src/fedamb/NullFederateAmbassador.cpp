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

/**
 * The NullFedereateAmbassador class is defined in the standard RTI/NullFedeateAmbassador.h
 * header file. Unlike every other class in the specification, it is defined purely within
 * the header file and does not require the RTI implementor to add any code (not even a
 * simple destructor implementation).
 *
 * However, if none of our RTI code brings in the header file, then the class, complete with
 * no-op implementation methods, never makes it into our DLL and thus isn't part of our
 * distribution. Thus, when client federates include the header file and go to use it, as
 * they are not defining the "BUILDING_RTI" symbol (given that they're not building and RTI)
 * the class is declared with dllimport, so the linker goes hunting for it. You can probably
 * now see the problem. It's not exported by the RTI library, so it won't be found. Let the
 * linker errors fly.
 *
 * To solve this, we have a simple, empty .cpp file that just brings in the header and thus
 * causes the declaration, with bodies, to be brought in and form part of this compilation
 * unit. We are defining BUILDING_RTI, which means the class will be marked as a dllexport,
 * and thus, our final RTI dll file will, funnily enough, export it so that client federates
 * can find it when they go to compile themselves.
 *
 * What a trial.
 */
#include "RTI/NullFederateAmbassador.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS
//------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------


IEEE1516E_NS_END

