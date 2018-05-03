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

 /**
 * This class contains metadata about a FOM NA type.
 * <p/>
 * NA types will only have a type of NA and a name stored in teh metadata.
 */
class NaType : public virtual IDatatype
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		/**
		* Constructor for NaType with specified name, size and endianness
		*/
		NaType( );
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
	public:

};
