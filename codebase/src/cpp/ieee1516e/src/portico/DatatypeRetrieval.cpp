#include "DatatypeRetrieval.h"
#include "utils\StringUtils.h"

PORTICO1516E_NS_START

const std::wstring DatatypeRetrieval::BASIC =      L"basicData";
const std::wstring DatatypeRetrieval::SIMPLE =     L"simpleData";
const std::wstring DatatypeRetrieval::ENUMERATED = L"enumeratedData";
const std::wstring DatatypeRetrieval::ARRAY =      L"arrayData";
const std::wstring DatatypeRetrieval::FIXED =      L"fixedRecordData";
const std::wstring DatatypeRetrieval::VARIANT =    L"variantRecordData";
const std::wstring DatatypeRetrieval::NA =         L"NA";

DatatypeRetrieval::DatatypeRetrieval()
{
	// We store this so we can decide if we want to update 
	// the FOM at runtime. 
	this->initialized = false;
}

DatatypeRetrieval::~DatatypeRetrieval()
{}

void DatatypeRetrieval::initialize(std::wstring fomString)
{
	std::wstring xmlIn =  fomString;
	pugi::xml_parse_result result = this->fomxml.load_string(xmlIn.c_str());

	// The FOM was parsed set initialized ot true.
	if (result.status == pugi::xml_parse_status::status_ok)
	{
		this->initialized = true;
	}
	else{
		// throw error that FOM could not be parsed
		throw RTIinternalError(L"The FOM could not be parsed. Please check FOM is correct.");
	}
}

bool DatatypeRetrieval::isInitialized()
{
	return this->initialized;
}

IDatatype* DatatypeRetrieval::getParameterDatatype(std::wstring dataTypeName)
{
	return getDatatype(dataTypeName);
}

IDatatype* DatatypeRetrieval::getAttributeDatatype(std::wstring dataTypeName)
{
	return getDatatype(dataTypeName);
}

IDatatype* DatatypeRetrieval::getDatatype(std::wstring dataTypeName)
{
	IDatatype* datatype = nullptr;

	// If it hasn't been cached then create it and cache it.
	if (this->typeCache.find(dataTypeName) == this->typeCache.end())
	{
		if (dataTypeName == L"NA")
		{
			datatype = new NaType();
		}
		else
		{
			// create and cache it
			pugi::xml_node typeNode = getDatatypeNode(dataTypeName);
			std::wstring classTypeName = typeNode.name();

			if (classTypeName == DatatypeRetrieval::BASIC)
			{
				datatype = getBasicType(typeNode);
			}
			else if (classTypeName == DatatypeRetrieval::SIMPLE)
			{
				datatype = getSimpleType(typeNode);
			}
			else if (classTypeName == DatatypeRetrieval::ENUMERATED)
			{
				datatype = getEnumeratedType(typeNode);
			}
			else if (classTypeName == DatatypeRetrieval::ARRAY)
			{
				datatype = getArrayType(typeNode);
			}
			else if (classTypeName == DatatypeRetrieval::FIXED)
			{
				datatype = getFixedRecordType(typeNode);
			}
			else if (classTypeName == DatatypeRetrieval::VARIANT)
			{
				datatype = getVariantRecordType(typeNode);
			}
		}

		// cache it
		this->typeCache[dataTypeName] = datatype;
	}
	else
	{
		// Get from cache using handle as key. 
		datatype = this->typeCache[dataTypeName];
	}

	return datatype;
}

IDatatype* DatatypeRetrieval::getBasicType(pugi::xml_node dataNode)
{

	// Get the parameters from the node
	std::wstring typeName = dataNode.attribute(L"name").as_string();
	int size = dataNode.attribute(L"size").as_int();

	std::wstring endiannessString = dataNode.attribute(L"endianness").as_string();
	Endianness end = endiannessString == L"LITTLE" ? Endianness::LITTLE : Endianness::BIG;

	// Create and cache the new BasicType
	return new BasicType(typeName, size, end);

}

IDatatype* DatatypeRetrieval::getSimpleType(pugi::xml_node dataNode)
{

	// Get the parameters from the node
	std::wstring typeName = dataNode.attribute(L"name").as_string();
	std::wstring representation = dataNode.attribute(L"representation").as_string();
	IDatatype* basicType = getAttributeDatatype(representation);

	// Create and cache the new BasicType
	return new SimpleType(typeName, basicType);
}

IDatatype* DatatypeRetrieval::getEnumeratedType(pugi::xml_node dataNode)
{
	std::list<Enumerator*> enumerators;

	std::wstring name = dataNode.attribute(L"name").as_string();
	std::wstring representation = dataNode.attribute(L"representation").as_string();

	// get type from attribute chain
	IDatatype* basicType = getAttributeDatatype(representation);

	for (pugi::xml_node enumerations = dataNode.first_child(); enumerations; enumerations = enumerations.next_sibling(L"enumerator"))
	{
		std::wstring enumerationName = enumerations.attribute(L"name").as_string();
		std::wstring enumerationValue = enumerations.attribute(L"values").as_string();

		//add to enumerator list
		enumerators.push_back(createEnumeratorAndCache(enumerationName, enumerationValue));
	}

	// Create and cache the new BasicType
	return new EnumeratedType(name, basicType, enumerators);
}

Enumerator* DatatypeRetrieval::createEnumeratorAndCache(std::wstring name, std::wstring value)
{
	std::wstring enumerationName = name;
	std::wstring enumerationValue = value;
	Enumerator* enumerator = new Enumerator(enumerationName, enumerationValue);

	// Cache the enumerator for use with the variant record type
	this->enumeratorCache[enumerationName] = enumerator;

	return enumerator;
}

