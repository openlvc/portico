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
package org.portico2.common.services.time.data;

/**
 * Enumeration that represents the various time advance request statuses a federate can have.
 */
public enum TAR
{
	NONE,        // no current time advance request pending
	REQUESTED,   // currently in a time advance request
	AVAILABLE,   // currently in a time advance request available
	PROVISIONAL; // an advance callback has been issued, but not yet consumed
}
