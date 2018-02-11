#include "RTI\portico\types\FixedRecordType.h"

FixedRecordType::FixedRecordType(const std::string& name, std::list<Field> fields)
{
    this->name = name;
    this->fields = fields;
}

FixedRecordType::~FixedRecordType()
{

}

std::list<Field>& FixedRecordType::getFields()
{
    return this->fields;
}

bool FixedRecordType::operator==(const FixedRecordType& other)
{
    return this->name == other.name /*&& this->fields == other.fields*/;
}

std::string FixedRecordType::getName() const
{
    return this->name;
}

DatatypeClass FixedRecordType::getDatatypeClass()
{
    return DatatypeClass::FIXEDRECORD;
}
