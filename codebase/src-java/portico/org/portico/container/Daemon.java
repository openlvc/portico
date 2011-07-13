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
package org.portico.container;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Basic marker to signal that a class is a Daemon. Any classes marked with this annotation should
 * also implement the {@link IDaemon} interface (which outlines the contract all Daemons must
 * fulfil).
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Daemon
{
	/** Each Daemon should have a unique name. If this isn't provided, the name that will
	    be given to the instances of classes marked with this annotation will be the non-qualified
	    name of the implementation class */
	String name() default "<undefined>";
}
