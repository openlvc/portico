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
 *
 */
#include "common.h"
#include "portico/types/SimpleType.h"

using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
SimpleType::SimpleType( const std::wstring& name, IDatatype *representation )
{
	this->name = name;
	this->representation = representation;
}

SimpleType::~SimpleType()
{

}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
std::wstring SimpleType::getName() const
{
	return this->name;
}

DatatypeClass SimpleType::getDatatypeClass() const
{
	return DatatypeClass::SIMPLE;
}

IDatatype* SimpleType::getRepresentation()
{
	return this->representation;       
}

 