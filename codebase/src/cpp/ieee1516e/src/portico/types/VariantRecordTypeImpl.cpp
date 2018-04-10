#include "RTI/portico/types/VariantRecordType.h"   
 
VariantRecordType::VariantRecordType(const std::string& name,
        const std::string& discriminantName,
        IDatatype* discriminantDatatype,
        std::list<Alternative>  alternatives)
{
    this->name = name;
    this->discriminantName = discriminantName;
    this->discriminantDatatype = discriminantDatatype;
    this->alternatives = alternatives;
}

VariantRecordType::~VariantRecordType()
{

}

std::string VariantRecordType::getDiscriminateName() const
{
    return this->discriminantName;
}

IDatatype* VariantRecordType::getDiscriminateDatatype() const
{
    return this->discriminantDatatype;
}

std::list<Alternative> VariantRecordType::getAlternatives() const
{
    return this->alternatives;
}

std::string VariantRecordType::getName() const
{
    return this->name;
}

DatatypeClass VariantRecordType::getDatatypeClass()
{
    return DatatypeClass::VARIANTRECORD;
}

 