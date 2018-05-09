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
#pragma once

#include "types/DatatypeClass.h"
#include "RTI/SpecificConfig.h"
#include <string>

namespace portico1516e
{
	/**
	 * Common interface for all FOM datatypes.
	 */
	class RTI_EXPORT IDatatype
	{
 		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public: 
			virtual ~IDatatype(){}

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * @return the name of this datatype.
			 */
			virtual std::wstring getName() const = 0;

			/**
			 * @return the FOM datatype class of this datatype (e.g. Basic, Simple, Enumerated, Array,
			 *         Fixed Record or Variant).
			 */
			virtual DatatypeClass getDatatypeClass() const = 0;
	};
}
