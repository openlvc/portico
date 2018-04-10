#pragma once

#include "RTI/portico/types/NaType.h"   

 
NaType::NaType()
{}
NaType::~NaType()
{}
 
std::string NaType::getName() const
{
    return "NA";
}
 
DatatypeClass NaType::getDatatypeClass()
{
	return DatatypeClass::NA;
}