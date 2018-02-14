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
package org.portico.impl;

import org.portico.lrc.LRC;

/**
 * Marker interface to help remove any tight-coupling between the RTI and Federate ambassador
 * interfaces for a specific HLA version and the LRC infrastructure itself.
 * <p/>
 * Different HLA interfaces need to hold references to different sets of objects. So that a single
 * {@link LRC} can be used with any HLA interface type, each will hold a reference to an
 * {@link ISpecHelper}. The implementations of this class can be HLA-interface specific. Any
 * components that use the ISpecHelper will have to cast it to the appropriate type before it is
 * able to use it to extract HLA-interface specific things.
 */
public interface ISpecHelper
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public HLAVersion getHlaVersion();
}
