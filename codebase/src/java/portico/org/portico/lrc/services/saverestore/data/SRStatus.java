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
package org.portico.lrc.services.saverestore.data;

public enum SRStatus
{
	NONE,           // no active save
	REQUESTED,      // a save registration has been requested
    INITIATED,      // a save has been initiated
    BEGUN,          // the federate has begun saving
    COMPLETE,       // the federate has successfully finished saving
    NOT_COMPLETE,   // the federate has unsuccessfully finished saving
};
