#pragma once

#include "portico/types/BasicType.h"   

 
BasicType::BasicType(const std::wstring& name, int size, Endianness endianness)
{
    this->name = name;
    this->size = size;
    this->endianness = endianness;
}

BasicType::~BasicType()
{
}


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
