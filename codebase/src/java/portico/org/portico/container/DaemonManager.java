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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.portico.lrc.LRC;
import org.portico.lrc.compat.JConfigurationException;

/**
 * This class manages a set of {@link IDaemon} implementations on behalf of a {@link Container}. It
 * aggregates together all the configured Daemon instances and makes it easy to manage their
 * lifecycle as one.
 */
public class DaemonManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private Logger logger;
	private Container container;
	private Map<String,IDaemon> daemons;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	protected DaemonManager( Container container )
	{
		this.container = container;
		this.logger = container.logger;
		this.daemons = new HashMap<String,IDaemon>();
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Add a new {@link IDaemon} to the group that is managed by this class.
	 */
	public synchronized void registerDaemon( IDaemon daemon )
	{
		if( daemon == null )
			throw new IllegalArgumentException( "Can't register Daemon: daemon was null" );
		
		if( daemons.containsKey(daemon.getName()) )
			throw new IllegalArgumentException( "Can't register Daemon: Already exists (" +
			                                    daemon.getName() + ")" );
		
		daemons.put( daemon.getName(), daemon );
		logger.debug( "registered daemon [" + daemon.getName() + "]" );
	}

	/**
	 * This method will find and remove the {@link IDaemon} of the given name from the group managed
	 * by this class. If there is no Daemon by that name, null will be returned. Note that this
	 * method will <b>NOT</b> stop the Daemon before removing it. It is up to you to stop the
	 * Daemon before removing it.
	 */
	public synchronized IDaemon removeDaemon( String daemonName )
	{
		if( daemons.containsKey(daemonName) )
			logger.debug( "removed daemon [" + daemonName + "]" );

		return daemons.remove( daemonName );
	}

	/**
	 * Returns <code>true</code> if there is a {@link IDaemon} of the given name registered with
	 * this manager, <code>false</code> otherwise.
	 */
	public synchronized boolean containsDaemon( String daemonName )
	{
		return daemons.containsKey( daemonName );
	}

	/**
	 * This will return an <i>unmodifiable</i> Set of all the contained {@link IDaemon}s. If there
	 * are no registered Daemons, an empty set will be returned.
	 */
	public synchronized Collection<IDaemon> getAllDaemons()
	{
		return Collections.unmodifiableCollection( daemons.values() );
	}
	
	public synchronized int size()
	{
		return daemons.size();
	}

	/**
	 * This method will attempt to start all the {@link IDaemon}s registered with the manager. If
	 * a given Daemon fails to configure or start, the error will be logged, but the manager will
	 * keep on going, attempting to start the other Daemons.
	 * <p/>
	 * <b>NOTE:</b> Any {@link IDaemon} that fails to start will be removed.
	 * <p/>
	 * <b>NOTE:</b> This method will only attempt to start daemons that are not active (those that
	 * return <code>false</code> from {@link IDaemon#isActive()}).
	 */
	public synchronized void startDaemons()
	{
		logger.debug( "STARTUP Trying to start [" +size()+ "] Daemons" );

		// try and start up each registered daemon that isn't active, keep count of the number
		// of daemons that are active, and the number of those that were started this time around
		int active = 0;
		int started = 0;
		
		for( String daemonName : daemons.keySet() )
		{
			// check to see if the daemon is already active
			if( daemons.get(daemonName).isActive() )
			{
				active++;
				continue;
			}
			
			try
			{
				// this will exception out if the daemon fails, or if it is already active
				startDaemon( daemonName );
				active++;
				started++;
			}
			catch( Throwable e )
			{
				logger.error( "Problem starting Daemon ["+daemonName+
				              "]. REMOVED DAEMON.", e );
				// this will have been logged in the startDaemon method, just remove the daemon
				removeDaemon( daemonName );
			}
		}
		
		logger.info( "STARTUP There are now ["+active+"/"+size()+"] active Daemons {" + started +
		             " started this time}" );
	}
	
	/**
	 * Start the {@link IDaemon} registered with the given name. If there is no daemon of that name,
	 * or there is a problem starting the daemon, an exception will be thrown.
	 */
	private synchronized void startDaemon( String daemonName ) throws Exception
	{
		logger.trace( "ATTEMPT Starting Daemon [" + daemonName + "]" );

		// locate the daemon
		IDaemon theDaemon = daemons.get( daemonName );
		if( theDaemon == null )
		{
			logger.warn( "FAILURE Can't start Daemon [" +daemonName+ "]: Daemon not registered" );
			throw new Exception( "Can't start Daemon [" +daemonName+ "]: Daemon not registered" );
		}

		// only start it if it isn't already active
		if( theDaemon.isActive() )
		{
			logger.warn( "FAILURE Can't start Daemon [" +daemonName+ "]: Daemon already active" );
			throw new Exception( "Can't start Daemon [" +daemonName+ "]: Daemon already active" );
		}
		
		// try and get the daemon to configure itself
		try
		{
			theDaemon.configure( this.container );
		}
		catch( JConfigurationException e )
		{
			logger.warn( "FAILURE Couldn't configure Daemon ["+daemonName+"]: "+e.getMessage(), e );
			throw e;
		}
		
		// try and start the daemon, log on success or error
		try
		{
			theDaemon.startDaemon();
			logger.debug( "SUCCESS Started Daemon [" +daemonName+ "]" );
		}
		catch( Exception e )
		{
			logger.warn( "FAILURE Couldn't start Daemon ["+daemonName+"]: " + e.getMessage(), e );
			throw e;
		}
	}
	
	/**
	 * This method will shutdown each of the contained {@link IDaemon}. If a Daemon fails to
	 * shutdown (throws an exception), this information will be logged, but the manager will move
	 * on to the next Daemon and attempt to shut it down.
	 */
	public synchronized void stopDaemons()
	{
		logger.debug( "SHUTDWN Trying to shutdown [" +size()+ "] Daemons" );

		// try and shutdown each active registered daemon, keep count of the number of daemons
		// that are inactive, and the number that were shutdown as part of this process
		int inactive = 0;
		int shutdown = 0;
		
		for( String daemonName : daemons.keySet() )
		{
			// check to see if the daemon is already inactive
			if( daemons.get(daemonName).isActive() == false )
			{
				inactive++;
				continue;
			}
			
			try
			{
				// will exception out if the shutdown fails (including if the daemon isn't active)
				stopDaemon( daemonName );
				inactive++;
				shutdown++;
			}
			catch( Exception e )
			{
				// ignore, it will have been logged in the stopDaemon method
			}
		}

		logger.info( "SHUTDWM There are now ["+inactive+"/"+size()+"] active Daemons {" + shutdown +
		             " shutdown this time}" );
	}
	
	/**
	 * Try and stop the registered {@link IDaemon} of the given name. If there is a problem shutting
	 * the Deamon down, or there is no Daemon of the given name, an exception will be thrown.
	 */
	private synchronized void stopDaemon( String daemonName ) throws Exception
	{
		logger.trace( "ATTEMPT Shutdown Daemon [" + daemonName + "]" );

		// locate the daemon
		IDaemon theDaemon = daemons.get( daemonName );
		if( theDaemon == null )
		{
			logger.warn( "FAILURE Can't shutdown Daemon ["+daemonName+"]: Daemon not registered" );
			throw new Exception( "Can't shutdown Daemon ["+daemonName+"]: Daemon not registered" );
		}

		// only stop it if it is active
		if( theDaemon.isActive() == false )
		{
			logger.debug( "SKIPPED Daemon [" + daemonName + "] is not active" );
			throw new Exception( "Daemon [" + daemonName + "] is not active" );
		}
		
		// try to do the shutdown
		try
		{
			theDaemon.stopDaemon();
			logger.debug( "SUCCESS Shutdown Daemon [" +daemonName+ "]" );
		}
		catch( Exception e )
		{
			logger.warn( "FAILURE Couldn't shutdown Daemon ["+daemonName+"]: "+e.getMessage(), e );
			throw e;
		}
	}
	
	/**
	 * Notify all the registered {@link IDaemon}s that a new LRC has started up via the
	 * {@link IDaemon#lrcCreated(LRC)} method.
	 */
	public synchronized void notifyLrcCreated( LRC lrc )
	{
		for( IDaemon daemon : daemons.values() )
			daemon.lrcCreated( lrc );
	}

	/**
	 * Notify all the registered {@link IDaemon}s that a previously existing LRC is about to be
	 * destroyed via the {@link IDaemon#lrcDestroyed(LRC)} method.
	 */
	public synchronized void notifyLrcDestroyed( LRC lrc )
	{
		for( IDaemon daemon : daemons.values() )
			daemon.lrcDestroyed( lrc );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	
}
