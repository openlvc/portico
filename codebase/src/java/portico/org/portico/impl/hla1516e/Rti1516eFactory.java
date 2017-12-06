/*
 *   Copyright 2012 The Portico Project
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
package org.portico.impl.hla1516e;

import org.portico.impl.hla1516e.types.encoding.HLA1516eEncoderFactory;
import org.portico.lrc.PorticoConstants;

import hla.rti1516e.RTIambassador;
import hla.rti1516e.RtiFactory;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.exceptions.RTIinternalError;

/**
 * Factory to create and return new Portico 1516e RTIambassador implementations and
 * provide various other useful information about the RTI.
 * <p/>
 * To actually create an RTI, you need to first get the {@link RtiFactoryFactory},
 * from which you can get an implementation of {@link RtiFactory} (this class).
 * You can then get your ambassador from there. 
 */
public class Rti1516eFactory implements RtiFactory
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
	public RTIambassador getRtiAmbassador() throws RTIinternalError
	{
		return new Rti1516eAmbassadorEx();
	}

	public EncoderFactory getEncoderFactory() throws RTIinternalError
	{
		return new HLA1516eEncoderFactory();
	}

	/**
	 * @return The string "portico". This is used when code specifically requests
	 *         an RtiFactory by name. The {@link RtiFactoryFactory} will look through
	 *         all the availabe service providers and ask each what its name is via
	 *         a call to this method, returning the first to match the requested name. 
	 */
	public String rtiName()
	{
		return "portico";
	}

	public String rtiVersion()
	{
		return PorticoConstants.RTI_VERSION;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