IDatatype* DatatypeRetrieval::getArrayType(pugi::xml_node dataNode)
{
	std::list<Dimension> dimensionList;

	std::wstring name = dataNode.attribute(L"name").as_string();
	std::wstring representation = dataNode.attribute(L"dataType").as_string();

	//get rep from name
	IDatatype* dataType = getAttributeDatatype(representation);

	for (pugi::xml_node dimensions = dataNode.first_child(); dimensions; dimensions = dimensions.next_sibling(L"cardinality"))
	{
		int lowerBounds = Dimension::CARDINALITY_DYNAMIC;
		int upperBounds = Dimension::CARDINALITY_DYNAMIC;
		std::wstring cardinality = dimensions.text().as_string();

		if (cardinality != L"Dynamic")
		{
			// check to see if we have the  '..' delimiter specifying bounds	
			if (cardinality.find(L"..") != std::wstring::npos)
			{
				// get upper and lower bounds 
				std::wstring delimiter = L"..";
				std::wstring lowerBoundString = cardinality.substr(0, cardinality.find(delimiter));
				std::wstring upperBoundString = cardinality.substr(1, cardinality.find(delimiter));
				lowerBounds = stoi(lowerBoundString);
				upperBounds = stoi(upperBoundString);

			}
			else // its just an integer
			{
				lowerBounds = dimensions.text().as_int();
			}
		}

		dimensionList.push_back(Dimension(lowerBounds, upperBounds));
	}

	// create the datatype
	return new ArrayType(name, dataType, dimensionList);
}

IDatatype* DatatypeRetrieval::getFixedRecordType(pugi::xml_node dataNode)
{
	std::list<Field> fieldList;

	std:wstring name = dataNode.attribute(L"name").as_string();

	// Get all the fields in this fixed record type
	for (pugi::xml_node fields = dataNode.first_child(); fields; fields = fields.next_sibling(L"field"))
	{
		std::wstring representation =  fields.attribute(L"dataType").as_string();
		std::wstring fieldName = fields.attribute(L"name").as_string();

		// get type from attribute chain
		IDatatype* datatype = getAttributeDatatype(representation);

		fieldList.push_back(Field(fieldName, datatype));
	}

	return new FixedRecordType(name, fieldList);
}

IDatatype* DatatypeRetrieval::getVariantRecordType(pugi::xml_node dataNode)
{
	std::list<Alternative> alternativesList;

	std::wstring name = dataNode.attribute(L"name").as_string();
	std::wstring discriminantName = dataNode.attribute(L"discriminant").as_string();
	std::wstring discriminantDatatypeRepresentation = dataNode.attribute(L"dataType").as_string();

	IDatatype* discriminantDatatype = getAttributeDatatype(discriminantDatatypeRepresentation);

	// Get all the alternatives in this fixed record type
	for (pugi::xml_node alternatives = dataNode.first_child(); alternatives; alternatives = alternatives.next_sibling(L"alternative"))
	{
		std::wstring alternativeName = alternatives.attribute(L"name").as_string();
		std::wstring alternativeDatatypeRepresentation = alternatives.attribute(L"dataType").as_string();
		IDatatype* alternativeDatatype = getAttributeDatatype(alternativeDatatypeRepresentation);
		std::list<Enumerator*> enumeratorList;

		// Get the enums for the alternatives
		for (pugi::xml_node enumerators = alternatives.first_child(); enumerators; enumerators = enumerators.next_sibling(L"enumerator"))
		{
			std::wstring enumeratorName = enumerators.text().as_string();

			// If the enumerator is not cached create the parent enumerated type then grab it.
			if (this->enumeratorCache.find(enumeratorName) == this->enumeratorCache.end())
			{
				// Init the parent enumerated type for this enumerator
				if (!initEnumeratedTypeByEnumerator(enumeratorName))
				{
					createEnumeratorAndCache(enumeratorName, L"");
				}
			}

			// probably should check if it was created rather than just assume it all worked great... probably
			enumeratorList.push_back(this->enumeratorCache[enumeratorName]);
		}

		alternativesList.push_back(Alternative(alternativeName, alternativeDatatype, enumeratorList));
	}


	return new VariantRecordType(name, discriminantName, discriminantDatatype, alternativesList);
}

bool DatatypeRetrieval::initEnumeratedTypeByEnumerator(std::wstring name)
{
	std::wstring queryString = L"//enumerator[@name ='" + name + L"']";
	pugi::xpath_node_set nodeSet = this->fomxml.select_nodes(queryString.c_str());

	if (nodeSet.size() > 1)
	{
		throw RTIinternalError(L"Enumerator name clash. All enumerator types should have unique names.");
		return false;
	}
	else if (nodeSet.size() == 0)
	{
		return false;
	}

	// Get the parent enumerated datatype by child name value
	pugi::xml_node enumeratorNode = nodeSet[0].node();
	pugi::xml_node enumeratorParent = enumeratorNode.parent();

	// Create the parent Enumerated type (this will cache it for use later)
	IDatatype* enumeratedType = getAttributeDatatype(enumeratorParent.attribute(L"name").as_string());

	return true;
}

pugi::xml_node DatatypeRetrieval::getDatatypeNode(std::wstring name)
{
	std::wstring queryString = L"//*[@name ='" + name + L"']";
	pugi::xpath_node_set nodeSet = this->fomxml.select_nodes(queryString.c_str());

	if (nodeSet.size() > 1)
	{
		throw RTIinternalError(L"Name clash, more than type exists with the same name. ");
	}

	return nodeSet[0].node();
}

PORTICO1516E_NS_END
