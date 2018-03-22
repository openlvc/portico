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
package org.portico.lrc.model.datatype.linker;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.model.ACMetadata;
import org.portico.lrc.model.PCMetadata;
import org.portico.lrc.model.datatype.Alternative;
import org.portico.lrc.model.datatype.ArrayType;
import org.portico.lrc.model.datatype.BasicType;
import org.portico.lrc.model.datatype.EnumeratedType;
import org.portico.lrc.model.datatype.Enumerator;
import org.portico.lrc.model.datatype.Field;
import org.portico.lrc.model.datatype.FixedRecordType;
import org.portico.lrc.model.datatype.IDatatype;
import org.portico.lrc.model.datatype.IEnumerator;
import org.portico.lrc.model.datatype.SimpleType;
import org.portico.lrc.model.datatype.VariantRecordType;

/**
 * This class is used during FOM import and Model Merging to resolve dependencies between datatypes
 * once all datatypes have been imported.
 * <p/>
 * When parsing a datatype from the FOM, there is no guarantee that any datatypes that it references
 * have been imported yet. Thus we insert a {@link DatatypePlaceholder} in its place and once all
 * datatypes have been imported all placeholders are resolved to their complete representations.
 * <p/>
 * To use the Linker, first add candidates to the pool of types that placeholders can be resolved to
 * by calling {@link #addCandidates(Set)}. 
 */
