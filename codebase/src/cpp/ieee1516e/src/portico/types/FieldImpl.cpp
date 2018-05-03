#include "portico/types/Field.h"   
 

 
Field::Field(const std::wstring& name, IDatatype* datatype)
{
	this->name = name;
	this->datatype = datatype;
}

Field::~Field()
{
    
}

std::wstring Field::getName() const
{
    return this->name;
}
 
IDatatype* Field::getDatatype()
{
    return this->datatype;
}

bool Field::operator==(const Field& other) const
{
    return name == other.name &&  datatype->getDatatypeClass() == other.datatype->getDatatypeClass();
}

 