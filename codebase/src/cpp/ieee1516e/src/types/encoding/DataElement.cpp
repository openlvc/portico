/*
 *   Copyright 2012 The Portico Project
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
 *
 */
#include "common.h"
#include "RTI/encoding/DataElement.h"

IEEE1516E_NS_START

//------------------------------------------------------------------------------------------
//                                       CONSTRUCTORS                                       
//------------------------------------------------------------------------------------------
DataElement::~DataElement()
{
	
}

//------------------------------------------------------------------------------------------
//                                     INSTANCE METHODS
//------------------------------------------------------------------------------------------
// Return true if given element is same type as this; otherwise, false.
bool DataElement::isSameTypeAs( const DataElement& inData ) const
{
	return typeid(*this) == typeid(inData);
}

// From standard headers:
//    Return a hash of the encoded data
//    Provides mechanism to map DataElement discriminants to variants
//    in VariantRecord.
// This method is virtual and should be overridden by all children. The version
// here will always return -1
Integer64 DataElement::hash() const
{
	return -1;
}

//------------------------------------------------------------------------------------------
//                                      STATIC METHODS
//------------------------------------------------------------------------------------------

IEEE1516E_NS_END
