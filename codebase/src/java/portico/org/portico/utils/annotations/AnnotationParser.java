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
package org.portico.utils.annotations;

import java.io.InputStream;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

/**
 * This class handles the bytecode inspector that will look at the bytes for a class file and
 * attempt to determine if the class contained within declares a particular annotation. This
 * makes use of the ASM library for bytecode interpretation.
 */
public class AnnotationParser extends ClassVisitor
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private String targetAnnotation;
	private boolean found;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	private AnnotationParser( String targetAnnotation )
	{
		super( 262144 );
		
		this.targetAnnotation = targetAnnotation;
		this.found = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	public AnnotationVisitor visitAnnotation( String name, boolean visible )
	{
		if( name.equals(targetAnnotation) )
			this.found = true;
		
		return new AnnotationVis(262144);
	}
	
	private class AnnotationVis extends AnnotationVisitor
	{

		public AnnotationVis( int api )
		{
			super( api );
			//TODO Auto-generated constructor stub
		}
		
		public void visit(String name, Object value) 
		{
			if (this.av != null) 
			{
				this.av.visit(name, value);
			}

		}

		public void visitEnum(String name, String desc, String value) 
		{
			if (this.av != null) 
			{
				this.av.visitEnum(name, desc, value);
			}

		}

		public AnnotationVisitor visitAnnotation(String name, String desc)
		{
			return this.av != null ? this.av.visitAnnotation(name, desc) : null;
		}

		public AnnotationVisitor visitArray(String name) 
		{
			return this.av != null ? this.av.visitArray(name) : null;
		}

		public void visitEnd() 
		{
			if (this.av != null) 
			{
				this.av.visitEnd();
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Will process the class file linked to the input stream and search it to see if the
	 * annotation provided is anywhere to be found. If the annotation has been declared, 
	 * <code>true</code> will be returned. If it hasn't <code>false</code> will be returned.
	 * <p/>
	 * <b>NOTE:</b> Always remember to close your input streams when you're finished with them.
	 * This method won't do it for you!
	 * 
	 * @param classBytes An input stream pointing to the bytes.
	 * @param annotationString The internal java name for the annotation class that you are
	 *                         searching for. E.g. "Lcom/lbf/commons/component/Component;"
	 * @return <code>true</code> if the annotation is declared, <code>false</code> otherwise
	 * @throws Exception If there is a problem reading the class or processing it
	 */
	public static boolean parseForAnnotation( InputStream classBytes, String annotationString )
		throws Exception
	{	
		try
		{
			ClassReader reader = new ClassReader( classBytes );
			AnnotationParser parser = new AnnotationParser( annotationString );
			reader.accept( parser, ClassReader.EXPAND_FRAMES );
			return parser.found;
		}
		catch( Exception e )
		{
			return false;
		}
	}

}
