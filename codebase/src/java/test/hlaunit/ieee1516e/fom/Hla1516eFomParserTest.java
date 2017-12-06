/*
 *   Copyright 2016 The Portico Project
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
package hlaunit.ieee1516e.fom;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.*;

import org.portico.impl.hla1516e.fomparser.FOM;
import org.portico.lrc.compat.JErrorReadingFED;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.ICMetadata;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.model.datatype.Alternative;
import org.portico.lrc.model.datatype.ArrayType;
import org.portico.lrc.model.datatype.BasicType;
import org.portico.lrc.model.datatype.DatatypeClass;
import org.portico.lrc.model.datatype.Dimension;
import org.portico.lrc.model.datatype.Endianness;
import org.portico.lrc.model.datatype.EnumeratedType;
import org.portico.lrc.model.datatype.Enumerator;
import org.portico.lrc.model.datatype.Field;
import org.portico.lrc.model.datatype.FixedRecordType;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.lrc.model.datatype.IEnumerator;
import org.portico.lrc.model.datatype.SimpleType;
import org.portico.lrc.model.datatype.VariantRecordType;
import org.portico.utils.fom.FomParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Verify that the 1516e FOM parser is behaving properly. This test was based (copied) from
 * the other parser tests which are more complete. As such, much of it is purposely commented
 * out for now and needs to be rounded out and fixed. We just can't do everything at once.
 * Such is life.
 */
