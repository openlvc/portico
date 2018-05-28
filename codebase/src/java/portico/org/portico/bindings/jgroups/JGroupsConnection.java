/*
 *   Copyright 2015 The Portico Project
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
package org.portico.bindings.jgroups;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.portico.bindings.ConnectedRoster;
import org.portico.bindings.IConnection;
import org.portico.lrc.LRC;
import org.portico.lrc.PorticoConstants;
import org.portico.lrc.compat.JConfigurationException;
import org.portico.lrc.compat.JFederateNotExecutionMember;
import org.portico.lrc.compat.JRTIinternalError;
import org.portico.lrc.model.ModelMerger;
import org.portico.lrc.model.ObjectModel;
import org.portico.utils.logging.Log4jConfigurator;
import org.portico.utils.messaging.PorticoMessage;
import org.portico2.common.services.federation.msg.CreateFederation;
import org.portico2.common.services.federation.msg.DestroyFederation;
import org.portico2.common.services.federation.msg.JoinFederation;
import org.portico2.common.services.federation.msg.ResignFederation;

public class JGroupsConnection implements IConnection
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private boolean running;
	protected Logger logger;
	private Map<String,Federation> federations;

	// the LRC we are providing the connection for
	private Federation joinedFederation;
	private LRC lrc;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public JGroupsConnection()
	{
		this.running = false;
		this.logger = null;            // set on configure()
		this.federations = new HashMap<String,Federation>();

		// Federation Management
		this.joinedFederation = null;  // set on joinFederation(), removed on resignFederation()
		this.lrc = null; // The LRC link is populated when we join a federation
		                 // We direct incoming messages for the federation to the joined LRC

	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------

	///////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////// Lifecycle Methods //////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * <i>This method is called by the Portico infrastructure during setup.</i>
	 * <p/>
	 * Called right after the connection has been instantiated. Let's the connection peform
	 * setup. If there is a configuration problem a {@link JConfigurationException} will be
	 * thrown.
	 * 
	 * @param lrc The LRC that this connection is servicing
	 * @param properties Additional configuration properties provided by the container
	 */
	public void configure( LRC lrc, Map<String,Object> properties )
		throws JConfigurationException
	{
		this.lrc = lrc;
		this.logger = LogManager.getFormatterLogger( "portico.lrc.jgroups" );
		// set the appropriate level for the jgroups logger, by default we will turn it off
		String jglevel = System.getProperty( Configuration.PROP_JGROUPS_LOGLEVEL, "OFF" );
		Log4jConfigurator.setLevel( jglevel, "org.jgroups" );
	}
	
	/**
	 * <i>This method is called by the Portico infrastructure during setup.</i>
	 * <p/>
	 * When it is time for the kernel to connect to the RTI/federate/etc... and to start accepting
	 * incoming messages, while being ready to send outgoing messages, this method is called. The
	 * connection implementations should use it to connect to network (or whatever communications
	 * mechanism is being used). This should include any discovery of remote components (such as
	 * the discovery of an RTI by federates).
	 */
	public void connect() throws JRTIinternalError
	{
		if( this.running )
			return;
		
		this.running = true;
		logger.info( "jgroups connection is up and running" );
	}
	
	/**
	 * <i>This method is called by the Portico infrastructure during shutdown.</i>
	 * <p/>
	 * When the kernel is ready to shutdown, it will call this method, signalling to the connection
	 * that it should disconnect and do any shutdown and cleanup necessary.
	 */
	public void disconnect() throws JRTIinternalError
	{
		if( this.running == false )
		{
			logger.info( "jgroups connection is already disconnected" );
			return;
		}

		logger.info( "jgroups connection is disconnecting..." );
		
		// for each federation we're connected to, disconnect from it
		for( Federation federation : federations.values() )
			federation.disconnect();

		federations.clear();
		logger.info( "jgroups connection has disconnected" );
	}
	
	///////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Message sending methods ///////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Broadcast the given message out to all participants in a federation asynchronously.
	 * As soon as the message has been received for processing or sent, this method is free
	 * to return. No response will be waited for.
	 * 
	 * @param message The message to broadcast
	 * @throws Exception If there was an error when sending the message
	 */
	public void broadcast( PorticoMessage message ) throws JFederateNotExecutionMember,
	                                                       JRTIinternalError
	{
		validateConnected();
		joinedFederation.send( message );
	}
	
	/**
	 * This method should be used when a message has to be sent and then time for responses to
	 * be broadcast back is needed before moving on. In this case, the connection will broadcast
	 * the message and then sleep for an amount of time appropriate based on the underlying
	 * comms protocol in use. For example, the JVM connection won't sleep for long, but a connection
	 * sending information over a network should wait longer.
	 * 
	 * @param message The message to broadcast
	 * @throws Exception If there was an error when sending the message
	 */
	public void broadcastAndSleep( PorticoMessage message ) throws Exception
	{
		validateConnected();
		joinedFederation.send( message );
		PorticoConstants.sleep( Configuration.RESPONSE_TIMEOUT );
	}

	/**
	 * Runs a simple check to make sure this connection is connected to a federation. If it isn't
	 * an exception is thrown, if it is, the method will happily return.
	 */
	private void validateConnected() throws JFederateNotExecutionMember
	{
		if( joinedFederation == null )
			throw new JFederateNotExecutionMember( "Connection has not been joined to a federation" );
	}

	///////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////// bootstrapping methods ////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds and returns the caches {@link Federation} with the given name. If we don't have
	 * it cached we connect to it and return. We cache these so that we only have to connect
	 * to a federation and find the co-ordinator once.
	 * 
	 * @throws Exception If there is a problem connecting to the channel
	 */
	private Federation findFederation( String federationName ) throws Exception
	{
		// NOTE: We maintain a map of all connections we've ever queried for. We have to
		//       join a JGroups channel before we can query anything, so we prefer to only
		//       to this once. When joining a channel there may/may not be a federation
		//       present inside. To find out, we need to source the Oracle...
		if( federations.containsKey(federationName) )
			return federations.get(federationName);

		// we don't know about it
		Federation federation = new Federation( federationName );
		federation.connect();
		federations.put( federationName, federation );
		return federation;
	}

	/**
	 * Find the channel for the federation we are trying to create (name is used)
	 * and then attempt to install a federation in it. If there is no existing
	 * federation in it, one will be created. If there is an exception will be thrown.
	 */
	public void createFederation( CreateFederation createMessage ) throws Exception
	{
		Federation federation = findFederation( createMessage.getFederationName() );
		federation.sendCreateFederation( createMessage.getModel() );
	}

	/**
	 * Join the federation of the given name. This will find the channel with the same
	 * name as the federation and connect to it. It will then attempt to mark this execution
	 * as a federate within that channel (as opposed to just a regular member).
	 * 
	 * == Federate Names ==
	 * A federation can optionally require that each federate have a unique name (RID option).
	 * If this is not the case, Portico will always ensure that federates do have unique names
	 * by modifying newly joining duplicates. If unique names are enforced and we try to join
	 * with an existing name, an exception is thrown.
	 * 
	 * == FOM Modules ==
	 * If there are additional FOM modules, we attempt a dry-run merge first to identify
	 * any issues. Should that be successful, we join and then apply the changes to our
	 * local copy of the FOM. The Role Call semantics that executes outside of the connection
	 * after we join handles the job of dispersing the new module information to everyone
	 * else (who then also apply them locally). 
	 */
	public ConnectedRoster joinFederation( JoinFederation joinMessage ) throws Exception
	{
		// connect to the federation channel if we are not already
		Federation federation = findFederation( joinMessage.getFederationName() );
		
		// validate that our FOM modules can be merged successfully with the existing FOM first
		logger.debug( "Validate that ["+joinMessage.getParsedJoinModules().size()+
		              "] modules can merge successfully with the existing FOM" );
		ModelMerger.mergeDryRun( federation.getManifest().getFom() , joinMessage.getParsedJoinModules() );
		logger.debug( "Modules can be merged successfully, continue with join" );

		// tell the channel that we're joining the federation
		String joinedName = federation.sendJoinFederation( joinMessage.getFederateName(), this.lrc );
		
		// the joined name could be different from what we asked for, so update the request
		// to make sure it is correct
		joinMessage.setFederateName( joinedName ); 
		
		// now that we've joined a federation, store it here so we can route messages to it
		this.joinedFederation = federation;
		
		// We have to merge the FOMs together here before we return to the Join handler and
		// a RoleCall is sent out. We do this because although we receive our own RoleCall
		// notice (with the additional modules) we won't process it as we can't tell if it's
		// one we sent out because we joined (and thus need to merge) or because someone else
		// joined. Additional modules will only be present if it is a new join, so
		// we could figure it out that way, but that will cause redundant merges for the JVM
		// binding (as all connections share the same object model reference). To cater to the
		// specifics of this connection it's better to put the logic in the connection rather than
		// in the generic-to-all-connections RoleCallHandler. Long way of saying we need to merge
		// in the additional join modules that were provided here. F*** IT! WE'LL DO IT LIVE!
		if( joinMessage.getParsedJoinModules().size() > 0 )
		{
			logger.debug( "Merging "+joinMessage.getParsedJoinModules().size()+
			              " additional FOM modules that we receive with join request" );

			ObjectModel fom = federation.getManifest().getFom();
			fom.unlock();
			federation.getManifest().setFom( ModelMerger.merge(fom,joinMessage.getParsedJoinModules()) );
			fom.lock();
		}

		// create and return the roster
		return new Roster( federation.getManifest().getLocalFederateHandle(),
		                   federation.getManifest().getFederateHandles(),
		                   federation.getManifest().getFom() );
	}

	/**
	 * Resign ourselves from the federation we are currently connected to. This will not
	 * disconnect us from the channel, but will rather just mark us as no longer being a
	 * federate (only a channel member).
	 * 
	 * An exception is thrown if any of the checks we run (such as whether we are infact
	 * even connected to a federation at all!) fail.
	 */
	public void resignFederation( ResignFederation resignMessage ) throws Exception
	{
		// make sure we're joined to the federation
		if( joinedFederation == null ||
			joinedFederation.getManifest().isLocalFederateJoined() == false )
		{
			throw new JFederateNotExecutionMember( "Federate ["+resignMessage.getFederateName()+
			                                       "] not joined to ["+
			                                       resignMessage.getFederationName()+"]" );
		}
		
		// send out the resign notification
		joinedFederation.sendResignFederation( resignMessage );
		
		// all happy, as we're no longer joined, set out joined channel to null
		joinedFederation = null;
	}
	
	/**
	 * Federation destroying is a bit of a catch-22. We need to connect to the channel
	 * that the federation is operating on in order to determine whether a federation is
	 * even active at all (which it may well not be). So in this case we connect to the
	 * channel and then if there is a federation running in there, we ask that it be
	 * destroyed by removing the contained FOM.
	 * 
	 * If there are members of the channel who are still currently federates, then this
	 * call will fail and thrown an exception.
	 * 
	 *    **NOTE** If we ever ask to destroy a federation, we will automatically
	 *             disconnect from the channel when done. Fair chance we don't 
	 *             care about it any more once we try a destroy.
	 */
	public void destroyFederation( DestroyFederation destroyMessage ) throws Exception
	{
		// connect to the channel if we are not already
		Federation federation = findFederation( destroyMessage.getFederationName() );
		try
		{
			federation.sendDestroyFederation();
		}
		finally
		{
			// We put this here because the above call may throw an exception if there
			// are federates still connected (and thus it can't destroy the federation).
			// That exception would in turn cascade out and prevent us from disconnecting
			// unless we did something about it!
			//
			// And now I'm removing this because it is causing problems with the unit tests
			// when using the JGroups binding. The federation is being successfully destroyed,
			// but we intend to keep the connection open, which this code is preventing. Still
			// can't 100% recall the use-case reason this was here, but keeping until memory jogged
			//federation.disconnect();
			//federations.remove( federation );
		}
	}

	/**
	 * For the JGroups binding this currently returns an empty string. It is difficult
	 * for us to comply here. Federations are ad-hoc and not run through any central
	 * source, so tehre is no central list. The only way to find out if a federation
	 * exists it to connect to the channel with the same name and see!
	 */
	public String[] listActiveFederations() throws Exception
	{
		return new String[]{};
	}
	
	//----------------------------------------------------------
	//                     STATIC METHODS
	//----------------------------------------------------------
}
