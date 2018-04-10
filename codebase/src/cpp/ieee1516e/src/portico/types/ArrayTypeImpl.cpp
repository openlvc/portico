#include "RTI/portico/types/ArrayType.h"

ArrayType::ArrayType(const std::string& name, IDatatype* datatype)
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions.push_back (Dimension( Dimension::CARDINALITY_DYNAMIC));
    
}

ArrayType::ArrayType(const std::string& name, IDatatype *datatype, int cardinality)
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions.push_back(Dimension(cardinality));
}

ArrayType::ArrayType(const std::string& name, IDatatype *datatype, const std::list<Dimension>& dimensions)
{
    this->name = name;
    this->datatype = datatype;
    this->dimensions = dimensions;
}

ArrayType::~ArrayType()
{

}
 

IDatatype* ArrayType::getDatatype()
{
    return this->datatype;
}

std::list<Dimension>& ArrayType::getDimensions()
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

std::string ArrayType::getName() const
{
    return this->getName();
}

DatatypeClass ArrayType::getDatatypeClass()
{
    return DatatypeClass::ARRAY;
}


/**

cardinality_dynamic = -1
Dimension::DYNAMIC = Dimension(cardinality_dynamic)
The check in the cunstructor checks for cardinality_dynaic 
Other than returning the same static instance this seems redundant to me...

Dimension getDimensionFor(int cardinality)
{
    if (cardinality == Dimension::CARDINALITY_DYNAMIC)
    {
        //Dimension::DYNAMIC;
    }
    else{
        new Dimension(cardinality);
    }
        
}
*/