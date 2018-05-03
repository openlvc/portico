#include "portico/types/Alternative.h" 
 
Alternative::Alternative(const std::wstring& name, IDatatype* datatype, const std::list<Enumerator*>& enumerators)
{
    this->name = name;
    this->datatype = datatype;
    this->enumerators = enumerators; 
}

Alternative::~Alternative()
{

}

IDatatype* Alternative::getDatatype() const
{
    return this->datatype;
}

std::list<Enumerator*> Alternative::getEnumerators() const
{
    return this->enumerators;
}

std::wstring Alternative::getName() const
{
    return this->name;
}

 