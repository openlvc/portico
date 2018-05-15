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
 *
 */
#pragma once

#include "portico/IDatatype.h"

namespace portico1516e
{
	/**
	 * This is a special system datatype that represents:
	 * <ol>
	 *  <li>
	 *      A placeholder for a datatype in an Object Model that does not support datatypes (e.g.
	 *      HLA 1.3).
	 *  </li>
	 *  <li>
	 *      Valid places in the FOM where NA can be listed as a datatype (e.g. privilegeToDelete in
	 *      1516, {@link Alternative} datatypes).
	 *  </li>
	 * </ol>
	 */
	class RTI_EXPORT NaType : public virtual IDatatype
	{
		//----------------------------------------------------------
		//                    STATIC VARIABLES
		//----------------------------------------------------------
	
		//----------------------------------------------------------
		//                   INSTANCE VARIABLES
		//----------------------------------------------------------

		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			NaType();
			virtual ~NaType();

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////// Datatype Interface ////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////
			virtual std::wstring getName() const;
			virtual DatatypeClass getDatatypeClass() const;

		//----------------------------------------------------------
		//                     STATIC METHODS
		//----------------------------------------------------------
	};
}
