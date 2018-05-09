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
#include "DatatypeRetrieval.h"

#include "jni/JniUtils.h"
#include "portico/types/BasicType.h"
#include "portico/types/EnumeratedType.h"
#include "portico/types/SimpleType.h"
#include "portico/types/ArrayType.h"
#include "portico/types/FixedRecordType.h"
#include "portico/types/VariantRecordType.h"
#include "portico/types/NaType.h"
#include "utils/StringUtils.h"

using namespace PORTICO1516E_NS;

const std::wstring DatatypeRetrieval::BASIC =      L"basicData";
const std::wstring DatatypeRetrieval::SIMPLE =     L"simpleData";
const std::wstring DatatypeRetrieval::ENUMERATED = L"enumeratedData";
const std::wstring DatatypeRetrieval::ARRAY =      L"arrayData";
const std::wstring DatatypeRetrieval::FIXED =      L"fixedRecordData";
const std::wstring DatatypeRetrieval::VARIANT =    L"variantRecordData";
const std::wstring DatatypeRetrieval::NA =         L"NA";

DatatypeRetrieval::DatatypeRetrieval( JavaRTI* javarti )
{
	// We store this so we can decide if we want to update 
	// the FOM at runtime.
	this->initialized = false;
	this->javarti = javarti;
}

DatatypeRetrieval::~DatatypeRetrieval()
{

}

bool DatatypeRetrieval::isInitialized()
{
	return this->initialized;
}


void DatatypeRetrieval::initialize() throw( RTIinternalError )
{
	JNIEnv* jnienv = this->javarti->getJniEnvironment();

	// call the method and convert to a string		
	jstring jFomString = (jstring)jnienv->CallObjectMethod( javarti->jproxy, javarti->GET_FOM );
	this->javarti->exceptionCheck();

	wstring fomString = JniUtils::toWideString( jnienv, jFomString );
	pugi::xml_parse_result result = this->fomxml.load_string( fomString.c_str() );

	// If the FOM was parsed set initialized to true, otherwise throw an exception
	if( result.status == pugi::xml_parse_status::status_ok )
		this->initialized = true;
	else
		throw RTIinternalError( L"The FOM could not be parsed. Please check FOM is correct." );
}

IDatatype* DatatypeRetrieval::getDatatype( const std::wstring& name )
	throw( RTIinternalError )
{
	if( !isInitialized() )
		this->initialize();

	IDatatype* datatype = nullptr;
	std::wstring nameLower = StringUtils::toLower( name );
	

	// If it hasn't been cached then create it and cache it.
	if( this->typeCache.find(nameLower) == this->typeCache.end() )
	{
		if( nameLower == L"na" )
		{
			datatype = new NaType();
		}
		else
		{ 
			pugi::xml_node typeNode = getDatatypeNode( name );
			std::wstring datatypeClassName = typeNode.name();

			if( datatypeClassName == DatatypeRetrieval::BASIC )
				datatype = createBasicType( typeNode );
			else if( datatypeClassName == DatatypeRetrieval::SIMPLE )
				datatype = createSimpleType( typeNode );
			else if( datatypeClassName == DatatypeRetrieval::ENUMERATED )
				datatype = createEnumeratedType( typeNode );
			else if( datatypeClassName == DatatypeRetrieval::ARRAY )
				datatype = createArrayType( typeNode );
			else if( datatypeClassName == DatatypeRetrieval::FIXED )
				datatype = createFixedRecordType(typeNode);
			else if( datatypeClassName == DatatypeRetrieval::VARIANT )
				datatype = createVariantRecordType(typeNode);
		}

		// cache it
		this->typeCache[nameLower] = datatype;
	}
	else
	{
		// Get from cache using handle as key. 
		datatype = this->typeCache[nameLower];
	}

	return datatype;
}

IDatatype* DatatypeRetrieval::createBasicType( const pugi::xml_node& node )
	throw( RTIinternalError )
{
	// Get the attributes from the node
	std::wstring typeName = node.attribute( L"name" ).as_string();
	int size = node.attribute( L"size" ).as_int();

	std::wstring endiannessString = node.attribute( L"endianness" ).as_string();
	Endianness end = endiannessString == L"LITTLE" ? Endianness::LITTLE : Endianness::BIG;
	
	return new BasicType( typeName, size, end );

}

IDatatype* DatatypeRetrieval::createSimpleType( const pugi::xml_node& node )
	throw( RTIinternalError )
{
	std::wstring typeName = node.attribute( L"name" ).as_string();
	std::wstring representation = node.attribute( L"representation" ).as_string();
	IDatatype* basicType = getDatatype( representation );

	return new SimpleType( typeName, basicType );
}

