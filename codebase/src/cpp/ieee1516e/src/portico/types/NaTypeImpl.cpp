#pragma once

#include "portico/types/NaType.h"   

 
NaType::NaType()
{}
NaType::~NaType()
{}
 
std::wstring NaType::getName() const
{
    return L"NA";
}
 
DatatypeClass NaType::getDatatypeClass() const
{
	return DatatypeClass::NA;
}