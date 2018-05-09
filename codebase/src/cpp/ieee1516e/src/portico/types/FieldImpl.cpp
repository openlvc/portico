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
 */
#include "common.h"
#include "portico/types/Field.h"   

using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
Field::Field( const std::wstring& name, IDatatype* datatype )
{
	this->name = name;
	this->datatype = datatype;
}

Field::~Field()
{
    
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
std::wstring Field::getName() const
{
    return this->name;
}
 
IDatatype* Field::getDatatype() const
{
    return this->datatype;
}

bool Field::operator==( const Field& other ) const
{
    return name == other.name && 
		   datatype->getDatatypeClass() == other.datatype->getDatatypeClass();
}

 