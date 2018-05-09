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
#include "portico/types/VariantRecordType.h"   
 
using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
VariantRecordType::VariantRecordType( const std::wstring& name,
                                      const std::wstring& discriminantName,
                                      IDatatype* discriminantDatatype,
                                      const std::list<Alternative>&  alternatives)
{
    this->name = name;
    this->discriminantName = discriminantName;
    this->discriminantDatatype = discriminantDatatype;
    this->alternatives = alternatives;
}

VariantRecordType::~VariantRecordType()
{

}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
std::wstring VariantRecordType::getDiscriminantName() const
{
    return this->discriminantName;
}

IDatatype* VariantRecordType::getDiscriminantDatatype() const
{
    return this->discriminantDatatype;
}

std::list<Alternative> VariantRecordType::getAlternatives() const
{
    return this->alternatives;
}

std::wstring VariantRecordType::getName() const
{
    return this->name;
}

DatatypeClass VariantRecordType::getDatatypeClass() const
{
    return DatatypeClass::VARIANTRECORD;
}
 