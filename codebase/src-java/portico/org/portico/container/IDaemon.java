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
package org.portico.container;

import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;

/**
 * <b>NOTE:</b> For automatic configuration by the Portico framework, all Daemons should declare
 * the {@link Daemon} annotation. Further, all implementations should have a no-arg constructor
 * so that instances can be created via reflection.
 * <p/>
 * In Portico, a {@link IDaemon} represents some process of component that needs to run inside the
 * local {@link Container}. This is just a generic facility to allow additional behaviour to be
 * plugged into the Portico infrastructure. Whether that behaviour has anything to do with the HLA
 * is of no concern to the rest of the framework. A {@link IDaemon} can really be used for any
 * purpose. To give you some examples (and hopefully set your mind wandering for new ideas), here
 * are some possible examples:
 * 
 * <ul>
 *   <li>Embed a Web Server to serve content from</li>
 *   <li>A "keep alive" daemon that sends a heartbeat to the RTI at regular intervals</li>
 *   <li>An IRC bot reporting on the status of various internal components</li>
 *   <li>A federation bridge that can send messages back and forth between federations</li>
 *   <li>A GUI displaying federation statistics or LRC loaded</li>
 *   <li>etc... anything you can come up with!</li>
 * </ul>
 * 
 * As you can see, some of these have a direct relation to the HLA, while others have nothing to do
 * with it at all. Remember, <i>a {@link IDaemon} is just a generic process that is associated with
 * a {@link Container}</i>.
 * 
 * <p/>
 * <b>Daemon Configuration</b>
 * <p/>
 * Configuration information should generally be passed to a {@link IDaemon} via java system
 * properties. This is the <b>highly recommended</b> way for a {@link IDaemon} to extract its
 * configuration information, as it means that any data provided in a RID file will be available.
 * In Portico, a RID file is just a java properties file. On startup, all the properties are read
 * in and registered as system properties. As configuration via a RID file is the most common way
 * to provide HLA RTI configuration data, it is strongly suggested that you use system properties
 * for {@link IDaemon} configuration.
 * <p/>
 * If your Daemon has more elaborate configuration needs, it is suggested that you read your
 * configuration from a seprate file (formatted however you like it), but that you get the location
 * of that file from a system property. Remember, when getting the value for a system property, you
 * can specify a default value to use if that property isn't set (an excellent way to apply 
 * defaults). The {@link PorticoConstants} class has some examples of this (the DEFAULT_TIMEOUT
 * static variable being one), so see the code for that class if you want an example.
 * 
 * <p/>
 * <b>Daemons and Threading</b>
 * <p/>
 * All {@link IDaemon} lifecycle methods are expected to return in a timely fashion. Thus, if your
 * daemon implementation requires ongoing processing to occur, it will have to start a separate
 * thread during the {@link #startDaemon()} call for this work to run in (and stop/clean up that
 * thread during the {@link #stopDaemon()} call).
 * 
 * <p/>
 * <b>New {@link LRC} notification</b>
 * <p/>
 * The group of {@link IDaemon}s exists inside a {@link Container}. However, all the interesting
 * activity is going on down at the {@link LRC} level. It is for this reason that each Daemon
 * will be notified whenever a new LRC is created and linked into a container. The Daemon can
 * then take whatever action it needs to hook into the LRC. 
 */
public interface IDaemon
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Each <i>type</i> {@link IDaemon} should have a unique, identifiable name. Generally speaking,
	 * this method can be implemented as a simple <code>return "ircbot";</code>, where "ircbot" is
	 * the name of the Daemon. This name is generally used in logging and other reporting.
	 */
	public String getName();
	
	/**
	 * This should only be called by the Portico framework. The name given to a Daemon will be
	 * the value extracted from the {@link Daemon} class-level annotation. If no name is provided
	 * in that annotation, the name of the implementation class (non-qualified) will be used.
	 */
	public void setName( String name );
	
	/**
	 * This method should return <code>true</code> if the {@link IDaemon} is active and running,
	 * <code>false</code> otherwise.
	 */
	public boolean isActive();

	/**
	 * This method is called before either of the other lifecycle methods and gives the Daemon a
	 * chance to configure itself. For more information on the recommended approach for obtaining
	 * configuration information from a user, see the class level comment {@link IDaemon here}.
	 * The {@link Container} used as a parameter is the Container in which the Daemon is registered.
	 */
	public void configure( Container container ) throws JConfigurationException;
	
	/**
	 * When the {@link Container} is ready, it will start the {@link IDaemon}s contained within it.
	 * At that time, this method will be called. Daemon implementations should take this opportunity
	 * to start any threads they need to complete their work, or generally kick off whatever is in
	 * need of kicking. The Daemon is expected to return from this method in a timely fashion, so
	 * it cannot use the current thread to perform any ongoing work.
	 */
	public void startDaemon() throws Exception;
	
	/**
	 * On {@link Container} shutdown, this method will be called to notify the {@link IDaemon} that
	 * processing is over and it is time to exit. Implementations should take this opportunity to
	 * kill any worker threads they have running, close any connections they have open, and do any
	 * general cleanup that is necessary.
	 */
	public void stopDaemon() throws Exception;
	
	/**
	 * When the {@link Container} starts a new {@link LRC}, this method will be called on all
	 * the {@link IDaemon}s, giving them access to it. The Daemon can then store it for later use
	 * or hook into it as required.
	 */
	public void lrcCreated( LRC lrc );

	/**
	 * Informs the {@link IDaemon} that the previously started {@link LRC} has become inactive
	 * and has been removed from the {@link Container}. The Daemon should now remove any storage
	 * it has relating to the lrc and break all links. It's dead. It's time to move on.
	 */
	public void lrcDestroyed( LRC lrc );

}
