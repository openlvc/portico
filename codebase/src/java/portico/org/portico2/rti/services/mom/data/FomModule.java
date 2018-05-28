/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.rti.services.mom.data;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.portico.lrc.compat.JCouldNotOpenFED;
import org.portico.lrc.compat.JErrorReadingFED;

/**
 * Represents the raw, unparsed content of a FED file
 */
public class FomModule implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private URL url;
	private String content;
	private String designator;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FomModule()
	{
		this.content = null;
		this.designator = null;
	}

	public FomModule( URL moduleUrl )
		throws JCouldNotOpenFED, JErrorReadingFED
	{
		try
		{
			URI moduleUri = moduleUrl.toURI();
			File moduleFile = new File( moduleUri );
			this.designator = moduleFile.getName();
			
			Path modulePath = Paths.get( moduleUri );
			this.content = new String( Files.readAllBytes(modulePath) );
		}
		catch( Exception e )
		{
			// Should not happen as the module was parsed into XML before this constructor was called
			throw new JErrorReadingFED( moduleUrl.toString() );
		}
	}
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public String getDesignator()
	{
		return this.designator;
	}
	
	public String getContent()
	{
		return this.content;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
