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

#include <float.h>
#include <typeinfo>
#include <stdio.h>
#include <math.h>

union TimeRepresentation
{
	HLA::Double asDouble;
    char asBytes[8];
};

//-----------------------------------------------------------------
// Constructors and Destructors
//-----------------------------------------------------------------
RTIfedTime::RTIfedTime()
{
	this->_fedTime = 0.0f;
	this->_zero = 0.0f;
	this->_epsilon = 0.000000001f;
	this->_positiveInfinity = DBL_MAX;

}

RTIfedTime::RTIfedTime(const HLA::Double& newTime)
{
	this->_fedTime = newTime;
	this->_zero = 0.0f;
	this->_epsilon = 0.000000001f;
	this->_positiveInfinity = DBL_MAX;
}

RTIfedTime::RTIfedTime(const HLA::FedTime& newTime)
{
	// Had a check in the DMSO version of RTIfedTime and it appears they don't even try to catch
	// bad cast exceptions. So if a bad cast exception does occur here, it's just going to get
	// propogated
	RTIfedTime newImpl = dynamic_cast<const RTIfedTime&>(newTime);
	this->_fedTime = newImpl._fedTime;
	this->_zero = newImpl._zero;
	this->_epsilon = newImpl._epsilon;
	this->_positiveInfinity = newImpl._positiveInfinity;
}

RTIfedTime::RTIfedTime(const RTIfedTime& newTime) : FedTime()
{
	this->_fedTime = newTime._fedTime;
	this->_zero = newTime._zero;
	this->_epsilon = newTime._epsilon;
	this->_positiveInfinity = newTime._positiveInfinity;
}

RTIfedTime::~RTIfedTime()
{
}

//-----------------------------------------------------------------
// Overloaded functions from HLA::FedTime
//-----------------------------------------------------------------
void RTIfedTime::setZero()
{
	this->_fedTime = this->_zero;
}

HLA::Boolean RTIfedTime::isZero()
{
	// Is _fedTime == to zero? Test for equality within the threshold of epsilon
	if ( fabs(this->_fedTime - this->_zero) < this->_epsilon )
	{
		return HLA::RTI_TRUE;
	}
	else
	{
		return HLA::RTI_FALSE;
	}
}

void RTIfedTime::setEpsilon()
{
	this->_fedTime = this->_epsilon;
}

void RTIfedTime::setPositiveInfinity()
{
	this->_fedTime = DBL_MAX;
}

HLA::Boolean RTIfedTime::isPositiveInfinity()
{
	if (this->_fedTime == DBL_MAX)
	{
		return HLA::RTI_TRUE;
	}
	else
	{
		return HLA::RTI_FALSE;
	}
}

int RTIfedTime::encodedLength() const
{
	return sizeof(HLA::Double);
}

void RTIfedTime::encode(char* buffer) const
{
	TimeRepresentation timeUnion;
	timeUnion.asDouble = this->_fedTime;
	memcpy(buffer, timeUnion.asBytes, sizeof(HLA::Double));
}

int RTIfedTime::getPrintableLength() const
{
	char buffer[1024];
	sprintf(buffer, "%f", this->_fedTime);
	int length = strlen(buffer) + 1;

	return length;
}

#ifdef BUILDING_DLC
void RTIfedTime::getPrintableString(char* buffer) const
{
	sprintf(buffer, "%f", this->_fedTime);
}
#else
void RTIfedTime::getPrintableString(char* buffer)
{
	sprintf(buffer, "%f", this->_fedTime);
}
#endif

//-----------------------------------------------------------------
// Overloaded operators from HLA::FedTime
//-----------------------------------------------------------------
HLA::FedTime& RTIfedTime::operator+= ( const HLA::FedTime& newTime ) throw( HLA::InvalidFederationTime )
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	this->_fedTime += newImpl.getTime();
	return *this;
}

HLA::FedTime & RTIfedTime::operator-= ( const HLA::FedTime& newTime ) throw( HLA::InvalidFederationTime )
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	this->_fedTime -= newImpl.getTime();
	return *this;
}

HLA::Boolean RTIfedTime::operator<= ( const HLA::FedTime& newTime ) const throw( HLA::InvalidFederationTime )
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	if( !(this->_fedTime > newImpl.getTime()) )
		return HLA::RTI_TRUE;
	else
		return HLA::RTI_FALSE;
}

HLA::Boolean RTIfedTime::operator< ( const HLA::FedTime& newTime ) const throw( HLA::InvalidFederationTime )
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	if( this->_fedTime < newImpl.getTime() )
		return HLA::RTI_TRUE;
	else
		return HLA::RTI_FALSE;
}

HLA::Boolean RTIfedTime::operator>= ( const HLA::FedTime& newTime ) const throw( HLA::InvalidFederationTime )
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	if( !(this->_fedTime < newImpl.getTime()) )
		return HLA::RTI_TRUE;
	else
		return HLA::RTI_FALSE;
}

HLA::Boolean RTIfedTime::operator> ( const HLA::FedTime& newTime ) const throw( HLA::InvalidFederationTime )
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	if( this->_fedTime > newImpl.getTime() )
		return HLA::RTI_TRUE;
	else
		return HLA::RTI_FALSE;
}

