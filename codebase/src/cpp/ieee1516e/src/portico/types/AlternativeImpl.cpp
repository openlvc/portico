#include "RTI/portico/types/Alternative.h" 
 
Alternative::Alternative(const std::string& name, IDatatype* datatype, std::list<Enumerator*> enumerators)
{
    this->name = name;
    this->datatype = datatype;
    this->enumerators = enumerators; 
}

Alternative::~Alternative()
{

}

IDatatype* Alternative::getDatatype()
{
    return this->datatype;
}

std::list<Enumerator*>& Alternative::getEnumerators()
{
    return this->enumerators;
}

std::string Alternative::getName() const
{
    return this->name;
}

 