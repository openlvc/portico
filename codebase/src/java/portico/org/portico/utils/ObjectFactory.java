/*
 *   Copyright 2009 The Portico Project
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
package org.portico.utils;

import java.lang.reflect.InvocationTargetException;

/**
 * The {@link ObjectFactory} class provides a couple of instance-creation convenience methods.
 * These methods can take care of the process of instantiating an instance of a class from a given
 * class name. The methods will also apply some validity checks to make sure the class is of the
 * type you are expecting.
 */
public class ObjectFactory
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
	 * This method will dynamically create an instance of any given class name and will
	 * verify that it is of the given target class. The class loader used will be the one
	 * that loaded this class (<code>ObjectFactory.class.getClassLoader()</code>).
	 *
	 * @param classname Name of the class to instantiate
	 * @param targetClass Class class to compare the generated instance to 
	 * @return An instance of the class requested
	 */
	public static <T> T create( String classname, Class<T> targetClass )
		throws Exception
	{
		return create( classname, ObjectFactory.class.getClassLoader(), targetClass );
	}
	
	/**
	 * This method is the same as {@link #create(String, Class)} except that it takes the class
	 * you want to instantiate. This method essentially encapsulates all instantiation logic and
	 * post-instantiation checks, removing that burdern from the user.
	 */
	public static <T> T create( Class<?> clazz, Class<T> targetClass )
		throws Exception
	{
		return create( clazz.getCanonicalName(), targetClass );
	}
	
	/**
	 * This method will dynamically create an instance of any given class name and will
	 * verify that it is of the given target class. The given class loader will be used to
	 * locate the class file
	 *
	 * @param classname The name of the class to instantiate
	 * @param loader The loader to use to get the class
	 * @param targetClass Type the created instance should be of (or a valid super type/interface)
	 * @return An instance of the given class name
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 */
	public static <T> T create( String classname, ClassLoader loader, Class<T> targetClass )
		throws Exception
	{
		// check the parameters
		if( classname == null || targetClass == null )
		{
			throw new ClassNotFoundException(
				"Invalid parameters found. Require non-null class name and target type" );
		}
		
		// we've got class name, attempt to create the instance
		Object retVal = Class.forName( classname, true, loader ).getDeclaredConstructor().newInstance();
		// got an instance of something, is it one of what we want?
		if( targetClass.isInstance(retVal) )
		{
			// yes it is! return it
			return targetClass.cast( retVal );
		}
		else
		{
			// we got an instance of something, but it isn't what we want
			// too many string appends for my liking, builder it up :)
			StringBuilder message = new StringBuilder( "The class [" );
			message.append( classname );
			message.append( "] is NOT of the required type [" );
			message.append( targetClass.getName() );
			message.append( "]" );
			throw new InstantiationException( message.toString() );
		}
	}
}
