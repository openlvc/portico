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
package org.portico.impl.hla1516e.dtx;

import org.portico.impl.hla1516e.types.encoding.HLA1516eEncoderFactory;
import org.portico.lrc.PorticoConstants;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIinternalError;

/**
 * Factory to create and return new Portico 1516e RTIambassador implementations with the Portico
 * Datatype Extensions.
 * <p/>
 * The dtx RTIambassador is identical to the standard 1516e RTIambassador but for the extra methods
 * that query attribute and parameter datatypes. To access to these methods, cast the return value 
 * of {@link #getRtiAmbassador()} to a {@link Rti1516eDtxAmbassador}.
 */
public class Rti1516eDtxFactory implements RtiFactory
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	public static final String RTI_NAME = "portico-dtx";

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public RTIambassador getRtiAmbassador() throws RTIinternalError
	{
		return new Rti1516eDtxAmbassador();
	}

	public EncoderFactory getEncoderFactory() throws RTIinternalError
	{
		return new HLA1516eEncoderFactory();
	}

	/**
	 * @return The string "portico-dtx". This is used when code specifically requests
	 *         an RtiFactory by name. The {@link RtiFactoryFactory} will look through
	 *         all the availabe service providers and ask each what its name is via
	 *         a call to this method, returning the first to match the requested name. 
	 */
	public String rtiName()
	{
		return RTI_NAME;
	}

	public String rtiVersion()
	{
		return PorticoConstants.RTI_VERSION;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
