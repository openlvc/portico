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
import java.util.Set;

import org.portico.lrc.model.datatype.Alternative;
import org.portico.lrc.model.datatype.ArrayType;
import org.portico.lrc.model.datatype.BasicType;
import org.portico.lrc.model.datatype.DatatypeClass;
import org.portico.lrc.model.datatype.DatatypeHelpers;
import org.portico.lrc.model.datatype.EnumeratedType;
import org.portico.lrc.model.datatype.Enumerator;
import org.portico.lrc.model.datatype.Field;
import org.portico.lrc.model.datatype.FixedRecordType;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.lrc.model.datatype.IEnumerator;
import org.portico.lrc.model.datatype.SimpleType;
import org.portico.lrc.model.datatype.VariantRecordType;

/**
 * This class will take an {@link ObjectModel} and render it as a String (complete with proper
 * indentation and the like)
 */
public class StringRenderer
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
	 */
	public String renderFOM( ObjectModel model )
	{
		StringBuilder builder = new StringBuilder();
		log( "== Beginning Object Model Render ==", builder, 0 );
		log( "===================================", builder, 0 );
		log( "==        Routing Spaces         ==", builder, 0 );
		log( "===================================", builder, 0 );
		renderSpaces( model.getAllSpaces(), builder, 0 );

		log( "===================================", builder, 0 );
		log( "==        Object Classes         ==", builder, 0 );
		log( "===================================", builder, 0 );
		renderObject( model.getObjectRoot(), builder, 0 );
		
		log( "===================================", builder, 0 );
		log( "==      Interaction Classes      ==", builder, 0 );
		log( "===================================", builder, 0 );
		renderInteraction( model.getInteractionRoot(), builder, 0 );
		
		log( "===================================", builder, 0 );
		log( "==            Datatypes          ==", builder, 0 );
		log( "===================================", builder, 0 );
		renderDatatypes( model, builder, 0 );
		
		return builder.toString();
	}
	
	private void renderSpaces( Collection<Space> spaces, StringBuilder builder, int level )
	{
		for( Space space : spaces )
		{
    		// log the space name
    		String header = "-> (space): " + space.getName() + " (handle:" +space.getHandle()+ ")";
    		log( header, builder, level );
    		
    		// log each of the contained dimensions
    		int buffer = findDimensionBuffer( space.getDimensions() );
    		for( Dimension dimension : space.getDimensions() )
    		{
    			String name = dimension.getName();
    			String desc = "   (dimension): " + name + ", " + pad(buffer-name.length()) +
    			              "handle=" + dimension.getHandle();
    			
    			log( desc, builder, (level+1) );
    		}
		}
	}

	private void renderObject( OCMetadata clazz, StringBuilder builder, int level )
	{
		////////////////////////
		// log the class name //
		////////////////////////
		String header = "-> (object class): " + clazz.getLocalName() +
		                " (handle: " + clazz.getHandle() + ")";
		log( header, builder, level );
		
		////////////////////////
		// log the attributes //
		////////////////////////
		int buffer = findBuffer( clazz.getDeclaredAttributes() );
		for( ACMetadata attribute : clazz.getDeclaredAttributes() )
		{
			String name = attribute.getName();
			IDatatype datatype = attribute.getDatatype();
			String desc = "   (attribute): " + name + ", " + pad(buffer-name.length()) +
			              "handle=" + attribute.getHandle() + ", datatype=" + datatype.getName() +  
			              ", order=" + attribute.getOrder() +
			              ", transport=" + attribute.getTransport() + ", space=" +
			              attribute.getSpace();
			
			log( desc, builder, (level+1) );
		}
		
		// log all the subclasses
		for( OCMetadata subclass : clazz.getChildTypes() )
		{
			renderObject( subclass, builder, (level+1) );
		}
	}
	
	private void renderInteraction( ICMetadata clazz, StringBuilder builder, int level )
	{
		////////////////////////
		// log the class name //
		////////////////////////
		String header = "-> (interaction class): " + clazz.getLocalName() +
		                " (handle: " + clazz.getHandle() +
		                ", order=" + clazz.getOrder() +
		                ", transport=" + clazz.getTransport() +
		                ", space=" + clazz.getSpace() +
		                ")";
		log( header, builder, level );
		
		////////////////////////
		// log the attributes //
		////////////////////////
		int buffer = findParamBuffer( clazz.getDeclaredParameters() );
		for( PCMetadata parameter : clazz.getDeclaredParameters() )
		{
			String name = parameter.getName();
			IDatatype datatype = parameter.getDatatype();
			String desc = "   (parameter): " + name + ", " + pad(buffer-name.length()) +
			              "handle=" + parameter.getHandle() + ", datatype=" + datatype.getName();
			
			log( desc, builder, (level+1) );
		}
		
		// log all the subclasses
		for( ICMetadata subclass : clazz.getChildTypes() )
		{
			renderInteraction( subclass, builder, (level+1) );
		}
	}
	
	private void renderDatatypes( ObjectModel model, StringBuilder builder, int level )
	{
		List<IDatatype> types = new ArrayList<IDatatype>( model.getDatatypes() );
		Collections.sort( types, new DatatypeComparator() );
		
		for( IDatatype type : types )
		{
			DatatypeClass typeClass = type.getDatatypeClass();
			String desc = "-> (" + typeClass + "): " + type.getName();
			switch( type.getDatatypeClass() )
			{
				case BASIC:
				{
					BasicType asBasic = (BasicType)type;
					desc += " (size=" + asBasic.getSize() + "bit" +
					        ", endianness: " + asBasic.getEndianness() + ")";
					log( desc, builder, level );
					break;
				}
				case SIMPLE:
				{
					SimpleType asSimple = (SimpleType)type;
					desc += " (representation=" + asSimple.getRepresentation() + ")";
					log( desc, builder, level );
					break;
				}
				case ENUMERATED:
				{
					EnumeratedType asEnumerated = (EnumeratedType)type;
					desc += " (representation=" + asEnumerated.getRepresentation() + ")";
					log( desc, builder, level );
					
					List<Enumerator> enumerators = asEnumerated.getEnumerators();
					int buffer = findEnumeratorBuffer( enumerators );
					for( Enumerator enumerator : asEnumerated.getEnumerators() )
					{
						String name = enumerator.getName();
						String enumDesc = "-> (enumerator): " + name + ", " + pad(buffer-name.length());
						enumDesc += enumerator.getValue();
						log( enumDesc, builder, level + 1 );
					}
					
					break;
				}
				case ARRAY:
				{
					ArrayType asArray = (ArrayType)type;
					desc += " (datatype=" + asArray.getDatatype().toString() + ")";
					log( desc, builder, level );
					
					for( org.portico.lrc.model.datatype.Dimension dimension : asArray.getDimensions() )
						log( "-> (dimension): " + dimension, builder, level + 1 );
					break;
				}
				case FIXEDRECORD:
				{
					FixedRecordType asFixed = (FixedRecordType)type;
					log( desc, builder, level );
					List<Field> fields = asFixed.getFields();
					int buffer = findFieldBuffer( fields );
					for( Field field : fields )
					{
						String name = field.getName();
						String fieldDesc = "-> (field): " + name + ", " + pad(buffer-name.length());
						fieldDesc += "datatype=" + field.getDatatype();
						log( fieldDesc, builder, level +1 );
					}
					
					break;
				}
				case VARIANTRECORD:
				{
					VariantRecordType asVariant = (VariantRecordType)type;
					log( desc, builder, level );
					
					String discDesc = "-> (discriminant): " + asVariant.getDiscriminantName() +  
					                  ", datatype=" + asVariant.getDiscriminantDatatype(); 
					log( discDesc, builder, level +1 );
					
					
					List<Alternative> alternatives = 
						new ArrayList<Alternative>( asVariant.getAlternatives() );
					alternatives.sort( new AlternativeComparator() );
					for( Alternative alternative : alternatives )
					{
						String name = alternative.getName();
						int buffer = findAlternativeBuffer( alternatives );
						String alternativeDesc = "-> (alternative): " + name + ", " + pad(buffer-name.length());
						alternativeDesc += "datatype=" + alternative.getDatatype();
						alternativeDesc += ", enumerators=";
						
						List<IEnumerator> enumerators = 
							new ArrayList<IEnumerator>( alternative.getEnumerators() );
						enumerators.sort( new EnumeratorComparator() );
						alternativeDesc += enumerators.toString();
						
						log( alternativeDesc, builder, level + 2 );
					}
					break;
				}
			}
		}
	}
	
	private int findBuffer( Set<ACMetadata> attributes )
	{
		int longest = 0;
		for( ACMetadata attribute : attributes )
		{
			if( attribute.getName().length() > longest )
				longest = attribute.getName().length();
		}
		
		return longest;
	}
	
	private int findParamBuffer( Set<PCMetadata> parameters )
	{
		int longest = 0;
		for( PCMetadata parameter : parameters )
		{
			if( parameter.getName().length() > longest )
				longest = parameter.getName().length();
		}
		
		return longest;
	}
	
	private int findDimensionBuffer( Set<Dimension> dimensions )
	{
		int longest = 0;
		for( Dimension dimension : dimensions )
		{
			if( dimension.getName().length() > longest )
				longest = dimension.getName().length();
		}
		
		return longest;
	}

	private int findFieldBuffer( Collection<? extends Field> fields )
	{
		int longest = 0;
		for( Field field : fields )
			longest = Math.max( longest, field.getName().length() );
		
		return longest;
	}
	
	private int findEnumeratorBuffer( Collection<? extends Enumerator> enumerators )
	{
		int longest = 0;
		for( Enumerator enumerator : enumerators )
			longest = Math.max( longest, enumerator.getName().length() );
		
		return longest;
	}
	
	private int findAlternativeBuffer( Collection<? extends Alternative> alternatives )
	{
		int longest = 0;
		for( Alternative alternative : alternatives )
			longest = Math.max( longest, alternative.getName().length() );
		
		return longest;
	}
	
	private String pad( int value )
	{
		char[] chars = new char[value];
		for( int i = 0; i < value; i++ )
		{
			chars[i] = ' ';
		}
		
		return new String( chars );
	}
	
	private void log( String msg, StringBuilder builder, int level )
	{
		for( int i = 0; i < level; i++ )
		{
			builder.append( "    " );
		}

		builder.append( msg );
		builder.append( "\n" );
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

