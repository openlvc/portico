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
package org.portico.lrc.model;

/**
 * Enumeration representing the sharing of interactions and object attributes 
 */
public enum Sharing
{
	PUBLISH,
	SUBSCRIBE,
	PUBLISHSUBSCRIBE,
	NEITHER;

	/**
	 * If the given FOM string is "PublishSubscribe", PUBLISHSUBSCRIBE is returned. If it is "Subscribe"
	 * then SUBSCRIBE is returned. If it is "Publish" then PUBLISH is returned. If the type "Neither" or
	 * is not known, NEITHER is returned.
	 */
	public static Sharing fromFomString( String fomString )
	{
		if( fomString.equalsIgnoreCase("PublishSubscribe") )
			return Sharing.PUBLISHSUBSCRIBE;
		if( fomString.equalsIgnoreCase("Subscribe") )
			return Sharing.SUBSCRIBE;
		if( fomString.equalsIgnoreCase("Publish") )
			return Sharing.PUBLISH;
		return NEITHER;
	}
};
