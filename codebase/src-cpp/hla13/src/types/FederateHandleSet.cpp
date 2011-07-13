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
#include "FederateHandleSet.h"

PORTICO13_NS_START

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
FederateHandleSet::FederateHandleSet( HLA::ULong size )
{
	// ignoring size for now as we're using a std::set that will grow for us
}

FederateHandleSet::~FederateHandleSet()
{
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Instance Methods //////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
void FederateHandleSet::checkIndex( HLA::ULong index ) const throw( HLA::ArrayIndexOutOfBounds )
{
	if( handles.empty() || index > handles.size()-1 )
	{
		char message[32];
		sprintf( message, "Index [%lo] out of bounds", index );
		throw HLA::ArrayIndexOutOfBounds( message ); // leak!
	}
}

HLA::ULong FederateHandleSet::size() const
{
	return (HLA::ULong)handles.size();
}

HLA::FederateHandle FederateHandleSet::getHandle( HLA::ULong index ) const
	throw( HLA::ArrayIndexOutOfBounds )
{
	this->checkIndex( index );

	HLA::ULong counter = 0;

	set<HLA::FederateHandle>::const_iterator setIterator;
	for( setIterator = this->handles.begin();
	     setIterator!=this->handles.end();
	     setIterator++ )
	{
		if( counter == index )
		{
			return *setIterator;
		}
		counter++;
	}
	return 0;
}

void FederateHandleSet::add( HLA::FederateHandle handle ) throw( HLA::ValueCountExceeded )
{
	handles.insert( handle );
}

void FederateHandleSet::remove( HLA::FederateHandle handle ) throw( HLA::ArrayIndexOutOfBounds )
{
	if( handles.erase(handle) == 0 )
	{
		char message[32];
		sprintf( message, "Handle [%lo] doesn't exist", handle );
		throw HLA::ArrayIndexOutOfBounds( message ); // leak!
	}
}

void FederateHandleSet::empty()
{
	handles.clear();
}

HLA::Boolean FederateHandleSet::isMember( HLA::FederateHandle handle ) const
{
	if( handles.find(handle) != handles.end() )
		return HLA::RTI_TRUE;
	else
		return HLA::RTI_FALSE;
}

//////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////// Static Methods ///////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////

PORTICO13_NS_END
