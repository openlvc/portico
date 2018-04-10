#include "RTI/portico/types/Field.h"   
 

 
Field::Field(const std::string& name, IDatatype* datatype)
{
    this->name = name;
	this->datatype = datatype;
}

Field::~Field()
{
    
}

std::string Field::getName() const
{
    return this->name;
}
 
IDatatype* Field::getDatatype()
{
    return this->datatype;
}

bool Field::operator==(const Field& other)
{
    return this->name == other.name && this->datatype == other.datatype;
}

 
