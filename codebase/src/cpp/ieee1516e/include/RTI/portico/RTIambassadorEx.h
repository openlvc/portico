/***********************************************************************
The IEEE hereby grants a general, royalty-free license to copy, distribute,
display and make derivative works from this material, for all purposes,
provided that any use of the material contains the following
attribution: "Reprinted with permission from IEEE 1516.1(TM)-2010".
Should you require additional information, contact the Manager, Standards
Intellectual Property, IEEE Standards Association (stds-ipr@ieee.org).

Copyright 2017 The Portico Project

This file is part of portico.
portico is free software; you can redistribute it and/or modify
it under the terms of the Common Developer and Distribution License (CDDL)
as published by Sun Microsystems. For more information see the LICENSE file.
Use of this software is strictly AT YOUR OWN RISK!!!
If something bad happens you do not have permission to come crying to me.
(that goes for your lawyer as well)
***********************************************************************/
/***********************************************************************
IEEE 1516.1 High Level Architecture Interface Specification C++ API
File: RTI/portico/RTIambassadorEx.h
***********************************************************************/

#pragma once

#include "RTI/RTIambassador.h"
#include "RTI/portico/IDatatype.h"

namespace rti1516e
{
	/**
	* This interface houses custom extensions to the standard IEEE-1516 (2010) interface
	* that are currently only supported by the Portico RTI. Any RTIambassador
	* instance returned from the RtiFactory by Portico will support this extensions
	* interface.
	* <p/>
	* This interface is currently only supported by the Portico RTI.
	* @see RtiFactory
	* @see RTIambassador
	*/
	class RTI_EXPORT RTIambassadorEx : public virtual RTIambassador {

	public:
		RTIambassadorEx();
		virtual ~RTIambassadorEx();

	public:
		/**
		* Returns the datatype of the specified Attribute.
		* <p/>
		* To determine the specific class of the datatype call {@link IDatatype#getDatatypeClass()}.
		* You can then cast the return value to the corresponding implementation (e.g.
		* {@link BasicType}, {@link SimpleType}, {@link EnumeratedType}, {@link ArrayType},
		* {@link FixedRecordType}, or {@link VariantRecordType}).
		* @see IDatatype
		*/
		virtual IDatatype* getAttributeDatatype( ObjectClassHandle whichClass,
												 AttributeHandle theHandle )
			throw ( 
				AttributeNotDefined,
				InvalidAttributeHandle,
				InvalidObjectClassHandle,
				FederateNotExecutionMember,
				NotConnected,
				RTIinternalError ) = 0 ;

		/**
		* Returns the datatype of the specified Parameter.
		* <p/>
		* To determine the specific class of the datatype call {@link IDatatype#getDatatypeClass()}.
		* You can then cast the return value to the corresponding implementation (e.g.
		* {@link BasicType}, {@link SimpleType}, {@link EnumeratedType}, {@link ArrayType},
		* {@link FixedRecordType}, or {@link VariantRecordType}).
		* @see IDatatype
		*/
		virtual IDatatype* getParameterDatatype( InteractionClassHandle whichClass,
												 ParameterHandle theHandle )
			throw ( 
				InteractionParameterNotDefined,
				InvalidParameterHandle,
				InvalidInteractionClassHandle,
				FederateNotExecutionMember,
				NotConnected,
				RTIinternalError ) = 0;

		/**
		* Return an in-memory representation of the unified Federation Object Model as an
		* XML encoded string. Currently, this contains only the datatype block modules.
		* @note The complete FOM will be added at a later stage.
		* @return The dataType block from the FOM as an XML encoded string.
		*/
		virtual std::wstring getFom() 
			throw ( 
				NotConnected,
				RTIinternalError) = 0;
	};
}
