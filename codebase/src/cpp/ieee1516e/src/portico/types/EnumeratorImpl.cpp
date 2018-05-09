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
#include "portico/types/Enumerator.h"
 
using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
Enumerator::Enumerator( const std::wstring& name, const std::wstring& value )
{
    this->name = name;
    this->value = value;
}

Enumerator::~Enumerator()
{

}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
bool Enumerator::operator==( const Enumerator& other ) const
{
    return this->name == other.name && this->value == other.value;
}

 
std::wstring Enumerator::getName() const
{
    return this->name;
}

std::wstring Enumerator::getValue() const
{
    return this->value;
}