@Test(groups={"Hla1516eFomParserTest","fom","fom1516e"})
public class Hla1516eFomParserTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private URL validFom;
	//private URL validWithoutSpaces;
	//private URL invalidUnbalanced;
	//private URL invalidBadComment;
	//private URL invalidRandomText;
	//private URL invalidAttBadSpace;
	private URL invalidAttBadTransport;
	private URL invalidAttBadOrder;
	//private URL invalidIntBadSpace;
	private URL invalidIntBadTransport;
	private URL invalidIntBadOrder;
	private URL validFomModule;
	private URL validFomModuleNoInteractions;
	private URL validFomModuleNoObjects;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		this.validFom               = ClassLoader.getSystemResource( "fom/testfom.xml" );
		this.validFomModule         = ClassLoader.getSystemResource( "fom/ieee1516e/testfomModule.xml" );
		this.validFomModuleNoInteractions   = ClassLoader.getSystemResource( "fom/ieee1516e/testfomModuleNoInteractions.xml" );
		this.validFomModuleNoObjects   = ClassLoader.getSystemResource( "fom/ieee1516e/testfomModuleNoObjects.xml" );
		//this.validWithoutSpaces     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-nospaces.xml" );
		//this.invalidUnbalanced      = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-unbalanced.xml" );
		//this.invalidBadComment      = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-badcomment.xml" );
		//this.invalidRandomText      = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-randomtext.xml" );
		//this.invalidAttBadSpace     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedAttributeSpace.xml" );
		this.invalidAttBadTransport = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedAttributeTransport.xml" );
		this.invalidAttBadOrder     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedAttributeOrder.xml" );
		//this.invalidIntBadSpace     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedInteractionSpace.xml" );
		this.invalidIntBadTransport = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedInteractionTransport.xml" );
		this.invalidIntBadOrder     = ClassLoader.getSystemResource( "fom/ieee1516e/testfom-undefinedInteractionOrder.xml" );
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Valid FOM Test Methods //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse in a valid HLA 1516e FOM
	 */
	@Test
	public void testParseValidHla1516eFomFull()
	{
		try
		{
			FOM.parseFOM( this.validFom );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	/**
	 * Parse in a valid HLA 1516e FOM where one module has no interactions element.
	 */
	@Test
	public void testParseValidHla1516eFomModuleNoInteractions()
	{
		try
		{
			List<ObjectModel> foms = new ArrayList<ObjectModel>();
			foms.add( FomParser.parse(this.validFomModule) );
			foms.add( FomParser.parse(this.validFomModuleNoInteractions) );
			
			ObjectModel combinedFOM = ModelMerger.merge( foms );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
		
		try
		{
			List<ObjectModel> foms = new ArrayList<ObjectModel>();
			foms.add( FomParser.parse(this.validFomModuleNoInteractions) );
			foms.add( FomParser.parse(this.validFomModule) );
			ObjectModel combinedFOM = ModelMerger.merge( foms );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	/**
	 * Parse in a valid HLA 1516e FOM where one module has no objects element.
	 */
	@Test
	public void testParseValidHla1516eFomModuleNoObjects()
	{
		try
		{
			List<ObjectModel> foms = new ArrayList<ObjectModel>();
			foms.add( FomParser.parse(this.validFomModuleNoObjects) );
			foms.add( FomParser.parse(this.validFomModule) );
			ObjectModel combinedFOM = ModelMerger.merge( foms );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	@Test 
	public void testParseValidHla1516eDatatypesBasic()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/datatypes.xml");
			ObjectModel model = FOM.parseFOM( url );
			
			IDatatype type = model.getDatatype( "UnsignedShort" );
			Assert.assertNotNull( type );
			Assert.assertTrue( type instanceof BasicType );
			Assert.assertEquals( type.getDatatypeClass(), DatatypeClass.BASIC );
			
			BasicType asBasic = (BasicType)type;

			// Name
			Assert.assertEquals( asBasic.getName(), "UnsignedShort" );
			
			// Size
			Assert.assertEquals( asBasic.getSize(), 16 );
			
			// Endianness
			Assert.assertEquals( asBasic.getEndianness(), Endianness.BIG );
			
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	@Test 
	public void testParseValidHla1516eDatatypesSimple()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/datatypes.xml");
			ObjectModel model = FOM.parseFOM( url );
			
			//
			// Type with standard basicData representation
			//
			IDatatype simpleStandard = model.getDatatype( "int16" );
			Assert.assertNotNull( simpleStandard );
			Assert.assertEquals( simpleStandard.getDatatypeClass(), DatatypeClass.SIMPLE );
			Assert.assertTrue( simpleStandard instanceof SimpleType );
			
			SimpleType asSimple = (SimpleType)simpleStandard;
			
			// Name
			Assert.assertEquals( simpleStandard.getName(), "int16" );
			
			// Representation
			IDatatype standardRepresentation = asSimple.getRepresentation();
			Assert.assertNotNull( standardRepresentation );
			Assert.assertEquals( standardRepresentation.getDatatypeClass(), DatatypeClass.BASIC );
			Assert.assertTrue( standardRepresentation instanceof BasicType );
			Assert.assertEquals( standardRepresentation, 
			                     model.getDatatype("HLAinteger16BE") );
			
			// Type with custom basicData representation
			IDatatype simpleCustom = model.getDatatype( "Channel" );
			Assert.assertNotNull( simpleCustom );
			Assert.assertEquals( simpleCustom.getDatatypeClass(), DatatypeClass.SIMPLE );
			Assert.assertTrue( simpleCustom instanceof SimpleType );
			
			SimpleType asCustom = (SimpleType)simpleCustom;
			
			// Name
			Assert.assertEquals( asCustom.getName(), "Channel" );
			
			// Representation
			IDatatype customRepresentation = asCustom.getRepresentation();
			Assert.assertNotNull( customRepresentation );
			Assert.assertEquals( customRepresentation.getDatatypeClass(), DatatypeClass.BASIC );
			Assert.assertTrue( customRepresentation instanceof BasicType );
			Assert.assertEquals( customRepresentation, 
			                     model.getDatatype("UnsignedShort") );
			
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	@Test 
	public void testParseValidHla1516eDatatypesEnumerated()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/datatypes.xml");
			ObjectModel model = FOM.parseFOM( url );
			
			IDatatype datatype = model.getDatatype( "WaiterTasks" );
			Assert.assertNotNull( datatype );
			Assert.assertEquals( datatype.getDatatypeClass(), DatatypeClass.ENUMERATED );
			Assert.assertTrue( datatype instanceof EnumeratedType );
			
			EnumeratedType asEnumerated = (EnumeratedType)datatype;
			
			// Name
			Assert.assertEquals( datatype.getName(), "WaiterTasks" );
			
			// Representation
			IDatatype standardRepresentation = asEnumerated.getRepresentation();
			Assert.assertNotNull( standardRepresentation );
			Assert.assertEquals( standardRepresentation.getDatatypeClass(), DatatypeClass.BASIC );
			Assert.assertTrue( standardRepresentation instanceof BasicType );
			Assert.assertEquals( standardRepresentation, 
			                     model.getDatatype("HLAinteger32BE") );
			
			// Enumerators
			List<Enumerator> enumerators = asEnumerated.getEnumerators();
			Assert.assertNotNull( enumerators );
			Assert.assertEquals( enumerators.size(), 5 );
			
			Enumerator enum0 = enumerators.get( 0 );
			Assert.assertEquals( enum0.getName(), "TakingOrder" );
			Assert.assertEquals( enum0.getValue().intValue(), 1 );
			
			Enumerator enum1 = enumerators.get( 1 );
			Assert.assertEquals( enum1.getName(), "Serving" );
			Assert.assertEquals( enum1.getValue().intValue(), 2 );
			
			Enumerator enum2 = enumerators.get( 2 );
			Assert.assertEquals( enum2.getName(), "Cleaning" );
			Assert.assertEquals( enum2.getValue().intValue(), 3 );
			
			Enumerator enum3 = enumerators.get( 3 );
			Assert.assertEquals( enum3.getName(), "CalculatingBill" );
			Assert.assertEquals( enum3.getValue().intValue(), 4 );
			
			Enumerator enum4 = enumerators.get( 4 );
			Assert.assertEquals( enum4.getName(), "Other" );
			Assert.assertEquals( enum4.getValue().intValue(), 5 );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	@Test 
	public void testParseValidHla1516eDatatypesArray()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/datatypes.xml");
			ObjectModel model = FOM.parseFOM( url );
			
			//
			// Array with single dimension, fixed cardinality
			//
			IDatatype typeFixed = model.getDatatype( "FixedArray" );
			Assert.assertNotNull( typeFixed );
			Assert.assertEquals( typeFixed.getDatatypeClass(), DatatypeClass.ARRAY );
			Assert.assertTrue( typeFixed instanceof ArrayType );
			
			ArrayType arrayFixed = (ArrayType)typeFixed;
			
			// Name
			Assert.assertEquals( arrayFixed.getName(), "FixedArray" );
			
			// Datatype
			IDatatype fixedDatatype = arrayFixed.getDatatype();
			Assert.assertEquals( fixedDatatype, model.getDatatype("HLAbyte") );
			
			// Dimensions
			List<Dimension> fixedDimensions = arrayFixed.getDimensions();
			Assert.assertEquals( fixedDimensions.size(), 1 );
			Dimension fixedDimension = fixedDimensions.get( 0 );
			Assert.assertFalse( fixedDimension.isCardinalityDynamic() );
			Assert.assertEquals( fixedDimension.getCardinalityLowerBound(), 2 );
			Assert.assertEquals( fixedDimension.getCardinalityUpperBound(), 2 );
			
			// Single dimension cardinality access through ArrayType
			Assert.assertFalse( arrayFixed.isCardinalityDynamic() );
			Assert.assertEquals( arrayFixed.getCardinalityLowerBound(), 2 );
			Assert.assertEquals( arrayFixed.getCardinalityUpperBound(), 2 );
			
			//
			// Array with single dimension, dynamic cardinality
			//
			IDatatype typeDynamic = model.getDatatype( "DynamicArray" );
			Assert.assertNotNull( typeDynamic );
			Assert.assertEquals( typeDynamic.getDatatypeClass(), DatatypeClass.ARRAY );
			Assert.assertTrue( typeDynamic instanceof ArrayType );
			
			ArrayType arrayDynamic = (ArrayType)typeDynamic;
			
			// Name
			Assert.assertEquals( arrayDynamic.getName(), "DynamicArray" );
			
			// Datatype
			IDatatype dynamicDatatype = arrayDynamic.getDatatype();
			Assert.assertEquals( dynamicDatatype, model.getDatatype("HLAunicodeChar") );
			
			// Dimensions
			List<Dimension> dynamicDimensions = arrayDynamic.getDimensions();
			Assert.assertEquals( dynamicDimensions.size(), 1 );
			Dimension dynamicDimension = dynamicDimensions.get( 0 );
			Assert.assertTrue( dynamicDimension.isCardinalityDynamic() );
			Assert.assertEquals( dynamicDimension.getCardinalityLowerBound(), 
			                     Dimension.CARDINALITY_DYNAMIC );
			Assert.assertEquals( dynamicDimension.getCardinalityUpperBound(), 
			                     Dimension.CARDINALITY_DYNAMIC );
			
			// Single dimension cardinality access through ArrayType
			Assert.assertTrue( arrayDynamic.isCardinalityDynamic() );
			Assert.assertEquals( arrayDynamic.getCardinalityLowerBound(), 
			                     Dimension.CARDINALITY_DYNAMIC );
			Assert.assertEquals( arrayDynamic.getCardinalityUpperBound(), 
			                     Dimension.CARDINALITY_DYNAMIC );
			
			//
			// Array with single dimension, range cardinality
			//
			IDatatype typeRange = model.getDatatype( "RangeArray" );
			Assert.assertNotNull( typeRange );
			Assert.assertEquals( typeRange.getDatatypeClass(), DatatypeClass.ARRAY );
			Assert.assertTrue( typeRange instanceof ArrayType );
			
			ArrayType arrayRange = (ArrayType)typeRange;
			
			// Name
			Assert.assertEquals( arrayRange.getName(), "RangeArray" );
			
			// Datatype
			IDatatype rangeDatatype = arrayRange.getDatatype();
			Assert.assertEquals( rangeDatatype, model.getDatatype("HLAindex") );
			
			// Dimensions
			List<Dimension> rangeDimensions = arrayRange.getDimensions();
			Assert.assertEquals( rangeDimensions.size(), 1 );
			Dimension rangeDimension = rangeDimensions.get( 0 );
			Assert.assertFalse( rangeDimension.isCardinalityDynamic() );
			Assert.assertEquals( rangeDimension.getCardinalityLowerBound(), 2 );
			Assert.assertEquals( rangeDimension.getCardinalityUpperBound(), 5 );
			
			// Single dimension cardinality access through ArrayType
			Assert.assertFalse( arrayRange.isCardinalityDynamic() );
			Assert.assertEquals( arrayRange.getCardinalityLowerBound(), 2 );
			Assert.assertEquals( arrayRange.getCardinalityUpperBound(), 5 );
			
			//
			// Array with multiple dimensions
			//
			IDatatype typeMulti = model.getDatatype( "MultiDimensionArray" );
			Assert.assertNotNull( typeMulti );
			Assert.assertEquals( typeMulti.getDatatypeClass(), DatatypeClass.ARRAY );
			Assert.assertTrue( typeMulti instanceof ArrayType );
			
			ArrayType arrayMulti = (ArrayType)typeMulti;
			
			// Name
			Assert.assertEquals( arrayMulti.getName(), "MultiDimensionArray" );
			
			// Datatype
			IDatatype multiDatatype = arrayMulti.getDatatype();
			Assert.assertEquals( multiDatatype, model.getDatatype("HLAoctetPairBE") );
			
			// Dimensions
			List<Dimension> multiDimensions = arrayMulti.getDimensions();
			Assert.assertEquals( multiDimensions.size(), 3 );
			
			Dimension multiDimension0 = multiDimensions.get( 0 );
			Assert.assertFalse( multiDimension0.isCardinalityDynamic() );
			Assert.assertEquals( multiDimension0.getCardinalityLowerBound(), 4 );
			Assert.assertEquals( multiDimension0.getCardinalityUpperBound(), 4 );
			
			Dimension multiDimension1 = multiDimensions.get( 1 );
			Assert.assertFalse( multiDimension1.isCardinalityDynamic() );
			Assert.assertEquals( multiDimension1.getCardinalityLowerBound(), 1 );
			Assert.assertEquals( multiDimension1.getCardinalityUpperBound(), 4 );
			
			Dimension multiDimension2 = multiDimensions.get( 2 );
			Assert.assertTrue( multiDimension2.isCardinalityDynamic() );
			Assert.assertEquals( multiDimension2.getCardinalityLowerBound(), 
			                     Dimension.CARDINALITY_DYNAMIC );
			Assert.assertEquals( multiDimension2.getCardinalityUpperBound(), 
			                     Dimension.CARDINALITY_DYNAMIC );
			
			//
			// Array that references a type not yet declared at parse time
			//
			IDatatype typeForward = model.getDatatype( "ForwardDeclArray" );
			Assert.assertNotNull( typeForward );
			Assert.assertEquals( typeForward.getDatatypeClass(), DatatypeClass.ARRAY );
			Assert.assertTrue( typeForward instanceof ArrayType );
			
			ArrayType arrayForward = (ArrayType)typeForward;
			
			// Ensure that the array's datatype was resolved at link time
			Assert.assertEquals( arrayForward.getDatatype(), 
			                     model.getDatatype("FixedRecordExample") );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	@Test 
	public void testParseValidHla1516eDatatypesFixedRecord()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/datatypes.xml");
			ObjectModel model = FOM.parseFOM( url );
			
			//
			// Fixed Record with several fields
			//
			IDatatype typeFixed = model.getDatatype( "FixedRecordExample" );
			Assert.assertNotNull( typeFixed );
			Assert.assertEquals( typeFixed.getDatatypeClass(), DatatypeClass.FIXEDRECORD );
			Assert.assertTrue( typeFixed instanceof FixedRecordType );
			
			FixedRecordType fixedRecord = (FixedRecordType)typeFixed;
			
			// Name
			Assert.assertEquals( fixedRecord.getName(), "FixedRecordExample" );
			
			// Fields
			List<Field> fixedFields = fixedRecord.getFields();
			Assert.assertEquals( fixedFields.size(), 2 );
			
			Field field0 = fixedFields.get( 0 );
			Assert.assertEquals( field0.getName(), "FirstField" );
			Assert.assertEquals( field0.getDatatype(), model.getDatatype("DynamicArray") );
			
			Field field1 = fixedFields.get( 1 );
			Assert.assertEquals( field1.getName(), "SecondField" );
			Assert.assertEquals( field1.getDatatype(), model.getDatatype("WaiterTasks") );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	@Test 
	public void testParseValidHla1516eDatatypesVariantRecord()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/datatypes.xml");
			ObjectModel model = FOM.parseFOM( url );
			
			//
			// Variant Record with several alternatives
			//
			IDatatype typeVariant = model.getDatatype( "VariantRecordExample" );
			Assert.assertNotNull( typeVariant );
			Assert.assertEquals( typeVariant.getDatatypeClass(), DatatypeClass.VARIANTRECORD );
			Assert.assertTrue( typeVariant instanceof VariantRecordType );
			
			VariantRecordType variantRecord = (VariantRecordType)typeVariant;
			
			// Name
			Assert.assertEquals( variantRecord.getName(), "VariantRecordExample" );
			
			// Discriminant Name
			Assert.assertEquals( variantRecord.getDiscriminantName(), "ValIndex" );
			
			// Discriminant Datatype
			IDatatype discriminantType = variantRecord.getDiscriminantDatatype(); 
			Assert.assertEquals( discriminantType, 
			                     model.getDatatype("WaiterTasks") );
			Assert.assertEquals( discriminantType.getDatatypeClass(), DatatypeClass.ENUMERATED );
			Assert.assertTrue( discriminantType instanceof EnumeratedType );
			List<Enumerator> discriminantEnumerators = 
				((EnumeratedType)discriminantType).getEnumerators();
			
			// Alternatives
			Set<Alternative> alternatives = variantRecord.getAlternatives();
			Assert.assertEquals( alternatives.size(), 3 );
			
			for( Alternative alternative : alternatives )
			{
				String altName = alternative.getName();
				IDatatype altDatatype = alternative.getDatatype();
				
				Set<IEnumerator> altEnumerators = alternative.getEnumerators();
				if( altName.equals("AlternativeOne") )
				{
					Assert.assertEquals( altDatatype, model.getDatatype("HLAboolean") );
					Assert.assertEquals( altEnumerators.size(), 1 );
					Assert.assertTrue( altEnumerators.contains(discriminantEnumerators.get(0)) );
				}
				else if( altName.equals("AlternativeTwo") )
				{
					Assert.assertEquals( altDatatype, model.getDatatype("DynamicArray") );
					Assert.assertEquals( altEnumerators.size(), 3 );
					Assert.assertTrue( altEnumerators.contains(discriminantEnumerators.get(1)) );
					Assert.assertTrue( altEnumerators.contains(discriminantEnumerators.get(2)) );
					Assert.assertTrue( altEnumerators.contains(discriminantEnumerators.get(3)) );
				}
				else if( altName.equals("Other") )
				{
					Assert.assertEquals( altDatatype, model.getDatatype("HLAtoken") );
					Assert.assertEquals( altEnumerators.size(), 1 );
					Assert.assertTrue( altEnumerators.contains(Enumerator.HLA_OTHER) );
				}
				else
				{
					Assert.fail( "Unknown alternative name" );
				}
			}
			
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: " + e.getMessage(), e );
		}
	}
	
	@Test 
	public void testParseValidHla1516eObjectAttributeDatatypes()
	{
		try
		{
			URL url = ClassLoader.getSystemResource( "fom/ieee1516e/datatypes/datatypes.xml" );
			ObjectModel model = FOM.parseFOM( url );
			
			int objectHandle = model.getObjectClassHandle( "ObjectOne" );
			
			// Attribute One should be standard type HLAinteger32BE
			ACMetadata attributeOne = model.getAttributeClass( objectHandle, "AttributeOne" );
			IDatatype attributeOneType = attributeOne.getDatatype();
			Assert.assertNotNull( attributeOneType );
			Assert.assertEquals( attributeOneType.getDatatypeClass(), DatatypeClass.BASIC );
			Assert.assertTrue( attributeOneType instanceof BasicType );
			Assert.assertEquals( attributeOneType.getName(), "HLAinteger32BE" );
			
			// Attribute Two should be custom type FixedRecordExample
			ACMetadata attributeTwo = model.getAttributeClass( objectHandle, "AttributeTwo" );
			IDatatype attributeTwoType = attributeTwo.getDatatype();
			Assert.assertNotNull( attributeTwoType );
			Assert.assertEquals( attributeTwoType.getDatatypeClass(), DatatypeClass.FIXEDRECORD );
			Assert.assertTrue( attributeTwoType instanceof FixedRecordType );
			Assert.assertEquals( attributeTwoType.getName(), "FixedRecordExample" );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: "+e.getMessage(), e );
		}
	}
	
	@Test 
	public void testParseValidHla1516eInteractionParameterDatatypes()
	{
		try
		{
			URL url = ClassLoader.getSystemResource( "fom/ieee1516e/datatypes/datatypes.xml" );
			ObjectModel model = FOM.parseFOM( url );
			
			ICMetadata interaction = model.getInteractionClass( "InteractionOne" );
			
			// Parameter One should be standard type HLAobjectClassBasedCount
			PCMetadata parameterOne = interaction.getDeclaredParameter( "ParameterOne" );
			IDatatype parameterOneType = parameterOne.getDatatype();
			Assert.assertNotNull( parameterOneType );
			Assert.assertEquals( parameterOneType.getDatatypeClass(), DatatypeClass.FIXEDRECORD );
			Assert.assertTrue( parameterOneType instanceof FixedRecordType );
			Assert.assertEquals( parameterOneType.getName(), "HLAobjectClassBasedCount" );
			
			// Parameter Two should be custom type ForwardDeclArray
			PCMetadata parameterTwo = interaction.getDeclaredParameter( "ParameterTwo" );
			IDatatype parameterTwoType = parameterTwo.getDatatype();
			Assert.assertNotNull( parameterTwoType );
			Assert.assertEquals( parameterTwoType.getDatatypeClass(), DatatypeClass.ARRAY );
			Assert.assertTrue( parameterTwoType instanceof ArrayType );
			Assert.assertEquals( parameterTwoType.getName(), "ForwardDeclArray" );
		}
		catch( Exception e )
		{
			Assert.fail( "Unexpected exception parsing valid FOM: "+e.getMessage(), e );
		}
	}
	
	/**
	 * Parse a valid FOM that doesn't contain spaces
	 */
//	@Test
//	public void testParseValidHla13FomWithoutSpaces()
//	{
//		// testfom-nospaces.fed
//		try
//		{
//			FOM.parseFOM( this.validWithoutSpaces );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Unexpected exception parsing valid FOM (no spaces): "+e.getMessage(), e );
//		}
//	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// Invalid FOM Test Methods /////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parse the XML FOM file that has the wrong number of parenthesis
	 */
//	@Test
//	public void testParseInvalidHla13FomWithUnbalancedParenthesis()
//	{
//		// testfom-unbalanced.fed
//		try
//		{
//			FOM.parseFOM( this.invalidUnbalanced );
//			Assert.fail( "Was expecting exception parsing invalid fom (unbalanced parenthesis)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "line 6, column 3" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (unbalanced parenthesis)", e );
//		}
//	}
	
//	@Test
//	public void testParseInvalidHla13FomWithMalformedComment()
//	{
//		// testfom-badcomment.fed
//		try
//		{
//			FOM.parseFOM( this.invalidBadComment );
//			Assert.fail( "Was expecting exception parsing invalid fom (bad comment)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "Lexical error at line 9, column 4" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (bad comment)", e );
//		}
//	}
	
//	@Test
//	public void testParseInvalidHla13FomWithRandomText()
//	{
//		// testfom-randomtext.fed
//		try
//		{
//			FOM.parseFOM( this.invalidRandomText );
//			Assert.fail( "Was expecting exception parsing invalid fom (random text)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "Encountered \"RANDOM\" at line 9, column 3" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (random text)", e );
//		}
//	}
	
//	@Test
//	public void testParseInvalidHla13FomWithUndefinedSpaceForAttribute()
//	{
//		// testfom-undefinedAttributeSpace.fed
//		try
//		{
//			FOM.parseFOM( this.invalidAttBadSpace );
//			Assert.fail( "Was expecting exception parsing invalid fom (undefined space - att)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "uses undefined space \"NoSuchSpace\"" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (undefined space - att)", e );
//		}
//	}
	
	@Test
	public void testParseInvalidHla1516eFomWithUndefinedTransportForAttribute()
	{
		// testfom-undefinedAttributeTransport.fed
		try
		{
			FOM.parseFOM( this.invalidAttBadTransport );

			// NOTE Made the 1516e parser more tolerant to FOM errors by allowing
			//      it to accept missing order children and apply defaults. As such,
			//      this should not fail. Need to add control over strict/non-strict parsing
			//Assert.fail( "Was expecting exception parsing invalid fom (undefined transport - attribute)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains("<transportation>"),
			            "Exception flagged wrong error: "+error.getMessage() );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined transport - att)", e );
		}
	}
	
	@Test
	public void testParseInvalidHla1516eFomWithUndefinedOrderForAttribute()
	{
		// testfom-undefinedAttributeOrder.fed
		try
		{
			FOM.parseFOM( this.invalidAttBadOrder );

			// NOTE Made the 1516e parser more tolerant to FOM errors by allowing
			//      it to accept missing order children and apply defaults. As such,
			//      this should not fail. Need to add control over strict/non-strict parsing
			//Assert.fail( "Was expecting exception parsing invalid fom (undefined order - attribute)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains("<order>"), "Exception flagged wrong error" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined order - att)", e );
		}
	}
	
//	@Test
//	public void testParseInvalidHla13FomWithUndefinedSpaceForInteraction()
//	{
//		// testfom-undefinedInteractionSpace.fed
//		try
//		{
//			FOM.parseFOM( this.invalidIntBadSpace );
//			Assert.fail( "Was expecting exception parsing invalid fom (undefined space - int)" );
//		}
//		catch( JErrorReadingFED error )
//		{
//			// success! (almost)
//			assertTrue( error.getMessage().contains( "uses undefined space: \"NoSuchSpace\"" ),
//			            "Exception occurred in unexpected location" );
//		}
//		catch( Exception e )
//		{
//			Assert.fail( "Wrong exception parsing invalid fom (undefined space - int)", e );
//		}
//	}
	
	@Test
	public void testParseInvalidHla1516eFomWithUndefinedTransportForInteraction()
	{
		// testfom-undefinedInteractionTransport.fed
		try
		{
			FOM.parseFOM( this.invalidIntBadTransport );

			// NOTE Made the 1516e parser more tolerant to FOM errors by allowing
			//      it to accept missing order children and apply defaults. As such,
			//      this should not fail. Need to add control over strict/non-strict parsing
			//Assert.fail( "Was expecting exception parsing invalid fom (undefined transport - interation)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains("<transportation>"),
			            "Exception flagged wrong error" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined transport - int)", e );
		}
	}
	
	@Test
	public void testParseInvalidHla1516eFomWithUndefinedOrderForInteraction()
	{
		// testfom-undefinedInteractionOrder.fed
		try
		{
			FOM.parseFOM( this.invalidIntBadOrder );
			
			// NOTE Made the 1516e parser more tolerant to FOM errors by allowing
			//      it to accept missing order children and apply defaults. As such,
			//      this should not fail. Need to add control over strict/non-strict parsing
			//Assert.fail( "Was expecting exception parsing invalid fom (undefined order - interation)" );
		}
		catch( JErrorReadingFED error )
		{
			// success! (almost)
			assertTrue( error.getMessage().contains("<order>"), "Exception flagged wrong error" );
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (undefined order - int)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithBasicDatatypeNameMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/basicNameMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (basicData missing name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (basicData missing name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithBasicDatatypeNameEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/basicNameEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (basicData empty name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (basicData empty name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithBasicDatatypeSizeMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/basicSizeMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (basicData missing size)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (basicData missing name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithBasicDatatypeSizeEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/basicSizeEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (basicData empty size)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (basicData empty size)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithBasicDatatypeSizeNonNumeric()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/basicSizeNonNumeric.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (basicData non-numeric size)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (basicData non-numeric size)", e );
		}
	}

	@Test 
	public void testParseInvalidHla1516eFomModuleWithBasicDatatypeSizeNegative()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/basicSizeNegative.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (basicData negative size)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (basicData negative size)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithSimpleDatatypeNameMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/simpleNameMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData missing name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData missing name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithSimpleDatatypeNameEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/simpleNameEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData empty name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData empty name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithSimpleDatatypeRepresentationMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/simpleRepresentationMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData missing representation)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData missing representation)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithSimpleDatatypeRepresentationEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/simpleRepresentationEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData empty representation)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData empty representation)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithSimpleDatatypeRepresentationDoesntExist()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/simpleRepresentationDoesntExist.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData representation doesn't exist)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData representation doesn't exist)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithSimpleDatatypeRepresentationNotBasicType()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/simpleRepresentationNotABasicType.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData representation not basicData)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData representation not basicData)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeNameMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayNameMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData missing name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData missing name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeNameEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayNameEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData empty name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData empty name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeDatatypeMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayDatatypeMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData missing datatype)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData missing datatype)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeDatatypeEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayDatatypeEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData empty datatype)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData empty datatype)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeDatatypeDoesntExist()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayDatatypeDoesntExist.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData datatype doesn't exist)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData datatype doesn't exist)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeCardinalityMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayCardinalityMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData missing cardinality)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData missing cardinality)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeCardinalityEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayCardinalityEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData empty cardinality)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData empty cardinality)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeCardinalityNonNumeric()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayCardinalityNonNumeric.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData non-numeric cardinality)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData non-numeric cardinality)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithArrayDatatypeCardinalityNegative()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/arrayCardinalityNegative.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (arrayData negative cardinality)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (arrayData negative cardinality)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeNameMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedNameMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData missing name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData missing name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeNameEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedNameEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData empty name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData empty name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeRepresentationMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedRepresentationMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData missing representation)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData missing representation)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeRepresentationEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedRepresentationEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData empty representation)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData empty representation)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeRepresentationDoesntExist()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedRepresentationDoesntExist.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData representation doesn't exist)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData representation doesn't exist)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeRepresentationNotBasicType()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedRepresentationNotABasicType.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData representation not basicData)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData representation not basicData)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeNoEnumerators()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedNoEnumerators.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData missing enumerators)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData missing enumerators)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeEnumeratorNameMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedEnumeratorNameMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData missing enumerator name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData missing enumerator name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeEnumeratorNameEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedEnumeratorNameEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (enumeratedData empty enumerator name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (enumeratedData empty enumerator name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeEnumeratorValueMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedEnumeratorValueMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData missing enumerator value)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData missing enumerator value)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeEnumeratorValueEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedEnumeratorValueEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData empty enumerator value)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData empty enumerator value)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithEnumeratedDatatypeEnumeratorValueNonNumeric()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/enumeratedEnumeratorValueNonNumeric.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (simpleData non-numeric enumerator value)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (simpleData non-numeric enumerator value)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithFixedRecordNameMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/fixedRecordNameMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (fixedRecord missing name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (fixedRecord missing name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithFixedRecordNameEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/fixedRecordNameEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (fixedRecord empty name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (fixedRecord empty name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithFixedRecordFieldNameMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/fixedRecordFieldNameMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (fixedRecord missing field name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (fixedRecord missing field name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithFixedRecordFieldNameEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/fixedRecordFieldNameEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (fixedRecord empty field name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (fixedRecord empty field name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithFixedRecordFieldDatatypeMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/fixedRecordFieldDatatypeMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (fixedRecord missing datatype name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (fixedRecord missing datatype name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithFixedRecordFieldDatatypeEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/fixedRecordFieldDatatypeEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (fixedRecord empty datatype name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (fixedRecord empty datatype name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithFixedRecordFieldDatatypeDoesntExist()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/fixedRecordFieldDatatypeDoesntExist.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (fixedRecord empty datatype doesn't exist)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (fixedRecord empty datatype doesn't exist)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithVariantRecordAlternativeEnumeratorMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/variantRecordAlternativeEnumeratorMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (variantRecord missing alternative enumerator)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (variantRecord missing alternative enumerator)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithVariantRecordAlternativeEnumeratorEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/variantRecordAlternativeEnumeratorEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (variantRecord empty alternative enumerator)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (variantRecord empty alternative enumerator)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithVariantRecordAlternativeEnumeratorDoesntExist()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/variantRecordAlternativeEnumeratorDoesntExist.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (variantRecord non-existant alternative enumerator)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (variantRecord non-existant alternative enumerator)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithVariantRecordAlternativeNameMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/variantRecordAlternativeNameMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (variantRecord missing alternative name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (variantRecord missing alternative name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithVariantRecordAlternativeNameEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/variantRecordAlternativeNameEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (variantRecord empty alternative name)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (variantRecord empty alternative name)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithVariantRecordAlternativeDatatypeMissing()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/variantRecordAlternativeDatatypeMissing.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (variantRecord missing alternative datatype)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (variantRecord alternative enumerator)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithVariantRecordAlternativeDatatypeEmpty()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/variantRecordAlternativeDatatypeEmpty.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (variantRecord empty alternative datatype)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (variantRecord empty alternative datatype)", e );
		}
	}
	
	@Test 
	public void testParseInvalidHla1516eFomModuleWithVariantRecordAlternativeDatatypeDoesntExist()
	{
		try
		{
			URL url = ClassLoader.getSystemResource("fom/ieee1516e/datatypes/variantRecordAlternativeDatatypeDoesntExist.xml");
			FOM.parseFOM( url );
			Assert.fail( "Was expecting exception parsing invalid fom (variantRecord non-existant alternative datatype)" );
		}
		catch( JErrorReadingFED error )
		{
			// Pass: expected this exception
		}
		catch( Exception e )
		{
			Assert.fail( "Wrong exception parsing invalid fom (variantRecord non-existant alternative datatype)", e );
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
