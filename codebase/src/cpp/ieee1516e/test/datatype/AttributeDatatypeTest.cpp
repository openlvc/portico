#include <portico/types/ArrayType.h>
#include <portico/types/BasicType.h>
#include <portico/types/EnumeratedType.h>
#include <portico/types/FixedRecordType.h>
#include <portico/types/NaType.h>
#include <portico/types/SimpleType.h>
#include <portico/types/VariantRecordType.h>

#include "AttributeDatatypeTest.h"

// Register test suite with the global repository
CPPUNIT_TEST_SUITE_REGISTRATION(AttributeDatatypeTest);
CPPUNIT_TEST_SUITE_NAMED_REGISTRATION(AttributeDatatypeTest, "datatypes"); 


AttributeDatatypeTest::AttributeDatatypeTest()
{
	this->defaultFederate = new Test1516eFederate( L"defaultFederate" ); 
}
AttributeDatatypeTest::~AttributeDatatypeTest()
{
	if( this->defaultFederate )
		delete this->defaultFederate; 
}
 
void AttributeDatatypeTest::setUp()
{ 
	this->defaultFederate->quickConnect();

	vector<wstring> modules;
	modules.push_back( L"etc/RPR-FOM2D18.xml" );
	modules.push_back( L"etc/datatypeEdgecases.xml" );

	try
	{
		RTIambassadorEx* rtiamb = this->defaultFederate->rtiamb;
		rtiamb->createFederationExecution( L"HLA_datatype_tests", modules );
	}
	catch (FederationExecutionAlreadyExists& exists)
	{
		wcout << L"Didn't create federation, it already existed" << endl;
	}

	
	this->defaultFederate->quickJoin(L"HLA_datatype_tests");
}

void AttributeDatatypeTest::tearDown()
{
	this->defaultFederate->quickResign();
	this->defaultFederate->quickDestroy( L"HLA_datatype_tests" );
	this->defaultFederate->quickDisconnect();
}

void AttributeDatatypeTest::testGetSimpleType()
{
	RTIambassadorEx* rtiamb = this->defaultFederate->rtiamb;
	ObjectClassHandle simpleClass = 
		rtiamb->getObjectClassHandle( L"HLAobjectRoot.ActiveSonarBeam" );
	AttributeHandle simpleAttribute = 
		rtiamb->getAttributeHandle( simpleClass, L"AzimuthBeamwidth" );

	IDatatype* datatype = rtiamb->getAttributeDatatype( simpleClass, simpleAttribute );
	CPPUNIT_ASSERT( datatype );
	CPPUNIT_ASSERT( datatype->getName() == L"Angle" );
	CPPUNIT_ASSERT( datatype->getDatatypeClass() == DATATYPE_SIMPLE );

	SimpleType* asSimple = dynamic_cast<SimpleType*>( datatype );
	CPPUNIT_ASSERT( asSimple );
	
	IDatatype* representation = asSimple->getRepresentation();
	CPPUNIT_ASSERT( representation );
	CPPUNIT_ASSERT( representation->getName() == L"HLAfloat32BE" );
	CPPUNIT_ASSERT( representation->getDatatypeClass() == DATATYPE_BASIC );
}

