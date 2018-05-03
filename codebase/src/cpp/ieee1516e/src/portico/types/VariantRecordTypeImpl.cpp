#include "portico/types/VariantRecordType.h"   
 
VariantRecordType::VariantRecordType(const std::wstring& name,
        const std::wstring& discriminantName,
        IDatatype* discriminantDatatype,
        const std::list<Alternative>&  alternatives)
{
    this->name = name;
    this->discriminantName = discriminantName;
    this->discriminantDatatype = discriminantDatatype;
    this->alternatives = alternatives;
}

VariantRecordType::~VariantRecordType()
{

}

std::wstring VariantRecordType::getDiscriminateName() const
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

std::wstring VariantRecordType::getName() const
{
    return this->name;
}

DatatypeClass VariantRecordType::getDatatypeClass() const
{
    return DatatypeClass::VARIANTRECORD;
}

 