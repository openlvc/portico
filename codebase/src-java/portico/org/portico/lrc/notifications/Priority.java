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

/** This enum describes where a notification fits in the {@link NotificationManager}. It
the priority of a listener is HIGH, it will be added to the front of the list. If the
priority is normal, it'll be added to the end of the list (but infront of any "low" listeners).
If it is low, it will be added to and kept at the end of the list. */
public enum Priority{ HIGH, NORMAL, LOW };
