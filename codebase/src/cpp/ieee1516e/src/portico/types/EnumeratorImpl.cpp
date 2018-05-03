#pragma once

#include "portico/types/Enumerator.h"
#include "portico/types/Endianness.h"  

 
Enumerator::Enumerator(const std::wstring& name, const std::wstring& value){
    this->name = name;
    this->value = value;
}

Enumerator::~Enumerator()
{

}

 
bool Enumerator::operator==(const Enumerator& other)
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


