/*
 *   Copyright 2017 The Portico Project
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
package org.portico.lrc.model.datatype;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.portico.lrc.compat.JInconsistentFDD;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;

/**
 * A utility class containing datatype methods that are used throughout Portico
 */
public class DatatypeHelpers
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

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Injects the MIM standard types into the provided {@link ObjectModel}
	 */
	public static void injectStandardDatatypes( ObjectModel model )
	{
		BasicType hlaInt32Be = new BasicType( "HLAinteger32BE", 32, Endianness.BIG );
		BasicType hlaInt64Be = new BasicType( "HLAinteger64BE", 64, Endianness.BIG );
		BasicType hlaFloat64Be = new BasicType( "HLAfloat64BE", 64, Endianness.BIG );
		BasicType hlaOctetPairBe = new BasicType( "HLAoctetPairBE", 16, Endianness.BIG );
		BasicType hlaOctet = new BasicType( "HLAoctet", 8, Endianness.BIG );
		IDatatype hlaAsciiChar = new SimpleType( "HLAASCIIchar", hlaOctet );
		IDatatype hlaUnicodeChar = new SimpleType( "HLAunicodeChar", hlaOctetPairBe );
		IDatatype hlaByte = new SimpleType( "HLAbyte", hlaOctet );
		IDatatype hlaCount = new SimpleType( "HLAcount", hlaInt32Be );
		IDatatype hlaBoolean = new EnumeratedType( "HLAboolean", 
		                                           hlaInt32Be, 
		                                           "HLAfalse", 
		                                           "HLAtrue" );
		IDatatype hlaSynchPointStatus = new EnumeratedType( "HLAsynchPointStatus", 
		                                                    hlaInt32Be,
		                                                    "NoActivity",
		                                                    "AttemptingToRegisterSynchPoint",
		                                                    "MovingToSynchPoint",
		                                                    "WaitingForRestOfFederation" );
		IDatatype hlaHandle = new ArrayType( "HLAhandle", hlaByte );
		IDatatype hlaUnicodeString = new ArrayType( "HLAunicodeString", hlaUnicodeChar );
		IDatatype hlaInteractionSubscription = 
			new FixedRecordType( "HLAinteractionSubscription", 
		                         new Field("HLAinteractionClass", hlaHandle),
		                         new Field("HLAactive", hlaBoolean) );
		IDatatype hlaObjectClassBasedCount = 
			new FixedRecordType( "HLAobjectClassBasedCount", 
		                         new Field("HLAobjectClass", hlaHandle),
		                         new Field("HLAcount", hlaCount) );
		IDatatype hlaInteractionCount = 
			new FixedRecordType( "HLAinteractionCount", 
		                         new Field("HLAinteractionClass", hlaHandle),
		                         new Field("HLAinteractionCount", hlaCount) );
		IDatatype hlaSynchPointFederate = 
			new FixedRecordType( "HLAsynchPointFederate", 
		                         new Field("HLAfederate", hlaHandle),
		                         new Field("HLAfederateSynchStatus", hlaSynchPointStatus) );
		
		model.addDatatype( NaType.INSTANCE );
		
		//
		// Basic Types
		//
		model.addDatatype( new BasicType("HLAinteger16BE", 16, Endianness.BIG) );
		model.addDatatype( hlaInt32Be );
		model.addDatatype( hlaInt64Be );
		model.addDatatype( new BasicType("HLAfloat32BE", 32, Endianness.BIG) );
		model.addDatatype( hlaFloat64Be );
		model.addDatatype( hlaOctetPairBe );
		
		model.addDatatype( new BasicType("HLAinteger16LE", 16, Endianness.LITTLE) );
		model.addDatatype( new BasicType("HLAinteger32LE", 32, Endianness.LITTLE) );
		model.addDatatype( new BasicType("HLAinteger64LE", 64, Endianness.LITTLE) );
		model.addDatatype( new BasicType("HLAfloat32LE", 32, Endianness.LITTLE) );
		model.addDatatype( new BasicType("HLAfloat64LE", 64, Endianness.LITTLE) );
		model.addDatatype( new BasicType("HLAoctetPairLE", 16, Endianness.LITTLE) );
		
		model.addDatatype( hlaOctet );
		
		//
		// Simple Types
		//
		
		model.addDatatype( hlaAsciiChar );
		model.addDatatype( hlaUnicodeChar );
		model.addDatatype( hlaByte );
		model.addDatatype( hlaCount );
		model.addDatatype( new SimpleType("HLAseconds", hlaInt32Be) );
		model.addDatatype( new SimpleType("HLAmsec", hlaInt32Be) );
		model.addDatatype( new SimpleType("HLAnormalizedFederateHandle", hlaInt32Be) );
		model.addDatatype( new SimpleType("HLAindex", hlaInt32Be) );
		model.addDatatype( new SimpleType("HLAinteger64Time", hlaInt64Be) );
		model.addDatatype( new SimpleType("HLAfloat64Time", hlaFloat64Be) );
		
		//
		// Enumerated Types
		//
		model.addDatatype( hlaBoolean );
		model.addDatatype( new EnumeratedType("HLAfederateState", 
		                                      hlaInt32Be,
		                                      new Enumerator("ActiveFederate", 1),
		                                      new Enumerator("FederateSaveInProgress", 3),
		                                      new Enumerator("FederateRestoreInProgress", 5)) );
		model.addDatatype( new EnumeratedType("HLAtimeState", 
		                                      hlaInt32Be,
		                                      "TimeGranted",
		                                      "TimeAdvancing") );
		model.addDatatype( new EnumeratedType("HLAownership", 
		                                      hlaInt32Be,
		                                      "Unowned",
		                                      "Owned") );
		model.addDatatype( new EnumeratedType("HLAresignAction", 
		                                      hlaInt32Be,
		                                      new Enumerator("DivestOwnership", 1),
		                                      new Enumerator("DeleteObjectInstances",2),
		                                      new Enumerator("CancelPendingAcquisitions",3),
		                                      new Enumerator("DeleteObjectInstancesThenDivestOwnership",4),
		                                      new Enumerator("CancelPendingAcquisitionsThenDeleteObjectInstancesThenDivestOwnership",5),
		                                      new Enumerator("NoAction",6)) );
		model.addDatatype( new EnumeratedType("HLAorderType", 
		                                      hlaInt32Be,
		                                      "Receive",
		                                      "TimeStamp") );
		model.addDatatype( new EnumeratedType("HLAswitch", 
		                                      hlaInt32Be,
		                                      "Disabled",
		                                      "Enabled") );
		model.addDatatype( hlaSynchPointStatus );
		model.addDatatype( new EnumeratedType("HLAnormalizedServiceGroup", 
		                                      hlaInt32Be,
		                                      "FederationManagement",
		                                      "DeclarationManagement",
		                                      "ObjectManagement",
		                                      "OwnershipManagement",
		                                      "TimeManagement",
		                                      "DataDistributionManagement",
		                                      "SupportServices") );
		
		//
		// Array Types
		//
		model.addDatatype( new ArrayType("HLAASCIIstring", hlaAsciiChar) );
		model.addDatatype( hlaUnicodeString );
		model.addDatatype( new ArrayType("HLAopaqueData", hlaByte) );
		model.addDatatype( new ArrayType("HLAtoken", hlaByte, 0) );
		model.addDatatype( hlaHandle );
		model.addDatatype( new ArrayType("HLAtransportationName", hlaUnicodeChar) );
		model.addDatatype( new ArrayType("HLAupdateRateName", hlaUnicodeChar) );
		model.addDatatype( new ArrayType("HLAlogicalTime", hlaByte) );
		model.addDatatype( new ArrayType("HLAtimeInterval", hlaByte) );
		model.addDatatype( new ArrayType("HLAhandleList", hlaHandle) );
		model.addDatatype( new ArrayType("HLAinteractionSubList", hlaInteractionSubscription) );
		model.addDatatype( new ArrayType("HLAargumentList", hlaUnicodeString) );
		model.addDatatype( new ArrayType("HLAobjectClassBasedCounts", hlaObjectClassBasedCount) );
		model.addDatatype( new ArrayType("HLAinteractionCounts", hlaInteractionCount) );
		model.addDatatype( new ArrayType("HLAsynchPointList", hlaUnicodeString) );
		model.addDatatype( new ArrayType("HLAsynchPointFederateList", hlaSynchPointFederate) );
		model.addDatatype( new ArrayType("HLAmoduleDesignatorList", hlaUnicodeString) );
		
		//
		// Fixed Record Types
		//
		model.addDatatype( hlaInteractionSubscription );
		model.addDatatype( hlaObjectClassBasedCount );
		model.addDatatype( hlaInteractionCount );
		model.addDatatype( hlaSynchPointFederate );
	}
	
	/**
	 * Checks to see if both datatypes are equivalent, issuing a warning through the provided 
	 * logger if they are not.
	 * <p/>
	 * This method is provided for the {@link ModelMerger} as it attempts to merge an extension
	 * FOM with a base FOM.
	 * <p/>
	 * If the types are not equivalent, a {@link JInconsistentFDD} exception is thrown detailing
	 * the reason why
	 */
	public static void validateEquivalent( IDatatype base, IDatatype extension )
		throws JInconsistentFDD
	{
		DatatypeClass baseClass = base.getDatatypeClass();
		DatatypeClass extensionClass = extension.getDatatypeClass();
		
		if( baseClass == extensionClass )
		{
			switch( baseClass )
			{
				case BASIC:
					validateEquivalent( (BasicType)base, (BasicType)extension );
					break;
				case SIMPLE:
					validateEquivalent( (SimpleType)base, (SimpleType)extension );
					break;
				case ENUMERATED:
					validateEquivalent( (EnumeratedType)base, (EnumeratedType)extension );
					break;
				case ARRAY:
					validateEquivalent( (ArrayType)base, (ArrayType)extension );
					break;
				case FIXEDRECORD:
					validateEquivalent( (FixedRecordType)base, (FixedRecordType)extension );
					break;
				default:
				case VARIANTRECORD:
					validateEquivalent( (VariantRecordType)base, (VariantRecordType)extension );
					break;
				case NA:
					break;
			}
		}
		else
		{
			// Different datatype classes!
			throw new JInconsistentFDD( "Datatype classes differ (base="+baseClass+ 
			                            ", extension="+extensionClass+")" );
		}
	}
	
	private static void validateEquivalent( BasicType base, BasicType extension )
		throws JInconsistentFDD
	{
		if( base.getSize() != extension.getSize() )
		{
			throw new JInconsistentFDD( "Sizes differ (base="+base.getSize()+
			                            ", extension="+extension.getSize() );
		}
		
		if( base.getEndianness() != extension.getEndianness() )
		{
			throw new JInconsistentFDD( "Endinanness differs (base="+base.getEndianness()+
			                            ", extension="+extension.getEndianness()+")" );
		}
	}
	
	private static void validateEquivalent( SimpleType base, SimpleType extension )
		throws JInconsistentFDD
	{
		IDatatype baseRepresentation = base.getRepresentation();
		IDatatype extensionRepresentation = extension.getRepresentation();
		
		// Checking the representation name is good enough for this check. The actual basic types
		// themselves will have their equivalence checked in turn
		if( !Objects.equals(baseRepresentation.getName(), extensionRepresentation.getName()) )
		{
			throw new JInconsistentFDD( "Representation differs (base="+baseRepresentation.getName()+
			                            ", extension="+extensionRepresentation.getName()+")" );
		}
	}
	
	private static void validateEquivalent( EnumeratedType base, EnumeratedType extension )
		throws JInconsistentFDD
	{
		// Just checking equivalence of representation at the moment as values are not parsed in
		IDatatype baseRepresentation = base.getRepresentation();
		IDatatype extensionRepresentation = extension.getRepresentation();
		
		// Checking the representation name is good enough for this check. The actual basic types
		// themselves will have their equivalence checked in turn
		if( !Objects.equals(baseRepresentation.getName(), extensionRepresentation.getName()) )
		{
			throw new JInconsistentFDD( "Representation differs (base="+baseRepresentation.getName()+
			                            ", extension="+extensionRepresentation.getName()+")" );
		}
		
		List<Enumerator> baseEnumerators = base.getEnumerators();
		List<Enumerator> extensionEnumerators = extension.getEnumerators();
		if( baseEnumerators.size() != extensionEnumerators.size() )
		{
			throw new JInconsistentFDD( "Number of enumerators differs (base="+baseEnumerators.size()+
			                            ", extension="+extensionEnumerators.size()+")" );
		}
		
		// Enumerator equivalence
		for( int i = 0 ; i < baseEnumerators.size() ; ++i )
		{
			Enumerator baseEnumerator = baseEnumerators.get( i );
			Enumerator extensionEnumerator = extensionEnumerators.get( i );
			
			if( !baseEnumerator.getName().equals(extensionEnumerator.getName()) )
			{
				throw new JInconsistentFDD( "Enumerator "+i+
				                            " name differs (base="+baseEnumerator.getName()+
				                            ", extension="+extensionEnumerator.getName()+")" );
			}
			
			if( !baseEnumerator.getValue().equals(extensionEnumerator.getValue()) )
			{
				throw new JInconsistentFDD( "Enumerator "+i+
				                            " values differ (base="+baseEnumerator.getValue()+
				                            ", extension="+extensionEnumerator.getValue()+")" );
			}
		}
	}
	
	private static void validateEquivalent( ArrayType base, ArrayType extension )
		throws JInconsistentFDD
	{
		IDatatype baseDatatype = base.getDatatype();
		IDatatype extensionDatatype = extension.getDatatype();
		
		// Checking the data type name is good enough for this check. The actual data types
		// themselves will have their equivalence checked in turn
		if( !Objects.equals(baseDatatype.getName(), extensionDatatype.getName()) )
		{
			throw new JInconsistentFDD( "Datatype differs (base="+baseDatatype.getName()+
			                            ", extension="+extensionDatatype.getName()+")" );
		}
		
		List<Dimension> baseDimensions = base.getDimensions();
		List<Dimension> extensionDimensions = extension.getDimensions();
		
		// Number of dimensions
		if( baseDimensions.size() != extensionDimensions.size() )
		{
			throw new JInconsistentFDD( "Number of dimensions differ (base="+baseDimensions.size()+
			                            ", extension="+extensionDimensions.size()+")" );
		}
		
		// Dimensional equivalence
		for( int i = 0 ; i < baseDimensions.size() ; ++i )
		{
			Dimension baseDimension = baseDimensions.get( i );
			Dimension extensionDimension = extensionDimensions.get( i );
			boolean lowerBoundEquivalent = baseDimension.getCardinalityLowerBound() == 
			                               extensionDimension.getCardinalityLowerBound(); 
			boolean upperBoundEquivalent = baseDimension.getCardinalityUpperBound() ==
			                               extensionDimension.getCardinalityUpperBound();
			if( !lowerBoundEquivalent || !upperBoundEquivalent )
			{
				throw new JInconsistentFDD( "Dimension "+i+
				                            " cardinality differs (base="+baseDimension.toString()+
				                            ", extension="+extensionDimension.toString()+")" );
			}
		}
	}
	
	private static void validateEquivalent( FixedRecordType base, FixedRecordType extension )
		throws JInconsistentFDD
	{
		List<Field> baseFields = base.getFields();
		List<Field> extensionFields = extension.getFields();
		
		// Number of fields
		if( baseFields.size() != extensionFields.size() )
		{
			throw new JInconsistentFDD( "Number of fields differ (base="+baseFields.size()+
			                            ", extension="+extensionFields.size()+")" );
		}
		
		// Field equivalence
		for( int i = 0 ; i < baseFields.size() ; ++i )
		{
			Field baseField = baseFields.get( i );
			Field extensionField = extensionFields.get( i );
			
			if( !Objects.equals( baseField.getName(), extensionField.getName()) )
			{
				throw new JInconsistentFDD( "Field "+i+
				                            " name differs (base="+baseField.getName()+
				                            ", extension="+extensionField.getName()+")" );
			}
			
			IDatatype baseFieldDatatype = baseField.getDatatype();
			IDatatype extensionFieldDatatype = extensionField.getDatatype();
			if( !Objects.equals(baseFieldDatatype.getName(), extensionFieldDatatype.getName()) )
			{
				throw new JInconsistentFDD( "Field "+i+
				                            " datatype differs (base="+baseFieldDatatype.getName()+
				                            ", extension="+extensionFieldDatatype.getName()+")" );
			}
		}
	}
	
	private static void validateEquivalent( VariantRecordType base, VariantRecordType extension )
		throws JInconsistentFDD
	{
		if( Objects.equals(base.getDiscriminantName(), extension.getDiscriminantName()) )
		{
			throw new JInconsistentFDD( "Discriminant name differs (base="+base.getDiscriminantName()+
			                            ", extension="+extension.getDiscriminantName()+")" );
		}
		
		IDatatype baseDiscriminantType = base.getDiscriminantDatatype();
		IDatatype extensionDiscriminantType = extension.getDiscriminantDatatype();
		if( !Objects.equals(baseDiscriminantType.getName(), extensionDiscriminantType.getName()) )
		{
			throw new JInconsistentFDD( "Discriminant datatype differs (base="+baseDiscriminantType.getName()+
			                            ", extension="+extensionDiscriminantType.getName()+")" );
		}
		
		Set<Alternative> baseAlternatives = base.getAlternatives();
		Set<Alternative> extensionAlternatives = extension.getAlternatives();
		
		// Number of alternatives
		if( baseAlternatives.size() != extensionAlternatives.size() )
		{
			throw new JInconsistentFDD( "Number of alternatives differ (base="+baseAlternatives.size()+
			                            ", extension="+extensionAlternatives.size()+")" );
		}
		
		// Alternative Equivalence
		for( Alternative baseAlternative : baseAlternatives )
		{
			String baseAlternativeName = baseAlternative.getName();
			Alternative extensionAlternative = null;
			for( Alternative extensionCandinate : extensionAlternatives )
			{
				if( extensionCandinate.getName().equals(baseAlternativeName) )
				{
					extensionAlternative = extensionCandinate;
					break;
				}
			}
			
			if( extensionAlternative == null )
				throw new JInconsistentFDD( "Extension does not contain alternative "+baseAlternativeName );
			
			IDatatype baseAlternativeType = baseAlternative.getDatatype();
			IDatatype extensionAlternativeType = extensionAlternative.getDatatype();
			if( !baseAlternativeType.getName().equals(extensionAlternative.getName()) )
			{
				throw new JInconsistentFDD( "Alternative "+baseAlternativeName+
				                            " datatype differs (base="+baseAlternativeType.getName()+
				                            ", extension="+extensionAlternativeType.getName()+")" );
			}
			
			Set<IEnumerator> baseAlternativeEnums = baseAlternative.getEnumerators();
			Set<IEnumerator> extensionAlternativeEnums = extensionAlternative.getEnumerators();
			if( !baseAlternativeEnums.equals(extensionAlternativeEnums) )
			{
				throw new JInconsistentFDD( "Alternative "+baseAlternativeName+
				                            " enumerators differ (base="+baseAlternativeEnums.toString()+
				                            ", extension="+extensionAlternativeEnums.toString()+")" );
			}
		}
	}
	
	/**
	 * Returns the lowest value enumerator in the provided collection
	 * <p/>
	 * Enumerator values are interpreted as a {@link Number} which is able to represent all of the
	 * standard basic datatypes. 
	 */
	public static IEnumerator getLowestEnumerator( Collection<? extends IEnumerator> enumerators )
	{
		IEnumerator min = null;
		for( IEnumerator enumerator : enumerators )
		{
			if( min == null )
			{
				min = enumerator;
			}
			else
			{
				Number minValue = min.getValue().longValue();
				Number enumeratorValue = enumerator.getValue();
				min = minValue.longValue() < enumeratorValue.longValue() ? min : 
				                                                           enumerator;
			}
		}
		
		return min;
	}
}