IDatatype* DatatypeRetrieval::createEnumeratedType( const pugi::xml_node& node )
	throw( RTIinternalError )
{
	std::list<Enumerator> enumerators;

	std::wstring name = node.attribute( L"name" ).as_string();
	std::wstring representation = node.attribute( L"representation" ).as_string();

	// get type from attribute chain
	IDatatype* basicType = getDatatype( representation );

	pugi::xpath_node_set enumeratorResults = node.select_nodes( L"./enumerator" );
	pugi::xpath_node_set::iterator resultIt = enumeratorResults.begin();
	while( resultIt != enumeratorResults.end() )
	{
		const pugi::xpath_node& enumResult = *resultIt++;
		const pugi::xml_node& enumerator = enumResult.node();
		std::wstring enumeratorName = enumerator.attribute( L"name" ).as_string();
		std::wstring enumeratorValue = enumerator.attribute( L"values" ).as_string();

		//add to enumerator list
		enumerators.push_back( Enumerator(enumeratorName, enumeratorValue) );
	}
	
	return new EnumeratedType( name, basicType, enumerators );
}

IDatatype* DatatypeRetrieval::createArrayType( const pugi::xml_node& node )
	throw( RTIinternalError )
{
	std::list<Dimension> dimensionList;

	std::wstring name = node.attribute( L"name" ).as_string();
	std::wstring representation = node.attribute( L"dataType" ).as_string();

	//get rep from name
	IDatatype* datatype = getDatatype( representation );

	pugi::xpath_node_set cardinalityResults = node.select_nodes( L"./cardinality" );
	pugi::xpath_node_set::iterator resultIt = cardinalityResults.begin();
	while( resultIt != cardinalityResults.end() )
	{
		const pugi::xpath_node& cardinalityResult = *resultIt++;
		const pugi::xml_node& dimension = cardinalityResult.node();

		int lowerBounds = Dimension::CARDINALITY_DYNAMIC;
		int upperBounds = Dimension::CARDINALITY_DYNAMIC;
		std::wstring cardinality = dimension.text().as_string();

		if( cardinality != L"Dynamic" )
		{
			// check to see if we have the  '..' delimiter specifying bounds	
			if( cardinality.find(L"..") != std::wstring::npos )
			{
				// get upper and lower bounds 
				std::wstring delimiter = L"..";
				std::wstring lowerBoundString = cardinality.substr( 0, cardinality.find(delimiter) );
				std::wstring upperBoundString = cardinality.substr( 1, cardinality.find(delimiter) );
				lowerBounds = stoi( lowerBoundString );
				upperBounds = stoi( upperBoundString );
			}
			else // its just an integer
			{
				lowerBounds = dimension.text().as_int();
				upperBounds = lowerBounds;
			}
		}

		dimensionList.push_back( Dimension(lowerBounds, upperBounds) );
	}

	// create the datatype
	return new ArrayType( name, datatype, dimensionList );
}

IDatatype* DatatypeRetrieval::createFixedRecordType( const pugi::xml_node& node )
	throw( RTIinternalError )
{
	std::list<Field> fieldList;

	std::wstring name = node.attribute( L"name" ).as_string();

	// Get all the fields in this fixed record type
	pugi::xpath_node_set fieldResults = node.select_nodes( L"./field" );
	pugi::xpath_node_set::iterator resultIt = fieldResults.begin();
	while( resultIt != fieldResults.end() )
	{
		const pugi::xpath_node& fieldResult = *resultIt++;
		const pugi::xml_node& field = fieldResult.node();

		std::wstring representation = field.attribute( L"dataType" ).as_string();
		std::wstring fieldName = field.attribute( L"name" ).as_string();

		// get type from attribute chain
		IDatatype* datatype = getDatatype( representation );

		fieldList.push_back( Field(fieldName, datatype) );
	}

	return new FixedRecordType(name, fieldList);
}

