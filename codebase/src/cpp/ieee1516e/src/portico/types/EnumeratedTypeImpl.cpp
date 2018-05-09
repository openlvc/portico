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
#include "portico/types/EnumeratedType.h"  

using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
EnumeratedType::EnumeratedType( const std::wstring& name, 
                                IDatatype* representation, 
                                const std::list<Enumerator>& enumerators )
{
    this->name = name;
    this->representation = representation;
    this->enumerators = enumerators;
}

EnumeratedType::~EnumeratedType()
{

}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
IDatatype* EnumeratedType::getRepresentation() const
{
    return this->representation;
}

std::list<Enumerator> EnumeratedType::getEnumerators() const
{
    return this->enumerators;
}

bool EnumeratedType::operator==( const EnumeratedType& other ) const
{
    return this->name == other.name && 
	       this->representation == other.representation;
     
}

std::wstring EnumeratedType::getName() const
{
    return this->name;
}
 
DatatypeClass EnumeratedType::getDatatypeClass() const
{
    return DatatypeClass::ENUMERATED;
}
