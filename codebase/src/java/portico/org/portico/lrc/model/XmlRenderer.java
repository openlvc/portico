/*
 *   Copyright 2006 The Portico Project
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
package org.portico.lrc.model; 


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List; 

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.portico.lrc.model.datatype.Alternative;
import org.portico.lrc.model.datatype.ArrayType;
import org.portico.lrc.model.datatype.BasicType;
import org.portico.lrc.model.datatype.DatatypeClass;
import org.portico.lrc.model.datatype.DatatypeHelpers;
import org.portico.lrc.model.datatype.Dimension;
import org.portico.lrc.model.datatype.EnumeratedType;
import org.portico.lrc.model.datatype.Enumerator;
import org.portico.lrc.model.datatype.Field;
import org.portico.lrc.model.datatype.FixedRecordType;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.lrc.model.datatype.IEnumerator;
import org.portico.lrc.model.datatype.SimpleType;
import org.portico.lrc.model.datatype.VariantRecordType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
 

/**
 * This class will take an {@link ObjectModel} and render it as a String (complete with proper
 * indentation and the like)
 */
public class XmlRenderer
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
	/**
	 * Takes the given {@link ObjectModel} and converts it into a String. The String it multi-lined
	 * and displays all the information about the object/interaction/attribute/parameter classes
	 * contained within the model. Inheritence is displayed using indenting.
	 * @throws ParserConfigurationException 
	 */
	public Document renderFOM( ObjectModel model )  
	{		
		Document fomxml = null;		
		
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();  		
			fomxml = db.newDocument();			
		}
		catch(Exception e)
		{
			throw new IllegalStateException(e);
		}

		
		Element objectModel = fomxml.createElement( "objectModel" );
		fomxml.appendChild( objectModel );
		
		// Routing Spaces    
//		renderSpaces( model.getAllSpaces(), builder, 0 );

 		// Object Classes  
//		renderObject( model.getObjectRoot(), builder, 0 );

		// Interaction Classes
