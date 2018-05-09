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
#include "portico/types/BasicType.h"   

using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
BasicType::BasicType( const std::wstring& name, int size, Endianness endianness )
{
    this->name = name;
    this->size = size;
    this->endianness = endianness;
}

BasicType::~BasicType()
{

}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
Endianness BasicType::getEndianness() const
{
	return this->endianness;
}
 
std::wstring BasicType::getName() const
{
	return this->name;
}
 
DatatypeClass BasicType::getDatatypeClass() const
{
	return DatatypeClass::BASIC;
}
