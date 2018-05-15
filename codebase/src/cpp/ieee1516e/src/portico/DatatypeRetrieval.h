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

#include <string>
#include "common.h"
#include "jni/JavaRTI.h"
#include "portico/IDatatype.h"
#include "portico/types/Enumerator.h"
#include "pugixml.hpp"

PORTICO1516E_NS_START


class JavaRTI; /// forward declaration of JavaRTI to resolve circular-dependency
class EnumeratedType;

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
		pugi::xml_document fomxml;                      /// Holds the FOM in xml data structure.
		bool initialized;                               /// True if the FOM has been initialized.
		std::map<std::wstring, IDatatype*> typeCache;   /// Stores the cache of all retrieved datatypes
		JavaRTI* javarti;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public:
		DatatypeRetrieval( JavaRTI* javarti );
		virtual ~DatatypeRetrieval();

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public:
		/**
		 * @return <code>true</code> if the FOM has been initialized, otherwise false.
		 */
		bool isInitialized();

		/**
		 * Returns the datatype defined in the Object Model under the specified name.
		 *
		 * @param name The name of the datatype being requested.
		 * @return The requested datatype
		 *
		 * @see IDatatype
		 */
		IDatatype* getDatatype( const std::wstring& name ) throw( RTIinternalError );

	private:
		/**
		 * Initializes datatype retrieval object.
		 *
		 * @throws RTIinternalError if the current federation's FOM cannot be parsed.
		  */
		void initialize() throw( RTIinternalError );

		/**
		 * Create a BasicType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the BasicType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see BasicType
		*/
		IDatatype* createBasicType( const pugi::xml_node& dataNode ) throw( RTIinternalError );

		/**
		 * Create a SimpleType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the SimpleType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see SimpleType
		 */
		IDatatype* createSimpleType( const pugi::xml_node& dataNode ) throw( RTIinternalError );

		/**
		 * Create a EnumeratedType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the EnumeratedType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see EnumeratedType
		 */
		IDatatype* createEnumeratedType( const pugi::xml_node& dataNode ) throw( RTIinternalError );

		/**
		 * Create a ArrayType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the ArrayType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see ArrayType
		 */
		IDatatype* createArrayType( const pugi::xml_node& dataNode ) throw( RTIinternalError );

		/**
		 * Create a FixedRecordType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the FixedRecordType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see FixedRecordType
		 */
		IDatatype* createFixedRecordType( const pugi::xml_node& dataNode ) throw( RTIinternalError );

		/**
		 * Create a VariantRecordType from information stored in the FOM
		 * @param dataNode The XML node that contains the information required
		 *				   to build the VariantRecordType.
		 * @return The pointer to the datatype requested.
		 * @see DatatypeClass
		 * @see IDatatype
		 * @see VariantRecordType
		 */
		IDatatype* createVariantRecordType( const pugi::xml_node& dataNode ) throw( RTIinternalError );

		/**
		 * Returns the enumerator of the specified EnumeratedType whose name matches 
		 * the provided name
		 *
		 * @param enumeration the EnumeratedType to search
		 * @param name the name of the enumerator to return
		 * @return the Enumerator whose name matches the query
		 * @throw RTIinternalError if no enumerator with the specified name can be found
		 */
		Enumerator getEnumeratorByName( const EnumeratedType* enumeration,
		                                const std::wstring& name ) 
			throw( RTIinternalError );

		/**
		 * Returns the XML FOM node that contains all the information on the datatype with the 
		 * specified name
		 * 
		 * @param name The name of the datatype
		 * @return The XML FOM node for the datatype requested, or an empty node if no node exists 
		 *         for the requested name
		 * @throw RTIinternalError if the FOM contains datatypes items with the same name.
		 */
		pugi::xml_node getDatatypeNode( const std::wstring& name ) throw( RTIinternalError );

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
};

PORTICO1516E_NS_END
