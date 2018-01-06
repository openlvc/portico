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

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class contains methods to locating classes (either in a specific location, or on the
 * classpath in general) that declare a particular class-level annotation. A user should provide
 * the Class of the Annotation they wish to load, and they will get back a set of Classes that
 * declare that annotation. Rather than loading each and every class it scans so that reflection
 * can be used to locate annotations, the methods of this class look directly at the bytecode for
 * a class (using the Java ASM project). This avoids having to load all the classes, saving both
 * memory and time.  
 */
public class AnnotationLocator
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	private AnnotationLocator()
	{
		this.logger = LogManager.getFormatterLogger( "portico.container" );
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Scan the system classpath for any classes that declare the given annotation. If there is a
	 * problem (a file can't be opened or read for exapmle) an exception will be thrown.
	 */
	private Set<Class<?>> _locateClassesWithAnnotation( Class<? extends Annotation> annotation )
		throws Exception
	{
		logger.trace( "(Annotation) Scanning full classpath for annotation: "+
		              annotation.getCanonicalName() );
		Set<Class<?>> classesWithAnnotation = new HashSet<Class<?>>();
		
		// check each of the URLs on the classpath
		URLClassLoader systemLoader = (URLClassLoader)ClassLoader.getSystemClassLoader();
		for( URL currentURL : systemLoader.getURLs() )
		{
			classesWithAnnotation.addAll( _locateClassesWithAnnotation(annotation, currentURL) );
		}
		
		logger.trace( "(Annotation) Scanned full classpath for annotation: "+
		              annotation.getCanonicalName() );
		return classesWithAnnotation;
	}
	
	/**
	 * Scan the given location for any classes that declare the given annotation. If there is a
	 * problem (a file can't be opened or read for exapmle) an exception will be thrown. If the
	 * URL points to a jar file, the internals of that jar file will be scanned. If the URL is
	 * a directory, all subdirectories will be searched (all the class files found will be scanned,
	 * as will any jar files).
	 */
	private Set<Class<?>> _locateClassesWithAnnotation( Class<? extends Annotation> annotation,
	                                                    URL urlToSearch )
		throws Exception
	{
		logger.trace( "(Annotation) Scan for annotation: type="+annotation.getCanonicalName()+
		              ", location="+urlToSearch );
		
		// convert the classname to the internal java representation of the name
		String annotationName = annotationToJavaName( annotation );

		// get the paths to all the class files with the annotation in the location
		Set<String> filenames = new HashSet<String>();
		
		try
		{
			if( isArchive(urlToSearch) )
			{
				filenames = filenamesInJar( urlToSearch, annotationName );
			}
			else if( isDirectory(urlToSearch) )
			{
				filenames = filenamesInDirectory( urlToSearch, annotationName );
			}
			else
			{
				// not an archive, ignore for now
				return new HashSet<Class<?>>();
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

		// load the classes that contain the annotation
		HashSet<Class<?>> classesWithAnnotation = new HashSet<Class<?>>();
		for( String filename : filenames )
		{
			String className = pathToClassName( filename );
			classesWithAnnotation.add( Class.forName(className) );
		}
		
		logger.trace( "(Annotation) Finished scanning ["+urlToSearch+"] for annotation ["+
		              annotation.getCanonicalName()+"], found ["+classesWithAnnotation.size()+
		              "] occurrences" );

		return classesWithAnnotation;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Private Helper Methods //////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns true if the given location points to a jar file
	 */
	private boolean isArchive( URL url )
	{
		return url.getFile().endsWith( ".jar" );
	}
	
	/**
	 * Returns true if the given URL represents a directory
	 */
	private boolean isDirectory( URL url )
	{
		return url.getFile().endsWith( "/" );
	}
	
	/**
	 * Convert a java class name into the internal java representation of it. For example,
	 * "org.portico.component.Component" would be turned into
	 * "Lorg/portico/component/Component;"
	 */
	private String annotationToJavaName( Class<? extends Annotation> annotation )
	{
		String javaName = annotation.getCanonicalName().replace( ".", "/" );
		return "L" + javaName + ";";
	}

	/**
	 * Converts a path name into a qualified class name. For example,
	 * "org/portico/component/Component.class" becomes, "org.portico.component.Component"
	 */
	private String pathToClassName( String path )
	{
		path = path.replace( "/", "." );
		path = path.replace( ".class", "" );
		return path;
	}
	
	/**
	 * Returns the paths to all the classes that declare the identified annotation inside the
	 * given jar file. If there are none, an empty set is returned. If there is any problem
	 * processing the jar, it will be ignored and an empty set will be returned.
	 * 
	 * @param theURL The URL of the jar file to search
	 * @param annotationName The internal java name of the annotation to look for (see
	 *                       {@link #annotationToJavaName(Class)})
	 * @return A set of all the paths to classes inside the jar file that delcare the annotation
	 */
	private Set<String> filenamesInJar( URL theURL, String annotationName )
	{
		try
		{
    		//System.out.println( "Searching for all classes with annotation [" + annotationName +
    		//                    "] in [" + theURL + "]" );
    
    		// turn the URL into a file that we can work with
    		HashSet<String> names = new HashSet<String>();
    		JarFile jarfile = new JarFile( new File(theURL.toURI()) );
    		
    		Enumeration<JarEntry> entries = jarfile.entries();
    		while( entries.hasMoreElements() )
    		{
    			JarEntry entry = entries.nextElement();
    
    			// skip directories
    			if( entry.isDirectory() )
    				continue;
    
    			// is this a class file?
    			if( entry.getName().endsWith(".class") == false )
    				continue;
    			
    			InputStream inputStream = jarfile.getInputStream( jarfile.getEntry(entry.getName()) );
    			// does this file posses the annotation?
    			if( AnnotationParser.parseForAnnotation(inputStream,annotationName) )
    				names.add( entry.getName() );
    			
    			// close off the stream
    			inputStream.close();
    		}
    		
			jarfile.close();
    		return names;
		}
		catch( Exception e )
		{
			return new HashSet<String>();
		}
	}
	
	/**
	 * Same as {@link #filenamesInDirectory(URL, URL, String)} except that it automatically fills
	 * out the baseURL. See that method for full details.
	 */
	private Set<String> filenamesInDirectory( URL theURL, String annotationName ) throws Exception
	{
		// by default, we assume that the base URL is the same as the URL we are searching
		return filenamesInDirectory( theURL, theURL, annotationName );
	}
	
	/**
	 * Search <code>theURL</code> (which should represent a directory) for all classes that declare
	 * the identified annotation. This method will look at all files in the directory. For each
	 * file:
	 * <ul>
	 *   <li>If the file is a jar file, that file will be scanned using
	 *       {@link #filenamesInJar(URL, String)} </li>
	 *   <li>If the file is a subdirectory, this method will recurse into it (see point below
	 *       about the <code>baseURL</code>)</li>
	 *   <li>If the file is a <code>.class</code> file, the method will scan it to see if it
	 *       contains the desired annotation.</li>
	 * </ul>
	 * 
	 * The return from this method will be a set of paths that point to all the class files
	 * declaring the desired annotation. If there are none, an empty set will be returned.
	 * <p/>
	 * <b>The Base URL</b>
	 * <p/>
	 * When a <code>.class</code> file is found in a directory, the path for that file needs to
	 * be added to the set of paths that are returned. However, this path needs to be converted
	 * into a fully-qualified class name down the track. The problem is that a File and URL will
	 * generally only give us full, absolute paths like the following:
	 * <p/>
	 * <code>"/home/tim/commons/codebase/build/test/classes/com/lbf/commons/Test.class"</code>
	 * <p/>
	 * The problem with this is that is we try and turn this into a qualified class name we don't
	 * know which part of the directory structure represents the package hierarchy and which part
	 * is just local path information specific to the particular environment. For this reason, we
	 * need the <code>baseURL</code>. This is turned into a String path and represents the base
	 * directory we started searching. When we find a class file, we just replace the baseURL
	 * string with an empty string, effectively removing the non-package related directory stuff
	 * and leaving us with something like "com/lbf/commons/Test.class" that we can easily turn
	 * into the fully-qualified class name of "com.lbf.commons.Test".
	 * <p/>
	 * When searching subdirectories recursively, each call will contain a different value for
	 * <code>theURL</code>, but the <code>baseURL</code> should stay the same. If the baseURL is
	 * null, the method will assume that <code>theURL</code> is the base url.
	 * 
	 * @param theURL The URL of the directory to search
	 * @param baseURL The base URL we began searching from
	 * @param annotationName The name of the annotation we are looking for, in the java internal
	 *                       formatting. e.g. "Lcom/lbf/commons/component/Component;"
	 * @return A set of paths that represent classes that declare the identified annotation
	 * @throws Exception If there is a problem reading or processing the directory, any
	 *                   contained files, etc...
	 */
	private Set<String> filenamesInDirectory( URL theURL, URL baseURL, String annotationName )
		throws Exception
	{
		//System.out.println( "Searching for all classes with annotation [" + annotationName +
		//                    "] in [" + theURL + "]" );

		// have we got a base URL? if not, assume that it is the theURL we are searching
		if( baseURL == null )
			baseURL = theURL;
		
		// turn the URL into a file we can work with, this will probably fail if the
		// URL doesn't represent a file on the local file system, but then again, that
		// is all we're really interested in here
		// why temp? see: http://weblogs.java.net/blog/kohsuke/archive/2007/04/how_to_convert.html
		String temp = URLDecoder.decode( theURL.getPath(), System.getProperty("file.encoding") );
		File file = new File( temp );
		if( file.isDirectory() == false )
		{
			throw new Exception( "Expected URL to be a directory: " + file );
		}
		
		Set<String> returnSet = new HashSet<String>();
		
		// search the directory for all class files
		for( File containedFile : file.listFiles() )
		{
			if( containedFile.isDirectory() )
			{
				// recurse into the subdirectory and process its contents
				returnSet.addAll( filenamesInDirectory(containedFile.toURI().toURL(),
				                                       baseURL,
				                                       annotationName) );
			}
			else if( containedFile.getAbsolutePath().endsWith(".jar") )
			{
				// it is a jar file, process it as such
				returnSet.addAll( filenamesInJar(containedFile.toURI().toURL(),annotationName) );
			}
			else if( containedFile.getAbsolutePath().endsWith(".class") )
			{
				// it is a class, check it for the desired annotation
				URL containedURL = containedFile.toURI().toURL();
				InputStream fileStream = containedURL.openStream();
				if( AnnotationParser.parseForAnnotation(fileStream,annotationName) )
				{
					// generate the path to the class. the files/urls will be working with
					// absolute names, whereas we are really only want the relative path from
					// the directory we originally scanned. this directory is pointed at by
					// the baseURL parameter. we need to compare the containedFile with the
					// baseURL, removing the base part leaving us with only the relative part
					//
					// for example, this should turn the following full path:
					//
					// "/home/tim/commons/codebase/build/test/classes/com/lbf/commons/Test.class"
					//
					// into something that only contains the path part that represents the
					// package hierarchy: "com/lbf/commons/Test.Class"
					String fullpath = containedURL.getPath();
					fullpath = fullpath.replace( baseURL.getPath(), "" );
					returnSet.add( fullpath );
				}
				
				// close off the stream
				fileStream.close();
			}
			
		}
		
		return returnSet;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	/**
	 * Search the entire system classpath for all classes that declare the provided annotation and
	 * return a set of all those classes. This method will cause the bytecode of each class to be
	 * looked at, rather than loading each class and using reflection. If there are none that have
	 * this annotation, an empty set is returned. If there is a problem searching the classpath or
	 * processing the class files, an exception will be thrown.
	 */
	public static Set<Class<?>> locateClassesWithAnnotation( Class<? extends Annotation> annotation )
		throws Exception
	{
		return new AnnotationLocator()._locateClassesWithAnnotation( annotation );
	}

	/**
	 * Same as {@link #locateClassesWithAnnotation(Class, URL)}. It will automatically convert the
	 * given location to a URL and call that method.
	 */
	public static Set<Class<?>> locateClassesWithAnnotation( Class<? extends Annotation> annotation,
	                                                         File locationToSearch )
		throws Exception
	{
		return AnnotationLocator.locateClassesWithAnnotation( annotation,
		                                                      locationToSearch.toURI().toURL() );
	}
	
	/**
	 * Search the given URL for all classes that declare the provided annotation and return a set
	 * of all those classes. If the URL is a jar file, this method will search inside it. If it
	 * points to a directory, this method will look for all ".class" files in that directory (and
	 * its subdirectories). If any jar files are found in this process, they will also be searched.
	 * <p/>
	 * This method uses bytecode inspection to determine if the class declares the specified
	 * annotation. By not using reflection this method avoids loading the classes, thus saving
	 * time and memory.
	 * <p/>
	 * If there is a problem searching the path, reading any of the files or extracting the
	 * necessary class information, an exception will be thrown. If there are no classes that
	 * declare the annotation, an empty set will be returned.
	 */
	public static Set<Class<?>> locateClassesWithAnnotation( Class<? extends Annotation> annotation,
	                                                         URL urlToSearch )
		throws Exception
	{
		return new AnnotationLocator()._locateClassesWithAnnotation( annotation, urlToSearch );
	}
}
