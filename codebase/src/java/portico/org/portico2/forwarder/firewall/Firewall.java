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

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public Firewall( ForwarderConfiguration configuration, StateTracker stateTracker, Logger logger )
	{
		this.stateTracker = stateTracker;
		this.configuration = configuration;
		this.logger = logger;

		this.enabled = false;
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	////////////////////////////////////////////////////////////////////////////////////////
	///  Accessors and Mutators   //////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////
	public final boolean isEnabled()
	{
		return this.enabled;
	}

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
		String qualifiedName = objectUpdate ? stateTracker.resolveObjectClass(federationHandle,classHandle) :
		                                      stateTracker.resolveInteractionClass(federationHandle,classHandle) ;
		
		if( qualifiedName == null )
			return false;
		
		// TODO Pattern match on the name (with the appropriate set) 
		return true;
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
