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
#include "AttributeHandleSet.h"

PORTICO13_NS_START

//////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Constructors ////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
AttributeHandleSet::AttributeHandleSet( HLA::ULong size )
{
	// ignoring size for now as we're using a std::set that will grow for us
}

AttributeHandleSet::~AttributeHandleSet()
{
}

//////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////// Instance Methods //////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////
void AttributeHandleSet::checkIndex( HLA::ULong index ) const throw( HLA::ArrayIndexOutOfBounds )
{
	if( isEmpty() || index > handles.size()-1 )
	{
		char message[32];
		sprintf( message, "Index [%lo] out of bounds", index );
		throw HLA::ArrayIndexOutOfBounds( message ); // leak!
	}
}

HLA::ULong AttributeHandleSet::size() const
{
	return (HLA::ULong)handles.size();
}

HLA::AttributeHandle AttributeHandleSet::getHandle( HLA::ULong index ) const
	throw( HLA::ArrayIndexOutOfBounds )
{
	this->checkIndex( index );

	HLA::ULong counter = 0;

	set<HLA::AttributeHandle>::const_iterator setIterator;
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

void AttributeHandleSet::add( HLA::AttributeHandle handle )
	throw( HLA::ArrayIndexOutOfBounds, HLA::AttributeNotDefined )
{
	handles.insert( handle );
}

void AttributeHandleSet::remove( HLA::AttributeHandle handle ) throw( HLA::AttributeNotDefined )
{
	if( handles.erase(handle) == 0 && handles.size() != 0 )
	{
		char message[40];
		sprintf( message, "Attribute [%lo] doesn't exist", handle );
		throw HLA::AttributeNotDefined( message ); // leak!
	}
}

void AttributeHandleSet::empty()
{
	handles.clear();
}

HLA::Boolean AttributeHandleSet::isEmpty() const
{
	if( handles.empty() )
		return HLA::RTI_TRUE;
	else
		return HLA::RTI_FALSE;
}

HLA::Boolean AttributeHandleSet::isMember( HLA::AttributeHandle handle ) const
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
