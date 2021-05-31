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

import java.util.Collection;
import java.util.Set;

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
		if( clazz == null )
			return;

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
			String desc = "   (attribute): " + name + ", " + pad(buffer-name.length()) +
			              "handle=" + attribute.getHandle() + ", order=" + attribute.getOrder() +
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
		if( clazz == null )
			return;

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
			String desc = "   (parameter): " + name + ", " + pad(buffer-name.length()) +
			              "handle=" + parameter.getHandle();
			
			log( desc, builder, (level+1) );
		}
		
		// log all the subclasses
		for( ICMetadata subclass : clazz.getChildTypes() )
		{
			renderInteraction( subclass, builder, (level+1) );
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
}

