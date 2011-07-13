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
package org.portico.lrc.notifications;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as being a notification listener. When a new {@link NotificationManager} is
 * created, all classes with this annotation will have new instances created and will be added
 * to the manager. Each listener can have a {@link Priority} which defaults to
 * {@link Priority#NORMAL}.
 * <p/>
 * When a new LRC is created, it will instantiate a new {@link NotificationManager}, which in turn
 * will search the Portico container plug-in path for all classes that present this annotation.
 * An instance of each of these classes will then be instantied and added to this manager. 
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NotificationListener
{
	Priority priority() default Priority.NORMAL;
}
