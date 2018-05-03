#include "portico/types/Dimension.h"


Dimension::Dimension(int cardinality) 
{
    this->lowerCardinality = cardinality;
    this->upperCardinality = cardinality;
}

Dimension::Dimension(int lower, int upper)
{
    this->lowerCardinality = lower;
    this->upperCardinality = upper;
}

Dimension::~Dimension()
{

}

int Dimension::getCardinalityLowerBound() const
{
    return this->lowerCardinality;
}

int Dimension::getCardinalityUpperBound() const
{
    return this->upperCardinality;
}

bool Dimension::isCardinalityDynamic() const
{
    return this->lowerCardinality == CARDINALITY_DYNAMIC;
}
