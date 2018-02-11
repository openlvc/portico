#pragma once

#include "RTI/portico/types/Enumerator.h"
#include "RTI/portico/types/Endianness.h"  

 
Enumerator::Enumerator(const std::string& name, const std::string& value){
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

 
std::string Enumerator::getName() const
{
    return this->name;
}

std::string Enumerator::getValue()
{
    return this->value;
}