public class Linker
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Map<String,IDatatype> lookup;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Linker()
	{
		this.lookup = new HashMap<String,IDatatype>(); 
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Returns the complete representation of the specified datatype.
	 * <p/>
	 * If the <code>type</code> parameter is a {@link DatatypePlaceholder} then its complete 
	 * representation is resolved and returned. 
	 * <p/>
	 * In all other cases the <code>type</code> parameter is returned as it already represents
	 * the complete type.
	 *  
	 * @param type the {@link IDatatype} to resolve
	 * @return the complete representation of the specified datatype
	 * @throws LinkerException if the <code>type</code> parameter could not be resolved to a
	 *                         complete datatype
	 */
	private IDatatype resolve( IDatatype type ) throws LinkerException
	{
		IDatatype actual = null;
		
		if( type instanceof DatatypePlaceholder )
		{
			String name = type.getName().toLowerCase();
			IDatatype candidate = lookup.get( name );
		
			if( candidate != null )
				actual = candidate;
			else
				throw new LinkerException( "Undefined datatype: " + type.getName() );
		}
		else
		{
			actual = type;
		}
		
		return actual;
	}
	
	/**
	 * Performs the same function as {@link #resolve(IDatatype)} but ensures that the resolved
	 * datatype is a {@link BasicType}.
	 * @throws LinkerException if the <code>type</code> parameter could not be resolved to a
	 *                         complete datatype or does not represent a BasicType
	 */
	private BasicType resolveBasic( IDatatype type ) throws LinkerException
	{
		IDatatype actual = resolve( type );
		if( actual instanceof BasicType )
			return (BasicType)actual;
		else
			throw new LinkerException( type.getName() + " is not a Basic type" );
	}
	
	/**
	 * Performs the same function as {@link #resolve(IDatatype)} but ensures that the resolved
	 * datatype is a {@link BasicType}.
	 * @throws LinkerException if the <code>type</code> parameter could not be resolved to a
	 *                         complete datatype or does not represent a BasicType
	 */
	private EnumeratedType resolveEnumerated( IDatatype type ) throws LinkerException
	{
		IDatatype actual = resolve( type );
		if( actual instanceof EnumeratedType )
			return (EnumeratedType)actual;
		else
			throw new LinkerException( type.getName() + " is not an Enumerated type" );
	}
	
	/**
	 * Fetches a named enumerator value from the specified {@link EnumeratedType}.
	 * @throws LinkerException if the {@link EnumeratedType} contains no enumerator value named
	 *                         <code>enumeratorName</code>
	 */
	private Enumerator getEnumeratorValue( EnumeratedType type, String enumeratorName )
		throws LinkerException
	{
		// Alternative entries in Variant Records can specify HLA_OTHER as a wildcard
		if( enumeratorName.equals(Enumerator.HLA_OTHER.getName()) )
			return Enumerator.HLA_OTHER;
		
		try
		{
			return type.valueOf( enumeratorName );
		}
		catch( IllegalArgumentException iae )
		{
			throw new LinkerException( "invalid enumerator value: " + 
			                           enumeratorName );
		}
	}
	
	/**
	 * Adds {@link IDatatype} candidates to the pool of complete representations that a
	 * {@link DatatypePlaceholder} can be resolved to
	 */
	public void addCandidates( Collection<? extends IDatatype> candidates )
	{
		// Add all candidates from the candidate set
		for( IDatatype candidate : candidates )
		{
			String name = candidate.getName().toLowerCase();
			if( !this.lookup.containsKey(name) )
				this.lookup.put( name, candidate );
		}
	}
	
	/**
	 * Resolves any {@link DatatypePlaceholder} references within a datatype to their complete
	 * representations.
	 * <p/>
	 * If the <code>type</code> parameter does not contain any {@link DatatypePlaceholder} references
	 * then no action is performed on the type.
	 * 
	 * @param type The type to link
	 * @throws LinkerException if the <code>type</code> contains {@link DatatypePlaceholder} references
	 *                         that can not be resolved
	 */
	public void linkType( IDatatype type ) throws LinkerException
	{
		switch( type.getDatatypeClass() )
		{
			case SIMPLE:
			{
				SimpleType asSimple = (SimpleType)type;
				BasicType representation = resolveBasic( asSimple.getRepresentation() );
				asSimple.setRepresentation( representation );
				break;
			}
			case ENUMERATED:
			{
				EnumeratedType asEnumerated = (EnumeratedType)type;
				BasicType representation = resolveBasic( asEnumerated.getRepresentation() );
				asEnumerated.setRepresentation( representation );
				break;
			}
			case ARRAY:
			{
				ArrayType asArray = (ArrayType)type;
				IDatatype datatype = resolve( asArray.getDatatype() );
				asArray.setDatatype( datatype );
				break;
			}
			case FIXEDRECORD:
			{
				FixedRecordType asFixedRecordType = (FixedRecordType)type;
				for( Field field : asFixedRecordType.getFields() )
				{
					IDatatype datatype = resolve( field.getDatatype() );
					field.setDatatype( datatype );
				}
				break;
			}
			case VARIANTRECORD:
			{
				VariantRecordType asVariantType = (VariantRecordType)type;
				EnumeratedType datatype = resolveEnumerated( asVariantType.getDiscriminantDatatype() );
				asVariantType.setDiscriminantDatatype( datatype );
				
				Set<Alternative> alternatives = asVariantType.getAlternatives();
				for( Alternative alternative : alternatives )
				{
					IDatatype alternativeDatatype = this.resolve( alternative.getDatatype() );
					alternative.setDatatype( alternativeDatatype );
					
					// The placeholder enumerator may contain a range of enumerators. As we didn't 
					// know the full value set at parse time, we have to expand out any ranges we 
					// come across now at link time
					Set<IEnumerator> incomingEnums = alternative.getEnumerators();
					Set<IEnumerator> linkedEnums = new HashSet<IEnumerator>();
					for( IEnumerator enumerator : incomingEnums )
					{
						String incomingName = enumerator.getName().trim();
						if( incomingName.startsWith("[") && 
							incomingName.endsWith("]") && 
							incomingName.contains("..") )
						{
							// Placeholder is a range of enumerator constants
							String rangeString = incomingName.substring( 1, incomingName.length() - 1 );
							String[] rangeTokens = rangeString.split( "\\.\\.", 2 );
							if( rangeTokens.length != 2 )
								throw new JConfigurationException( "Enumerator range must contain a lower and an upper bound" );
							
							Enumerator lower = getEnumeratorValue( datatype, 
							                                       rangeTokens[0].trim() );
							Enumerator upper = getEnumeratorValue( datatype, 
							                                       rangeTokens[1].trim() );
							
							List<Enumerator> enumerators = datatype.getEnumerators();
							linkedEnums.add( lower );
							boolean inRange = false;
							for( int i = 0 ; i < enumerators.size() ; ++i )
							{
								Enumerator enumeratorAtI = enumerators.get( i );
								if( enumeratorAtI == upper )
									inRange = false;
							
								if( inRange )
									linkedEnums.add( enumeratorAtI );
								
								if( enumeratorAtI == lower )
									inRange = true;
							}
							linkedEnums.add( upper );
						}
						else
						{
							// Placeholder is just the one enumerator
							linkedEnums.add( getEnumeratorValue(datatype, incomingName) );
						}
					}
					
					alternative.setEnumerators( linkedEnums );
				}
				
				break;
			}
			case BASIC:
			default:
			{
				// No action required
				break;
			}
		}
	}
	
	public void linkAttribute( ACMetadata attribute ) throws LinkerException
	{
		IDatatype datatype = attribute.getDatatype(); 
		if( datatype instanceof DatatypePlaceholder )
		{
			try
			{
				IDatatype resolved = resolve( datatype );
				attribute.setDatatype( resolved );
			}
			catch( LinkerException le )
			{
				// Rethrow the exception, but prefix the message with the attribute name
				throw new LinkerException( "Attribute " + attribute.getName() + " " + le.getMessage(), 
				                           le );
			}
		}
	}
	
	public void linkParameter( PCMetadata parameter ) throws LinkerException
	{
		IDatatype datatype = parameter.getDatatype();
		if( datatype instanceof DatatypePlaceholder )
		{
			try
			{
				IDatatype resolved = resolve( datatype );
				parameter.setDatatype( resolved );
			}
			catch( LinkerException le )
			{
				// Rethrow the exception, but prefix the message with the parameter name
				throw new LinkerException( "Parameter " + parameter.getName() + " " + le.getMessage(), 
				                           le );
			}
		}
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
