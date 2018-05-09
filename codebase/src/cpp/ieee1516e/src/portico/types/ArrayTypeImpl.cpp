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
#include "portico/types/ArrayType.h"

using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
ArrayType::ArrayType( const std::wstring& name, IDatatype* datatype )
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions.push_back( Dimension(Dimension::CARDINALITY_DYNAMIC) );
    
}

ArrayType::ArrayType( const std::wstring& name, IDatatype* datatype, int cardinality )
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions.push_back( Dimension(cardinality) );
}

ArrayType::ArrayType( const std::wstring& name, 
                      IDatatype* datatype, 
                      const std::list<Dimension>& dimensions )
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions = dimensions;
}

ArrayType::~ArrayType()
{

}
 
//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
IDatatype* ArrayType::getDatatype() const
{
    return this->datatype;
}

std::list<Dimension> ArrayType::getDimensions() const
{
    return this->dimensions;
}

int ArrayType::getCardinalityLowerBound() const
{
   return this->dimensions.front().getCardinalityLowerBound();
}

int ArrayType::getCardinalityUpperBound() const
{
    return this->dimensions.front().getCardinalityUpperBound();
}

bool ArrayType::isCardinalityDynamic() const
{
    return this->dimensions.front().isCardinalityDynamic();
}

std::wstring ArrayType::getName() const
{
	return this->name;
}

DatatypeClass ArrayType::getDatatypeClass() const
{
    return DatatypeClass::ARRAY;
}
