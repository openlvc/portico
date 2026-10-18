/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups.channel;

import java.io.Serializable;

import org.portico.lrc.model.ObjectModel;

/**
 * The payload of a create federation notification, for a federation that was created with
 * something other than the default logical time implementation.
 * <p/>
 * A create notification used to carry the bare {@link ObjectModel}, and it still does whenever the
 * federation uses the default implementation, so that a channel shared with an older Portico keeps
 * working exactly as it did. Only a federation that asks for another implementation - which an
 * older Portico could not have honoured in any case - sends this instead.
 */
public class FederationCreation implements Serializable
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	private static final long serialVersionUID = 98121116105109L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ObjectModel fom;
	private String timeImplementationName;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public FederationCreation( ObjectModel fom, String timeImplementationName )
	{
		this.fom = fom;
		this.timeImplementationName = timeImplementationName;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public ObjectModel getFom()
	{
		return this.fom;
	}

	public String getTimeImplementationName()
	{
		return this.timeImplementationName;
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
