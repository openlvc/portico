#include "portico/types/ArrayType.h"

ArrayType::ArrayType(const std::wstring& name, IDatatype* datatype)
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions.push_back (Dimension( Dimension::CARDINALITY_DYNAMIC));
    
}

ArrayType::ArrayType(const std::wstring& name, IDatatype *datatype, int cardinality)
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions.push_back(Dimension(cardinality));
}

ArrayType::ArrayType(const std::wstring& name, IDatatype *datatype, const std::list<Dimension>& dimensions)
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions = dimensions;
}

ArrayType::~ArrayType()
{

}
 

IDatatype* ArrayType::getDatatype() const
{
    return this->datatype;
}

std::list<Dimension> ArrayType::getDimensions() const
{
    return this->dimensions;
}

int ArrayType::getCardinalityLowerBound() 
{
   return this->dimensions.front().getCardinalityLowerBound();
}

int ArrayType::getCardinalityUpperBound() 
{
    return this->dimensions.front().getCardinalityUpperBound();
}

bool ArrayType::isCardinalityDynamic() 
{
    return this->dimensions.front().isCardinalityDynamic();
}

std::wstring ArrayType::getName() const
{
    return this->getName();
}

DatatypeClass ArrayType::getDatatypeClass() const
{
    return DatatypeClass::ARRAY;
}