HLA::Boolean RTIfedTime::operator== ( const HLA::FedTime& newTime ) const throw( HLA::InvalidFederationTime )
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	// Test for equality within a threshold of epsilon
	if( fabs(this->_fedTime - newImpl.getTime()) < this->_epsilon )
		return HLA::RTI_TRUE;
	else
		return HLA::RTI_FALSE;
}

HLA::FedTime& RTIfedTime::operator= ( const HLA::FedTime& rhs ) throw( HLA::InvalidFederationTime )
{
	// check for assignment to self
	if( this == &rhs )
	{
		return *this;
	}

	this->_fedTime = dynamic_cast<const RTIfedTime&>(rhs)._fedTime;
	return *this;
}

//-----------------------------------------------------------------
// Implementation functions
//-----------------------------------------------------------------
HLA::Double RTIfedTime::getTime() const
{
	return this->_fedTime;
}

//-----------------------------------------------------------------
// Implementation operators
//-----------------------------------------------------------------
HLA::Boolean RTIfedTime::operator== (const HLA::Double& theTime) const
	throw (HLA::InvalidFederationTime)
{
	// Test for equality within a threshold of epsilon
	if ( fabs(this->_fedTime - theTime) < this->_epsilon )
	{
		return HLA::RTI_TRUE;
	}
	else
	{
		return HLA::RTI_FALSE;
	}
}

HLA::Boolean RTIfedTime::operator!= (const HLA::FedTime& newTime) const
	throw (HLA::InvalidFederationTime)
{
	RTIfedTime newImpl;

	try
	{
		newImpl = dynamic_cast<const RTIfedTime&>(newTime);
	}
	catch( std::bad_cast )
	{
		throw HLA::InvalidFederationTime("!= operator called with non RTIfedTime class");
	}

	// Test for non-equality within a threshold of epsilon
	if ( fabs( this->_fedTime - newImpl._fedTime) > this->_epsilon )
	{
		return HLA::RTI_TRUE;
	}
	else
	{
		return HLA::RTI_FALSE;
	}
}

HLA::Boolean RTIfedTime::operator!= (const HLA::Double& newTime) const
	throw (HLA::InvalidFederationTime)
{
	// Test for non-equality within a threshold of epsilon
	if ( fabs(this->_fedTime - newTime) > this->_epsilon )
	{
		return HLA::RTI_TRUE;
	}
	else
	{
		return HLA::RTI_FALSE;
	}
}

HLA::FedTime& RTIfedTime::operator= (const RTIfedTime& newTime) throw (HLA::InvalidFederationTime)
{
	this->_fedTime = newTime._fedTime;
	this->_zero = newTime._zero;
	this->_positiveInfinity = newTime._positiveInfinity;
	this->_epsilon = newTime._epsilon;
	return *this;
}

HLA::FedTime& RTIfedTime::operator= (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	this->_fedTime = newTime;
	return *this;
}

HLA::FedTime& RTIfedTime::operator*= (const HLA::FedTime& newTime) throw (HLA::InvalidFederationTime)
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	this->_fedTime *= newImpl._fedTime;
	return *this;
}

HLA::FedTime& RTIfedTime::operator/= (const HLA::FedTime& newTime) throw (HLA::InvalidFederationTime)
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	this->_fedTime /= newImpl._fedTime;
	return *this;
}

HLA::FedTime& RTIfedTime::operator+= (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	this->_fedTime += newTime;
	return *this;
}

HLA::FedTime& RTIfedTime::operator-= (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	this->_fedTime -= newTime;
	return *this;
}

HLA::FedTime& RTIfedTime::operator*= (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	this->_fedTime *= newTime;
	return *this;
}

HLA::FedTime& RTIfedTime::operator/= (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	this->_fedTime /= newTime;
	return *this;
}

RTIfedTime RTIfedTime::operator+ (const HLA::FedTime& newTime) throw (HLA::InvalidFederationTime)
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	HLA::Double newValue = (this->_fedTime + newImpl._fedTime);
	return RTIfedTime(newValue);
}

RTIfedTime RTIfedTime::operator+ (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	HLA::Double newValue = this->_fedTime + newTime;
	return RTIfedTime(newValue);
}

RTIfedTime RTIfedTime::operator- (const HLA::FedTime& newTime) throw (HLA::InvalidFederationTime)
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	HLA::Double newValue = (this->_fedTime - newImpl._fedTime);
	return RTIfedTime(newValue);
}

RTIfedTime RTIfedTime::operator- (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	HLA::Double newValue = this->_fedTime - newTime;
	return RTIfedTime(newValue);
}

RTIfedTime RTIfedTime::operator* (const HLA::FedTime& newTime) throw (HLA::InvalidFederationTime)
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	HLA::Double newValue = (this->_fedTime * newImpl._fedTime);
	return RTIfedTime(newValue);
}

RTIfedTime RTIfedTime::operator* (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	HLA::Double newValue = this->_fedTime * newTime;
	return RTIfedTime(newValue);
}

RTIfedTime RTIfedTime::operator/ (const HLA::FedTime& newTime) throw (HLA::InvalidFederationTime)
{
	const RTIfedTime newImpl = (const RTIfedTime&)newTime;
	HLA::Double newValue = (this->_fedTime / newImpl._fedTime);
	return RTIfedTime(newValue);
}

RTIfedTime RTIfedTime::operator/ (const HLA::Double& newTime) throw (HLA::InvalidFederationTime)
{
	HLA::Double newValue = this->_fedTime / newTime;
	return RTIfedTime(newValue);
}

