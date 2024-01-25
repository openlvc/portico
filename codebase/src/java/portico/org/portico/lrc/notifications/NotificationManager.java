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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.portico.container.Container;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.model.ObjectModel;
import org.portico2.common.services.federation.msg.RoleCall;

/**
 * The {@link NotificationManager} is responsible for aggregating {@link INotificationListener}s
 * and informing them when notification events occurs.
 * <p/>
 * When a new LRC is created, a new {@link NotificationManager} will be created inside it. The
 * first time a NotificationManager is created it will scan the {@link Container} plug-in path
 * for any classes with the {@link NotificationListener} annotation. These classes will be
 * stored internally and then applied to all NotificationManager's that are created.
 */
public class NotificationManager
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/** The set of all known notification listeners */
	// this is initially null so that is triggers the locateAllListeners static method
	private static Set<Class<?>> LISTENER_CLASSES = null;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private List<INotificationListener> highPriority;
	private List<INotificationListener> normalPriority;
	private List<INotificationListener> lowPriority;
	
	// This is the list that is iterated over when listeners are called, it is a composite
	// of the three other lists, regenerated each time a new listener is added.
	// Obviously it's not the more efficient way to do it, but we expect a small number of
	// listeners, so we'll just live with inefficiently in favor of conceptual simplicity
	private List<INotificationListener> listeners;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	/**
	 * Creates a new NotificationManager and scans the current classpath for any classes with the
	 * {@link NotificationListener} annotation. Any found will be inspected to make sure they
	 * implement the {@link INotificationListener} interface, and if so, a new instance will be
	 * constructed (through a no-arg constructor) and added to the list of listeners according to
	 * its {@link NotificationListener#priority() priority}.
	 * <p/>
	 * If there is a problem locating the listener classes or instantiating ANY of them, 
	 */
	private NotificationManager() throws JConfigurationException
	{
		// somewhere to store the listeners
		this.highPriority = new LinkedList<INotificationListener>();
		this.normalPriority = new LinkedList<INotificationListener>();
		this.lowPriority = new LinkedList<INotificationListener>();
		this.listeners = new LinkedList<INotificationListener>();
		

		// instantiate each of the listeners
		for( Class<?> clazz : LISTENER_CLASSES )
		{
   			// get the annotation and check that the class implements INotifcationListener
   			NotificationListener annotation = getAnnotation( clazz );
   			Class<? extends INotificationListener> listenerClass =
   				clazz.asSubclass( INotificationListener.class );
   			
   			// create a new instance of the listener
   			try
   			{
   				INotificationListener listener = listenerClass.getDeclaredConstructor().newInstance();
   				addListener( annotation.priority(), listener );
   			}
   			catch( Exception e )
   			{
   				throw new JConfigurationException(
   				    "Error instantiating instance of INotificationListener class ["+
   				    listenerClass.getCanonicalName()+"]", e );
   			}
		}
	}

	private NotificationListener getAnnotation( Class<?> clazz )
	{
		// check that the class is an INotificationHandler
		if( INotificationListener.class.isAssignableFrom(clazz) == false )
		{
			throw new JConfigurationException( "Class [" + clazz.getCanonicalName() +
			    "] does not implement INotificationListener (but has NotificationListener "+
			    "annotation): listener not loaded" );
		}
		
		// convert the class to a handler class
		return clazz.getAnnotation( NotificationListener.class );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	/**
	 * Adds a new listener to the end of the list for the given {@link Priority}
	 */
	public void addListener( Priority priority, INotificationListener listener )
	{
		switch( priority )
		{
			case HIGH: highPriority.add(listener); break;
			case NORMAL: normalPriority.add(listener); break;
			case LOW: lowPriority.add(listener); break;
		}

		// regenerate the composite listeners list
		listeners.clear();
		listeners.addAll( highPriority );
		listeners.addAll( normalPriority );
		listeners.addAll( lowPriority );
	}
	
	/**
	 * Add a new {@link INotificationListener} to the end of the list.
	 */
	public void addListener( INotificationListener listener )
	{
		addListener( Priority.NORMAL, listener );
	}
	
	/**
	 * Add a new {@link INotificationListener} at the given index in the list. If the index
	 * is past the end of the list, the listener will just be added to the end.
	 */
	public void addListener( int index, INotificationListener listener )
	{
		this.listeners.add( index, listener );
	}
	
	public List<INotificationListener> getAllListeners()
	{
		return Collections.unmodifiableList( listeners );
	}
	
	//////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// Notification Methods ////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////
	public void localFederateJoinedFederation( int federateHandle,
	                                           String federateName,
	                                           String federateType,
	                                           String federationName,
	                                           ObjectModel fom )
	{
		for( INotificationListener listener : listeners )
		{
			try
			{
				listener.localFederateJoinedFederation( federateHandle,
				                                        federateName,
				                                        federateType,
				                                        federationName,
				                                        fom );
			}
			catch( Throwable throwable )
			{
				// log and continue
				throwable.printStackTrace();
			}
		}
	}

	public void remoteFederateJoinedFederation( RoleCall federateState )
	{
		for( INotificationListener listener : listeners )
		{
			try
			{
				listener.remoteFederateJoinedFederation( federateState );
			}
			catch( Throwable throwable )
			{
				// log and continue
				throwable.printStackTrace();
			}
		}
	}

	public void localFederateResignedFromFederation()
	{
		for( INotificationListener listener : listeners )
		{
			try
			{
				listener.localFederateResignedFromFederation();
			}
			catch( Throwable throwable )
			{
				// log and continue
				throwable.printStackTrace();
			}
		}
	}

	public void remoteFederateResignedFromFederation( int federateHandle, String federateName )
	{
		for( INotificationListener listener : listeners )
		{
			try
			{
				listener.remoteFederateResignedFromFederation( federateHandle, federateName );
			}
			catch( Throwable throwable )
			{
				// log and continue
				throwable.printStackTrace();
			}
		}
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
	public static synchronized final NotificationManager newNotificationManager()
	{
		if( LISTENER_CLASSES == null )
			LISTENER_CLASSES = new HashSet<Class<?>>();
		
		return new NotificationManager();
	}
	
}