void AttributeDatatypeTest::testGetEnumeratedType()
{
	RTIambassadorEx* rtiamb = this->defaultFederate->rtiamb;
	ObjectClassHandle enumeratedClass = 
		rtiamb->getObjectClassHandle( L"HLAobjectRoot.ActiveSonarBeam" );
	AttributeHandle enumeratedAttribute = 
		rtiamb->getAttributeHandle(enumeratedClass, L"ScanPattern");

	IDatatype* datatype = rtiamb->getAttributeDatatype( enumeratedClass, enumeratedAttribute );
	CPPUNIT_ASSERT( datatype );
	CPPUNIT_ASSERT( datatype->getName() == L"ActiveSonarScanPatternEnum16" );
	CPPUNIT_ASSERT( datatype->getDatatypeClass() == DATATYPE_ENUMERATED );

	EnumeratedType* asEnumerated = dynamic_cast<EnumeratedType*>( datatype );
	CPPUNIT_ASSERT( asEnumerated );

	IDatatype* representation = asEnumerated->getRepresentation();
	CPPUNIT_ASSERT( representation );
	CPPUNIT_ASSERT( representation->getName() == L"HLAinteger16BE" );
	CPPUNIT_ASSERT( representation->getDatatypeClass() == DATATYPE_BASIC );

	list<Enumerator> enumerators = asEnumerated->getEnumerators();
	CPPUNIT_ASSERT( enumerators.size() == 6 );

	list<Enumerator>::iterator it = enumerators.begin();
	Enumerator enumerator = *it++;
	CPPUNIT_ASSERT( enumerator.getName() == L"ScanPatternNotUsed" );
	CPPUNIT_ASSERT( enumerator.getValue() == L"0" );

	enumerator = *it++;
	CPPUNIT_ASSERT( enumerator.getName() == L"Conical" );
	CPPUNIT_ASSERT( enumerator.getValue() == L"1" );

	enumerator = *it++;
	CPPUNIT_ASSERT( enumerator.getName() == L"Helical" );
	CPPUNIT_ASSERT( enumerator.getValue() == L"2" );

	enumerator = *it++;
	CPPUNIT_ASSERT( enumerator.getName() == L"Raster" );
	CPPUNIT_ASSERT( enumerator.getValue() == L"3" );

	enumerator = *it++;
	CPPUNIT_ASSERT( enumerator.getName() == L"SectorSearch" );
	CPPUNIT_ASSERT( enumerator.getValue() == L"4" );

	enumerator = *it++;
	CPPUNIT_ASSERT( enumerator.getName() == L"ContinuousSearch" );
	CPPUNIT_ASSERT( enumerator.getValue() == L"5" );
}

void AttributeDatatypeTest::testGetArrayTypeDynamic()
{
	RTIambassadorEx* rtiamb = this->defaultFederate->rtiamb;
	ObjectClassHandle arrayClass = 
		rtiamb->getObjectClassHandle( L"HLAobjectRoot.BaseEntity.PhysicalEntity" );
	AttributeHandle arrayAttribute = 
		rtiamb->getAttributeHandle( arrayClass, L"ArticulatedParametersArray" );

	IDatatype* datatype = rtiamb->getAttributeDatatype( arrayClass, arrayAttribute );
	CPPUNIT_ASSERT( datatype );
	CPPUNIT_ASSERT( datatype->getName() == L"ArticulatedParameterArray" );
	CPPUNIT_ASSERT( datatype->getDatatypeClass() == DATATYPE_ARRAY );

	ArrayType* asArray = dynamic_cast<ArrayType*>( datatype );
	CPPUNIT_ASSERT( asArray );
	CPPUNIT_ASSERT( asArray->isCardinalityDynamic() );
	CPPUNIT_ASSERT( asArray->getCardinalityLowerBound() == Dimension::CARDINALITY_DYNAMIC );
	CPPUNIT_ASSERT( asArray->getCardinalityUpperBound() == Dimension::CARDINALITY_DYNAMIC );
	
	list<Dimension> dimensions = asArray->getDimensions();
	CPPUNIT_ASSERT( dimensions.size() == 1 );

	Dimension dimension = *dimensions.begin();
	CPPUNIT_ASSERT( dimension.isCardinalityDynamic() );
	CPPUNIT_ASSERT( dimension.getCardinalityLowerBound() == Dimension::CARDINALITY_DYNAMIC );
	CPPUNIT_ASSERT( dimension.getCardinalityUpperBound() == Dimension::CARDINALITY_DYNAMIC );

	IDatatype* representation = asArray->getDatatype();
	CPPUNIT_ASSERT( representation );
	CPPUNIT_ASSERT( representation->getName() == L"ArticulatedParameterStruct" );
	CPPUNIT_ASSERT( representation->getDatatypeClass() == DATATYPE_FIXEDRECORD );
}