//		renderInteraction( model.getInteractionRoot(), builder, 0 );
		
		
		// Data types
		renderDatatypes( model, fomxml, objectModel);
					
		return fomxml;	
	}
	
	private void renderSpaces( Collection<Space> spaces, StringBuilder builder, int level )
	{
 
	}

	private void renderObject( OCMetadata clazz, StringBuilder builder, int level )
	{
 
	}
	
	private void renderInteraction( ICMetadata clazz, StringBuilder builder, int level )
	{
 
	}
	
	private void renderDatatypes( ObjectModel model, Document doc, Element objectModel)
	{
		// Create elements
		Element datatype = doc.createElement( "dataTypes" );
		Element basicDataRepresentations = doc.createElement( "basicDataRepresentations" );
		Element simpleDataTypes = doc.createElement( "simpleDataTypes" );
		Element enumeratedDataTypes = doc.createElement( "enumeratedDataTypes" );
		Element arrayDataTypes = doc.createElement( "arrayDataTypes" );
		Element fixedRecordDataTypes = doc.createElement( "fixedRecordDataTypes" );
		Element variantRecordDataTypes = doc.createElement( "variantRecordDataTypes" );
		
		// add elements to xml docs
		objectModel.appendChild( datatype );			
		datatype.appendChild( basicDataRepresentations );
		datatype.appendChild( simpleDataTypes );
		datatype.appendChild( enumeratedDataTypes );
		datatype.appendChild( arrayDataTypes );
		datatype.appendChild( fixedRecordDataTypes );
		datatype.appendChild( variantRecordDataTypes );
				
		
		List<IDatatype> types = new ArrayList<IDatatype>( model.getDatatypes() );
		Collections.sort( types, new DatatypeComparator() );
		
		for( IDatatype type : types )
		{
			DatatypeClass typeClass = type.getDatatypeClass();
			
			 
			switch( type.getDatatypeClass() )
			{
				case BASIC:
				{
					// Get the data type
					BasicType asBasic = (BasicType)type;
					
					// Create the xml element and attributes
					Element basicDatatype = doc.createElement( "basicData" );
					basicDatatype.setAttribute( "name", asBasic.getName() );
					basicDatatype.setAttribute( "size",  Integer.toString( asBasic.getSize() ));
					basicDatatype.setAttribute( "endianness", asBasic.getEndianness().toString() );
					
					// Add it to the parent node
					basicDataRepresentations.appendChild( basicDatatype );
					break;
				}
				case SIMPLE:
				{
					// Get the data type
					SimpleType asSimple = (SimpleType)type;
					
					// Create the xml element and attributes
					Element simpleDatatype = doc.createElement( "simpleData" ); 
					simpleDatatype.setAttribute( "name", asSimple.getName() );
					simpleDatatype.setAttribute( "representation",  asSimple.getRepresentation().toString() );

					// Add it to the parent node
					simpleDataTypes.appendChild( simpleDatatype );
					break;
				}
				case ENUMERATED:
				{
					EnumeratedType asEnumerated = (EnumeratedType)type;
					
					// Create the xml element and attributes
					Element enumeratedData = doc.createElement( "enumeratedData" );  
					enumeratedData.setAttribute( "name",  asEnumerated.getName() );
					enumeratedData.setAttribute( "representation",  asEnumerated.getRepresentation().toString() );
									 
					// Get each enumerator associated with this enumerator type.
					List<Enumerator> enumerators = asEnumerated.getEnumerators();
					for( Enumerator enumerator : asEnumerated.getEnumerators() )
					{
						Element enumeratorElement  = doc.createElement( "enumerator" ); 
						enumeratorElement.setAttribute( "name", enumerator.getName());
						enumeratorElement.setAttribute( "values",  enumerator.getValue().toString() );
						enumeratedData.appendChild( enumeratorElement );
					}
					
					// Add it to the parent node
					enumeratedDataTypes.appendChild( enumeratedData  );
					break;
				}
				case ARRAY:
				{
					ArrayType asArray = (ArrayType)type;
					
					// Create the xml element and attributes
					Element arrayData = doc.createElement( "arrayData" ); 
					arrayData.setAttribute( "name", asArray.getName());
					arrayData.setAttribute( "dataType",  asArray.getDatatype().toString() );
				 						
					String dimensionString = "";
					
					for( org.portico.lrc.model.datatype.Dimension dimension : asArray.getDimensions() )
					{					
						// Create the enumerator element
						Element dimensionElement = doc.createElement( "cardinality" ); 
						dimensionElement.setTextContent( Dimension.toFomString( dimension ) ); 
						arrayData.appendChild( dimensionElement );
						
					}
									 
					// Add it to the parent node
					arrayDataTypes.appendChild( arrayData  );
					break;
				}
				case FIXEDRECORD:
				{
					FixedRecordType asFixed = (FixedRecordType)type;
					
					// Create the xml element and attributes
					Element fixedRecordData = doc.createElement( "fixedRecordData" ); 
					fixedRecordData.setAttribute( "name", asFixed.getName());
					
					// Get each enumerator associated with this enumerator type.
					List<Field> fields = asFixed.getFields(); 
					for( Field field : fields )
					{
						Element fieldElement  = doc.createElement( "field" ); 
						fieldElement.setAttribute( "name", field.getName());
						fieldElement.setAttribute( "dataType", field.getDatatype().toString());								 
						fixedRecordData.appendChild( fieldElement );
					}
					
					// Add it to the parent node
					fixedRecordDataTypes.appendChild( fixedRecordData  );
					break;
				}
				case VARIANTRECORD:
				{
					VariantRecordType asVariant = (VariantRecordType)type;
			 
					// Create the xml element and attributes
					Element variantRecordType = doc.createElement( "variantRecordData" ); 
					variantRecordType.setAttribute( "name", asVariant.getName());
					variantRecordType.setAttribute( "discriminant", asVariant.getDiscriminantName());
					variantRecordType.setAttribute( "dataType", asVariant.getDiscriminantDatatype().toString());
					
					
					List<Alternative> alternatives = new ArrayList<Alternative>( asVariant.getAlternatives() );
					alternatives.sort( new AlternativeComparator() );
					for( Alternative alternative : alternatives )
					{
						
						// Create the xml element and attributes for alternative
						Element alternateElement = doc.createElement( "alternative" ); 
						alternateElement.setAttribute( "name", alternative.getName());
						alternateElement.setAttribute( "dataType", alternative.getDatatype().toString());						
						
						// Create each enumerator value entry
						List<IEnumerator> enumerators = new ArrayList<IEnumerator>( alternative.getEnumerators() );
						enumerators.sort( new EnumeratorComparator() );	
						
						for(IEnumerator enumeratorEntry: enumerators)
						{
							// Create the enumerator element
							Element enumeratorElement = doc.createElement( "enumerator" ); 
							enumeratorElement.setTextContent( enumeratorEntry.toString() ); 
							alternateElement.appendChild( enumeratorElement );
						}									
						
						//Add the alternative entry to the element						
						variantRecordType.appendChild( alternateElement );
					}					
					
					// Add it to the parent node
					variantRecordDataTypes.appendChild( variantRecordType  );
					break;
				}
			}
		} 
	}

	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	private static class DatatypeComparator implements Comparator<IDatatype>
	{
		@Override
		public int compare( IDatatype o1, IDatatype o2 )
		{
			int result = o1.getDatatypeClass().compareTo( o2.getDatatypeClass() );
			if( result == 0 )
				result = o1.getName().compareTo( o2.getName() );
			
			return result;
		}		
	}
	
	private static class EnumeratorComparator implements Comparator<IEnumerator>
	{
		@Override
		public int compare( IEnumerator o1, IEnumerator o2 )
		{
			long o1Value = o1.getValue().longValue();
			long o2Value = o2.getValue().longValue();
			
			if( o1Value == o2Value )
				return 0;
			else if( o1Value > o2Value )
				return 1;
			else
				return -1;
		}
		
	}
	
	private static class AlternativeComparator implements Comparator<Alternative>
	{
		private EnumeratorComparator enumCompare;
		
		public AlternativeComparator()
		{
			this.enumCompare = new EnumeratorComparator();
		}
		
		@Override
		public int compare( Alternative o1, Alternative o2 )
		{
			IEnumerator o1Lowest = DatatypeHelpers.getLowestEnumerator( o1.getEnumerators() );
			IEnumerator o2Lowest = DatatypeHelpers.getLowestEnumerator( o2.getEnumerators() );
			
			return enumCompare.compare( o1Lowest, o2Lowest );
		}
		
	}
}

