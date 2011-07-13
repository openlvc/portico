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
package org.portico.lrc;

import java.util.concurrent.TimeUnit;

/**
 * Central location for LRC properties
 */
public class LRCProperties
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	//////////////////////////
	// System Property Keys //
	//////////////////////////
	/** System property used to specify the log level for the lrc callback processor. By
	    default, this logger gets the same level as the main lrc logger
	    <p/>NOTE: Not supported after v0.8 */
	public static final String PROP_CALLBACK_LOGLEVEL = "portico.lrc.callback.loglevel";
	
	/** System property used to specify the tick timeout */
	public static final String PROPERTY_TT = "portico.lrc.tt";

	/** System property used to specify the number of messages that are allowed in the queue
	    before the LRC starts issuing warnings about its size, default: 500 */
	public static final String PROPERTY_QUEUE_WARNING_COUNT = "portico.lrc.queue.warningCount";

	//////////////////////////////
	// Configuration Properties //
	//////////////////////////////
	/** The key the reference to the LRC instance will be stored under in the initialization
	    properties given to all LRC handers when they are created */
	public static final String KEY_LRC = "portico.lrc";
	
	////////////////////////
	// General Properties //
	////////////////////////
	/** default LRC tick time - measured in nanoseconds */
	public static long LRC_TICK_TIMEOUT =
		TimeUnit.MILLISECONDS.toNanos( Long.parseLong(System.getProperty(PROPERTY_TT,"5") ) );
	
	/** the number of messages the queue can hold before the LRC starts issuing warnings about
	    not ticking enough, default: 500 */
	public static int LRC_QUEUE_WARNING_COUNT =
		Integer.parseInt( System.getProperty(PROPERTY_QUEUE_WARNING_COUNT,"500") );
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
