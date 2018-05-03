#include "portico\types\FixedRecordType.h"

FixedRecordType::FixedRecordType(const std::wstring& name, const std::list<Field>& fields)
{
    this->name = name;
    this->fields = fields;
}

FixedRecordType::~FixedRecordType()
{

}

std::list<Field> FixedRecordType::getFields() const
{
    return this->fields;
}

bool FixedRecordType::operator==(const FixedRecordType& other) const
{
    return this->name == other.name && this->fields == other.fields;
}

std::wstring FixedRecordType::getName() const
{
    return this->name;
}

DatatypeClass FixedRecordType::getDatatypeClass() const
{
    return DatatypeClass::FIXEDRECORD;
}