IDatatype* DatatypeRetrieval::createVariantRecordType( const pugi::xml_node& node )
	throw( RTIinternalError )
{
	std::list<Alternative> alternativesList;

	std::wstring name = node.attribute( L"name" ).as_string();
	std::wstring discriminantName = node.attribute( L"discriminant" ).as_string();
	std::wstring discriminantDatatypeName = node.attribute( L"dataType").as_string();

	IDatatype* discriminantDatatype = getDatatype( discriminantDatatypeName );
	EnumeratedType* discriminantAsEnum = dynamic_cast<EnumeratedType*>( discriminantDatatype );

	// Get all the alternatives in this fixed record type
	pugi::xpath_node_set alternativeResults = node.select_nodes( L"./alternative" );
	pugi::xpath_node_set::iterator alternativeIt = alternativeResults.begin();
	while( alternativeIt != alternativeResults.end() )
	{
		const pugi::xpath_node& alternativeResult = *alternativeIt++;
		const pugi::xml_node& alternativeNode = alternativeResult.node();

		std::wstring alternativeName = alternativeNode.attribute(L"name").as_string();
		std::wstring alternativeDatatypeName = alternativeNode.attribute( L"dataType" ).as_string();
		IDatatype* alternativeDatatype = getDatatype( alternativeDatatypeName );
		std::list<Enumerator> enumeratorList;

		// Get the enums for the alternatives
		pugi::xpath_node_set enumeratorResults = alternativeNode.select_nodes( L"./enumerator" );
		pugi::xpath_node_set::iterator enumeratorIt = enumeratorResults.begin();
		while(enumeratorIt != enumeratorResults.end() )
		{
			const pugi::xpath_node& enumeratorResult = *enumeratorIt++;
			const pugi::xml_node& enumeratorNode = enumeratorResult.node();
			std::wstring enumeratorName = enumeratorNode.text().as_string();

			Enumerator enumerator = this->getEnumeratorByName( discriminantAsEnum, 
			                                                   enumeratorName );
			enumeratorList.push_back( enumerator );
		}

		Alternative alternative( alternativeName, alternativeDatatype, enumeratorList );
		alternativesList.push_back( alternative );
	}

	return new VariantRecordType( name, 
	                              discriminantName, 
	                              discriminantDatatype,
	                              alternativesList );
}

Enumerator DatatypeRetrieval::getEnumeratorByName( const EnumeratedType* enumeration,
                                                   const std::wstring& name)
	throw( RTIinternalError )
{
	if( !enumeration )
		throw RTIinternalError( L"null enumeration" );

	std::list<Enumerator> enumerators = enumeration->getEnumerators();
	std::list<Enumerator>::iterator it = enumerators.begin();
	while( it != enumerators.end() )
	{
		const Enumerator& enumerator = *it++;
		if (enumerator.getName() == name)
			return enumerator;
	}

	throw RTIinternalError( enumeration->getName() + L"has no enumerator named " + name );
}

pugi::xml_node DatatypeRetrieval::getDatatypeNode( const std::wstring& name )
{
	// Search Basic Types
	std::wstring queryString = L"/objectModel/dataTypes/basicDataRepresentations/basicData[@name='" + name + L"']";
	pugi::xpath_node_set nodeSet = this->fomxml.select_nodes( queryString.c_str() );
	if( nodeSet.size() != 0 )
		return nodeSet[0].node();

	// Search Simple Types
	queryString = L"/objectModel/dataTypes/simpleDataTypes/simpleData[@name='" + name + L"']";
	nodeSet = this->fomxml.select_nodes( queryString.c_str() );
	if( nodeSet.size() != 0 )
		return nodeSet[0].node();

	// Search Enumerated Types
	queryString = L"/objectModel/dataTypes/enumeratedDataTypes/enumeratedData[@name='" + name + L"']";
	nodeSet = this->fomxml.select_nodes( queryString.c_str() );
	if( nodeSet.size() != 0 )
		return nodeSet[0].node();

	// Search Array Types
	queryString = L"/objectModel/dataTypes/arrayDataTypes/arrayData[@name='" + name + L"']";
	nodeSet = this->fomxml.select_nodes( queryString.c_str() );
	if( nodeSet.size() != 0 )
		return nodeSet[0].node();

	// Fixed Record Types
	queryString = L"/objectModel/dataTypes/fixedRecordDataTypes/fixedRecordData[@name='" + name + L"']";
	nodeSet = this->fomxml.select_nodes( queryString.c_str() );
	if( nodeSet.size() != 0 )
		return nodeSet[0].node();

	// Variant Record Types
	queryString = L"/objectModel/dataTypes/variantRecordDataTypes/variantRecordData[@name='" + name + L"']";
	nodeSet = this->fomxml.select_nodes( queryString.c_str() );
	if( nodeSet.size() != 0 )
		return nodeSet[0].node();

	throw RTIinternalError( L"Invalid datatype " + name );
}
