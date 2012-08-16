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
package org.portico.dlc;

import org.portico.impl.hla13.Rti13Ambassador;
import org.portico.impl.hla13.types.HLA13AttributeHandleSet;
import org.portico.impl.hla13.types.HLA13FederateHandleSet;
import org.portico.impl.hla13.types.HLA13SuppliedAttributes;
import org.portico.impl.hla13.types.HLA13SuppliedParameters;
import org.portico.lrc.PorticoConstants;

import hla.rti.AttributeHandleSet;
import hla.rti.FederateHandleSet;
import hla.rti.SuppliedAttributes;
import hla.rti.SuppliedParameters;
import hla.rti.RTIinternalError;
import hla.rti.jlc.RTIambassadorEx;
import hla.rti.jlc.RtiFactory;

/**
 * This class provides the {@link RtiFactory} implementation required by the HLA 1.3 DLC spec
 * (<a href="http://www.sisostds.org/index.php?tg=articles&idx=More&article=40&topics=18">accessible
 * here</a>). The methods of this class create and return Portico-specific implementations of the
 * various HLA specified types.
 * <p/>
 * This class should <b>NEVER</b> be referenced directly. Rather, instances are obtained through
 * the {@link hla.rti.jlc.RtiFactoryFactory} class. You can reference the fully qualified class
 * name of this class when using the factory-factory, but don't create instance of this class
 * itself.
 */
public class HLA13RTIFactory implements RtiFactory
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

	public HLA13RTIFactory()
	{
		// initialization will happen in the createRtiAmbassador() method 
	}
	
	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public RTIambassadorEx createRtiAmbassador() throws RTIinternalError
	{
		// return the RTIamb from the LRC //
		return new Rti13Ambassador();
	}

	public AttributeHandleSet createAttributeHandleSet()
	{
		return new HLA13AttributeHandleSet();
	}

	public FederateHandleSet createFederateHandleSet()
	{
		return new HLA13FederateHandleSet();
	}

	public SuppliedAttributes createSuppliedAttributes()
	{
		return new HLA13SuppliedAttributes();
	}

	public SuppliedParameters createSuppliedParameters()
	{
		return new HLA13SuppliedParameters();
	}

	public String RtiName()
	{
		return PorticoConstants.RTI_NAME;
	}

	public String RtiVersion()
	{
		return PorticoConstants.RTI_VERSION;
	}

	public long getMinExtent()
	{
		return PorticoConstants.MIN_EXTENT;
	}

	public long getMaxExtent()
	{
		return PorticoConstants.MAX_EXTENT;
	}
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
