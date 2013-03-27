/*
 *   Copyright 2009 The Portico Project
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

////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// DLC 1.3 Global Stuff ///////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
#ifdef BUILDING_DLC
// Vendor-specific name and version of the RTI implementation
const char* rti13::RTIname()
{
	return "Portico";
}

// identical to MOM attributes of same name
const char* rti13::RTIversion()
{
	stringstream ss;
	ss << STRING_FROM_MACRO(PORTICO_VERSION);
	ss << " (build ";
	ss << STRING_FROM_MACRO(PORTICO_BUILD_NUMBER);
	ss << ")";
	ss.str();
}

#endif

////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////// NG6 Global Stuff /////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////
//#ifndef BUILDING_DLC // only include this if we're building the DLC interface

RTI_STD::ostream RTI_EXPORT &
operator << (RTI_STD::ostream &os, RTI::Exception *e)
{
	os << e->_name << " (" << e->_reason << ")";
	return os;
}

RTI_STD::ostream RTI_EXPORT &
operator << (RTI_STD::ostream &os, RTI::Exception const &e)
{
	os << e._name << " (" << e._reason << ")";
	return os;
}

//-----------------------------------------------------------------
// Implementation friends
//-----------------------------------------------------------------
RTI_STD::ostream RTI_EXPORT & operator<< (RTI_STD::ostream& os, const RTI::FedTime& newTime)
{
	// Convert to a pointer, as the compiler is complaining about something not being const
	RTI::FedTime* timePointer = (RTI::FedTime*)&newTime;

	// Can't put the time as a double directly on the OStream here as it seems to require a Locale
	// We fiddled around with locales but found that they ended up just swallowing all the output

	// as a result, we'll just convert the time to a string, and put that on the ostream
	char* asString = new char[timePointer->getPrintableLength() + 1]();
	timePointer->getPrintableString(asString);

	os << asString;

	delete [] asString;
	return os;
}

RTIfedTime operator+ (const RTI::Double& doubleTime, const RTI::FedTime& fedTime)
{
	// no bad_cast exceptions thrown here, see << operator for more details
	RTI::Double fedTimeAsDouble = 0;
	RTIfedTime newImpl = dynamic_cast<const RTIfedTime&>(fedTime);
	fedTimeAsDouble = newImpl.getTime();
	return RTIfedTime( doubleTime + fedTimeAsDouble );
}

RTIfedTime operator- (const RTI::Double& doubleTime, const RTI::FedTime& fedTime)
{
	// no bad_cast exceptions thrown here, see << operator for more details
	RTI::Double fedTimeAsDouble = 0;
	RTIfedTime newImpl = dynamic_cast<const RTIfedTime&>(fedTime);
	fedTimeAsDouble = newImpl.getTime();
	return RTIfedTime( doubleTime - fedTimeAsDouble );
}

RTIfedTime operator* (const RTI::Double& doubleTime, const RTI::FedTime& fedTime)
{
	// no bad_cast exceptions thrown here, see << operator for more details
	RTI::Double fedTimeAsDouble = 0;
	RTIfedTime newImpl = dynamic_cast<const RTIfedTime&>(fedTime);
	fedTimeAsDouble = newImpl.getTime();
	return RTIfedTime( doubleTime * fedTimeAsDouble );
}

RTIfedTime operator/ (const RTI::Double& doubleTime, const RTI::FedTime& fedTime)
{
	// no bad_cast exceptions thrown here, see << operator for more details
	RTI::Double fedTimeAsDouble = 0;
	RTIfedTime newImpl = dynamic_cast<const RTIfedTime&>(fedTime);
	fedTimeAsDouble = newImpl.getTime();
	return RTIfedTime( doubleTime / fedTimeAsDouble );
}

//#endif

