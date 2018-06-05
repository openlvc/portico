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
package org.portico.lrc;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.portico.lrc.compat.JConfigurationException;
import org.portico.utils.ResourceLocator;

public class PorticoConstants
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The official name for the RTI, used in the RTIFactory */
	public static final String RTI_NAME = "Portico";
	
	/** The current version of the RTI */
	public static final String RTI_VERSION = getRtiVersion();
	
	/** Consistent form for identifying an invalid time */
	public static final double NULL_TIME = -1.0;
	
	/** Consistent value for an invalid handle */
	public static final int NULL_HANDLE = -1;
	
	/** The handle to use for the "RTI" */
	public static final int RTI_HANDLE = 0;
	
	//////////////////////////////////////////////
	///////// Portico General Properties /////////
	//////////////////////////////////////////////
	/** The log level to apply to all loggers under the "portico" level.
	    This level defaults to WARN. You can alter this level through
	    a system property as long as you do before the container is loaded (such as in the RID) */
	public static String PORTICO_LOG_LEVEL = "WARN";
	
	/** The log level for all container level elements (messaging, configuration, etc...). This
	    can be overridden through the RID file */
	public static String CONTAINER_LOG_LEVEL = "OFF";
	
	////////////////////////////////////////////////
	//////////// HLA-Related Properties ////////////
	////////////////////////////////////////////////
	/** Should the ObjectModel types return their qualified names from toString()? If true, they
	    will, otherwise, they'll return the handle values */
	public static boolean USE_Q_NAMES = true;

	/** Min/Max extent values for use in DDM */
	public static final long MIN_EXTENT = 0;

	/** Min/Max extent values for use in DDM */
	public static final long MAX_EXTENT = Long.MAX_VALUE;

	/** The maximum number of extents allowed per region - at the moment there is no explicit
	    reason for this number, it is just an arbitrary choice that should be safe */
	public static final int MAX_NUMBER_OF_EXTENTS = 1024;
	
	/** The maximum number of federates allowed in any single federation. Currently 1024. */
	public static final int MAX_FEDERATES = 1024;
	
	/** The maximum number of objects a federate is allowed to register. This value is calculated
	    as {@link Integer#MAX_VALUE} / {@link #MAX_FEDERATES} */
	public static final int MAX_OBJECTS = Integer.MAX_VALUE / MAX_FEDERATES;

	/** The maximum number of regions a federate is allowed to register. This value is calculated
	    as {@link Integer#MAX_VALUE} / {@link #MAX_FEDERATES} */
	public static final int MAX_REGIONS = Integer.MAX_VALUE / MAX_FEDERATES;

	///////////////////////////////////////////////
	//////////// System Property Names ////////////
	///////////////////////////////////////////////
	/** The name of the system property that represents the location of the Portico install */
	public static final String PROPERTY_RTI_HOME = "rti.home";
	
	/** System property for identifying the portico plugin path */
	public static final String PROPERTY_PLUGIN_PATH = "portico.pluginpath";
	
	/** System property for defining if the MOM is enabled or disabled */
	public static final String PROPERTY_MOM = "portico.mom";
	
	/** System property for defining if the FOM should be printed on federation startup */
	public static final String PROPERTY_PRINT_FOM = "portico.fom.print";
	
	/** System property for defining if the auto-route facility should be enabled in a
	    kernel, by default it is not. Values should be "true" or "false" */
	public static final String PROPERTY_KERNEL_AUTOROUTE = "portico.kernel.autoRoute";

	/** System property that identifies the value for {@link #PORTICO_LOG_LEVEL}. */
	public static final String PROPERTY_PORTICO_LOG_LEVEL = "portico.loglevel";
	
	/** System property that identifies the log level for the portico container */
	public static final String PROPERTY_CONTAINER_LOG_LEVEL = "portico.container.loglevel";

	/** System property used to specify the connection implementation to use */
	public static final String PROPERTY_CONNECTION = "portico.connection";
	
	/** System property that specifies which items should be logged using their handles, rather
	    than their names. By default, when object, attribute, interaction and parameter types are
	    the subject of logging, their handles are used. On the other hand, when logging about
	    federates, their names are used. The value of this propery should be a comma-separated
	    list specifying which of all of these should be logged as handles, rather than names. The
	    opposite of this exists for specifying which should be logged using names. The full list
	    of values that can be in the list is:
	    <ul>
	      <li>objectClass</li>
	      <li>attributeClass</li>
	      <li>interactionClass</li>
	      <li>parameterClass</li>
	      <li>objectInstance</li>
	      <li>space</li>
	      <li>dimension</li>
	      <li>federate</li>
	    </ul>
	    
	    So, if you want all FOM-data to be logged as handles, but object instances and federates
	    to be logged by name, you would have the following property/value settings:
	    <ul>
	      <li><b>portico.logWithHandles=objectClass,attributeClass,interactionClass,...</b></li>
	      <li><b>portico.logWithNames=objectInstance,federate</b></li>
	    </ul>
	    
	    If there is a conflict where an item is in both lists, the value in logWithNames will take
	    preference.
	 */
	public static final String PROPERTY_LOG_WITH_HANDLES = "portico.logWithHandles";
	
	/** See javadoc for {@link #PROPERTY_LOG_WITH_HANDLES} */
	public static final String PROPERTY_LOG_WITH_NAMES = "portico.logWithNames";
	
	/** System property for defining which directory save/restore dump files should be in */
	public static final String PROPERTY_SAVE_DIRECTORY = "portico.saveDirectory";
	
	/** System property for defining which directory to put the log file in */
	public static final String PROPERTY_LOG_DIR = "portico.logdir";
	
	/** System property for defining whether or not calls to unsupported RTIambassador methods
	    should result in an exception or not: default is to NOT throw an exception */
	public static final String PROPERTY_UNSUPPORTED_EXCEPTIONS = "portico.unsupportedExceptions";

	/** System propery for defining whether or not to consult the other federates to check if an
	    object name is unique when a federate requests a specific name. Default is *not* to check */
	public static final String PROPERTY_NEGOTIATE_OBJECT_NAMES = "portico.object.negotiateNames";
	
	/** System property for defining whether or not a federate has to have a unique name when
	    joining a federation. By default it should and this check is enabled */
	public static final String PROPERTY_UNIQUE_FEDERATE_NAMES = "portico.uniqueFederateNames";
	
	///////////////////////////////////////////////
	////////////// Kernel Properties //////////////
	///////////////////////////////////////////////
	/** Component name that identifies a Kernel representing a HLA 1.3 LRC */
	public static final String KEYWORD_LRC13 = "lrc13";

	/** Component name that identifies a Kernel representing a HLA 1.3 (java1 - DMSO compat) LRC */
	public static final String KEYWORD_LRCJAVA1 = "lrcjava1";
	
	/** Component name that identifies a Kernel representing a HLA 1516 LRC */
	public static final String KEYWORD_LRC1516 = "lrc1516";
	
	/** Component name that identifies a Kernel representing a HLA 1516-Evolved LRC */
	public static final String KEYWORD_LRC1516e = "lrc1516e";
	
	///////////////////////////////////////////////
	//////////// Connection Properties ////////////
	///////////////////////////////////////////////
	/** The constant that signals a timeout isn't needed and that the connection should wait
	    forever until it gets a reply */
	public static final long CONNECTION_INFINITE_TIMEOUT = 0;
	
	/** The default connection implementation class */
	public static final String CONNECTION_DEFAULT_IMPL =
		"org.portico.bindings.jgroups.JGroupsConnection";
	
	//////////////////////////////////////////////
	////////// Private Local Properties //////////
	//////////////////////////////////////////////
	/** Last value for {@link #PROPERTY_LOG_WITH_HANDLES} */
	private static String valueOfPrintWithHandles = "<initial>";
	
	/** Last value for {@link #PROPERTY_LOG_WITH_NAMES} */
	private static String valueOfPrintWithNames = "<initial>";
	// cached values for handle printing values
	private static boolean logObjectClassHandles = true;
	private static boolean logAttributeClassHandles = true;
	private static boolean logInteractionClassHandles = true;
	private static boolean logParameterClassHandles = true;
	private static boolean logObjectInstanceHandles = true;
	private static boolean logSpaceHandles = false;
	private static boolean logDimensionHandles = false;
	private static boolean logFederateHandles = false;
	
	//////////////////////////////////////////////
	///////// Portico C++ Property Names /////////
	//////////////////////////////////////////////
	/** Settings passed over from the C++ side as system properties to tell us some
	    specifics about the C++ app running. Used to determine libraries and paths
	    for loadback */
	public static final String PROPERTY_CPP_MODE = "portico.cpp.mode";
	public static final String PROPERTY_CPP_COMPILER = "portico.cpp.compiler";
	public static final String PROPERTY_CPP_HLAVERSION = "portico.cpp.hlaversion";
	public static final String PROPERTY_CPP_ARCH = "portico.cpp.arch";
	
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
	 * Returns the current set of system propeties as a Map<String,Object>(). A new instance of the
	 * map is created each time this method is called.
	 */
	public static Map<String,Object> getSystemPropertiesAsMap()
	{
		HashMap<String,Object> map = new HashMap<String,Object>();
		Properties properties = System.getProperties();
		for( Object key : properties.keySet() )
			map.put( (String)key, properties.get(key) );
		
		return map;
	}
	
	/**
	 * Load the properties file "build.properties" into the system properties and return
	 * the value of the value of "${build.version} (build ${build.number})"
	 */
	private static String getRtiVersion()
	{
		if( System.getProperty("build.version") == null )
		{
			try
			{
				ResourceLocator.loadPropertiesResource( "build.properties" );
			}
			catch( Exception e )
			{
				// do nothing, not much we can do
			}
		}
		
		//  
		String buildVersion = System.getProperty( "build.version", "unknown" );
		String buildNumber = System.getProperty( "build.number", "unknown" );
		return buildVersion + " (build "+buildNumber+")";
	}

	/**
	 * This method firstly checks the environment variable <code>RTI_RID_FILE</code> for the
	 * location of the RID file. If the variable <code>RTI_RID_FILE</code> is defined and the
	 * file contained within exists and can be read, then that file is returned. If not, the
	 * location <code>./RTI.rid</code>. If the file exists and can be read then it is returned.
	 * <p/>
	 * If both paths lead to nothing, then null is returned.
	 */
	public static File getRidFileLocation()
	{
		// FIX: PORT-592
		// ==========================
		// To conform with DMSO, RID file should be searched for at RTI_RID_FILE and then in the 
		// current working directory
		String rtiRidFileEnv = System.getenv( "RTI_RID_FILE" );
		
		// If RTI_RID_FILE was set
		if ( rtiRidFileEnv != null )
		{	
			// Check to see if the file that RTI_RID_FILE was pointing to is valid
			File ridFileFromEnv = new File( rtiRidFileEnv );
			if( ridFileFromEnv.exists() && ridFileFromEnv.isFile() && ridFileFromEnv.canRead() )
			{
				// if it is, return it!
				return ridFileFromEnv;
			}
		}
		
		// If RTI_RID_FILE was NOT set, or the file it was pointing to was invalid, then
		// fall through to checking the file specified in PorticoConstants.DEFAULT_RTI_RID
		File ridFileFromDefault = new File( "RTI.rid" );
		
		// If the RID file from the default path is valid then return it
		if( ridFileFromDefault.exists() &&
			ridFileFromDefault.isFile() &&
			ridFileFromDefault.canRead() )
		{
			return ridFileFromDefault;
		}

		// If execution gets to here then the RID file has not been found at the RTI_RID_FILE
		// or from PorticoConstants.DEFAULT_RTI_RID, therefore return null
		return null;
	}
	
	/**
	 * Gets the RTI_HOME value where Portico is installed. If the system property
	 * {@link #PROPERTY_RTI_HOME} is set, that value will be used. If it isn't, this method will
	 * check the <code>RTI_HOME</code> environment variable. If that is also not set, the current
	 * directory will be used.
	 */
	public static File getRtiHome()
	{
		// check the rti.home system property
		String location = System.getProperty( "rti.home" );
		if( location != null )
			return new File( location );
		
		// check the RTI_HOME environment variable
		location = System.getenv( PROPERTY_RTI_HOME );
		if( location != null )
			return new File( location );
		else
			return new File( System.getProperty("user.dir") );
	}

	/**
	 * This me
	 * 
	 * Returns the location to which save files should be dumped and restore files read from. The
	 * default value for this is "./saves". To change this, set a value for the
	 * {@link #PROPERTY_SAVE_DIRECTORY} system property.
	 * <p/>
	 * This method will also 
	 */
	public static String getSaveLocation( String saveLabel, String federateName )
	{
		String directory = System.getProperty( PROPERTY_SAVE_DIRECTORY, "./savedata" );
		return directory+"/"+saveLabel+"/"+federateName+".save";
	}
	
	/**
	 * This is a wrapper for {@link System#getProperty(String, String)} except that it will
	 * sanitize boolean values. If the value of the property is "on", "enabled" or "true" (ignoring
	 * case), then <code>true</code> will be returned. If the value is "off", "disabled" or "false"
	 * then false will be returned. If the value is none of these, an exception will be thrown to
	 * indicate the error. If the value for the system property is null, the default value will be
	 * used.
	 */
	public static boolean getBooleanProperty( String propertyName, String defaultValue )
		throws JConfigurationException
	{
		String initialPropertyValue = System.getProperty( propertyName, defaultValue );
		String propertyValue = initialPropertyValue.toLowerCase();

		if( propertyValue.equals("true") ||
			propertyValue.equals("on") ||
			propertyValue.equals("enabled") ||
			propertyValue.equals("enable") )
		{
			return true;
		}
		if( propertyValue.equals("false") ||
			propertyValue.equals("off") ||
			propertyValue.equals("disabled") ||
			propertyValue.equals("disable") )
		{
			return false;
		}
		
		// we have an invalid value if we get here
		throw new JConfigurationException( "Invalid value for system property \"" + propertyName +
		                                   "\", found=" + initialPropertyValue +
		                                   " expected bool (true,false,on,off,enabled,disabled)" );
	}

	/**
	 * This is a wrapper for {@link System#getProperty(String, String)} except that it will
	 * sanitize int values. If the value of the property is not an integer, an exception will be
	 * thrown. If there is no property set for that system property, the default value will be used.
	 */
	public static int getIntProperty( String propertyName, String defaultValue )
		throws JConfigurationException
	{
		String propertyValue = System.getProperty( propertyName, defaultValue );
		try
		{
			return Integer.parseInt( propertyValue );
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Invalid value for system property \"" +propertyName+
			                                   "\", found="+propertyValue+", expected int" );
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////// HLA Related Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The property that defines whether or not the MOM is enabled by looking at the system
	 * property {@link #PROPERTY_MOM}. If the value of this property equals "true" (case
	 * insensitive), then this method will return true. Otherwise it will return false and MOM
	 * support should be disabled.
	 */
	public static boolean isMomEnabled()
	{
		return getBooleanProperty( PROPERTY_MOM, "enabled" );
	}
	
	/**
	 * Enables the use of the Mom.
	 */
	public static void enableMom()
	{
		System.setProperty( PROPERTY_MOM, "enabled" );
	}
	
	/**
	 * Disables the use of the Mom.
	 */
	public static void disableMom()
	{
		System.setProperty( PROPERTY_MOM, "disabled" );
	}
	
	/**
	 * Returns true if the FOM should be logged every time a federation is created.
	 */
	public static boolean isPrintFom()
	{
		return getBooleanProperty( PROPERTY_PRINT_FOM, "disabled" );
	}

	/////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Handle/Name Print Methods ///////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////
	private static void updateLogValues()
	{
		// does the current values different from their original values?
		if( valueOfPrintWithHandles.equals(System.getProperty(PROPERTY_LOG_WITH_HANDLES)) == false ||
			valueOfPrintWithNames.equals(System.getProperty(PROPERTY_LOG_WITH_NAMES)) == false )			
		{
			// reset to defaults
			logObjectClassHandles = true;
			logAttributeClassHandles = true;
			logInteractionClassHandles = true;
			logParameterClassHandles = true;
			logObjectInstanceHandles = true;
			logSpaceHandles = false;
			logDimensionHandles = false;
			logFederateHandles = false;
			
			String propertyValue = System.getProperty( PROPERTY_LOG_WITH_HANDLES, "" );

			// update handles value
			valueOfPrintWithHandles = propertyValue;
			if( valueOfPrintWithHandles.contains("objectClass") )
				logObjectClassHandles = true;
			if( valueOfPrintWithHandles.contains("attributeClass") )
				logAttributeClassHandles = true;
			if( valueOfPrintWithHandles.contains("interactionClass") )
				logInteractionClassHandles = true;
			if( valueOfPrintWithHandles.contains("parameterClass") )
				logParameterClassHandles = true;
			if( valueOfPrintWithHandles.contains("objectInstance") )
				logObjectInstanceHandles = true;
			if( valueOfPrintWithHandles.contains("space") )
				logSpaceHandles = true;
			if( valueOfPrintWithHandles.contains("dimension") )
				logDimensionHandles = true;
			if( valueOfPrintWithHandles.contains("federate") )
				logFederateHandles = true;
			
			// update the name values
			propertyValue = System.getProperty( PROPERTY_LOG_WITH_NAMES, "" );
			valueOfPrintWithNames = propertyValue;
			if( valueOfPrintWithNames.contains("objectClass") )
				logObjectClassHandles = false;
			if( valueOfPrintWithNames.contains("attributeClass") )
				logAttributeClassHandles = false;
			if( valueOfPrintWithNames.contains("interactionClass") )
				logInteractionClassHandles = false;
			if( valueOfPrintWithNames.contains("parameterClass") )
				logParameterClassHandles = false;
			if( valueOfPrintWithNames.contains("objectInstance") )
				logObjectInstanceHandles = false;
			if( valueOfPrintWithNames.contains("space") )
				logSpaceHandles = false;
			if( valueOfPrintWithNames.contains("dimension") )
				logDimensionHandles = false;
			if( valueOfPrintWithNames.contains("federate") )
				logFederateHandles = false;
		}
	}

	public static boolean isPrintHandlesForObjectClass()
	{
		updateLogValues();
		return logObjectClassHandles;
	}
	
	public static boolean isPrintHandlesForAttributeClass()
	{
		updateLogValues();
		return logAttributeClassHandles;
	}
	
	public static boolean isPrintHandlesForInteractionClass()
	{
		updateLogValues();
		return logInteractionClassHandles;
	}
	
	public static boolean isPrintHandlesForParameterClass()
	{
		updateLogValues();
		return logParameterClassHandles;
	}
	
	public static boolean isPrintHandlesForSpaces()
	{
		updateLogValues();
		return logSpaceHandles;
	}
	
	public static boolean isPrintHandlesForDimensions()
	{
		updateLogValues();
		return logDimensionHandles;
	}
	
	public static boolean isPrintHandlesForObjects()
	{
		updateLogValues();
		return logObjectInstanceHandles;
	}
	
	public static boolean isPrintHandlesForFederates()
	{
		updateLogValues();
		return logFederateHandles;
	}

	/**
	 * @return True if the RTI ambassador should throw an exception if a federate attempts to
	 *         call a method that is not yet support by Portico. False otherwise.
	 */
	public static boolean shouldThrowExceptionForUnsupportedCall()
	{
		return getBooleanProperty( PROPERTY_UNSUPPORTED_EXCEPTIONS, "false" );
	}
	
	public static boolean isObjectNamingNegotiated()
	{
		return getBooleanProperty( PROPERTY_NEGOTIATE_OBJECT_NAMES, "false" );
	}

	public static boolean isUniqueFederateNamesRequired()
	{
		return getBooleanProperty( PROPERTY_UNIQUE_FEDERATE_NAMES, "true" );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// C++ Property Methods /////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The Portico C++ interface is a thin JNI wrapper around the Java library. When it starts,
	 * it creates a JVM and loads the Java library (and can thus communicate with it). To allow
	 * the Java side to communicate back with the C++ side, the Java side must load the C++
	 * libraries (we call this "load back").
	 * <p/>
	 * We load libraries by name. If the C++ debug libraries have been used, their name will be
	 * the standard library name with "d" on the end. To ensure we load the library by the correct
	 * name, if the debug C++ libraries loaded us, we have to use the name "[libname]d". When the
	 * debug C++ libraries load us, they will set a system property outlining whether they are in
	 * debug or release mode. This method will return true if the debug libraries should be loaded. 
	 */
	public static boolean isCppDebugSession()
	{
		String property = System.getProperty(PROPERTY_CPP_MODE,"release").trim().toLowerCase();
		return property.equals("debug");
	}

	/**
	 * Is the C++ federate that called the Java runtime a 32-bit C++ application?
	 */
	public static boolean isCpp32bit()
	{
		String property = System.getProperty(PROPERTY_CPP_ARCH,"x86").trim().toLowerCase();
		return property.equals("x86");
	}
	
	/**
	 * Is the C++ federate that called the Java runtime a 64-bit C++ application?
	 */
	public static boolean isCpp64bit()
	{
		return !isCpp32bit();
	}

	public static String getCppCompilerString()
	{
		return System.getProperty( PROPERTY_CPP_COMPILER, "" );
	}

	/**
	 * Gets the HLA interface version string for the C++ interface that loaded the Java side
	 * of the RTI (hla13, dlc13, ieee1516e).
	 */
	public static String getCppHlaVersion()
	{
		return System.getProperty( PROPERTY_CPP_HLAVERSION, "unknown" );
	}

	/**
	 * Is the C++ federate that called the Java runtime an HLA v1.3 federate?
	 */
	public static boolean isCppHla13()
	{
		return getCppHlaVersion().equals("hla13");
	}
	
	/**
	 * Is the C++ federate that called the Java runtime an HLA v1.3 (DLC) federate?
	 */
	public static boolean isCppDlc13()
	{
		return getCppHlaVersion().equals("dlc13");
	}
	
	/**
	 * Is the C++ federate that called the Java runtime an IEEE-1516e federate?
	 */
	public static boolean isCppIeee1516e()
	{
		return getCppHlaVersion().equals("ieee1516e");
	}

	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Container Methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////

	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Helper Methods ////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Use file method to safely convert File instances to URL instances. This particular task is
	 * notorious for causing problems. This method should be used to centralise the process so that
	 * any problems encountered can be easily fixed.
	 */
	public static URL fileToUrl( File file ) throws JConfigurationException
	{
		try
		{
			return file.toURI().toURL();
		}
		catch( Exception e )
		{
			throw new JConfigurationException( "Problem converting file ["+file+"] to URL: "+
			                                   e.getMessage(), e );
		}
	}
	
	/**
	 * Sleep for the given number of milliseconds or until interrupted.
	 */
	public static void sleep( long millis )
	{
		try
		{
			Thread.sleep( millis );
		}
		catch( Exception e )
		{
			return;
		}
	}

	/**
	 * This is a helper method that will convert a set of integers (presumed to be handles)
	 * into a String that lists them in the format: {1,2,3,x}
	 */
	public static String setToString( Set<Integer> set )
	{
		if( set == null || set.isEmpty() )
			return "{empty}";
		
		// generate the string containing the handles
		StringBuilder builder = new StringBuilder( "{" );

		// treesets are sorted by default, thus, the output will be sorted
		Iterator<Integer> iterator = new TreeSet<Integer>(set).iterator(); 
		while( iterator.hasNext() )
		{
			builder.append( iterator.next() );
			if( iterator.hasNext() )
				builder.append( "," );
		}
		
		builder.append( "}" );
		return builder.toString();
	}
	
	/**
	 * This is the same as {@link #setToString(Set)}, except that it works on an int[].
	 */
	public static String arrayToString( int[] array )
	{
		if( array == null || array.length == 0 )
			return "{empty}";
		
		// generate the return string
		StringBuilder builder = new StringBuilder( "{" );
		for( int i = 0; i < array.length; i++ )
		{
			builder.append( array[i] );
			if( i < array.length )
				builder.append( "," );
		}
		
		builder.append( "}" );
		return builder.toString();
	}
	
	/**
	 * The same as {@link #arrayToString(int[])} except that it adds information about the size
	 * of the values provided in the second 2d array. Sample output: {32(12b),3(1b)}
	 * 
	 * @param array The handles to put in the output
	 * @param values The values associated with the handles, the length of the values array is used
	 */
	public static String arrayToStringWithSizes( int[] array, byte[][] values )
	{
		if( array == null || array.length == 0 )
			return "{empty}";
		
		// generate the return string
		StringBuilder builder = new StringBuilder( "{" );
		for( int i = 0; i < array.length; i++ )
		{
			// put the handle value in there
			builder.append( array[i] );

			// put the amount of value data in there
			builder.append( "(" );
			builder.append( values[i].length );
			builder.append( "b)" );
			
			if( i < array.length )
				builder.append( "," );
		}
		
		builder.append( "}" );
		return builder.toString();
	}
	
	/**
	 * Returns a string representation of the contents of the map in the following form:
	 * <p/>
	 * "{15(4b),17(10b),16(4b)}"
	 * <p/>
	 * The first numbers are the handles, the numbers in parenthesis are the size of the byte[]
	 * corresponding to those numbers.
	 */
	public static String mapToStringWithSizes( HashMap<Integer,byte[]> map )
	{
		if( map == null )
			return "{null}";
		else if( map.isEmpty() )
			return "{empty}";
				
		StringBuilder builder = new StringBuilder( "{" );
		boolean first = true;
		for( Integer key : map.keySet() )
		{
			if( first )
				first = false;
			else
				builder.append( "," );
			
			builder.append( key );
			builder.append( "(" );
			byte[] value = map.get( key );
			if( value == null )
				builder.append( "null" );
			else
				builder.append( value.length );
			
			builder.append( "b)" );
		}
		
		builder.append( "}" );
		return builder.toString();
	}
}
