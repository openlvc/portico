/*
 *   Copyright 2008 The Portico Project
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
package org.portico.misc;

import java.net.URL;

import org.portico.impl.hla13.Rti13Ambassador;
import org.portico.impl.hla1516.Rti1516Ambassador;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups={"CrossVersionParsingTest","misc","fom","fom13"})
public class CrossVersionParsingTest
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private URL valid13Fom;
	private URL valid1516Fom;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	@BeforeClass(alwaysRun=true)
	public void beforeClass()
	{
		this.valid13Fom = ClassLoader.getSystemResource( "fom/testfom.fed" );
		this.valid1516Fom = ClassLoader.getSystemResource( "fom/testfom.xml" );
	}
	
	@Test
	public void parse1516From13() throws Exception
	{
		Rti13Ambassador rtiamb = new Rti13Ambassador();
		rtiamb.createFederationExecution( "miscFederation", valid1516Fom );
		rtiamb.destroyFederationExecution( "miscFederation" );
	}
	
	@Test
	public void parse13From1516() throws Exception
	{
		Rti1516Ambassador rtiamb = new Rti1516Ambassador();
		rtiamb.createFederationExecution( "miscFederation", valid13Fom );
		rtiamb.destroyFederationExecution( "miscFederation" );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
