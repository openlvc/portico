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
#include "portico/types/FixedRecordType.h"

using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
FixedRecordType::FixedRecordType( const std::wstring& name, 
                                  const std::list<Field>& fields )
{
    this->name = name;
    this->fields = fields;
}

FixedRecordType::~FixedRecordType()
{

}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
std::list<Field> FixedRecordType::getFields() const
{
    return this->fields;
}

bool FixedRecordType::operator==(const FixedRecordType& other) const
{
    return this->name == other.name && this->fields == other.fields;
}

std::wstring FixedRecordType::getName() const
{
    return this->name;
}

DatatypeClass FixedRecordType::getDatatypeClass() const
{
    return DATATYPE_FIXEDRECORD;
}