void AttributeDatatypeTest::testGetArrayTypeFixed()
{
	RTIambassadorEx* rtiamb = this->defaultFederate->rtiamb;
	ObjectClassHandle arrayClass = 
		rtiamb->getObjectClassHandle( L"HLAobjectRoot.BaseEntity.AggregateEntity" );

	// Note AggregateMarking is a fixed record, however it contains a fixed array
	AttributeHandle arrayAttribute = 
		rtiamb->getAttributeHandle( arrayClass, L"AggregateMarking" );

	IDatatype* datatype = rtiamb->getAttributeDatatype( arrayClass, arrayAttribute );
	FixedRecordType* asFixed = dynamic_cast<FixedRecordType*>( datatype );
	CPPUNIT_ASSERT( asFixed );
	
	list<Field> fields = asFixed->getFields();
	list<Field>::iterator it = fields.begin();
	IDatatype* fixedArrayDatatype = 0;
	while( it != fields.end() )
	{
		const Field& currentField = *it++;
		if( currentField.getName() == L"MarkingData" )
		{
			fixedArrayDatatype = currentField.getDatatype();
			break;
		}
	}
	CPPUNIT_ASSERT( fixedArrayDatatype );

	ArrayType* asArray = dynamic_cast<ArrayType*>( fixedArrayDatatype );
	CPPUNIT_ASSERT( asArray );
	CPPUNIT_ASSERT( !asArray->isCardinalityDynamic() );
	CPPUNIT_ASSERT( asArray->getCardinalityLowerBound() == 31 );
	CPPUNIT_ASSERT( asArray->getCardinalityUpperBound() == 31 );
	
	list<Dimension> dimensions = asArray->getDimensions();
	CPPUNIT_ASSERT( dimensions.size() == 1 );

	Dimension dimension = *dimensions.begin();
	CPPUNIT_ASSERT( !dimension.isCardinalityDynamic() );
	CPPUNIT_ASSERT( dimension.getCardinalityLowerBound() == 31 );
	CPPUNIT_ASSERT( dimension.getCardinalityUpperBound() == 31 );

	IDatatype* representation = asArray->getDatatype();
	CPPUNIT_ASSERT( representation );
	CPPUNIT_ASSERT( representation->getName() == L"HLAbyte" );
	CPPUNIT_ASSERT( representation->getDatatypeClass() == DATATYPE_SIMPLE );
}

void AttributeDatatypeTest::testGetFixedRecordType()
{
	RTIambassadorEx* rtiamb = this->defaultFederate->rtiamb;
	ObjectClassHandle fixedClass = 
		rtiamb->getObjectClassHandle( L"HLAobjectRoot.BaseEntity" );
	AttributeHandle fixedAttribute = 
		rtiamb->getAttributeHandle( fixedClass, L"EntityType" );

	IDatatype* datatype = rtiamb->getAttributeDatatype( fixedClass, fixedAttribute );
	CPPUNIT_ASSERT( datatype );
	CPPUNIT_ASSERT( datatype->getName() == L"EntityTypeStruct" );
	CPPUNIT_ASSERT( datatype->getDatatypeClass() == DATATYPE_FIXEDRECORD );

	FixedRecordType* asFixed = dynamic_cast<FixedRecordType*>( datatype );
	list<Field> fields = asFixed->getFields();
	CPPUNIT_ASSERT( fields.size() == 7 );

	list<Field>::iterator it = fields.begin();

	Field field = *it++;
	IDatatype* fieldType = field.getDatatype();
	CPPUNIT_ASSERT( field.getName() == L"EntityKind" );
	CPPUNIT_ASSERT( fieldType->getName() == L"HLAbyte" );
	CPPUNIT_ASSERT( fieldType->getDatatypeClass() == DATATYPE_SIMPLE );

	field = *it++;
	fieldType = field.getDatatype();
	CPPUNIT_ASSERT( field.getName() == L"Domain" );
	CPPUNIT_ASSERT( fieldType->getName() == L"HLAbyte" );
	CPPUNIT_ASSERT( fieldType->getDatatypeClass() == DATATYPE_SIMPLE );

	field = *it++;
	fieldType = field.getDatatype();
	CPPUNIT_ASSERT( field.getName() == L"CountryCode" );
	CPPUNIT_ASSERT( fieldType->getName() == L"unsignedInt16" );
	CPPUNIT_ASSERT( fieldType->getDatatypeClass() == DATATYPE_SIMPLE );

	field = *it++;
	fieldType = field.getDatatype();
	CPPUNIT_ASSERT( field.getName() == L"Category" );
	CPPUNIT_ASSERT( fieldType->getName() == L"HLAbyte" );
	CPPUNIT_ASSERT( fieldType->getDatatypeClass() == DATATYPE_SIMPLE );

	field = *it++;
	fieldType = field.getDatatype();
	CPPUNIT_ASSERT( field.getName() == L"Subcategory" );
	CPPUNIT_ASSERT( fieldType->getName() == L"HLAbyte" );
	CPPUNIT_ASSERT( fieldType->getDatatypeClass() == DATATYPE_SIMPLE );

	field = *it++;
	fieldType = field.getDatatype();
	CPPUNIT_ASSERT( field.getName() == L"Specific" );
	CPPUNIT_ASSERT( fieldType->getName() == L"HLAbyte" );
	CPPUNIT_ASSERT( fieldType->getDatatypeClass() == DATATYPE_SIMPLE );

	field = *it++;
	fieldType = field.getDatatype();
	CPPUNIT_ASSERT( field.getName() == L"Extra" );
	CPPUNIT_ASSERT( fieldType->getName() == L"HLAbyte" );
	CPPUNIT_ASSERT( fieldType->getDatatypeClass() == DATATYPE_SIMPLE );
}

