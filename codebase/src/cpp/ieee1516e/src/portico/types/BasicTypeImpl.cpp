#pragma once

#include "RTI/portico/types/BasicType.h"   

 
BasicType::BasicType(const std::string& name, int size, Endianness endianness)
{
    this->name = name;
    this->size = size;
    this->endianness = endianness;
}

BasicType::~BasicType()
{
}


Endianness BasicType::getEndianness()
{
	return this->endianness;
}
 
std::string BasicType::getName() const
{
	return this->name;
}
 
DatatypeClass BasicType::getDatatypeClass()
{
	return DatatypeClass::BASIC;
}