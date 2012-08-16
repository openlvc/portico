/*
 *   Copyright 2010 The Portico Project
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
package org.portico.bindings.ptalk.protocol;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation defines which classes represent a PTalk protocol. For configuration purposes,
 * each {@link Protocol} must have a name. During configuration, the protocol can consult the 
 * system properties to look for settings with this name. For more info, see {@link IProtocol}.
 * <p/>
 * Additionally, all classes declaring this annotation must implement the {@link IProtocol}
 * interface. If they do not, they will not be loaded.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Protocol
{
	/** The name given to the protocol. This string is used to identify the protocol in any
	    configuration options. */
	String name() default "<unknown>";
}
