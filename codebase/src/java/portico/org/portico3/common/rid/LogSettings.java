/*
 *   Copyright 2022 The Portico Project
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
package org.portico3.common.rid;

import java.io.File;

import org.portico3.common.compatibility.JConfigurationException;
import org.portico3.rti.commandline.Argument;
import org.portico3.rti.commandline.CommandLine;

/**
 * Represents the logging configuration options from a {@link RID}.
 */
public class LogSettings
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	// General Options
	private String logLevel;
	private File logDir;
	private boolean printFom;
	
	// Log-With Options
	// True = Handle, False = Names
	private boolean logWithHandleObjectClass;
	private boolean logWithHandleAttributeClass;
	private boolean logWithHandleInteractionClass;
	private boolean logWithHandleParameterClass;
	private boolean logWithHandleObjectInstance;
	private boolean logWithHandleSpace;
	private boolean logWithHandleDimension;
	private boolean logWithHandleFederate;
	
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	protected LogSettings()
	{
		// General Options
		this.logLevel = "INFO";
		this.logDir = new File( "logs" );
		this.printFom = false;
		
		// Low-With Options
		this.logWithHandleObjectClass       = true;    // handle
		this.logWithHandleAttributeClass    = true;    // handle
		this.logWithHandleInteractionClass  = true;    // handle
		this.logWithHandleParameterClass    = true;    // handle
		this.logWithHandleObjectInstance    = false;   // name
		this.logWithHandleSpace             = true;    // handle
		this.logWithHandleDimension         = true;    // handle
		this.logWithHandleFederate          = false;   // name
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////////
	/// General Options   //////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public String getLogLevel()
	{
		return logLevel;
	}

	public void setLogLevel( String logLevel )
	{
		this.logLevel = logLevel;
	}

	public File getLogDir()
	{
		return logDir;
	}

	public void setLogDir( File directory )
	{
		this.logDir = directory;
	}
	
	public void setLogDir( String directory )
	{
		this.logDir = new File( directory );
	}

	public boolean isPrintFom()
	{
		return printFom;
	}

	public void setPrintFom( boolean printFom )
	{
		this.printFom = printFom;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Log-With Options   /////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	public boolean isLogHandlesForObjectClass()
	{
		return logWithHandleObjectClass;
	}

	public void setLogHandlesForObjectClass( boolean printWithHandles )
	{
		this.logWithHandleObjectClass = printWithHandles;
	}

	public boolean isLogHandlesForAttributeClass()
	{
		return logWithHandleAttributeClass;
	}

	public void setLogHandlesForAttributeClass( boolean printWithHandles )
	{
		this.logWithHandleAttributeClass = printWithHandles;
	}

	public boolean isLogHandlesForInteractionClass()
	{
		return logWithHandleInteractionClass;
	}

	public void setLogHandlesForInteractionClass( boolean printWithHandles )
	{
		this.logWithHandleInteractionClass = printWithHandles;
	}

	public boolean isLogHandlesForParameterClass()
	{
		return logWithHandleParameterClass;
	}

	public void setLogHandlesForParameterClass( boolean printWithHandles )
	{
		this.logWithHandleParameterClass = printWithHandles;
	}

	public boolean isLogHandlesForObjects()
	{
		return logWithHandleObjectInstance;
	}

	public void setLogHandlesForObjects( boolean printWithHandles )
	{
		this.logWithHandleObjectInstance = printWithHandles;
	}

	public boolean isLogHandlesForSpaces()
	{
		return logWithHandleSpace;
	}

	public void setLogHandlesForSpaces( boolean printWithHandles )
	{
		this.logWithHandleSpace = printWithHandles;
	}

	public boolean isLogHandlesForDimensions()
	{
		return logWithHandleDimension;
	}

	public void setLogHandlesForDimensions( boolean printWithHandles )
	{
		this.logWithHandleDimension = printWithHandles;
	}

	public boolean isLogHandlesForFederates()
	{
		return logWithHandleFederate;
	}

	public void setLogHandlesForFederates( boolean printWithHandles )
	{
		this.logWithHandleFederate = printWithHandles;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/// Internal Use Only   ////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	protected void applyOverrides( CommandLine commandline ) throws JConfigurationException
	{
		if( commandline.isPresent(Argument.LogLevel) )
			this.setLogLevel( commandline.getValue(Argument.LogLevel) );
		
		if( commandline.isPresent(Argument.LogDir) )
			this.setLogDir( commandline.getValue(Argument.LogDir) );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
