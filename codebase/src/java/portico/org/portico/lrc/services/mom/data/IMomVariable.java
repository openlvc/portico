/*
 *   Copyright 2021 The Portico Project
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
package org.portico.lrc.services.mom.data;

import org.portico.impl.HLAVersion;

/**
 * Method interface for MOM class variables that will allow us to store method references that
 * point to a particular method on a particular instance of a MOM object. We use these to get an
 * encoded value that can be put into MOM object updates.
 */
public interface IMomVariable
{
	byte[] getValue( HLAVersion version );
}
