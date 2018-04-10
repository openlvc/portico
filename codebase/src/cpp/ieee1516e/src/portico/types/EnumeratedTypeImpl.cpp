
#include "RTI/portico/types/EnumeratedType.h"  
#include <string>

EnumeratedType::EnumeratedType(const std::string& name, IDatatype* representation, std::list<std::string> enumerators)
{
    this->name = name;
    this->representation = representation;
    this->createEnumeratorsFromNames(enumerators);
}

EnumeratedType::EnumeratedType(const std::string& name, IDatatype* representation, std::list<Enumerator*> enumerators)
{
    this->name = name;
    this->representation = representation;
    this->enumerators = enumerators;
}

EnumeratedType::~EnumeratedType()
{

}

IDatatype* EnumeratedType::getRepresentation()
{
    return this->representation;
}

std::list<Enumerator*>& EnumeratedType::getEnumerators()
{
    return this->enumerators;
}

bool EnumeratedType::operator == (const EnumeratedType& other)
{
    return this->name == other.name && this->representation == other.representation;
     
}

std::list<Enumerator*> EnumeratedType::createEnumeratorsFromNames(const std::list<std::string>& constants)
{ 
    std::list<Enumerator*> enumeratorList = std::list<Enumerator*>();
    std::list<std::string>::const_iterator itr = constants.begin();
    unsigned value = 0;
    for (itr ; itr != constants.end(); itr++)
    {
        enumeratorList.push_back(new Enumerator(*itr, std::to_string(static_cast<long long>(value))));
        value++;
    }

    return enumeratorList;
}

std::string EnumeratedType::getName() const
{
    return this->name;
}
 
DatatypeClass EnumeratedType::getDatatypeClass()
{
    return DatatypeClass::ENUMERATED;
}

 