void AttributeDatatypeTest::testGetVariantRecordType()
{
	RTIambassadorEx* rtiamb = this->defaultFederate->rtiamb;
	ObjectClassHandle variantClass = 
		rtiamb->getObjectClassHandle( L"HLAobjectRoot.EmbeddedSystem.RadioTransmitter" );

	// Note AntennaPatternArray is an array type, however its datatype is a variant record
	AttributeHandle variantAttribute = 
		rtiamb->getAttributeHandle( variantClass, L"AntennaPatternData" );

	IDatatype* datatype = rtiamb->getAttributeDatatype( variantClass, variantAttribute );
	CPPUNIT_ASSERT( datatype );
	
	ArrayType* asArray = dynamic_cast<ArrayType*>( datatype );
	IDatatype* arrayDatatype = asArray->getDatatype();
	CPPUNIT_ASSERT( arrayDatatype );
	CPPUNIT_ASSERT( arrayDatatype->getName() == L"AntennaPatternStruct" );
	CPPUNIT_ASSERT( arrayDatatype->getDatatypeClass() == DATATYPE_VARIANTRECORD );

	VariantRecordType* asVariant = dynamic_cast<VariantRecordType*>( arrayDatatype );
	IDatatype* discriminantType = asVariant->getDiscriminantDatatype();
	CPPUNIT_ASSERT( asVariant );
	CPPUNIT_ASSERT( asVariant->getDiscriminantName() == L"AntennaPatternType" );
	CPPUNIT_ASSERT( discriminantType );
	CPPUNIT_ASSERT( discriminantType->getName() == L"AntennaPatternTypeEnum32" );
	CPPUNIT_ASSERT( discriminantType->getDatatypeClass() == DATATYPE_ENUMERATED );

	list<Alternative> alternatives = asVariant->getAlternatives();
	list<Alternative>::iterator it = alternatives.begin();
	CPPUNIT_ASSERT( alternatives.size() == 2 );
	
	Alternative alternative = *it++;
	IDatatype* alternativeDatatype = alternative.getDatatype();
	list<Enumerator> alternativeEnumerators = alternative.getEnumerators();
	CPPUNIT_ASSERT( alternativeEnumerators.size() == 1 );
	Enumerator firstEnumerator = *alternativeEnumerators.begin();
	CPPUNIT_ASSERT( firstEnumerator.getName() == L"Beam" );
	CPPUNIT_ASSERT( alternative.getName() == L"BeamAntenna" );
	CPPUNIT_ASSERT( alternativeDatatype->getName() == L"BeamAntennaStruct" );

	alternative = *it++;
	alternativeDatatype = alternative.getDatatype();
	alternativeEnumerators = alternative.getEnumerators();
	CPPUNIT_ASSERT( alternativeEnumerators.size() == 1 );
	firstEnumerator = *alternativeEnumerators.begin();
	CPPUNIT_ASSERT( firstEnumerator.getName() == L"SphericalHarmonic" );
	CPPUNIT_ASSERT( alternative.getName() == L"SphericalHarmonicAntenna" );
	CPPUNIT_ASSERT( alternativeDatatype->getName() == L"SphericalHarmonicAntennaStruct" );
}

void AttributeDatatypeTest::testGetNAType()
{
	RTIambassadorEx* rtiamb = this->defaultFederate->rtiamb;
	ObjectClassHandle naClass = 
		rtiamb->getObjectClassHandle( L"HLAobjectRoot.A" );

	// Note AntennaPatternArray is an array type, however its datatype is a variant record
	AttributeHandle naAttribute = 
		rtiamb->getAttributeHandle( naClass, L"naattribute" );

	IDatatype* datatype = rtiamb->getAttributeDatatype( naClass, naAttribute );
	CPPUNIT_ASSERT( datatype );
	CPPUNIT_ASSERT( datatype->getDatatypeClass() == DATATYPE_NA );
}

