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

#include "jni/JavaRTI.h"
#include "../../../../lib/pugixml/include/pugixml.hpp"
#include <string>

#include "portico\types\BasicType.h"
#include "portico\types\EnumeratedType.h"
#include "portico\types\SimpleType.h"
#include "portico\types\ArrayType.h"
#include "portico\types\FixedRecordType.h"
#include "portico\types\VariantRecordType.h"
#include "portico\types\NaType.h"

PORTICO1516E_NS_START


class JavaRTI; /// forward declaration of JavaRTI to resolve circular-dependency

/**
 * This is the class that handles all of the grunt work for retrieving datatype
 * information. It is a singleton class that stores the FOM as an XML based datastructure.
 * The FOM is initialized on the first call to this class. Once a datatype is
 * retrieved it is cahced for fast retrieval later. Dependencies are dynacically loaded
 * recursivly when creating the dependant datatype.
 * <p/>
 * @see IDatatype
 * @see RTIambassadorEx
 */
class DatatypeRetrieval
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private:
		static const std::wstring BASIC;
		static const std::wstring SIMPLE;
		static const std::wstring ENUMERATED;
		static const std::wstring ARRAY;
		static const std::wstring FIXED;
		static const std::wstring VARIANT;
		static const std::wstring NA;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private:
		pugi::xml_document fomxml;						/// Hold the FOM in xml data structure.
		bool initialized;								/// True if the FOM has been initialized.
		std::map<std::wstring, IDatatype*> typeCache;			/// Stores the cache of all retrieved datatypes
		std::map<std::wstring, Enumerator*> enumeratorCache;	/// Stores the cache of all recieved enumerators

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		DatatypeRetrieval();
		~DatatypeRetrieval();
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/***
		 * Initialize the FOM xml object for the datatype retrieval object.
		 * @param The fom object in xml as a widestring
		 * @note An RTIinternalError is thrown if the FOM cannot be parsed.
		 */
		void initialize(std::wstring fomString);

		/**
		 * Check to see if the FOM has been initialized.
		 * @return True if the fom has been initialized, otherwise false.
		 */
		bool isInitialized();

		/**
		 * Get the parameter datatype given the name of a datatype.
		 * @param dataTypeName The name of the class being requested.
		 * @return The pointer to the datatype requested.
		 */
		IDatatype* getParameterDatatype(std::wstring dataTypeName);

		/**
		 * Get the attribute datatype given the name of a datatype.
		 * @param dataTypeName The name of the class being requested.
		 * @return The pointer to the datatype requested.
		 */
		IDatatype* getAttributeDatatype(std::wstring dataTypeName);

	private:
		/**
		 * Using the FOM get the datatypeClass for the datatype with classTypeName.
		 * @param classTypeName The name of the class the type is being requested for.
		 * @return The datatypeClass of the requested class.
		 * @see DatatypeClass
		 */
		DatatypeClass getDatatypeClassFromName(wstring classTypeName);

		/**
		 * Get a datatype given the name of a class. This is shared by both
		 * parameter and attribute requests.
		 * @param dataTypeName The name of the class being requested.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see BasicType
		 * @see SimpleType
		 * @see ArrayType
		 * @see EnumeratedType
		 * @see FixedRecordType
		 * @see VariantRecordType
		 */
		IDatatype* getDatatype(std::wstring dataTypeName);

		/**
		 * Create a BasicType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the BasicType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see BasicType
		*/
		IDatatype* getBasicType(pugi::xml_node dataNode);

		/**
		 * Create a SimpleType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the SimpleType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see SimpleType
		 */
		IDatatype* getSimpleType(pugi::xml_node dataNode);

		/**
		 * Create a EnumeratedType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the EnumeratedType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see EnumeratedType
		 */
		IDatatype* getEnumeratedType(pugi::xml_node dataNode);

		/**
		 * Create a ArrayType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the ArrayType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see ArrayType
		 */
		IDatatype* getArrayType(pugi::xml_node dataNode);

		/**
		 * Create a FixedRecordType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the FixedRecordType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see FixedRecordType
		 */
		IDatatype* getFixedRecordType(pugi::xml_node dataNode);

		/**
		 * Create a VariantRecordType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the VariantRecordType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see VariantRecordType
		 */
		IDatatype* getVariantRecordType(pugi::xml_node dataNode);


		/**
		 * Get the XML FOM node that contains all the information on the
		 * requested datatype.
		 * @note An RTIinternalError is thrown if the FOM contains multiple items
		 *       with the same name.
		 * @param name The name of the class being requested.
		 * @return The XML FOM node for the datatype requested.
		 */
		pugi::xml_node getDatatypeNode(std::wstring name);

		/**
		 * Create and cache an enumeratedType given the name of one of it's
		 * child enumerations.
		 * @param name The name of the enumerator that we want the parent created.
		 * @return True if we initialized the parent type, otherwise false..
		 */
		bool initEnumeratedTypeByEnumerator(std::wstring name);

		/**
		 * Create and cache an enumerator for use in VariantRecordTypes.
		 * @param name The name of the enumerator.
		 * @param value The value of the enumerator.
		 * @return True if we initialized the parent type, otherwise false..
		 */
		Enumerator* createEnumeratorAndCache(std::wstring name, std::wstring value);

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public:

};

PORTICO1516E_NS_END

