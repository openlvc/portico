/*
 *   Copyright 2018 The Portico Project
 *
 *   This file is part of portico.
 *
 *   portico is free software; you can redistribute it and/or modify
 *   it under the terms of the Common Developer and Distribution License (CDDL)
 *   as published by Sun Microsystems. For more information see the LICENSE file.
 *
 *   Use of this software is strictly AT YOUR OWN RISK!!!
 *   If something bad happens you do not have permission to come crying to me.
 *   (that goes for your lawyer as well)
 */
#include "common.h"
#include "portico/types/Dimension.h"

using namespace PORTICO1516E_NS;

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
Dimension::Dimension( int cardinality ) 
{
    this->lowerCardinality = cardinality;
    this->upperCardinality = cardinality;
}

Dimension::Dimension( int lower, int upper )
{
    this->lowerCardinality = lower;
    this->upperCardinality = upper;
}

Dimension::~Dimension()
{

}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
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
