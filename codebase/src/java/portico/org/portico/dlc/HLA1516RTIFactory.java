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

import org.portico.impl.hla1516.Rti1516Ambassador;
import org.portico.lrc.PorticoConstants;

import hla.rti1516.RTIambassador;
import hla.rti1516.RTIinternalError;
import hla.rti1516.jlc.RtiFactory;

/**
 * This class provides the {@link RtiFactory} implementation required by the HLA 1516 DLC spec
 * (<a href="http://www.sisostds.org/index.php?tg=articles&idx=More&article=40&topics=18">accessible
 * here</a>). The methods of this class create and return Portico-specific implementations of the
 * various HLA specified types.
 * <p/>
 * This class should <b>NEVER</b> be referenced directly. Rather, instances are obtained through
 * the {@link hla.rti1516.jlc.RtiFactoryFactory} class. You can reference the fully qualified class
 * name of this class when using the factory-factory, but don't create instance of this class
 * itself.
 */
public class HLA1516RTIFactory implements RtiFactory
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
		try
		{
			return new Rti1516Ambassador();
		}
		catch( RTIinternalError rtie )
		{
			throw rtie;
		}
		catch( Exception e )
		{
			throw new RTIinternalError( e );
		}
	}

	public String RtiName()
	{
		return PorticoConstants.RTI_NAME;
	}

	public String RtiVersion()
	{
		return PorticoConstants.RTI_VERSION;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
