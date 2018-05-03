
#include "portico/types/EnumeratedType.h"  
#include <string>

EnumeratedType::EnumeratedType(const std::wstring& name, IDatatype* representation, const std::list<std::wstring>& enumerators)
{
    this->name = name;
    this->representation = representation;
    this->createEnumeratorsFromNames(enumerators);
}

EnumeratedType::EnumeratedType(const std::wstring& name, IDatatype* representation, const std::list<Enumerator*>& enumerators)
{
    this->name = name;
    this->representation = representation;
    this->enumerators = enumerators;
}

EnumeratedType::~EnumeratedType()
{

}

IDatatype* EnumeratedType::getRepresentation() const
{
    return this->representation;
}

std::list<Enumerator*> EnumeratedType::getEnumerators() const
{
    return this->enumerators;
}

bool EnumeratedType::operator == (const EnumeratedType& other)
{
    return this->name == other.name && this->representation == other.representation;
     
}

std::list<Enumerator*> EnumeratedType::createEnumeratorsFromNames(const std::list<std::wstring>& constants)
{ 
    std::list<Enumerator*> enumeratorList = std::list<Enumerator*>();
    std::list<std::wstring>::const_iterator itr = constants.begin();
    unsigned value = 0;
    for (itr ; itr != constants.end(); itr++)
    {
        enumeratorList.push_back(new Enumerator(*itr, std::to_wstring(static_cast<long long>(value++))));
    }

    return enumeratorList;
}

std::wstring EnumeratedType::getName() const
{
    return this->name;
}
 
DatatypeClass EnumeratedType::getDatatypeClass() const
{
    return DatatypeClass::ENUMERATED;
}

 
