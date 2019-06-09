/*
 *   Copyright 2018 The Portico Project
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
package org.portico2.forwarder.firewall;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico2.common.configuration.ForwarderConfiguration;
import org.portico2.forwarder.Direction;
import org.portico2.forwarder.tracking.StateTracker;

/**
 * The {@link Firewall} is used to decide whether or not data messages should be allowed
 * to pass through the forwarder. It takes the configuration from the RID file in the form
 * of class names (object and interaction) that it will accept updates for. On each data
 * message, it then uses the {@link StateTracker} to resolve handles into a qualified name
 * and will match that against the rules from the configuration file.</p>
 * 
 * If the types overlap with a configured rule, the firewall advises of the match.
 */
public class Firewall
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private ForwarderConfiguration configuration;
	private StateTracker stateTracker;
	private Logger logger;
	private boolean enabled;
	
	private Map<String,Pattern> allowedImportObjects;
	private Map<String,Pattern> allowedImportInteractions;
	private Map<String,Pattern> allowedExportObjects;
	private Map<String,Pattern> allowedExportInteractions;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Firewall( ForwarderConfiguration configuration, StateTracker stateTracker, Logger logger )
	{
		this.stateTracker = stateTracker;
		this.configuration = configuration;
		this.logger = LogManager.getFormatterLogger( logger.getName()+".firewall" );

		// is the firewall even enabled?
		this.enabled = configuration.isFirewallEnabled();
		
		// extract the allows import/export class configuration
		this.allowedImportObjects      = new HashMap<>();
		this.allowedImportInteractions = new HashMap<>();
		this.allowedExportObjects      = new HashMap<>();
		this.allowedExportInteractions = new HashMap<>();
		
		buildPatternMap( configuration.getAllowedImportObjects(), allowedImportObjects );
		buildPatternMap( configuration.getAllowedExportObjects(), allowedExportObjects );
		buildPatternMap( configuration.getAllowedImportInteractions(), allowedImportInteractions );
		buildPatternMap( configuration.getAllowedExportInteractions(), allowedExportInteractions );
		logConfiguration();
	}
	
	private void buildPatternMap( Set<String> strings, Map<String,Pattern> store )
	{
		for( String temp : strings )
		{
			//  Case Insensitive: (?i)
			// Wildcard Sequence: .*?
			String regex = "(?i)"+temp.replace( "*" , ".*?" );
			store.put( temp, Pattern.compile(regex) );
		}
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	/**
	 * Should the given update be accepted. We will look at:
	 * 
	 * <ul>
	 *   <li><b>Direction</b> of travel, so we know which ruleset to match against.</li>
	 *   <li>Whether the message contains an <b>object/attribute</b> update (or an interaction,
	 *       represented as false).</li>
	 *   <li>The <b>federation handle</b> so we can find the federation this update is inside
	 *       and then look up its object model to resolve names against.</li>
	 *   <li>The <b>class handle</b> (object or interaction depending on above) to find the name
	 *       for and then match against.</li>
	 * </ul>
	 * 
	 * @param direction        Direction the message is travelling so we can use correct rule set
	 * @param objectUpdate     Is this an object update (true) or an interaction (false)
	 * @param federationHandle The handle of the federation we're in (so we can find the FOM for it)
	 * @param classHandle      The object/interaction class handle to lookup the qualified name for
	 *                         and then match against the rule set.
	 * @return True if the message should be accepted and forwarded; false otherwise
	 */
	public final boolean acceptUpdate( Direction direction,
	                                   boolean objectUpdate,
	                                   int federationHandle,
	                                   int classHandle )
	{
		// if the firewall is off, accept the message
		if( !enabled )
			return true;

		// find the qualified name for the class
		String qualifiedName = objectUpdate ? stateTracker.resolveObjectHandleToClassName(federationHandle,classHandle) :
		                                      stateTracker.resolveInteractionClass(federationHandle,classHandle) ;
		
		// figure out the pattern set we need to match against depending on whether the
		// flow is upstream or downstream, and whether it is an interaction of reflection
		Map<String,Pattern> patterns = null;
		if( direction == Direction.Upstream )
		{
			if( objectUpdate ) patterns = allowedExportObjects;
			else               patterns = allowedExportInteractions;
		}
		else
		{
			if( objectUpdate ) patterns = allowedImportObjects;
			else               patterns = allowedImportInteractions;
		}
		
		// check all the patterns to see if we have a match
		for( String rule : patterns.keySet() )
		{
			Pattern pattern = patterns.get( rule ); 
			if( pattern.matcher(qualifiedName).matches() )
			{
				if( logger.isTraceEnabled() )
				{
    				logger.trace( "[ACCEPT] %s (%s) Firewall has matched %s against rule %s",
    				              direction.flowDirection(),
    				              objectUpdate ? "Reflection" : "Interaction",
    				              qualifiedName,
    				              rule );
				}
				return true;
			}
		}
		
		// if we get here, there was no match, so it's denial time
		if( logger.isTraceEnabled() )
		{
    		logger.trace( "[REJECT] %s (%s) Firewall has blocked %s",
    		              direction.flowDirection(),
    		              objectUpdate ? "Reflection" : "Interaction",
    		              qualifiedName );
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public final boolean isEnabled()
	{
		return this.enabled;
	}

	private void logConfiguration()
	{
		logger.info( "Firewall Status: "+(enabled? "ENABLED":"DISABLED") );
		logger.debug( "Firewall Configuration" );
		logger.debug( "  [Import - Objects]" );
		logConfigurationDetail( allowedImportObjects.keySet() );
		logger.debug( "  [Export - Objects]" );
		logConfigurationDetail( allowedExportObjects.keySet() );
		logger.debug( "  [Import - Interactions]" );
		logConfigurationDetail( allowedImportInteractions.keySet() );
		logger.debug( "  [Export - Interactions]" );
		logConfigurationDetail( allowedExportInteractions.keySet() );
		logger.debug( "" );
	}

	private void logConfigurationDetail( Set<String> list )
	{
		if( list.isEmpty() )
		{
			logger.debug( "    - None" );
			return;
		}
		
		for( String string : list )
			logger.debug( "    - %s", string );
	}

	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
