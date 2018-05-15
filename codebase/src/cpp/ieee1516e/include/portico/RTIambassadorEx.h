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

#include "RTI/RTIambassador.h"
#include "portico/IDatatype.h"

namespace portico1516e
{
	/**
	 * This interface houses custom extensions to the standard IEEE-1516 (2010) interface
	 * that are currently only supported by the Portico RTI.
	 * <p/>
	 * You can access functionality defined at the RTIambassadorEx by casting the return
	 * value of {@link RTIambassadorFactory#createRTIambassador()} to RTIambassadorEx.
	 *
	 * @see RTIambassador
	 * @see RTIambassadorFactory
	 */
	class RTI_EXPORT RTIambassadorEx : public virtual rti1516e::RTIambassador 
	{
		//----------------------------------------------------------
		//                      CONSTRUCTORS
		//----------------------------------------------------------
		public:
			virtual ~RTIambassadorEx() {};

		//----------------------------------------------------------
		//                    INSTANCE METHODS
		//----------------------------------------------------------
		public:
			/**
			 * Returns the datatype of the specified Attribute.
			 * <p/>
			 * To determine the specific class of the datatype call {@link IDatatype#getDatatypeClass()}.
			 * You can then cast the return value to the corresponding implementation (e.g.
			 * {@link BasicType}, {@link SimpleType}, {@link EnumeratedType}, {@link ArrayType},
			 * {@link FixedRecordType}, or {@link VariantRecordType}).
			 * <p/>
			 * <b>Memory Management</b> the pointer returned by this function points to the internal
			 * LRC datatype cache and should not be deleted by the user.
			 *
			 * @see IDatatype
			 */
			virtual IDatatype* getAttributeDatatype( rti1516e::ObjectClassHandle whichClass,
			                                         rti1516e::AttributeHandle theHandle )
				throw ( rti1516e::AttributeNotDefined,
				        rti1516e::InvalidAttributeHandle,
				        rti1516e::InvalidObjectClassHandle,
				        rti1516e::FederateNotExecutionMember,
				        rti1516e::NotConnected,
				        rti1516e::RTIinternalError ) = 0 ;

			/**
			 * Returns the datatype of the specified Parameter.
			 * <p/>
			 * To determine the specific class of the datatype call 
			 * {@link IDatatype#getDatatypeClass()}. You can then cast the return value to the 
			 * corresponding implementation (e.g. {@link BasicType}, {@link SimpleType}, 
			 * {@link EnumeratedType}, {@link ArrayType}, {@link FixedRecordType}, or 
			 * {@link VariantRecordType}).
			 * <p/>
			 * <b>Memory Management</b> the pointer returned by this function points to the internal
			 * LRC datatype cache and should not be deleted by the user.
			 *
			 * @see IDatatype
			 */
			virtual IDatatype* getParameterDatatype( rti1516e::InteractionClassHandle whichClass,
			                                         rti1516e::ParameterHandle theHandle )
				throw ( rti1516e::InteractionParameterNotDefined,
				        rti1516e::InvalidParameterHandle,
				        rti1516e::InvalidInteractionClassHandle,
				        rti1516e::FederateNotExecutionMember,
				        rti1516e::NotConnected,
				        rti1516e::RTIinternalError ) = 0;
	};
}
