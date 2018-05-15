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
#include "portico/types/Alternative.h" 

using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
Alternative::Alternative( const std::wstring& name,
                          IDatatype* datatype,
                          const std::list<Enumerator>& enumerators )
{
    this->name = name;
    this->datatype = datatype;
    this->enumerators = enumerators; 
}

Alternative::~Alternative()
{

}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
IDatatype* Alternative::getDatatype() const
{
    return this->datatype;
}

std::list<Enumerator> Alternative::getEnumerators() const
{
    return this->enumerators;
}

std::wstring Alternative::getName() const
{
    return this->name;
}

 