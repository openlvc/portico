#pragma once

#include "portico/types/SimpleType.h"


SimpleType::SimpleType(const std::wstring& name, IDatatype *representation)
{
	this->name = name;
	this->representation = representation;
}

SimpleType::~SimpleType()
{
}

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
	return this;       
}

 